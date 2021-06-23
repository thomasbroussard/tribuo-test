package fr.trb.ai.tribuo.data.processors;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.tribuo.data.columnar.FieldProcessor;

public class DateFieldProcessor extends GenericFieldProcessor implements FieldProcessor {


	private final String format;

	public DateFieldProcessor(String fieldName, String format) {
		super(fieldName, GeneratedFeatureType.CATEGORICAL);
		this.format = format;
	}



	public void mapValue(String value, Map<String,Number> values){
		LocalDateTime dateTime = LocalDateTime.parse(value, DateTimeFormatter.ofPattern(this.format));
		values.put("month",dateTime.getMonthValue());
		values.put("day", dateTime.getDayOfMonth());
		values.put("hour", dateTime.getHour());
		values.put("minute", dateTime.getMinute());
	}


	@Override
	public FieldProcessor copy(String newFieldName) {
		return null;
	}


}
