/********************************************************************
    Author: Minh B. Do -- Arizona State University
*******************************************************************/
package edu.asu.sapa.complex_ds;

import edu.asu.sapa.basic_ds.*;

import java.util.*;

/**
 * GState: Store the "State" information. The state basically containing two
 * part. A list of predicates that are true and their achivement time (stored in
 * GPredDB), and a set of values for all the continous functions (stored in
 * GMResDB). The state is part of the StatePair structure. Refer to the
 * documents of StatePair, GPredDB, and GMResDB for details.
 */
public class GState {
	GPredDB predDB;
	GMResDB mresDB;

	public GState(int numFunc) {
		predDB = new GPredDB();
		mresDB = new GMResDB(numFunc);
	}

	public GState(GState s) {
		predDB = new GPredDB(s.predDB);
		mresDB = new GMResDB(s.mresDB);
	}

	public void setPredDB(GPredDB p) {
		// predDB = new GPredDB(p);
		predDB = p;
	}

	public GPredDB getPredDB() {
		// return new GPredDB(predDB);
		return predDB;
	}

	public void setMResDB(GMResDB m) {
		mresDB = new GMResDB(m);
	}

	public GMResDB getMResDB() {
		return new GMResDB(mresDB);
	}

	/** Check if an action is applicable to this state */
	public boolean applicable(GAction a) {
		if (predDB.applicable(a) && mresDB.applicable(a))
			return true;
		else
			return false;
	}

	/** Update a state (both PredDB and MResDB) upon applying an action */
	public void update(GAction a, float time) {
		predDB.update(a, time);
		mresDB.update(a, true);
	}

	/** Update a state according to the set of events */
	public void update(ArrayList eventList) {
		predDB.update(eventList);
	}

	/** BM: Added July 22, 2003. For supporting "pending preconditions" **/
	public boolean containPred(Object predID) {
		return predDB.containPred(predID);
	}
}