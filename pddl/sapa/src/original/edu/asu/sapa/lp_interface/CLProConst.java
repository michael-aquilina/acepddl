/*********************************************************
         Author: Minh B. Do - Arizona State University.
*********************************************************/
package edu.asu.sapa.lp_interface;

/**
 * Causal Link Protection: (1 - X^p_{AB}) + (Y^p_{A'A} + Y^p_{BA'}) >= 1
 */

public class CLProConst {
	public int xID;
	public int y1ID;
	public int y2ID;

	public CLProConst(int x, int y1, int y2) {
		xID = x;
		y1ID = y1;
		y2ID = y2;
	}
}
