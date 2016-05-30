package algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.core.Files;

public class InstanceSelection {
	 /**
     * Selecciona las instancias de forma aleatoria en funcion del porcentaje
     * y de si hay igual distribucion. Las instancias se seleccionan a traves de
     * sus indices dentro del dataset.
     * 
     * @param dataset, el dataset con las instancias.
     * @return indexes, lista con los indices de las instancias de interes.
     */
    public static List<Integer> selectInstances(myDataset dataset,boolean equalDistribution,double percentage) {
    	
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
        		selected.addAll(randomSelection(before.get(classLabel),percentage));
    	} else
    		selected.addAll(randomSelection(allIndexes,percentage));
    	
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
    public static Map<String, List<Integer>> mapIndexesToClasses(List<Integer> indexes, String[] classes) {
    	
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
    public static List<Integer> randomSelection(List<Integer> indexes, double percentage) {
    	
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
    public static void printStatistics(Map<String, List<Integer>> before,
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
    
    public static void writeSelected(List<Integer> indexes, myDataset dataset, String filename) {
    	String output = dataset.copyHeader();
    	for(Integer index :indexes)
    		output += dataset.IS.getInstance(index) + "\n";
    	
    	Files.writeFile(filename, output);
    }
}
