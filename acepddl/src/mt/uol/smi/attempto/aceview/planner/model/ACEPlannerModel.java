package mt.uol.smi.attempto.aceview.planner.model;

import java.util.ArrayList;
import java.util.List;

public class ACEPlannerModel extends ACEPlannerObject {
	private String name;
	private ACEPlannerActionListModel actionList; // these are all snippets
	private ACEPlannerProblemModel problem;
	private ACEPlannerSnippetsUsedModel sentencesUsed;
	private ACEPlannerGoalModel goal;
	private ACEPlannerSolutionModel solution;

	public ACEPlannerModel() {
		super();
		name = "not set";
		actionList = new ACEPlannerActionListModel();
		sentencesUsed = new ACEPlannerSnippetsUsedModel();
		problem = new ACEPlannerProblemModel();
		goal = new ACEPlannerGoalModel();
		solution = new ACEPlannerSolutionModel();
	}

	public ACEPlannerModel(String id) {
		super(id);
		name = "not set";
		actionList = new ACEPlannerActionListModel();
		sentencesUsed = new ACEPlannerSnippetsUsedModel();
		problem = new ACEPlannerProblemModel();
		goal = new ACEPlannerGoalModel();
		solution = new ACEPlannerSolutionModel();
	}

	public ACEPlannerModel(ACEPlannerModel m) {
		super(m);
		this.actionList = new ACEPlannerActionListModel(m.getActionListModel());
		this.goal = new ACEPlannerGoalModel(m.getGoal());
		this.name = m.getName();
		this.problem = new ACEPlannerProblemModel(m.getProblem());
		this.sentencesUsed = new ACEPlannerSnippetsUsedModel(m.getSentencesUsed());
		this.solution = new ACEPlannerSolutionModel(m.getSolution());
	}

	public ACEPlannerActionListModel getActionListModel() {
		return actionList;
	}

	public void setActionListModel(ACEPlannerActionListModel actionList) {
		this.actionList = actionList;
	}

	public ACEPlannerProblemModel getProblem() {
		return problem;
	}

	public void setProblem(ACEPlannerProblemModel problem) {
		this.problem = problem;
	}

	public ACEPlannerSnippetsUsedModel getSentencesUsed() {
		return sentencesUsed;
	}

	public void setSentencesUsed(ACEPlannerSnippetsUsedModel sentencesUsed) {
		this.sentencesUsed = sentencesUsed;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}	
	
	public ACEPlannerGoalModel getGoal() {
		return goal;
	}

	public void setGoal(ACEPlannerGoalModel goal) {
		this.goal = goal;
	}	

	public ACEPlannerSolutionModel getSolution() {
		return solution;
	}

	public void setSolution(ACEPlannerSolutionModel solution) {
		this.solution = solution;
	}

	public boolean removeAction(ACEPlannerActionModel toRemove) {
		ACEPlannerActionModel action = findAction(toRemove);
		if (action != null) {
			return actionList.getList().remove(action);
		}
		return false;
	}

	public boolean hasAction(ACEPlannerActionModel toFind) {
		return findAction(toFind) != null;
	}

	public ACEPlannerActionModel findAction(ACEPlannerActionModel toRemove) {
		for (ACEPlannerActionModel action : actionList.getList()) {
			if (action.equals(toRemove)) {
				return action;
			}
		}
		return null;
	}

	public ACEPlannerActionModel findActionById(String id) {
		for (ACEPlannerActionModel action : actionList.getList()) {
			if (action.getId().equals(id)) {
				return action;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		String result = "";
		result += "PlannerModel " + getName() + " " + getId();
		result += "\nActions:";
		for (ACEPlannerActionModel model : actionList.getList()) {
			result += "\n" + model.toString();
		}
		result += "\nSentences used:";
		for (ACEPlannerSnippet model : sentencesUsed.getSnippets()) {
			result += "\n" + model.toString();
		}
		result += "\nDomain:";
		for (ACEPlannerSnippet model : problem.getSnippets()) {
			result += "\n" + model.toString();
		}
		return result;
	}
}
