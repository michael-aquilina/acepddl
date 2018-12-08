/********************************************************************
     Author: Minh B. Do - Arizona State University
*********************************************************************/

package edu.asu.sapa.utils;

/**
 * OrderRelation: Represent the relations between two actions. It can be causal
 * relation, mutex relations based on confliction in using some predicate or
 * metric resource
 */

public class OrderRelation {
	public int relation; // 1: causal link; 2: pred mutex; 3: resource mutex
	public int actIndex; // Action which is odered before by "relation" to
	// a particular action that use this OrderRelation class.

	public int saTime; // Start action: 0: start time; 1: end time
	public int eaTime; // End action: 0: start time; 1: end time

	public int relID; // This variable specifies the predicate/function ID
	// involved with this ordering relation

	public OrderRelation(int act, int sa, int ea, int rel, int id) {
		actIndex = act;
		saTime = sa;
		eaTime = ea;
		relation = rel;
		relID = id;
	}
}
