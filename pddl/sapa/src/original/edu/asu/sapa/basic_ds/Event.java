/***********************************************************
     Author: Minh B. Do - Arizona State University
***********************************************************/
package edu.asu.sapa.basic_ds;

/**
 * Event: Store the class that represent an event. Event occurs at some time
 * instance and changes the value of some predicate (T/F) in the state.
 */
public class Event {
	Integer predID; // ID of this Ground Predicate
	boolean neg; // Negative. T: Delete; F: Add
	public float time;

	public Event() {
		predID = new Integer(-1);
		time = 0;
		neg = false;
	}

	public Event(Integer p) {
		predID = p;
		neg = false;
		time = 0;
	}

	public Event(Integer p, boolean n) {
		predID = p;
		neg = n;
		time = 0;
	}

	public Event(Integer p, boolean n, float t) {
		predID = p;
		neg = n;
		time = t;
	}

	public Event(Event e) {
		predID = e.predID;
		neg = e.neg;
		time = e.time;
	}

	public void setPred(Integer p) {
		predID = p;
	}

	public Integer getPred() {
		return predID;
	}

	/** Whether this event is "negative" (T->F) or "positive" (F->T) */
	public void setNeg(boolean b) {
		neg = b;
	}

	public boolean getNeg() {
		return neg;
	}

	/* Set the (continuous) time instance at which this event occurs */
	public void setTime(float f) {
		time = f;
	}

	public float getTime() {
		return time;
	}
}