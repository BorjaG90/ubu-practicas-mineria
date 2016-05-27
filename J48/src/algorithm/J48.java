package algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import keel.dataset.Attribute;
import keel.dataset.Attributes;
//import algorithm.C45Obsoleto.Triplet;
import keel.dataset.Instance;

public class J48 {

	private myDataset dataset;

	private Node root;
	
	private String model;

	private double confidence;

	private double entropy;

	class Node {
		public String classLabel;
		public boolean isLeaf = false;
		public Data nodeData;
		public double precision;
		public Node left;
		public Node right;
	}
	
	class Data {
		public String attrName;
		public int attrIndex;
		public int attrType;
		public String attrValue;
		
		public Data() {}
		
		public Data(int attrIndex, int attrType, String attrName, String attrValue) {
			this.attrIndex = attrIndex;
			this.attrType = attrType;
			this.attrValue = attrValue;
			this.attrName = attrName;
		}
	}

	public J48(myDataset dataset, double pruneConfidence) {
		this.dataset = dataset;
		this.confidence = pruneConfidence;
		this.model = null;
	}

	public void buildClassifier() {

		List<Instance> instances = Arrays.asList(dataset.IS.getInstances());
		List<Set<String>> attributes = getAllAttributes(instances);

		this.entropy = calculateEntropy(instances);
		
		this.root = buildTree(instances, attributes);
	}
	
	// REFACTORIZAR EL METODO
	private Node buildTree(List<Instance> instances, List<Set<String>> attributes) {

		Node root = new Node();
		Map<String, List<Instance>> instancesPerClass = filterByClass(instances);
		
		// Si todas las instancias son de la misma clase
		if(instancesPerClass.size() == 1) {
			root.classLabel = new ArrayList<String>(instancesPerClass.keySet()).get(0);
			root.precision = instancesPerClass.get(root.classLabel).size() /
					(double) instances.size();
			root.isLeaf = true;
			return root;
		}
		
		// Si no hay atributos que procesar
		if(attributes.size() == 0) {
			root.classLabel = mostCommonClass(instancesPerClass);
			root.precision = instancesPerClass.get(root.classLabel).size() / 
					(double) instances.size();
			root.isLeaf = true;
			return root;
		}
		
		// Obtiene el atributo-valor que mayor ganancia de informacion
		// proporciona
		
		// REFACTORIZAR
		Attribute attr;
		double gain = 0, maxGain = 0;
		for(int i = 0; i < attributes.size(); i++) {
			attr = Attributes.getAttribute(i);
			for(String value : attributes.get(i)) {
				gain = calculateGain(instances, i, value);
				if(maxGain < gain) {
					maxGain = gain;
					root.nodeData = new Data(i, attr.getType(), attr.getName(), value);
				}
			}
		}
		
		int attrIndex = root.nodeData.attrIndex;
		String attrValue = root.nodeData.attrValue;
		Map<Boolean, List<Instance>> filteredInst;
		
		attributes.get(attrIndex).remove(attrValue);
		if(attributes.get(attrIndex).size() == 0)
			attributes.remove(attrIndex);
		
		if(root.nodeData.attrType == Attribute.NOMINAL)
			filteredInst = filterByNominalAttr(instances, attrIndex, attrValue);
		else
			filteredInst = filterByNumericAttr(instances, attrIndex, Double.parseDouble(attrValue));
		
		List<Instance> list;
		List<Set<String>> copyOfAttributes = new ArrayList<Set<String>>();
		
		// Copia la lista de atributos antes de las llamadas recursivas.
		for(Set<String> values : attributes)
			copyOfAttributes.add(new HashSet<String>(values));
		
		list = filteredInst.get(false);
		if(list == null) {
			root.right = new Node();
			root.right.isLeaf = true;
			root.right.classLabel = mostCommonClass(instancesPerClass);
			// poner la precision en el nodo
		} else {
			attr = Attributes.getAttribute(root.nodeData.attrIndex);
			root.right = buildTree(list, copyOfAttributes);
		}
		
		list = filteredInst.get(true);
		if(list == null) {
			root.left = new Node();
			root.left.isLeaf = true;
			root.left.classLabel = mostCommonClass(instancesPerClass);
			// poner la precision en el nodo
		} else {
			attr = Attributes.getAttribute(root.nodeData.attrIndex);
			root.left = buildTree(filteredInst.get(true), copyOfAttributes);
		}
					
		return root;
	}
	
	/**
	 * Devuelve la clase mas comun dentro de un conjunto de instancias
	 * filtradas por clase;
	 * 
	 * @param instancesPerClass conjunto de instancias filtradas por clase
	 * @return la clase mas comun dentro del conjunto
	 */
	private String mostCommonClass(Map<String, List<Instance>> instancesPerClass) {
		
		int freq = 0;
		String classLabel = "";
		
		for(String label : instancesPerClass.keySet()) {
			if(instancesPerClass.get(label).size() > freq) {
				freq = instancesPerClass.get(label).size();
				classLabel = label;
			}
		}
		return classLabel;
	}
	
	/**
	 * Calcula la ganancia de informacion para un conjunto de instancias
	 * filtradas por el valor de un atributo.
	 * 
	 * @param dict conjunto de instancias filtradas
	 * @return ganancia de informacion
	 */
	private double calculateGain(List<Instance> instances, int attrIndex, String value) {

		double gain = entropy;
		double p;
		Map<Boolean, List<Instance>> dict;
		Attribute attr = Attributes.getAttribute(attrIndex);
		
		if(attr.getType() == Attribute.NOMINAL)
			dict = filterByNominalAttr(instances, attrIndex, value);
		else
			dict = filterByNumericAttr(instances, attrIndex, Double.parseDouble(value));
		
		for (Boolean key : dict.keySet()) {
			p = dict.get(key).size() / (double) instances.size();
			gain -= p * calculateEntropy(dict.get(key));
		}
		return gain;
	}

	/**
	 * Calcula la entropia para un conjunto de instancias.
	 * 
	 * @param instances
	 *            conjunto de instancias
	 * @return la entropia para las instancias
	 */
	private double calculateEntropy(List<Instance> instances) {
	
		Map<String, List<Instance>> dict = filterByClass(instances);
		double entropy = 0.0;
		double p;
		
		for (String classLabel : dict.keySet()) {
			p = (double) dict.get(classLabel).size() / instances.size();
			entropy += p * (Math.log(p) / Math.log(2));
		}
		return -entropy;
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
	
	// TODO COMMENTAR
	private Map<String, List<Instance>> filterByClass(List<Instance> instances) {
		
		Map<String, List<Instance>> dict = new HashMap<String, List<Instance>>();
		
		// Filtra las instancias por clase
		for (Instance i : instances) {
			String c = i.getOutputNominalValues(0);
			if (!dict.containsKey(c))
				dict.put(c, new ArrayList<Instance>());
			dict.get(c).add(i);
		}
		return dict;
	}

	/**
	 * Obtiene una lista con todos los atributos y los posibles valores de
	 * cada atributo. Para los atributos continuos se obtienen los valores
	 * de los puntos de corte donde se produce cambio de clase.
	 * 
	 * @param instances lista de instancias
	 * @return lista de listas con los valores de cada atributo.
	 */
	private List<Set<String>> getAllAttributes(List<Instance> instances) {
		
		List<Set<String>> values = new ArrayList<Set<String>>();
		int numAttrs = Attributes.getInputNumAttributes();
		
		for (int i = 0; i < numAttrs; i++) {
			Attribute attr = Attributes.getAttribute(i);
			if(attr.getType() == Attribute.NOMINAL)
				values.add(i, new HashSet<String>(attr.getNominalValuesList()));
			else 
				values.add(i, getSplitPoints(instances, i));
		}
		
		return values;
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
	 * Obtiene los puntos de corte para un atributo continuo. Los puntos de corte
	 * se representan como objetos de la clase String.
	 * 
	 * @param instances lista con las instancias
	 * @param attrIndex indice del atributo continuo
	 * @return lista con los puntos de corte
	 */
	private Set<String> getSplitPoints(List<Instance> instances, int attrIndex) {
		
		Set<String> values = new HashSet<String>();
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
				values.add(String.valueOf(cutPoint));
			}
		}
		
		return values;
	}

	// add parameter to the method
	public String classifyInstance(Instance instance) {
		Node node = this.root;
		boolean status;
		
		while(!node.isLeaf) {
			if(node.nodeData.attrType == Attribute.NOMINAL) {
				String value = instance.getInputNominalValues(node.nodeData.attrIndex);
				status = value.equals(node.nodeData.attrValue);
			} else  {
				double value = instance.getInputRealValues(node.nodeData.attrIndex);
				status = value <= Double.parseDouble(node.nodeData.attrValue);
			}
			
			if(status)
				node = node.left;
			else
				node = node.right;
		}
		
		return node.classLabel;
	}

	public String getModel() {
		if(this.model == null)
			this.model = buildModel(root, 0);
		return this.model;
	}
	
	private String buildModel(Node root, int treeLevel) {
		
		String result = "";
		String tabs = "";
		for(int i = 0; i < treeLevel; i++) 
			tabs += "\t";
		
		if(root.isLeaf) {
			result += tabs + "class = " + root.classLabel + "\n";
			return result;
		}
		
		result += tabs + "if " + root.nodeData.attrName;
		if(root.nodeData.attrType == Attribute.NOMINAL)
			result += " == ";
		else 
			result += " <= ";
		
		result += root.nodeData.attrValue + " then:\n";
		result += buildModel(root.left, treeLevel + 1);
		
		result += tabs + "else:\n";
		result += buildModel(root.right, treeLevel + 1);
		
		return result;
	}
			
}
