package uk.nhs.itk.ciao.toc;

import java.io.IOException;
import java.io.InputStream;

/**
 * A reader capable of reading a discharge summary from a stream of bytes and
 * converting it into a known type.
 * 
 * @param <T> The document representation returned by this reader
 */
public interface DischargeSummaryReader<T> {
	/**
	 * Reads a discharge summary document from an stream of bytes, converting it
	 * into the specified type.
	 * <p>
	 * The input stream should is not closed by this method.
	 * 
	 * @param in The input stream to read
	 * @return The converted document
	 * @throws IOException If the document could not be read or converted
	 */
	T readDocument(InputStream in) throws IOException;
}
