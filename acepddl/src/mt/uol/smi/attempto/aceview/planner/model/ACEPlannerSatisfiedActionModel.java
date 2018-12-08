package mt.uol.smi.attempto.aceview.planner.model;

import java.util.ArrayList;
import java.util.ArrayList;
import java.util.List;
import java.util.List;

import org.apache.log4j.Logger;

import ch.uzh.ifi.attempto.aceview.ACESnippet;

public class ACEPlannerSatisfiedActionModel extends ACEPlannerObject {

	private int order;
	private ACEPlannerActionModel action;
	private List<ACEPlannerSatisifiedParameter> parameters;
	private static final Logger logger = Logger.getLogger(ACEPlannerSatisfiedActionModel.class);

	public ACEPlannerSatisfiedActionModel() {
		super();
		order = 0;
		action = new ACEPlannerActionModel();
		parameters = new ArrayList<ACEPlannerSatisifiedParameter>();
	}

	public ACEPlannerSatisfiedActionModel(ACEPlannerSatisfiedActionModel a) {
		super(a);
		this.order = a.getOrder();
		this.parameters = new ArrayList<ACEPlannerSatisifiedParameter>();
		for (ACEPlannerSatisifiedParameter p : a.getSatisfiedParameters()) {
			this.parameters.add(new ACEPlannerSatisifiedParameter(p));
		}
		this.action = new ACEPlannerActionModel(a.getAction());
	}	
	
	public ACEPlannerSatisfiedActionModel(ACEPlannerActionModel action) {
		super();
		this.order = 0;
		this.action = new ACEPlannerActionModel(action);
		this.parameters = new ArrayList<ACEPlannerSatisifiedParameter>();
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public ACEPlannerActionModel getAction() {
		return action;
	}

	public void setAction(ACEPlannerActionModel action) {
		this.action = action;
	}

	public List<ACEPlannerSatisifiedParameter> getSatisfiedParameters() {
		return parameters;
	}

	public void setParameters(List<ACEPlannerSatisifiedParameter> parameters) {
		this.parameters = parameters;
	}

	@Override
	public String toString() {
		String actionName = getAction().getActionName();
		for (ACEPlannerEffectTuple effect : this.action.getEffects()) {
			for(ACEPlannerSatisifiedParameter param : effect.getSatisifedParameters()) {
				actionName = actionName.replaceAll(param.getParameterValue(),param.getSatisifiedIndividualName());
			}
		}
		String result = "Action " + this.order + " : " + actionName;
		return result;
	}
}
