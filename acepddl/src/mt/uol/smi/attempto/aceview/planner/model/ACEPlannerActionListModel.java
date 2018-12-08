package mt.uol.smi.attempto.aceview.planner.model;

import java.util.ArrayList;
import java.util.List;

public class ACEPlannerActionListModel extends ACEPlannerObject {
	private List<ACEPlannerActionModel> actionList; 
	
	public ACEPlannerActionListModel() {
		super();
		actionList = new ArrayList<ACEPlannerActionModel>();
	}

	public ACEPlannerActionListModel(String id) {
		super(id);
		actionList = new ArrayList<ACEPlannerActionModel>();
	}

	public ACEPlannerActionListModel(ACEPlannerActionListModel m) {
		super(m);
		this.actionList = new ArrayList<ACEPlannerActionModel>();
		for(ACEPlannerActionModel a : m.getList()) {
			this.actionList.add(new ACEPlannerActionModel(a));
		}
	}

	public List<ACEPlannerActionModel> getList() {
		return actionList;
	}

	public void setList(List<ACEPlannerActionModel> actionList) {
		this.actionList = actionList;
	}
	
}
