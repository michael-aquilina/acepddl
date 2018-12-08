package mt.uol.smi.attempto.aceview.planner.ui;

import java.util.Comparator;

import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerActionModel;

public class ACEPlannerActionModelComparator implements Comparator<ACEPlannerActionModel> {
	@Override
	public int compare(ACEPlannerActionModel o1, ACEPlannerActionModel o2) {
		if (o1 == null || o2 == null || o1.getActionName() == null || o2.getActionName() == null) {
			return 0;
		}
		return o1.getActionName().compareTo(o2.getActionName());
	}
}
