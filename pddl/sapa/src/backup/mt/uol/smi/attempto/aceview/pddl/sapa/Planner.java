/****************************************************************
     Author: Minh B. Do - Arizona State University
****************************************************************/
package mt.uol.smi.attempto.aceview.pddl.sapa;

import mt.uol.smi.attempto.aceview.pddl.sapa.basic_ds.*;
import mt.uol.smi.attempto.aceview.pddl.sapa.complex_ds.*;
import mt.uol.smi.attempto.aceview.pddl.sapa.lp_interface.OptimalLP_PP;
import mt.uol.smi.attempto.aceview.pddl.sapa.parsing.*;
import mt.uol.smi.attempto.aceview.pddl.sapa.rmtpg.*;
import mt.uol.smi.attempto.aceview.pddl.sapa.utils.*;

import java.io.*;
import java.util.*;

/**
 * Planner.java: The main class to actually do the search will use the main DS
 * and utility functions defined in other classes.
 */
public class Planner {
	/***** Main datastructures *****/
	// static SapaFrame sf; //GUI root frame
	StateManager stateMan; // All information related to Predicates & Resource
	ArrayList gActions; // Equivalent with "groundActions", but in "GAction" class
	// This one will actually be used through the search. groundActions are kind
	// of "intermediate" parsing class. May need to be eliminated later.
	Grounding groundInstance;
	GMResDB mresDB, initMResDB;
	Utility util = new Utility();
	RMTPG rmtpg = new RMTPG();

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

	/**
	 * Utility functions to create the dummy actions representing the initial state
	 * (effects as facts in initstate) and goal state (preconds as goals)
	 */
	private GAction getInitAct() {
		GPredDB initState = stateMan.getInitState().getPredDB();
		GAction initAct = new GAction(0, 0, 0, initState.numPred(), 0);

		for (int i = 0; i < initState.numPred(); i++) {
			initAct.putAdd(initState.getPred(i).intValue(), i);
			initAct.putAddTimeEffect(0, i);
		}
		initAct.setDType(true);
		initAct.setDStatic(0);

		return initAct;
	}

	private GAction getGoalAct(ArrayList goals) {
		GAction goalAct = new GAction(goals.size(), 0, 0, 0, 0);

		for (int i = 0; i < goals.size(); i++) {
			goalAct.addPrecond(((Integer) goals.get(i)).intValue(), i);
			goalAct.putPreTime(0, i);
		}

		goalAct.setDType(true);
		goalAct.setDStatic(0);

		return goalAct;
	}

	/**
	 * Function to output the (best) plan to the file with name specified in the
	 * command line Option: 1 - original plan; 2 - greedy pp plan; 3 - optimal pp
	 * plan
	 */
	private void outputPlanToFile(int option, String finalPlan) {
		// Output the final plan to the file if specified
		String fileName = new String();
		switch (option) {
		case 1:
			System.out.println(";;-----------Original plan returned by Sapa-------------");
			System.out.print(finalPlan);
			System.out.println(";;-----------End original plan--------------------------\n");

			fileName = new String(outfileName + ".org");
			break;
		case 2:
			System.out.println(";;----------Greedily Post-processed plan-----------------");
			System.out.println(finalPlan);
			System.out.println(";;-----------End greedily post-processed plan------------");

			fileName = new String(outfileName + ".gpp");
			break;
		case 3:
			System.out.println(";;----------Optimally Post-processed plan-----------------");
			System.out.println(finalPlan);
			System.out.println(";;-----------End optimally post-processed plan------------");

			fileName = new String(outfileName + ".opp");
			break;
		default:
			System.out.println("Planner.outputPlanToFile(): There is no such option.");
			System.exit(0);
		}

		// Do not output the original plan to file now, may change latter
		if ((outfileName.length() == 0) || (option == 1))
			return;

		try {
			FileWriter out = new FileWriter(fileName, true);
			out.write(finalPlan);
			out.flush();
			out.close();
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	/**
	 * Function to print the solution
	 */
	private void processSolution(ArrayList actions, ArrayList times, ArrayList durs, float totalCost) {
		float time = 0, dur = 0, makespan = 0, totalDur = 0;
		ArrayList actSigList = new ArrayList(), gActs = new ArrayList();
		int actId;

		// Prepare the set of actions in the plan for post-processing to find POP plan
		for (int i = 0; i < actions.size(); i++) {
			actId = ((Integer) actions.get(i)).intValue();

			actSigList.add(groundInstance.getActSig(actId));
			gActs.add(gActions.get(((Integer) actions.get(i)).intValue()));

			totalDur += ((Float) durs.get(i)).floatValue();
		}

		/*
		 * Start newly added post-processing part
		 */
		GreedyPost greedyPostProObj = new GreedyPost();
		OptimalLP_PP optimalPostProObj = new OptimalLP_PP();

		// Create one dummy action representing the initial state
		GAction initAct = getInitAct();

		gActs.add(0, initAct);
		times.add(0, new Float(0));
		durs.add(0, new Float(0));
		actSigList.add(0, "Init*Action");

		// Create one dummy action representing the goal state
		GAction goalAct = getGoalAct(util.getSortedGoals());

		gActs.add(goalAct);
		durs.add(new Float(0));
		times.add(new Float(totalDur + 0.01));
		actSigList.add("Goal*Action");

		Date aDate = new Date();
		long time3 = aDate.getTime();

		greedyPostProObj.initialize(gActs, actSigList, times, durs, totalDur, bestMakespan);
		optimalPostProObj.initialize(gActs, actSigList, times, durs, bestMakespan, 1, util.getLhsResource(), initMResDB,
				groundInstance);
		origPlan = optimalPostProObj.getOrigPlan();
		if (!postProcessFlag)
			outputPlanToFile(1, origPlan);

		// Testing: Temporarily ignore the post-processing step now. Should remove the
		// following line later
		if (postProcessFlag) {
			makespan = greedyPostProObj.buildOrdering();
			// greedyPostProObj.removeRedundantMutex();
			// The "if" condition is for running different configurations consecutively
			// trying to find better quality plans. NEED TO DO THE SAME FOR
			// the *OptimalProcessing* part.
			if ((bestMakespan < 0) || (bestMakespan > makespan)) {
				bestMakespan = makespan;
				gppPlan = greedyPostProObj.getPlan();
				// outputPlanToFile(2, gppPlan);
			}

			aDate = new Date();
			System.out.println(";; Partialization time: " + (aDate.getTime() - time3) + " milisecs");

			// START GUI (need to change to accommodate the OptimalPostProcessing also)
			stringOut += greedyPostProObj.output;
			if (!text_mode) {
				// sf.showProgressWindow(false);
				// sf.showOutput(greedyPostProObj, groundInstance, stringOut);
			}
			// END GUI

			// ---->>> Testing *optimal* post-processing
			// makespan = optimalPostProObj.SolveMILPEncoding();
			// oppPlan = optimalPostProObj.getPlan();
			// outputPlanToFile(3,oppPlan);
			// end testing

			// ---->>> Testing the OCPlan
			// gppOCPlan = greedyPostProObj.getOCPlan();
			// oppOCPlan = optimalPostProObj.getOCPlan();

			/*
			 * System.out.
			 * println("\n----------------- OCPlan after GREEDY post-processed ---------");
			 * System.out.println(gppOCPlan.logOrdersToString());
			 * System.out.println("\nGreedily Post-Processed STN:\n" +
			 * gppOCPlan.stnToString());
			 * System.out.println("\nGreedily Post-Processed's EST:\n" +
			 * gppOCPlan.estToString());
			 * 
			 * System.out.
			 * println("\n----------------- OCPlan after OPTIMAL post-processed ---------");
			 * System.out.println(oppOCPlan.logOrdersToString());
			 * System.out.println("\nOptimally Post-Processed STN:\n" +
			 * oppOCPlan.stnToString());
			 * System.out.println("\nOptimally Post-Processed's EST:\n" +
			 * oppOCPlan.estToString());
			 * 
			 * System.out.
			 * println("\n-------- Comparing two OCPlans's Logical Order Set ------------");
			 * if(gppOCPlan.subsummedBy(oppOCPlan))
			 * System.out.println("-->gppOCPlan is subsummed by oppOCPlan"); else
			 * System.out.println("-->gppOCPlan is NOT subsummed by oppOCPlan");
			 * 
			 * if(oppOCPlan.subsummedBy(gppOCPlan))
			 * System.out.println("-->oppOCPlan is subsummed by gppOCPlan"); else
			 * System.out.println("-->oppOCPlan is NOT subsummed by gppOCPlan");
			 * 
			 * System.out.
			 * println("\n------ Compare two OCPlans's Action Starting times -----------");
			 * gppOCPlan.equalsEST(oppOCPlan);
			 * 
			 * System.out.
			 * println("\n<< Output two OCPlan's equivalent p.c plans to files >>");
			 * outputPlanToFile(2, gppOCPlan.pcPlanToString(actSigList, durs));
			 * outputPlanToFile(3, oppOCPlan.pcPlanToString(actSigList, durs));
			 */
			/*
			 * System.out.println("<< OrigMS = " + optimalPostProObj.getOrigMakespan() +
			 * " | GppMS = " + gppOCPlan.getMakespan() + " | OppMS = " +
			 * oppOCPlan.getMakespan() + " >>");
			 */
			// end testing
		}
	}

	/** Some data structure shared in the next two functions **/
	boolean checkBestHeu;
	StateInfo sp, initState;
	float heuristicValue;
	int aID, propResult, generatedState = 1, exploredState = 0;
	long time1, time2;
	Date aDate;

	/*** Function to generate the search tree until the solution node is found ***/
	private int solutionSearching() {
		Integer aPred;
		GAction act;
		int i, j, k;
		StateInfo tempSP;
		float resadjValue;

		// USED FOR TESTING
		ArrayList actionsForThisState = new ArrayList(100);
		// USED FOR STATISTICS GATHERING
		ArrayList prevCompareList = new ArrayList(100);

		while (true) {
			sp = stateMan.getStateInfo();

			// Change to another option if it's not within deltaHeu of the best state so far
			if (checkBestHeu && (exploredState > exploredStateLimit)) {
				heuristicValue = sp.getDistance();
				if (heuristicValue > bestHeuValue + deltaHeu) {
					System.out.println(";; Heu = " + heuristicValue + " BestHeu = " + bestHeuValue
							+ "  Generated State = " + generatedState + "  Explored State = " + exploredState);
					break;
				}
			}

			exploredState++;

			if (sp == null) {
				System.out.println(";;No solution (1)!!!");
				stringOut += "No solution (1)!!!\n";
				System.out.println(";;State generated: " + generatedState + "     .State explored: " + exploredState);
				stringOut += "<p>States generated: " + generatedState + " <p>States explored:\t" + exploredState + "\n";
				// return 0;
				break;
			}

			actionsForThisState.clear();

			GAction[] applicableActions = rmtpg.applicableActions(sp, gActions);

			int applicableActionsLength = applicableActions.length;

			/* Try to find all applicable actions to this state */
			for (i = 0; i < applicableActionsLength; i++) {
				act = applicableActions[i];

				// If we decide to use the helpful action option
				if (haFlag) {
					if (!sp.haQualified(act, haneFlag))
						continue;
				}

				tempSP = new StateInfo(sp);
				tempSP.update(act);

				propResult = rmtpg.costPropagation(tempSP.getCurrentPredDB(), tempSP.getAllEvents(), tempSP.getCSTime(),
						tempSP.getAllPConds(), tempSP.getAllPCTime());

				// BM: Added July 23, 2003. To handle "at end" condition (to check dynamic
				// goals)
				for (j = 0; j < tempSP.numPCond(); j++) {
					if (!rmtpg.achievable(tempSP.getPCond(j), tempSP.getPCTime(j))) {
						propResult = -1;
						break;
					}
				}

				// Discard state that violates the goal-deadline time or goals are not
				// achievable.
				if (propResult < 0)
					continue;

				if (propResult > 0) { // Propagation successed, now get the real value
					heuristicValue = rmtpg.getHeuristicValue();

					if (heuristicValue < 0) { // Debug purpose
						System.out.println("*****Something wrong, heuristic value < 0, propResult > 0*****");
						stringOut += "*****Something wrong, heuristic value < 0, propResult > 0*****\n";
						return 0;
					}

					if (heuristicValue == 0) { // Solution found
						aDate = new Date();
						time2 = aDate.getTime();
						System.out.println(";; Search time " + (time2 - time1) + " milisecs");
						stringOut += "<p>Planning Time:\t         " + (time2 - time1) + " ms\n";
						time1 = time2;
						System.out.println(
								";; State generated: " + generatedState + "     State explored: " + exploredState);
						stringOut += "<p>States generated:      " + generatedState + "\n";
						stringOut += "<p>States explored:         " + exploredState + "\n";
						processSolution(tempSP.getActions(), tempSP.getTime(), tempSP.getADur(), tempSP.gValue());

						return 1;
					}

					// Call function to find the adjustment of the heuristic value
					if (res_adj && relaxedPlanOption) {
						resadjValue = util.resourceAdjustment(rmtpg.getRelaxedPlan(), rmtpg.getRPSize(),
								tempSP.getCurrentState().getMResDB(), false);
						heuristicValue += resadjValue;
					}

					heuristicValue = G_WEIGHT * tempSP.gValue() + H_WEIGHT * heuristicValue;
					tempSP.setDistance(heuristicValue);

					// If the *auto* option is on. Need to keep track of best heuristic we have so
					// far.
					if ((checkBestHeu == true) && (heuristicValue < bestHeuValue))
						bestHeuValue = heuristicValue;

					// If we decide to use the helpful action option
					if (haFlag) {
						if (!haneFlag) {
							tempSP.setHaEffects(rmtpg.collectHAPEffects(), rmtpg.numHAPEffects(), null);
						} else {
							Integer ne;
							haActs = rmtpg.getHActs();
							haNEffects.clear();
							// IMPROVE Remove cast
							for (k = 0; k < haActs.size(); k++) {
								aID = ((Integer) haActs.get(k)).intValue();

								for (j = 0; j < ((GAction) gActions.get(aID)).numDelete(); j++) {
									ne = new Integer(((GAction) gActions.get(aID)).getDelete(j));

									if (!haNEffects.contains(ne))
										haNEffects.add(ne);
								}
							}

							tempSP.setHaEffects(rmtpg.collectHAPEffects(), rmtpg.numHAPEffects(), haNEffects);
						}
					}

					stateMan.addStateInfo(tempSP);
					generatedState++;

					// Check for time-out termination every FREQ generated nodes
					if ((generatedState % freq) == 0) {
						time2 = (new Date()).getTime();
						if ((time2 - time1) > 1000 * cutoff) {
							System.out.println("****Exit after TIME-OUT = " + cutoff + " seconds****\n");
							stringOut += "****Exit after TIME-OUT = " + cutoff + " seconds****\n\n";

							return 0;
						}
					}
				} else { // Propagation indicate that the goals satisfied in init-state
					aDate = new Date();
					time2 = aDate.getTime();
					System.out.println("\nTime " + (time2 - time1) + " milisecs.");
					stringOut += "<p>Time " + (time2 - time1) + " milisecs.\n";
					System.out.println(";;State generated: " + generatedState);
					stringOut += "<p>State generated: " + generatedState + "\n";
					System.out.println(";;State explored: " + exploredState);
					stringOut += "<p>State explored: " + exploredState + "\n";
					processSolution(tempSP.getActions(), tempSP.getTime(), tempSP.getADur(), 0);

					return 1;
				}
			} // end for loop for finding all applicable actions

			int numNewPredicatesInNewState = 0;
			int numOldPredicatesNotInNewState = 0;
			int compareListSize = actionsForThisState.size();

			// numNewStates++;

			boolean printState = false;
			if (printState) {
				System.out.print("current actions are: ");
				for (int l = 0; l < compareListSize; l++) {
					System.out.print((Integer) actionsForThisState.get(l) + " ");
				}
				System.out.println();

				for (int l = 0; l < compareListSize; l++) {
					Integer diffVal = (Integer) actionsForThisState.get(l);
					if (!prevCompareList.contains(diffVal)) {
						System.out.println("difference - previous state does not contain: " + diffVal);
						numNewPredicatesInNewState++;
					}
				}
				int prevCompareListSize = prevCompareList.size();
				for (int l = 0; l < prevCompareListSize; l++) {
					Integer diffVal = (Integer) prevCompareList.get(l);
					if (!actionsForThisState.contains(diffVal)) {
						System.out.println("difference - current state does not contain: " + diffVal);
						numOldPredicatesNotInNewState++;
					}
				}
			}
			// totalNewDifferences += numNewPredicatesInNewState;
			// System.out.println("finished: current state: " + numNewStates + " average
			// difference: " + (double)totalNewDifferences / (double)numNewStates);

			prevCompareList.clear();
			prevCompareList = new ArrayList(actionsForThisState);
			actionsForThisState.clear();

			/* Decision of not apply any action, but apply all next events */
			if (sp.canMoveForward()) {
				// Check if we move forward, then if we get pass some goal's deadline
				// before achieve it --> the resulting state from advance-time action
				// is inconsistent with deadline goals
				if ((!deadlineFlag) || util.checkConsistentGoalDeadline(sp.getCurrentPredDB(), sp.getNextEventTime())) {
					tempSP = new StateInfo(sp);
					tempSP.moveForward(); // Q: Why don't we need to check if all goals satisfied here???
					// A: Because if the set of future events satisfy the goals within given
					// deadlines,
					// then that state will have heuristic value = 0 => don't need to wait until
					// advance
					// the time until all those future events occur to realize it.

					/****
					 * Newly changed part -- recalculating the heu when apply "advance-time"
					 *****/
					// For actual code -- refer to May20-2001 Version
					/*******************************/

					/**** Old part ******/
					stateMan.addStateInfo(tempSP);
					generatedState++;
					/*******************/
				}
			}
		}

		return 2;
	}

	/**
	 * Function to help the -quality option
	 */
	int qualityStep;

	private void switchQualityOption() {
		switch (lookaheadOption) {
		case 1:
			lookaheadOption = 2;
			break;
		case 2:
			lookaheadOption = -1;
			break;
		case -1:
			lookaheadOption = 1;
			break;
		}

		qualityStep++;
		if (qualityStep > 3)
			qualityFlag = false;
		else
			System.out.println("\n;;       Try to find better makespan solution with qualityFlag is on");
	}

	int autoStep;

	private void switchAutoOption() {
		if (autoStep == 1) {
			haFlag = true;
			haneFlag = true;
			System.out.println(";; Switch -ha => -hane: ");
		}

		if (autoStep == 2) {
			haFlag = false;
			haneFlag = false;
			checkBestHeu = false;
			System.out.println(";; Switch -hane => no-ha: ");
		}

		autoStep++;
		if (autoStep > 3)
			autoFlag = false;
	}

	/**
	 * Main routine to solve the problem
	 */
	public int solve(Domain domain, Problem prob) {
		int i, j, k;

		/* Call some initialization functions */
		deadlineFlag = prob.goalDeadlineFlag();
		domain.ppTypeTree();
		prob.absorbConstants(domain.getConstantMap());

		/* Initializing the Utility instance */
		prob.makeTypeMap(domain);
		groundInstance = new Grounding(dupaFlag);

		domain.setLhs(util.getLHS(domain));
		stateMan = groundInstance.getStateManager(domain, prob);

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

		System.out.println(";;Function: " + mresDB.numFunction() + ". Predicate: " + groundInstance.numPred()
				+ ". Grounded Action: " + gActions.size());
		stringOut += "<p>Functions:         " + mresDB.numFunction() + "<p>Predicates:\t         "
				+ groundInstance.numPred() + "<p>Grounded Actions:     " + gActions.size() + "\n";
		// ........... END DEBUGGING .............

		/*
		 * Finish initialization. Do some pre-processing.
		 */
		aDate = new Date();
		time1 = aDate.getTime();

		System.out.println(";;Parsing & grounding: " + (time1 - timeX) + " milisecs.");
		stringOut += "<p>Parsing\\Grounding:   " + (time1 - timeX) + " ms\n";
		System.out.println("\n;;<<< Start Searching for Solution >>>");

		// Pass the Goals & Ground Actions information to the Utility class
		util.initialize(gActions, stateMan.getGoalState().getPredDB().getAllPred(),
				stateMan.getGoalState().getPredDB().getAllTimeMap());

		// Preprocessing the maximum value for achieving each resource
		util.maxResPreprocess(stateMan.getInitState().getMResDB());

		// Set the right helpful action flag
		if (autoFlag) {
			haFlag = true;
			haneFlag = true;
		}

		// Initialize the RTPG class
		rmtpg.initGoals(util.getSortedGoals(), util.getGoalTime());
		rmtpg.optionSetting(deadlineFlag, costPropOption, relaxedPlanOption, goalCostOption, haFlag, haneFlag,
				lookaheadOption);
		rmtpg.buildBiLevelGraph(groundInstance.numPred(), gActions);
		/*
		 * Start searching
		 */
		sp = new StateInfo(prob.numInitFunct());
		sp.setCurrentState(stateMan.getInitState());
		// BM: Added Sep 2, 2003 for exogenous events
		ArrayList tempA;
		Event e;
		tempA = stateMan.getInitEvents();
		int numEvents = tempA.size();
		if (numEvents > 0)
			System.out.println("There are " + numEvents + " events in the initial state.");
		for (i = 0; i < numEvents; i++) {
			e = (Event) tempA.get(i);
			sp.addEvent(e); // NOT TESTING PART

			// Testing
			if (e.getNeg())
				System.out.print("InitEvent: -");
			else
				System.out.print("InitEvent: +");
			System.out.println(e.getPred() + " - Time: " + e.getTime());
			// End Testing
		}
		// End Testing

		sp.setCSTime(0);

		// Calculate the heuristic value for the INITIAL state. It will always be
		// the starting point for any search option
		propResult = rmtpg.costPropagation(stateMan.getInitState().getPredDB(), sp.getAllEvents(), 0, new ArrayList(),
				new ArrayList());

		if (propResult < 0) {
			System.out.println(";;No solution at initial state !!");
			stringOut += "No solution !!\n";
			stateMan.clearStateQueue();
			return 0;
		}
		if (propResult == 0) {
			System.out.println(";;Goals satisfied in the initial state");
			stringOut += "Goals satisfied in the initial state\n";
			stateMan.clearStateQueue();
			return 0;
		}

		heuristicValue = rmtpg.getHeuristicValue();
		sp.setDistance(H_WEIGHT * heuristicValue);

		// If we decide to use the helpful action option
		if (haFlag) {
			if (!haneFlag) {
				sp.setHaEffects(rmtpg.collectHAPEffects(), rmtpg.numHAPEffects(), null);
			} else {
				Integer ne;
				haActs = rmtpg.getHActs();
				haNEffects.clear();

				for (i = 0; i < haActs.size(); i++) {
					aID = ((Integer) haActs.get(i)).intValue();

					for (j = 0; j < ((GAction) gActions.get(aID)).numDelete(); j++) {
						ne = new Integer(((GAction) gActions.get(aID)).getDelete(j));

						if (!haNEffects.contains(ne))
							haNEffects.add(ne);
					}
				}

				sp.setHaEffects(rmtpg.collectHAPEffects(), rmtpg.numHAPEffects(), haNEffects);
			}
		}

		initState = new StateInfo(sp);
		stateMan.addStateInfo(sp);

		/*
		 * If we run with the "auto" option, we will first try the "-ha" flag, then
		 * "-hane" flag, then finally turn both of them off. Switching between options
		 * occur when there is the best estimated heuristic value in the queue INCREASE.
		 * May try other approaches to switch between different options.
		 */
		bestHeuValue = heuUpperLimit;
		deltaHeu = (float) 3 * G_WEIGHT;
		exploredStateLimit = gActions.size() / 5;
		if (exploredStateLimit < 100)
			exploredStateLimit = 100;
		bestMakespan = (float) -1.0;

		if (autoFlag) {
			autoStep = 1;
			checkBestHeu = true;
			haFlag = true;
			haneFlag = false;
			System.out.println(";; Run with *auto* option. Start with -ha flag.");
		} else {
			checkBestHeu = false;
		}

		if (qualityFlag == true) {
			qualityStep = 1;
		}

		int solutionFlag;
		boolean qualityStarted = false;
		while (true) {
			/* Start taking the best *StateInfo* from stateMan and try to extend it */
			solutionFlag = solutionSearching();

			switch (solutionFlag) {
			case 0: // Error in solutionSearching()
				stateMan.clearStateQueue();
				return 0;
			case 1: // Solution found
				autoFlag = false;
				checkBestHeu = true;

				// If we switch on the qualityFlag and want to continue for better solution
				if (qualityFlag) {
					switchQualityOption();
				}
				break;
			case 2: // Break out of solutionSearching() due to over limit checking
				if (autoFlag)
					switchAutoOption();
				else {
					switchQualityOption();
				}
				break;
			}

			// It no more search is needed to be done because "auto" & "quality" are both
			// *off*
			if ((autoFlag == false) && (qualityFlag == false)) {
				stateMan.clearStateQueue();
				// outputPlanToFile();
				return 0;
			}

			// Add stuff to reinitialize when we switch to another option in line (autoFlag
			// = true)
			bestHeuValue = heuUpperLimit;
			stateMan.resetStateQueue(initState);

			if (autoFlag) {
				rmtpg.resetHAOption(haFlag, haneFlag);
			} else { // if autoFlag == false, then qualityFlag == true
				rmtpg.resetLAOption(lookaheadOption);
			}

			generatedState = 1;
			exploredState = 0;
		}
	}

	/********* MAIN FUNCTION ***********/
	public static void main(String args[]) {
		PDDL21Parser parser21 = new PDDL21Parser(System.in);
		Planner sapa = new Planner();
		boolean lineArgs = false;
		System.out.println(";; " + System.getProperty("user.dir"));
		if (args.length < 2) {
			sapa.printUsage();
			System.exit(1);
		} else {
			/* Reading the options to run the planner */
			sapa.readOptions(args);
			lineArgs = true;
		}

		if (!text_mode) {
			// sf = new SapaFrame(lineArgs);
			// sf.show();
		}

		while (text_mode || (!text_mode)) {
			Domain domain;// = new Domain();
			Problem prob;// = new Problem();

			FileInputStream pddl_file;
			if (!text_mode) {
				// sf.resetArgs();

				stringOut = new String("");
			}

			while (args.length < 1) {
			}
			if (args.length < 1) {
				// args = sf.getArgs();
				sapa.readOptions(args);
			}

			/*
			 * Instantiate a parser
			 */
			Date d = new Date();
			timeX = d.getTime(); // Get the starting time of the program

			/*** Parse the Domain specification file ****/
			try {
				pddl_file = new java.io.FileInputStream(args[0]);
				PDDL21Parser.ReInit(pddl_file);
			} catch (java.io.FileNotFoundException e) {
				System.out.println("Domain file " + args[0] + " not found !!!");
				return;
			}

			try {
				domain = PDDL21Parser.parse_domain_pddl();
				System.out.println(";;Domain file succesfully read !!" + " num actions = " + domain.numAction());
			} catch (ParseException e) {
				System.out.println("Exception while parsing domain file!!");
				e.printStackTrace();
				return;
			}

			/**** Parse the problem file ****/
			try {
				pddl_file = new java.io.FileInputStream(args[1]);
				PDDL21Parser.ReInit(pddl_file);
			} catch (java.io.FileNotFoundException e) {
				System.out.println("Problem file " + args[1] + " not found !!!");
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

			if (!text_mode) {
			}
			// sf.showProgressWindow(true);

			stringOut += "<html><head></head><body>Domain: " + domain.getName() + "\n";
			stringOut += "<p>Problem: " + prob.getName() + "\n";
			sapa.solve(domain, prob);

			args = new String[0];

			if (text_mode)
				break;
		}
	}

	/**
	 * Print the usage to run the planner
	 */
	private void printUsage() {
		System.out.println("Usage: java [Sapa-dir].Planner domain.pddl problem.pddl [option]\n"
				+ "Flags: -debug -cp [NUMBER] -norp -gc -la [NUMBER] -noauto -postProcess"
				+ " -ha -hane -noresadj -timelimit [NUMBER] -freq [NUMBER] -outfile [STRING]\n\n"

				// +"\t-gui Use the GUI\n"
				+ "\t-debug          Print detailed problems, domains, actions information\n"
				+ "\t-cp             Cost Propagation Option: 0-max; 1-sum (default); 2-Combo\n"
				+ "\t-norp           Direct heuristic (DO NOT extract relaxed plan)\n"
				+ "\t-gc             GoalCost Aggregation Option: 0-max; 1-sum(default); 2-Combo\n"
				+ "\t-la             Lookahead option. Default: lookahead = -1\n"
				+ "\t-noauto         Do not use the *auto* running option (auto switch between option)\n"
				// + "\t-quality Try to improve the quality after found first solution (with
				// different options)\n"
				+ "\t-ha             Helpful actions (auto = false)\n"
				+ "\t-hane           Using negative effects of helpful actions (auto=false)\n"
				+ "\t-noresadj       Do not use the resource adjustment technique\n"
				+ "\t-dupa           Check duplicate parameters in action description\n"
				+ "\t-timelimit      Time cutoff in seconds (to stop the program)\n"
				+ "\t-freq           Frequency to check the time cutoff limit\n"
				+ "\t                    (e.g number of generated search nodes)\n"
				+ "\t-hw             Weight given to the h value (heuristic = g + hw*h)\n"
				+ "\t-outfile        Output file for the solution.\n"
				+ "\t-postProcess    Perform greedy post processing to re-order actions\n"
				+ "                      (use before validating plans)\n"
				+ "Default options: -cp 1 -gc 1 -la -1 -timelimit 7200 -freq 200\n\n");
	};

	/**
	 * Function to parse the running options
	 */
	private void readOptions(String args[]) {
		int i, j;

		for (i = 2; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-gui")) {
				System.out.println("The GUI is not available in this version of Sapa.");
				System.exit(0);
				// text_mode = false;
				continue;
			}
			if (args[i].equalsIgnoreCase("-debug")) {
				debug_mode = true;
				continue;
			}
			if (args[i].equalsIgnoreCase("-cp")) {
				if (i + 1 >= args.length) {
					System.out.println("No cost propagation value specified. Ignore -cp flag.");
					continue;
				}

				try {
					costPropOption = (new Integer(args[++i])).intValue();
				} catch (NumberFormatException e) {
					costPropOption = 1;
					System.out.println("Cost prop. option in INCORRECT format. Ignore -cp flag.");
				}

				if ((costPropOption < 0) || (costPropOption > 2)) {
					System.out.println("Valid costPropOption = 0,1, or 2. Use default value");
					costPropOption = 1;
				}
				continue;
			}
			if (args[i].equalsIgnoreCase("-norp")) {
				relaxedPlanOption = false;
				continue;
			}
			if (args[i].equalsIgnoreCase("-gc")) {
				if (i + 1 >= args.length) {
					System.out.println("Need to specify goal cost option. Ignore -gc flag.");
					continue;
				}

				try {
					goalCostOption = (new Integer(args[++i])).intValue();
				} catch (NumberFormatException e) {
					goalCostOption = 1;
					System.out.println("Goal cost option in INCORRECT format. Ignore -gc flag.");
				}

				if ((goalCostOption < 0) || (goalCostOption > 2)) {
					System.out.println("Valid goalCostOption = 0,1 or 2. Use default value");
					goalCostOption = 1;
				}
				continue;
			}
			if (args[i].equalsIgnoreCase("-la")) {
				if (i + 1 >= args.length) {
					System.out.println("Need to specify lookahead value. Ignore -la flag.");
					continue;
				}

				try {
					lookaheadOption = (new Integer(args[++i])).intValue();
				} catch (NumberFormatException e) {
					lookaheadOption = 1;
					System.out.println("Goal cost option in INCORRECT format. Ignore -gc flag.");
				}

				if ((lookaheadOption < -1) || (lookaheadOption > 2)) {
					System.out.println("Valid lookaheadOption = -1,0,1 or 2. Use default value");
					lookaheadOption = -1;
				}
				continue;
			}
			if (args[i].equalsIgnoreCase("-noauto")) {
				autoFlag = false;
				continue;
			}
			if (args[i].equalsIgnoreCase("-quality")) {
				qualityFlag = true;
				continue;
			}
			if (args[i].equalsIgnoreCase("-ha")) {
				haFlag = true;
				autoFlag = false;
				continue;
			}
			if (args[i].equalsIgnoreCase("-hane")) {
				haneFlag = true;
				haFlag = true;
				autoFlag = false;
				continue;
			}
			if (args[i].equalsIgnoreCase("-noresadj")) {
				res_adj = false;
				continue;
			}
			if (args[i].equalsIgnoreCase("-dupa")) {
				dupaFlag = true;
				continue;
			}
			if (args[i].equalsIgnoreCase("-postProcess")) {
				postProcessFlag = true;
				continue;
			}
			if (args[i].equalsIgnoreCase("-timelimit")) {
				if (i + 1 >= args.length) {
					System.out.println("No time limit specified. Ignore -timelimit flag.");
					continue;
				}

				try {
					cutoff = (new Integer(args[++i])).intValue();
				} catch (NumberFormatException e) {
					cutoff = 7200;
					System.out.println("Time cutoff limit in INCORRECT format. Ignore flag.");
				}
				continue;
			}
			if (args[i].equalsIgnoreCase("-freq")) {
				if (i + 1 >= args.length) {
					System.out.println("No frequency value specified. Ignore -freq flag.");
					continue;
				}

				try {
					freq = (new Integer(args[++i])).intValue();
				} catch (NumberFormatException e) {
					freq = 200;
					System.out.println("Frequency value in INCORRECT format. Ignore -freq flag.");
				}
				continue;
			}
			if (args[i].equalsIgnoreCase("-hw")) {
				if (i + 1 >= args.length) {
					System.out.println("No H_WEIGHT value specified. Ignore -hw flag.");
					continue;
				}

				try {
					H_WEIGHT = (new Integer(args[++i])).intValue();
				} catch (NumberFormatException e) {
					H_WEIGHT = 5;
					System.out.println("H_WEIGHT value in INCORRECT format. Ignore -hw flag.");
				}
				continue;
			}
			if (args[i].equalsIgnoreCase("-outfile")) {
				if (i + 1 >= args.length) {
					System.out.println("No outfile name specified. Ignore -outfile flag.");
					continue;
				}

				outfileName += args[++i];
				continue;
			}

			System.out.println("Ignore incorrect flag: " + args[i]);
		}
	}
}
