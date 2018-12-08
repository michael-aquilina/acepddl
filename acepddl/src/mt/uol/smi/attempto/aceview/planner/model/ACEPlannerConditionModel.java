package mt.uol.smi.attempto.aceview.planner.model;

import java.util.ArrayList;
import java.util.List;

import ch.uzh.ifi.attempto.aceview.ACEAnswer;

public class ACEPlannerConditionModel extends ACEPlannerObject {

	private List<ACEPlannerParameter> parameters;
	private ACEPlannerSnippet acePlannerSnippet;
	private ACEAnswer answer;

	public ACEPlannerConditionModel() {
		super();
		parameters = new ArrayList<ACEPlannerParameter>();
	}

	public ACEPlannerConditionModel(ACEPlannerSnippet aceSnippet) {
		super();
		acePlannerSnippet = aceSnippet;
		parameters = new ArrayList<ACEPlannerParameter>();
	}

	public ACEPlannerConditionModel(String id, ACEPlannerSnippet aceSnippet) {
		super(id);
		acePlannerSnippet = aceSnippet;
		parameters = new ArrayList<ACEPlannerParameter>();
	}

	public ACEPlannerConditionModel(ACEPlannerConditionModel m) {
		super(m);
		this.acePlannerSnippet = new ACEPlannerSnippet(m.getAcePlannerSnippet());
		this.parameters = new ArrayList<ACEPlannerParameter>();
		for (ACEPlannerParameter a : m.getParameters()) {
			this.parameters.add(new ACEPlannerParameter(a));
		}
	}

	public List<ACEPlannerParameter> getParameters() {
		return parameters;
	}

	public boolean removeParameter(ACEPlannerParameter toRemove) {
		ACEPlannerParameter parameter = findParameter(toRemove);
		if (parameter != null) {
			return parameters.remove(parameter);
		}
		return false;
	}

	public boolean hasParameter(ACEPlannerParameter toFind) {
		return findParameter(toFind) != null;
	}

	public ACEPlannerParameter findParameter(ACEPlannerParameter toRemove) {
		for (ACEPlannerParameter parameter : parameters) {
			if (parameter.equals(toRemove)) {
				return parameter;
			}
		}
		return null;
	}

	public void setParameters(List<ACEPlannerParameter> parameters) {
		this.parameters = parameters;
	}

	public ACEPlannerSnippet getAcePlannerSnippet() {
		return acePlannerSnippet;
	}

	public void setAcePlannerSnippet(ACEPlannerSnippet acePlannerSnippet) {
		this.acePlannerSnippet = acePlannerSnippet;
	}

	public String getSentence() {
		if (this.acePlannerSnippet != null) {
			return this.acePlannerSnippet.getSentence();
		}
		return "";
	}

	@Override
	public String toString() {
		String result = "Condition " + getSentence();
		for (ACEPlannerParameter parameter : parameters) {
			result += "\n" + parameter.toString();
		}
		return result;
	}

}
