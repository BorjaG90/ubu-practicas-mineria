package algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

//import algorithm.C45Obsoleto.Triplet;
import keel.dataset.Instance;

public class C45 {
	public myDataset dataset; 
	public C45(myDataset dataset){
		this.dataset=dataset;
	}
	public void iniciar(){
		//
		for(){
			
		}
	}
	
	
	//-----------------------------------------//
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
