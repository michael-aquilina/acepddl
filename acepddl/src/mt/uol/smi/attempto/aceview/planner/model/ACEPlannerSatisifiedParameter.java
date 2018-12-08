package mt.uol.smi.attempto.aceview.planner.model;

import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.reasoner.Node;

public class ACEPlannerSatisifiedParameter extends ACEPlannerParameter {

	private Node<OWLNamedIndividual> satisifiedIndividual;
	private String satisifiedIndividualName;
	
	public ACEPlannerSatisifiedParameter() {
		super();
	}

	public ACEPlannerSatisifiedParameter(ACEPlannerParameter param) {
		super(param);
		this.satisifiedIndividualName = "";
	}
	
	public ACEPlannerSatisifiedParameter(ACEPlannerSatisifiedParameter m) {
		super(m);
		this.parameterName = m.getParameterName();
		this.parameterValue = m.getParameterValue();
		this.satisifiedIndividual = m.getSatisifiedIndividual();
		this.satisifiedIndividualName = m.getSatisifiedIndividualName();
	}

	public Node<OWLNamedIndividual> getSatisifiedIndividual() {
		return satisifiedIndividual;
	}

	public void setSatisifiedIndividual(Node<OWLNamedIndividual> satisifiedIndividual) {
		this.satisifiedIndividual = satisifiedIndividual;
	}

	public String getSatisifiedIndividualName() {
		return satisifiedIndividualName;
	}

	public void setSatisifiedIndividualName(String satisifiedIndividualName) {
		this.satisifiedIndividualName = satisifiedIndividualName;
	}

	@Override
	public String toString() {		
		String result = this.getParameterValue() + " was satisfied by : " + this.satisifiedIndividualName;
		return result;
	}	

	
}
