package algorithm;

/**
 * <p>Title: Algorithm</p>
 *
 * <p>Description: It contains the implementation of the algorithm</p>
 *
 *
 * <p>Company: KEEL </p>
 *
 * @author Alberto Fernández
 * @version 1.0
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.core.*;

import keel.dataset.*;

public class Algorithm {

	private myDataset train, val, test;
	private String inputTr, inputTst, inputVal;
	private String outputTr, outputTst, outputModel;
	private String modifiedTr, modifiedVal, modifiedTst; // Ficheros preprocesados
	
	private double sampling;
	private double confidence;
	private boolean equalDistribution;
	private boolean preprocess;
	
	private int nClasses;

	private boolean somethingWrong = false; // to check if everything is
											// correct.

	private C45 classifier;

	/**
	 * Default constructor
	 */
	public Algorithm() {
	}

	/**
	 * It reads the data from the input files (training, validation and test)
	 * and parse all the parameters from the parameters array.
	 * 
	 * @param parameters
	 *            parseParameters It contains the input files, output files and
	 *            parameters
	 */
	public Algorithm(parseParameters parameters) {

		train = new myDataset();
		val = new myDataset();
		test = new myDataset();

		inputTr = parameters.getTrainingInputFile();
		inputVal = parameters.getValidationInputFile();
		inputTst = parameters.getTestInputFile();

		try {
			System.out.println("\nReading the training set: " + inputTr);
			train.readClassificationSet(inputTr, true);

			System.out.println("\nReading the validation set: " + inputVal);
			val.readClassificationSet(inputVal, false);

			System.out.println("\nReading the test set: " + inputTst);
			test.readClassificationSet(inputTst, false);
		} catch (IOException e) {
			System.err.println("There was a problem while reading the input data-sets: " + e);
			somethingWrong = true;
		}

		somethingWrong = somethingWrong || train.hasMissingAttributes();

		outputTr = parameters.getTrainingOutputFile();
		outputTst = parameters.getTestOutputFile();
		
		preprocess = Boolean.parseBoolean(parameters.getParameter(0));
		confidence = Double.parseDouble(parameters.getParameter(1));
		outputModel = parameters.getOutputFile(0);
		
		System.out.println("\n*----- Configuration -----*");
		System.out.println("Preprocess: " + preprocess);
		System.out.println("Confidence level (%): " + confidence);
		System.out.println("Model output: " + outputModel);

		if (preprocess) {
			modifiedTr = parameters.getInputFile(0);
			modifiedVal = parameters.getInputFile(1);
			modifiedTst = parameters.getInputFile(2);
			
			sampling = Double.parseDouble(parameters.getParameter(2));
			equalDistribution = Boolean.parseBoolean(parameters.getParameter(3));

			System.out.println("Processed training dataset: " + modifiedTr);
			System.out.println("Processed validation dataset: " + modifiedVal);
			System.out.println("Processed test dataset: " + modifiedTst);
			System.out.println("Sample size (%): " + sampling);
			System.out.println("Equal distribution: " + equalDistribution);
		}
	}

	/**
	 * It launches the algorithm
	 */
	public void execute() {
		if (somethingWrong) { // We do not execute the program
			System.err.println("An error was found, either the data-set have numerical values or missing values.");
			System.err.println("Aborting the program");
			// We should not use the statement: System.exit(-1);
		} else {
			if(preprocess)
				processData();
			
			classifyData();
		}
	}

	/**
	 * Pre-process
	 */
	private void processData() {

		List<Integer> instances;
		System.out.println("\n*----- Instance Selection -----*");
		
		System.out.println("Processing training dataset...");
		instances = InstanceSelection.selectInstances(train, equalDistribution, sampling);
		InstanceSelection.writeSelected(instances, train, modifiedTr);
		
		System.out.println("Processing test dataset...");
		instances = InstanceSelection.selectInstances(test, equalDistribution, sampling);
		InstanceSelection.writeSelected(instances, test, modifiedTst);

		System.out.println("Preprocessing finished");
		
		try {
			System.out.println("\nReading the processed training set: " + modifiedTr);
			train.readClassificationSet(modifiedTr, true);
			System.out.println("\nReading the processed validation set: " + modifiedTr);
			val.readClassificationSet(modifiedTr, false);
			System.out.println("\nReading the processed test set: " + modifiedTst);
			test.readClassificationSet(modifiedTst, false);
		} catch (IOException e) {
			System.err.println("There was a problem while reading " + "the modified data-sets: " + e);
			somethingWrong = true;
		}
		
	}

	/**
	 * Classification
	 */
	private void classifyData() {
		
		System.out.println("\n*----- C4.5 Classification -----*");
		
		classifier = new C45(train, confidence);
		System.out.println("Building classifier...");
		classifier.buildClassifier();
		System.out.println("Done");
		
		Files.writeFile(outputModel, this.classifier.getModel());
		
		System.out.println("\nClassifying train dataset ...");
		doOutput(train, outputTr);
		
		System.out.println("\nClassifying test dataset ...");
		doOutput(test, outputTst);
	}

	/**
	 * It generates the output file from a given dataset and stores it in a file
	 * 
	 * @param dataset
	 *            myDataset input dataset
	 * @param filename
	 *            String the name of the file
	 */
	private void doOutput(myDataset dataset, String filename) {
		
		String output, expected, result;
		int i, hits = 0;
		output = dataset.copyHeader(); //insert the header in the output file
		
		for (i = 0; i < dataset.getnData(); i++) {
			expected = dataset.getOutputAsString(i);
			result = this.classificationOutput(dataset.IS.getInstance(i));
			output += expected + " " + result + "\n";
			if(result.equals(expected))
				hits++;
		}
		Files.writeFile(filename, output);
		System.out.println("Correctly classified: " + hits + "/" + i + " ("
				+ (double) hits / i * 100 + "%)");
		
	}

	/**
	 * It returns the algorithm classification output given an input example
	 * 
	 * @param exampleIndex
	 *            index of the input example
	 * @return String the output generated by the algorithm
	 */
	private String classificationOutput(Instance instance) {
		String output = new String("?");

		output = classifier.classifyInstance(instance);

		return output;
	}

	/**
	 * It returns the algorithm regression output given an input example
	 * 
	 * @param example
	 *            double[] The input example
	 * @return double the output generated by the algorithm
	 */
	private double regressionOutput(double[] example) {
		double output = 0.0;
		/**
		 * Here we should include the algorithm directives to generate the
		 * regression output from the input example
		 */
		return output;
	}

}
