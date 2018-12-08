/* RMTGPTest.java
 * Created on May 14, 2004
 */
package mt.uol.smi.attempto.aceview.pddl.sapa;

// Java packages
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;

import mt.uol.smi.attempto.aceview.pddl.ACEPlannerRunner;
import mt.uol.smi.attempto.aceview.pddl.sapa.basic_ds.GAction;
import mt.uol.smi.attempto.aceview.pddl.sapa.complex_ds.GMResDB;
import mt.uol.smi.attempto.aceview.pddl.sapa.complex_ds.StateInfo;
import mt.uol.smi.attempto.aceview.pddl.sapa.complex_ds.StateManager;
import mt.uol.smi.attempto.aceview.pddl.sapa.parsing.ACEPDDLParser;
import mt.uol.smi.attempto.aceview.pddl.sapa.parsing.Domain;
import mt.uol.smi.attempto.aceview.pddl.sapa.parsing.Grounding;
import mt.uol.smi.attempto.aceview.pddl.sapa.parsing.PDDL21Parser;
import mt.uol.smi.attempto.aceview.pddl.sapa.parsing.ParseException;
import mt.uol.smi.attempto.aceview.pddl.sapa.parsing.Problem;
import mt.uol.smi.attempto.aceview.pddl.sapa.rmtpg.RMTPG;
import mt.uol.smi.attempto.aceview.pddl.sapa.utils.Utility;

/**
 * TODO create javadoc for RMTGPTest.java.
 * 
 * @author bentonj
 */
public class RMTGPTest {

	static final private boolean debug_mode = true;
	static private Domain domain;
	static private Problem prob;
	static private StateManager stateMan;

	public static void readFiles(String domainFile, String problemFile) {
		PDDL21Parser parser21 = new PDDL21Parser(System.in);
		boolean lineArgs = false;

		FileInputStream pddl_file;

		/*
		 * Instantiate a parser
		 */
		Date d = new Date();
		long timeX = d.getTime(); // Get the starting time of the program

		/*** Parse the Domain specification file ****/
		try {
			pddl_file = new java.io.FileInputStream(domainFile);
			PDDL21Parser.ReInit(pddl_file);
		} catch (java.io.FileNotFoundException e) {
			System.out.println("Domain file " + domainFile + " not found !!!");
			return;
		}

		try {
			domain = PDDL21Parser.parse_domain_pddl();
			System.out.println(";;Domain file succesfully read !!" + " num actions = " + domain.numAction());
		} catch (mt.uol.smi.attempto.aceview.pddl.sapa.parsing.ParseException e) {
			System.out.println("Exception while parsing domain file!!");
			e.printStackTrace();
			return;
		}

		/**** Parse the problem file ****/
		try {
			pddl_file = new java.io.FileInputStream(problemFile);
			PDDL21Parser.ReInit(pddl_file);
		} catch (java.io.FileNotFoundException e) {
			System.out.println("Problem file " + problemFile + " not found !!!");
			return;
		}

		try {
			prob = PDDL21Parser.parse_problem_pddl();
			System.out.println(";;Problem file succesfully read !!");
		} catch (ParseException e) {
			System.out.println("Exception while parsing problem file!!");
			e.printStackTrace();
			return;
		}
	}

	public static RMTPG RMTPG(Domain domain, Problem prob) {
		int i, j, k;

		/* Call some initialization functions */
		boolean deadlineFlag = prob.goalDeadlineFlag();
		boolean dupaFlag = false;
		domain.ppTypeTree();
		prob.absorbConstants(domain.getConstantMap());

		/* Initializing the Utility instance */
		prob.makeTypeMap(domain);
		Grounding groundInstance = new Grounding(dupaFlag);
		Utility util = new Utility();
		domain.setLhs(util.getLHS(domain));
		stateMan = groundInstance.getStateManager(domain, prob);

		GMResDB mresDB, initMResDB;

		// Get the initial mresDB in the problem file
		mresDB = groundInstance.getMResDB();
		stateMan.setInitMResDB(mresDB);
		initMResDB = new GMResDB(mresDB);

		// Find all the ID of function appear in the lhs of mySet
		// those are only function that can possibly be change value
		ArrayList lhs = new ArrayList();
		for (i = 0; i < mresDB.numFunction(); i++) {
			if (domain.inLhs(groundInstance.getFuncName(i)))
				lhs.add(new Integer(i));
		}
		util.setLhs(lhs);

		ArrayList gActions;
		gActions = groundInstance.getGroundActions(stateMan, mresDB);

		// Printing for DEBUGGING purpose
		if (debug_mode == true) {
			/* For mresDB */
			System.out.println("MResDB's size: " + mresDB.numFunction());
			System.out.println(mresDB.toString());

			/* For a set of Grounded Actions (Abstract form & Integer-converted form) */
			System.out.println("\n<<< Ground Actions >>>");
			for (i = 0; i < gActions.size(); i++) {
				System.out.println(i + ". " + groundInstance.getActSig(i));
				System.out.println((GAction) gActions.get(i));
			}
		}

		System.out.println(";;Function: " + mresDB.numFunction() + ". Predicate: " + groundInstance.numPred() + ". Grounded Action: " + gActions.size());
		// ........... END DEBUGGING .............

		/*
		 * Finish initialization. Do some pre-processing.
		 */
		Date aDate = new Date();
		long time1 = aDate.getTime();

		// System.out.println(";;Parsing & grounding: " + (time1-timeX) + "
		// millisecs.");

		System.out.println("\n;;<<< Start Searching for Solution >>>");

		// Pass the Goals & Ground Actions information to the Utility class
		util.initialize(gActions, stateMan.getGoalState().getPredDB().getAllPred(), stateMan.getGoalState().getPredDB().getAllTimeMap());

		// Preprocessing the maximum value for achieving each resource
		util.maxResPreprocess(stateMan.getInitState().getMResDB());

		// Set the right helpful action flag
		/*
		 * if( autoFlag ) { haFlag = true; haneFlag = true; }
		 */
		boolean haFlag = false;
		boolean haneFlag = false;
		// Initialize the RMTPG class
		RMTPG rmtpg = new RMTPG();
		rmtpg.initGoals(util.getSortedGoals(), util.getGoalTime());
		int costPropOption = 1;
		boolean relaxedPlanOption = true;
		int goalCostOption = 1;
		int lookaheadOption = -1;
		rmtpg.optionSetting(deadlineFlag, costPropOption, relaxedPlanOption, goalCostOption, haFlag, haneFlag, lookaheadOption);
		rmtpg.buildBiLevelGraph(groundInstance.numPred(), gActions);

		return rmtpg;
	}

	public static void fileBased(String[] args) {
		String separator = System.getProperty("file.separator");
		String domainDir = "driverlog";
		String domainFile = "driverlogTimed.pddl";
		String problemFile = "pfile12";
		String problemQualified = "domains" + separator + domainDir + separator + problemFile;
		String domainQualified = "domains" + separator + domainDir + separator + domainFile;
		readFiles(domainQualified, problemQualified);
		// setup RMTPG
		RMTPG rmtpg = RMTPG(domain, prob);
		StateInfo sp = new StateInfo(prob.numInitFunct());
		sp.setCurrentState(stateMan.getInitState());
		sp.setCSTime(0);

		// Calculate the heuristic value for the INITIAL state.
		int propResult = rmtpg.costPropagation(stateMan.getInitState().getPredDB(), sp.getAllEvents(), 0, new ArrayList(), new ArrayList());

		System.out.println("prop Result:" + propResult);
		if (propResult < 0) {
			System.out.println(";;No solution at initial state !!");
			stateMan.clearStateQueue();
			System.exit(-1);
		}

		if (propResult == 0) {
			System.out.println(";;Goals satisfied in the initial state");
			stateMan.clearStateQueue();
			System.exit(-1);
		}
	}

	public static void main(String[] args) {
		// setup RMTPG
		RMTPG rmtpg = RMTPG(domain, prob);
		StateInfo sp = new StateInfo(prob.numInitFunct());
		sp.setCurrentState(stateMan.getInitState());
		sp.setCSTime(0);

		// Calculate the heuristic value for the INITIAL state.
		int propResult = rmtpg.costPropagation(stateMan.getInitState().getPredDB(), sp.getAllEvents(), 0, new ArrayList(), new ArrayList());

		System.out.println("prop Result:" + propResult);
		if (propResult < 0) {
			System.out.println(";;No solution at initial state !!");
			stateMan.clearStateQueue();
			System.exit(-1);
		}

		if (propResult == 0) {
			System.out.println(";;Goals satisfied in the initial state");
			stateMan.clearStateQueue();
			System.exit(-1);
		}
	}
}
