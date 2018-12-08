/********************************************************************
   Author: Minh B. Do (Arizona State University - binhminh@asu.edu)
*********************************************************************/
package mt.uol.smi.attempto.aceview.pddl.sapa.parsing;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import mt.uol.smi.attempto.aceview.pddl.sapa.basic_ds.Function;
import mt.uol.smi.attempto.aceview.pddl.sapa.basic_ds.Predicate;

/**
 * This class represent the problem structure that we parsed from the problem
 * file in PDDL2.1
 */
public class Problem {
	String name; // Name of the problem
	String domain; // Name of the domain used for this problem

	// List of all object *types*
	ArrayList object_type;

	// Collection of objects of each type (Map: Type->ArrayList of objects)
	Hashtable object_map;

	// ArrayList of Predicate represent the initial state
	ArrayList init_state;

	// ArrayList of Function values represent the initial state
	ArrayList init_func;

	// ArrayList of Exogenous Events & the event times (added Aug 26, 2003)
	// Note that different events can be related to the same predicate but
	// different time points
	final static int NUM_EXO_EVENT_LIMIT = 100;
	ArrayList exo_events;
	float[] exo_event_time = new float[NUM_EXO_EVENT_LIMIT];
	boolean[] exo_event_sign = new boolean[NUM_EXO_EVENT_LIMIT];

	// ArrayList of Predicate represent the goal state
	ArrayList goal_state;
	Hashtable goal_time_map;

	// Flag to state if there are deadline goal constraints for this problem
	boolean dlConst = false;

	public Problem() {
		name = new String();
		object_type = new ArrayList();
		object_map = new Hashtable();
		init_state = new ArrayList();
		init_func = new ArrayList();
		exo_events = new ArrayList();
		goal_state = new ArrayList();
		goal_time_map = new Hashtable();
	}

	/** Set the name of this problem */
	public void setName(String n) {
		name = n;
	}

	public String getName() {
		return name;
	}

	/** Domain name used for this problem */
	public void setDomain(String n) {
		domain = n;
	}

	public String getDomain() {
		return domain;
	}

	/**
	 * Add an object type (e.g aircraf, package, satellite) and the set of objects
	 * of that type. The objects can be declared in the problem file OR as
	 * "constants" in the domain file.
	 */
	public void addObjectType(String type, ArrayList objSet) {
		ArrayList objArray;

		if (!object_type.contains(type)) {
			objArray = new ArrayList(objSet);
			object_type.add(type);
			object_map.put(type, objArray);
		} else {
			objArray = (ArrayList) object_map.remove(type);
			for (int i = 0; i < objSet.size(); i++)
				objArray.add(objSet.get(i));
			object_map.put(type, objArray);
		}
	}

	/**
	 * Merge the constants declared in the domain file into the list of objects
	 * declared in the problem file
	 */
	public void absorbConstants(Hashtable constantMap) {
		String type;
		for (Enumeration e = constantMap.keys(); e.hasMoreElements();) {
			type = (String) e.nextElement();
			addObjectType(type, (ArrayList) constantMap.get(type));
		}
	}

	public int numObjectType() {
		return object_type.size();
	}

	/** Get a indexed object type */
	public String getObjectType(int index) {
		return (String) object_type.get(index);
	}

	/** Get the set of objects belong to a given type */
	public ArrayList getObjectMap(String type) {
		return (ArrayList) object_map.get(type);
	}

	/**
	 * Add a predicate to the list of predicates that are true in the initial state
	 */
	public void addInitPred(Predicate pred) {
		if (!init_state.contains(pred))
			init_state.add(pred);
	}

	public int numInitPred() {
		return init_state.size();
	}

	public Predicate getInitPred(int index) {
		return (Predicate) init_state.get(index);
	}

	/** Add a function specified in the problem file */
	public void addInitFunct(Function func) {
		if (!init_func.contains(func))
			init_func.add(func);
	}

	public int numInitFunct() {
		return init_func.size();
	}

	public Function getInitFunct(int index) {
		return (Function) init_func.get(index);
	}

	/** Add a exogenous event specified in the problem file */
	public void addExoEvent(Predicate eEvent, float time, boolean b) {
		exo_events.add(eEvent);
		exo_event_time[exo_events.size() - 1] = time;
		exo_event_sign[exo_events.size() - 1] = b;
	}

	public int numExoEvent() {
		return exo_events.size();
	}

	public Predicate getExoEvent(int index) {
		return (Predicate) exo_events.get(index);
	}

	public float getExoEventTime(int index) {
		return exo_event_time[index];
	}

	public boolean getExoEventSign(int index) {
		return exo_event_sign[index];
	}

	/**
	 * Add a predicate to the list of predicates need to be true in the goal state
	 */
	public void addGoalPred(Object o) {
		if (!goal_state.contains(o))
			goal_state.add(o);
	}

	public int numGoalPred() {
		return goal_state.size();
	}

	public Predicate getGoalPred(int index) {
		return (Predicate) goal_state.get(index);
	}

	/**
	 * For problem with goal deadline, specify the goal time for each goal predicate
	 */
	public void putGoalTime(Predicate goal, Float time) {
		goal_time_map.put(goal, time);
	}

	public Float getGoalTime(Predicate goal) {
		return (Float) goal_time_map.get(goal);
	}

	/** Print the problem description */
	public String toString() {
		String s = new String();
		String obj;
		Function f;
		ArrayList v;
		int i, j;

		s += "(" + name + " " + domain + "\n";
		s += "(:objects ";
		for (i = 0; i < numObjectType(); i++) {
			obj = getObjectType(i);
			s += obj + "-(";
			v = (ArrayList) getObjectMap(obj);
			for (j = 0; j < v.size(); j++)
				s += (String) v.get(j) + " ";
			s += ")";
		}
		s += ")\n";

		s += "(:init ";
		for (i = 0; i < numInitPred(); i++)
			s += getInitPred(i) + " ";
		s += ")\n";

		s += "(:init_function ";
		for (i = 0; i < numInitFunct(); i++) {
			f = (Function) getInitFunct(i);
			s += f + "-";
			s += f.getValue();
			s += " ";
		}
		s += ")\n";

		s += "(:goals ";
		for (i = 0; i < numGoalPred(); i++)
			s += getGoalPred(i);
		s += ")\n";

		return s;
	}

	/**
	 * Function to revive the type-map Hashtable based on the type structure of the
	 * domain
	 */
	public void makeTypeMap(Domain domain) {
		ArrayList objArrayList, superArrayList;
		String type, superType, tempType;
		int i, j;

		for (i = 0; i < domain.numType(); i++) {
			type = domain.getType(i);

			if (!object_type.contains(type)) {
				object_type.add(type);
				object_map.put(type, new ArrayList());
			}
		}

		for (i = 0; i < object_type.size(); i++) {
			type = (String) object_type.get(i);

			while (true) {
				if (domain.getParent(type) == null)
					break;

				superType = (String) domain.getParent(type);

				objArrayList = (ArrayList) object_map.get(type);
				superArrayList = (ArrayList) object_map.get(superType);

				for (j = 0; j < objArrayList.size(); j++)
					if (!superArrayList.contains(objArrayList.get(j)))
						superArrayList.add(objArrayList.get(j));

				object_map.put(superType, superArrayList);
				type = superType;
			}
		}
	}

	/**
	 * Manage deadline constraints
	 */
	public void setDeadlineFlag() {
		dlConst = true;
	}

	public boolean goalDeadlineFlag() {
		return dlConst;
	}
}
