/********************************************************************
     Author: Minh B. Do - Arizona State University
*********************************************************************/
package edu.asu.sapa.lp_interface;

/**
 * This class represents the MILP variables in the encoding: X^p_{AB}, Y^p_{AB},
 * Z^r_{AB}
 */
public class LP_Var {
	public int type; // 1: Causal link; 2: Logical mutex; 3: Resource Mutex

	public int object; // PropID in CausalLink/Logical-Mutex; ResID in ResMutex
	public int act1; // Index in the plan of the first action (supporting)
	public int act2;

	public boolean a1st; // If the first end point is act1's start time (false = endtime)
	public boolean a2st;

	public float sepDur; // Necessary reparatating duration between a1st & a2st

	public LP_Var(int t, int o, int a1, int a2, boolean st1, boolean st2, float sDur) {
		type = t;
		object = o;
		act1 = a1;
		act2 = a2;
		a1st = st1;
		a2st = st2;
		sepDur = sDur;
	}
}
