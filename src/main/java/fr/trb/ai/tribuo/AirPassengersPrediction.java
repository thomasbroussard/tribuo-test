package fr.trb.ai.tribuo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

import org.tribuo.Model;
import org.tribuo.MutableDataset;
import org.tribuo.Trainer;
import org.tribuo.common.tree.DecisionTreeTrainer;
import org.tribuo.common.tree.RandomForestTrainer;
import org.tribuo.data.columnar.FieldProcessor;
import org.tribuo.data.columnar.RowProcessor;
import org.tribuo.data.columnar.processors.field.DoubleFieldProcessor;
import org.tribuo.data.columnar.processors.response.FieldResponseProcessor;
import org.tribuo.data.csv.CSVDataSource;
import org.tribuo.regression.RegressionFactory;
import org.tribuo.regression.Regressor;
import org.tribuo.regression.ensemble.AveragingCombiner;
import org.tribuo.regression.evaluation.RegressionEvaluator;
import org.tribuo.regression.rtree.CARTRegressionTrainer;
import org.tribuo.regression.rtree.impurity.MeanSquaredError;

import fr.trb.ai.tribuo.data.processors.GenericFieldProcessor;
import fr.trb.ai.tribuo.data.processors.LabelEncodingProcessor;
import static org.tribuo.common.tree.AbstractCARTTrainer.MIN_EXAMPLES;

public class AirPassengersPrediction {

	public static void main(String[] args) throws IOException {
		var trainPath = Paths.get("src/main/resources/airpassengers/train.csv");
		var testPath = Paths.get("src/main/resources/airpassengers/test.csv");
		var csvLines = Files.readAllLines(trainPath, StandardCharsets.UTF_8);
		csvLines.stream().limit(5).forEach(System.out::println);
		var fieldProcessors = new LinkedHashMap<String, FieldProcessor>();

		//--- data preparation ---

		//Maps fields to features with a custom mapper (to be improved)
		//use date mapping (custom)
		fieldProcessors.put("DateOfDeparture", new GenericFieldProcessor("DateOfDeparture", FieldProcessor.GeneratedFeatureType.CATEGORICAL) {
			@Override
			public void mapValue(String value, Map<String, Number> values) {
				LocalDate date = LocalDate.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
				values.put("month", date.getMonthValue());
				values.put("day", date.getDayOfMonth());
			}
		});

		//use label encoding (custom)
		LabelEncodingProcessor departure = new LabelEncodingProcessor("Departure");
		fieldProcessors.put("Departure", departure);
		LabelEncodingProcessor arrival = new LabelEncodingProcessor("Arrival");
		fieldProcessors.put("Arrival", arrival);

		//use double mapping (built-in)
		fieldProcessors.put("WeeksToDeparture", new DoubleFieldProcessor("WeeksToDeparture"));

		//defines target feature
		FieldResponseProcessor<Regressor> responseProcessor = new FieldResponseProcessor<>("log_PAX","0", new RegressionFactory());

		//defines how a row should be treated
		RowProcessor<Regressor> regressorRowProcessor = new RowProcessor<>(responseProcessor, fieldProcessors);

		//defines the train source
		var trainSource = new CSVDataSource<>(trainPath,regressorRowProcessor,true);
		MutableDataset<Regressor> examples = new MutableDataset<>(trainSource);
		//displays the mapping from the examples parsing
		System.out.println(departure.getLabelEncodingMap());

		//defines the test source
		var testSource = new CSVDataSource<>(testPath,regressorRowProcessor,true);
		MutableDataset<Regressor> testExamples = new MutableDataset<>(testSource);

		//--- training ---

		//configure random forest for regression
		DecisionTreeTrainer<Regressor> treeTrainer = new CARTRegressionTrainer(Integer.MAX_VALUE,
				MIN_EXAMPLES, 0.0f, 0.5f, false, new MeanSquaredError(), Trainer.DEFAULT_SEED);
		RandomForestTrainer<Regressor> trainer = new RandomForestTrainer<Regressor>(treeTrainer, new AveragingCombiner(),10);

		//train
		Model<Regressor> model = trainer.train(examples);

		//evaluate
		var evaluator = new RegressionEvaluator();
		var evaluation = evaluator.evaluate(model,testExamples);
		System.out.println(evaluation);

	}
}
