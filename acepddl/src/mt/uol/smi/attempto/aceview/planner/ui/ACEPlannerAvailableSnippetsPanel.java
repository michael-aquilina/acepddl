package mt.uol.smi.attempto.aceview.planner.ui;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;

import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerActionModel;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerSelectedSnippet;
import mt.uol.smi.attempto.aceview.planner.model.AvailableSnippetsTableModel;

public class ACEPlannerAvailableSnippetsPanel extends JPanel {

	private static final Logger logger = Logger.getLogger(ACEPlannerAvailableSnippetsPanel.class);

	public ACEPlannerSnippetSelectionTable getTableSnippets() {
		return table;
	}

	public void setTableSnippets(ACEPlannerSnippetSelectionTable tableSnippets) {
		this.table = tableSnippets;
	}

	private ACEPlannerSnippetSelectionTable table;
	private final AvailableSnippetsTableModel snippetsTM;

	public ACEPlannerAvailableSnippetsPanel(ACEPlannerActionModel action) {
		super();
		snippetsTM = new AvailableSnippetsTableModel(action);
		table = new ACEPlannerSnippetSelectionTable(snippetsTM);
		table.setPreferredScrollableViewportSize(new Dimension(300, 70));
		table.setFillsViewportHeight(true);
		table.getSelectionModel().addListSelectionListener(new RowListener());
		table.getColumnModel().getSelectionModel().addListSelectionListener(new ColumnListener());
		setLayout(new GridLayout(1, 1));
		add(new JScrollPane(table));
	}

	private void outputSelection() {
		for (ACEPlannerSelectedSnippet snippet : table.getTableSnippetsModel().getSelectedSnippets()) {
			logger.info(snippet.getSentence() 
					+ (snippet.getAceSnippet().isQuestion() ? " isQuestion" : "") 
					+ (snippet.isEffect() ? " isEffect" : ""));
		}
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
}