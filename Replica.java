
public class Replica {
	
	 
    private static int numThreads = 7;
	private static Thread workingThreads[];
	private static int limit = 256;

	public static void main(String[] args) {
		
		 workingThreads = new Thread[numThreads];
		
		//DGraph graph = new VList(limit);
		DGraph graph = new VListLocked(limit);
		
		long startTime = System.nanoTime();
		
		for (int i=0; i < numThreads; i++) {
            workingThreads[i] = new Thread(new Worker(graph, startTime));
            workingThreads[i].start();
        }
		
		//System.out.println("Processors: " + Runtime.getRuntime().availableProcessors());
		
		
		
		for(int i = 1; i < 1000000; i++) {
			try {
				graph.insert(i);
		        //System.out.println(graph.printVL());
				
			}catch(InterruptedException ex){
				System.out.println(ex);
				break;
			}
		}
		
		
		
		
		

	}

}
