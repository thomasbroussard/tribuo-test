package fr.trb.ai.tribuo;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.tribuo.Model;
import org.tribuo.MutableDataset;
import org.tribuo.Prediction;
import org.tribuo.Trainer;
import org.tribuo.classification.Label;
import org.tribuo.classification.LabelFactory;
import org.tribuo.classification.dtree.CARTClassificationTrainer;
import org.tribuo.classification.dtree.impurity.GiniIndex;
import org.tribuo.classification.ensemble.VotingCombiner;
import org.tribuo.classification.evaluation.LabelEvaluator;
import org.tribuo.common.tree.RandomForestTrainer;
import org.tribuo.data.csv.CSVLoader;
import org.tribuo.datasource.IDXDataSource;
import org.tribuo.evaluation.TrainTestSplitter;

import static org.tribuo.common.tree.AbstractCARTTrainer.MIN_EXAMPLES;

public class Main {


	public static void main(String[] args) throws IOException {

		var labelFactory = new LabelFactory();
		var csvLoader = new CSVLoader<>(labelFactory);
		var irisHeaders = new String[]{"sepalLength", "sepalWidth", "petalLength", "petalWidth", "species"};
		var irisesSource = csvLoader.loadDataSource(Paths.get("./src/main/resources/bezdekIris.data"),"species",irisHeaders);
		var irisSplitter = new TrainTestSplitter<>(irisesSource,0.7,1L);

		var trainingDataset = new MutableDataset<>(irisSplitter.getTrain());
		var testingDataset = new MutableDataset<>(irisSplitter.getTest());

		System.out.printf("Training data size = %d, number of features = %d, number of classes = %d%n",trainingDataset.size(),trainingDataset.getFeatureMap().size(),trainingDataset.getOutputInfo().size());
		System.out.printf("Testing data size = %d, number of features = %d, number of classes = %d%n",testingDataset.size(),testingDataset.getFeatureMap().size(),testingDataset.getOutputInfo().size());

		CARTClassificationTrainer treeTrainer = new CARTClassificationTrainer(Integer.MAX_VALUE,
				MIN_EXAMPLES, 0.0f, 0.5f, false, new GiniIndex(), Trainer.DEFAULT_SEED);
		RandomForestTrainer<Label> trainer = new RandomForestTrainer<>(treeTrainer,new VotingCombiner(),10);
		System.out.println(trainer.toString());

		Model<Label> model = trainer.train(trainingDataset);
		var evaluator = new LabelEvaluator();
		var evaluation = evaluator.evaluate(model,testingDataset);
		System.out.println(evaluation);

	}
}
