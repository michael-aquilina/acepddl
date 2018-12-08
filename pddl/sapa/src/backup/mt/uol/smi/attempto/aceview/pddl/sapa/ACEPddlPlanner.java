/****************************************************************
     Author: Minh B. Do - Arizona State University
****************************************************************/
package mt.uol.smi.attempto.aceview.pddl.sapa;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import ch.uzh.ifi.attempto.aceview.ACEText;
import mt.uol.smi.attempto.aceview.pddl.IACEPlannerRunner;
import mt.uol.smi.attempto.aceview.pddl.model.ACEPlannerActionModel;
import mt.uol.smi.attempto.aceview.pddl.sapa.basic_ds.Event;
import mt.uol.smi.attempto.aceview.pddl.sapa.basic_ds.GAction;
import mt.uol.smi.attempto.aceview.pddl.sapa.complex_ds.ACEPlannerStateInfo;
import mt.uol.smi.attempto.aceview.pddl.sapa.complex_ds.ACEPlannerStateManager;
import mt.uol.smi.attempto.aceview.pddl.sapa.complex_ds.GMResDB;
import mt.uol.smi.attempto.aceview.pddl.sapa.complex_ds.OCPlan;
import mt.uol.smi.attempto.aceview.pddl.sapa.complex_ds.StateInfo;
import mt.uol.smi.attempto.aceview.pddl.sapa.parsing.ACEPlannerDomain;
import mt.uol.smi.attempto.aceview.pddl.sapa.parsing.ACEPlannerGrounding;
import mt.uol.smi.attempto.aceview.pddl.sapa.parsing.ACEPlannerProblem;
import mt.uol.smi.attempto.aceview.pddl.sapa.rmtpg.ACEPlannerRMTPG;
import mt.uol.smi.attempto.aceview.pddl.sapa.utils.Utility;

/**
 * Planner.java: The main class to actually do the search will use the main DS
 * and utility functions defined in other classes.
 */
public class ACEPlannerPlanner implements IPlanner {
	/***** Main datastructures *****/
	// static SapaFrame sf; //GUI root frame
	ACEPlannerStateManager stateMan; // All information related to Predicates & Resource
	ArrayList gActions; // Equivalent with "groundActions", but in "GAction" class
	ACEPlannerGrounding groundInstance;
	// This one will actually be used through the search. groundActions are kind
	// of "intermediate" parsing class. May need to be eliminated later.
	GMResDB mresDB, initMResDB;
	Utility util = new Utility();
	ACEPlannerRMTPG rmtpg = new ACEPlannerRMTPG();

	/*****
	 * List of running options (setting up the cost function and heuristics)
	 *****/
	int costPropOption = 1; // Currently: 0 - max; 1 - sum; 2 - combo;
	boolean relaxedPlanOption = true; // F-no relaxed plan; T-extract relaxed plan;
	int goalCostOption = 1; // In case no relaxed plan, how to aggregate the
	// goal costs: 0 - max; 1 - sum; 2 - combo

	int lookaheadOption = -1; // Default: -1-lookahead (-1 = infinite lookahead)
	boolean haFlag = false; // Helpful action option
	boolean haneFlag = false; // Using the negative effects of the helpful actions also.
	boolean autoFlag = true, qualityFlag = false;
	float bestHeuValue, heuUpperLimit = (float) 100000.00, deltaHeu, bestMakespan;
	int exploredStateLimit;
	boolean postProcessFlag = false;

	static boolean text_mode = true; // Run the GUI
	boolean debug_mode = false; // Want to print the debug information or not
	boolean res_adj = true; // If we want to do resource adjustment
	boolean deadlineFlag = false; // If the goals have deadline or not.
	// NOTE: TURN deadlineFlag TO *TRUE* WHEN PDDL2.1 IS EXTENDED TO HANDLE
	// DEADLINES
	public boolean dupaFlag = false; // Do we allow the two parameter of the same type in
	// one action to map to the same object or not.

	int cutoff = 7200;
	int freq = 200;

	ArrayList haNEffects = new ArrayList();
	ArrayList haActs;

	String outfileName = new String(""); // Filename to output the solution
	// Strings/classes store the original p.c. plans and the greedily/optimally
	// post-processed
	String origPlan = new String(), gppPlan = new String(), oppPlan = new String();
	OCPlan gppOCPlan, oppOCPlan;

	static String stringOut; // Used for the GUI

	/**************************************************************************/
	// Weight for calculating the heuristic values
	int G_WEIGHT = 1, H_WEIGHT = 5;
	static long timeX;

	/** Some data structure shared in the next two functions **/
	boolean checkBestHeu;
	ACEPlannerStateInfo sp, initState;
	float heuristicValue;
	int aID, propResult, generatedState = 1, exploredState = 0;
	long time1, time2;
	Date aDate;

	/*** Function to generate the search tree until the solution node is found ***/
	private int solutionSearching() {
		ACEPlannerStateInfo tempSP;
		while (true) {
			sp = stateMan.getStateInfo();
			exploredState++;

			if (sp == null) {
				System.out.println(";;No solution (1)!!!");
				stringOut += "No solution (1)!!!\n";
				System.out.println(";;State generated: " + generatedState + "     .State explored: " + exploredState);
				stringOut += "<p>States generated: " + generatedState + " <p>States explored:\t" + exploredState + "\n";
				return 0;
			} else {
				boolean goalMet = stateMan.isGoalMet(sp);
				if (goalMet) {
					return 1;
				}
			}

			List<ACEPlannerActionModel> applicableActions = rmtpg.applicableActions(sp, gActions);

			/* Try to find all applicable actions to this state */
			for (ACEPlannerActionModel act : applicableActions) {

				tempSP = new ACEPlannerStateInfo(sp);
				tempSP.update(act);

				boolean goalMet = stateMan.isGoalMet(tempSP);

				if (goalMet) { // Solution found
					aDate = new Date();
					time2 = aDate.getTime();
					System.out.println(";; Search time " + (time2 - time1) + " milisecs");
					stringOut += "<p>Planning Time:\t         " + (time2 - time1) + " ms\n";
					time1 = time2;

					return 1;
				}

				stateMan.addStateInfo(tempSP);
			} // end for loop for finding all applicable actions
			if (exploredState > exploredStateLimit) {
				return 2;
			}
		}
	}


	/**
	 * Main routine to solve the problem
	 */
	public int solve(ACEPlannerDomain domain, ACEPlannerProblem prob, IACEPlannerRunner runner) {
		stateMan = groundInstance.getStateManager(domain, prob);

		// Initialize the RTPG class
		rmtpg.initGoals(prob.getGoalState());

		/*
		 * Start searching
		 */
		sp = new ACEPlannerStateInfo(0);
		sp.setCurrentState(stateMan.getInitState());

		initState = new ACEPlannerStateInfo(sp);
		stateMan.addStateInfo(sp);
		
		int solutionFlag;
		/* Start taking the best *StateInfo* from stateMan and try to extend it */
		solutionFlag = solutionSearching();

		return solutionFlag;
	}
}
