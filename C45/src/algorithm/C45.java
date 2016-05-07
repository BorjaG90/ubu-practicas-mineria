package algorithm;
/**
 * Clase C45
 * @author Plamen Peytov & Borja Gete
 *
 */
public class C45 {
	/**
	 * Metodo que crea el arbol de decision a partir de un dataset
	 */
	public void createTree(){}
	/**
	 * Metodo que devuelve el modelo obtenido a partir del arbol
	 * en un fichero
	 */
	public void returnModel(){}
	/**
	 * Metodo que evalua a que clase del modelo pertenece una instancia
	 */
	public void evaluate(){}
	
	/**
	 * Clase nodoC45
	 * @author Borja Gete & Plamen Peytov
	 *
	 */
	class nodoC45{
		private int id;
		private int father;
		private String attribute;
		private String value;
		private int numValue;
		private boolean isRoot;
		private boolean isNumeric;
		/*Constructors*/
		/**
		 * Constructor de nodo raiz con atributo numerico
		 * @param id
		 * @param attr
		 * @param value
		 */
		public nodoC45(int id,String attr,int value){
			setId(id);
			setNumValue(value);
			setRoot(true);
			this.isNumeric=true;
		}
		/**
		 * Constructor de nodo raiz con atributo no numerico
		 * @param id
		 * @param attr
		 * @param value
		 */
		public nodoC45(int id,String attr,String value){
			setId(id);
			setValue(value);
			setRoot(true);
			this.isNumeric=false;
		}
		/**
		 * Constructor de nodo con atributo numerico
		 * @param id
		 * @param attr
		 * @param value
		 * @param father
		 */
		public nodoC45(int id, String attr,int value,int father){
			setId(id);
			setNumValue(value);
			setFather(father);
			setRoot(false);
			this.isNumeric=true;
		}
		/**
		 * Constructor de nodo con atributo no numerico
		 * @param id
		 * @param attr
		 * @param value
		 * @param father
		 */
		public nodoC45(int id, String attr,String value,int father){
			setId(id);
			setValue(value);
			setFather(father);
			setRoot(false);
			this.isNumeric=false;
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
	}//End of Class NodoC45
}
