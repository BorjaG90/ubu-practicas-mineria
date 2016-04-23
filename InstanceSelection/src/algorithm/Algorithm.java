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
            //We do here the algorithm's operations        	
            //nClasses = train.getnOutputs();
        	
        	// Listas con los indices de las instancias de interes
        	List<Integer> trainIndexes = selectInstances(train);
        	List<Integer> testIndexes = selectInstances(test);
            
            //Finally we should fill the training and test output files
            doOutput(this.train, trainIndexes, this.outputTr);
            doOutput(this.test, testIndexes, this.outputTst);

            System.out.println("Algorithm Finished");
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
    	
    	// Diccionario con los indices de las instancias de cada clase
    	Map<String, List<Integer>> dict =
    			mapIndexesToClasses(dataset.getOutputAsString());
    	List<Integer> selected= new ArrayList<Integer>();
    	
    	if(equalDistribution) {
    		// Selecciona el porcentaje de indices de instancias de cada clase
    		// y los agrega a una lista
        	for(String classLabel : dataset.getClasses())
        		selected.addAll(randomSelection(dict.get(classLabel)));
    	} else {
    		// Agrega todos los indices de todas las instancias a una lista
    		for(String classLabel : dataset.getClasses())
    			selected.addAll(dict.get(classLabel));
    		// Selecciona un porcentaje de todos los indices de instancias
    		selected = randomSelection(selected);
    	}
    	return selected;
    }
    
    /**
     * Crea un diccionario en el que se mapea cada clase con una lista de indices
     * de las instancias del data-set que son de la clase.
     * 
     * @return dict, diccionario con los indices de las instancias de cada calse.
     */
    private Map<String, List<Integer>> mapIndexesToClasses(String[] instances) {
    	
    	Map<String, List<Integer>> dict = new HashMap<String, List<Integer>>();
    	
    	for(int i = 0; i < instances.length; i++) {
    		if(!dict.containsKey(instances[i]))
    			dict.put(instances[i], new ArrayList<Integer>());
    		
    		dict.get(instances[i]).add(i);
    	}
    	return dict;
    }
    
    // TODO: REHACER LOS METODOS doOutput y classificationOutput !!!
    
    /**
     * Selecciona al azar un porcentaje de los indices correspondientes a
     * instancias del data-set.
     * 
     * @param indexes, lista con los indices de instancias del data-set.
     * @return selected, sub-lista con el porcentaje de indices seleccionados
     * 			sobre el total de indices.
     */
    private List<Integer> randomSelection(List<Integer> indexes) {
    	
    	int indexesToSelect = (int)(indexes.size() * percentage / 100);
    	List<Integer> selected = new ArrayList<Integer>();
    	
    	for(int i = 0; i < indexesToSelect; i++)
    		selected.add(indexes.remove((int)(Math.random() * indexes.size())));
    	
    	return selected;
    }

    /**
     * It generates the output file from a given dataset and stores it in a file
     * @param dataset myDataset input dataset
     * @param indexes list of indexes corresponding to a subset of instances
     * @param filename String the name of the file
     */
    private void doOutput(myDataset dataset, List<Integer> indexes, String filename) {
        String output = new String("");
        output = dataset.copyHeader(); //we insert the header in the output file
        //We write the output for each example
        for (int index : indexes) {
            //for classification:
            output += dataset.getOutputAsString(index) + " " +
                    this.classificationOutput(dataset.getExample(index)) + "\n";
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
