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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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

/**
 * <p>
 * This view component contains a table with all the questions in the ACE text,
 * and a pane where the answers to the selected question are shown.
 * </p>
 * 
 * @author Kaarel Kaljurand
 */
public class ACEPlannerActionPanel extends JPanel {
	private static final Logger logger = Logger.getLogger(ACEPlannerActionPanel.class);
	private ACEPlannerAvailableSnippetsPanel snippets;
	private ACEPlannerActionModel model;
	private ACEPlannerConditionPanel conditionPanel;
	private ACEPlannerEffectPanel effectsPanel;
	private JPanel details;
	private JSplitPane splitpane;
	private final JButton btnSaveAction = new JButton("Save Action");
	private final JButton btnRemoveAction = new JButton("Remove Action");
	private final JTextField txtActionName = new JTextField("Action");
	protected final JPanel panelButtons = new JPanel();

	TableModelListener tableChangesListener = new TableModelListener() {

		@Override
		public void tableChanged(TableModelEvent e) {
			int row = e.getLastRow();
			logger.info("Edited row is "+ row);
			if (getModel() != null && row >= 0 && row < snippets.getTableSnippets().getTableSnippetsModel().getSnippets().size()) {
				ACEPlannerSelectedSnippet selectedSnippet = snippets.getTableSnippets().getTableSnippetsModel().getSnippets().get(row);
				if (selectedSnippet.isSelected()) {
					if (selectedSnippet.getAceSnippet().isQuestion()) {
						ACEPlannerConditionModel condition = model.findCondition(selectedSnippet.getAceSnippet());
						if (condition == null) {
							logger.info("+++Adding new condition+++");
							condition = new ACEPlannerConditionModel(selectedSnippet);
							model.getConditions().add(condition);
						} else {
							logger.info("***Existing condition***.");
						}
						hideEffectsPanel();
						initConditionsPanel(condition);
					} else {
						initEffectsPanel(getModel());
						hideConditionsPanel();
						ACEPlannerEffectTuple snippet = model.findACEPlannerEffectTuple(selectedSnippet.getAceSnippet());
						if (snippet == null) {
							snippet = new ACEPlannerEffectTuple(selectedSnippet);
							model.getEffects().add(snippet);
							// ACEPlannerManager.getInstance().updateActionFromModel(model);
							logger.info("+++ New effect added +++");
						} else {
							logger.info("*** Existing effect ***.");
						}
					}
				} else {
					hideConditionsPanel();
					hideEffectsPanel();
					if (selectedSnippet.getAceSnippet().isQuestion()) {
						model = ACEPlannerManager.getInstance().removeConditionFromModel(model, selectedSnippet);
					} else {
						boolean result = model.removeEffect(selectedSnippet.getAceSnippet());
						logger.info("---Removed snippet : " + result);
						result = model.removeEffect(selectedSnippet.getAceSnippet());
						logger.info("---Removed effect : " + result);
					}
					hideConditionsPanel();
				}
				output();
			}
		}
	};

	ListSelectionListener selectionListener = new ListSelectionListener() {
		@Override
		public void valueChanged(ListSelectionEvent e) {
			 ListSelectionModel lsm = (ListSelectionModel)e.getSource();
			if (e.getValueIsAdjusting()) {
				return;
			}
			int row = lsm.getMinSelectionIndex();
			logger.info("Selected row is "+ row);
			if (getModel() != null && row >= 0 && row < snippets.getTableSnippets().getTableSnippetsModel().getSnippets().size()) {
				ACEPlannerSelectedSnippet selectedSnippet = snippets.getTableSnippets().getTableSnippetsModel().getSnippets().get(row);
				if (selectedSnippet.isSelected()) {
					if (selectedSnippet.getAceSnippet().isQuestion()) {
						ACEPlannerConditionModel condition = model.findCondition(selectedSnippet.getAceSnippet());
						if (condition == null) {
							logger.info("+++Adding new condition+++");
							condition = new ACEPlannerConditionModel(selectedSnippet);
							model.getConditions().add(condition);
							ACEPlannerManager.getInstance().updateActionFromModel(model);
						} else {
							logger.info("***Existing condition***.");
						}
						initConditionsPanel(condition);
						hideEffectsPanel();
						refresh();
					} else {
						initEffectsPanel(getModel());
						hideConditionsPanel();
						ACEPlannerEffectTuple snippet = model.findACEPlannerEffectTuple(selectedSnippet.getAceSnippet());
						if (snippet == null) {
							snippet = new ACEPlannerEffectTuple(selectedSnippet);
							model.getEffects().add(snippet);
							ACEPlannerManager.getInstance().updateActionFromModel(model);
							logger.info("+++ New effect added +++");
						} else {
							logger.info("*** Existing effect ***.");
						}
					}
				} else {
					hideConditionsPanel();
					hideEffectsPanel();
				}
			}
		}
	};

	public void refresh() {
		this.details.repaint();
		if (conditionPanel != null) {
			this.conditionPanel.refresh();
		}
		if (effectsPanel != null) {
			this.effectsPanel.refresh();
		}
		if (splitpane != null) {
			this.splitpane.repaint();
		}
		this.revalidate();
		this.repaint();
	}

	public ACEPlannerActionPanel(ACEPlannerActionModel model) {
		setModel(model);
	}

	public void hideConditionsPanel() {
		logger.info("Hiding condition panel");
		if (conditionPanel != null) {
			logger.info("Condition panel not null. Removing");
			details.remove(conditionPanel);
			conditionPanel.setVisible(false);
		}
		conditionPanel = null;
		refresh();
	}

	public void hideEffectsPanel() {
		logger.info("Hiding effectsPanel");
		if (effectsPanel != null) {
			logger.info("effectsPanel panel not null. Removing");
			details.remove(effectsPanel);
			effectsPanel.setVisible(false);
		}
		effectsPanel = null;
		refresh();
	}

	public void initConditionsPanel(ACEPlannerConditionModel condition) {
		if (condition == null) {
			return;
		}
		logger.info("Showing condition panel");
		if (conditionPanel != null) {
			logger.info("Removing current condition panel");
			details.remove(conditionPanel);
		}
		conditionPanel = new ACEPlannerConditionPanel(model, condition);
		conditionPanel.setVisible(true);
		details.add(conditionPanel, BorderLayout.CENTER);
		refresh();
	}

	public void initEffectsPanel(ACEPlannerActionModel actionModel) {
		if (actionModel == null) {
			return;
		}
		logger.info("Showing effect panel");
		if (effectsPanel != null) {
			logger.info("Removing current effect panel");
			details.remove(effectsPanel);
		}
		effectsPanel = new ACEPlannerEffectPanel(actionModel);
		effectsPanel.setVisible(true);
		details.add(effectsPanel, BorderLayout.CENTER);
		refresh();
	}

	public void init(ACEPlannerActionModel model) {
		logger.info("+++Initialising action ++");
		txtActionName.setText(model.getActionName());
		snippets = new ACEPlannerAvailableSnippetsPanel(model);
		// ACEPlannerManager.getInstance().updateAction(model);
		details = new JPanel(new BorderLayout());
		initConditionsPanel(null);
		JSplitPane splitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, snippets, details);
		setLayout(new BorderLayout());
		setMinimumSize(new Dimension(10, 500));
		setMaximumSize(new Dimension(10, 500));
		add(splitpane, BorderLayout.CENTER);
		snippets.getTableSnippets().getTableSnippetsModel().addTableModelListener(tableChangesListener);

		snippets.getTableSnippets().getSelectionModel().addListSelectionListener(selectionListener);
		btnSaveAction.setEnabled(true);
		btnSaveAction.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				logger.info("Updating action");
				settingActionName(txtActionName.getText());
				// setModel(ACEPlannerManager.getInstance().updateActionFromModel(getModel()));
				logger.info("action updated");
			}
		});
		btnRemoveAction.setEnabled(true);
		btnRemoveAction.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				logger.info("Removing action");
				ACEPlannerManager.getInstance().removeActionFromModel(getModel());
				logger.info("removed acton");
			}
		});

		txtActionName.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				settingActionName(txtActionName.getText());
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				settingActionName(txtActionName.getText());
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				settingActionName(txtActionName.getText());
			}
		});

		panelButtons.setLayout(new BoxLayout(panelButtons, BoxLayout.LINE_AXIS));
		panelButtons.add(btnSaveAction);
		panelButtons.add(btnRemoveAction);
		panelButtons.add(txtActionName);
		add(panelButtons, BorderLayout.NORTH);
		logger.info("+++Action initialised++" + model.getId());
	}

	private void settingActionName(String value) {
		logger.info("Setting name to " + value);
		getModel().setActionName(value);
	}

	private void output() {
		if (this.model != null) {
			logger.info("\nConditions are:");
			for (ACEPlannerConditionModel condition : this.model.getConditions()) {
				logger.info("Condition " + condition.getAcePlannerSnippet().getAceSnippet());
			}
			logger.info("\nEffects are:");
			for (ACEPlannerEffectTuple tuple : this.model.getEffects()) {
				logger.info("Effect " + tuple.toString());
			}
			logger.info("\n");
		}
	}

	public ACEPlannerAvailableSnippetsPanel getSnippets() {
		return snippets;
	}

	public void setSnippets(ACEPlannerAvailableSnippetsPanel snippets) {
		this.snippets = snippets;
	}

	public ACEPlannerActionModel getModel() {
		return model;
	}

	public void setModel(ACEPlannerActionModel model) {
		this.model = model;
		init(this.model);
	}

}