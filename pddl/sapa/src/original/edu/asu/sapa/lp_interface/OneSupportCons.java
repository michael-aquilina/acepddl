/*********************************************************
         Author: Minh B. Do - Arizona State University.
*********************************************************/
package edu.asu.sapa.lp_interface;

/**
 * Only one support for each action's (pre)condition: \Sigma X^p_{AB} = 1
 */
public class OneSupportCons {
	int numVar; // Number of X^p_{AB} variables
	int[] varID;

	public OneSupportCons(int nV) {
		numVar = nV;
		varID = new int[nV];
	}

	public void addVar(int index, int id) {
		varID[index] = id;
	}

	public int size() {
		return numVar;
	}

	public int getVar(int index) {
		return varID[index];
	}
}
