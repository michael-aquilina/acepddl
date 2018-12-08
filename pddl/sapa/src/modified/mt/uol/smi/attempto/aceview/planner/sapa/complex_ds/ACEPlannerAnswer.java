package mt.uol.smi.attempto.aceview.planner.sapa.complex_ds;

import java.awt.HeadlessException;
import java.util.Set;

import org.apache.log4j.Logger;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.IndividualNodeSetPolicy;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import com.google.common.collect.Sets;

import ch.uzh.ifi.attempto.aceview.ACESnippet;
import ch.uzh.ifi.attempto.aceview.util.NodeComparator;
import ch.uzh.ifi.attempto.aceview.util.Showing;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerSnippet;

public class ACEPlannerAnswer {

	private static final Logger logger = Logger.getLogger(ACEPlannerAnswer.class);

	// TODO: make sure that individuals are returned so that sameAs-individuals
	// are in the same node

	// Set of all the entities that occur as answers
	private Set<OWLEntity> entities = Sets.newHashSet();
	private Set<Node<OWLClass>> subClasses = Sets.newTreeSet(new NodeComparator());
	private Set<Node<OWLClass>> superClasses = Sets.newTreeSet(new NodeComparator());
	private Set<Node<OWLNamedIndividual>> individuals = Sets.newTreeSet(new NodeComparator());

	private boolean isSatisfiable = true;
	private boolean isIndividualAnswersComplete = false;
	private boolean isSubClassesAnswersComplete = false;

	private IndividualNodeSetPolicy individualNodeSetPolicy = null;

	public ACEPlannerAnswer(OWLOntologyManager mngr, OWLReasoner reasoner, ACESnippet snippet) {
		OWLClassExpression dlquery = snippet.getDLQuery();
		if (dlquery == null) {
			logger.error("Setting answers to null as dlquery is null : " + snippet.toString());
			setAnswersToNull();
		} else {
			try {
				individualNodeSetPolicy = reasoner.getIndividualNodeSetPolicy();

				if (isSatisfiable(reasoner, dlquery)) {
					logger.trace("ACEPlanner Answer is satisfiable : " + snippet.toString());
					setAnswerLists(mngr, reasoner, dlquery);
				} else {
					isSatisfiable = false;
					logger.trace("ACEPlanner Answer is not satisfiable : " + snippet.toString());
					setAnswersToNull();
				}
				logger.trace("ACEPlanner Answer: " + toString());
			} catch (HeadlessException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean containsEntity(OWLEntity entity) {
		return entities.contains(entity);
	}

	public Set<Node<OWLNamedIndividual>> getIndividuals() {
		return individuals;
	}

	public Set<Node<OWLClass>> getSubClasses() {
		return subClasses;
	}

	public Set<Node<OWLClass>> getSuperClasses() {
		return superClasses;
	}

	public int getIndividualsCount() {
		if (individuals == null) {
			return -1;
		}
		return individuals.size();
	}

	public int getSubClassesCount() {
		if (subClasses == null) {
			return -1;
		}
		return subClasses.size();
	}

	public int getSuperClassesCount() {
		if (superClasses == null) {
			return -1;
		}
		return superClasses.size();
	}

	public boolean isSatisfiable() {
		return isSatisfiable;
	}

	public boolean isIndividualAnswersComplete() {
		return isIndividualAnswersComplete;
	}

	public void setIndividualAnswersComplete(boolean b) {
		isIndividualAnswersComplete = b;
	}

	public boolean isSubClassesAnswersComplete() {
		return isSubClassesAnswersComplete;
	}

	public void setSubClassesAnswersComplete(boolean b) {
		isSubClassesAnswersComplete = b;
	}

	public IndividualNodeSetPolicy getIndividualNodeSetPolicy() {
		return individualNodeSetPolicy;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("==== ANSWER Individuals ==== >> ");
		sb.append("Individuals: " + getIndividualsCount() + ": " + getIndividuals());
		sb.append("<< ================\n");

		return sb.toString();
	}

	private void setAnswersToNull() {
		individuals = null;
		subClasses = null;
		superClasses = null;
		entities.clear();
	}

	private void setAnswerLists(OWLOntologyManager mngr, OWLReasoner reasoner, OWLClassExpression desc) {
		// false gives more answers (but might be slower)
		NodeSet<OWLNamedIndividual> indNodeSet = reasoner.getInstances(desc, false);
		setIndividualsAnswerList(indNodeSet.getNodes());
	}

	private void setIndividualsAnswerList(Set<Node<OWLNamedIndividual>> indNodes) {
		for (Node<OWLNamedIndividual> node : indNodes) {
			individuals.add(node);
			entities.addAll(node.getEntities());
		}
	}

	// TODO: exclude also based on Showing.isShow(node)
	private void setClassAnswerList(Set<Node<OWLClass>> answerList, Set<Node<OWLClass>> classNodes, boolean sub) {
		for (Node<OWLClass> node : classNodes) {
			if (sub && !node.isBottomNode() || !sub && !node.isTopNode()) {
				answerList.add(node);
				entities.addAll(node.getEntities());
			}
		}
	}

	private boolean isCompleteIndividuals(OWLDataFactory df, OWLReasoner reasoner, OWLClassExpression desc,
			Set<OWLNamedIndividual> answers) {
		OWLClassExpression completenessTest = df.getOWLObjectIntersectionOf(desc,
				df.getOWLObjectComplementOf(df.getOWLObjectOneOf(answers)));

		return (!isSatisfiable(reasoner, completenessTest));
	}

	private boolean isCompleteSubClasses(OWLDataFactory df, OWLReasoner reasoner, OWLClassExpression desc,
			Set<OWLClass> answers) {
		OWLClassExpression completenessTest = df.getOWLObjectIntersectionOf(desc,
				df.getOWLObjectComplementOf(df.getOWLObjectUnionOf(answers)));

		return (!isSatisfiable(reasoner, completenessTest));
	}

	private boolean isSatisfiable(OWLReasoner reasoner, OWLClassExpression desc) {
		return reasoner.isSatisfiable(desc);
	}
}