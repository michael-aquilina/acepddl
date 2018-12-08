/****************************************************************
     Author: Minh B. Do - Arizona State University
****************************************************************/
package mt.uol.smi.attempto.aceview.planner.sapa;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import mt.uol.smi.attempto.aceview.planner.sapa.complex_ds.ACEPlannerGState;
import mt.uol.smi.attempto.aceview.planner.sapa.complex_ds.ACEPlannerPredDB;
import mt.uol.smi.attempto.aceview.planner.sapa.complex_ds.ACEPlannerStateInfo;
import mt.uol.smi.attempto.aceview.planner.sapa.complex_ds.ACEPlannerStateManager;
import mt.uol.smi.attempto.aceview.planner.sapa.parsing.ACEPlannerDomain;
import mt.uol.smi.attempto.aceview.planner.sapa.parsing.ACEPlannerGrounding;
import mt.uol.smi.attempto.aceview.planner.sapa.parsing.ACEPlannerParser;
import mt.uol.smi.attempto.aceview.planner.sapa.parsing.ACEPlannerProblem;
import mt.uol.smi.attempto.aceview.planner.sapa.rmtpg.ACEPlannerRMTPG;
import mt.uol.smi.attempto.aceview.planner.ACEPlannerManager;
import mt.uol.smi.attempto.aceview.planner.IACEPlannerManager;
import mt.uol.smi.attempto.aceview.planner.IACEPlannerRunner;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerActionListModel;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerActionModel;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerSatisfiedActionModel;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerSolutionModel;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerSolutionState;

/**
 * Planner.java: The main class to actually do the search will use the main DS
 * and utility functions defined in other classes.
 */
public class ACEPlannerPlanner implements IPlanner {

	private static final Logger logger = Logger.getLogger(ACEPlannerPlanner.class);

	/***** Main datastructures *****/
	// static SapaFrame sf; //GUI root frame
	ACEPlannerStateManager stateMan; // All information related to Predicates & Resource
	ACEPlannerActionListModel gActions; // Equivalent with "groundActions", but in "GAction" class
	ACEPlannerGrounding groundInstance;
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

	public ACEPlannerPlanner() {
		groundInstance = new ACEPlannerGrounding();
		exploredStateLimit = 30;
	}

	String outfileName = new String(""); // Filename to output the solution
	// Strings/classes store the original p.c. plans and the greedily/optimally
	// post-processed
	String origPlan = new String(), gppPlan = new String(), oppPlan = new String();

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
	private ACEPlannerSolutionModel solutionSearching() {
		exploredState = 0;
		exploredStateLimit = 30;
		// check if initial state satisfies the goal
		logger.debug("Checking initial state for goal...");
		ACEPlannerStateInfo tempSP = stateMan.getStateInfo();
		boolean goalMet = stateMan.isGoalMet(tempSP);
		if (goalMet) {
			ACEPlannerSolutionModel solution = new ACEPlannerSolutionModel();
			solution.setSolutionState(ACEPlannerSolutionState.INITIAL_STATE_SATISFIES_GOAL);
			logger.info("Solution found in intial state");
			return solution;
		}
		stateMan.resetStateQueue(tempSP);

		while (true) {
			sp = stateMan.getStateInfo();
			exploredState++;

			if (sp == null) {
				logger.info("No solution!!!\n");
				logger.debug(
						"<p>States generated: " + generatedState + " <p>States explored:\t" + exploredState + "\n");
				ACEPlannerSolutionModel solution = new ACEPlannerSolutionModel();
				solution.setSolutionState(ACEPlannerSolutionState.NOT_FOUND);
				return solution;
			}

			logger.debug("Getting applicable actions");
			// we get all applicable actions with their effects with individuals
			List<ACEPlannerSatisfiedActionModel> applicableActions = rmtpg.applicableActions(sp, gActions.getList());

			/* Try to find all applicable actions to this state */
			for (ACEPlannerSatisfiedActionModel act : applicableActions) {

				tempSP = new ACEPlannerStateInfo(sp);
				tempSP.update(act);

				goalMet = stateMan.isGoalMet(tempSP);

				if (goalMet) { // Solution found
					aDate = new Date();
					time2 = aDate.getTime();
					logger.info("Solution found!");
					logger.info("Search time " + (time2 - time1) + " milisecs");
					logger.info("<p>Planning Time:\t         " + (time2 - time1) + " ms\n");
					time1 = time2;
					logger.debug(stringOut);
					ACEPlannerSolutionModel solution = new ACEPlannerSolutionModel();
					solution.setSolutionState(ACEPlannerSolutionState.FOUND);
					int step = 1;
					for (ACEPlannerSatisfiedActionModel a : tempSP.getActions()) {
						a.setOrder(step);
						solution.getSatisfiedActions().add(a);
						step++;
					}
					return solution;
				}

				stateMan.addStateInfo(tempSP);
			} // end for loop for finding all applicable actions
			if (exploredState > exploredStateLimit) {
				logger.info(
						"The limit of " + exploredStateLimit + " cycles has been reached without finding a solution!");
				ACEPlannerSolutionModel solution = new ACEPlannerSolutionModel();
				solution.setSolutionState(ACEPlannerSolutionState.LIMIT_REACHED);
				return solution;
			}
		}
	}

	/**
	 * Main routine to solve the problem
	 */
	public ACEPlannerSolutionModel solve(IACEPlannerRunner runner) {
		logger.debug("Called to solve.");

		// resetting all ace text
		ACEPlannerGState.setAllAcetext(new ACEPlannerPredDB());

		ACEPlannerParser parser = new ACEPlannerParser(runner.getDomain());
		ACEPlannerDomain domain = parser.parse_domain_pddl();
		ACEPlannerProblem prob = parser.parse_problem_pddl();
		stateMan = groundInstance.getStateManager(domain, prob, runner.getModelManager(), runner.getDataFactory(),
				runner.getAceLexicon());

		gActions = runner.getDomain().getActionListModel();

		// Initialize the RTPG class
		rmtpg.initGoals(prob.getGoalState());
		/*
		 * Start searching
		 */
		sp = new ACEPlannerStateInfo(runner.getModelManager(), runner.getDataFactory(), runner.getAceLexicon());
		sp.setCurrentState(stateMan.getInitState());

		initState = new ACEPlannerStateInfo(sp);
		stateMan.addStateInfo(sp);

		ACEPlannerSolutionModel solution;

		logger.debug("Starting to search...");
		/* Start taking the best *StateInfo* from stateMan and try to extend it */
		solution = solutionSearching();
		logger.debug("Returning results.");
		return solution;
	}
}
