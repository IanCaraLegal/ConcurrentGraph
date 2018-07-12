
public class Worker implements Runnable {
	
	DGraph graph;
	private long startTime;

	
	public Worker(DGraph graph, long startTime) {
		this.graph = graph;
		this.startTime = startTime;
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
//				System.out.println("Message: " + data);
//				System.out.println(Thread.currentThread());
				long currentTime = System.nanoTime();
				while(System.nanoTime() - currentTime < 50000) {
					
				}
				
				try {
	                graph.remove(data);
// System.out.println(graph.printVL());
	                
	                if(data == 999999) {
	                	long endTime = System.nanoTime();
	                	System.out.println("Took "+(endTime - startTime) + " ns"); 
	                }
	            
	            } catch (InterruptedException ex) {
	                System.err.println(ex);
	                break;
	            }
			}
		}
	}
}
