package algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//import algorithm.C45Obsoleto.Triplet;
import keel.dataset.Instance;

public class J48 {
	public void crearNodo(){
		
	}
	/**
	 * Obtiene la ganancia de un nodo
	 * @param conjunto Entropia del conjunto
	 * @param clases Entropia de los hijos
	 * @param instancias Nº de instancias por hijo
	 * @param instanciasTotal Nº total de instancias del conjunto
	 * @return
	 */
	public float ganancia(float conjunto,float[] clases, int[] instancias,int instanciasTotal){
		float ganancia=conjunto;
		for(int i=0;i<clases.length;i++){
			ganancia-=((float)instancias[i]-(float)instanciasTotal)*clases[i];
		}
		return ganancia;
	}
	/**
	 * Calculamos la entropia de un conjunto pasado como una tripleta
	 * @param tripletas Conjunto de instancias
	 */
	public float entropia(Triplet[] tripletas){
		float salida=0;
		Map<String, Float> dict = new HashMap<String, Float>();
		for (int i=0; i<tripletas.length; i++){
			if(dict.containsKey(tripletas[i].output)){
				dict.put(tripletas[i].output, (float)dict.get(tripletas[i].output)+1);
			}else{
				dict.put(tripletas[i].output, (float)0);
			}
		}
		Iterator it = dict.keySet().iterator();
		while(it.hasNext()){
			Object key=it.next();
			salida-=dict.get(key)/(float)tripletas.length*(Math.log(dict.get(key)/(float)tripletas.length)/Math.log(2));
		}
		return salida;
	}
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
	 * Devuelve 2 Tripletas divididas por el punto de corte
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
				tri1[2]=tripletas[i];
			}
			salida[0]=tri1;
			salida[1]=tri2;
			
		}else{//Es nominal
			int[]numeroDI=new int[ids.length]; //Numero de instancias
			int j=0;
			int k=0;
			for(int i=0;i<tripletas.length;i++){
				if(tripletas[i].id==ids[j]){
					k++;
					numeroDI[j]=k;
					j++;
					k=0;
				}else{
					k++;
				}
			}
			for(int l=0;l<numeroDI.length;l++){
				salida[l]=new Triplet[j];
				for(int i=0;i<numeroDI[l];i++){
					salida[l][i]=tripletas[i];
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
	public Triplet[] obtenerTripleta(Instance[] instances,myDataset dataset,int attribute){
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
		private int numValue;
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
		public Node(int id,String attr,int value){
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
		public Node(int id, String attr,int value,int father){
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
		public void setNumValue(int value){
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
		public int getNumValue(){
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
		public String output;
		public Triplet(int id,double value, String output){
			this.id=id;
			this.value=value;
			this.output=output;
		}
		@Override
        public int compareTo(Triplet t) {
            if (value < t.value)
                return -1;
            if (value > t.value)
                return 1;
            return 0;
        }
	}
}
