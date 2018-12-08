/*********************************************************
         Author: Minh B. Do - Arizona State University.
*********************************************************/
package edu.asu.sapa.lp_interface;

/**
 * Constraint between an ordering variables and two starting time points. This
 * class is actually used to represent three different types of constraints that
 * have the same form: (1) Causal Link & starting times: M.(1-X^p_{AB}) +
 * (st^p_B - et^p_A) > 0 (2) Mutex & starting times: M.(1-X^p_{AB}) + (st^p_B -
 * et^p_A) > 0 (3) Resource ordering: M.(1-X^r_{AB}) + (st^r_B - et^r_A) > 0
 */
public class OrderTemporalConst {
	public int type; // 1: Causal link; 2: Mutex; 3: Resource
	public int xID; // From the ID of X^r_{AB}, we can figure out r, A, B.

	public OrderTemporalConst(int t, int ipvarID) {
		type = t;
		xID = ipvarID;
	}
}
