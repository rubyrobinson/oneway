import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CreateDistribution {
	
	public void createDistribution(String parkingLotType, String segmentType) throws IOException{
		//Decide type of configuration
		int numSegments = (int) (2 + Math.random() * 10);
		int numParkingLots = numSegments - 1;
		File file = new File("config.txt");
		if (!file.exists()) {
			file.createNewFile();
		}
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		String content = numSegments + "\n";
			
		if(segmentType.equals("long")){
			for(int i=0; i<numSegments; i++){
				int segmentgLength = 30 + (int) (Math.random() * 50);
				content += segmentgLength + " ";
			}
		}
		else if(segmentType.equals("medium")){
			for(int i=0; i<numSegments; i++){
				int segmentgLength = 10 + (int) (Math.random() * 20);
				content += segmentgLength + " ";
			}
		}
		else if(segmentType.equals("short")){
			for(int i=0; i<numSegments; i++){
				int segmentgLength = 2 + (int) (Math.random() * 5);
				content += segmentgLength + " ";
			}
		}
		else if (segmentType.equals("alternateLong")){
			boolean large = true;
			for(int i=0; i<numParkingLots; i++){
				if(large){
					int parkingCapacity = 30 + (int) (Math.random() * 100);
					content += parkingCapacity + " ";
					large = false;
				}
				else{
					int parkingCapacity = 2 + (int) (Math.random() * 5);
					content += parkingCapacity + " ";
					large = true;
				}
			}
		}
		else if (segmentType.equals("alternateShort")){
			boolean large = false;
			for(int i=0; i<numParkingLots; i++){
				if(large){
					int parkingCapacity = 2 + (int) (Math.random() * 5);
					content += parkingCapacity + " ";
					large = true;
				}
				else{
					int parkingCapacity = 30 + (int) (Math.random() * 100);
					content += parkingCapacity + " ";
					large = false;
				}
			}
		}
		content += "\n";
	if(parkingLotType.equals("xlarge")){
		for(int i=0; i<numParkingLots; i++){
			int parkingCapacity = 50 + (int) (Math.random() * 100);
			content += parkingCapacity + " ";
		}
	}
	else if(parkingLotType.equals("large")){
		for(int i=0; i<numParkingLots; i++){
			int parkingCapacity = 10 + (int) (Math.random() * 20);
			content += parkingCapacity + " ";
		}
	}
	else if(parkingLotType.equals("medium")){
		for(int i=0; i<numParkingLots; i++){
			int parkingCapacity = 5 + (int) (Math.random() * 10);
			content += parkingCapacity + " ";
		}
	}
	else if(parkingLotType.equals("small")){
		for(int i=0; i<numParkingLots; i++){
			int parkingCapacity = 2 + (int) (Math.random() * 5);
			content += parkingCapacity + " ";
		}
	}
	else if(parkingLotType.equals("alternateLarge")){
		boolean large = true;
		for(int i=0; i<numParkingLots; i++){
			if(large){
				int parkingCapacity = 10 + (int) (Math.random() * 20);
				content += parkingCapacity + " ";
				large = false;
			}
			else{
				int parkingCapacity = 2 + (int) (Math.random() * 5);
				content += parkingCapacity + " ";
				large = true;
			}
		}
	}
	else {
		boolean large = false;
		for(int i=0; i<numParkingLots; i++){
			if(large){
				int parkingCapacity = 2 + (int) (Math.random() * 5);
				content += parkingCapacity + " ";
				large = true;
			}
			else{
				int parkingCapacity = 10 + (int) (Math.random() * 20);
				content += parkingCapacity + " ";
				large = false;
			}
		}
	}
	bw.write(content);
	bw.close();
		}
	}
