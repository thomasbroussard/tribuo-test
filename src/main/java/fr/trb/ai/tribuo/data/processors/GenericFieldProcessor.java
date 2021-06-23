package fr.trb.ai.tribuo.data.processors;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.tribuo.data.columnar.ColumnarFeature;
import org.tribuo.data.columnar.FieldProcessor;
import org.tribuo.data.columnar.processors.field.DoubleFieldProcessor;

import com.oracle.labs.mlrg.olcut.provenance.ConfiguredObjectProvenance;
import com.oracle.labs.mlrg.olcut.provenance.impl.ConfiguredObjectProvenanceImpl;

public abstract class GenericFieldProcessor implements FieldProcessor {

	protected final String fieldName;
	protected final FieldProcessor.GeneratedFeatureType type;

	public GenericFieldProcessor(String fieldName, FieldProcessor.GeneratedFeatureType type){
		this.fieldName = fieldName;
		this.type = type;
	}

	@Override
	public String getFieldName() {
		return this.fieldName;
	}

	@Override
	public GeneratedFeatureType getFeatureType() {
		return this.type;
	}


	@Override
	public final List<ColumnarFeature> process(String value) {
		Map<String,Number> doubleMap = new LinkedHashMap<>();
		mapValue(value, doubleMap);
		List<ColumnarFeature> features = new ArrayList<>();
		for (Map.Entry<String, Number> entry : doubleMap.entrySet()){
			features.add(new ColumnarFeature(fieldName, entry.getKey(), entry.getValue().doubleValue()));
		}
		return features;

	}

	public abstract void mapValue(String value, Map<String,Number> values);


	@Override
	public String toString() {
		return "DoubleFieldProcessor(fieldName=" + getFieldName() + ")";
	}

	@Override
	public ConfiguredObjectProvenance getProvenance() {
		return new ConfiguredObjectProvenanceImpl(this,"FieldProcessor");
	}

	@Override
	public FieldProcessor copy(String newFieldName) {
		return null;
	}
}
