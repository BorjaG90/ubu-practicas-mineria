package algorithm;

import java.text.DecimalFormat;
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

public class C45 {

	private myDataset dataset;

	private Node root;
	
	private String model;

	private double confidence;

	private double entropy;

	class Node {
		public boolean isLeaf = false;
		public double precision;
		public String classLabel;
		public AttrData data;
		public Node left;
		public Node right;
	}
	
	class AttrData {
		public String attrName;
		public int attrIndex;
		public int attrType;
		public String attrValue;
		
		public AttrData() {}
		
		public AttrData(int attrIndex, int attrType, String attrName, String attrValue) {
			this.attrIndex = attrIndex;
			this.attrType = attrType;
			this.attrValue = attrValue;
			this.attrName = attrName;
		}
	}

	public C45(myDataset dataset, double pruneConfidence) {
		this.dataset = dataset;
		this.confidence = pruneConfidence;
		this.model = null;
	}

	public void buildClassifier() {

		List<Instance> instances = Arrays.asList(dataset.IS.getInstances());
		List<Set<String>> attributes = getAllAttributes(instances);

		this.entropy = computeEntropy(instances);		
		this.root = buildTree(instances, attributes);
	}
	
	// add parameter to the method
	public String classifyInstance(Instance instance) {
		Node node = this.root;
		boolean status;
		
		while(!node.isLeaf) {
			if(node.data.attrType == Attribute.NOMINAL) {
				String value = instance.getInputNominalValues(node.data.attrIndex);
				status = value.equals(node.data.attrValue);
			} else  {
				double value = instance.getInputRealValues(node.data.attrIndex);
				status = value <= Double.parseDouble(node.data.attrValue);
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

	private Node buildTree(List<Instance> instances, List<Set<String>> attributes) {
		
		Node root = new Node();
		Map<String, Integer> frequencies = getClassFrequencies(instances);
		String mostCommonClass = mostCommonClass(frequencies);
		double precision = (double) frequencies.get(mostCommonClass) /
				 instances.size();
		
		// Si todas las instancias son de la misma clase O
		// Si no hay mas atributos que procesar O
		// Si la precision del nodo es igual o mayor que la confianza
		if(frequencies.get(mostCommonClass) == instances.size() 
		   || attributes.size() == 0) {
			root.isLeaf = true;
			root.classLabel = mostCommonClass;
			root.precision = precision;
			return root;
		}
		
		// Obtiene el atributo con mayor ganancia de informacion
		root.data = getBestAttribute(instances, attributes);
		
		// Copia la lista de atributos antes de las llamadas recursivas
		List<Set<String>> copyOfAttributes = new ArrayList<Set<String>>();
		for(Set<String> values : attributes)
			copyOfAttributes.add(new HashSet<String>(values));
		
		// Filtra las instancias que tienen el valor del atributo de las que no
		Map<Boolean, List<Instance>> filteredInst;
		if(root.data.attrType == Attribute.NOMINAL) {
			filteredInst = filterByNominalAttr(instances, root.data.attrIndex,
					root.data.attrValue);
		} else {
			filteredInst = filterByNumericAttr(instances, root.data.attrIndex,
					Double.parseDouble(root.data.attrValue));
		}		
		
		if(filteredInst.get(false) == null) {
			root.right = new Node();
			root.right.isLeaf = true;
			root.right.classLabel = mostCommonClass;
			root.right.precision = precision;
		} else {
			root.right = buildTree(filteredInst.get(false), copyOfAttributes);
		}
		
		if(filteredInst.get(true) == null) {
			root.left = new Node();
			root.left.isLeaf = true;
			root.left.classLabel = mostCommonClass;
			root.right.precision = precision;
		} else {
			root.left = buildTree(filteredInst.get(true), copyOfAttributes);
		}
					
		return root;
	}
	
	
	/**
	 * Obtiene el atributo que mejor clasifica las instancias, es decir,
	 * el atributo que mayor ganancia de informacion proporciona.
	 * 
	 * @param instances lista de instancias
	 * @param attributes lista de atributos
	 * @return la informacion del atributo que mejor clasifica las instancias
	 */
	private AttrData getBestAttribute(List<Instance> instances, List<Set<String>> attributes) {
		
		Attribute attr;
		AttrData data = null;
		double gain = 0, maxGain = Double.MIN_VALUE;
		
		for(int i = 0; i < attributes.size(); i++) {
			attr = Attributes.getAttribute(i);
			for(String value : attributes.get(i)) {
				gain = computeGain(instances, i, value);
				if(maxGain < gain) {
					maxGain = gain;
					data = new AttrData(i, attr.getType(), attr.getName(), value);
				}
			}
		}
		
		attributes.get(data.attrIndex).remove(data.attrValue);
		if(attributes.get(data.attrIndex).size() == 0)
			attributes.remove(data.attrIndex);
		return data;
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
	 * Devuelve la clase mas comun a partir de un diccionario con 
	 * las frecuencias de las clases.
	 * 
	 * @param frequencies diccionario con las frequencias
	 * @return la clase mas comun
	 */
	private String mostCommonClass(Map<String, Integer> frequencies) {
		
		int freq = 0;
		String classLabel = null;
		
		for(Map.Entry<String, Integer> entry : frequencies.entrySet()) {
			if(entry.getValue() > freq) {
				freq = entry.getValue();
				classLabel = entry.getKey();
			}
		}
		
		return classLabel;
	}
	
	/**
	 * Calcula las frecuencias de cada clase, es decir, el numero de instancias
	 * que pertenecen a cada clase.
	 *
	 * @param instances conjunto de instancias
	 * @return diccionario clase-frequencia
	 */
	private Map<String, Integer> getClassFrequencies(List<Instance> instances) {
		
		String classLabel;
		Map<String, Integer> dict = new HashMap<String, Integer>();
		
		for(Instance i : instances) {
			classLabel = i.getOutputNominalValues(0);
			if(!dict.containsKey(classLabel))
				dict.put(classLabel, 0);
			dict.put(classLabel, dict.get(classLabel) + 1);
		}
		return dict;
	}
	
	/**
	 * Calcula la ganancia de informacion para un conjunto de instancias
	 * filtradas por el valor de un atributo.
	 * 
	 * @param dict conjunto de instancias filtradas
	 * @return ganancia de informacion
	 */
	private double computeGain(List<Instance> instances, int attrIndex, String value) {

		double sum = 0;
		double p;
		Map<Boolean, List<Instance>> dict;
		Attribute attr = Attributes.getAttribute(attrIndex);
		
		if(attr.getType() == Attribute.NOMINAL)
			dict = filterByNominalAttr(instances, attrIndex, value);
		else
			dict = filterByNumericAttr(instances, attrIndex, Double.parseDouble(value));
		
		for (Boolean key : dict.keySet()) {
			p = dict.get(key).size() / (double) instances.size();
			sum += p * computeEntropy(dict.get(key));
		}
		return entropy - sum;
	}

	/**
	 * Calcula la entropia para un conjunto de instancias.
	 * 
	 * @param instances
	 *            conjunto de instancias
	 * @return la entropia para las instancias
	 */
	private double computeEntropy(List<Instance> instances) {
	
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
			if(!instA.getOutputNominalValues(0).equals(instB.getOutputNominalValues(0))) {
				cutPoint = (instA.getInputRealValues(attrIndex) +
						instB.getInputRealValues(attrIndex)) / 2;
				values.add(new DecimalFormat("#.###").format(cutPoint));
			}
		}
		
		return values;
	}
	
	/**
	 * Construye la representacion del modelo a partir del arbol de clasificacion.
	 * 
	 * @param root raiz del arbol en cada nivel
	 * @param treeLevel nivel de profundidad del nodo raiz
	 * @return
	 */
	private String buildModel(Node root, int treeLevel) {
		
		String result = "";
		String tabs = "";
		for(int i = 0; i < treeLevel; i++) 
			tabs += "\t";
		
		if(root.isLeaf) {
			result += tabs + "class = " + root.classLabel + "\n";
			return result;
		}
		
		result += tabs + "if " + root.data.attrName;
		if(root.data.attrType == Attribute.NOMINAL)
			result += " == ";
		else 
			result += " <= ";
		
		result += root.data.attrValue + " then:\n";
		result += buildModel(root.left, treeLevel + 1);
		
		result += tabs + "else:\n";
		result += buildModel(root.right, treeLevel + 1);
		
		return result;
	}
	
}
