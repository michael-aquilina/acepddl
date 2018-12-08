package mt.uol.smi.attempto.aceview.planner.model;

import java.util.Comparator;

import ch.uzh.ifi.attempto.aceview.ACESnippet;

public class ACEPlannerSelectedSnippet extends ACEPlannerSnippet {

	private boolean selected;
	private boolean effect;

	public ACEPlannerSelectedSnippet(ACESnippet aceSnippet, boolean selected) {
		super(aceSnippet);
		this.selected = selected;
		this.effect = false;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean value) {
		this.selected = value;
	}

	public boolean isEffect() {
		return effect;
	}

	public void setEffect(boolean effect) {
		this.effect = effect;
	}
}
