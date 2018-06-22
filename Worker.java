
public class Worker implements Runnable {
	
	DGraph graph;

	
	public Worker(DGraph graph) {
		this.graph = graph;
	}
	
	
	public void run() {
		
		while(true) {
			int data;
			try {
                data = graph.process();
            } catch (InterruptedException ex) {
                System.err.println(ex);
                break;
            }
			
			if(data != -1) {
				System.out.println("Message: " + data);
				System.out.println(Thread.currentThread());
				
				try {
	                graph.remove(data);
 System.out.println(graph.printVL());
	            } catch (InterruptedException ex) {
	                System.err.println(ex);
	                break;
	            }
			}
		}
	}
}
