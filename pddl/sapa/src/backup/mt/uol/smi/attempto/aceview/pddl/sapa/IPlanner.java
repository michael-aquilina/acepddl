package mt.uol.smi.attempto.aceview.pddl.sapa;

import mt.uol.smi.attempto.aceview.pddl.IACEPlannerRunner;
import mt.uol.smi.attempto.aceview.pddl.sapa.parsing.ACEPlannerDomain;
import mt.uol.smi.attempto.aceview.pddl.sapa.parsing.ACEPlannerProblem;

public interface IPlanner {

	int solve(ACEPlannerDomain domain, ACEPlannerProblem prob, IACEPlannerRunner runner);

}
