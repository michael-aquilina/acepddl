package mt.uol.smi.attempto.aceview.planner.ui;

import java.awt.Font;

import javax.swing.JTable;

import mt.uol.smi.attempto.aceview.planner.model.AvailableSnippetsTableModel;
import mt.uol.smi.attempto.aceview.planner.model.EffectSnippetsTableModel;

public class ACEPlannerEffectTable extends JTable {

	public ACEPlannerEffectTable() {
		init();
	}

	public ACEPlannerEffectTable(EffectSnippetsTableModel tm) {
		super(tm);
		init();
	}

	public void setFont(String fontName, int fontSize) {
		setFont(new Font(fontName, Font.PLAIN, fontSize));
	}

	public EffectSnippetsTableModel getEffectSnippetsTableModel() {
		return (EffectSnippetsTableModel) this.getModel();
	}

	private void init() {
	}
}