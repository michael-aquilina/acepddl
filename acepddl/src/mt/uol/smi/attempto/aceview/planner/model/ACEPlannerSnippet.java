package mt.uol.smi.attempto.aceview.planner.model;

import java.util.Comparator;

import ch.uzh.ifi.attempto.ace.ACESentence;
import ch.uzh.ifi.attempto.aceview.ACESnippet;
import ch.uzh.ifi.attempto.aceview.ACESnippetImpl;

public class ACEPlannerSnippet extends ACEPlannerObject {
	protected ACESnippet aceSnippet;

	public ACEPlannerSnippet(ACESnippet aceSnippet) {
		super();
		this.aceSnippet = aceSnippet;
	}

	public ACEPlannerSnippet(String id, ACESnippet aceSnippet) {
		super(id);
		this.aceSnippet = aceSnippet;
	}

	public ACEPlannerSnippet(ACEPlannerSnippet a) {
		super(a);
		if (a.getAceSnippet() == null) {
			this.aceSnippet = null;
		} else if (a.getAceSnippet().getAxiom() == null) {
			this.aceSnippet = new ACESnippetImpl(a.getAceSnippet().getDefaultNamespace(),
					a.getAceSnippet().getSentences());
		} else {
			this.aceSnippet = new ACESnippetImpl(a.getAceSnippet().getDefaultNamespace(), a.getSentence(),
					a.getAceSnippet().getAxiom());
		}
	}

	public ACEPlannerSnippet() {
		super();
	}

	public ACESnippet getAceSnippet() {
		return aceSnippet;
	}

	public void setAceSnippet(ACESnippet aceSnippet) {
		this.aceSnippet = aceSnippet;
	}

	public String getSentence() {
		String sentence = "";
		try {
			if (this.aceSnippet != null && this.aceSnippet.getSentences() != null
					&& this.aceSnippet.getSentences().size() > 0) {
				for (ACESentence snippetSentence : this.aceSnippet.getSentences()) {
					if (snippetSentence != null && snippetSentence.toSimpleString() != null) {
						sentence += snippetSentence.toSimpleString();
					}
				}
			}
		} catch (NullPointerException ex) {
			// do nothing
		}
		return sentence;
	}

	@Override
	public String toString() {
		return "ACEPlannerSnippet -> " + getSentence();
	}

	public boolean isEqual(ACEPlannerSnippet toCompare) {
		if (toCompare == null) {
			return false;
		}
		if (toCompare.getAceSnippet() == null) {
			return false;
		}
		if (this.getAceSnippet() == null) {
			return false;
		}
		return this.getAceSnippet().isEqual(toCompare.getAceSnippet());
	}

}
