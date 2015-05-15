package uk.nhs.itk.ciao.toc;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import uk.nhs.itk.ciao.io.MultiCauseIOException;

/**
 * A delegate document reader which converts an intermediate DOM into a JSON representation.
 * <p>
 * Individual document structures are handled by specialized instances of {@link PropertyExtractor}
 * provided at construction time.
 */
public class DomToJsonAdaptor implements DischargeSummaryReader<String> {
	private static final Logger LOGGER = LoggerFactory.getLogger(DomToJsonAdaptor.class);
	
	private final DischargeSummaryReader<? extends Document> delegate;
	private List<PropertyExtractor> propertyExtractors;
	
	/**
	 * Adapts/wraps the specified reader to provide JSON output
	 */
	public static <T extends Document> DomToJsonAdaptor adapt(final DischargeSummaryReader<T> delegate) {
		return new DomToJsonAdaptor(delegate);
	}
	
	private DomToJsonAdaptor(final DischargeSummaryReader<? extends Document> delegate) {
		this.delegate = Preconditions.checkNotNull(delegate);
		this.propertyExtractors = Arrays.asList(
				new DischargeNotificationExtractor(),
				new EDDischargeExtractor());
	}
	
	@Override
	public String readDocument(final InputStream in) throws IOException {
		final Document document = delegate.readDocument(in);
		return interpretDocument(document).toString();
	}
	
	private JSONObject interpretDocument(final Document document) throws IOException {
		final List<Exception> suppressedExceptions = Lists.newArrayList();
		for (final PropertyExtractor propertyExtractor: propertyExtractors) {
			try {
				return propertyExtractor.extractProperties(document);
			} catch (Exception e) {
				if (LOGGER.isTraceEnabled()) {
					LOGGER.trace("Unable to interpret document using property extractor {}", propertyExtractor, e);
				}
				suppressedExceptions.add(e);
			}
		}
		
		throw new MultiCauseIOException("No property extractors could interpret the document", suppressedExceptions);
	}
	
	/**
	 * Extracts properties from an incoming document and stores them in JSON form.
	 * <p>
	 * Subclasses can supply the expected property patterns and can override the
	 * default text extraction
	 */
	private abstract static class PropertyExtractor {
		private final Deque<Key> keys;
		
		public PropertyExtractor(final String... propertyNames) {
			this(Arrays.asList(propertyNames));
		}
		
		public PropertyExtractor(final List<String> propertyNames) {
			keys = Lists.newLinkedList();
			for (int index = propertyNames.size() - 1; index >= 0; index--) {
				final String propertyName = propertyNames.get(index);
				if (keys.isEmpty()) {
					keys.addFirst(new Key(propertyName));
				} else {
					keys.addFirst(new Key(propertyName, keys.getFirst()));
				}
			}
		}
		
		/**
		 * Extracts known properties from the document and stores them in JSON form
		 *
		 * @param document The document to parse
		 * @return The properties in JSON form
		 * @throws IOException If no properties could be extracted from the document
		 */
		public JSONObject extractProperties(final Document document) throws IOException {
			final Map<String, String> properties = Maps.newLinkedHashMap();
			final String textContent = getTextContent(document);
			
			for (final Key key: keys) {
				final String value = key.findValue(textContent);
				if (!value.isEmpty()) {
					properties.put(key.name, value);
				}
			}
			
			if (properties.isEmpty()) {
				throw new IOException("No matching properties could be found");
			}
			return new JSONObject(properties);
		}
		
		protected String getTextContent(final Document document) {
			return document.getDocumentElement().getTextContent();
		}
	}
	
	/**
	 * Extracts properties from a DischargeNotification structured document
	 */
	private static class DischargeNotificationExtractor extends PropertyExtractor {
		public DischargeNotificationExtractor() {
			super("Ward",
				"Hospital Number",
				"NHS Number",
				"Ward Tel",
				"Patient Name",
				"Consultant",
				"D.O.B",
				"Speciality",
				"Date of Admission",
				"Discharged by",
				"Date of Discharge",
				"Role / Bleep",
				"Discharge Address",
				"GP");
		}
		
		/**
		 * The default text content extraction is altered because there is
		 * no known terminator for the GP property (it varies from document
		 * to document). Instead the closing html p tag is used to find 
		 * the end.
		 */
		@Override
		protected String getTextContent(final Document document) {
			final StringBuilder text = new StringBuilder();
			final NodeList nodes = document.getElementsByTagName("p");
			
			for (int index = 0; index < nodes.getLength(); index++) {		
				final String nodeText = nodes.item(index).getTextContent();
				if (nodeText.trim().startsWith("Ward")) {
					text.append(nodeText);
				} else if (text.length() > 0) {
					text.append(nodeText);
					if (nodeText.trim().startsWith("GP")) {
						break;
					}
				}
			}
			
			return text.toString();
		}
	}
	
	/**
	 * Extracts properties from an ED Discharge structured document
	 */
	private static class EDDischargeExtractor extends PropertyExtractor {
		public EDDischargeExtractor() {
			super("Re",
				"ED No",
				"DOB",
				"Hosp No",
				"Address",
				"NHS No",
				"The patient", // a bit hacky - finds end of last property
				"Seen By",
				"Investigations",
				"Working Diagnosis",
				"Referrals",
				"Outcome",
				"Comments for GP",
				"If you have any"); // also a bit hacky
		}
	}
	
	/**
	 * Represents a property key / name to find within the document
	 */
	private static class Key {
		private final String name;
		private final Pattern pattern;
		
		public Key(final String name) {
			this(name, null);
		}
		
		public Key(final String name, final Key next) {
			this.name = name;
			final String suffix = next == null ? "" :
				Pattern.quote(next.name);
			this.pattern = Pattern.compile(Pattern.quote(name) +
					"\\s*:\\s*+(.*)\\s*+" + suffix);
		}
		
		/**
		 * Attempts to find the property value in the specified text
		 * 
		 * @param text The text to search
		 * @return The associated value if one could be found, or the empty string otherwise.
		 */
		public String findValue(final String text) {
			final Matcher matcher = pattern.matcher(text);
			return matcher.find() ? matcher.group(1).trim() : "";
		}
	}
}
