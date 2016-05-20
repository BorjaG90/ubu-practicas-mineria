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

    myDataset train, val, test;
    String outputTr, outputTst, InputTr, InputTst;
    int nClasses;

    //We may declare here the algorithm's parameters
    double percentage;
    boolean equalDistribution;
    boolean preprocess;
    String modifiedTr, modifiedTst; //Ruta de datasets modificados
        

    private boolean somethingWrong = false; //to check if everything is correct.

    /**
     * Default constructor
     */
    public Algorithm() {
    }

    /**
     * It reads the data from the input files (training, validation and test) and parse all the parameters
     * from the parameters array.
     * @param parameters parseParameters It contains the input files, output files and parameters
     */
    public Algorithm(parseParameters parameters) {

        train = new myDataset();
        val = new myDataset();
        test = new myDataset();
        try {
        	InputTr=parameters.getTrainingInputFile();
        	InputTst=parameters.getTestInputFile();
        	
            System.out.println("\nReading the training set: " + InputTr);
            train.readClassificationSet(InputTr, true);
            
            System.out.println("\nReading the validation set: " +
                               parameters.getValidationInputFile());
            val.readClassificationSet(parameters.getValidationInputFile(), false);
            
            System.out.println("\nReading the test set: " + InputTst);
            test.readClassificationSet(InputTst, false);
        } catch (IOException e) {
            System.err.println(
                    "There was a problem while reading the input data-sets: " +
                    e);
            somethingWrong = true;
        }

        //We may check if there are some numerical attributes, because our algorithm may not handle them:
        //somethingWrong = somethingWrong || train.hasNumericalAttributes();
        //somethingWrong = somethingWrong || train.hasMissingAttributes();

        outputTr = parameters.getTrainingOutputFile();
        outputTst = parameters.getTestOutputFile();

        //Now we parse the parameters, for example:
        /*
         seed = Long.parseLong(parameters.getParameter(0));
         iterations = Integer.parseInt(parameters.getParameter(1));
         crossOverProb = Double.parseDouble(parameters.getParameter(2));
         */
        preprocess = Boolean.parseBoolean(parameters.getParameter(0));
        System.out.println("\nPreprocess: " + preprocess);
        if (preprocess){
        	percentage = Double.parseDouble(parameters.getParameter(1));
        	equalDistribution = Boolean.parseBoolean(parameters.getParameter(2));
        	modifiedTr=parameters.getParameter(3);
        	modifiedTst=parameters.getParameter(4);
        	System.out.println("\nPercentage: " + percentage);
            System.out.println("Equal distribution: " + equalDistribution);
        }
    }
    /**
     * Pre-process 
     */
    public void preProcess(){
    	/*Instance Selection*/
    	System.out.println("\nProcessing Instance selection in  training dataset...");
    	InstanceSelection.writeSelected(InstanceSelection.selectInstances(train, equalDistribution, percentage), train, modifiedTr);
    	System.out.println("\nProcessing Instance selection in test dataset...");
    	InstanceSelection.writeSelected(InstanceSelection.selectInstances(test, equalDistribution, percentage), test, modifiedTst);
        
        System.out.println("\nAlgorithm Finished");
    }
    /**
     * Classification
     */
    public void classification(){
    	//If there's preprocess we load the modified datasets after the preprocessing -Borja
    	if (preprocess){
    		try {
                System.out.println("\nReading the training set: " + modifiedTr);
                train.readClassificationSet(modifiedTr, true);
                System.out.println("\nReading the validation set: " + modifiedTr);
                val.readClassificationSet(modifiedTr, false);
                System.out.println("\nReading the test set: " + modifiedTst);
                test.readClassificationSet(modifiedTst, false);
            } catch (IOException e) {
                System.err.println(
                        "There was a problem while reading the modified data-sets: " +
                        e);
                somethingWrong = true;
            }
    	}
    	//Ejecutar C45
    }
    /**
     * It launches the algorithm
     */
    public void execute() {
        if (somethingWrong) { //We do not execute the program
            System.err.println("An error was found, either the data-set have numerical values or missing values.");
            System.err.println("Aborting the program");
            //We should not use the statement: System.exit(-1);
        } else {
        	System.out.println("*--PREPROCESSING--*");
        	preProcess();
        	System.out.println("*--CLASSIFICATION--*");
        	classification();
            
        }
    }
   
    /**
     * It generates the output file from a given dataset and stores it in a file
     * @param dataset myDataset input dataset
     * @param filename String the name of the file
     */
    private void doOutput(myDataset dataset, String filename) {
        String output = new String("");
        output = dataset.copyHeader(); //we insert the header in the output file
        //We write the output for each example
        for (int i = 0; i < dataset.getnData(); i++) {
            //for classification:
            output += dataset.getOutputAsString(i) + " " +
            		this.classificationOutput(dataset.getExample(i)) + "\n";
        //for regression:
        //output += dataset.getOutputAsReal(i) + " " +(double)this.regressionOutput(dataset.getExample(i)) + "\n";
                    
        }
        Files.writeFile(filename, output);
    }

    /**
     * It returns the algorithm classification output given an input example
     * @param example double[] The input example
     * @return String the output generated by the algorithm
     */
    private String classificationOutput(double[] example) {
        String output = new String("?");
        /**
          Here we should include the algorithm directives to generate the
          classification output from the input example
         */

        return output;
    }

    /**
     * It returns the algorithm regression output given an input example
     * @param example double[] The input example
     * @return double the output generated by the algorithm
     */
    private double regressionOutput(double[] example) {
	    double output = 0.0;
        /**
          Here we should include the algorithm directives to generate the
          regression output from the input example
         */
        return output;
    }    

}
