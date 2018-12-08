package mt.uol.smi.attempto.aceview.planner.model.event;

public interface ACEPlannerListener<T> {
	void handleChange(T event);
}