package uk.nhs.itk.ciao.toc;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.parser.pdf.PDFParserConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * Application to parse and extract property values from discharge summary PDF documents
 */
public class DischargeSummaryParser implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(DischargeSummaryParser.class);
	
	public static void main(final String[] args) throws Exception {
		final File inputFolder = new File(args[0]);
		final File outputFolder = new File(args[1]);
		new DischargeSummaryParser(inputFolder, outputFolder).run();
	}
	
	private final File inputFolder;
	private final File outputFolder;
	private DischargeSummaryReader<String> reader;
	
	public DischargeSummaryParser(final File inputFolder, final File outputFolder) throws ParserConfigurationException {
		this.inputFolder = Preconditions.checkNotNull(inputFolder);
		this.outputFolder = Preconditions.checkNotNull(outputFolder);
		
		Preconditions.checkState(inputFolder.isDirectory());
		outputFolder.mkdirs();
		Preconditions.checkState(outputFolder.isDirectory());
		
		final PDFParser pdfParser = new PDFParser();
		final PDFParserConfig parserConfig = pdfParser.getPDFParserConfig();
		parserConfig.setSortByPosition(true);
		
		 this.reader = DomToJsonAdaptor.adapt(new TikaDischargeSummaryReader(pdfParser));
	}
	
	@Override
	public void run() {
		int count = 0;
		
		for (final File file: inputFolder.listFiles()) {
			if (!file.isFile() || !file.canRead()) {
				continue;
			}
			
			InputStream in = null;
			Writer writer = null;
			try {
				LOGGER.info("Parsing file: {}", file);
				in = new FileInputStream(file);
				final String result = reader.readDocument(in);

				final File outputFile = new File(outputFolder, file.getName());				
				try {					
					writer = new FileWriter(outputFile);
					writer.write(result);
					writer.flush();
					count++;
				} catch (IOException e) {
					LOGGER.warn("Unable to write to output file {}", outputFile);
				}				
			} catch (IOException e) {
				LOGGER.warn("Unable to parse file: {}", file, e);
			} finally {
				closeQuietly(in);
				closeQuietly(writer);
			}
		}
		
		LOGGER.info("Succesfully parsed {} files - check {} for the output", count, outputFolder.getAbsoluteFile());
	}
	
	private static void closeQuietly(final Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (IOException e) {
				LOGGER.debug("IOException while closing resource", e);
			}
		}
	}
}
