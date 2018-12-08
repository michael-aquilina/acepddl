/*************************************************************
   Author: Minh B. Do - Arizona State University
**************************************************************/
package edu.asu.sapa.complex_ds;

import edu.asu.sapa.basic_ds.*;

import java.util.*;

/**
 * GPredDB: Class to manage the set of predicates that are true at a given time
 * point.
 */

public class GPredDB {
	ArrayList predList; // List of (grounded) Predicate's IDs
	Hashtable timeMap; // Map each pred with the time instance that
	// it will be true from that point (<= current-time). Will not
	// be important for *current-state*, but important for *goal-state*
	// and *future-state* may also be important for *init-state* also.

	float eTime; // Ealiest time among preds
	float lTime; // Latest time among preds.
	// Note that the lTime may be *smaller* than the time of the state
	// that contains this GPredDB

	/** Constructor */
	public GPredDB() {
		predList = new ArrayList();
		timeMap = new Hashtable();

		eTime = 1000000;
		lTime = 0;
	}

	public GPredDB(GPredDB p) {
		predList = new ArrayList(p.predList);
		timeMap = new Hashtable(p.timeMap);

		eTime = p.eTime;
		lTime = p.lTime;
	}

	/**
	 * Construct a predDB from a ArrayList of predicates and the time point that all
	 * of them are true
	 */
	public GPredDB(ArrayList seed, float time) {
		predList = new ArrayList(seed);
		timeMap = new Hashtable();

		for (int i = 0; i < predList.size(); i++) {
			timeMap.put(predList.get(i), new Float(time));
		}

		eTime = time;
		lTime = time;
	}

	/** Get the earliest time point at which ONE of the predicate is true */
	public float getETime() {
		return eTime;
	}

	/** Get the erliest time point where ALL of the predicates are true */
	public float getLTime() {
		return lTime;
	}

	/** Add a predicate to the set of predicates that are true */
	public void addPred(Integer pred) {
		predList.add(pred);
	}

	public int numPred() {
		return predList.size();
	}

	public Integer getPred(int index) {
		return (Integer) predList.get(index);
	}

	/** BM: Added July 2003 for "pending precond" extension **/
	public boolean containPred(Object predID) {
		return predList.contains(predID);
	}

	/** Get all the predicates, mostly used in duplicate this GPredDB */
	public ArrayList getAllPred() {
		return predList;
	}

	/** Set the time point when a predicate starts to be true */
	public void putTimeMap(Object pred, Object time) {
		timeMap.put(pred, time);
	}

	public Float getTimeMap(Object pred) {
		return (Float) timeMap.get(pred);
	}

	public Hashtable getAllTimeMap() {
		return timeMap;
	}

	/**
	 * Function to check if a action is applicable in this GPredDB. Thus, if all of
	 * the actions logical (pre)conditions are subsumed by the set of predicates in
	 * this GPredDB
	 */
	public boolean applicable(GAction a) {
		int i;

		for (i = 0; i < a.numPrecond(); i++) {
			if (a.getPreTime(i) > 1) // Don't check for "at end" condition
				continue;
			if (!predList.contains(new Integer(a.getPrecond(i))))
				return false;
		}

		return true;
	}

	/** Update this GPredDB according to an action's logical effects */
	public void update(GAction a, float t) {
		Integer p;
		int time;
		int i, j;

		for (i = 0; i < a.numDelete(); i++) {
			p = new Integer(a.getDelete(i));
			// time = a.getTimeEffect(p);
			time = a.getDeleteTimeEffect(i);

			// Instant effect at the beginning of an action
			if (time < 1) {
				if ((j = predList.indexOf(p)) > -1) {
					predList.remove(j);
				}
				timeMap.remove(p);
			}
		}

		for (i = 0; i < a.numAdd(); i++) {
			p = new Integer(a.getAdd(i));
			// time = a.getTimeEffect(p);
			time = a.getAddTimeEffect(i);

			if (time < 1) {
				if (!predList.contains(p)) {
					predList.add(p);
					timeMap.put(p, new Float(t));
				}
			}
		}
	}

	/**
	 * Removes the specified fact (by ID) from this state.
	 * 
	 * @param fact
	 *            The fact to remove from this state.
	 * @author J. Benton
	 */
	public void removePredicate(int fact) {
		Integer factInt = new Integer(fact);
		int idx;
		if ((idx = predList.indexOf(factInt)) >= 0) {
			timeMap.remove(factInt);
			predList.remove(idx);
		}
	}

	/** Update a predDB if one event occur */
	public void update(Event e) {
		if (!e.getNeg()) {
			if (!predList.contains(e.getPred())) {
				predList.add(e.getPred());
				timeMap.put(e.getPred(), new Float(e.getTime()));
			}
		} else {
			predList.remove(e.getPred());
			timeMap.remove(e.getPred());
		}
	}

	/** Update a GPredDB according to a set of events */
	public void update(ArrayList aList) {
		int i;
		Event e;
		for (i = 0; i < aList.size(); i++) {
			e = (Event) aList.get(i);
			if (!e.getNeg()) {
				if (!predList.contains(e.getPred())) {
					predList.add(e.getPred());
					timeMap.put(e.getPred(), new Float(e.getTime()));
				}
			} else {
				predList.remove(e.getPred());
				timeMap.remove(e.getPred());
			}
		}
	}
}
