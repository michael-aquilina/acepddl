package mt.uol.smi.attempto.aceview.planner.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Joiner;

import ch.uzh.ifi.attempto.ace.ACESplitter;
import ch.uzh.ifi.attempto.aceview.ACESnippet;

public class AbstractACEPlannerSnippetListModel extends ACEPlannerObject {

	private List<ACEPlannerSnippet> snippets;
	private final Joiner joiner = Joiner.on(" ");

	public AbstractACEPlannerSnippetListModel() {
		super();
		snippets = new ArrayList<ACEPlannerSnippet>();
	}

	public AbstractACEPlannerSnippetListModel(String id) {
		super(id);
		snippets = new ArrayList<ACEPlannerSnippet>();
	}

	public AbstractACEPlannerSnippetListModel(AbstractACEPlannerSnippetListModel m) {
		super(m);
		this.snippets = new ArrayList<ACEPlannerSnippet>();
		for (ACEPlannerSnippet a : m.getSnippets()) {
			this.snippets.add(new ACEPlannerSnippet(a));
		}

	}

	public List<ACEPlannerSnippet> getSnippets() {
		return snippets;
	}

	public void setSnippets(List<ACEPlannerSnippet> snippets) {
		this.snippets = snippets;
	}

	public String getText() {
		String result = "";
		if (snippets != null && snippets.size() > 0) {
			String newLine = System.getProperty("line.separator");
			Collections.sort(snippets, new ACEPlannerSnippetComparator());
			for (ACEPlannerSnippet PlannerSnippet : snippets) {
				result += PlannerSnippet.getSentence() + newLine + newLine;
			}
		}
		return result;
	}

	public boolean hasSnippet(ACEPlannerSnippet toFind) {
		return findSnippet(toFind) != null;
	}

	public ACEPlannerSnippet findSnippet(ACEPlannerSnippet toFind) {
		for (ACEPlannerSnippet snippet : snippets) {
			if (snippet != null && toFind != null && snippet.isEqual(toFind)) {
				return snippet;
			}
		}
		return null;
	}

	public ACEPlannerSnippet findSnippet(ACESnippet toFind) {
		for (ACEPlannerSnippet snippet : snippets) {
			if (snippet != null && toFind != null && snippet.getAceSnippet() != null
					&& snippet.getAceSnippet().isEqual(toFind)) {
				return snippet;
			}
		}
		return null;
	}

	public boolean hasSentencePhrase(String toFind) {
		return findSentencePhrase(toFind) != null;
	}

	public ACEPlannerSnippet findSentencePhrase(String toFind) {
		toFind = joiner.join(ACESplitter.getSentences(toFind));
		for (ACEPlannerSnippet snippet : snippets) {
			if (snippet != null && toFind != null && snippet != null && snippet.getAceSnippet() != null
					&& snippet.getAceSnippet().toString().equals(toFind)) {
				return snippet;
			}
		}
		return null;
	}

	public boolean removeSnippet(ACEPlannerSnippet toRemove) {
		ACEPlannerSnippet snippet = findSnippet(toRemove);
		if (snippet != null) {
			return snippets.remove(snippet);
		}
		return false;
	}

	public boolean addSnippet(ACEPlannerSnippet toAdd) {
		if (!this.hasSnippet(toAdd)) {
			return snippets.add(toAdd);
		}
		return false;
	}
}
