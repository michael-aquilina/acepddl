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

package mt.uol.smi.attempto.aceview.planner.ui.view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.log4j.Logger;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;

import ch.uzh.ifi.attempto.aceview.ACETextManager;
import ch.uzh.ifi.attempto.aceview.model.event.ACEViewEvent;
import ch.uzh.ifi.attempto.aceview.model.event.ACEViewListener;
import ch.uzh.ifi.attempto.aceview.model.event.TextEventType;
import ch.uzh.ifi.attempto.aceview.ui.view.AbstractACEViewComponent;
import mt.uol.smi.attempto.aceview.planner.ACEPlannerManager;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerActionModel;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerModel;
import mt.uol.smi.attempto.aceview.planner.model.event.ACEPlannerEvent;
import mt.uol.smi.attempto.aceview.planner.model.event.ACEPlannerListener;
import mt.uol.smi.attempto.aceview.planner.model.event.ModelEventType;
import mt.uol.smi.attempto.aceview.planner.ui.ACEPlannerActionListPanel;

public class ACEPlannerViewComponent extends AbstractACEViewComponent {
	private final String lblSavePlanner = "Save Planner";
	private final String lblCreatePlanner = "Create Planner";
	private final String lblPleaseWait = "Please wait...";
	private final JCheckBox cbxSearchPlanner = new JCheckBox("Search path");
	private final JButton btnUpdateModel = new JButton(lblCreatePlanner);
	private final JButton btnAddAction = new JButton("Add Action");
	private final JButton btnReloadList = new JButton("Reload List");
	private final JTextField txtPlannerName = new JTextField("Enter name");
	private ACEPlannerActionListPanel actionList;
	protected final JPanel panelButtons = new JPanel();
	private static final Logger logger = Logger.getLogger(ACEPlannerViewComponent.class);

	private final ACEViewListener<ACEViewEvent<TextEventType>> aceTextManagerListener = new ACEViewListener<ACEViewEvent<TextEventType>>() {
		@Override
		public void handleChange(ACEViewEvent<TextEventType> event) {
			if (event.isType(TextEventType.ACETEXT_CHANGED)) {
				checkForPlanner();
			}
		}
	};

	private final OWLModelManagerListener listener = new OWLModelManagerListener() {
		@Override
		public void handleChange(OWLModelManagerChangeEvent event) {
			if (event.isType(EventType.ACTIVE_ONTOLOGY_CHANGED)) {
				checkForPlanner();
				refreshView();
			}
		}
	};

	private ACEPlannerListener<ACEPlannerEvent<ModelEventType>> acePlannerManagerListener = new ACEPlannerListener<ACEPlannerEvent<ModelEventType>>() {
		@Override
		public void handleChange(ACEPlannerEvent<ModelEventType> event) {
			if (event.isType(ModelEventType.MODEL_CHANGED)) {
				logger.info("ACEPlannerView caught and event");
				checkForPlanner();
				refreshView();
			}
		}
	};

	private void refreshView() {
		logger.info("Refresing view");
		ACEPlannerModel currentModel = ACEPlannerManager.getInstance().getCurrentlyBeingModifiedACEPlannerModel();
		txtPlannerName.setText(currentModel.getName());
		checkForPlanner();
		actionList.refresh();
		refresh();
		logger.info("***View refreshed***");
	}

	private void refresh() {
		actionList.refresh();
		this.revalidate();
		this.repaint();
	}

	@Override
	public void disposeView() {
		removeHierarchyListener(hierarchyListener);
		ACEPlannerManager.getInstance().removeListener(acePlannerManagerListener);
		ACETextManager.getInstance().removeListener(aceTextManagerListener);
		getOWLModelManager().removeListener(listener);
		this.actionList.dispose();
	}

	@Override
	public void initialiseView() throws Exception {

		cbxSearchPlanner.setSelected(false);
		cbxSearchPlanner.setToolTipText("Build a Planner with the given question and answers.");
		cbxSearchPlanner.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateView();
			}
		});

		btnAddAction.setEnabled(true);
		btnAddAction.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				logger.info("adding new action");
				btnAddAction.setEnabled(false);
				ACEPlannerManager.getInstance().addAction();
				btnAddAction.setEnabled(true);
				logger.info("action added");
			}
		});
		btnUpdateModel.setEnabled(true);
		btnUpdateModel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				btnUpdateModel.setEnabled(false);
				txtPlannerName.setEnabled(false);
				setPleaseWait(btnUpdateModel);
				String name = txtPlannerName.getText();
				logger.info("Name is " + name);
				ACEPlannerManager.getInstance().getCurrentlyBeingModifiedACEPlannerModel().setName(name);

				// Update actions
				List<ACEPlannerActionModel> current = new ArrayList<ACEPlannerActionModel>();
				for (ACEPlannerActionModel actionModel : actionList.getACEPlannerActionModels()) {
					current.add(actionModel);
				}
				ACEPlannerManager.getInstance().getCurrentlyBeingModifiedACEPlannerModel().getActionListModel().setList(current);
				ACEPlannerManager.getInstance().updateModel();
				setOk(btnUpdateModel, lblSavePlanner);
				txtPlannerName.setEnabled(true);
				btnUpdateModel.setEnabled(true);
			}

			private void setPleaseWait(JButton button) {
				button.setEnabled(false);
				button.setText(lblPleaseWait);
			}

			private void setOk(JButton button, String text) {
				button.setEnabled(true);
				button.setText(text);
			}
		});
		btnUpdateModel.setText(lblCreatePlanner);

		btnReloadList.setEnabled(true);
		btnReloadList.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ACEPlannerManager.getInstance().reloadProblemDomain();
			}
		});

		panelButtons.setLayout(new BoxLayout(panelButtons, BoxLayout.LINE_AXIS));
		panelButtons.add(btnAddAction);
		panelButtons.add(btnReloadList);
		panelButtons.add(btnUpdateModel);
		panelButtons.add(txtPlannerName);
		actionList = new ACEPlannerActionListPanel();
		setLayout(new BorderLayout());
		JScrollPane scrolledActionList = new JScrollPane(actionList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		add(panelButtons, BorderLayout.NORTH);
		add(scrolledActionList, BorderLayout.CENTER);

		refreshView();

		txtPlannerName.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				settingPlannerName(txtPlannerName.getText());
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				settingPlannerName(txtPlannerName.getText());
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				settingPlannerName(txtPlannerName.getText());
			}
		});
		addHierarchyListener(hierarchyListener);
		ACEPlannerManager.getInstance().addListener(acePlannerManagerListener);
		ACETextManager.getInstance().addListener(aceTextManagerListener);
		getOWLModelManager().addListener(listener);

	}

	private void settingPlannerName(String value) {
		logger.info("Setting name to " + value);
		getPlannerModel().setName(value);
	}

	private void checkForPlanner() {
		boolean acePlannerFound = ACEPlannerManager.getInstance().isPlannerDataExists();
		boolean PlannerLoaded = ACEPlannerManager.getInstance().isSavedPlannerLoaded();
		cbxSearchPlanner.setVisible(acePlannerFound && PlannerLoaded);
		btnAddAction.setVisible(acePlannerFound && PlannerLoaded);
		btnReloadList.setVisible(acePlannerFound || (acePlannerFound && !PlannerLoaded));
		btnUpdateModel.setVisible(!(acePlannerFound) || PlannerLoaded);
		if (!acePlannerFound) {
			btnUpdateModel.setText(lblCreatePlanner);
		} else {
			btnUpdateModel.setText(lblSavePlanner);
		}
		refresh();
	}

	@Override
	protected OWLObject updateView() {
		OWLEntity entity = getOWLWorkspace().getOWLSelectionModel().getSelectedEntity();
		return entity;
	}

	public ACEPlannerModel getPlannerModel() {
		return ACEPlannerManager.getInstance().getCurrentlyBeingModifiedACEPlannerModel();
	}

}