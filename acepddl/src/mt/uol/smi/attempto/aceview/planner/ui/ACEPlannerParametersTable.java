package mt.uol.smi.attempto.aceview.planner.ui;

import java.awt.Font;

import javax.swing.JTable;

import mt.uol.smi.attempto.aceview.planner.model.ParametersTableModel;

public class ACEPlannerParametersTable extends JTable {

	public ACEPlannerParametersTable() {
		init();
	}

	public ACEPlannerParametersTable(ParametersTableModel tm) {
		super(tm);
		init();
	}

	public void setFont(String fontName, int fontSize) {
		setFont(new Font(fontName, Font.PLAIN, fontSize));
	}

	public ParametersTableModel getTableParametersModel() {
		return (ParametersTableModel) this.getModel();
	}

	private void init() {
		setShowGrid(true);
	}
}