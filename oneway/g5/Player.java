package oneway.g5;

import oneway.sim.MovingCar;
import oneway.sim.Parking;

import java.util.*;
import static java.lang.Math.abs;

public class Player extends oneway.sim.Player {

	public enum States {
		NORMAL, FLUSH
	}
	
	private static int tick = 0;
	private States state;
	private boolean flush = true;

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
		 * for (MovingCar car : movingCars) { if (!(car.equals(null))) {
		 * System.out.println(car.block); System.out.println(car.segment); } }
		 */
		// variables
		
		if(tick<3){
			this.state = States.NORMAL;
		}

		if(sumLeft(movingCars, left) + sumRight(movingCars, right) + left[nsegments].size() + right[0].size() != 0)
			tick++;

		if(flush)
			this.state = States.FLUSH;

		boolean goLeft = true;
		int totLeft = 0;
		int totRight = 0;
		
		switch (this.state) {

		// set all lights to green -> turn off lights if in danger -> flush if
		// deadlock
		case NORMAL: {
			// set all lights to green
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

				if (trafficFlownow[i - 1] > 0)
					goingright = trafficFlownow[i - 1];

				if (trafficFlownow[i] < 0)
					goingleft = -trafficFlownow[i];

				if (left[i].size() + right[i].size() + goingright + goingleft >= (capacity[i] - 2))
				{
					indanger[i] = true;
				}
			}

			for (int i = 0; i != nsegments; ++i) {
				// if right bound has car
				// and the next parking lot is not in danger
				// if (right[i].size() > 0 && !hasTraffic(movingCars, i, -1)) {
				if (hasTraffic(movingCars, i, -1)) {
					rlights[i] = false;
					System.out.printf("a rlights[%d] = off\n", i);
				}

				// if (left[i + 1].size() > 0 && !hasTraffic(movingCars, i, 1))
				if (hasTraffic(movingCars, i, 1)) {
					llights[i] = false;
				}

				// if both left and right is on
				// find which dir is in more danger
				if (rlights[i] && llights[i] && right[i].size() > 0
						&& left[i + 1].size() > 0) {
					rlights[i] = false;
					System.out.printf("b rlights[%d] = off\n", i);
				}
			}

			int counter = 0;
			while (counter != nsegments) {
				LinkedList<Integer> m = new LinkedList<Integer>();
				m = chainOfDanger(indanger, counter);
				//System.out.printf("what is the size of  m %d\n", m.size());
				//if (m.size()>0)
					//System.out.printf("the first one inside m is %d", m.get(0));
				if (m.size() == 1) {
					//System.out.printf("\nthere is only one node in danger\n",
							//m.get(0));
					int lefttime = cleartime(m.get(0) - 1, movingCars);
					int righttime = cleartime(m.get(0), movingCars);
					int index = m.get(0);
					if (lefttime > righttime) {
							llights[index] = false;
							//System.out.printf("c llights[%d] = off\n", index);
							if (!rlights[index]) {
								rlights[index - 1] = false;
								System.out.printf("c rlights[%d] = off\n", index - 1);
							
						}
					} else {

						rlights[index - 1] = false;
						System.out.printf("d rlights[%d] = off\n", index - 1);
						if (!llights[index-1]) {
							rlights[index] = false;
							System.out.printf("d rlights[%d] = off\n",index);
							//System.out.printf("d llights[%d] = off\n", index);
					}
					//	llights[index] = false;
						//System.out.printf("d llights[%d] = off\n", index);
					}
				
				}

				if (m.size() > 1) {
					allindanger(m.get(0), m.get(m.size() - 1), movingCars,
							llights, rlights, left, right);
					counter = counter + m.size();
					//System.out.printf("counter is %d", counter);
				}
				counter++;
			} // end of while loop

			// last resort safetyCheck
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
			//if (nonetraffic){
				System.out.println("we are in none traffic status");
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
						this.state = States.FLUSH;
						rlights[i] = true;
						for (int j = i; j != nsegments; j++) {
							rlights[j] = true;
						}
						return;
					}
				}
				if (this.state==state.NORMAL){
					System.out.println("\n********in normal******\n");
					this.state = States.FLUSH;
					if (right[0].size()==0) {
					llights[0] = true;
					for (int j = 0; j != nsegments; j++) {
						llights[j] = true;
					}
					return;
					}
					else{
						rlights[0] = true;
						for (int j = 0; j != nsegments; j++) {
							rlights[j] = true;
						}
						return;
					}
				}
				
			}
			// last resort safetyCheck
			safetyCheck(movingCars, left, right, llights, rlights);
			break;
		}

		case FLUSH: {
			System.out.println("******flush state*******");

			for (int i = 0; i != nsegments; ++i) {
				llights[i] = false;
				rlights[i] = false;
			}

			if(this.flush){
				totLeft = sumLeft(movingCars, left) + left[nsegments].size();
				totRight = sumRight(movingCars, right) + right[0].size();
			}else{
				totLeft = sumLeft(movingCars, left);
				totRight = sumRight(movingCars, right);
			}

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

		// used to compare which side to favor traffic
		int totLeft = sumLeft(movingCars, left);
		int totRight = sumRight(movingCars, right);

		// used to check for car on segments
		int[] trafficFlowNow = new int[nsegments];
		trafficFlowNow = trafficFlow(movingCars);

		// checks for crash prevention between 3 segments
		for (int i = 0; i < nsegments - 2; i++) {
			if (llights[i + 1] && rlights[i + 1]) {
				for (MovingCar car1 : movingCars) {
					if (car1.segment == i && car1.dir > 0) {
						for (MovingCar car2 : movingCars) {
							if (car2.segment == i + 2 && car2.dir < 0) {
								if ((nblocks[i] - car1.block + 1) == car2.block) {
									if (totLeft > totRight) {
										rlights[i + 1] = false;
										System.out.printf(
												"f rlights[%d] = off\n", i + 1);
									} else {
										llights[i + 1] = false;
										System.out.printf(
												"f llights[%d] - off\n", i + 1);
									}
								}
							}
						}
					}
				}
			}
		}
		// checks for crash prevention between 2 segments and a parking lot
		// case: ]--- --- 01>[i]--- --- ---[<1]
		for (int i = 0; i < nsegments - 2; i++) {
			if (llights[i] && left[i + 1].size() > 0) {
				for (MovingCar car : movingCars) {
					if (car.segment == i && car.dir > 0
							&& (nblocks[i] - car.block - 1) == 1) {
						if (capacity[i] == (right[i].size() + left[i - 1]
								.size()))
							llights[i] = false;
						else if (totLeft > totRight) {
							System.out
									.println("Stanley is shutting off rlights["
											+ i + "]!");
							rlights[i] = false;
						} else {
							llights[i] = false;
							System.out
									.println("Stanley is shutting off llights["
											+ i + "]!");
						}
					}
				}
			}
		}
		// case: [1>]--- --- ---[ ]<01--- ---
		for (int i = 1; i < nsegments - 1; i++) {
			if (rlights[i] && right[i].size() > 0) {
				for (MovingCar car : movingCars) {
					if (car.segment == i + 1 && car.dir < 0 && car.block == 0) {
						if (capacity[i] == (right[i].size() + left[i - 1]
								.size())){
							rlights[i] = false; System.out.printf("g rlights[%d] = false\n", i);
						}
						else if (totLeft > totRight) {
							rlights[i] = false;
							System.out
									.println("Stanley is shutting off rlights["
											+ i + "]!");
						} else {
							llights[i] = false;
							System.out
									.println("Stanley is shutting off llights["
											+ i + "]!");
						}
					}
				}
			}
		}

		// checks for edge cases of the 3 segment crash prevention
		for (MovingCar car : movingCars) {
			if (car.segment == (nsegments - 2) && car.dir > 0
					&& (nblocks[nsegments - 2] - car.block) == 1
					&& rlights[nsegments - 1]) {
					
					llights[nsegments - 1] = false;
			}
			if (car.segment == 1 && car.dir < 0 && car.block == 0 && llights[0]) {
					rlights[0] = false;
			}
		}

		// finally do check for immediate crashes

		// checks for edge cases of incoming traffic
		for (MovingCar car : movingCars) {
			if (car.segment == 0 && car.dir < 0)
				rlights[0] = false;
			if (car.segment == nsegments - 1 && car.dir > 0)
				llights[nsegments - 1] = false;
		}

		// makes sure we do not turn on lights when there is incoming traffic
		for (int i = 0; i < nsegments; i++) {
			if (trafficFlowNow[i] < 0) {
				rlights[i] = false;
			}
			if (trafficFlowNow[i] > 0) {
				llights[i] = false;
			}
		}

/*		// safety check for 0 capacity parking lots
		for(int i =1; i<nsegments; i++){
			if(capacity[i] == 0){
				llights[i-1] = true;
				rlights[i] = true;
			}
		}
*/
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

		if (left[nsegments-1].size() == 0
				&& trafficFlownow[nsegments - 1] >= 0) {
			rlights[nsegments - 1] = true;
		}

		// in flush case, no traffic and edge lights are off
		// so we turn on right most rlights and check to see when we
		// can open more rlights traversing from the right
		if (nsegments - 1 > 0) {
			for (int j = nsegments - 2; j > 0; j--) {
				if (left[j + 1].size() == 0 && rlights[j + 1]
						&& trafficFlownow[j] >= 0) {
					rlights[j] = true;
				}
			}
		}

		// if no car is queued to go right
		// might as well open up right parking lot
		if (sumRight(car, right) + right[0].size() == 0)
			llights[nsegments - 1] = true;
		else
			llights[nsegments - 1] = false;
		// make sure no sneaky cars pop up from left
		rlights[0] = false;

		//if flush test then enable llights[nsegments-1]
		if(this.flush)
			llights[nsegments-1] = true;
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

		if (right[0].size() == 0 && trafficFlownow[0] <= 0) {
			llights[0] = true;
			// System.out.println("set the last light to green\n");
		}

		// we turn on left most llights and check to see when we
		// can open more llights traversing from the left
		if (nsegments > 0) {
			for (int j = 1; j < nsegments; j++) {
				if (right[j].size() == 0 && llights[j - 1]
						&& trafficFlownow[j] <= 0) {
					llights[j] = true;
				}
			}
		}

		// finally check if anycar is queued to go left,
		// if not, then might as well open up left parking lot

		if (sumLeft(car, left) + left[nsegments].size() == 0)
			rlights[0] = true;
		else
			rlights[0] = false;

		// makes sure no sneaky cars will pop up
		llights[nsegments - 1] = false;
		
		//if flush test
		if(this.flush)
			rlights[0] = true;

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
		System.out.println((totalLeft + left[left.length - 1].size()) * -1);
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
		System.out.println("THIS IS SEGMENT " + segment);
		int farthestBlock = 0;
		int segmentLength = nblocks[segment];
		for (MovingCar car : cars) {
			if (car.segment == segment) {
				int position = car.block;
				if (car.dir > 0) {
					position = segmentLength - position;
				}
				if (position > farthestBlock) {
					farthestBlock = car.block;
				}
			}
		}
		return farthestBlock;
	}

	public void allindanger(int i, int j, MovingCar[] movingCars,
			boolean[] llights, boolean[] rlights, Parking[] left,
			Parking[] right) {
		System.out.println("\n all in danger!!!!!\n");
		rlights[i - 1] = false; System.out.printf("i rlights[%d] = false\n", i-1);
		llights[j] = false;
		int[] trafficFlownow = new int[nsegments];
		trafficFlownow = trafficFlow(movingCars);

		for (int k = i; k <= j; k++) {
			System.out.printf("%d is in danger\n", k);
			if (left[k].size() + right[k].size() != capacity[k]
					|| (left[k].size() + right[k].size() == capacity[k])
					&& ((trafficFlownow[k - 1] <= 0))
					&& (trafficFlownow[k] >= 0)) {

				llights[k - 1] = false;
				rlights[k] = false; System.out.printf("j rlights[%d] = false\n", k);
			} else {
				System.out.printf("\nwe should not shut off %d\n", k);
				rlights[k] = true;
			}
		}

		if (sameWay(movingCars)) {
			System.out.printf("\n same way\n");
			int hascar = 0;
			for (int k = 0; k != nsegments; k++) {
				if (trafficFlownow[k] > 0) {
					hascar += 1;
					System.out.println("right");
				}
				if (trafficFlownow[k] < 0) {
					hascar = -1;
					System.out.println("left");
				}
			}

			if (hascar > 0) {

				for (int k = i; k <= j + 1; k++) {
					llights[j] = false;
					System.out.printf("g llights[%d] = false\n", j);
					rlights[k - 1] = true;
					System.out.printf("\nsame traffic turn on rlights[%d]\n",
							k - 1);
				}
			} else {
				for (int k = i; k <= j; k++) {
					rlights[i - 1] = false;
					System.out.printf("k rlights[%d] = false\n", i - 1);
					if (!llights[j - 1]) {
						llights[j] = false;
						System.out.printf("h llights[%d] = false\n", j);
					}
					System.out.printf("shut off the rlights[%d]\n", i - 1);
				}
			}
		}
	}

	// each index refers to the parking lot index
	// each index contains LinkedList of lots that are adjacent and in danger to
	// it
	private LinkedList<Integer> chainOfDanger(boolean[] indanger, int index) {
		LinkedList<Integer> adjacentDangers = new LinkedList<Integer>();
		System.out.println("Hey I've got the index!!!!!" + index + "and is this in danger" + indanger[index]);
		boolean notInDanger = false;

		if (indanger[index]) {
			adjacentDangers.add(index);
		}

		for (int i = 1; index + i < nsegments && !notInDanger; i++) {
			notInDanger = true;
			System.out.println("is the next one in danger: " + indanger[index + i]);
			if (index + i < nsegments) {
				if (indanger[index + i]) {
					adjacentDangers.add(index + i);
					notInDanger = false;
				}
			}
		}
		return adjacentDangers;
	}

	private int nsegments;
	private int[] nblocks;
	private int[] capacity;
}
