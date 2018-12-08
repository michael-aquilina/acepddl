package mt.uol.smi.attempto.aceview.planner.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;

import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerConditionModel;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerParameter;
import mt.uol.smi.attempto.aceview.planner.model.ParametersTableModel;

public class ACEPlannerParametersPanel extends JPanel implements ActionListener {

	private static final Logger logger = Logger.getLogger(ACEPlannerParametersPanel.class);

	private ACEPlannerParametersTable table;
	private JCheckBox rowCheck;
	private JCheckBox columnCheck;
	private JCheckBox cellCheck;
	private final JButton btnAddParameter = new JButton("Add Parameter");
	protected final JPanel panelButtons = new JPanel();
	private JScrollPane tableScrollPane;

	private ParametersTableModel snippetsTM = new ParametersTableModel(new ACEPlannerConditionModel());
	private ACEPlannerConditionModel condition;

	public ACEPlannerParametersPanel(ACEPlannerConditionModel condition) {
		super();
		this.condition = condition;
		setCondition(this.condition);
		// only 1 parameter for this current version
		btnAddParameter.setEnabled(getCondition().getParameters().size() == 0);
		btnAddParameter.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getCondition().getParameters().add(new ACEPlannerParameter("Object", "param"+getCondition().getParameters().size()));
				setCondition(getCondition());
				// only 1 parameter for this current version
				btnAddParameter.setEnabled(false);
				table.getTableParametersModel().fireTableDataChanged();
				table.revalidate();
				table.repaint();
			}
		});
		panelButtons.setLayout(new BoxLayout(panelButtons, BoxLayout.LINE_AXIS));
		panelButtons.add(btnAddParameter);
		setLayout(new BorderLayout());
		add(panelButtons, BorderLayout.NORTH);
	}

	public void refresh() {
		if (table != null) {
			setCondition(getCondition());
			table.getTableParametersModel().fireTableDataChanged();
			table.revalidate();
			table.repaint();
		}
		this.revalidate();
		this.repaint();
	}

	public ACEPlannerParametersTable getTable() {
		return table;
	}

	public void setTable(ACEPlannerParametersTable table) {
		this.table = table;
	}

	public ACEPlannerConditionModel getCondition() {
		return condition;
	}

	public void setCondition(ACEPlannerConditionModel condition) {
		this.condition = condition;
		logger.info("Asked to set condition");
		if (this.condition == null) {
			logger.info("Condition is null");
		} else {
			logger.info("Condition is not null");
			snippetsTM.setCondition(condition);
			table = new ACEPlannerParametersTable(snippetsTM);
			table.setPreferredScrollableViewportSize(new Dimension(500, 70));
			table.setFillsViewportHeight(true);
			table.getSelectionModel().addListSelectionListener(new RowListener());
			table.getColumnModel().getSelectionModel().addListSelectionListener(new ColumnListener());
			tableScrollPane = new JScrollPane(table);
			add(tableScrollPane, BorderLayout.CENTER);
			table.getTableParametersModel().fireTableDataChanged();
		}

	}

	@Override
	public void actionPerformed(ActionEvent event) {
		String command = event.getActionCommand();
		// Cell selection is disabled in Multiple Interval Selection
		// mode. The enabled state of cellCheck is a convenient flag
		// for this status.
		if ("Row Selection" == command) {
			table.setRowSelectionAllowed(rowCheck.isSelected());
			// In MIS mode, column selection allowed must be the
			// opposite of row selection allowed.
			if (!cellCheck.isEnabled()) {
				table.setColumnSelectionAllowed(!rowCheck.isSelected());
			}
		} else if ("Column Selection" == command) {
			table.setColumnSelectionAllowed(columnCheck.isSelected());
			// In MIS mode, row selection allowed must be the
			// opposite of column selection allowed.
			if (!cellCheck.isEnabled()) {
				table.setRowSelectionAllowed(!columnCheck.isSelected());
			}
		} else if ("Cell Selection" == command) {
			table.setCellSelectionEnabled(cellCheck.isSelected());
		} else if ("Multiple Interval Selection" == command) {
			table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			// If cell selection is on, turn it off.
			if (cellCheck.isSelected()) {
				cellCheck.setSelected(false);
				table.setCellSelectionEnabled(false);
			}
			// And don't let it be turned back on.
			cellCheck.setEnabled(false);
		} else if ("Single Interval Selection" == command) {
			table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
			// Cell selection is ok in this mode.
			cellCheck.setEnabled(true);
		} else if ("Single Selection" == command) {
			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			// Cell selection is ok in this mode.
			cellCheck.setEnabled(true);
		}

		// Update checkboxes to reflect selection mode side effects.
		rowCheck.setSelected(table.getRowSelectionAllowed());
		columnCheck.setSelected(table.getColumnSelectionAllowed());
		if (cellCheck.isEnabled()) {
			cellCheck.setSelected(table.getCellSelectionEnabled());
		}
	}

	private void outputSelection() {
		logger.info("\nParam list is now");
		for (ACEPlannerParameter param : table.getTableParametersModel().getParameters()) {
			logger.info(param.toString());
		}
		logger.info(".\n");
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