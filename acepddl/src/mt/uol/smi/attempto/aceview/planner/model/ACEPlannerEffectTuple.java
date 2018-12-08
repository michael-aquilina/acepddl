package mt.uol.smi.attempto.aceview.planner.model;

import java.util.ArrayList;
import java.util.List;

public class ACEPlannerEffectTuple extends ACEPlannerObject {

	private List<ACEPlannerSatisifiedParameter> parameters;
	private ACEPlannerSnippet unformattedResult;
	private ACEPlannerSnippet formattedResult;
	private boolean negated;

	public ACEPlannerEffectTuple() {
		super();
		this.negated = false;
		this.parameters = new ArrayList<ACEPlannerSatisifiedParameter>();
		this.unformattedResult = new ACEPlannerSnippet();
		this.formattedResult = new ACEPlannerSnippet();
	}

	public ACEPlannerEffectTuple(ACEPlannerSnippet unformattedResult) {
		super();
		this.negated = false;
		this.parameters = new ArrayList<ACEPlannerSatisifiedParameter>();
		this.unformattedResult = unformattedResult;
		this.formattedResult = new ACEPlannerSnippet();
	}

	public ACEPlannerEffectTuple(ACEPlannerEffectTuple a) {
		super(a);
		this.formattedResult = new ACEPlannerSnippet(a.getFormattedResult());
		this.unformattedResult = new ACEPlannerSnippet(a.getUnformattedResult());
		this.negated = a.isNegated();
		this.parameters = new ArrayList<ACEPlannerSatisifiedParameter>();
		for (ACEPlannerSatisifiedParameter m : a.getSatisifedParameters()) {
			this.parameters.add(new ACEPlannerSatisifiedParameter(m));
		}
	}

	public List<ACEPlannerSatisifiedParameter> getSatisifedParameters() {
		return parameters;
	}

	public void setSatisifedParameters(List<ACEPlannerSatisifiedParameter> parameters) {
		this.parameters = parameters;
	}

	public ACEPlannerSnippet getUnformattedResult() {
		return unformattedResult;
	}

	public void setUnformattedResult(ACEPlannerSnippet result) {
		this.unformattedResult = result;
	}

	public boolean isNegated() {
		return negated;
	}

	public void setNegated(boolean negated) {
		this.negated = negated;
	}

	public ACEPlannerSnippet getFormattedResult() {
		return formattedResult;
	}

	public void setFormattedResult(ACEPlannerSnippet formattedResult) {
		this.formattedResult = formattedResult;
	}

	public String getTransformation() {
		String result = "Effect - " + this.getUnformattedResult().getSentence();
		if (this.getSatisifedParameters().size() > 0) {
			result += "Parameters: ";
			for (ACEPlannerSatisifiedParameter param : this.getSatisifedParameters()) {
				result += param.getParameterValue() + " => " + param.getSatisifiedIndividualName() + " ,";
			}
			result = result.substring(0, result.length() - 2);
		} else {
			result += " Did not use parameters.";
		}
		return result;
	}

	@Override
	public String toString() {
		String sentence = "n/a";
		if (this.getUnformattedResult() != null && this.getUnformattedResult() != null) {
			sentence = this.getUnformattedResult().getSentence();
		}
		return sentence + " -> " + (isNegated() ? "Negated" : "Not negated");
	}

}
