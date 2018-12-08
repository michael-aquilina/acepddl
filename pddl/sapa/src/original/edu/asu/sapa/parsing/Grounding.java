/**************************************************************************
   Author: Minh B. Do - Arizona State Univ.
***************************************************************************/
package edu.asu.sapa.parsing;

import edu.asu.sapa.basic_ds.*;
import edu.asu.sapa.complex_ds.*;

import java.util.*;

/**
 * Grounding: Class to help to ground Predicate, Function, Actions, StateManager
 * etc. according to the templates in the domain file and the object instances
 * in the problem file.
 */
public class Grounding {
	ArrayList aArrayList = new ArrayList();
	// Predicates that can (estimatedly) possibly be true/false
	// those are preds that are true/false in the initial state
	// or belong to the add/delete list of some actions
	// (we can write a more sophisticated algorithm later to make
	// the list more correct. Now only the estimation)
	boolean[] dynamicPositivePreds;
	boolean[] dynamicNegativePreds;
	// List of predicates true in the initial state
	ArrayList initialPreds;

	// list of possible static predicates (initially only the initial state
	// predicates)
	int[] staticPositivePreds;
	boolean[] staticPositivePredsFlag; // false = static
	int initialPredsSize; // initial size of predicates

	// list of possible static functions (initially a list of initial state
	// functions)
	boolean[] staticFunctionsFlag; // false = static
	int initialFuncSize; // number of functions defined in initial state

	ArrayList predSigList = new ArrayList(); // List of signature of predicate
	// the index of them will represent a ground predicate's ID.
	ArrayList funcSigList = new ArrayList();
	ArrayList actSigList = new ArrayList();

	boolean dupaFlag = true; // Do we want to check duplicate parameters?
	// (two para map to the same object)

	// GAction instance;

	Problem aProb;
	Domain aDomain;

	public Grounding(boolean dupa) {
		dupaFlag = dupa;
	}

	/** Check if "parent" is a supertype of "child" */
	public boolean checkSuperType(Domain domain, String parent, String child) {
		if (parent.equalsIgnoreCase(child))
			return true;

		String temp = child, ans;
		while ((ans = domain.getParent(temp)) != null) {
			if (parent.equalsIgnoreCase(ans))
				return true;
			temp = ans;
		}

		return false;
	}

	/**
	 * Function to check if a function instance is correctly specified according to
	 * the domain and problem specification (if all objects in the function are in
	 * correct types)
	 */
	public int checkExistFunc(Function func) {
		String signature = func.getName();
		int i;

		for (i = 0; i < func.funcSize(); i++)
			signature += "*" + func.getObj(i);

		return funcSigList.indexOf(signature);

	}

	/**
	 * Function to check if a predicate instance is correctly specified according to
	 * the domain and problem specification. Similar to checkFunc() above. Used to
	 * check initial/goal predicates in the problem file
	 */
	public int checkExistPred(Predicate pred) {
		String signature = pred.getName();
		int i;

		for (i = 0; i < pred.predSize(); i++)
			signature += "*" + pred.getObj(i);

		return predSigList.indexOf(signature);
	}

	/**
	 * Ground (find all "signature") the predicate template (e.g: (move ?truck
	 * ?city1 ?city2)) will all combinations of relevant objects
	 */
	private void getAllGroundPredicate(Predicate template) {
		int size = template.predSize(), pointer, i, j;
		int[] index = new int[size], numObj = new int[size];
		ArrayList[] objSet = new ArrayList[size];
		String type;
		String predSig; // Signature of the pred = concatenation of all objects

		if (size == 0) {
			predSigList.add(new String(template.getName()));
			return;
		}

		for (i = 0; i < size; i++) {
			index[i] = 0;
			type = template.getObj(i);
			objSet[i] = (ArrayList) aProb.getObjectMap(type);
			numObj[i] = objSet[i].size();
		}

		pointer = size - 1;
		while (true) {
			predSig = new String(template.getName());
			for (i = 0; i < size; i++) {
				predSig += "*" + (String) objSet[i].get(index[i]);
			}
			predSigList.add(predSig);

			// If we go over the limit, recursively backtrack and increase obj index
			while (++index[pointer] >= numObj[pointer]) {
				index[pointer] = 0;
				pointer--;
				if (pointer < 0) {
					break;
				}
			}

			// If we already go through all possible combinations
			if (pointer < 0)
				break;
			else
				pointer = size - 1;
		}
	}

	/**
	 * Get the StateManager object according to domain/problem specification.
	 * Specifically, ground all predicates, actions, read in the initial/goal state.
	 */
	public StateManager getStateManager(Domain aD, Problem aP) {
		aDomain = aD;
		aProb = aP;

		StateManager pm = new StateManager(aProb.numInitFunct());
		Predicate tempPred; // Grounded Predicate
		String name, type;
		int i, j, k, baseIndex, index;

		/*
		 * Get the predicate *template* from the domain file, then get the object
		 * *instance* from the problem file and then combine them to get the set of
		 * grounded predicates
		 */
		for (i = 0; i < aDomain.numPred(); i++) {
			tempPred = aDomain.getPred(i); // get the template
			getAllGroundPredicate(tempPred);
		}

		// Get all ground predicates from the Domain specification to make
		// the initial PredDB
		name = new String();
		ArrayList aPredList = new ArrayList();
		for (i = 0; i < aProb.numInitPred(); i++) {
			tempPred = (Predicate) aProb.getInitPred(i);
			if ((index = checkExistPred(tempPred)) < 0) {
				System.out.println("Predicate: " + tempPred + " is not correctly specified.");
				System.exit(0);
			}

			// Put the ground Predicate ID in the PredDB
			aPredList.add(new Integer(index));
		}
		pm.setInitPredDB(new ArrayList(aPredList));

		// Get the Goal State
		Float goalTime;
		GPredDB p = new GPredDB();
		for (i = 0; i < aProb.numGoalPred(); i++) {
			tempPred = (Predicate) aProb.getGoalPred(i);
			goalTime = (Float) aProb.getGoalTime(tempPred);
			/* sanity check */
			if ((index = checkExistPred(tempPred)) < 0) {
				System.out.println("Predicate: " + tempPred + " is not correctly specified.");
				System.exit(0);
			}

			// Put the ground predicate in the Goal GPredDB
			p.addPred(new Integer(index));
			p.putTimeMap(new Integer(index), goalTime);
		}
		pm.setGoalPredDB(p);

		// BM: Added Aug 27, 2003. Read in the Exogenous Event list
		for (i = 0; i < aProb.numExoEvent(); i++) {
			tempPred = aProb.getExoEvent(i);
			if ((index = checkExistPred(tempPred)) < 0) {
				System.out.println("Predicate: " + tempPred + " is not correctly specified.");
				System.exit(0);
			}

			pm.addInitEvent(new Event(new Integer(index), aProb.getExoEventSign(i), aProb.getExoEventTime(i)));
		}

		return pm;
	}

	/**
	 * Get the MResDB, thus ground all *Functions* of the domain (metric resources).
	 * Quite similar to the function "getAllGroundPredicate". The diffence in
	 * intialize the set of functions and predicate is that the set of function will
	 * not change from the one specified in the problem file. Thus the set of
	 * function in the initial state is also the set of all functions. This must be
	 * called before groundActions() is called.
	 */
	public GMResDB getMResDB() {
		GMResDB resDB = new GMResDB(aProb.numInitFunct());
		resDB.solidify(); // indicate that the number of functions will never increase
		Function gFunc = new Function();
		String signature;
		int i, j, baseIndex;

		// Get all ground function from the Domain specification
		baseIndex = 0;
		String name = new String();
		for (i = 0; i < aProb.numInitFunct(); i++) {
			gFunc = (Function) aProb.getInitFunct(i);

			signature = new String(gFunc.getName());
			for (j = 0; j < gFunc.funcSize(); j++)
				signature += "*" + gFunc.getObj(j);
			funcSigList.add(signature);

			resDB.setValue(i, gFunc.getValue());
		}

		return resDB;
	}

	/**
	 * Ground a predicate according to the template and a set of ground instance
	 */
	private Integer groundPredicate(Hashtable objMap, Predicate template) {
		Object obj, objTemp;
		String signature = new String(template.getName());
		int index;

		for (int i = 0; i < template.predSize(); i++) {
			objTemp = template.getObj(i);

			if (((String) objTemp).charAt(0) == '?') { // If is a normal variable
				obj = objMap.get(objTemp);

				if (obj == null) {
					System.out.println("Error in Grounding.groundPredicate: " + "unable getting"
							+ (String) template.getObj(i) + "for predicate template: " + template);
					System.exit(0);
				}

				signature += "*" + (String) obj;
			} else // If this object is a constant or ground object already
				signature += "*" + (String) objTemp;
		}

		index = predSigList.indexOf(signature);

		if (index < 0) {
			System.out.println("Grounding.groundPredicate: Predicate " + signature + " is invalid");
			System.exit(0);
		}

		return new Integer(index);
	}

	/**
	 * Function to ground a function according to the template and a set of ground
	 * instance
	 */
	private int groundFunction(Hashtable objMap, Function template) {
		String signature = new String(template.getName());
		Object obj, objTemp;
		int index;

		for (int i = 0; i < template.funcSize(); i++) {
			objTemp = template.getObj(i);

			if (((String) objTemp).charAt(0) == '?') {
				obj = (String) objMap.get(objTemp);
				if (obj == null) {
					System.out.println("Error in Grounding.groundFunction: " + "unable to ground while getting "
							+ (String) template.getObj(i) + " for function template: " + template);
					System.exit(0);
				}

				signature += "*" + (String) obj;
			} else {
				signature += "*" + (String) objTemp;
			}
		}

		index = funcSigList.indexOf(signature);

		return index;
	}

	/**
	 * Function to get the name of a function based on its index in funcSigList
	 */
	public String getFuncName(int funcIndex) {
		String signature = (String) funcSigList.get(funcIndex);
		int index = signature.indexOf('*');
		if (index < 0)
			index = signature.length();

		return signature.substring(0, index);
	}

	public String getFuncSig(int funcIndex) {
		return (String) funcSigList.get(funcIndex);
	}

	public int numPred() {
		return predSigList.size();
	}

	private String getPredName(int predIndex) {
		String signature = (String) predSigList.get(predIndex);
		int index = signature.indexOf('*');
		if (index < 0)
			index = signature.length();

		return signature.substring(0, index);
	}

	public String getPredSig(int predIndex) {
		return (String) predSigList.get(predIndex);
	}

	/**
	 * Get the "signature" of an action given its index in the list of all actions.
	 * Used when printing the final solution.
	 */
	public String getActSig(int actIndex) {
		return (String) actSigList.get(actIndex);
	}

	/**
	 * Function to ground a math-formula according to the set of object instance and
	 * their matchings (?truck->truck-1) XXX bug where we must define (distance
	 * (city-a city-a) 0) for some reason.
	 */
	private GMathForm groundMathForm(Hashtable objMap, MathForm template, GMResDB mr, GAction instance) {
		GMathForm mf = new GMathForm();

		int type = template.getType();
		mf.setType(type);

		if (type == 2) // mf = #t
			return mf;

		if (type == 1) { // mf is some static value
			mf.setValue(template.getValue());
			return mf;
		}

		if (type == 5) { // mf = ?duration
			if (instance.getDType()) { // Action duration is static
				mf.setType(1);
				mf.setValue(instance.getDStatic());
				return mf;
			} else { // Action duration is dynamic
				mf = new GMathForm(instance.getDDynamic());
				return mf;
			}
		}

		if (type == 0) { // some function
			int f = groundFunction(objMap, template.getElement());

			// If that function is not specified in the initial state (invalid function)
			if (f < 0)
				return null;

			// Check if we can replace that function by its value
			if (aDomain.inLhs(getFuncName(f)))
				mf.setElement(f);
			else { // Not in the *lhs* => replace it with the real value;
				mf.setType(1);
				mf.setValue(mr.getValue(f));
			}
			return mf;
		}

		if (type == 3) { // Non-primitive form. Exp: (fuel truck1)*10
			GMathForm left = groundMathForm(objMap, template.getLeft(), mr, instance);
			GMathForm right = groundMathForm(objMap, template.getRight(), mr, instance);
			if (left == null || right == null) {
				System.out.println("Error in grounding Mathform left/right");
				System.exit(0);
			} else {
				if ((left.getType() != 1) || (right.getType() != 1)) {
					mf.setLeft(left);
					mf.setRight(right);
					mf.setOperator(template.getOperator());
				} else {
					mf.setType(1);
					switch (template.getOperator()) {
					case '+':
						mf.setValue(left.getValue() + right.getValue());
						break;
					case '-':
						mf.setValue(left.getValue() - right.getValue());
						break;
					case '*':
						mf.setValue(left.getValue() * right.getValue());
						break;
					case '/':
						mf.setValue(left.getValue() / right.getValue());
						break;
					}
				}
				return mf;
			}
		}

		return mf;
	}

	/**
	 * Function to get a vector of all object set instances from the set of object
	 * types. For example, for list of types (?truck ?loc1 ?loc2) then find all
	 * combinations such as (truck1 phoenix la) that match.
	 */
	private void getObjSet(ArrayList types, ArrayList ins, int index) {
		String type = (String) types.get(index);
		ArrayList objIns = (ArrayList) aProb.getObjectMap(type);

		for (int i = 0; i < objIns.size(); i++) {
			if (index >= ins.size())
				ins.add(objIns.get(i));
			else
				ins.set(index, objIns.get(i));

			if (index == (types.size() - 1))
				aArrayList.add(new ArrayList(ins));
			else
				getObjSet(types, ins, index + 1);
		}
	}

	/**
	 * Function to check if two parameters for one ground actions are the same (so
	 * we can eliminate that action if needed). For example the action (move phoenix
	 * phoenix).
	 */
	private boolean checkDuplicateParameter(ArrayList paraList) {
		int i;
		Object s;

		for (i = 0; i < paraList.size(); i++) {
			s = paraList.get(i);

			if (paraList.indexOf(s) != paraList.lastIndexOf(s))
				return true;
		}
		return false;
	}

	/**
	 * Function to get the set of predicates that will NEVER CHANGE VALUE during the
	 * planning process (e.g (in-city ?airport ?city)). This knowledge can be used
	 * to REDUCE the set of ground actions such that the one which has (in-city
	 * ?location ?city) in the precodition and (in-city ?location ?city) is FALSE in
	 * the initial state.
	 */
	private void initializeDynamicPredicate(StateManager pm) {
		int i, index, numGroundPred = predSigList.size();
		Event e;

		dynamicPositivePreds = new boolean[numGroundPred];
		dynamicNegativePreds = new boolean[numGroundPred];

		initialPredsSize = initialPreds.size();
		staticPositivePredsFlag = new boolean[initialPredsSize]; // false = static
		staticPositivePreds = new int[initialPredsSize];

		// get the IDs of the initial predicates
		for (i = 0; i < initialPredsSize; i++) {
			staticPositivePreds[i] = ((Integer) initialPreds.get(i)).intValue();
		}

		// Initialize according to the facts in the initial state
		for (i = 0; i < numGroundPred; i++) {
			if (initialPreds.contains(new Integer(i))) {
				dynamicPositivePreds[i] = true;
				dynamicNegativePreds[i] = false;
			} else {
				dynamicPositivePreds[i] = false;
				dynamicNegativePreds[i] = true;
			}
		}

		// Initialize according to the initial exogenous events
		for (i = 0; i < pm.numInitEvent(); i++) {
			e = pm.getInitEvent(i);
			index = e.getPred().intValue();

			if (e.getNeg()) {
				dynamicNegativePreds[index] = true;
			} else {
				dynamicPositivePreds[index] = true;
			}

			/*
			 * J. Benton, June 11, 2004: Added to make sure exogenous events are noticed
			 * when discovering what's static. XXX EXPERIMENTAL -- NEEDS TESTING
			 */
			int staticPositivePredsLength = staticPositivePreds.length;
			for (int j = 0; j < staticPositivePreds.length; j++) {
				// its not a static if an exogenous event modifies it!
				if (staticPositivePreds[j] == e.getPred().intValue()) {
					staticPositivePredsFlag[j] = true;
				}
			}
			/* J. Benton end addition */
		}
	}

	/*
	 * Function to get a set of ground actions from the domain and problem
	 * specification: - Check that ALL the parameters should be DIFFERENT from each
	 * other - If something doesn't change during the course of plan (do not appear
	 * then prevent from generate actions such as (drive ?phx-airport
	 * ?denver-aiport) because of the precondition (in-city ?denver-airport ?phx)
	 * that can NEVER become true. - J. Benton, June 10, 2004: Removes static
	 * positive predicates from the initial state of the state manager and also
	 * removes them as preconditions from actions.
	 */
	public ArrayList getGroundActions(StateManager pm, GMResDB mr) {
		Action aTemplate;
		ArrayList gActionList = new ArrayList();
		String actSignature;
		GAction instance;

		ArrayList objList = new ArrayList();
		int i, j, k, l, count = 0;

		// Set up the static predicate list (predicate name) -- see function's
		// description
		initialPreds = pm.getInitState().getPredDB().getAllPred();
		initializeDynamicPredicate(pm);

		// Check if the domain name in the domain and the problem files match
		if (!aDomain.getName().equals(aProb.getDomain())) {
			System.out.println(";; Domain names do not match in domain and prob files!!");
			return gActionList;
		}

		/*
		 * J. Benton June 11, 2004: initialize the static functions array
		 */
		staticFunctionsFlag = new boolean[mr.numFunction()];
		initialFuncSize = mr.numFunction();

		/* J. Benton end addition */

		// Start getting all action templates and then ground them
		for (i = 0; i < aDomain.numAction(); i++) {
			aTemplate = (Action) aDomain.getAction(i);

			// Get and ground all objects
			// Get the list of Types of all Object used in this abstract action
			objList.clear();
			for (j = 0; j < aTemplate.numPara(); j++) {
				Object obj = aTemplate.getPara(j);
				objList.add(aTemplate.getParaType(obj));
			}

			// Get all instance *set* of the template specified by objList
			// Each element of aArrayList will be an ArrayList represent
			// grounded object for Action's parameters (e.g: (package1 truck1 pgh-po))
			aArrayList.clear();

			// BM: Added July 20, 2003 to handle the action template with no parameter
			if (objList.size() > 0) {
				getObjSet(objList, new ArrayList(), 0);
				j = 0;
			} else {
				j = -1;
			}

			// big "make grounded action" loop
			for (; j < aArrayList.size(); j++) {
				ArrayList gIns = new ArrayList();
				if (j >= 0)
					gIns = (ArrayList) aArrayList.get(j);

				// BM: Added Sep 6, 2002 -- Check if two objects in the parameter list are
				// the same instance
				if (dupaFlag && checkDuplicateParameter(gIns))
					continue;

				// Start setting up an instance's components
				instance = new GAction(aTemplate.numPrecond(), aTemplate.numTest(), aTemplate.numDelete(),
						aTemplate.numAdd(), aTemplate.numSet());

				Hashtable objMap = new Hashtable();
				actSignature = new String(aTemplate.getName() + "*");
				for (k = 0; k < gIns.size(); k++) {
					actSignature += (String) gIns.get(k) + "*";
					objMap.put(aTemplate.getPara(k), gIns.get(k));
				}

				/*
				 * Grounding an action's duration
				 */
				if (aTemplate.getDType()) { // Duration is static
					instance.setDType(true);
					instance.setDStatic(aTemplate.getDStatic());
				} else { // Duration is a dynamic value, ground a MathForm
					GMathForm mf = groundMathForm(objMap, aTemplate.getDDynamic(), mr, instance);
					if (mf == null) {
						continue;
					}

					if (mf.getType() == 1) {
						instance.setDType(true);
						instance.setDStatic(mf.getValue());
					} else {
						instance.setDType(false);
						instance.setDDynamic(mf);
					}
				}

				/*
				 * Grounding an action's cost
				 */
				if (aTemplate.getCType()) { // Cost is static
					instance.setCType(true);
					instance.setCStatic(aTemplate.getCStatic());
				} else { // Cost is of type dynamic, ground a MathForm
					GMathForm mf = groundMathForm(objMap, aTemplate.getCDynamic(), mr, instance);
					if (mf == null) {
						continue;
					}
					if (mf.getType() == 1) {
						instance.setCType(true);
						instance.setCStatic(mf.getValue());
					} else {
						instance.setCType(false);
						instance.setCDynamic(mf);
					}
				}

				/*
				 * Preconditions, Adds & Deletes
				 */
				Integer pred;
				Integer time;

				// Preconditions (predicate)
				for (k = 0; k < aTemplate.numPrecond(); k++) {
					time = aTemplate.getPrecondTime(k);
					pred = groundPredicate(objMap, aTemplate.getPrecond(k));

					instance.addPrecond(pred.intValue(), k);
					instance.putPreTime(time.intValue(), k);
				}

				// Add effects (predicate)
				for (k = 0; k < aTemplate.numAdd(); k++) {
					time = aTemplate.getAddTimeEffect(k);
					pred = groundPredicate(objMap, aTemplate.getAdd(k));

					instance.putAdd(pred.intValue(), k);
					instance.putAddTimeEffect(time.intValue(), k);
				}

				// Delete effects (predicate)
				for (k = 0; k < aTemplate.numDelete(); k++) {
					time = aTemplate.getDeleteTimeEffect(k);
					pred = groundPredicate(objMap, aTemplate.getDelete(k));

					instance.putDelete(pred.intValue(), k);
					instance.putDeleteTimeEffect(time.intValue(), k);
				}

				/*
				 * For Test & Set
				 */

				// perform "set" grounding (i.e. increase/decrease)
				MySet setTemplate;
				String aString;
				for (k = 0; k < aTemplate.numSet(); k++) {
					setTemplate = aTemplate.getSet(k);
					time = aTemplate.getSetTimeEffect(k);

					int f = groundFunction(objMap, setTemplate.getLeftSide());

					/*
					 * J. Benton, June 11, 2004: static function discovery
					 */
					// mark "f" as a dynamic (i.e. non-static) function
					staticFunctionsFlag[f] = true; // true = dynamic
					/* J. Benton end addition */
					GMathForm mf = groundMathForm(objMap, setTemplate.getRightSide(), mr, instance);

					if ((f < 0) || (mf == null))
						break;

					aString = setTemplate.getAssign();
					if (aString.equals("="))
						instance.putSet(new GMySet(f, 0, mf), k);
					if (aString.equals("-="))
						instance.putSet(new GMySet(f, 1, mf), k);
					if (aString.equals("+="))
						instance.putSet(new GMySet(f, 2, mf), k);
					if (aString.equals("*="))
						instance.putSet(new GMySet(f, 3, mf), k);
					if (aString.equals("/="))
						instance.putSet(new GMySet(f, 4, mf), k);

					instance.putSetTimeEffect(time.intValue(), k);
				}

				if (k < aTemplate.numSet())
					continue;

				// perform Test grounding
				Test testTemplate;
				for (k = 0; k < aTemplate.numTest(); k++) {
					testTemplate = aTemplate.getTest(k);
					time = aTemplate.getTestTime(k);

					int f = groundFunction(objMap, testTemplate.getLeftSide());
					GMathForm mf = groundMathForm(objMap, testTemplate.getRightSide(), mr, instance);

					if ((f < 0) || (mf == null))
						break;

					aString = testTemplate.getComparator();
					if (aString.equals("=="))
						instance.putTest(new GTest(f, 0, mf), k);
					if (aString.equals("<"))
						instance.putTest(new GTest(f, 1, mf), k);
					if (aString.equals("<="))
						instance.putTest(new GTest(f, 2, mf), k);
					if (aString.equals(">"))
						instance.putTest(new GTest(f, 3, mf), k);
					if (aString.equals(">="))
						instance.putTest(new GTest(f, 4, mf), k);

					// Duration for this test
					instance.putTestTime(time.intValue(), k);
				}

				// If we can't ground the Test according to initial function, then
				// we will not be able to instantiate this action
				if (k < aTemplate.numTest())
					continue;

				// Add actions to the array of grounded actions
				instance.setID(count++);
				gActionList.add(instance);
				actSigList.add(actSignature);
			}
		}

		// Remove "never be applicable actions" according to the dynamic predicate rules
		// First round to setup the dynamicPositive/NegativePreds arrays
		for (i = 0; i < gActionList.size(); i++) {
			instance = (GAction) gActionList.get(i);
			for (j = 0; j < instance.numAdd(); j++) {
				dynamicPositivePreds[instance.getAdd(j)] = true;
			}

			for (j = 0; j < instance.numDelete(); j++) {
				dynamicNegativePreds[instance.getDelete(j)] = true;
			}
		}

		// Second round to remove actions
		boolean noDurativeAct = true;
		for (i = 0; i < gActionList.size(); i++) {
			instance = (GAction) gActionList.remove(i);

			for (j = 0; j < instance.numPrecond(); j++) {
				// Only deal with positive precondition now
				if (dynamicPositivePreds[instance.getPrecond(j)] == false) {
					actSigList.remove(i);
					i--;
					break;
				}
			}

			// Reset the action's ID
			if (j == instance.numPrecond()) {
				instance.setID(i);
				gActionList.add(i, instance);

				if ((instance.getDType() == false) || ((instance.getDType() == true) && (instance.getDStatic() > 0.0)))
					noDurativeAct = false;

				// search for non-static elements
				for (k = 0; k < instance.numDelete(); k++) {
					for (l = 0; l < initialPredsSize; l++)
						if (instance.getDelete(k) == staticPositivePreds[l]) {
							staticPositivePredsFlag[l] = true; // i.e. not static
							break;
						}
				}
			}
			// System.out.print(instance);
		} // end instance for loop

		/*
		 * J. Benton, June 11, 2004: Since we have identified the static functions we
		 * can now fully evaluate all functions that use them and store their values
		 * rather than insist that they be looked up in the metric resource table
		 * (GMResDB) each time they are referenced. (Like compiling down constants...
		 * would be nice if PDDL allowed us to specify that a paritcular value is
		 * constant, rather than slow us down by reasoning about it.)
		 * 
		 * Added June 12, 2004: Notice that Utility.java performs the same method (by
		 * identifying the lhs functions that are modified for an domain). This is
		 * performed on the ungrounded actions, what we have done is the same thing for
		 * the grounded actions. Though, perhaps the two ideas can be combined for
		 * faster grounding.
		 */
		int actSize = gActionList.size();
		int numSets;
		int numTests;
		/*
		 * System.out.print(";; "); for (int x=0;x<staticFunctionsFlag.length;x++) {
		 * System.out.print(staticFunctionsFlag[x] + ":");
		 * System.out.print(mr.getValue(x) + " "); }
		 * 
		 * System.out.println();
		 */
		for (int x = 0; x < actSize; x++) {
			GAction inst = (GAction) gActionList.get(x);
			// All "sets" for this action... (effects)
			numSets = inst.numSet();
			for (int y = 0; y < numSets; y++) {
				inst.getSet(y).setConstant(mr, staticFunctionsFlag);
			}
			// and all "tests" for this action... (preconditions)
			numTests = inst.numTest();
			for (int y = 0; y < numTests; y++) {
				inst.getTest(y).setConstant(mr, staticFunctionsFlag);
			}

			/* J. Benton, June 12, 2004: Adding compilation for duration and cost. */
			// and duration...
			if (!inst.getDType()) { // false means duration is a formula
				try {
					GMathForm dynamicDuration = inst.getDDynamic();
					dynamicDuration.valueCheckStatics(mr, staticFunctionsFlag);
					if (dynamicDuration.getType() == 1) { // 1 = constant value
						inst.setDStatic(dynamicDuration.getValue());
						inst.setDType(true);
						inst.duration_constant = true;
					}
				} catch (CannotEvaluateException e) {
					inst.duration_constant = false;
				}
			}

			// and cost...
			if (!inst.getCType()) { // false means cost is a formula
				try {
					GMathForm dynamicCost = inst.getCDynamic();
					dynamicCost.valueCheckStatics(mr, staticFunctionsFlag);
					if (dynamicCost.getType() == 1) { // 1 = constant value
						inst.setCStatic(dynamicCost.getValue());
						inst.setCType(true);
						inst.cost_constant = true;
					}
				} catch (CannotEvaluateException e) {
					inst.cost_constant = false;
				}
			}
			/* J. Benton addition end */
		}
		/* J. Benton addition end */

		/*
		 * J. Benton, June 12, 2004: Now that we've compiled the functions down we can
		 * remove the static functions from the function database, thereby minimizing
		 * what needs to be copied between states.
		 */

		GMResDB mResDB = pm.getInitState().getMResDB();

		mResDB.removeResources(staticFunctionsFlag);
		pm.setInitMResDB(mResDB);

		/* J. Benton addition end */

		/*
		 * J. Benton, June 9/10, 2004: get all good static predicates, remove them from
		 * actions and initial state.
		 */

		int copyIndex = k = 0; // element in array to test next
		boolean firstDynamic = true;
		while (copyIndex < staticPositivePreds.length) {
			// if its static (false means its static)
			if (!staticPositivePredsFlag[copyIndex]) {
				// if the next one is dynamic, then it will be the first
				// of a list of dynamic predicates
				firstDynamic = true;
				staticPositivePreds[k] = staticPositivePreds[copyIndex];
				staticPositivePredsFlag[k] = staticPositivePredsFlag[copyIndex];
				copyIndex++;
				k++;
			} else {
				// copy down
				// if this is not the first dynamic predicate in a contiguous line of them
				// then increment the copyIndex
				if (!firstDynamic) {
					++copyIndex;
				}
				// to handle case where the last predicate is dynamic
				// (so we don't need to copy anything... we are done).
				if (copyIndex == staticPositivePreds.length)
					break;
				firstDynamic = false;
				staticPositivePreds[k] = staticPositivePreds[copyIndex];
				staticPositivePredsFlag[k] = staticPositivePredsFlag[copyIndex];
			}
		}

		// make new array
		int[] staticPositivePredsNew = new int[k];

		// copy old array into new array
		System.arraycopy(staticPositivePreds, 0, staticPositivePredsNew, 0, k);
		staticPositivePreds = staticPositivePredsNew;

		// remove static precondition predicates from actions and initial state
		int staticPredsLength = staticPositivePreds.length;
		for (i = 0; i < actSize; i++) {
			instance = (GAction) gActionList.get(i);
			int numPrecond = instance.numPrecond();
			k = 0;
			while (k < instance.numPrecond()) {
				boolean removed;
				removed = false;
				int preCond = instance.getPrecond(k);
				for (j = 0; j < staticPredsLength; j++) {
					// if the current precondition is static...
					if (staticPositivePreds[j] == preCond) {
						// then remove it from the action.
						instance.removePrecond(k);
						removed = true;
						break;
					}
				}
				if (!removed) {
					k++;
				}
			}

			// resize the preconditions array for this action
			instance.resetPreconditionArray();
			// System.out.print(instance);
		}

		int currentInitPredSize = initialPreds.size();

		// since staticPositivePreds is derived from initialPreds,
		// we know that we can remove all static predicates easily.
		for (i = 0; i < staticPredsLength; i++) {
			if (initialPreds.contains(new Integer(staticPositivePreds[i]))) {
				initialPreds.remove(new Integer(staticPositivePreds[i]));
			}
		}

		System.out.println(";; Number of Static Predicates: " + (currentInitPredSize - initialPreds.size()));

		pm.setInitPredDB(initialPreds);

		// remove static goal predicates, just in case they are there -- probably not,
		// but must do this for completeness.
		GPredDB goalPreds = pm.getGoalState().getPredDB();
		for (i = 0; i < staticPredsLength; i++) {
			goalPreds.removePredicate(staticPositivePreds[i]);
		}

		pm.setGoalPredDB(goalPreds);

		/* J. Benton addition end */

		// Check if the domain contains only instantaneous actions, then convert
		// them to 1.0 duration action according to the TGP rules
		if (noDurativeAct) {
			for (i = 0; i < actSize; i++) {
				instance = (GAction) gActionList.remove(i);
				instance.setDStatic((float) 1.0);

				for (j = 0; j < instance.numPrecond(); j++) {
					// Set up if that precondition should be "at start" or "over all"
					if (instance.indexDelete(instance.getPrecond(j)) < 0)
						instance.putPreTime(1, j);
				}

				for (j = 0; j < instance.numAdd(); j++) {
					// If "add" effect then put it at the end
					instance.putAddTimeEffect(1, j);
				}

				gActionList.add(i, instance);
			}
		}

		return gActionList;
	}
}
