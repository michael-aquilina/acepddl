package mt.uol.smi.attempto.aceview.planner.model;

import java.util.ArrayList;
import java.util.ArrayList;
import java.util.List;
import java.util.List;

import org.apache.log4j.Logger;

import ch.uzh.ifi.attempto.aceview.ACESnippet;

public class ACEPlannerActionModel extends ACEPlannerObject {

	private String actionName;
	private List<ACEPlannerParameter> parameters;
	private List<ACEPlannerConditionModel> conditions; // these are questions
	private List<ACEPlannerEffectTuple> effects; // these are answers
	private static final Logger logger = Logger.getLogger(ACEPlannerActionModel.class);

	public ACEPlannerActionModel() {
		super();
		actionName = "";
		parameters = new ArrayList<ACEPlannerParameter>();
		conditions = new ArrayList<ACEPlannerConditionModel>();
		effects = new ArrayList<ACEPlannerEffectTuple>();
	}

	public ACEPlannerActionModel(ACEPlannerActionModel m) {
		super(m);
		actionName = m.getActionName();
		this.parameters = new ArrayList<ACEPlannerParameter>();
		for (ACEPlannerParameter a : m.getParameters()) {
			if (a != null)
				this.parameters.add(new ACEPlannerParameter(a));
		}
		conditions = new ArrayList<ACEPlannerConditionModel>();
		for (ACEPlannerConditionModel a : m.getConditions()) {
			if (a != null)
				this.conditions.add(new ACEPlannerConditionModel(a));
		}
		effects = new ArrayList<ACEPlannerEffectTuple>();
		for (ACEPlannerEffectTuple a : m.getEffects()) {
			if (a != null)
				this.effects.add(new ACEPlannerEffectTuple(a));
		}
	}

	public String getActionName() {
		return actionName;
	}

	public void setActionName(String label) {
		this.actionName = label;
	}

	public List<ACEPlannerParameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<ACEPlannerParameter> parameters) {
		this.parameters = parameters;
	}

	public List<ACEPlannerConditionModel> getConditionsAsList() {
		return new ArrayList<ACEPlannerConditionModel>(conditions);
	}

	public List<ACEPlannerConditionModel> getConditions() {
		return conditions;
	}

	public List<ACEPlannerConditionModel> getConditionsPostive() {
		return conditions;
	}

	public void setConditions(List<ACEPlannerConditionModel> conditions) {
		this.conditions = conditions;
	}

	public List<ACEPlannerEffectTuple> getEffects() {
		return effects;
	}

	public void setEffects(List<ACEPlannerEffectTuple> effects) {
		this.effects = effects;
	}

	public boolean removeEffect(ACESnippet toRemove) {
		ACEPlannerEffectTuple effect = findACEPlannerEffectTuple(toRemove);
		if (effect != null) {
			return effects.remove(effect);
		}
		return false;
	}

	public boolean hasEffect(ACESnippet toFind) {
		return findACEPlannerEffectTuple(toFind) != null;
	}

	public ACEPlannerEffectTuple findACEPlannerEffectTuple(ACESnippet toFind) {
		for (ACEPlannerEffectTuple effect : effects) {
			if (effect.getUnformattedResult().getAceSnippet().isEqual(toFind)) {
				return effect;
			}
		}
		return null;
	}

	public boolean removeCondition(ACESnippet toRemove) {
		ACEPlannerConditionModel condition = findCondition(toRemove);
		if (condition != null) {
			return conditions.remove(condition);
		}
		return false;
	}

	public boolean hasCondition(ACESnippet toFind) {
		return findCondition(toFind) != null;
	}

	public ACEPlannerConditionModel findCondition(ACESnippet toFind) {
		for (ACEPlannerConditionModel condition : conditions) {
			if (condition.getAcePlannerSnippet().getAceSnippet().isEqual(toFind)) {
				return condition;
			}
		}
		return null;
	}


	@Override
	public String toString() {
		String result = "Action " + getActionName();
		result += "\tConditions:";
		if (conditions != null) {
			for (ACEPlannerConditionModel condition : conditions) {
				result += "\t\t" + condition.toString();
			}
		}
		result += "\nEffects snippets:";
		if (effects != null) {
			for(ACEPlannerEffectTuple tuple : effects) {
				result += "\n" +tuple.toString();
			}
		}
		return result;
	}
}
