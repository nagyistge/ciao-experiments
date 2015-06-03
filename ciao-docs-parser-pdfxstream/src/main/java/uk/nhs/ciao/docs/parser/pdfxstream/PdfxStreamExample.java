package uk.nhs.ciao.docs.parser.pdfxstream;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import com.google.common.io.Closeables;
import com.snowtide.PDF;
import com.snowtide.pdf.Document;
import com.snowtide.pdf.OutputTarget;
import com.snowtide.pdf.Page;
import com.snowtide.pdf.util.TableUtils;

public class PdfxStreamExample {
	public static void main(final String[] args) throws IOException {
		new PdfxStreamExample().run();
	}
	
	public void run() throws IOException {
		for (final String resourceName: Arrays.asList("Example.pdf", "Example2.pdf", "Example3.pdf")) {
			printTextContent(resourceName);
			printTabularContent(resourceName);
		}
	}
	
	private void printTextContent(final String resourceName) throws IOException {
		InputStream in = null;
		Document pdf = null;
		try {
			in = getClass().getResourceAsStream(resourceName);
			pdf = PDF.open(in, resourceName);
			
			final StringBuilder text = new StringBuilder();
		    pdf.pipe(new OutputTarget(text));
		    System.out.println("********************************************");
		    System.out.println("Text content of:" + resourceName);
		    System.out.println("********************************************");
		    System.out.println(text);
		    System.out.println();
		} finally {
			Closeables.close(pdf, true);
			Closeables.closeQuietly(in);
		}
	}
	
	private void printTabularContent(final String resourceName) throws IOException {
		InputStream in = null;
		Document pdf = null;
		try {			
			in = getClass().getResourceAsStream(resourceName);
			pdf = PDF.open(in, resourceName);
		    
		    System.out.println("********************************************");
		    System.out.println("Tabular content of:" + resourceName);
		    System.out.println("********************************************");
		    
		    int pageNumber = 1;
		    for (final Page page: pdf.getPages()) {
		    	System.out.println("**** Page " + (pageNumber++));
		    	for (final String line: TableUtils.convertTablesToCSV(page, ',')) {
		    		System.out.println(line);
		    	}
			}
		    
		    System.out.println();
		} finally {
			Closeables.close(pdf, true);
			Closeables.closeQuietly(in);
		}
	}
}
