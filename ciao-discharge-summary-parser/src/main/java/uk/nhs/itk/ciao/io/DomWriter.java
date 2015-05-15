package uk.nhs.itk.ciao.io;

import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

public class DomWriter {
	private final TransformerFactory factory;
	private int indentNumber = 2;
	
	public DomWriter() {
		this(TransformerFactory.newInstance());
	}
	
	public DomWriter(final TransformerFactory factory) {
		this.factory = factory;
		this.factory.setAttribute("indent-number", indentNumber);
	}
	
	public void setIndentNumber(final int indentNumber) {
		if (indentNumber < 0) {
			throw new IllegalArgumentException("indentNumber cannot be negative");
		}
		this.indentNumber = indentNumber;
		this.factory.setAttribute("indent-number", indentNumber);
	}
	
	public void write(final Document document, final OutputStream out) throws TransformerException {	
		transform(document, new StreamResult(out));		
	}
	
	public void write(final Document document, final Writer writer) throws TransformerException {	
		transform(document, new StreamResult(writer));		
	}
	
	public String toString(final Document document) throws TransformerException {
		final StringWriter writer = new StringWriter();		
		write(document, writer);
		return writer.toString();
	}
	
	public void transform(final Document document, final Result result) throws TransformerException {
		Transformer transformer = factory.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount",
				String.valueOf(indentNumber));
		
		transformer.transform(new DOMSource(document), result);
	}
}
