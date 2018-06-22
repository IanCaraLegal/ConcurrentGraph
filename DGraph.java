
public interface DGraph {
	public void insert(int data) throws InterruptedException;
	public int process() throws InterruptedException;
	public void remove(int data) throws InterruptedException; 
	public String printVL();
		
}
