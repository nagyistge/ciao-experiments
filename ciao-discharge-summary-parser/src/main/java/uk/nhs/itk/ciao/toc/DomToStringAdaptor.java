package uk.nhs.itk.ciao.toc;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;

import uk.nhs.itk.ciao.io.DomWriter;

import com.google.common.base.Preconditions;

/**
 * A delegate document reader which converts an intermediate DOM into a string representation.
 * 
 * @see DomWriter
 */
public class DomToStringAdaptor implements DischargeSummaryReader<String> {
	private final DischargeSummaryReader<? extends Document> delegate;
	private final DomWriter writer;
	
	public static <T extends Document> DomToStringAdaptor adapt(final DischargeSummaryReader<T> delegate) {
		return new DomToStringAdaptor(delegate);
	}
	
	private DomToStringAdaptor(final DischargeSummaryReader<? extends Document> delegate) {
		this.delegate = Preconditions.checkNotNull(delegate);
		this.writer = new DomWriter();
	}
	
	@Override
	public String readDocument(final InputStream in) throws IOException {
		try {
			final Document document = delegate.readDocument(in);
			return writer.toString(document);
		} catch (TransformerException e) {
			throw new IOException(e);
		}
	}
}
