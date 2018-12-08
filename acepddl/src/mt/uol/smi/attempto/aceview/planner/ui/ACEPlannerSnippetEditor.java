package mt.uol.smi.attempto.aceview.planner.ui;

import javax.swing.JTextArea;

import ch.uzh.ifi.attempto.aceview.lexicon.Autocompleter;
import ch.uzh.ifi.attempto.aceview.ui.SnippetAutocompleter;

public class ACEPlannerSnippetEditor extends JTextArea {

	private SnippetAutocompleter snippetAutocompleter;

	public ACEPlannerSnippetEditor() {
		init();
	}

	public ACEPlannerSnippetEditor(int rows, int columns) {
		super(rows, columns);
		init();
	}

	public void setAutocompleter(Autocompleter ac) {
		if (snippetAutocompleter == null) {
			snippetAutocompleter = new SnippetAutocompleter(this, ac);
		} else {
			snippetAutocompleter.setAutocompleter(ac);
		}
	}

	private void init() {
		setEnabled(true);
		setEditable(true);
		setTabSize(2);
		setLineWrap(true);
		setWrapStyleWord(true);
	}
}