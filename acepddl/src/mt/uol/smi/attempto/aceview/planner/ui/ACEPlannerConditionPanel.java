package mt.uol.smi.attempto.aceview.planner.ui;

/*
 * This file is part of ACE View.
 * Copyright 2008-2009, Attempto Group, University of Zurich (see http://attempto.ifi.uzh.ch).
 *
 * ACE View is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * ACE View is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with ACE View.
 * If not, see http://www.gnu.org/licenses/.
 */

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;

import mt.uol.smi.attempto.aceview.planner.ACEPlannerManager;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerActionModel;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerConditionModel;

/**
 * <p>
 * This view component contains a table with all the questions in the ACE text,
 * and a pane where the answers to the selected question are shown.
 * </p>
 * 
 * @author Kaarel Kaljurand
 */
public class ACEPlannerConditionPanel extends JPanel {
	private static final Logger logger = Logger.getLogger(ACEPlannerConditionPanel.class);
	private ACEPlannerActionModel actionModel;
	private ACEPlannerConditionModel conditionModel = new ACEPlannerConditionModel();
	private final JButton btnSaveCondition = new JButton("Save Condition");
	protected final JPanel panelButtons = new JPanel();
	protected final JLabel conditionSentence = new JLabel("");
	protected ACEPlannerParametersPanel parametersPanel;

	public ACEPlannerConditionPanel(ACEPlannerActionModel actionModel, ACEPlannerConditionModel conditionModel) {
		this.actionModel = actionModel;
		this.conditionModel = conditionModel;
		this.parametersPanel = new ACEPlannerParametersPanel(conditionModel);
		setLayout(new BorderLayout());
		btnSaveCondition.setEnabled(true);
		btnSaveCondition.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				logger.info("Saving condition");
//				ACEPlannerManager.getInstance().updateActionFromModel(getActionModel());
				logger.info("condition save");
			}
		});
		panelButtons.setLayout(new BoxLayout(panelButtons, BoxLayout.LINE_AXIS));
		panelButtons.add(btnSaveCondition);
		panelButtons.add(conditionSentence);
		parametersPanel.getTable().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) {
					return;
				}
				logger.info("Parameters row change");
			}
		});
		add(panelButtons, BorderLayout.NORTH);
		add(new JScrollPane(parametersPanel), BorderLayout.CENTER);
		init(conditionModel);
	}

	public void init(ACEPlannerConditionModel conditionModel) {
		if (conditionModel != null) {
			this.conditionModel = conditionModel;
			this.conditionSentence.setText(this.conditionModel.getSentence());
		}
	}

	public void refresh() {
		if (parametersPanel != null) {
			this.parametersPanel.refresh();
		}
		this.revalidate();
		this.repaint();
	}

	public ACEPlannerActionModel getActionModel() {
		return actionModel;
	}

	public void setActionModel(ACEPlannerActionModel actionModel) {
		this.actionModel = actionModel;
	}

	public ACEPlannerConditionModel getConditionModel() {
		return conditionModel;
	}

	public void setConditionModel(ACEPlannerConditionModel conditionModel) {
		this.conditionModel = conditionModel;
	}
}