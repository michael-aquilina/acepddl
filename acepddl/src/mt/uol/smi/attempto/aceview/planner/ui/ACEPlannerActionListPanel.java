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
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.log4j.Logger;

import mt.uol.smi.attempto.aceview.planner.ACEPlannerManager;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerActionModel;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerModel;
import mt.uol.smi.attempto.aceview.planner.model.event.ACEPlannerEvent;
import mt.uol.smi.attempto.aceview.planner.model.event.ACEPlannerListener;
import mt.uol.smi.attempto.aceview.planner.model.event.ModelEventType;

public class ACEPlannerActionListPanel extends JPanel {
	private static final Logger logger = Logger.getLogger(ACEPlannerActionListPanel.class);
	private final JComponent actionsPanel = new JPanel(new GridLayout(0, 1));
	private JScrollPane scrollpaneSnippets;
	private List<ACEPlannerActionPanel> panels;
	
	private ACEPlannerListener<ACEPlannerEvent<ModelEventType>> acePlannerManagerListener = new ACEPlannerListener<ACEPlannerEvent<ModelEventType>>() {
		@Override
		public void handleChange(ACEPlannerEvent<ModelEventType> event) {
			if (event.isType(ModelEventType.MODEL_CHANGED) || event.isType(ModelEventType.ACTION_LIST_CHANGED)) {
				logger.info("ACEPlannerActionListPanel caught an event");
				updateActionListModel();
			}
		}
	};

	public ACEPlannerActionListPanel() {
		super();
		this.setLayout(new BorderLayout());
		updateActionListModel();
		scrollpaneSnippets = new JScrollPane(actionsPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.add(scrollpaneSnippets, BorderLayout.CENTER);
		ACEPlannerManager.getInstance().addListener(acePlannerManagerListener);
	}

	public void addAction(ACEPlannerActionModel newAction) {
		ACEPlannerActionPanel actionPanel = new ACEPlannerActionPanel(newAction);
		panels.add(actionPanel);
		actionsPanel.add(actionPanel);
		refresh();
	}

	public ACEPlannerModel getListModel() {
		return ACEPlannerManager.getInstance().getCurrentlyBeingModifiedACEPlannerModel();
	}

	public void updateActionListModel() {
		this.actionsPanel.removeAll();
		panels = new ArrayList<ACEPlannerActionPanel>();
		List<ACEPlannerActionModel> actions = getListModel().getActionListModel().getList();
		Collections.sort(actions, new ACEPlannerActionModelComparator());
		for (ACEPlannerActionModel actionModel : actions) {
			addAction(actionModel);
		}
		refresh();
	}

	public void refresh() {
		actionsPanel.revalidate();
		actionsPanel.repaint();
		if (scrollpaneSnippets != null) {
			this.scrollpaneSnippets.revalidate();
			this.scrollpaneSnippets.repaint();
		}
	}
	
	public List<ACEPlannerActionModel> getACEPlannerActionModels(){
		List<ACEPlannerActionModel> current = new ArrayList<ACEPlannerActionModel>();
		for(ACEPlannerActionPanel panel : panels) {
			current.add(panel.getModel());
		}
		return current;
	}
	
	public void dispose() {
		ACEPlannerManager.getInstance().removeListener(acePlannerManagerListener);
	}

}