package mt.uol.smi.attempto.aceview.planner;

public class ACEPlannerManager {

	private static IACEPlannerManager manager;

	private ACEPlannerManager() {
		// TODO Auto-generated constructor stub
	}

	public static IACEPlannerManager getInstance() {
		if (manager == null) {
			manager = new ACEPlannerManagerDefault();
		}
		return manager;
	}

}
