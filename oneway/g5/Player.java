package oneway.g5;

import oneway.sim.MovingCar;
import oneway.sim.Parking;

import java.util.*;
import static java.lang.Math.abs;

public class Player extends oneway.sim.Player {
	// if the parking lot is almost full
	// it asks the opposite direction to yield
	private static double AlmostFull = 0.6;
	
	// private static boolean flush;
	
	public enum States {
		NORMAL, FLUSH
	}
	
	private States state;
	
	public Player() {
	}
	
	public void init(int nsegments, int[] nblocks, int[] capacity) {
		this.nsegments = nsegments;
		this.nblocks = nblocks;
		this.capacity = capacity.clone();
		this.state = States.NORMAL;
	}
	
	public void setLights(MovingCar[] movingCars, Parking[] left,
						  Parking[] right, boolean[] llights, boolean[] rlights) {
		/*
		 for (MovingCar car : movingCars) {
			 if (!(car.equals(null))) {
				 System.out.println(car.block);
			 System.out.println(car.segment);
			 }
		 }
		 */
		
		// variables
		boolean goLeft = true;
		int totLeft = 0;
		int totRight = 0;
		
		switch (this.state) {
				
			//set all lights to green -> turn off lights if in danger -> flush if deadlock
			case NORMAL: {
				//set all lights to green
				for (int i = 0; i != nsegments; ++i) {
					llights[i] = true;
					rlights[i] = true;
				}
				
				boolean[] indanger = new boolean[nsegments + 1];
				int[] trafficFlownow = new int[nsegments];
				trafficFlownow = trafficFlow(movingCars);
			
				// find out almost full parking lots
				
				for (int i = 1; i != nsegments; ++i) {
					
					int goingright = 0;
					int goingleft = 0;
					
					if (trafficFlownow[i - 1]>0)
						goingright = trafficFlownow[i - 1];
					
					if (trafficFlownow[i]<0)
						goingleft = -trafficFlownow[i];
					
					if (left[i].size() + right[i].size()
						+ goingright+ goingleft > (capacity[i]) - 2) {
						// System.out.printf("we are in danger!!\n");
						indanger[i] = true;
						
					}
					
				}
				for (int i = 0; i != nsegments; ++i) {
					// if right bound has car
					// and the next parking lot is not in danger
					// if (right[i].size() > 0 && !hasTraffic(movingCars, i, -1)) {
					if (hasTraffic(movingCars, i, -1)) {
						rlights[i] = false;
					}
					
					// if (left[i + 1].size() > 0 && !hasTraffic(movingCars, i, 1))
					// {
					if (hasTraffic(movingCars, i, 1)) {
						llights[i] = false;
					}
					
					// if both left and right is on
					// find which dir is in more danger
					if (rlights[i] && llights[i] && right[i].size() > 0
												 && left[i + 1].size() > 0) {
						rlights[i] = false;
					}
				}
				int counter=0;
				while (counter++ != nsegments) {
					LinkedList<Integer> m = new LinkedList<Integer>();
					m = chainOfDanger(indanger, counter);
					System.out.println(m);
					System.out.println("******************");
					if (m.size()==1){
					if (indanger[counter]) {
						System.out.printf("%d is in danger\n", counter);
						int lefttime = cleartime(counter - 1, movingCars);
						System.out.printf("%d the left time is\n ", lefttime);
						int righttime = cleartime(counter, movingCars);
						System.out.printf("%d the right time is \n", righttime);
						if (lefttime > righttime) {
							if (!indanger[counter + 1]) {
								llights[counter] = false;
								if (!rlights[counter])
									rlights[counter-1] = false; 
								System.out
								.printf("shut off the left light %d\n", counter);
							} else {
								llights[counter] = false;
								rlights[counter- 1] = true;
								System.out
								.printf("shut off the left light %d\n", counter);
								System.out
								.printf("turn on the right light %d\n", counter-1);
							}
						} else {
							if (!indanger[counter- 1]) {
								rlights[counter - 1] = false;
								if (!llights[counter-1])
									llights[counter] = false;
								System.out.printf("shut off the right light %d\n",
												  counter);
							} else {
								llights[counter] = true;
								rlights[counter - 1] = false;
								System.out
								.printf("shut off the left light %d\n", counter);
								System.out
								.printf("turn on the right light %d\n", counter-1);
							}
						}
					}
					
					}
					if (m.size()>1) {
						allindanger(counter, counter+m.size()-1, movingCars, llights, rlights);
						counter = counter+m.size();
					}

				}
				//last resort safetyCheck
				safetyCheck(movingCars, left, right, llights, rlights);

				// in case of no car is moving due to inDanger
				// check for such condition, then open up a light
				// one by one
				boolean alloff = true;
				boolean nonetraffic = true;
				
				for (int i = 0; i != nsegments; i++) {
					if ((rlights[i] && right[i].size() > 0)
						|| (llights[i] && left[i + 1].size() > 0))
						alloff = false;
					if (trafficFlownow[i] != 0)
						nonetraffic = false;
				}
				
				// checking priority of flushing left or right
				totLeft = sumLeft(movingCars, left);
				totRight = sumRight(movingCars, right);
				
				if (totLeft > totRight)
					goLeft = true;
				else
					goLeft = false;
				
				// check if flush condition is met
				if (alloff && nonetraffic) {
					for (int i = 0; i != nsegments; i++) {
						if (left[i + 1].size() > 0) {
							this.state = States.FLUSH;
							llights[i] = true;
							for (int j = i; j != nsegments; j++) {
								llights[j] = true;
							}
							return;
						}
					}
					for (int i = nsegments; i != 1; i--) {
						if (right[i - 1].size() > 0) {
							rlights[i] = true;
							for (int j = i; j != nsegments; j++) {
								rlights[j] = true;
							}
							return;
						}
					}
				}
				
			}
				break;
				
			case FLUSH: {
				System.out.println("******flush state*******");
				
				for (int i = 0; i != nsegments; ++i) {
					llights[i] = false;
					rlights[i] = false;
				}
				// rlights[0]=false;
				// llights[nsegments]=false;
				
				totLeft = sumLeft(movingCars, left);
				totRight = sumRight(movingCars, right);
				
				if (goLeft) {
					if (totLeft != 0)
				flushleft(llights, rlights, left, right, movingCars);
					else if (totRight != 0)
						flushright(llights, rlights, left, right, movingCars);
					if (totLeft == 0 && totRight == 0)
						this.state = States.NORMAL;
					
				} else {
					if (totRight != 0)
						flushright(llights, rlights, left, right, movingCars);
					else if (totLeft != 0)
						flushleft(llights, rlights, left, right, movingCars);
					if (totLeft == 0 && totRight == 0)
						this.state = States.NORMAL;
				}
				
			}
		}
	}
	
	public void safetyCheck(MovingCar[] movingCars, Parking[] left,
						  Parking[] right, boolean[] llights, boolean[] rlights) {
		
		//used to compare which side to favor traffic
		int totLeft = sumLeft(movingCars, left);
		int totRight = sumRight(movingCars, right);
	
		// checks for crash prevention between 3 segments
		for(int i = 0; i < nsegments -2; i++){
			if(llights[i+1] && rlights[i+1]){
				for(MovingCar car1 : movingCars){
					if(car1.segment == i && car1.dir > 0){
						for(MovingCar car2 : movingCars){
							if(car2.segment == i+2 && car2.dir < 0){
								if((nblocks[i]-car1.block+1) == car2.block){
									if(totLeft > totRight){
										rlights[i+1] = false;
										System.out.printf("\nit is here we shut off the right light %d\n", i);
									}else
										llights[i+1] = false;
								}
							}	
						}
					}
				}
			}
		}

		// checks for crash prevention between 2 segments and a parking lot
		// case: ]--- --- 01>[i]--- --- ---[<1]
		for(int i = 0; i < nsegments - 2; i++){
			if(left[i+1].size() > 0){
				for(MovingCar car : movingCars){
					if(car.segment == i && car.dir > 0 && (nblocks[i] - car.block - 1) == 1){
						if(totLeft > totRight)
							rlights[i] = false;
						else
							llights[i] = false;
					}
				}
			}
		}
		// case: [1>]--- --- ---[i]<01--- ---
		for(int i = 0; i < nsegments - 1; i++){
			if(right[i].size() > 0){
				for(MovingCar car : movingCars){
					if(car.segment == i+1 && car.dir < 0 && car.block == 1){
						if(totLeft > totRight)
							rlights[i] = false;
						else
							llights[i] = false;
					}
				}
			}
		}
		
		// checks for edge cases of the 3 segment crash prevention
		for(MovingCar car : movingCars){
			if(car.segment == (nsegments-2) && car.dir > 0 && (nblocks[nsegments-2] - car.block -1) == 1
			   && rlights[nsegments-1]){
				if(totLeft > totRight)
					rlights[nsegments-1] = false;
				else
					llights[nsegments-1] = false;
			}
			if(car.segment == 1 && car.dir < 0 && car.block == 1
			   && llights[0]){
				if(totLeft > totRight)
					rlights[0] = false;
				else
					llights[0] = false;
			}
		}
	
		// checks for edge cases of incoming traffic
		for(MovingCar car : movingCars){
		    if(car.segment == 0 && car.dir <0)
				rlights[0] = false;
		    if(car.segment == nsegments-1 && car.dir > 0)
				llights[nsegments-1] = false;
		}

	}


	// check if the segment has traffic
	private boolean hasTraffic(MovingCar[] cars, int seg, int dir) {
		for (MovingCar car : cars) {
			if (car.segment == seg && car.dir == dir)
				return true;
		}
		return false;
	}
	
	private void flushleft(boolean[] llights, boolean[] rlights,
						   Parking[] left, Parking[] right, MovingCar[] car) {
		System.out.println("the left flush");
		for (int i = 0; i != nsegments; ++i) {
			llights[i] = true;
			rlights[i] = false;
		}
		int[] trafficFlownow = new int[nsegments];
		
		trafficFlownow = trafficFlow(car);
		if (left[nsegments - 1].size() == 0
			&& trafficFlownow[nsegments - 1] >= 0) {
			rlights[nsegments - 1] = true;
			System.out.println("set the last light to green\n");
		}
		if (nsegments - 2 > 0) {
			for (int j = nsegments - 2; j != 0; j--) {
				if (left[j].size() == 0) {
					
					if (trafficFlownow[j + 1] >= 0) {
						if (j + 2 < nsegments) {
							if (rlights[j + 2] == true) {
								System.out
								.printf("there is no car going left, so enable the right one %d \n",
										j);
								rlights[j + 1] = true;
							}
						} else {
							System.out
							.printf("there is no car going left, so enable the right one %d \n",
									j);
							rlights[j + 1] = true;
						}
					}
				} else
					llights[j] = true;
			}
		}
		llights[nsegments - 1] = false;
	}
	
	private void flushright(boolean[] llights, boolean[] rlights,
							Parking[] left, Parking[] right, MovingCar[] car) {
		System.out.println("the right flush\n");
		for (int i = 0; i != nsegments; ++i) {
			llights[i] = false;
			rlights[i] = true;
		}
		int[] trafficFlownow = new int[nsegments];
		
		trafficFlownow = trafficFlow(car);
		if (right[0].size() == 0
			&& trafficFlownow[0] <= 0) {
			llights[0] = true;
			//System.out.println("set the last light to green\n");
		}
		if (nsegments > 1) {
			for (int j = 1; j != nsegments; j++) {
				if (right[j].size() == 0) {
					
					if (trafficFlownow[j -1] <= 0) {
						if (nsegments>1) {
							if (llights[j -1 ] == true) {
								System.out.printf("there is no car going right, so enable the left one %d \n",j-1);
								llights[j] = true;
							}
						} else {
							System.out
							.printf("there is no car going left, so enable the right one %d \n",j);
							llights[j] = true;
						}
					}
				} else
					rlights[j] = true;
			}
		}
		llights[nsegments-1] = false;
		rlights[0] = false;
	}
	
	private int[] trafficFlow(MovingCar[] cars) {
		int[] segmentFlow = new int[nsegments];
		
		for (int i = 0; i < segmentFlow.length; i++) {
			segmentFlow[i] = 0;
		}
		
		for (MovingCar car : cars) {
			if (segmentFlow[car.segment] == 0) {
				segmentFlow[car.segment]++;
				segmentFlow[car.segment] *= car.dir;
			} else {
				if (car.dir > 0) {
					segmentFlow[car.segment]++;
				} else {
					segmentFlow[car.segment]--;
				}
			}
		}
		
		// System.out.println(Arrays.toString(segmentFlow));
		// System.out.println("************");
		return segmentFlow;
	}
	
	private int[] lesstraffic(int[] trafficflow, Parking[] left, Parking[] right) {
		int[] lesstraffic = new int[nsegments];
		int lefttraffic;
		int righttraffic;
		for (int i = 1; i != nsegments; ++i) {
			lefttraffic = 0;
			righttraffic = 0;
			if (trafficflow[i] < 0 && trafficflow[i - 1] > 0) {
				// opposite direction, try to pass the parking lot
				// The total number of cars on both segment and parking lot
				// trying to go left/right
				lefttraffic = -trafficflow[i] + left[i].size();
				righttraffic = trafficflow[i - 1] + right[i].size();
			}
			if (lefttraffic < righttraffic) {
				lesstraffic[i] = -lefttraffic;
			} else
				lesstraffic[i] = righttraffic;
		}
		return lesstraffic;
		
	}
	
	public int sumLeft(MovingCar[] cars, Parking[] left) {
		int[] carOnRoad = trafficFlow(cars);
		int totalLeft = 0;
		for (int value : carOnRoad) {
			if (value < 0) {
				totalLeft += value;
			}
		}
		for (Parking carsInLeft : left) {
			if (!(carsInLeft == null)) {
				totalLeft -= carsInLeft.size();
			}
		}
		return (totalLeft + left[left.length - 1].size()) * -1;
	}
	
	public boolean sameWay(MovingCar[] cars) {
		int[] carOnRoad = trafficFlow(cars);
		boolean right = false;
		boolean left = false;
		for (int value : carOnRoad) {
			if (value > 0)
				right = true;
			if (value < 0)
				left = true;
			
		}
		if (left && right)
			return false;
		else
			return true;
	}
	
	public int sumRight(MovingCar[] cars, Parking[] right) {
		int[] carOnRoad = trafficFlow(cars);
		int totalRight = 0;
		for (int value : carOnRoad) {
			if (value > 0) {
				totalRight += value;
			}
		}
		for (Parking carsInRight : right) {
			if (!(carsInRight == null)) {
				totalRight += carsInRight.size();
			}
		}
		return totalRight - right[0].size();
	}
	
	public int cleartime(int segment, MovingCar[] cars) {
		int farthestBlock = 0;
		int segmentLength = nblocks[segment];
		for (MovingCar car : cars) {
			if (car.segment == segment) {
				int position = car.block;
				if(car.dir > 0){
					position = segmentLength - position;
				}
				if (position > farthestBlock) {
					farthestBlock = car.block;
				}
			}
		}
		return farthestBlock;
	}

	public void allindanger(int i, int j, MovingCar[] movingCars, boolean[] llights, boolean[] rlights) {
		System.out.println("\n all in danger!!!!!\n");
		for (int k = i; k <= j; k++) {
			//if (indanger[i]) {
				System.out.printf("%d is in danger\n", k);
		}
		int lefttime = cleartime(i - 1, movingCars);
		System.out.printf("%d the left time is\n ", lefttime);
		int righttime = cleartime(j, movingCars);
		System.out.printf("%d the right time is \n", righttime);
		if (lefttime > righttime) {
			for (int k = i; k<=j; k++) {
						llights[k] = false;
						if (!rlights[i]) {
							rlights[i-1] = false; 
							System.out.printf("shut off the left light %d\n", i);
						} 
			}
		}
		else{
			for (int k = i; k<=j; k++) {
					rlights[k - 1] = false;
					if (!llights[k-1])
						llights[k] = false;
					System.out.printf("shut off the right light %d\n", i);
			} 
		}
	}
		
			//each index refers to the parking lot index
			//each index contains LinkedList of lots that are adjacent and in danger to it
			private LinkedList<Integer> chainOfDanger(boolean[] indanger, int index){
				LinkedList<Integer> adjacentDangers = new LinkedList<Integer>();
				if(indanger[index]){
					adjacentDangers.add(index);
				}
				boolean notInDanger = false;
				for(int i=1; index + i< nsegments  && !notInDanger; i++){
				notInDanger = true;
				if(index+i<nsegments){
					if(indanger[index+i]){
						adjacentDangers.add(index+i);
					}
					notInDanger = false;
				}
			  }
				return adjacentDangers;
			}
	
	
	private int nsegments;
	private int[] nblocks;
	private int[] capacity;
}
