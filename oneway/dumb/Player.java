package oneway.g5;

import oneway.sim.MovingCar;
import oneway.sim.Parking;

import java.util.*;
import static java.lang.Math.abs;

public class Player extends oneway.sim.Player {
	// if the parking lot is almost full
	// it asks the opposite direction to yield
	private static double AlmostFull = 0.6;

	public Player() {
	}

	public void init(int nsegments, int[] nblocks, int[] capacity) {
		this.nsegments = nsegments;
		this.nblocks = nblocks;
		this.capacity = capacity.clone();
	}

	public void setLights(MovingCar[] movingCars, Parking[] left,
			Parking[] right, boolean[] llights, boolean[] rlights) {
		// Strategy:
		// 1. initially turn all traffic lights off
		// 2. check each parking lot
		// if it has pending cars, try to turn the light green
		// a) if there is no opposite traffic, go ahead and turn right
		// b) if there is opposite traffic, but the parking lot is piled up
		// turn red the opposite traffic light.
		// resume turning the traffic light after the traffic is clear
		// This strategy avoids car crash, but it cannot guarantee all cars
		// will be delivered in time and the parking lot is never full

		for (int i = 0; i != nsegments; ++i) {
			llights[i] = false;
			rlights[i] = false;
		}

		boolean[] indanger = new boolean[nsegments + 1];

		int[] trafficFlownow = new int[nsegments];

		trafficFlownow = trafficFlow(movingCars);

		// find out almost full parking lot
		for (int i = 1; i != nsegments; ++i) {
			System.out.printf("the %d block's length is %d\n", i, nblocks[i]);
			System.out.printf("the %d block's traffice is %d\n", i,
					trafficFlownow[i]);
			System.out.printf("the %d parking lot's capacity is %d\n", i,
					capacity[i]);
			System.out.printf("the %d parking left is %d\n", i, left[i].size());
			System.out.printf("the %d parking right is %d\n", i,
					right[i].size());
			if (left[i].size() + right[i].size() + abs(trafficFlownow[i - 1])
					+ abs(trafficFlownow[i]) > (capacity[i]) * AlmostFull) {
				System.out.printf("we are in danger!!\n");
				indanger[i] = true;
			}
		}

		for (int i = 0; i != nsegments; ++i) {
			// if right bound has car
			// and the next parking lot is not in danger
			if (right[i].size() > 0 && !hasTraffic(movingCars, i, -1)) {
				rlights[i] = true;
			}

			if (left[i + 1].size() > 0 && !hasTraffic(movingCars, i, 1)) {
				llights[i] = true;
			}

			// if both left and right is on
			// find which dir is in more danger
			if (rlights[i] && llights[i]) {
				/*
				 * double lratio = 1.0 * (left[i+1].size() + right[i+1].size())
				 * / capacity[i+1]; double rratio = 1.0 * (left[i].size() +
				 * right[i].size()) / capacity[i]; if (lratio > rratio)
				 * rlights[i] = false; else llights[i] = false;
				 */
				int[] lesstrafficnow = new int[nsegments];
				lesstrafficnow = lesstraffic(trafficFlownow, left, right);
			}
		}
		for (int i = 1; i != nsegments; i++) {
			if (indanger[i]) {
				System.out.printf("%d is in danger", i);
				rlights[i - 1] = false;
				llights[i] = false;
			}
		}
		for (int i = 0; i != nsegments; i++) {
			if (rlights[i])
				llights[i] = false;

		}
		boolean alloff = true;
		boolean nonetraffic = true;
		for (int i = 0; i != nsegments; i++) {
			if (rlights[i] || llights[i])
				alloff = false;
			if (trafficFlownow[i] != 0)
				nonetraffic = false;
		}
		if (alloff && nonetraffic) {
			for (int i = 0; i != nsegments; i++) {
				if (left[i + 1].size() > 0) {
					llights[i] = true;
					return;
				}
			}
			for (int i = nsegments; i != 1; i--)
			if (right[i-1].size() > 0) {
				rlights[i] = true;
				return;
			}
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

		System.out.println(Arrays.toString(segmentFlow));
		System.out.println("************");
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

	private int nsegments;
	private int[] nblocks;
	private int[] capacity;
}
