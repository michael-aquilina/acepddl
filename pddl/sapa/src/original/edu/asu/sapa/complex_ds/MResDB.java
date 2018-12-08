/************************************************************
    MResDB.java: Manage all the metric resources and other
    metric quantity. Basically, handle the "function"
    instances of type declared in the *domain* file and
    instantiated after parse the *problem* file.
************************************************************/
package edu.asu.sapa.complex_ds;

import edu.asu.sapa.basic_ds.*;

import java.util.*;

public class MResDB {
	ArrayList types; // List of function types (eg distance, fuel etc)
	Hashtable typeIndexMap; // Map each type -> the first appearance
	// of function of that type in the groundFuncs structure. May delete
	// them later

	ArrayList groundFuncs; // Set of all functions, the *index* in this vector
	// is also the *funcID* of that grounded function.

	// Hashtable funcMap; // Map each function with its respected value. ID->Value

	public MResDB() {
		types = new ArrayList();
		typeIndexMap = new Hashtable();

		groundFuncs = new ArrayList();
	}

	public MResDB(MResDB m) {
		groundFuncs = new ArrayList(m.getAllFunc());
	}

	/* For types */
	public void addType(Object t) {
		types.add(t);
	}

	public void addTypeSet(ArrayList t) {
		types.addAll(t);
	}

	public int numType() {
		return types.size();
	}

	public String getType(int index) {
		return (String) types.get(index);
	}

	/* For typeIndexMap */
	public void putTypeIndex(Object key, Object value) {
		typeIndexMap.put(key, value);
	}

	public int getTypeIndex(Object key) {
		return ((Integer) typeIndexMap.get(key)).intValue();
	}

	/* For groundFuncs set of functions */
	public void addFunc(Function func) {
		groundFuncs.add(func);
	}

	public Function getFunc(int index) {
		return (Function) groundFuncs.get(index);
	}

	public int numFunc() {
		return groundFuncs.size();
	}

	public ArrayList getAllFunc() {
		return groundFuncs;
	}

	public String toString() {
		String s = new String();
		for (int i = 0; i < groundFuncs.size(); i++)
			s += ((Function) groundFuncs.get(i)).toString() + "\n";

		return s;
	}
}
