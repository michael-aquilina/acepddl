package mt.uol.smi.attempto.aceview.planner.ui;

import java.awt.Font;

import javax.swing.JTable;

import mt.uol.smi.attempto.aceview.planner.model.AvailableSnippetsTableModel;

public class ACEPlannerSnippetSelectionTable extends JTable {

	public ACEPlannerSnippetSelectionTable() {
		init();
	}

	public ACEPlannerSnippetSelectionTable(AvailableSnippetsTableModel tm) {
		super(tm);
		init();
	}

	public void setFont(String fontName, int fontSize) {
		setFont(new Font(fontName, Font.PLAIN, fontSize));
	}

	public AvailableSnippetsTableModel getTableSnippetsModel() {
		return (AvailableSnippetsTableModel) this.getModel();
	}

	private void init() {
	}
}