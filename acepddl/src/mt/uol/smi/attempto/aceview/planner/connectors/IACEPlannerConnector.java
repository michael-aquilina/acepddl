package mt.uol.smi.attempto.aceview.planner.connectors;

import mt.uol.smi.attempto.aceview.planner.IACEPlannerRunner;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerModel;

public interface IACEPlannerConnector {

	ACEPlannerModel run(ACEPlannerModel model, IACEPlannerRunner runner);
	
	IACEPlannerConnector getInstance();
}
