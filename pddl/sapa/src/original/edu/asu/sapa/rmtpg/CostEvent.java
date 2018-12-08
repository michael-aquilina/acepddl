/********************************************************
   Author: Minh B. Do (Arizona State University)
********************************************************/
package edu.asu.sapa.rmtpg;

/**
 * CostEvent: Class represents the cost update event. It's used when propagate
 * the cost in the RMTPG and specify the cost to achieve a given fact is update
 * at a specific time.
 */
public class CostEvent {
	public int factID; // fact activate by this event
	public float time; // time that this event will occur
	public float cost; // cost of this event
	public int actID; // action that cause this event

	public CostEvent(int fId, float t, float c, int aId) {
		factID = fId;
		time = t;
		cost = c;
		actID = aId;
	}
}
