package mt.uol.smi.attempto.aceview.planner.model.event;

public class ACEPlannerEvent<T> {

	private final T type;

	public ACEPlannerEvent(T type) {
		this.type = type;
	}

	public T getType() {
		return type;
	}

	public boolean isType(T type) {
		return this.type.equals(type);
	}
}