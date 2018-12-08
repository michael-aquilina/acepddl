package mt.uol.smi.attempto.aceview.planner.model;

import java.util.ArrayList;
import java.util.ArrayList;
import java.util.List;
import java.util.List;

import org.apache.log4j.Logger;

import ch.uzh.ifi.attempto.aceview.ACESnippet;

public class ACEPlannerSolutionModel extends ACEPlannerObject {

	private ACEPlannerSolutionState solutionState;
	private List<ACEPlannerSatisfiedActionModel> satisfiedActions; // these are answers
	private static final Logger logger = Logger.getLogger(ACEPlannerSolutionModel.class);

	public ACEPlannerSolutionModel() {
		super();
		solutionState = ACEPlannerSolutionState.NOT_FOUND;
		satisfiedActions = new ArrayList<ACEPlannerSatisfiedActionModel>();
	}
	
	public ACEPlannerSolutionModel(ACEPlannerSolutionModel model) {
		solutionState = model.getSolutionState();
		this.satisfiedActions = new ArrayList<ACEPlannerSatisfiedActionModel>();
		for (ACEPlannerSatisfiedActionModel p : model.getSatisfiedActions()) {
			this.satisfiedActions.add(new ACEPlannerSatisfiedActionModel(p));
		}
	}
	
	public ACEPlannerSolutionModel(ACEPlannerSolutionState solutionState, List<ACEPlannerSatisfiedActionModel> satisfiedActions) {
		super();
		this.solutionState = solutionState;
		this.satisfiedActions = satisfiedActions;
	}	
	
	public ACEPlannerSolutionState getSolutionState() {
		return solutionState;
	}

	public void setSolutionState(ACEPlannerSolutionState solutionState) {
		this.solutionState = solutionState;
	}

	public List<ACEPlannerSatisfiedActionModel> getSatisfiedActions() {
		return satisfiedActions;
	}

	public void setSatisfiedActions(List<ACEPlannerSatisfiedActionModel> satisfiedActions) {
		this.satisfiedActions = satisfiedActions;
	}

	@Override
	public String toString() {
		String result = "Solution : " + this.getSolutionState().toString();
		for (ACEPlannerSatisfiedActionModel satisfiedAction : satisfiedActions) {
			result += "\n" + satisfiedAction.toString();
		}
		return result;
	}

}
