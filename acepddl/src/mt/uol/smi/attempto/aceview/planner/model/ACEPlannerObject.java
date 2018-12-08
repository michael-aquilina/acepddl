package mt.uol.smi.attempto.aceview.planner.model;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLIndividual;

public class ACEPlannerObject {

	protected IRI iri;
	protected String id;
	protected OWLIndividual owlIndividual;

	public ACEPlannerObject() {
		this.id = java.util.UUID.randomUUID().toString();
	}

	public ACEPlannerObject(String id) {
		this.id = id;
	}

	public ACEPlannerObject(ACEPlannerObject a) {
		this.id = a.getId();
		this.iri = a.getIri();
		this.owlIndividual = a.getOWLIndividual();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public IRI getIri() {
		return iri;
	}

	public void setIri(IRI iri) {
		this.iri = iri;
	}

	public OWLIndividual getOWLIndividual() {
		return owlIndividual;
	}

	public void setOWLIndividual(OWLIndividual owlIndividual) {
		this.owlIndividual = owlIndividual;
	}

}
