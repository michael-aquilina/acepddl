/***************************************************************
   Author: Minh B. Do
****************************************************************/
package mt.uol.smi.attempto.aceview.pddl.sapa.basic_ds;

import java.util.*;

/**
 * Predicate: Store the Predicate (abstract) template. It contains the set of
 * objects involved with this Predicate that specified in the domain file.
 */
public class Predicate {
	String name; // Name of this predicate

	ArrayList objList; // List of object this predicate represents
	// can be abstract type (aircraft) in domain file, abstract obj
	// (?car1) in domain's action declaration, or real object (car1)
	// in the problem file. Also can be constaint in object file

	public Predicate() {
		objList = new ArrayList();
	}

	/**
	 * Set the name of the predicate.
	 */
	public void setName(String n) {
		name = n;
	}

	public String getName() {
		return name;
	}

	/**
	 * Size or number of objects in this predicate. The object can be object type
	 * (e.g truck) or abstract object instance (e.g ?truck1) specified in the domain
	 * file, or the actual object instance (e.g truck-la) identified in the problem
	 * file.
	 */
	public int predSize() {
		return objList.size();
	}

	public void addObj(Object obj) {
		objList.add(obj);
	}

	public void addObj(Object obj, int numSameObj) {
		for (int i = 0; i < numSameObj; i++)
			objList.add(obj);
	}

	public String getObj(int index) {
		return (String) objList.get(index);
	}

	/**
	 * Compare if two Predicates are equal
	 */
	private boolean equals(Predicate p) {
		if (!name.equals(p.getName()))
			return false;

		if (objList.size() != p.predSize())
			return false;

		for (int i = 0; i < objList.size(); i++)
			if (!p.getObj(i).equals(objList.get(i)))
				return false;

		return true;
	}

	public String toString() {
		String s = new String();
		s += " (" + name + " ";
		for (int i = 0; i < objList.size(); i++) {
			s += " ?" + (String) objList.get(i);
		}
		s += ")";

		return s;
	}
}
