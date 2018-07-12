
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Random;
import java.util.concurrent.Semaphore;

public class VList implements DGraph{
	
	final private Lock mutex = new ReentrantLock();
	private vnode head;
	private vnode tail;
	private int limit;
	private Semaphore sem = null;
	final private Semaphore semHasReady = new Semaphore(0); 
	
	class vnode{
		
		private Vertex vertex;
		private int data;
		private int depends;
		vnode next;
		boolean removed;
	    enode head;
	    enode tail;
	    // final private Lock lock;
	    final private ReentrantLock lock;
	    Random r = new Random();
	    
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
			lock = new ReentrantLock();
		}
		
		//aumenta a dependencia desse vnode, precisa esperar retirar mais nodos da lista para poder ser processado
		public void dependsMore(){
			depends++;
		}
		//diminui a dependencia desse vnode, algum nodo que ele dependia foi removido da lista
		public void dependsLess() {
			depends--;
			if(depends == 0) {
				semHasReady.release();
			}
		}
		//retorna o inicio da lista de dependentes (quem depende desse vnode)
		public enode getDependents() {
			return head;
		}
		//retorna a quantidade de nodos que esse nodo depende
		public int getDepends() {
			return depends;
		}
		//retorna o enumerador que diz se esse vnode ee Head, Tail ou Message da lista
		public Vertex getVertex() {
			return vertex;
		}
		
		public boolean isDependent(int otherData) {
			float random = r.nextFloat();
			if(random <= 0.05) {
				return true;
			}
			
		return false;
		}
		
		public Lock getLock() {
			return lock;
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
					aux.getMe().getLock().lock();
					aux.getMe().dependsLess();
					aux.getMe().getLock().unlock();					
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
	
	
	public VList(int limit) {
		
		this.limit = limit;
		
		sem = new Semaphore(limit, true);

		head = new vnode(-1, Vertex.HEAD);
		tail = new vnode(-1, Vertex.TAIL);
		
		head.setNext(tail);
	}
	//adiciona um novo dado na lista para ser processado
	public void insert(int data) throws InterruptedException {

		mutex.lock();
		 try {
			 
			  sem.acquire();
		      vnode newVnode = new vnode(data, Vertex.MESSAGE);

			  newVnode.getLock().lock();
	          vnode aux = head;
	          
			  aux.getLock().lock();
	          while(aux.getNext().getVertex() != Vertex.TAIL) {
	        	  
	        	  
	        	  if((newVnode.isDependent(aux.getData())) && (aux.getVertex() != Vertex.HEAD)) { //checar se nodo que sera inserido depende do que esta sendo olhado
	        		  newVnode.dependsMore();
	        		  aux.insert(newVnode);  		  
	        	  }
				  
	        	  vnode temp = aux;
	        	  aux = aux.getNext();
				  aux.getLock().lock();
	        	  temp.getLock().unlock();
	          }
	          
	           // aux.getLock().lock();
			  
        	  if((newVnode.isDependent(aux.getData()))  && (aux.getVertex() != Vertex.HEAD)) { //checar se nodo que sera inserido depende do que esta sendo olhado
        		  newVnode.dependsMore();
        		  aux.insert(newVnode);
        	  }
	         
	          aux.setNext(newVnode); 
	          newVnode.setNext(tail);
	          
	          aux.getLock().unlock();
	          
	          if(newVnode.getDepends() == 0) {
	        	  semHasReady.release();
	          }
	            
	          newVnode.getLock().unlock();
	          
	          
	        } finally {
	            mutex.unlock();
	        }
	}
	//procura na lista por algum dado para processar
	public int process() throws InterruptedException {
		
		semHasReady.acquire();
		
		vnode aux = head;
		vnode passed;
		
		aux.getLock().lock();
		
		while(aux.getVertex() != Vertex.TAIL) {
			
			passed = aux;
			aux = aux.getNext();
			aux.getLock().lock();
			passed.getLock().unlock();
			
			//try {
		          if(!aux.isRemoved() && aux.getDepends() == 0 && (aux.getVertex() != Vertex.TAIL)) {
		        	  aux.removed();
  		            aux.getLock().unlock();
				
		        	  return aux.getData();
		         }    
		    
		        //} finally {
					//}
		}
		aux.getLock().unlock();
		return -1;
		
	}
	//a thread que processou um nodo chama esse metodo para procurar o nodo que ela processou
	//e remover esse nodo da lista
	public void remove(int data) throws InterruptedException{
		//mutex.lock();
		//try{
		vnode aux = head;
		vnode passed;
		
		aux.getLock().lock();
		aux.getNext().getLock().lock();
		
		while(aux.getVertex() != Vertex.TAIL) {
			if(aux.getNext().getData() == data) {
				
				vnode removed = aux.getNext();   // removed aponta para nodo a ser removido
				
				// aux.getLock().lock();
				// removed.getLock().lock();
				 //removed.getNext().getLock().lock();
				
				removed.destroyEdges(); //diminui contadores de dependencia de todos os nodos da lista de dependencias do nodo removido
				
				//try {
					  
			    aux.setNext(removed.getNext()); 
			    
			    //    } finally {
						sem.release();
			            aux.getLock().unlock();
			            removed.getLock().unlock();
						
			            //aux.getNext().getLock().unlock();
						// }
				
				break;
					
			}else {
				passed = aux;
				aux = aux.getNext();
				aux.getNext().getLock().lock();
				passed.getLock().unlock();
			}
			
			
		}
		//} finally {
       // mutex.unlock();
       //}
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
