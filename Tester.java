//package oneway;
import java.io.IOException;

public class Tester {
	public static void main(String args[]) throws IOException{
		CreateDistribution run = new CreateDistribution();
		run.createDistribution(args[0], args[1]);
	}
}
