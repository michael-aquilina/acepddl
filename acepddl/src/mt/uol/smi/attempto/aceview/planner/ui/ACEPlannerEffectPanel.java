package mt.uol.smi.attempto.aceview.planner.ui;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.apache.log4j.Logger;

import mt.uol.smi.attempto.aceview.planner.ACEPlannerManager;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerActionModel;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerConditionModel;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerEffectTuple;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerSelectedSnippet;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerSnippet;
import mt.uol.smi.attempto.aceview.planner.model.EffectSnippetsTableModel;

/**
 * <p>
 * This view component contains a table with all the questions in the ACE text,
 * and a pane where the answers to the selected question are shown.
 * </p>
 * 
 * @author Kaarel Kaljurand
 */
public class ACEPlannerEffectPanel extends JPanel {
	private static final Logger logger = Logger.getLogger(ACEPlannerEffectPanel.class);
	private ACEPlannerActionModel actionModel;

	TableModelListener tableChangesListener = new TableModelListener() {

		@Override
		public void tableChanged(TableModelEvent e) {
			int row = e.getLastRow();
			logger.info("Edited row is " + row);
			if (getActionModel() != null && row >= 0 && row < getTableEffects().getEffectSnippetsTableModel().getActionModel().getEffects().size()) {
				ACEPlannerEffectTuple selectedSnippet = getTableEffects().getEffectSnippetsTableModel().getActionModel().getEffects().get(row);
				getActionModel().findACEPlannerEffectTuple(selectedSnippet.getUnformattedResult().getAceSnippet()).setNegated(selectedSnippet.isNegated());
				output();
			}
		}
	};
	
	private void output() {
		if (getActionModel() != null) {
			logger.info("\nEffects now are:");
			for (ACEPlannerEffectTuple effect : getActionModel().getEffects()) {
				logger.info("Effect " + effect);
			}
			logger.info("\n");
		}
	}

	public ACEPlannerEffectTable getTableEffects() {
		return table;
	}

	public void setTableSnippets(ACEPlannerEffectTable tableSnippets) {
		this.table = tableSnippets;
	}

	private ACEPlannerEffectTable table;
	private final EffectSnippetsTableModel snippetsTM;

	public ACEPlannerEffectPanel(ACEPlannerActionModel actionModel) {
		this.actionModel = actionModel;
		snippetsTM = new EffectSnippetsTableModel(actionModel);
		table = new ACEPlannerEffectTable(snippetsTM);
		table.setPreferredScrollableViewportSize(new Dimension(300, 70));
		table.setFillsViewportHeight(true);
		table.getSelectionModel().addListSelectionListener(new RowListener());
		table.getColumnModel().getSelectionModel().addListSelectionListener(new ColumnListener());
		setLayout(new GridLayout(1, 1));
		add(new JScrollPane(table));
		init(actionModel);
	}

	public void init(ACEPlannerActionModel actionModel) {
		if (actionModel != null) {
			this.actionModel = actionModel;
			this.table.getEffectSnippetsTableModel().setAction(this.actionModel);
		}
	}

	public void refresh() {
		if (table != null) {
			this.table.revalidate();
			this.table.repaint();
		}
		this.revalidate();
		this.repaint();
	}

	private class RowListener implements ListSelectionListener {
		@Override
		public void valueChanged(ListSelectionEvent event) {
			if (event.getValueIsAdjusting()) {
				return;
			}
			logger.info("ROW SELECTION EVENT. ");
			outputSelection();
		}
	}

	private class ColumnListener implements ListSelectionListener {
		@Override
		public void valueChanged(ListSelectionEvent event) {
			if (event.getValueIsAdjusting()) {
				return;
			}
			logger.info("COLUMN SELECTION EVENT. ");
			outputSelection();
		}
	}

	private void outputSelection() {
		for (ACEPlannerEffectTuple snippet : table.getEffectSnippetsTableModel().getActionModel().getEffects()) {
			logger.info(snippet.getUnformattedResult().getSentence() + (snippet.isNegated() ? " Negated" : ""));
		}
	}

	public ACEPlannerActionModel getActionModel() {
		return actionModel;
	}

	public void setActionModel(ACEPlannerActionModel actionModel) {
		this.actionModel = actionModel;
	}
}