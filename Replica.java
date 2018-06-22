
public class Replica {
	
	 
    private static int numThreads = 16;
	private static Thread workingThreads[];

	public static void main(String[] args) {
		
		 workingThreads = new Thread[numThreads];
		
		DGraph graph = new VList();
		
		for (int i=0; i < numThreads; i++) {
            workingThreads[i] = new Thread(new Worker(graph));
            workingThreads[i].start();
        }
		
		
		for(int i = 1; i < 10000; i++) {
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
