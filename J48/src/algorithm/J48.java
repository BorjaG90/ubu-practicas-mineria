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

	}

	public J48(myDataset dataset, double pruneConfidence) {
		this.dataset = dataset;
		this.confidence = pruneConfidence;
	}

	public void buildClassifier() {

		List<Instance> instances = Arrays.asList(dataset.IS.getInstances());
		
		this.entropy = calculateEntropy(instances);
		System.out.println("=======ENTROPIA: " + entropy +" ======");
		root = buildTree(instances);
	}

	private Node buildTree(List<Instance> instances) {

		Node root = new Node();
		int numAttr = Attributes.getNumAttributes();
		
		// crear el metodo check same class
		if (checkSameClass(instances) || numAttr == 0) {
			root.isLeaf = true;
			return root;
		}

		double gain;
		int attrIndex;
		for (int i = 0; i < numAttr; i++) {
			// OBTENER POSIBLES VALORES DEL ATRIBUTO: funcion getValues
			gain = calculateGain(instances, i, );
		}

		return root;
	}
	
//	private List<String> getValues(int attrIndex) {
//		
//		List<String> values = new ArrayList<String>();
//		Attribute attr = Attributes.getAttribute(attrIndex);
//		if(attr.getType() == Attribute.NOMINAL) {
//			// TODO CONVERTIR A UNA LISTA BUENA
//			values = new ArrayList<String>(attr.getNominalValuesList());
//		} else {
//			String[] classes = dataset.getClasses();
//			// ORDENAR POR EL ATRIBUTO NUMERICO
//			for(int i = 1; i < dataset.getnData(); i++) {
//				if(classes[i - 1] != classes[i]) {
//					// HACER PUNTO DE CORTE
//				}
//				
//				// Obtiene las instancias consecutivas para calcular el corte
//				dataset.IS.getInstance(i - 1).getInputRealValues(attrIndex);
//				dataset.IS.getInstance(i).getInputRealValues(attrIndex);
//			}
//		}
//		
//		return values;
//	}
	
	// OK
	// REFACTORIZAR EL METODO EN DOS
	private double calculateGain(List<Instance> instances, int attrIndex, String attrValue) {

		Attribute attr = Attributes.getAttribute(attrIndex);
		Map<Boolean, List<Instance>> dict = new HashMap<Boolean, List<Instance>>();
				
		double gain = entropy;
		boolean status;
		
		if (attr.getType() == Attribute.NOMINAL) {
			// El bucle separa las instancias que tienen el valor del
			// atributo de las que no para poder calcular la ganancia
			for(Instance i : instances) {
				status = i.getInputNominalValues(attrIndex) == attrValue;
				if(!dict.containsKey(status))
					dict.put(status, new ArrayList<Instance>());
				dict.get(status).add(i);
			}
		} else { // ATRIBUTOS NUMERICOS Y/O REALES
			Double attrToDouble = Double.parseDouble(attrValue);
			for(Instance i : instances) {
				status = i.getInputRealValues(attrIndex) <= attrToDouble;
				if(!dict.containsKey(status))
					dict.put(status, new ArrayList<Instance>());
				dict.get(status).add(i);
			}
		}
		
		for(Boolean key : dict.keySet())
			gain -= dict.get(key).size() / (double) instances.size() *
					calculateEntropy(dict.get(key));

		return gain;
	}
	
	/**
	 * Calcula la entropia para un conjunto de instancias.
	 * 
	 * @param instances conjunto de instancias
	 * @return la entropia para las instancias
	 */
	private double calculateEntropy(List<Instance> instances) {
		
		Map<String, List<Instance>> dict = mapInstancesToClasses(instances);
		double entropy = 0.0;
		double p;
		
		for(String label : dict.keySet()) {
			p = (dict.get(label).size() / (double) instances.size());
			entropy += -p * (Math.log(p) / Math.log(2));
		}
		return entropy;
	}
	
    /**
     * Crea un diccionario en el que se mapea cada instancia de una lista
     * a la clase a la que pertenece.
     * 
     * @param instances, lista de instancias.
     * @param classes, array con las etiquetas de todas las clases del dataset.
     * @return dict, diccionario con los indices de las instancias de cada calse.
     */
    private Map<String, List<Instance>> mapInstancesToClasses(List<Instance> instances) {
    	
    	Map<String, List<Instance>> dict = new HashMap<String, List<Instance>>();
    	   	
    	for(Instance i : instances) {
    		String c = i.getOutputNominalValues(0); // Obtiene la clase de la instancia
    		if(!dict.containsKey(c))
    			dict.put(c, new ArrayList<Instance>());
    		dict.get(c).add(i);
    	}
    	return dict;
    }
    
	/**
	 * sortByAttribute
	 * Metodo que ordena una lista de instancias por uno de sus atributos. Se
	 * utiliza solo para atributos continuos.
	 * El atributo de la instancia se identifica a traves de su indice dentro
	 * del dataset. 
	 * 
	 * @param instances lista de instancias del dataset
	 * @param attrIndex indice del atributo dentro del dataset
	 * @return lista ordenada de instancias
	 */
	private List<Instance> sortByAttribute(List<Instance> instances, int attrIndex) {
		
		List<Instance> sorted = new ArrayList<Instance>(instances);
		Collections.sort(sorted, new Comparator<Instance>() {

			@Override
			public int compare(Instance a, Instance b) {
				double valueA = a.getInputRealValues(attrIndex);
				double valueB = b.getInputRealValues(attrIndex);
				
				if(valueA < valueB)
					return -1;
				else if (valueA > valueB)
					return 1;
				else
					return 0;
			}
			
		});
		return sorted;
	}

	// add parameter to the method
	public String classifyInstance() {
		return "";
	}

	public String getModel() {
		return "";
	}

	
	
// ========================== CODIGO DE BORJA ================================
	
	/**
	 * Obtiene la ganancia de un nodo
	 * 
	 * @param conjunto
	 *            Entropia del conjunto
	 * @param clases
	 *            Entropia de los hijos
	 * @param instancias
	 *            Nº de instancias por hijo
	 * @param instanciasTotal
	 *            Nº total de instancias del conjunto
	 * @return
	 */
	public double obtenerGanancia(double conjunto, double[] clases, int[] instancias, int instanciasTotal) {
		double ganancia = conjunto;
		for (int i = 0; i < clases.length; i++) {
			ganancia -= ((double) instancias[i] - (double) instanciasTotal) * clases[i];
		}
		return ganancia;
	}

	/**
	 * Calculamos la entropia de un conjunto pasado como una tripleta
	 * 
	 * @param tripletas
	 *            Conjunto de instancias
	 */
	public double obtenerEntropia(Triplet[] tripletas) {
		double salida = 0;
		Map<String, Double> dict = new HashMap<String, Double>();
		for (int i = 0; i < tripletas.length; i++) {
			if (dict.containsKey(tripletas[i].output)) {
				dict.put(tripletas[i].output, (double) dict.get(tripletas[i].output) + 1);
			} else {
				dict.put(tripletas[i].output, (double) 0);
			}
		}
		Iterator it = dict.keySet().iterator();
		while (it.hasNext()) {
			Object key = it.next();
			salida -= dict.get(key) / (double) tripletas.length
					* (Math.log(dict.get(key) / (double) tripletas.length) / Math.log(2));
		}
		return salida;
	}

	/**
	 * Devuelve un array de puntos de corte en las que cambia la clase donde
	 * justo despues hay que realizar un corte
	 * 
	 * @param tripletas
	 *            tripletas ordenadas
	 * @return array de cortes
	 */
	/*
	 * public double[] devolverCortes(Triplet[] tripletas){ List<Double>
	 * cortes=new ArrayList<Double>(); String clase=tripletas[0].output; for
	 * (int i=1; i<tripletas.length; i++){ if(clase!=tripletas[i].output){
	 * cortes.add((tripletas[i].value + tripletas[i-1].value)/2); //Añadimos el
	 * valor del corte clase=tripletas[i].output; } } double[] salida=new
	 * double[cortes.size()]; for(int i=0; i<cortes.size(); i++) salida[i] =
	 * cortes.get(i); return salida;
	 * 
	 * }
	 */
	/**
	 * Devuelve un array de ids de instancias en las que cambia la clase donde
	 * justo despues hay que realizar un corte
	 * 
	 * @param tripletas
	 * @return
	 */
	public int[] devolverCortes(Triplet[] tripletas) {
		List<Integer> cortes = new ArrayList<Integer>();
		String clase = tripletas[0].output;
		for (int i = 1; i < tripletas.length; i++) {
			if (clase != tripletas[i].output) {
				cortes.add(i - 1); // Añadimos el id previo al corte
				clase = tripletas[i].output;
			}
		}
		int[] salida = new int[cortes.size()];
		for (int i = 0; i < cortes.size(); i++)
			salida[i] = cortes.get(i);
		return salida;

	}

	/**
	 * Devuelve 2 Tripletas divididas por el punto de corte solo para numericos
	 * 
	 * @param tripletas
	 * @param id
	 */
	public Triplet[][] dividirTripleta(Triplet[] tripletas, int[] ids) {
		Triplet[][] salida = new Triplet[ids.length][];
		if (ids.length == 1) { // Si el conjunto se divide en 2
								// subconjuntos(numerico)
			int index = 1;
			while (tripletas[index].id != ids[0]) {
				index++;
			}
			Triplet[] tri1 = new Triplet[index];
			Triplet[] tri2 = new Triplet[tripletas.length - index];
			for (int i = 0; i < index; i++) {
				tri1[i] = tripletas[i];
			}
			for (int i = index; i < tripletas.length; i++) {
				tri2[i] = tripletas[i];
			}
			salida[0] = tri1;
			salida[1] = tri2;

		} else {// Es nominal
			int[] numeroDI = new int[ids.length]; // Numero de instancias
			// int clase=0;
			// int rep=0;
			for (int inst = 0, clase = 0, rep = 0; inst < tripletas.length; inst++) {
				if (tripletas[inst].id == ids[clase]) {
					rep++;
					numeroDI[clase] = rep;
					clase++;
					rep = 0;
				} else {
					rep++;
				}
			}
			for (int clase = 0; clase < numeroDI.length; clase++) {
				salida[clase] = new Triplet[clase];
				for (int inst = 0; inst < numeroDI[clase]; inst++) {
					salida[clase][inst] = tripletas[inst];
				}
			}
		}
		return salida;
	}

	/**
	 * Devuelve una lista de tripletas,ordenada por atributo con el id,valor y
	 * clase de cada instancia del array de instancias pasado
	 * 
	 * @param instances
	 *            Array de instancias
	 * @param dataset
	 * @param attribute
	 *            Atributo por el cual ordenar
	 * @return lista Lista ordenada por atributo de tripletas
	 */
	public Triplet[] obtenerTripleta(Instance[] instances, int attribute) {
		Triplet[] lista = new Triplet[instances.length];
		for (int i = 0; i < dataset.getnData(); i++) {
			lista[i] = new Triplet(i, instances[i].getAllInputValues()[attribute], dataset.getOutputAsString(i));
		}
		Arrays.sort(lista);
		return lista;
	}

	/**
	 * Clase node
	 * 
	 * @author Borja Gete & Plamen Peytov
	 *
	 */
	// class Node{
	// private int id;
	// private int father;
	// private String attribute;
	// private String value;
	// private double numValue;
	// private boolean isRoot;
	// private boolean isNumeric;
	// private List<Integer> sons;
	// /*Constructors*/
	// /**
	// * Constructor de nodo raiz con atributo numerico
	// * @param id
	// * @param attr
	// * @param value
	// */
	// public Node(int id,String attr,double value){
	// setId(id);
	// setNumValue(value);
	// setRoot(true);
	// this.isNumeric=true;
	// this.sons=new ArrayList<Integer>();
	// }
	// /**
	// * Constructor de nodo raiz con atributo no numerico
	// * @param id
	// * @param attr
	// * @param value
	// */
	// public Node(int id,String attr,String value){
	// setId(id);
	// setValue(value);
	// setRoot(true);
	// this.isNumeric=false;
	// this.sons=new ArrayList<Integer>();
	// }
	// /**
	// * Constructor de nodo con atributo numerico
	// * @param id
	// * @param attr
	// * @param value
	// * @param father
	// */
	// public Node(int id, String attr,double value,int father){
	// setId(id);
	// setNumValue(value);
	// setFather(father);
	// setRoot(false);
	// this.isNumeric=true;
	// this.sons=new ArrayList<Integer>();
	// }
	// /**
	// * Constructor de nodo con atributo no numerico
	// * @param id
	// * @param attr
	// * @param value
	// * @param father
	// */
	// public Node(int id, String attr,String value,int father){
	// setId(id);
	// setValue(value);
	// setFather(father);
	// setRoot(false);
	// this.isNumeric=false;
	// this.sons=new ArrayList<Integer>();
	// }
	//
	// /*Methods*/
	// //Set
	// public void setId(int id){
	// this.id=id;
	// }
	// public void setFather(int id){
	// this.father=id;
	// }
	// public void setAttribute(String attr){
	// this.attribute=attr;
	// }
	// public void setNumValue(double value){
	// this.numValue=value;
	// }
	// public void setValue(String value){
	// this.value=value;
	// }
	// public void setRoot(boolean root){
	// this.isRoot=root;
	// }
	// //Get
	// public int getId(){
	// return this.id;
	// }
	// public int getFather(){
	// return this.father;
	// }
	// public String getAttribute(){
	// return this.attribute;
	// }
	// public String getValue(){
	// return this.value;
	// }
	// public double getNumValue(){
	// return this.numValue;
	// }
	// /*Add*/
	// public void addSon(int id){
	// this.sons.add(id);
	// }
	// /*Del*/
	// public void delSon(int id){
	// this.sons.remove(id);
	// }
	// /*Querys*/
	// /**
	// * Devuelve True si el nodo es raiz del arbol
	// * @return
	// */
	// public boolean isRoot(){
	// return this.isRoot;
	// }
	// /**
	// * Devuelve True si el atributo del nodo es numerico
	// * @return
	// */
	// public boolean isNumeric() {
	// return this.isNumeric;
	// }
	// }//End of Class node
	class Triplet implements Comparable<Triplet> {
		public int id;
		public double value;
		public String svalue;
		public String output;

		public Triplet(int id, double value, String output) {
			this.id = id;
			this.value = value;
			this.output = output;
		}

		// public Triplet(int id,String value, String output){
		// this.id=id;
		// this.svalue=value;
		// this.output=output;
		// }
		@Override
		public int compareTo(Triplet t) {
			if (value < t.value)
				return -1;
			if (value > t.value)
				return 1;
			return 0;
		}
	}

	class Atributo {
		public int id;
		public int type;
		public String label;
		public double value;

		public Atributo(int id, int type, String label, double value) {
			this.id = id;
			this.value = value; // can be null
			this.label = label;
			this.type = type;
		}
	}
}
