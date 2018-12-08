/***********************************************************
   Author: Minh B. Do - Arizona State University
***********************************************************/
package mt.uol.smi.attempto.aceview.pddl.sapa.basic_ds;

import java.util.*;

/**
 * Function: Store the (abstract) Function template used to ground functions
 */
public class Function {
	String name;
	float value; // Initial value of this function (in problem file)

	ArrayList objList; // type/object in the templace
	// (e.g ?aircraft in "domain", or aircraft1 in "problem")

	public Function() {
		objList = new ArrayList();
		value = 0;
	}

	/** Name of this function template */
	public void setName(String n) {
		name = new String(n);
	}

	public String getName() {
		return name;
	}

	/** The float value of this function */
	public void setValue(float v) {
		value = v;
	}

	public float getValue() {
		return value;
	}

	/** Number of object involved with this function */
	public int funcSize() {
		return objList.size();
	}

	public String getObj(int index) {
		return (String) objList.get(index);
	}

	public void addObj(String obj) {
		objList.add(obj);
	}

	public void addObj(String obj, int numSameObj) {
		for (int i = 0; i < numSameObj; i++)
			objList.add(obj);
	}

	/** For printing */
	public String toString() {
		String s = new String();
		Object obj;

		s += "(" + name;
		for (int i = 0; i < objList.size(); i++) {
			obj = objList.get(i);
			s += " " + (String) obj;
		}
		s += ")";

		// if( !abst )
		s += " - " + (new Float(value)).toString();

		return s;
	}
}
