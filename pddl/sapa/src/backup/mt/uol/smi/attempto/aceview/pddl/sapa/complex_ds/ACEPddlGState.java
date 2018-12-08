/********************************************************************
    Author: Minh B. Do -- Arizona State University
*******************************************************************/
package mt.uol.smi.attempto.aceview.pddl.sapa.complex_ds;

import java.util.ArrayList;
import java.util.List;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import ch.uzh.ifi.attempto.aceview.ACEAnswer;
import ch.uzh.ifi.attempto.aceview.ACESnippet;
import ch.uzh.ifi.attempto.aceview.ACEText;
import ch.uzh.ifi.attempto.aceview.ACETextImpl;
import mt.uol.smi.attempto.aceview.pddl.model.ACEPlannerActionModel;
import mt.uol.smi.attempto.aceview.pddl.model.ACEPlannerConditionModel;
import mt.uol.smi.attempto.aceview.pddl.model.ACEPlannerEffectTuple;
import mt.uol.smi.attempto.aceview.pddl.model.ACEPlannerSnippet;
import mt.uol.smi.attempto.aceview.pddl.sapa.basic_ds.Event;

public class ACEPlannerGState {
	private ACEPlannerPredDB acetext;

	public ACEPlannerGState(int num) {
		acetext = new ACEPlannerPredDB();
	}

	public ACEPlannerGState(ACEPlannerGState s) {
		acetext = new ACEPlannerPredDB();
		for (ACESnippet snippet : s.getAcetext().getSnippets()) {
			acetext.add(snippet);
		}
	}

	public ACEText<OWLEntity, OWLLogicalAxiom> getAcetext() {
		return acetext;
	}

	public void setAcetext(ACEPlannerPredDB acetext) {
		this.acetext = acetext;
	}
	public void setAcetext(List<ACEPlannerSnippet> snippets) {
		acetext = new ACEPlannerPredDB();
		for(ACEPlannerSnippet snippet: snippets) {
			acetext.add(snippet.getAceSnippet());
		}		
	}

	/** Check if an action is applicable to this state */
	public boolean applicable(ACEPlannerActionModel a) {

		for (ACEPlannerConditionModel condition : a.getConditions()) {
			// check if there is an answer for each condition.
			// if not, then the action is not satisified and is not applicable
			if (!hasAnswer(condition.getAcePlannerSnippet().getAceSnippet())) {
				return false;
			}
		}
		return true;
	}

	public boolean hasAnswer(ACESnippet question) {
		if (question.hasAxioms()) {
			ACEAnswer answer = acetext.getAnswer(question);
			if (answer == null) {
			} else {
				if (answer.isSatisfiable()) {
					return true;
				}
			}
		}
		return false;
	}

	/** Update a state with changes upon applying an action */
	public void update(ACEPlannerActionModel a) {
		for (ACEPlannerEffectTuple tuple : a.getEffects()) {
			if (tuple.isNegated()) {
				acetext.remove(tuple.getFormattedResult().getAceSnippet());				
			} else {
				acetext.add(tuple.getFormattedResult().getAceSnippet());
			}
		}
	}
	
	public void update(ArrayList aList) {
		int i;
		Event e;
		for (i = 0; i < aList.size(); i++) {
			e = (Event) aList.get(i);
			if (!e.getNeg()) {
				System.out.println("Update not neg from event queue");				
			} else {
				System.out.println("Update from event queue");				
			}
		}
	}

	/** Update a state according to the set of events */
	public void add(ACESnippet newSnippet) {
		acetext.add(newSnippet);
	}
	
	public boolean hasSnippet(ACESnippet snippet) {
		return acetext.contains(snippet);
	}

}