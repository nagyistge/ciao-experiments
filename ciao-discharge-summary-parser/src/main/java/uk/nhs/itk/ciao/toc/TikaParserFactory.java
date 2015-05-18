package uk.nhs.itk.ciao.toc;

import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.DefaultParser;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.parser.pdf.PDFParserConfig;

/**
 * Constructs and configures Tika parsers
 */
public class TikaParserFactory {
	private TikaParserFactory() {
		// Suppress default constructor
	}
	
	public static Parser createParser() {		
		return new AutoDetectParser(createPdfParser(), new DefaultParser());
	}
	
	private static PDFParser createPdfParser() {
		final PDFParser pdfParser = new PDFParser();
		final PDFParserConfig parserConfig = pdfParser.getPDFParserConfig();
		parserConfig.setSortByPosition(true);
		return pdfParser;
	}
}
