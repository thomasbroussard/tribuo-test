package fr.trb.ai.tribuo.data.processors;

import java.util.LinkedHashMap;
import java.util.Map;

public class LabelEncodingProcessor extends GenericFieldProcessor {


	private final Map<String, Integer> labelEncodingMap = new LinkedHashMap<>();
	private int currentIndex = 1;

	public LabelEncodingProcessor(String fieldName) {
		super(fieldName, GeneratedFeatureType.CATEGORICAL);
	}

	@Override
	public void mapValue(String value, Map<String, Number> values) {
		Integer integer = labelEncodingMap.computeIfAbsent(value, k -> currentIndex++);
		values.put(getFieldName(), integer);
	}

	public Map<String, Integer> getLabelEncodingMap() {
		return labelEncodingMap;
	}
}
