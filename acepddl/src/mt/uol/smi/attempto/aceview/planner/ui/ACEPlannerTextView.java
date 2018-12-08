package mt.uol.smi.attempto.aceview.planner.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import org.apache.log4j.Logger;
import org.protege.editor.core.ProtegeApplication;
import org.protege.editor.core.ui.progress.BackgroundTask;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import com.google.common.collect.Sets;

import ch.uzh.ifi.attempto.ace.ACESentence;
import ch.uzh.ifi.attempto.ace.ACESplitter;
import ch.uzh.ifi.attempto.aceview.ACESnippet;
import ch.uzh.ifi.attempto.aceview.ACEText;
import ch.uzh.ifi.attempto.aceview.ACETextManager;
import ch.uzh.ifi.attempto.aceview.model.event.ACEViewEvent;
import ch.uzh.ifi.attempto.aceview.model.event.ACEViewListener;
import ch.uzh.ifi.attempto.aceview.model.event.TextEventType;
import ch.uzh.ifi.attempto.aceview.ui.ACESnippetEditor;
import ch.uzh.ifi.attempto.aceview.ui.util.ComponentFactory;

public class ACEPlannerTextView extends AbstractOWLViewComponent {

	private static final Logger logger = Logger.getLogger(ACEPlannerTextView.class);

	private final ACESnippetEditor aceTextArea = new ACESnippetEditor(25, 80);
	private final JLabel labelMessage = new JLabel();
	private final JButton buttonUpdate = ComponentFactory.makeButton("Update");

	private final ACEViewListener<ACEViewEvent<TextEventType>> aceTextManagerListener = new ACEViewListener<ACEViewEvent<TextEventType>>() {
		@Override
		public void handleChange(ACEViewEvent<TextEventType> event) {
			if (event.isType(TextEventType.ACTIVE_ACETEXT_CHANGED)) {
				aceTextArea.setAutocompleter(ACETextManager.getInstance().getActiveACELexicon().getAutocompleter());
			}
			showText();
		}
	};

	@Override
	protected void disposeOWLView() {
		ACETextManager.getInstance().removeListener(aceTextManagerListener);
	}

	@Override
	protected void initialiseOWLView() throws Exception {

		buttonUpdate.setToolTipText("Update the knowledge base on the basis of the changes done in the textarea.");
		buttonUpdate.setMnemonic(KeyEvent.VK_ENTER);

		buttonUpdate.addActionListener(new ActionListener() {
			/**
			 * <p>
			 * Updates the active ACE text on the basis of the textarea.
			 * </p>
			 * 
			 * <ol>
			 * <li>Checks if an existing snippet includes a sentence from the textarea.
			 * These textarea sentences that are not included by any snippet are collected
			 * into <code>addedSentences</code>.</li>
			 * <li>Check if an existing snippet contains a sentence that the textarea does
			 * not contain. Such sentences are collected into
			 * <code>removedSentences</code>.</li>
			 * <li><code>addedSentences</code> are added to the current text and
			 * <code>removedSentences</code> are removed.</li>
			 * </ol>
			 * 
			 * BUG: Does the order of add/delete matter (semantically and performancewise)?
			 */
			@Override
			public void actionPerformed(ActionEvent e) {

				buttonUpdate.setEnabled(false);

				ACEText<OWLEntity, OWLLogicalAxiom> acetext = ACETextManager.getInstance().getActiveACEText();

				logger.info("Updating active knowledge base.");

				final Set<List<ACESentence>> newSentenceLists = new LinkedHashSet<List<ACESentence>>();
				final Set<ACESnippet> removedSnippets = Sets.newHashSet();
				Set<List<ACESentence>> oldSentenceLists = Sets.newHashSet();

				List<List<ACESentence>> textareaSentenceLists = ACESplitter.getParagraphs(aceTextArea.getText());
				for (List<ACESentence> sentences : textareaSentenceLists) {
					if (acetext.contains(sentences)) {
						oldSentenceLists.add(sentences);
					} else {
						newSentenceLists.add(sentences);
					}
				}

				logger.info("Add: " + newSentenceLists);

				for (ACESnippet s : acetext.getSnippets()) {
					if (!oldSentenceLists.contains(s.getSentences())) {
						removedSnippets.add(s);
					}
				}

				logger.info("Del: " + removedSnippets);

				displayMessage("Adding " + newSentenceLists.size() + " and deleting " + removedSnippets.size() + " snippet(s)");

				updateActiveACEText(newSentenceLists, removedSnippets);
			}
		});

		aceTextArea.setAutocompleter(ACETextManager.getInstance().getActiveACELexicon().getAutocompleter());

		JScrollPane scrollpaneAce = new JScrollPane(aceTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		Box panelButtonAndLabel = new Box(BoxLayout.X_AXIS);
		panelButtonAndLabel.add(buttonUpdate);
		// Note: Glue does not seem to work with labels that contain HTML
		panelButtonAndLabel.add(Box.createHorizontalGlue());
		panelButtonAndLabel.add(labelMessage);
		panelButtonAndLabel.add(Box.createHorizontalGlue());

		setLayout(new BorderLayout());
		add(scrollpaneAce, BorderLayout.CENTER);
		add(panelButtonAndLabel, BorderLayout.NORTH);

		ACETextManager.getInstance().addListener(aceTextManagerListener);
		showText();
	}

	private void showText() {
		ACEText acetext = ACETextManager.getInstance().getActiveACEText();
		int numberOfSnippets = acetext.size();
		if (numberOfSnippets == 1) {
			setHeaderText("1 snippet");
		} else {
			setHeaderText(numberOfSnippets + " snippets");
		}
		aceTextArea.setText(acetext.toString());
	}

	private void displayMessage(String message) {
		labelMessage.setText(message);
		labelMessage.validate();
	}

	/**
	 * Can this task be stopped somehow?
	 * 
	 * @param addedSentenceLists
	 * @param removedSnippets
	 */
	private void updateActiveACEText(final Set<List<ACESentence>> addedSentenceLists, final Set<ACESnippet> removedSnippets) {
		final BackgroundTask task = ProtegeApplication.getBackgroundTaskManager().startTask("updating the active ACE text");

		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				Date dateBegin = new Date();
				ACETextManager.getInstance().addAndRemoveItems(addedSentenceLists, removedSnippets);
				Date dateEnd = new Date();
				double duration = (dateEnd.getTime() - dateBegin.getTime()) / 1000;

				ProtegeApplication.getBackgroundTaskManager().endTask(task);

				displayMessage("Updated in " + duration + " seconds");
				aceTextArea.requestFocusInWindow();
				buttonUpdate.setEnabled(true);
			}
		};
		Thread t = new Thread(runnable, "Update the active ACE text");
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
	}
}
