package mt.uol.smi.attempto.aceview.planner.sapa;

import mt.uol.smi.attempto.aceview.planner.sapa.parsing.ACEPlannerDomain;
import mt.uol.smi.attempto.aceview.planner.sapa.parsing.ACEPlannerProblem;
import mt.uol.smi.attempto.aceview.planner.IACEPlannerManager;
import mt.uol.smi.attempto.aceview.planner.IACEPlannerRunner;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerSolutionModel;

public interface IPlanner {

	ACEPlannerSolutionModel solve(IACEPlannerRunner runner);

}
