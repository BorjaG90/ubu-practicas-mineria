package algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import keel.dataset.Attributes;
//import algorithm.C45Obsoleto.Triplet;
import keel.dataset.Instance;

public class J48 {
	public myDataset dataset;
	public J48(myDataset dataset){
		this.dataset=dataset;
	}
	public void iniciar(){
		List<Node> arbol= new LinkedList<Node>(); 
		String[] atributos = dataset.getNames();
		ID3(dataset.IS.getInstances(),atributos);
	}
	/**
	 * Devuelve true si todas las instancias pertenecen a la misma clase
	 * @param instancias
	 * @param dataset
	 */
	public boolean comprobarClase(Triplet[] instancias){
		String clase = dataset.getOutputAsString(instancias[0].id);
		boolean salida = true;
		for(int i=1;i<instancias.length && salida;i++){
			if(!dataset.getOutputAsString(instancias[i].id).equals(clase)){
				salida = false;
			}
		}
		return salida;
	}
	public void ID3(Instance[] ejemplos,String[] atributos){
		Triplet[] tripleta = obtenerTripleta(ejemplos,0);
		if(comprobarClase(tripleta)){ //Si todos los ejemplos pertenecen a la misma clase
			
		}else{
			for(int at=0;at<atributos.length; at++){
				//Si no esta clasificado ya
				double ganancia=0;
				int afectados=0;
				if(atributos[at]!=null){
					//Si es nominal
					if(dataset.getType(at)==dataset.NOMINAL){
						tripleta=obtenerTripleta(ejemplos,at);
						double entropia=obtenerEntropia(tripleta);
						//Obtener valores del atributo
						String[] valoresAttr=obtenerValoresNominales(at);
						int[] nInstAttr=new int[valoresAttr.length];
						//Obtener entropia de los hijos
						double[] entroHijos=new double[valoresAttr.length];
						//Por cada valor diferente del atributo
						for(int valor=0;valor<valoresAttr.length;valor++){
							//Añadimos las instancias que tienen ese atributo a tal valor
							List<Instance> listI=new ArrayList<Instance>();
							for(int inst=0;inst<ejemplos.length;inst++){
								if(Attributes.getInputAttribute(at).getNominalValue(inst).equals(valoresAttr[valor])){
									listI.add(ejemplos[inst]);
								}
							}
							Instance[] ie=new Instance[listI.size()];
							//Guardamos la entropia de ese valor del atributo en el array
							for(int n=0;n<listI.size();n++) ie[n]=listI.get(n);
							entroHijos[valor]=obtenerEntropia(obtenerTripleta(ie,at));
						}
						if(obtenerGanancia(entropia,entroHijos,nInstAttr,ejemplos.length)>ganancia){
							ganancia=obtenerGanancia(entropia,entroHijos,nInstAttr,ejemplos.length);
						}else if(obtenerGanancia(entropia,entroHijos,nInstAttr,ejemplos.length)==ganancia){
						}
						
						
					}else{
						
					}
				}
			}
		}
	}
	/**
	 * Obtiene una lista de los diferentes valores nominales
	 * que puede tener un atributo
	 */
	public String[] obtenerValoresNominales(int attr){
		String x[]= new String[Attributes.getInputAttribute(attr).getNominalValuesList().size()];
		Attributes.getInputAttribute(attr).getNominalValuesList().toArray(x);
		return x;
	}
	/**
	 * Obtiene la lista completa de atributos a revisar
	 * Añadiendo cada valor de atributo continuo como un atributo individual
	 * @param dataset
	 * @return
	 */
	public Atributo[] obtenerAtributos(){
		List<Atributo> puntos=new ArrayList<Atributo>();
		int id=0;
		for (int i=0;i<dataset.getnInputs();i++){
			//Si es contino
			if(dataset.getType(i)==dataset.INTEGER||dataset.getType(i)==dataset.REAL){
				int[] cortes=devolverCortes(obtenerTripleta(dataset.IS.getInstances(),i));
				//double[] cortecillos=new double[cortes.length];
				for(int j=0;j<cortes.length;j++){
					//cortecillos[j]=(dataset.IS.getInstance(cortes[j]).getAllInputValues()[i]+dataset.IS.getInstance(cortes[j]+1).getAllInputValues()[i])/2;
					puntos.add(new Atributo(id,dataset.getType(i),dataset.getNames()[i],(dataset.IS.getInstance(cortes[j]).getAllInputValues()[i]+dataset.IS.getInstance(cortes[j]+1).getAllInputValues()[i])/2));
				}
				//puntos.add(new Atributo(id,dataset.getType(i),dataset.getNames()[i],cortecillos[j])));
			}else{ //si es nominal
				puntos.add(new Atributo(id,dataset.getType(i),dataset.getNames()[i],0));
			}
		}
		Atributo[] salida=new Atributo[puntos.size()];
		for(int i=0; i<puntos.size(); i++) salida[i] = puntos.get(i);
		return salida;
		
	}
	/**
	 * Obtiene la ganancia de un nodo
	 * @param conjunto Entropia del conjunto
	 * @param clases Entropia de los hijos
	 * @param instancias Nº de instancias por hijo
	 * @param instanciasTotal Nº total de instancias del conjunto
	 * @return
	 */
	public double obtenerGanancia(double conjunto,double[] clases, int[] instancias,int instanciasTotal){
		double ganancia=conjunto;
		for(int i=0;i<clases.length;i++){
			ganancia-=((double)instancias[i]-(double)instanciasTotal)*clases[i];
		}
		return ganancia;
	}
	/**
	 * Calculamos la entropia de un conjunto pasado como una tripleta
	 * @param tripletas Conjunto de instancias
	 */
	public double obtenerEntropia(Triplet[] tripletas){
		double salida=0;
		Map<String, Double> dict = new HashMap<String, Double>();
		for (int i=0; i<tripletas.length; i++){
			if(dict.containsKey(tripletas[i].output)){
				dict.put(tripletas[i].output, (double)dict.get(tripletas[i].output)+1);
			}else{
				dict.put(tripletas[i].output, (double)0);
			}
		}
		Iterator it = dict.keySet().iterator();
		while(it.hasNext()){
			Object key=it.next();
			salida-=dict.get(key)/(double)tripletas.length*(Math.log(dict.get(key)/(double)tripletas.length)/Math.log(2));
		}
		return salida;
	}
	/**
	 * Devuelve un array de puntos de corte en las que cambia la clase
	 * donde justo despues hay que realizar un corte
	 * @param tripletas tripletas ordenadas
	 * @return array de cortes
	 */
	/*public double[] devolverCortes(Triplet[] tripletas){
		List<Double> cortes=new ArrayList<Double>();
		String clase=tripletas[0].output;
		for (int i=1; i<tripletas.length; i++){
			if(clase!=tripletas[i].output){
				cortes.add((tripletas[i].value + tripletas[i-1].value)/2); //Añadimos el valor del corte
				clase=tripletas[i].output;
			}
		}
		double[] salida=new double[cortes.size()];
		for(int i=0; i<cortes.size(); i++) salida[i] = cortes.get(i);
		return salida;
		
	}*/
	/**
	 * Devuelve un array de ids de instancias en las que cambia la clase
	 * donde justo despues hay que realizar un corte
	 * @param tripletas
	 * @return
	 */
	public int[] devolverCortes(Triplet[] tripletas){
		List<Integer> cortes=new ArrayList<Integer>();
		String clase=tripletas[0].output;
		for (int i=1; i<tripletas.length; i++){
			if(clase!=tripletas[i].output){
				cortes.add(i-1); //Añadimos el id previo al corte
				clase=tripletas[i].output;
			}
		}
		int[] salida=new int[cortes.size()];
		for(int i=0; i<cortes.size(); i++) salida[i] = cortes.get(i);
		return salida;
		
	}
	/**
	 * Devuelve 2 Tripletas divididas por el punto de corte solo para numericos
	 * @param tripletas
	 * @param id
	 */
	public Triplet[][] dividirTripleta(Triplet[] tripletas,int[] ids){
		Triplet[][] salida=new Triplet[ids.length][];
		if(ids.length==1){ //Si el conjunto se divide en 2 subconjuntos(numerico)
			int index=1;
			while(tripletas[index].id!=ids[0]){
				index++;
			}
			Triplet[] tri1 =new Triplet[index];
			Triplet[] tri2 =new Triplet[tripletas.length-index];
			for(int i=0;i<index;i++){
				tri1[i]=tripletas[i];
			}
			for(int i=index;i<tripletas.length;i++){
				tri2[i]=tripletas[i];
			}
			salida[0]=tri1;
			salida[1]=tri2;
			
		}else{//Es nominal
			int[]numeroDI=new int[ids.length]; //Numero de instancias
			//int clase=0;
			//int rep=0;
			for(int inst=0, clase=0,rep=0;inst<tripletas.length;inst++){
				if(tripletas[inst].id==ids[clase]){
					rep++;
					numeroDI[clase]=rep;
					clase++;
					rep=0;
				}else{
					rep++;
				}
			}
			for(int clase=0;clase<numeroDI.length;clase++){
				salida[clase]=new Triplet[clase];
				for(int inst=0;inst<numeroDI[clase];inst++){
					salida[clase][inst]=tripletas[inst];
				}
			}
		}
		return salida;
	}
	/**
	 * Devuelve una lista de tripletas,ordenada por atributo
	 *  con el id,valor y clase de cada instancia
	 *  del array de instancias pasado
	 * @param instances Array de instancias
	 * @param dataset
	 * @param attribute Atributo por el cual ordenar
	 * @return lista Lista ordenada por atributo de tripletas
	 */
	public Triplet[] obtenerTripleta(Instance[] instances,int attribute){
		Triplet[] lista =new Triplet[instances.length];
		for(int i=0;i<dataset.getnData();i++){
			lista[i]=new Triplet(i,instances[i].getAllInputValues()[attribute],dataset.getOutputAsString(i));
		}
		Arrays.sort(lista);
		return lista;
	}
	/**
	 * Clase node
	 * @author Borja Gete & Plamen Peytov
	 *
	 */
	class Node{
		private int id;
		private int father;
		private String attribute;
		private String value;
		private double numValue;
		private boolean isRoot;
		private boolean isNumeric;
		private List<Integer> sons;
		/*Constructors*/
		/**
		 * Constructor de nodo raiz con atributo numerico
		 * @param id
		 * @param attr
		 * @param value
		 */
		public Node(int id,String attr,double value){
			setId(id);
			setNumValue(value);
			setRoot(true);
			this.isNumeric=true;
			this.sons=new ArrayList<Integer>();
		}
		/**
		 * Constructor de nodo raiz con atributo no numerico
		 * @param id
		 * @param attr
		 * @param value
		 */
		public Node(int id,String attr,String value){
			setId(id);
			setValue(value);
			setRoot(true);
			this.isNumeric=false;
			this.sons=new ArrayList<Integer>();
		}
		/**
		 * Constructor de nodo con atributo numerico
		 * @param id
		 * @param attr
		 * @param value
		 * @param father
		 */
		public Node(int id, String attr,double value,int father){
			setId(id);
			setNumValue(value);
			setFather(father);
			setRoot(false);
			this.isNumeric=true;
			this.sons=new ArrayList<Integer>();
		}
		/**
		 * Constructor de nodo con atributo no numerico
		 * @param id
		 * @param attr
		 * @param value
		 * @param father
		 */
		public Node(int id, String attr,String value,int father){
			setId(id);
			setValue(value);
			setFather(father);
			setRoot(false);
			this.isNumeric=false;
			this.sons=new ArrayList<Integer>();
		}
		
		/*Methods*/
		//Set
		public void setId(int id){
			this.id=id;
		}
		public void setFather(int id){
			this.father=id;
		}
		public void setAttribute(String attr){
			this.attribute=attr;
		}
		public void setNumValue(double value){
			this.numValue=value;
		}
		public void setValue(String value){
			this.value=value;
		}
		public void setRoot(boolean root){
			this.isRoot=root;
		}
		//Get
		public int getId(){
			return this.id;
		}
		public int getFather(){
			return this.father;
		}
		public String getAttribute(){
			return this.attribute;
		}
		public String getValue(){
			return this.value;
		}
		public double getNumValue(){
			return this.numValue;
		}
		/*Add*/
		public void addSon(int id){
			this.sons.add(id);
		}
		/*Del*/
		public void delSon(int id){
			this.sons.remove(id);
		}
		/*Querys*/
		/**
		 * Devuelve True si el nodo es raiz del arbol
		 * @return
		 */
		public boolean isRoot(){
			return this.isRoot;
		}
		/**
		 * Devuelve True si el atributo del nodo es numerico
		 * @return
		 */
		public boolean isNumeric() {  
			return this.isNumeric;
		}  
	}//End of Class node
	class Triplet implements Comparable<Triplet>{ 
		public int id; 
		public double value; 
		public String svalue;
		public String output;
		public Triplet(int id,double value, String output){
			this.id=id;
			this.value=value;
			this.output=output;
		}
//		public Triplet(int id,String value, String output){
//			this.id=id;
//			this.svalue=value;
//			this.output=output;
//		}
		@Override
        public int compareTo(Triplet t) {
            if (value < t.value)
                return -1;
            if (value > t.value)
                return 1;
            return 0;
        }
	}
	class Atributo{
		public int id;
		public int type;
		public String label;
		public double value;
		public Atributo(int id,int type,String label,double value){
			this.id=id;
			this.value=value; //can be null
			this.label=label;
			this.type=type;
		}
	}
}
