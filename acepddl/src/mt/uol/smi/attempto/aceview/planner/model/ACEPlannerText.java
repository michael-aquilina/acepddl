package mt.uol.smi.attempto.aceview.planner.model;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import com.google.common.collect.Maps;

import ch.uzh.ifi.attempto.aceview.ACEText;
import ch.uzh.ifi.attempto.aceview.ACETextImpl;

public class ACEPlannerText {

	private ACEText<OWLEntity, OWLLogicalAxiom> acetext;

	public ACEPlannerText() {
		acetext = new ACETextImpl();
	}

	public ACEText<OWLEntity, OWLLogicalAxiom> getAceText() {
		return acetext;
	}

	public void setAcetext(ACEText<OWLEntity, OWLLogicalAxiom> acetext) {
		this.acetext = acetext;
	}	
	
	public int size() {
		return this.acetext.getSnippets().size();
	}

}
