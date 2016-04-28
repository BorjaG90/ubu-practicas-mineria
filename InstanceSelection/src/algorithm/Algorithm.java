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
    String outputTr, outputTst;
    int nClasses;

    //We may declare here the algorithm's parameters
    double percentage;
    boolean equalDistribution;
        

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
            System.out.println("\nReading the training set: " +
                               parameters.getTrainingInputFile());
            train.readClassificationSet(parameters.getTrainingInputFile(), true);
            System.out.println("\nReading the validation set: " +
                               parameters.getValidationInputFile());
            val.readClassificationSet(parameters.getValidationInputFile(), false);
            System.out.println("\nReading the test set: " +
                               parameters.getTestInputFile());
            test.readClassificationSet(parameters.getTestInputFile(), false);
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
        percentage = Double.parseDouble(parameters.getParameter(0));
        equalDistribution = Boolean.parseBoolean(parameters.getParameter(1));
        System.out.println("\nPercentage: " + percentage);
        System.out.println("Equal distribution: " + equalDistribution);
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
        	System.out.println("\nProcessing training dataset...");
        	writeSelected(selectInstances(train), train, outputTr);
        	
        	System.out.println("\nProcessing test dataset...");
        	writeSelected(selectInstances(test), test, outputTst);
            
            System.out.println("\nAlgorithm Finished");
        }
    }
      
    /**
     * Selecciona las instancias de forma aleatoria en funcion del porcentaje
     * y de si hay igual distribucion. Las instancias se seleccionan a traves de
     * sus indices dentro del dataset.
     * 
     * @param dataset, el dataset con las instancias.
     * @return indexes, lista con los indices de las instancias de interes.
     */
    private List<Integer> selectInstances(myDataset dataset) {
    	
		Map<String, List<Integer>> before;
		Map<String, List<Integer>> after;
		
    	List<Integer> allIndexes = new ArrayList<Integer>();    	
    	List<Integer> selected = new ArrayList<Integer>();
    	    	
    	// Añade todos los indices de instancias a una lista 
    	for(int i = 0; i < dataset.getnData(); i++)
    		allIndexes.add(i);
    	
		before = mapIndexesToClasses(allIndexes, dataset.getOutputAsString());
    	if(equalDistribution) {
    		for(String classLabel : dataset.getClasses())
        		selected.addAll(randomSelection(before.get(classLabel)));
    	} else
    		selected.addAll(randomSelection(allIndexes));
    	
		after = mapIndexesToClasses(selected, dataset.getOutputAsString());
		
    	printStatistics(before, after, dataset);
    	
    	return selected;
    }
       
    /**
     * Crea un diccionario en el que se mapea cada instancia de una lista
     * a la clase a la que pertenece. La instancia se representa a través de
     * su índice en el data-set.
     * 
     * @param indexes, lista de los indices de las instancias a mapear.
     * @param classes, array con las etiquetas de todas las clases del dataset.
     * @return dict, diccionario con los indices de las instancias de cada calse.
     */
    private Map<String, List<Integer>> mapIndexesToClasses(List<Integer> indexes, String[] classes) {
    	
    	Map<String, List<Integer>> dict = new HashMap<String, List<Integer>>();
    	   	
    	for(Integer i : indexes) {
    		if(!dict.containsKey(classes[i]))
    			dict.put(classes[i], new ArrayList<Integer>());
    		dict.get(classes[i]).add(i);
    	}
    	return dict;
    }
    
    /**
     * Selecciona al azar un porcentaje de los indices correspondientes a
     * instancias del data-set.
     * 
     * @param indexes, lista con los indices de instancias del data-set.
     * @return selected, sub-lista con el porcentaje de indices seleccionados
     * 			sobre el total de indices.
     */
    private List<Integer> randomSelection(List<Integer> indexes) {
    	
    	int indexesToSelect = (int)Math.round((indexes.size() * percentage / 100));
    	
    	// Crea una copia de la lista de indices y la referencia a la misma variable
    	indexes = new ArrayList<Integer>(indexes);
    	List<Integer> selected = new ArrayList<Integer>();
    	int index;
    	
    	for(int i = 0; i < indexesToSelect; i++) {
    		index = (int)Math.round((Math.random() * (indexes.size() - 1)));
    		selected.add(indexes.remove(index));
    	}
    	return selected;
    }
    
    /**
     * Muestra una estadistica sobre las instancias del data-set antes y despues
     * de aplicar seleccion de instancias.
     * 
     * Muestra el numero de instancias iniciales y seleccionadas de cada clase
     * asi como el numero total de instancias iniciales y seleccionadas.
     * 
     * @param before, diccionario con las instancias de cada clase antes de la seleccion.
     * @param after, diccionario con las instancias de cada clase despues de la seleccion.
     * @param dataset, data-set inicial.
     */
    private void printStatistics(Map<String, List<Integer>> before,
    		Map<String, List<Integer>> after, myDataset dataset) {
    	
    	int instancesBefore = 0;
    	int instancesAfter = 0;
    	int nBefore;
    	int nAfter;
    	
    	System.out.println(">>>>>>>>>>>>\tSTATISTICS\t<<<<<<<<<<<<");
    	System.out.println("\t\t\tBEFORE\tAFTER\t%");
    	for(String classLabel : dataset.getClasses()) {
    		nBefore = before.get(classLabel) == null ? 0 : before.get(classLabel).size();
    		instancesBefore += nBefore;
    		
    		nAfter = after.get(classLabel) == null ? 0 : after.get(classLabel).size();
    		instancesAfter += nAfter;
    		
     		System.out.printf("Instances of class %s:\t%d\t%d\t%.2f\n",
     				classLabel, nBefore, nAfter, (double)nAfter / nBefore * 100);
    	}
    	
 		System.out.printf("Total instances:\t%d\t%d\t%.2f\n",
 				instancesBefore, instancesAfter, (double)instancesAfter/ instancesBefore* 100);
    	
    }
    
    private void writeSelected(List<Integer> indexes, myDataset dataset, String filename) {
    	String output = dataset.copyHeader();
    	for(Integer index :indexes)
    		output += dataset.IS.getInstance(index) + "\n";
    	
    	Files.writeFile(filename, output);
    }

    /**
     * It generates the output file from a given dataset and stores it in a file
     * @param dataset myDataset input dataset
     * @param indexes list of indexes corresponding to a subset of instances
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
