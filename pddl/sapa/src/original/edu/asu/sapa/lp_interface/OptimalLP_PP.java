/**********************************************************
        Author: Minh B. Do - Arizona State University
***********************************************************/

package edu.asu.sapa.lp_interface;

import edu.asu.sapa.basic_ds.*;
import edu.asu.sapa.complex_ds.*;
import edu.asu.sapa.utils.*;
import edu.asu.sapa.lpsolve.*;
import edu.asu.sapa.parsing.*;

import java.util.*;

/**
 * OptimalLP_PP.java: Interface to convert the problem of finding an optimal o.c
 * plan from the set of actions in the original p.c plan by converting it into
 * the MILP encoding and use the lp_solve to solve the problem.
 *
 * Note: The result would be better or equal than the one returned by doing
 * Post-Processing using the GreedyPost class.
 */

public class OptimalLP_PP {
	// Constraint types
	final static short TRUE = 1;
	final static short FALSE = 0;

	final static short LE = 0;
	final static short EQ = 1;
	final static short GE = 2;
	final static short OF = 3;

	// Define some constants
	final static short CAUSAL_LINK = 1;
	final static short LOGICAL_MUTEX = 2;
	final static short RES_MUTEX = 3;
	final static float DELTA = (float) 0.01;

	// Constants for the objective function
	final static short MIN_MAKESPAN = 1;
	final static short MIN_ORDERING = 2;
	final static short MAX_AVE_FLEX = 3;
	final static short MAX_MIN_FLEX = 4;
	final static short MAX_AVE_SLACK = 5;
	final static short MAX_MIN_SLACK = 6;

	// Big M constant
	// final static double BIG_M = 1000000;
	// final static double BIG_M = 1000;
	// final static double SMALL_M = 0.000001;
	// final static double SMALL_M = 0.001;
	double BIG_M, SMALL_M;
	final static double SMALL_ADJ = 0.001;
	final static int SIG_MASK = 10000;

	final static int ORIG_PLAN = 1;
	final static int OPT_PLAN = 2;

	public Ordering[] orderingSet;
	public ArrayList aPlan, actSigList;
	public float[] startTimes, actDurs;
	public int planSize;

	int[] cActs; // Set of actions that support a specific proposition
	int caIndex = 0;

	int[] dActs; // Set of actions that delete a specific proposition
	int daIndex = 0;

	int[] resActs; // Set of actions that change a given resource
	int raIndex;

	float delta = (float) 0.01;

	int ceIndex, lmIndex, rmIndex, stIndex; // Indexes of each var type in the IP_Vars array.
	int numCE, numLM, numRM, numST, numResPreConst = 0; // Number of causal effect, log-mutex, and res-mutex vars
	int numGoal;

	ArrayList[] goalSA; // Supporting action set for each goal
	float bestMakespan; // The best solution makespan we found so far
	String outputPlan = new String();
	int ObjFunction; // Objective function (minimize makespan, minimize #orderings etc.)

	ArrayList IP_Vars = new ArrayList();
	ArrayList IP_Var_Sig = new ArrayList();

	ArrayList Mutex_Consts = new ArrayList();
	ArrayList OneSupport_Consts = new ArrayList();
	ArrayList CLProtect_Consts = new ArrayList();
	ArrayList OrderTemp_Consts = new ArrayList();
	edu.asu.sapa.lp_interface.ResPreCons[] resPre_Const;
	// ArrayList ResPre_Consts = new ArrayList();
	ArrayList TemporalBound_Consts = new ArrayList();

	// lp_solve related variables
	solve lpSolve = new solve();
	lprec lpIn;

	double v[];
	int totalVars, totalConst;

	ArrayList lhsRes;
	LP_Utility lpUtil = new LP_Utility();
	DResProfile resProfile;

	GMResDB initMR_DB;
	Grounding groundInstance;

	ArrayList solution = new ArrayList(); // LP solution get by lpSolve.get_solution()

	String optPlan = new String();
	String origPlan = new String();

	boolean tp1, tp2; // tp (timepoint) = true/false = start/end time
	OCPlan anOCPlan;

	int[] clArray;

	public OptimalLP_PP() {

	}

	/**
	 * Passing the information from the Planner.java class after the solution is
	 * found
	 */
	public void initialize(ArrayList actions, ArrayList actionSigList, ArrayList times, ArrayList durs, float bms,
			int objFunc, ArrayList lhsResource, GMResDB initMResDB, Grounding gI) {
		int i;
		GAction anAction;

		aPlan = new ArrayList(actions);

		actSigList = actionSigList;
		planSize = aPlan.size();

		startTimes = new float[planSize];
		actDurs = new float[planSize];

		for (i = 0; i < planSize; i++) {
			startTimes[i] = ((Float) times.get(i)).floatValue();
			actDurs[i] = ((Float) durs.get(i)).floatValue();
		}

		orderingSet = new Ordering[planSize];
		for (i = 0; i < planSize; i++)
			orderingSet[i] = new Ordering(i);

		cActs = new int[planSize];
		dActs = new int[planSize];
		resActs = new int[planSize];

		bestMakespan = bms; // To check if this solution has the best makespan
		numCE = numLM = numRM = 0;

		ObjFunction = objFunc;
		lhsRes = lhsResource;

		initMR_DB = new GMResDB(initMResDB);
		// Debugging
		// System.out.println("\nOptimalLP_PP.initialize(): " + initMResDB.toString());
		//
		resProfile = lpUtil.getResProfile(actions, lhsResource, new GMResDB(initMResDB));

		groundInstance = gI; // Solely for debugging functions only, can be deleted later.
		IP_Var_Sig.add(new Long(0));

		origPlan = buildPlan(ORIG_PLAN);

		// Setup the value of BIG_M and SMALL_M dynamically
		BIG_M = DELTA;
		SMALL_M = 1;
		for (i = 1; i < (planSize - 1); i++) {
			BIG_M += actDurs[i];
			if (SMALL_M > (actDurs[i] / 100))
				SMALL_M = actDurs[i] / 100;
		}
		BIG_M = BIG_M * 1.5;

		clArray = new int[planSize];
		for (i = 0; i < planSize; i++)
			clArray[i] = 0;
	}

	/**
	 * Function to find a list of actions that support a given *prop* (Extended to
	 * include goals also)
	 */
	private void getSPAct(int prop, int actIndex) {
		GAction act;
		int addTime;
		int i, index;

		// Finding the set of actions that support *prop* (excluding actIndex)
		caIndex = 0;
		for (i = 0; i < planSize; i++) {
			if (i == actIndex)
				continue;

			act = (GAction) aPlan.get(i);
			if ((index = act.indexAdd(prop)) < 0)
				continue;

			cActs[caIndex++] = i;
		}

		// If we can't find the right supporter for *prop*, report error
		if (caIndex == 0) {
			System.out.println("OptimalLP_PP.getSPAct(): No supporter for precond.");
			System.exit(1);
		}
	}

	/**
	 * Function to find a list of actions that delete a given *prop* (Extended to
	 * include goals also)
	 */
	private void getDAct(int prop) {
		GAction act;
		int delTime;
		int i, index;

		// Finding the set of actions that delete *prop*
		daIndex = 0;
		for (i = 0; i < planSize; i++) {
			act = (GAction) aPlan.get(i);
			if ((index = act.indexDelete(prop)) < 0)
				continue;

			dActs[daIndex++] = i;
		}
	}

	/**
	 * Function to find a list of actions that use (change value) of a given
	 * ResourceID
	 */
	private void getResAct(int resID) {
		GAction act;
		int delTime;
		int i, j, index;

		// Finding the set of actions that change the value of *resID*
		raIndex = 0;
		for (i = 0; i < planSize; i++) {
			act = (GAction) aPlan.get(i);

			for (j = 0; j < act.numSet(); j++)
				if (resID == act.getSet(j).getLeftSide())
					resActs[raIndex++] = i;
		}
	}

	/**
	 * Function to add a new variable to the list of IP_Vars
	 */
	private void addIPVar(int type, int objID, int act1, int act2) {
		float sepDur = getSepDur(type, objID, act1, act2);

		IP_Vars.add(new LP_Var(type, objID, act1, act2, tp1, tp2, sepDur));

		// Build the signature for each variable
		long signature;

		// Check for error
		if (objID >= SIG_MASK || act1 >= SIG_MASK || act2 >= SIG_MASK) {
			System.out.println("OptimalLP_PP.addIPVar: Error in argument values");
			System.exit(0);
		}

		// System.out.println("AddIPVar: type = " + type + " objID = " + objID
		// + " act1 = " + act1 + " act2 = " + act2);

		signature = (long) type;
		signature = signature * SIG_MASK + objID;
		signature = signature * SIG_MASK + act1;
		signature = signature * SIG_MASK + act2;
		IP_Var_Sig.add(new Long(signature));
	}

	/**
	 * Function to find a mutex variable Y^p_{AB} or Y^r_{AB} given the values of p,
	 * r, A, B.
	 */
	private int getIPVarID(int type, int objID, int act1, int act2) {
		long signature;

		signature = (long) type;
		signature = signature * SIG_MASK + objID;
		signature = signature * SIG_MASK + act1;
		signature = signature * SIG_MASK + act2;

		return IP_Var_Sig.indexOf(new Long(signature)); // Added one for the dummy var at index 0
	}

	private void debugSignature(int type, int objID, int act1, int act2) {
		long signature;

		signature = (long) type;
		signature = signature * SIG_MASK + objID;
		signature = signature * SIG_MASK + act1;
		signature = signature * SIG_MASK + act2;

		if (!IP_Var_Sig.contains(new Long(signature))) {
			System.out.println("Searching for: " + signature);
			System.out.println("Signature list: ");
			for (int i = 0; i < IP_Var_Sig.size(); i++)
				System.out.println((Long) IP_Var_Sig.get(i));
		}
	}

	/**
	 * Function to go backward and build the set of variables stored in LP_Var
	 * datastructure. Also setting up several types of constraints.
	 */
	private void setupLPVars() {
		int i, j, k, l, addIndex = 0, index;
		GAction anAct, tempAct;
		int prop, resID;

		OneSupportCons osCons;
		MutexCons mexCons;

		OneSupport_Consts.clear();
		Mutex_Consts.clear();
		// Go through all actions and their preconditions
		// to find the set of supporting variable X^p_{AB}
		IP_Vars.add(new LP_Var(0, 0, 0, 0, true, true, 0)); // Start with a dummy var (lp_solve requires vars indexed
															// from 1)
		ceIndex = IP_Vars.size();
		for (i = planSize - 1; i > 0; i--) {
			anAct = (GAction) aPlan.get(i);

			for (j = 0; j < anAct.numPrecond(); j++) {
				prop = anAct.getPrecond(j);
				getSPAct(prop, i);

				// Set up the set of variables for causal-effect and one constraint
				// for them.
				osCons = new OneSupportCons(caIndex);
				for (k = 0; k < caIndex; k++) {
					osCons.addVar(k, IP_Vars.size());
					addIPVar(CAUSAL_LINK, prop, cActs[k], i);
				}
				OneSupport_Consts.add(osCons);
				// Debugging/statistic
				clArray[osCons.size()]++;
			}
		}
		numCE = IP_Vars.size() - 1;

		// Now the logical mutex variables
		lmIndex = IP_Vars.size();
		for (i = planSize - 1; i >= 0; i--) {
			anAct = (GAction) aPlan.get(i);

			for (j = 0; j < anAct.numPrecond(); j++) {
				prop = anAct.getPrecond(j);
				getDAct(prop);

				for (k = 0; k < daIndex; k++) {
					if ((i != dActs[k]) && (getIPVarID(LOGICAL_MUTEX, prop, dActs[k], i) < 0)) {
						addIPVar(LOGICAL_MUTEX, prop, i, dActs[k]);
						addIPVar(LOGICAL_MUTEX, prop, dActs[k], i);

						Mutex_Consts.add(new MutexCons(IP_Vars.size() - 2, IP_Vars.size() - 1));
					}
				}
			}

			for (j = 0; j < anAct.numAdd(); j++) {
				prop = anAct.getAdd(j);
				getDAct(prop);

				for (k = 0; k < daIndex; k++) {
					if ((i != dActs[k]) && (getIPVarID(LOGICAL_MUTEX, prop, dActs[k], i) < 0)) {
						addIPVar(LOGICAL_MUTEX, prop, dActs[k], i);
						addIPVar(LOGICAL_MUTEX, prop, i, dActs[k]);

						Mutex_Consts.add(new MutexCons(IP_Vars.size() - 2, IP_Vars.size() - 1));
					}
				}
			}
		}
		numLM = IP_Vars.size() - lmIndex;

		// Now the resource mutex variables. Only need ordering variable between two
		// actions if one change some resource value that the other check for
		// condition to execute.
		// NOTE: Right now, do not let two actions to execute concurrently (Level 4
		// would require changes here).
		rmIndex = IP_Vars.size();
		for (i = planSize - 1; i > 0; i--) {
			anAct = (GAction) aPlan.get(i);

			for (j = 0; j < anAct.numTest(); j++) {
				resID = anAct.getTest(j).getLeftSide();
				getResAct(resID);

				for (k = 0; k < raIndex; k++) {
					if ((i != resActs[k]) && (getIPVarID(RES_MUTEX, resID, resActs[k], i) < 0)) {
						addIPVar(RES_MUTEX, resID, resActs[k], i);
						addIPVar(RES_MUTEX, resID, i, resActs[k]);

						Mutex_Consts.add(new MutexCons(IP_Vars.size() - 2, IP_Vars.size() - 1));
					}
				}
			}
		}
		numRM = IP_Vars.size() - rmIndex;

		// Start the "virtual" index for the action starting time variables
		stIndex = IP_Vars.size();
		numST = planSize;

		// Debugging

		//
	}

	/**
	 * Function to build the set of constraints for the encoding. NOTE:
	 * Mutex(logical & resource) & OnlyOneSupport constraints are already found in
	 * setupLPVar() function
	 */
	private void setupLPConsts() {
		int i, varIndex, propID, act1, act2, ipvarID1, ipvarID2;
		LP_Var aVar;
		GAction anAct, resAct;

		// First: setup the CausalLinkProtection constraints
		CLProtect_Consts.clear();
		OrderTemp_Consts.clear();
		for (varIndex = ceIndex; varIndex < ceIndex + numCE; varIndex++) {
			aVar = (LP_Var) IP_Vars.get(varIndex);
			propID = aVar.object;
			act1 = aVar.act1;
			act2 = aVar.act2;

			// For a given var X^p_{AB}, find action A' delete p and
			// mutex var Y^p_{A'A}, Y^p_{BA'}
			getDAct(propID);

			for (i = 0; i < daIndex; i++) {
				if ((dActs[i] == act2) || (dActs[i] == act1))
					continue;

				// Get the ID of the two variables Y^p_{A'A}, Y^p_{BA'}
				ipvarID1 = getIPVarID(LOGICAL_MUTEX, propID, dActs[i], act1);
				ipvarID2 = getIPVarID(LOGICAL_MUTEX, propID, act2, dActs[i]);

				// Debugging
				// System.out.println("\tLOG_MUTEX: propID = " + propID
				// + " | act1 = " + dActs[i] + " | act2 = " + act1
				// + " --> getIPVarID() = " + ipvarID1);
				// System.out.println("\tLOG_MUTEX: propID = " + propID
				// + " | act1 = " + act2 + " | act2 = " + dActs[i]
				// + " --> getIPVarID() = " + ipvarID2);
				// ----------

				if ((ipvarID1 < 0) || (ipvarID2 < 0)) {
					System.out.println("OptimalLP_PP.setupLPConsts(): Check for not existing signature!!");
					debugSignature(LOGICAL_MUTEX, propID, dActs[i], act1);
					debugSignature(LOGICAL_MUTEX, propID, act2, dActs[i]);
					System.exit(0);
				}

				// Set up the causal-link protection constraint
				CLProtect_Consts.add(new CLProConst(varIndex, ipvarID1, ipvarID2));
			}

			// Second: setup the "ordering vs. action's starting time" constraints
			OrderTemp_Consts.add(new OrderTemporalConst(CAUSAL_LINK, varIndex));
		}

		// Third: setup the "logical/resource-mutex vs. action's starting time"
		// constraints
		for (varIndex = lmIndex; varIndex < stIndex; varIndex++) {
			if (varIndex < rmIndex)
				OrderTemp_Consts.add(new OrderTemporalConst(LOGICAL_MUTEX, varIndex));
			else
				OrderTemp_Consts.add(new OrderTemporalConst(RES_MUTEX, varIndex));
		}

		// Fourth: Constraint (most complex) for the resource precondition
		setupResPreCons();

		// Fifth: Goal deadline constraints (if exist), take care of it later.
		// Using the "TemporalBoundConst" class

	}

	/**
	 * Function to setup the resource-precondition constraints
	 */
	private void setupResPreCons() {
		int i, j, k, resID;
		GAction anAct;
		GMathForm aMF;
		GTest aTest;
		GMySet aSet;

		ResPreCons aRPC;
		float resAmount, rhsValue;
		Integer resIDInt;

		// Debugging
		// System.out.println("******* Start the setupResPreCons() function
		// **********");
		// resProfile.printResInformation(planSize);
		//

		// Set up the arraylist ResPre_Consts for the number of instances
		// of the class ResPreCons (cardinality = lhsRes.size(), which is # of dynamic
		// resource)
		resPre_Const = new ResPreCons[lhsRes.size()];
		// Debugging
		// System.out.println("\nOptimalLP_PP.setupResPreCons() - initMR_DB's values:");
		//
		for (i = 0; i < lhsRes.size(); i++) {
			resPre_Const[i] = new ResPreCons();
			resID = ((Integer) lhsRes.get(i)).intValue();
			resPre_Const[i].setID(resID);
			resPre_Const[i].setInitLevel(initMR_DB.getValue(resID));

			// Debugging
			// System.out.println("(resID:" + resID + ",V: " +
			// initMR_DB.getValue(resID) + ") ");
			//
		}

		// Build the array ResPre_Consts;
		for (i = 0; i < planSize; i++) {
			// Use the lhsResProfile (DResProfile instance) build earlier
			// Post the *equality* constraints
			// Note: SHOULD be done before checking the precondition (tests)
			for (j = 0; j < resProfile.numResValue(i); j++) {
				resIDInt = resProfile.getResID(i, j);
				resAmount = resProfile.getResValue(i, j);

				// Post the *equality* constraint
				resPre_Const[lhsRes.indexOf(resIDInt)].addCheckAct(i, resAmount, 0);

				// Debugging
				// System.out.println("OptimalLP_PP.setupResPreCons() equality const: (AID:" + i
				// +
				// ",Amount:" + resAmount + ") ");
				//
			}

			// Now the *test* constraints
			anAct = (GAction) aPlan.get(i);
			for (j = 0; j < anAct.numTest(); j++) {
				aTest = anAct.getTest(j);
				resID = aTest.getLeftSide();
				aMF = aTest.getRightSide();

				if (!lhsRes.contains(new Integer(resID))) {
					System.out.println("OptimalLP_PP.setupPreCons: lhs function in Test is a constant"
							+ " check the test again and possibly interchange the lhs and rhs of the formula");
					System.exit(0);
				}

				// Evaluate the value of the rhs using the information in the DResProfile
				rhsValue = valuate(aMF, i);
				resPre_Const[lhsRes.indexOf(new Integer(resID))].addCheckAct(i, rhsValue, aTest.getComparator());
			}

			// Now the *set* constraints
			for (j = 0; j < anAct.numSet(); j++) {
				aSet = anAct.getSet(j);
				resID = aSet.getLeftSide();
				aMF = aSet.getRightSide();

				// Evaluate the value of the rhs using the information in the DResProfile
				rhsValue = valuate(aMF, i);
				switch (aSet.getAssign()) {
				case 0: // Assignment (=)
					resPre_Const[lhsRes.indexOf(new Integer(resID))].addChangeAct(i,
							rhsValue - resProfile.getResValue2(i, resID));
					break;
				case 1: // Decrease (-=)
					resPre_Const[lhsRes.indexOf(new Integer(resID))].addChangeAct(i, -1 * rhsValue);
					break;
				case 2: // Increase (+=)
					resPre_Const[lhsRes.indexOf(new Integer(resID))].addChangeAct(i, rhsValue);
					break;
				default:
					System.out.println("OptmalLP_PP.setupPreCons: Do not handle those cases for now");
					System.exit(1);
				}
			}
		}

		// Debugging
		// System.out.println("******* End of the setupResPreCons() function
		// **********");
		//

	}

	/**
	 * Function to evaluate the value of the rhs formula given the function values
	 * stored in the DResProfile instance
	 */
	private float valuate(GMathForm mf, int actIndex) {
		switch (mf.getType()) {
		case 0: // Ground function (lhsFunc) - check the DResProfile instance
			return resProfile.getResValue2(actIndex, mf.getElement());
		case 1: // primitive value
			return mf.getValue();
		default:
			switch (mf.getOperator()) {
			case '+':
				return valuate(mf.getLeft(), actIndex) + valuate(mf.getRight(), actIndex);
			case '-':
				return valuate(mf.getLeft(), actIndex) - valuate(mf.getRight(), actIndex);
			case '*':
				return valuate(mf.getLeft(), actIndex) * valuate(mf.getRight(), actIndex);
			case '/':
				return valuate(mf.getLeft(), actIndex) / valuate(mf.getRight(), actIndex);
			default:
				return 0;
			}
		}

	}

	private void cleanVarArray() {
		for (int i = 1; i <= totalVars; i++)
			v[i] = 0;
	}

	/**
	 * Function to convert the constraints represented in our Java classes to the
	 * real format of variables and constraints in lp_solve code
	 */
	private void lpsolveConvert(int objFunc) {
		int i, j, k, xID;

		// Decide the size of the encoding (include var/const for objective function)
		totalVars = (IP_Vars.size() - 1) + planSize; // IP_Var + starting time var (V_ms = St_{A_goal})
		// discount the "dummy var" at IP_Vars[0]
		/*
		 * totalConst = Mutex_Consts.size() + OneSupport_Consts.size() +
		 * CLProtect_Consts.size() + OrderTemp_Consts.size() +
		 * TemporalBound_Consts.size() + planSize + getNumResPrecondConst();
		 */
		totalConst = 0;

		// Debugging
		System.out
				.println("\nOptimalLP_PP.lpsolveConvert(): totalVars = " + totalVars + " | totalConst = " + totalConst);
		//

		// Start setting up the constraints
		lpIn = new lprec(totalConst, totalVars);
		/* */
		// lpIn.debug = TRUE;
		// lpIn.verbose = TRUE;
		// lpIn.trace = TRUE;
		/* */

		// The row (array) representing the constraint
		v = new double[totalVars + 1];
		v[0] = 0;

		// **** First, setup the objective function *****
		// Debugging purpose now
		if (objFunc != MIN_MAKESPAN) {
			System.out.println("OptimalLP_PP.OBJ_MILP(): No objFunc != MIN_MAKESPAN now");
			System.exit(0);
		}

		cleanVarArray();
		v[totalVars] = 1;
		// Set the objective function in lp_solve format
		lpSolve.set_obj_fn(lpIn, v);
		lpSolve.set_minim(lpIn);

		// The constraint set for the objective function variable (makespan =
		// st_{A_goal}) > et_Ai = st_Ai + dur[Ai]
		// Debugging
		// System.out.println("\nConstraints et_Ai < st_{A_goal} = makespan: ");
		//
		for (i = 0; i < planSize - 1; i++) { // Neglect the last action, which is A_goal
			v[stIndex + i] = -1;
			lpSolve.add_constraint(lpIn, v, GE, (actDurs[i] + SMALL_M));
			// Debugging
			// printLPConstraint(v, GE, (actDurs[i] + SMALL_M));
			//
			v[stIndex + i] = 0;
		}
		v[totalVars] = 0;

		// Debugging
		// System.out.println("\nConstraints et_{A_init} < st_Ai: ");
		//
		v[stIndex] = -1;
		for (i = 1; i < planSize - 1; i++) {
			v[stIndex + i] = 1;
			lpSolve.add_constraint(lpIn, v, GE, SMALL_M);
			// Debugging
			// printLPConstraint(v, GE, SMALL_M);
			//
			v[stIndex + i] = 0;
		}
		v[stIndex] = 0;

		// Set up the binary constraints for ordering variables: X^p_{AB} \in {0,1}
		// Debugging
		// System.out.println("\nBinary constraints for ordering variables: X^p_{AB} in
		// {0,1}:");
		//
		for (i = 1; i < stIndex; i++) {
			lpSolve.set_int(lpIn, i, TRUE);

			/*
			 * v[i] = 1; lpSolve.add_constraint(lpIn, v, LE, 1);
			 * lpSolve.add_constraint(lpIn, v, GE, 0); // Debugging printLPConstraint(v, LE,
			 * 1); printLPConstraint(v, GE, 0); // v[i] = 0;
			 */
			lpSolve.set_upbo(lpIn, i, 1);
			lpSolve.set_lowbo(lpIn, i, 0);
		}

		// Set up the mutex constraints:
		// Debugging
		// System.out.println("\nMutex constraints for mutex variables: X^p_{AB} +
		// X^p_{BA} = 1:");
		//
		MutexCons mc;
		cleanVarArray();
		for (i = 0; i < Mutex_Consts.size(); i++) {
			mc = (MutexCons) Mutex_Consts.get(i);
			v[mc.vID1] = 1;
			v[mc.vID2] = 1;

			lpSolve.add_constraint(lpIn, v, EQ, 1);
			// Debugging
			// printLPConstraint(v, EQ, 1);
			//
			v[mc.vID1] = 0;
			v[mc.vID2] = 0;
		}

		// Set up the "Only one support" constraints
		// Debugging
		// System.out.println("\nOnly one support constraints:");
		//
		OneSupportCons osc;
		for (i = 0; i < OneSupport_Consts.size(); i++) {
			osc = (OneSupportCons) OneSupport_Consts.get(i);

			for (j = 0; j < osc.size(); j++) {
				v[osc.getVar(j)] = 1;
			}
			lpSolve.add_constraint(lpIn, v, EQ, 1);
			// Debugging
			// printLPConstraint(v, EQ, 1);
			//

			for (j = 0; j < osc.size(); j++) {
				v[osc.getVar(j)] = 0;
			}
		}

		// Set up the "causal link protection" constraints
		// Debugging
		// System.out.println("\nCausallink protection constraints:");
		//
		CLProConst clp;
		for (i = 0; i < CLProtect_Consts.size(); i++) {
			clp = (CLProConst) CLProtect_Consts.get(i);

			v[clp.xID] = -1;
			v[clp.y1ID] = 1;
			v[clp.y2ID] = 1;

			lpSolve.add_constraint(lpIn, v, GE, 0);
			// Debugging
			// printLPConstraint(v,GE,0);
			//
			v[clp.xID] = 0;
			v[clp.y1ID] = 0;
			v[clp.y2ID] = 0;
		}

		// Set up the constraints between ordering & starting time vars
		setupLPOrderTemporalConst();

		// Set up the resource (pre)condition constraints
		setupLPResCons();
	}

	/**
	 * Function to setup the constraints between ordering and temporal (starting
	 * time) variables
	 */
	private void setupLPOrderTemporalConst() {
		int i, j, xID;
		int constType, constObj, act1ID, act2ID;
		float sepDur;
		double rhsValue;

		LP_Var lpVar;
		OrderTemporalConst otc;

		// Debugging
		// System.out.println("\nLP Order Temporal Const: ");
		//
		for (i = 0; i < OrderTemp_Consts.size(); i++) {
			otc = (OrderTemporalConst) OrderTemp_Consts.get(i);
			xID = otc.xID;

			// Get the two actions A, B that involved with that X^p_{AB}
			lpVar = (LP_Var) IP_Vars.get(xID);

			// Set up the LP constraint
			v[xID] = BIG_M;
			v[stIndex + lpVar.act1] = 1;
			v[stIndex + lpVar.act2] = -1;
			rhsValue = BIG_M - lpVar.sepDur;
			lpSolve.add_constraint(lpIn, v, LE, rhsValue);
			// Debugging
			// printLPConstraint(v, LE, rhsValue);
			//

			// clean up
			v[xID] = 0;
			v[stIndex + lpVar.act1] = 0;
			v[stIndex + lpVar.act2] = 0;
		}

	}

	/**
	 * Now the ResPre_Const array is all set up, need to convert it to the real set
	 * of linear constraints
	 */
	private int getNumResPrecondConst() {
		int totalResConst = 0;
		for (int i = 0; i < lhsRes.size(); i++)
			totalResConst += resPre_Const[i].numCheckAct();

		return totalResConst;
	}

	private void setupLPResCons() {
		int preType, ipVar, i, j, k;
		int checkActID, changeActID, resID;
		float checkAmount, changeAmount;
		double rhsValue;
		int[] tempIpVar = new int[planSize + 1];

		// Debugging
		// System.out.println("\nResource Constraints:");
		//
		for (i = 0; i < lhsRes.size(); i++) {
			for (j = 0; j < resPre_Const[i].numCheckAct(); j++) {
				numResPreConst++;

				// Get the constraint first
				resID = resPre_Const[i].getID();
				checkActID = resPre_Const[i].getCheckAct(j);
				checkAmount = resPre_Const[i].getCheckAmount(j);
				preType = resPre_Const[i].getCheckType(j);

				// Now get the set of actions that change the value of resID.
				// set up the constraints between those actions and actID
				for (k = 0; k < resPre_Const[i].numChangeAct(); k++) {
					changeActID = resPre_Const[i].getChangeActID(k);
					if (changeActID == checkActID)
						continue;

					changeAmount = resPre_Const[i].getChangeAmount(k);
					// Get the ILP var represent the ordering relation between (checkID,changeID)
					ipVar = getIPVarID(RES_MUTEX, resID, changeActID, checkActID);

					// For debugging purpose
					if (ipVar < 0) {
						System.out.println("OptimalLP_PP.setupLPResCons(): Getting wrong RES_MUTEX IP_VarID.");
						debugSignature(RES_MUTEX, resID, changeActID, checkActID);
						System.exit(0);
					}

					v[ipVar] = changeAmount;
					// Debugging
					// System.out.print(" + v" + ipVar + "." + changeAmount);
					//
					tempIpVar[k] = ipVar;
				}

				// Debugging
				// System.out.println("\nOptimalLP_PP.setupLPResCons(): checkAmount = " +
				// checkAmount
				// + " | initLevel = " + resPre_Const[i].getInitLevel());
				//

				rhsValue = checkAmount - resPre_Const[i].getInitLevel();
				switch (preType) {
				case 0: // Equal (=)
					// System.out.println(" = " + rhsValue);
					lpSolve.add_constraint(lpIn, v, EQ, rhsValue);
					// Debugging
					// printLPConstraint(v, EQ, rhsValue);
					//
					break;
				case 1: // Smaller (<)
					// System.out.println(" < " + rhsValue);
					lpSolve.add_constraint(lpIn, v, LE, rhsValue - SMALL_M);
					// Debugging
					// printLPConstraint(v, LE, rhsValue - SMALL_M);
					//
					break;
				case 2: // Smaller or equal (<=)
					// System.out.println(" <= " + rhsValue);
					lpSolve.add_constraint(lpIn, v, LE, rhsValue);
					// Debugging
					// printLPConstraint(v, LE, rhsValue);
					//
					break;
				case 3: // Greater (>)
					// System.out.println(" < " + rhsValue);
					lpSolve.add_constraint(lpIn, v, GE, rhsValue + SMALL_M);
					// Debugging
					// printLPConstraint(v, GE, rhsValue + SMALL_M);
					//
					break;
				case 4: // Greater or equal (>=)
					// System.out.println(" >= " + rhsValue);
					lpSolve.add_constraint(lpIn, v, GE, rhsValue);
					// Debugging
					// printLPConstraint(v, GE, rhsValue);
					//
					break;
				default:
					System.out.println("There is no such constraint type.");
				}

				// Reset the constraint
				for (k = 0; k < resPre_Const[i].numChangeAct(); k++) {
					v[tempIpVar[k]] = 0;
				}
			}
		}
	}

	/**
	 * Function to get the duration that separated between A1--p-->A2
	 * (causal-link/mutex)
	 */
	private float getSepDur(int consType, int obj, int a1Index, int a2Index) {
		int i, j, k;
		int addTime1, delTime1, preTime1;
		int addTime2, delTime2, preTime2;

		GAction act1 = (GAction) aPlan.get(a1Index), act2 = (GAction) aPlan.get(a2Index);

		switch (consType) {
		case CAUSAL_LINK:
			addTime1 = act1.getAddTimeEffectObj(obj);
			if (addTime1 < 0) {
				System.out.println("OptimalLP_PP.getSepDur(): Action " + a1Index + " do not add propID = " + obj);
				System.exit(0);
			}

			if (act2.indexPrecond(obj) < 0) {
				System.out.println(
						"OptimalLP_PP.getSepDur(): Action " + a2Index + " do not have precond propID = " + obj);
				System.exit(0);
			}

			tp1 = (addTime1 == 0);
			tp2 = true;
			return (addTime1 * actDurs[a1Index] + DELTA);

		case LOGICAL_MUTEX:
			// First decide if it's add/precond/delete obj of each action (act1, act2)
			preTime1 = act1.getPreTimeObj(obj);
			addTime1 = act1.getAddTimeEffectObj(obj);
			delTime1 = act1.getDeleteTimeEffectObj(obj);

			preTime2 = act2.getPreTimeObj(obj);
			addTime2 = act2.getAddTimeEffectObj(obj);
			delTime2 = act2.getDeleteTimeEffectObj(obj);

			float maxDur = -(actDurs[a2Index] + DELTA);
			float tempDur;
			// Action 1 has pre/add at the end; Action 2 deletes at the start/end
			if ((preTime1 == 1) || (addTime1 == 1)) {
				if (delTime2 == 0) {
					tp1 = false;
					tp2 = true;
					return (actDurs[a1Index] + DELTA);
				}
				if (delTime2 == 1) {
					tempDur = (actDurs[a1Index] - actDurs[a2Index]) + DELTA;
					if (maxDur < tempDur) {
						tp1 = false;
						tp2 = false;
						maxDur = tempDur;
					}
				}
			}

			// Action 1 has pre/add at the start; Action 2 deletes at the start/end
			if ((preTime1 == 0) || (addTime1 == 0)) {
				if (delTime2 == 0) {
					if (maxDur < DELTA) {
						tp1 = true;
						tp2 = true;
						maxDur = DELTA;
					}
				}
				if (delTime2 == 1) {
					tempDur = DELTA - actDurs[a2Index];
					if (maxDur < tempDur) {
						tp1 = true;
						tp2 = false;
						maxDur = tempDur;
					}
				}
			}

			// Action 1 delete at the start; Action 2 has pre/add at start/end
			if (delTime1 == 0) {
				if ((preTime2 == 0) || (addTime2 == 0)) {
					tempDur = DELTA;
					if (maxDur < tempDur) {
						tp1 = true;
						tp2 = true;
						maxDur = tempDur;
					}
				}

				if ((preTime2 == 1) || (addTime2 == 1)) {
					tempDur = DELTA - actDurs[a2Index];
					if (maxDur < tempDur) {
						tp1 = true;
						tp2 = false;
						maxDur = tempDur;
					}
				}
			}

			// Action 1 delete at the end; Action 2 has pre/add at start/end
			if (delTime1 == 1) {
				if ((preTime2 == 0) || (addTime2 == 0)) {
					tp1 = false;
					tp2 = true;
					return actDurs[a1Index] + DELTA;
				}

				if ((preTime2 == 1) || (addTime2 == 1)) {
					tempDur = DELTA + (actDurs[a1Index] - actDurs[a2Index]);
					if (maxDur < tempDur) {
						tp1 = false;
						tp2 = false;
						maxDur = tempDur;
					}
				}
			}

			if (maxDur < -actDurs[a2Index]) {
				System.out.println("OptimalLP_PP.getSepDur(): error in getting maxDur (a1=" + a1Index + ",a2=" + a2Index
						+ ",obj=" + obj + ",cType=" + consType + ",maxDur=" + maxDur + ",-actDur=" + (-actDurs[a2Index])
						+ ")");
				System.exit(0);
			}

			return maxDur;

		case RES_MUTEX:
			if (((act1.indexTest(obj) > -1) && (act2.indexSet(obj) > -1))
					|| ((act1.indexSet(obj) > -1) && (act2.indexTest(obj) > -1)))
				return (actDurs[a1Index] + DELTA);

			System.out.println("OptimalLP_PP.getSepDur(): Error in processing resMutex");
			System.exit(0);

			return 0;
		default:
			System.out.println("OptimalLP_PP.getSepDur(): Unknown type of variable.");
			System.exit(0);
			return 0;
		}
	}

	/**
	 * Some functions to debug the set of constraints and variables added to the
	 * encoding
	 */
	private void printActionDetail() {
		int i, j, pID, rID;
		GAction anAct;

		for (i = 0; i < planSize; i++) {
			anAct = (GAction) aPlan.get(i);
			System.out.println("\nA" + i + ": " + (String) actSigList.get(i));
			System.out.print("Preconds: ");
			for (j = 0; j < anAct.numPrecond(); j++) {
				pID = anAct.getPrecond(j);
				System.out.print("(" + groundInstance.getPredSig(pID) + "," + pID + ") ");
			}

			System.out.print("\nAdds: ");
			for (j = 0; j < anAct.numAdd(); j++) {
				pID = anAct.getAdd(j);
				System.out.print("(" + groundInstance.getPredSig(pID) + "," + pID + ") ");
			}

			System.out.print("\nDeletes: ");
			for (j = 0; j < anAct.numDelete(); j++) {
				pID = anAct.getDelete(j);
				System.out.print("(" + groundInstance.getPredSig(pID) + "," + pID + ") ");
			}

			System.out.print("\nTest's lhs: ");
			for (j = 0; j < anAct.numTest(); j++) {
				rID = anAct.getTest(j).getLeftSide();
				System.out.print("(" + groundInstance.getFuncSig(rID) + "," + rID + ") ");
			}

			System.out.print("\nSet's lhs: ");
			for (j = 0; j < anAct.numSet(); j++) {
				rID = anAct.getSet(j).getLeftSide();
				System.out.print("(" + groundInstance.getFuncSig(rID) + "," + rID + ") ");
			}
			System.out.println();
		}
	}

	private void printIP_VarDetail() {
		int i;
		LP_Var aVar;

		System.out.println("\nLP Variable List: ");
		for (i = 0; i < IP_Vars.size(); i++) {
			aVar = (LP_Var) IP_Vars.get(i);
			System.out.println(i + ". " + "Type: " + aVar.type + " - ObjID: " + aVar.object + " - Act1: " + aVar.act1
					+ " - Act2: " + aVar.act2);
		}
		System.out.println();
	}

	private void printILP_ConstDetail() {
		int i, j;
		MutexCons mc;
		OneSupportCons osc;
		CLProConst clpc;
		OrderTemporalConst otc;

		// Mutex constraints
		System.out.println("\nMutex Constraint List: ");
		for (i = 0; i < Mutex_Consts.size(); i++) {
			mc = (MutexCons) Mutex_Consts.get(i);
			System.out.println(i + ". " + "vID1 = " + mc.vID1 + " | vID2 = " + mc.vID2);
		}

		// One support constraints
		System.out.println("\nOne Support Constraint List: ");
		for (i = 0; i < OneSupport_Consts.size(); i++) {
			osc = (OneSupportCons) OneSupport_Consts.get(i);
			System.out.print(i + ". " + "Size = " + osc.size() + ": (");
			for (j = 0; j < osc.size(); j++) {
				System.out.print(osc.getVar(j) + ",");
			}
			System.out.println(")");
		}

		// Causallink protection constraints
		System.out.println("\nCausallink Protection Constraint List: ");
		for (i = 0; i < CLProtect_Consts.size(); i++) {
			clpc = (CLProConst) CLProtect_Consts.get(i);
			System.out.println(i + ". x = " + clpc.xID + "; y1 = " + clpc.y1ID + "; y2 = " + clpc.y2ID);
		}

		// Order vs. Temporal Constraints
		System.out.println("\nOrder->Temporal constraints (directly related to ILP Variables: ");
		for (i = 0; i < OrderTemp_Consts.size(); i++) {
			otc = (OrderTemporalConst) OrderTemp_Consts.get(i);
			System.out.println("Type: " + otc.type + " ILPVarID: " + otc.xID);
		}

		// Resource related constraint (mostly are resource-condition constraints)
		System.out.println("\nResource condition constraints: ");
		// This one has to be printed out when setting up those constraints.
		// Print them here would be too complicate
	}

	/*
	 * Function to print the LP constraint in its original form
	 */
	private void printLPConstraint(double[] v, int cType, double rhs) {
		int i;

		System.out.print("pLPConst:\t");

		for (i = 1; i <= totalVars; i++) {
			if (v[i] != 0)
				System.out.print("v" + i + "." + v[i] + " + ");
		}

		switch (cType) {
		case LE:
			System.out.print(" <= " + rhs);
			break;
		case EQ:
			System.out.print(" = " + rhs);
			break;
		case GE:
			System.out.print(" >= " + rhs);
			break;
		}
		System.out.println();
	}

	/****** End the list of debugging functions ******/

	/**
	 * Function to fine-tune the MILP solution returned by lp_solve The reason is
	 * that lp_solve return *real* value for *integer* variables so we have to round
	 * those value up to get the actual integer values.
	 */
	private void adjustSolution(double[] lpSol) {
		int i;

		solution.clear();
		solution.add(new Integer(0));
		for (i = 1; i < stIndex; i++) {
			solution.add(new Integer((int) (lpSol[i] + SMALL_ADJ)));
		}
		for (i = stIndex; i < stIndex + numST; i++)
			solution.add(new Float(lpSol[i]));
	}

	/**
	 * Function to interprete the results returned by lp_solve. Also use the
	 * interpreted result to build an instance of the OCPlan class.
	 */
	private float MILP_Interprete(int result) {
		int i, j, ipVarValue, a1ID, a2ID, pID, rID;
		double stVarValue, objFuncValue;
		LP_Var lpVar;

		switch (result) {
		case constant.OPTIMAL:
			// System.out.println("Objective function's value: " +
			// lpSolve.get_objvalue(lpIn));
			// lpSolve.print_solution(lpIn);
			anOCPlan = new OCPlan(planSize, OneSupport_Consts.size(), (int) numLM / 2, (int) numRM / 2);

			adjustSolution(lpSolve.get_solution(lpIn));
			objFuncValue = lpSolve.get_objvalue(lpIn);

			// Print the solution
			System.out.println("\n------------ Optimal O.C plan returned from MILP solver -----------");
			System.out.println("Objective Func. Value: " + objFuncValue + "\n");

			// Causal links
			System.out.println("Causallinks: ");
			for (i = ceIndex; i < ceIndex + numCE; i++) {
				if (((Integer) solution.get(i)).intValue() == 1) {
					lpVar = (LP_Var) IP_Vars.get(i);
					a1ID = lpVar.act1;
					a2ID = lpVar.act2;
					pID = lpVar.object;

					System.out.print("  (v" + i + "=1: A" + a1ID + "-" + pID + "->" + "A" + a2ID + ")");
					anOCPlan.addLogOrder(lpVar.type, a1ID, a2ID, lpVar.a1st, lpVar.a2st, pID, lpVar.sepDur);
				} else {
					// System.out.println("v" + i + " = 0");
				}
			}

			// Logical Mutex
			System.out.println("\n\nLogical Mutex Orderings: ");
			for (i = lmIndex; i < lmIndex + numLM; i++) {
				if (((Integer) solution.get(i)).intValue() == 1) {
					lpVar = (LP_Var) IP_Vars.get(i);
					a1ID = lpVar.act1;
					a2ID = lpVar.act2;
					pID = lpVar.object;

					System.out.print("  (v" + i + "=1: A" + a1ID + "-" + pID + "->" + "A" + a2ID + ")");
					anOCPlan.addLogOrder(lpVar.type, a1ID, a2ID, lpVar.a1st, lpVar.a2st, pID, lpVar.sepDur);
				} else {
					// System.out.println("v" + i + " = 0");
				}
			}

			// Resource Mutex Ordering
			System.out.println("\n\nResource Mutex Orderings: ");
			for (i = rmIndex; i < (rmIndex + numRM); i++) {
				if (((Integer) solution.get(i)).intValue() == 1) {
					lpVar = (LP_Var) IP_Vars.get(i);
					a1ID = lpVar.act1;
					a2ID = lpVar.act2;
					pID = lpVar.object;

					System.out.print("  (v" + i + "=1: A" + a1ID + "-" + pID + "->" + "A" + a2ID + ")");
					anOCPlan.addLogOrder(lpVar.type, a1ID, a2ID, lpVar.a1st, lpVar.a2st, pID, lpVar.sepDur);
				} else {
					// System.out.println("v" + i + " = 0");
				}
			}

			anOCPlan.buildTempOrderings();
			anOCPlan.sort();

			// Starting time variable values
			System.out.println("\nAction's starting times: ");
			for (i = stIndex; i < stIndex + planSize; i++) {
				System.out.print("(v" + i + "," + ((Float) solution.get(i)).floatValue() + ")  ");
			}
			System.out.println("\n-------------------------------------------\n");

			optPlan = buildPlan(OPT_PLAN);

			return (float) objFuncValue;
		case constant.MILP_FAIL:
			System.out.println("OptimalLP_PP: MILP Failed in lpsolve");
			break;
		case constant.INFEASIBLE:
			System.out.println("OptimalLP_PP: MILP Encoding is INFEASIBLE");
			System.out.println("Debugging information:");

			// Printing the debugging information
			// printActionDetail();
			printIP_VarDetail();
			printILP_ConstDetail();
			//

			break;
		case constant.UNBOUNDED:
			System.out.println("OptimalLP_PP: MILP Encoding is UNBOUNDED");
			break;
		default:
			System.out.println("OptimalLP_PP: lpsolve returned error. Error Code = " + result
					+ " check lpsolve.constant class for error code's meaning.");
		}
		return -1;
	}

	/**
	 * Function to print an action in the format required in the competition using
	 * the action signature.
	 */
	private String printAction(int whichPlan, int index) {
		String signature = (String) actSigList.get(index);
		String s = new String();
		int sIndex, oldIndex;

		if (whichPlan == OPT_PLAN)
			s += ((Float) solution.get(stIndex + index)).floatValue() + ": ";
		else
			s += startTimes[index] + ": ";

		sIndex = signature.indexOf('*');
		s += "(" + signature.substring(0, sIndex);
		while (true) {
			oldIndex = sIndex + 1;
			sIndex = signature.indexOf('*', oldIndex);
			if (sIndex < 0)
				break;

			s += " " + signature.substring(oldIndex, sIndex);
		}
		s += ")";
		s += " [" + actDurs[index] + "]\n";

		return s;
	}

	/**
	 * Function to print the optimal plan returned by MILP solver according to the
	 * starting time in the final MILP solution.
	 */
	private String buildPlan(int whichPlan) {
		String aPlan = new String("");
		for (int i = 1; i < planSize - 1; i++) {
			aPlan += printAction(whichPlan, i);
		}

		return new String(aPlan);
	}

	public String getPlan() {
		return new String(optPlan);
	}

	public String getOrigPlan() {
		return new String(origPlan);
	}

	public float getOrigMakespan() {
		float makespan = 0;
		for (int i = 0; i < planSize - 1; i++)
			if (makespan < startTimes[i] + actDurs[i])
				makespan = startTimes[i] + actDurs[i];

		return makespan;
	}

	public OCPlan getOCPlan() {
		return anOCPlan;
	}

	/**
	 * Function to convert to an MILP to find a consistent o.c plan
	 */
	public float SolveMILPEncoding() {
		int numVar, numConst, i;

		// Call function to setup the variables
		setupLPVars();

		// Call function to setup (the rest) of the constraint
		setupLPConsts();

		// Call the function to convert to the lpsolve format
		lpsolveConvert(1);

		// Debugging: Printing the statistical data before solving
		System.out.print("\nOptimalLP_PP's statistic: ");
		System.out.println("CL: " + OneSupport_Consts.size() + "; LogMutex: " + (int) numLM / 2 + "; ResMutex: "
				+ (int) numRM / 2 + "; ResPreCons: " + numResPreConst);
		System.out.print("CausalLink's Stat: ");
		for (i = 0; i < planSize; i++)
			if (clArray[i] > 0)
				System.out.print(clArray[i] + "/" + i + " | ");
		System.out.println();
		// end debugging

		System.out.println(" ** start solving using lp_solve **");
		// Solve the encoding
		int result = lpSolve.solve(lpIn);

		// Interprete back the results from the lp_solve
		return MILP_Interprete(result);
	}
}
