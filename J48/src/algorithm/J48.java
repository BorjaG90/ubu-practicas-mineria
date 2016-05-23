package algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import keel.dataset.Attribute;
import keel.dataset.Attributes;
//import algorithm.C45Obsoleto.Triplet;
import keel.dataset.Instance;

public class J48 {

	public myDataset dataset;

	private Node root;

	private double confidence;

	private double entropy;

	class Node {
		public boolean isLeaf;
		public int type;
		public int nominalValue;
		public int realValue;
		public Node left;
		public Node right;
	}

	public J48(myDataset dataset, double pruneConfidence) {
		this.dataset = dataset;
		this.confidence = pruneConfidence;
	}

	public void buildClassifier() {

		List<Instance> instances = Arrays.asList(dataset.IS.getInstances());

		this.entropy = calculateEntropy(instances);
		System.out.println("=======ENTROPIA: " + entropy + " ======");
		root = buildTree(instances);
	}
	
	//
	private Node buildTree(List<Instance> instances) {

		Node root = new Node();
		int numAttrs = Attributes.getInputNumAttributes();
		
		// crear el metodo check same class
		if (checkSameClass(instances) || numAttr == 0) {
			root.isLeaf = true;
			//AGREGAR MAS DATOS AL NODO
			return root;
		}

		double gain;
		double maxGain;
		int attrIndex;
		Map<Boolean, List<Instance>> dict;
		
		for (int i = 0; i < numAttrs; i++) {
			Attribute attr = Attributes.getAttribute(i);
			if(attr.getType() == Attribute.NOMINAL) {
				List<String> values = new ArrayList<String>(attr.getNominalValuesList());
				for(String value : values) {
					dict = filterByNominalAttr(instances, i, value);
				}
			} else {
				List<Double> values = getSplitPoints(instances, i);
				for(Double value : values) {
					dict = filterByNumericAttr(instances, i, value);

				}
			}				
		}

		return root;
	}
	
	/**
	 * Calcula la ganancia de informacion para un conjunto de instancias
	 * filtradas por el valor de un atributo.
	 * 
	 * @param dict conjunto de instancias filtradas
	 * @return ganancia de informacion
	 */
	private double calculateGain(Map<Boolean, List<Instance>> dict) {

		double gain = entropy;
		double size = dict.get(true).size() + dict.get(false).size();
		
		for (Boolean key : dict.keySet())
			gain -= dict.get(key).size() / size * calculateEntropy(dict.get(key));

		return gain;
	}

	// FILTRA LAS INSTANCIAS SEGUN EL VALOR DEL ATRIBUTO NOMINAL
	private Map<Boolean, List<Instance>> filterByNominalAttr(List<Instance> instances,
			int attr, String value) {
		
		Map<Boolean, List<Instance>> dict = new HashMap<Boolean, List<Instance>>();
		boolean status;
		
		for (Instance i : instances) {
			status = i.getInputNominalValues(attr).equals(value);
			if (!dict.containsKey(status))
				dict.put(status, new ArrayList<Instance>());
			dict.get(status).add(i);
		}
		return dict;
	}
	
	// FILTRA LAS INSTANCIAS SEGUN EL VALOR DEL ATRIBUTO NUMERICO
	private Map<Boolean, List<Instance>> filterByNumericAttr(List<Instance> instances,
			int attr, double value) {

		Map<Boolean, List<Instance>> dict = new HashMap<Boolean, List<Instance>>();
		boolean status;
		
		for (Instance i : instances) {
			status = i.getInputRealValues(attr) <= value;
			if (!dict.containsKey(status))
				dict.put(status, new ArrayList<Instance>());
			dict.get(status).add(i);
		}
		return dict;
	}

	/**
	 * Calcula la entropia para un conjunto de instancias.
	 * 
	 * @param instances
	 *            conjunto de instancias
	 * @return la entropia para las instancias
	 */
	private double calculateEntropy(List<Instance> instances) {

		Map<String, List<Instance>> dict = filterByClasses(instances);
		double entropy = 0.0;
		double p;

		for (String label : dict.keySet()) {
			p = (dict.get(label).size() / (double) instances.size());
			entropy += -p * (Math.log(p) / Math.log(2));
		}
		return entropy;
	}

	/**
	 * Crea un diccionario en el que se mapea cada instancia de una lista a la
	 * clase a la que pertenece.
	 * 
	 * @param instances,
	 *            lista de instancias.
	 * @param classes,
	 *            array con las etiquetas de todas las clases del dataset.
	 * @return dict, diccionario con los indices de las instancias de cada
	 *         calse.
	 */
	private Map<String, List<Instance>> filterByClasses(List<Instance> instances) {

		Map<String, List<Instance>> dict = new HashMap<String, List<Instance>>();

		for (Instance i : instances) {
			String c = i.getOutputNominalValues(0); // Obtiene la clase de la
													// instancia
			if (!dict.containsKey(c))
				dict.put(c, new ArrayList<Instance>());
			dict.get(c).add(i);
		}
		return dict;
	}

	/**
	 * sortByAttribute Metodo que ordena una lista de instancias por uno de sus
	 * atributos. Se utiliza solo para atributos continuos. El atributo de la
	 * instancia se identifica a traves de su indice dentro del dataset.
	 * 
	 * @param instances
	 *            lista de instancias del dataset
	 * @param attrIndex
	 *            indice del atributo dentro del dataset
	 * @return lista ordenada de instancias
	 */
	private List<Instance> sortByAttribute(List<Instance> instances, int attrIndex) {

		List<Instance> sorted = new ArrayList<Instance>(instances);
		Collections.sort(sorted, new Comparator<Instance>() {

			@Override
			public int compare(Instance a, Instance b) {
				double valueA = a.getInputRealValues(attrIndex);
				double valueB = b.getInputRealValues(attrIndex);

				if (valueA < valueB)
					return -1;
				else if (valueA > valueB)
					return 1;
				else
					return 0;
			}

		});
		return sorted;
	}

	/**
	 * Obtiene los puntos de corte para un atributo continuo.
	 * 
	 * @param instances lista con las instancias
	 * @param attrIndex indice del atributo continuo
	 * @return lista con los puntos de corte
	 */
	private List<Double> getSplitPoints(List<Instance> instances, int attrIndex) {
		
		List<Double> values = new ArrayList<Double>();
		List<Instance> sorted = sortByAttribute(instances, attrIndex);
		Instance instA, instB;
		double cutPoint;
		
		for(int i = 1; i < sorted.size(); i++) {
			instA = sorted.get(i - 1);
			instB = sorted.get(i);
			
			//Comara las clases de dos instancias consecutivas
			if(!instA.getOutputNominalValues(0).equals(
					instB.getOutputNominalValues(0))) {
				cutPoint = (instA.getInputRealValues(attrIndex) +
						instB.getInputRealValues(attrIndex)) / 2;
				values.add(cutPoint);
			}
		}
		
		return values;
	}

	// add parameter to the method
	public String classifyInstance() {
		return "";
	}

	public String getModel() {
		return "";
	}
	
	// private List<String> getValues(int attrIndex) {
	//
	// List<String> values = new ArrayList<String>();
	// Attribute attr = Attributes.getAttribute(attrIndex);
	// if(attr.getType() == Attribute.NOMINAL) {
	// // TODO CONVERTIR A UNA LISTA BUENA
	// values = new ArrayList<String>(attr.getNominalValuesList());
	// } else {
	// String[] classes = dataset.getClasses();
	// // ORDENAR POR EL ATRIBUTO NUMERICO
	// for(int i = 1; i < dataset.getnData(); i++) {
	// if(classes[i - 1] != classes[i]) {
	// // HACER PUNTO DE CORTE
	// }
	//
	// // Obtiene las instancias consecutivas para calcular el corte
	// dataset.IS.getInstance(i - 1).getInputRealValues(attrIndex);
	// dataset.IS.getInstance(i).getInputRealValues(attrIndex);
	// }
	// }
	//
	// return values;
	// }
		
}
