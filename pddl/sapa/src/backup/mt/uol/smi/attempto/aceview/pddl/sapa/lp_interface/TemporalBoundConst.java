/*********************************************************
         Author: Minh B. Do - Arizona State University.
*********************************************************/
package mt.uol.smi.attempto.aceview.pddl.sapa.lp_interface;

/**
 * Pure temporal constraints such as goal deadlines or bound on starting time of
 * actions.
 */
public class TemporalBoundConst {
	public int type; // 1: UpperBound; 2: LowerBound
	public int actID; // Action that has its starting time bounded

	public TemporalBoundConst(int t, int aID) {
		type = t;
		actID = aID;
	}
}
