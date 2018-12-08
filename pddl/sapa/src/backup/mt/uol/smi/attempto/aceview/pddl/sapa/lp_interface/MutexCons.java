/*********************************************************
         Author: Minh B. Do - Arizona State University.
*********************************************************/
package mt.uol.smi.attempto.aceview.pddl.sapa.lp_interface;

/**
 * Mutex constraint: Y^p_{AB} + Y^p_{BA} = 1. p: Proposition *or* resource ID
 */

public class MutexCons {
	public int vID1;
	public int vID2;

	public MutexCons(int v1, int v2) {
		vID1 = v1;
		vID2 = v2;
	}
}
