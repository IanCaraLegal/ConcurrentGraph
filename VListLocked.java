
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class VListLocked implements DGraph {
	
	final private Lock mutex = new ReentrantLock();
	final private Condition notFull = mutex.newCondition();
	
	private vnode head;
	private vnode tail;
	private int limit;
	private int size;
	
	class vnode{
		
		private Vertex vertex;
		private int data;
		private int depends;
		vnode next;
		boolean removed;
	    enode head;
	    enode tail;

	    
	    class enode{
	    	private Vertex vertex;
	    	vnode whoIAm;
	    	enode next;
	    	
	    	public enode (vnode whoYouAre, Vertex vertex){
	    		whoIAm = whoYouAre;
	    		this.vertex = vertex;
	    		next = null;
	    	}
	    	
	    	//retorna o enumerador que diz se o nodo ee Head, Tail ou esta no meio da lista
	    	public Vertex getVertex() {
	    		return vertex;
	    	}
	    	
	    	//referencia para o vnode que esse enode representa
	    	public vnode getMe() {
	    		return whoIAm;
	    	}
	    	
	    	public enode getNext() {
	    		return next;
	    	}
	    	
	    	public void setNext(enode next) {
	    		this.next = next; 
	    	}
	    	
	    }
		
		
		public vnode(int data, Vertex vertex){
			
			this.data = data;
			this.vertex = vertex;
			depends = 0;
			head = new enode(null, Vertex.HEAD);
			tail = new enode(null, Vertex.TAIL);
			head.setNext(tail);	
			removed = false;
		}
		
		//aumenta a dependencia desse vnode, precisa esperar retirar mais nodos da lista para poder ser processado
		public void dependsMore(){
			depends++;
		}
		//diminui a dependencia desse vnode, algum nodo que ele dependia foi removido da lista
		public void dependsLess() {
			depends--;
		}
		//retorna o inicio da lista de dependentes (quem depende desse vnode)
		public enode getDependents() {
			return head;
		}
		//retorna a quantidade de nodos que esse nodo depende
		public int getDepends() {
			return depends;
		}
		
		public boolean isDependent(int otherData) {
			if(data % 2 == otherData % 2) {
				return true;
			}
			
			return false;
		}
		
		//retorna o enumerador que diz se esse vnode ee Head, Tail ou Message da lista
		public Vertex getVertex() {
			return vertex;
		}
		
		//dado que sera processado, que o nodo guarda
		public int getData() {
			return data;
		}
		
		public vnode getNext() {
			return next;
		}
		
		public void setNext(vnode next) {
			this.next = next;
		}
		//boolean que diz se esse nodo ja foi processado
		public boolean isRemoved() {
			return removed;
		}
		//quando esse nodo ee pego para ser processado ee chamado esse metodo
		public void removed() {
			removed = true;
		}
		
		//quando esse nodo ee removido da lista de vertices destroyedges ee chamado
		//diminui 1 da quantidade de dependencias de todos os dependentes desse nodo
		public void destroyEdges() { 
			enode aux = head;
			
			while(aux.getVertex() != Vertex.TAIL) {
				if(aux.getVertex() != Vertex.HEAD) {
					aux.getMe().dependsLess();					
				}				
				aux = aux.getNext();
			}
		}
		//adiciona mais um enode para a lista de arestas desse nodo
		public void insert(vnode newNode) {
			enode newEnode = new enode(newNode, Vertex.MESSAGE);
			
			enode aux = head;
			
			while(aux.getNext().getVertex() != Vertex.TAIL) {
				aux = aux.getNext();
			}
			
			aux.setNext(newEnode);
			newEnode.setNext(tail);
		}
		
		public String printe() {
			String l = " ";
			enode aux2 = head.getNext();
		    while(aux2.getVertex()!=Vertex.TAIL) {
			    l = l+aux2.getMe().getData()+",";
			    aux2 = aux2.getNext();
			}
			return l;
		}
		
	}
	
	
	public VListLocked() {
		
		limit = 256;

		head = new vnode(-1, Vertex.HEAD);
		tail = new vnode(-1, Vertex.TAIL);
		
		head.setNext(tail);
	}
	//adiciona um novo dado na lista para ser processado
	public void insert(int data) throws InterruptedException {

		mutex.lock();
		 try {
			 
			  while(this.size() == limit) {
				  notFull.await();
			  }
			 
		      vnode newVnode = new vnode(data, Vertex.MESSAGE);
	          vnode aux = head;

	          while(aux.getNext().getVertex() != Vertex.TAIL) {
	        	  
	        	  
	        	  if((newVnode.isDependent(aux.getData())) && (aux.getVertex() != Vertex.HEAD)) { //checar se nodo que sera inserido depende do que esta sendo olhado
	        		  newVnode.dependsMore();
	        		  aux.insert(newVnode);  		  
	        	  }
				  
	        	  aux = aux.getNext();
				
	          }

			  
        	  if((newVnode.isDependent(aux.getData())) && (aux.getVertex() != Vertex.HEAD)) { //checar se nodo que sera inserido depende do que esta sendo olhado
        		  newVnode.dependsMore();
        		  aux.insert(newVnode);
        	  }
	         
	          aux.setNext(newVnode); 
	          newVnode.setNext(tail);
	          this.size++;
	          
	        } finally {
	            mutex.unlock();
	        }
	}
	//procura na lista por algum dado para processar
	public int process() throws InterruptedException {
		
		mutex.lock();
		try {
			vnode aux = head;
			
			while(aux.getVertex() != Vertex.TAIL) {
			
			   aux = aux.getNext();

			        if(!aux.isRemoved() && aux.getDepends() == 0 && (aux.getVertex() != Vertex.TAIL)) {
			        	 aux.removed();
					
			        	return aux.getData();
			        }    
			}
			
			
		}finally {
			mutex.unlock();
		}
		
		return -1;
		
	}
	//a thread que processou um nodo chama esse metodo para procurar o nodo que ela processou
	//e remover esse nodo da lista
	public void remove(int data) throws InterruptedException{
	   mutex.lock();
	   try{
		   vnode aux = head;
		
		   while(aux.getVertex() != Vertex.TAIL) {
			   if(aux.getNext().getData() == data) {
				
				  vnode removed = aux.getNext();   // removed aponta para nodo a ser removido
				
				  removed.destroyEdges(); //diminui contadores de dependencia de todos os nodos da lista de dependencias do nodo removido
						  
			      aux.setNext(removed.getNext()); 	
			      
			      this.size--;
			      notFull.signal();
				
				  break;
					
			  }else {
				 aux = aux.getNext();
			  }
					
		 }
		} finally {
            mutex.unlock();
        }
	}
	
	public int size() {
		return size;
	}
	
	public String printVL() {
		
		String list = "Head ";
		vnode aux = head.getNext();
		while(aux.getVertex() != Vertex.TAIL) {		
			list = list + " " + aux.getData()+"<"+aux.getDepends()+">";
//			list = list + "["+aux.printe()+"]";
			aux = aux.getNext();
	
		}
		list = list + " Tail";

		return list;
	}
	
}

