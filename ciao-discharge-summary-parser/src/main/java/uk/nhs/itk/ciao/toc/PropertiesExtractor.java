package uk.nhs.itk.ciao.toc;

import java.util.Map;

public interface PropertiesExtractor<T> {
	Map<String, Object> extractProperties(T document) throws UnsupportedDocumentTypeException;
}
