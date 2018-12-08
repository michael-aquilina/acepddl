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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import org.apache.log4j.Logger;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;

import ch.uzh.ifi.attempto.ace.ACESentence;
import ch.uzh.ifi.attempto.ace.ACESplitter;
import ch.uzh.ifi.attempto.aceview.ACESnippet;
import ch.uzh.ifi.attempto.aceview.ACETextManager;
import ch.uzh.ifi.attempto.aceview.ui.util.ComponentFactory;
import mt.uol.smi.attempto.aceview.planner.ACEPlannerManager;
import mt.uol.smi.attempto.aceview.planner.ACEPlannerRunner;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerProblemModel;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerModel;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerSnippet;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerSnippetsUsedModel;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerSolutionModel;
import mt.uol.smi.attempto.aceview.planner.model.event.ACEPlannerEvent;
import mt.uol.smi.attempto.aceview.planner.model.event.ACEPlannerListener;
import mt.uol.smi.attempto.aceview.planner.model.event.ModelEventType;
import mt.uol.smi.attempto.aceview.planner.ui.ACEPlannerSnippetEditor;

/**
 * <p>
 * This view component shows the ACE text in a simple text area which can be
 * edited to update the text. A double newline separates the snippets.
 * </p>
 * 
 * @author Kaarel Kaljurand
 */
public class ACEPlannerTextViewComponent extends AbstractOWLViewComponent {

	private static final Logger logger = Logger.getLogger(ACEPlannerTextViewComponent.class);

	private final ACEPlannerSnippetEditor txtDomain = new ACEPlannerSnippetEditor(25, 30);
	private final ACEPlannerSnippetEditor txtSnippetsUsed = new ACEPlannerSnippetEditor(25, 30);
	private final ACEPlannerSnippetEditor txtGoal = new ACEPlannerSnippetEditor(25, 30);
	private final ACEPlannerSnippetEditor txtSolution = new ACEPlannerSnippetEditor(25, 30);
	private final JLabel labelMessage = new JLabel();
	private final JButton btnUpdate = ComponentFactory.makeButton("Update Data");
	private final JButton btnRun = ComponentFactory.makeButton("Find Solution");

	private ACEPlannerListener<ACEPlannerEvent<ModelEventType>> acePlannerManagerListener = new ACEPlannerListener<ACEPlannerEvent<ModelEventType>>() {
		@Override
		public void handleChange(ACEPlannerEvent<ModelEventType> event) {
			if (event.isType(ModelEventType.MODEL_CHANGED)) {
				logger.info("ACEPlannerView caught and event");
				checkStates();
				refreshView();
			} else if (event.isType(ModelEventType.NEW_SNIPPETS_LOADED)) {
				updateModifiedModelWithTextWithOutSave();
				ACEPlannerManager.getInstance().updateModel();
				ACEPlannerManager.getInstance().fireEvent(ModelEventType.MODEL_CHANGED);
			}
		}
	};
	
	private void updateModifiedModelWithTextWithOutSave() {
		List<List<ACESentence>> textareaSentenceLists = ACESplitter.getParagraphs(txtDomain.getText());
		ACEPlannerManager.getInstance().getCurrentlyBeingModifiedACEPlannerModel().getProblem().getSnippets()
				.clear();
		for (List<ACESentence> sentences : textareaSentenceLists) {
			if (sentences != null) {
				List<ACESnippet> snippets = ACEPlannerManager.getInstance().getSnippetMissing(sentences);
				for (ACESnippet snippet : snippets) {
					ACEPlannerManager.getInstance().getCurrentlyBeingModifiedACEPlannerModel().getProblem()
							.getSnippets().add(new ACEPlannerSnippet(snippet));
				}
			}
		}
		textareaSentenceLists = ACESplitter.getParagraphs(txtSnippetsUsed.getText());
		ACEPlannerManager.getInstance().getCurrentlyBeingModifiedACEPlannerModel().getSentencesUsed()
				.getSnippets().clear();
		for (List<ACESentence> sentences : textareaSentenceLists) {
			if (sentences != null) {
				List<ACESnippet> snippets = ACEPlannerManager.getInstance().getSnippetMissing(sentences);
				for (ACESnippet snippet : snippets) {
					ACEPlannerManager.getInstance().getCurrentlyBeingModifiedACEPlannerModel()
							.getSentencesUsed().getSnippets().add(new ACEPlannerSnippet(snippet));
				}
			}
		}
		textareaSentenceLists = ACESplitter.getParagraphs(txtGoal.getText());
		ACEPlannerManager.getInstance().getCurrentlyBeingModifiedACEPlannerModel().getGoal().getSnippets()
				.clear();
		for (List<ACESentence> sentences : textareaSentenceLists) {
			if (sentences != null) {
				List<ACESnippet> snippets = ACEPlannerManager.getInstance().getSnippetMissing(sentences);
				for (ACESnippet snippet : snippets) {
					ACEPlannerManager.getInstance().getCurrentlyBeingModifiedACEPlannerModel().getGoal()
							.getSnippets().add(new ACEPlannerSnippet(snippet));
				}
			}
		}
	}

	private final OWLModelManagerListener listener = new OWLModelManagerListener() {
		@Override
		public void handleChange(OWLModelManagerChangeEvent event) {
			if (event.isType(EventType.ACTIVE_ONTOLOGY_CHANGED)) {
				checkStates();
				refreshView();
			}
		}
	};

	private void checkStates() {
		boolean acePlannerFound = ACEPlannerManager.getInstance().isPlannerDataExists();
		boolean PlannerLoaded = ACEPlannerManager.getInstance().isSavedPlannerLoaded();
		btnUpdate.setEnabled(acePlannerFound && PlannerLoaded);
		btnRun.setEnabled(acePlannerFound && PlannerLoaded);
		refresh();
	}

	private void refresh() {
		this.revalidate();
		this.repaint();
	}

	private void refreshView() {
		logger.info("Refresing text view");
		ACEPlannerModel currentModel = ACEPlannerManager.getInstance().getCurrentlyBeingModifiedACEPlannerModel();

		txtDomain.setText(currentModel.getProblem().getText());
		txtSnippetsUsed.setText(currentModel.getSentencesUsed().getText());
		txtGoal.setText(currentModel.getGoal().getText());
		checkStates();
		refresh();
		logger.info("***text view refreshed***");
	}

	public ACEPlannerSnippetEditor getSolutionSnippetEditor() {
		return txtSolution;
	}

	@Override
	protected void disposeOWLView() {
		ACEPlannerManager.getInstance().removeListener(acePlannerManagerListener);
		getOWLModelManager().removeListener(listener);
	}

	@Override
	protected void initialiseOWLView() throws Exception {

		btnUpdate.setToolTipText("Update the knowledge base on the basis of the changes done in the textarea.");
		btnUpdate.setMnemonic(KeyEvent.VK_ENTER);
		btnRun.setToolTipText("Find a solution with the actions given the domain and goal");

		btnRun.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				logger.info("Running called...");
				getSolutionSnippetEditor().setText("Searching...");
				refresh();
				ACEPlannerSolutionModel solution = ACEPlannerRunner.getInstance().getSolution(
						ACEPlannerManager.getInstance().getCurrentlySavedACEPlannerModel(),
						ACEPlannerManager.getInstance().getModelManager(),
						ACEPlannerManager.getInstance().getDataFactory());
				logger.info("Running finished! " + solution);
				getSolutionSnippetEditor().setText(solution.toString());
			}
		});

		btnUpdate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				btnUpdate.setEnabled(false);

				logger.info("Updating active domain and usedsnippets.");

				updateModifiedModelWithTextWithOutSave();
				List<String> list = new ArrayList<String>();
				list.add(txtDomain.getText());
				list.add(txtSnippetsUsed.getText());
				list.add(txtGoal.getText());
				ACEPlannerManager.getInstance().updateACEText(list, ModelEventType.NEW_SNIPPETS_LOADED);
			}
		});

		txtDomain.setAutocompleter(ACETextManager.getInstance().getActiveACELexicon().getAutocompleter());
		txtSnippetsUsed.setAutocompleter(ACETextManager.getInstance().getActiveACELexicon().getAutocompleter());
		txtGoal.setAutocompleter(ACETextManager.getInstance().getActiveACELexicon().getAutocompleter());

		JPanel pnlDomain = new JPanel(new BorderLayout());
		JPanel pnlSnippetsUsed = new JPanel(new BorderLayout());
		JPanel pnlGoal = new JPanel(new BorderLayout());
		JPanel pnlSolution = new JPanel(new BorderLayout());

		JLabel lblDomain = new JLabel("Domain");
		JLabel lblSnippetsUsed = new JLabel("Snippets Used");
		JLabel lblGoal = new JLabel("Goal");
		JLabel lblSolution = new JLabel("Solution");

		JScrollPane scrollpaneDomain = new JScrollPane(txtDomain, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		JScrollPane scrollpaneSnippetsUsed = new JScrollPane(txtSnippetsUsed, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		JScrollPane scrollpaneGoal = new JScrollPane(txtGoal, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		JScrollPane scrollpaneSolution = new JScrollPane(txtSolution, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		pnlDomain.add(lblDomain, BorderLayout.NORTH);
		pnlDomain.add(scrollpaneDomain, BorderLayout.CENTER);

		pnlSnippetsUsed.add(lblSnippetsUsed, BorderLayout.NORTH);
		pnlSnippetsUsed.add(scrollpaneSnippetsUsed, BorderLayout.CENTER);

		pnlGoal.add(lblGoal, BorderLayout.NORTH);
		pnlGoal.add(scrollpaneGoal, BorderLayout.CENTER);

		pnlSolution.add(lblSolution, BorderLayout.NORTH);
		pnlSolution.add(scrollpaneSolution, BorderLayout.CENTER);

		JPanel pnlMain = new JPanel(new GridLayout(2, 2));
		pnlMain.add(pnlDomain);
		pnlMain.add(pnlSnippetsUsed);
		pnlMain.add(pnlGoal);
		pnlMain.add(pnlSolution);

		Box panelButtonAndLabel = new Box(BoxLayout.X_AXIS);
		panelButtonAndLabel.add(btnUpdate);
		panelButtonAndLabel.add(btnRun);

		// Note: Glue does not seem to work with labels that contain HTML
		panelButtonAndLabel.add(Box.createHorizontalGlue());
		panelButtonAndLabel.add(labelMessage);
		panelButtonAndLabel.add(Box.createHorizontalGlue());

		setLayout(new BorderLayout());
		add(pnlMain, BorderLayout.CENTER);
		add(panelButtonAndLabel, BorderLayout.NORTH);

		refreshView();
		ACEPlannerManager.getInstance().addListener(acePlannerManagerListener);
		getOWLModelManager().addListener(listener);
	}

}