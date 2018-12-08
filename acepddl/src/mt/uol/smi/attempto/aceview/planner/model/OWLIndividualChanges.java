package mt.uol.smi.attempto.aceview.planner.model;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntologyChange;

public class OWLIndividualChanges {
	private OWLIndividual individual;
	private Set<OWLOntologyChange> changes;

	public OWLIndividualChanges(OWLIndividual individual, Set<OWLOntologyChange> changes) {
		super();
		this.individual = individual;
		this.changes = changes;
	}

	public OWLIndividual getIndividual() {
		return individual;
	}

	public void setIndividual(OWLIndividual individual) {
		this.individual = individual;
	}

	public Set<OWLOntologyChange> getChanges() {
		return changes;
	}

	public void setChanges(Set<OWLOntologyChange> changes) {
		this.changes = changes;
	}
}