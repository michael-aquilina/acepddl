package mt.uol.smi.attempto.aceview.planner.connectors;

import mt.uol.smi.attempto.aceview.planner.IACEPlannerRunner;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerModel;

public class DefaultConnector implements IACEPlannerConnector {

	static final private boolean debug_mode = true;
//	static private ACEPlannerDomain domain;
//	static private ACEPlannerProblem prob;
//	static private StateManager stateMan;

	private DefaultConnector() {
	}

	@Override
	public ACEPlannerModel run(ACEPlannerModel model, IACEPlannerRunner runner) {
		readACEPlanner(runner);
//		IPlanner planner = new ACEPlannerPlanner();
//		planner.solve(domain, prob, runner);
		return model;
	}

	@Override
	public IACEPlannerConnector getInstance() {
		return new DefaultConnector();
	}

	private void readACEPlanner(IACEPlannerRunner runner) {
//		ACEPlannerParser parser21 = new ACEPlannerParser(runner);
//
//		domain = parser21.parse_domain_Planner();
//		prob = parser21.parse_problem_Planner();

	}

}
