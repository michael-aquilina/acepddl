/***********************************************************
   Author: Minh B. Do - Arizona State University
***********************************************************/
package mt.uol.smi.attempto.aceview.pddl.sapa.parsing;

import java.util.*;

import mt.uol.smi.attempto.aceview.pddl.sapa.basic_ds.*;

/**
 * Domain: Storing Domain structure returned by parser from reading the domain
 * file in PDDL2.1.
 */
public class Domain {
	String name;
	ArrayList requirements = new ArrayList();

	ArrayList types = new ArrayList(); // For object types in this domain
	Hashtable typeMap = new Hashtable(); // Key: SubType; Value: SuperType

	ArrayList constants = new ArrayList(); // For "object" constaints
	Hashtable constantType = new Hashtable(); // Key: Object; Value: Type of that Object
	Hashtable typeConstants = new Hashtable(); // Key: Type; Value: array of objects of that type

	ArrayList predicates = new ArrayList(); // List of all (abstract) predicate
	ArrayList functions = new ArrayList(); // List of all (abstract) functions

	ArrayList actions = new ArrayList(); // List of all action

	ArrayList lhs = new ArrayList(); // List of all function that appear in some
	// lefthand-side of some formular (so their values may be changed by
	// some action), we use this information when grounding actions

	boolean typedDomain = true; // If the domain is typed or untyped (old format)

	/** For name of the domain */
	public void setName(String n) {
		name = n;
	}

	public String getName() {
		return name;
	}

	public void setDomainType(boolean typeFlag) {
		typedDomain = typeFlag;

		if (typedDomain == false)
			putType("basic_type");
	}

	public boolean isTyped() {
		return typedDomain;
	}

	/** For requirement list (e.g. strips, durative-action) */
	public void putReq(Object o) {
		requirements.add(o);
	}

	public int numReq() {
		return requirements.size();
	}

	public String getReq(int index) {
		return (String) requirements.get(index);
	}

	/** Add a new object type (e.g aircraft, truck, phyobj) */
	public void putType(Object o) {
		if (!types.contains(o))
			types.add(o);
	}

	public int numType() {
		return types.size();
	}

	public String getType(int index) {
		return (String) types.get(index);
	}

	public ArrayList getAllTypes() {
		return types;
	}

	/**
	 * Add a type map between CHILD and PARENT types (e.g. truck,plane -> vehicle)
	 */
	public void putTypeMap(ArrayList type_list, String super_type) {
		Object obj;
		int i;

		for (i = 0; i < type_list.size(); i++) {
			obj = type_list.get(i);

			putType(obj);
			typeMap.put(obj, super_type);
		}

		putType(super_type);
	}

	/** Get the parent type of a given subtype */
	public String getParent(String subType) {
		return (String) typeMap.get(subType);
	}

	/**
	 * Function to exclusively deal with the "either" declaration in predicate type
	 * declaration (e.g. ZenoTravel domain in the competition). Re-arrange the type
	 * tree that map sub-type and super-type.
	 */
	public void ppTypeTree() {
		String eitherSuperType, subTypeName, superTypeName;
		ArrayList subTypes = new ArrayList();
		Predicate aPred;

		int i, j, typeIndex, oldIndex = 0, eIndex = types.size();

		/* Go through all predicate template to find the eitherSuperType */
		for (i = 0; i < predicates.size(); i++) {
			aPred = (Predicate) predicates.get(i);
			for (j = 0; j < aPred.predSize(); j++) {
				eitherSuperType = aPred.getObj(j);
				if (eitherSuperType.indexOf('*') >= 0)
					types.add(eitherSuperType);
			}
		}

		for (i = eIndex; i < types.size(); i++) {
			eitherSuperType = (String) types.get(i);

			// Get the list of subtypes of his eitherSuperType
			oldIndex = 0;
			while ((typeIndex = eitherSuperType.indexOf('*', oldIndex)) > 0) {
				subTypes.add(eitherSuperType.substring(oldIndex, typeIndex));
				oldIndex = typeIndex + 1;
			}

			// Check if all subtypes share the same parent. If not report error
			superTypeName = getParent((String) subTypes.get(0));
			for (j = 1; j < subTypes.size(); j++) {
				if (!superTypeName.equals(getParent((String) subTypes.get(j)))) {
					System.out.println("Error: types in the declaration (either " + eitherSuperType
							+ ") do not share same parent type.");
					System.exit(1);
				}
			}

			// Set the eitherSuperType to be the subType of the shared parent type
			for (j = 0; j < subTypes.size(); j++)
				typeMap.put(subTypes.get(j), eitherSuperType);

			typeMap.put(eitherSuperType, superTypeName);
		}
	}

	/** Add a list of constants */
	public void putConstants(ArrayList cList, String cType) {
		String constant;
		for (int i = 0; i < cList.size(); i++) {
			constant = (String) cList.get(i);
			constants.add(constant);
			constantType.put(constant, cType);
		}

		typeConstants.put(cType, new ArrayList(cList));
	}

	public String getConstant(int index) {
		return (String) constants.get(index);
	}

	public String getConstantType(String constant) {
		return (String) constantType.get(constant);
	}

	public ArrayList getConstantList(String cType) {
		return (ArrayList) typeConstants.get(cType);
	}

	public Hashtable getConstantMap() {
		return typeConstants;
	}

	/** Add a predicate to the predicate list */
	public void putPred(Predicate pred) {
		predicates.add(pred);
	}

	public int numPred() {
		return predicates.size();
	}

	public Predicate getPred(int index) {
		return (Predicate) predicates.get(index);
	}

	/** Add a function to the list of functions */
	public void putFunction(Function func) {
		functions.add(func);
	}

	public int numFunction() {
		return functions.size();
	}

	public Function getFunction(int index) {
		return (Function) functions.get(index);
	}

	/** Add an action to the action template set */
	public void putAction(Action act) {
		actions.add(act);
	}

	public int numAction() {
		return actions.size();
	}

	public Action getAction(int index) {
		return (Action) actions.get(index);
	}

	/**
	 * Add a function name to the list of function that can be changed by some
	 * action. Thus, predicate type that may change value when we do planning
	 */
	public void addLhs(String predName) {
		lhs.add(predName);
	}

	public void setLhs(ArrayList predNameList) {
		lhs = new ArrayList(predNameList);
	}

	public boolean inLhs(String predName) {
		return lhs.contains(predName);
	}
}
