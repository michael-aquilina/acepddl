package mt.uol.smi.attempto.aceview.planner;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.OWLDataFactory;

import com.google.common.collect.Lists;

import ch.uzh.ifi.attempto.aceview.ACETextManager;
import ch.uzh.ifi.attempto.aceview.lexicon.TokenMapper;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerActionModel;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerEffectTuple;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerModel;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerSatisifiedParameter;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerSnippet;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerSolutionModel;
import mt.uol.smi.attempto.aceview.planner.model.event.ACEPlannerEvent;
import mt.uol.smi.attempto.aceview.planner.model.event.ACEPlannerListener;
import mt.uol.smi.attempto.aceview.planner.model.event.ModelEventType;
import mt.uol.smi.attempto.aceview.planner.sapa.ACEPlannerPlanner;
import mt.uol.smi.attempto.aceview.planner.sapa.IPlanner;

public final class ACEPlannerRunner implements IACEPlannerRunner {

	private static ACEPlannerRunner instance;
	private ACEPlannerModel plannerModel;
	private static final Logger logger = Logger.getLogger(ACEPlannerRunner.class);
	private final List<ACEPlannerListener<ACEPlannerEvent<ModelEventType>>> acePlannerChangeListeners = Lists
			.newArrayList();
	private ACEPlannerSolutionModel solution;
	private static IPlanner planner;
	private OWLModelManager modelManager;
	private OWLDataFactory dataFactory;

	private ACEPlannerRunner() {
	}

	public static ACEPlannerRunner getInstance() {
		if (instance == null) {
			instance = new ACEPlannerRunner();
			planner = new ACEPlannerPlanner();
		}
		return instance;
	}

	public void setDomain(ACEPlannerModel model) {
		plannerModel = model;
	}

	@Override
	public ACEPlannerModel getDomain() {
		if (plannerModel == null) {
			logger.info("Initialising PlannerModel");
			plannerModel = new ACEPlannerModel();
		}
		return plannerModel;
	}

	public void addListener(ACEPlannerListener<ACEPlannerEvent<ModelEventType>> listener) {
		acePlannerChangeListeners.add(listener);
	}

	public void removeListener(ACEPlannerListener<ACEPlannerEvent<ModelEventType>> listener) {
		acePlannerChangeListeners.remove(listener);
	}

	public void setSolution(ACEPlannerSolutionModel solution, OWLModelManager modelManager,
			OWLDataFactory dataFactory) {
		this.solution = solution;
	}

	@Override
	public ACEPlannerSolutionModel getSolution(ACEPlannerModel model, OWLModelManager modelManager,
			OWLDataFactory dataFactory) {
		this.plannerModel = model;
		this.modelManager = modelManager;
		this.dataFactory = dataFactory;
		for (ACEPlannerActionModel action : model.getActionListModel().getList()) {
			for (ACEPlannerEffectTuple effect : action.getEffects()) {
				effect.setFormattedResult(new ACEPlannerSnippet());
				effect.setSatisifedParameters(new ArrayList<ACEPlannerSatisifiedParameter>());
			}
		}
		return this.planner.solve(this);
	}

	@Override
	public OWLModelManager getModelManager() {
		return modelManager;
	}

	@Override
	public OWLDataFactory getDataFactory() {
		return dataFactory;
	}

	@Override
	public TokenMapper getAceLexicon() {
		return ACETextManager.getInstance().getActiveACELexicon();
	}

	//
	// public Set<OWLNamedIndividual> showAnswer(ACESnippet question) {
	// if (question.hasAxioms()) {
	// ACEAnswer answer = acetext.getAnswer(question);
	// if (answer == null) {
	// }
	// else {
	// final OWLClassExpression dlquery = question.getDLQuery();
	// if (answer.isSatisfiable()) {
	// logger.info("Showing answers");
	//// showAnswers(dlquery, answer);
	// final Set<Node<OWLNamedIndividual>> individuals = answer.getIndividuals();
	// return renderIndividuals(individuals, dlquery);
	// }
	// }
	// }
	// return new HashSet<OWLNamedIndividual>();
	// }
	//
	// private void addSnippet(ACESnippet snippet) {
	// acetext.add(snippet);
	// // TODO: BUG: we should pick the ontology that corresponds to the
	// // ACE text. This is not always the active ontology.
	// getAddChanges(getACEPlannerActiveOntology(), snippet);
	//// fireEvent(TextEventType.ACETEXT_CHANGED);
	// }
	//
	// private List<? extends OWLAxiomChange> getAddChanges(OWLOntology ontology,
	// ACESnippet snippet) {
	// List<AddAxiomByACEView> changes = Lists.newArrayList();
	// Set<OWLLogicalAxiom> snippetAxioms = snippet.getLogicalAxioms();
	//
	// // If the snippet corresponds to a single axiom, then we
	// // annotate this axiom with ACE View specific annotations.
	// if (snippetAxioms.size() == 1) {
	// OWLLogicalAxiom axiom = snippetAxioms.iterator().next();
	// // OWLLogicalAxiom annotatedAxiom =
	// // annotateAxiomWithSnippet(owlModelManager.getOWLDataFactory(), axiom,
	// // snippet);
	// // changes.add(new AddAxiomByACEView(ontology, annotatedAxiom));
	// changes.add(new AddAxiomByACEView(ontology, axiom));
	// } else {
	// for (OWLLogicalAxiom axiom : snippetAxioms) {
	// changes.add(new AddAxiomByACEView(ontology, axiom));
	// }
	// }
	//
	// return changes;
	// }
	// private void showAnswers(final OWLClassExpression dlquery, final ACESnippet
	// question) {
	//
	// ACEAnswer answer = acetext.getAnswer(question);
	// final Set<Node<OWLNamedIndividual>> individuals = answer.getIndividuals();
	// final Set<Node<OWLClass>> subclasses = answer.getSubClasses();
	// final Set<Node<OWLClass>> superclasses = answer.getSuperClasses();
	//
	// int ic = individuals.size();
	// int dc = subclasses.size();
	// int ac = superclasses.size();
	//
	// if (ic == 0 && dc == 0 && ac == 0) {
	// logger.info("This question has no known answers.");
	// } else {
	// logger.info(ic + " named individuals (" + answer.getIndividualNodeSetPolicy()
	// + "): ");
	// }
	// }
	//
	// private Set<OWLNamedIndividual>
	// renderIndividuals(Set<Node<OWLNamedIndividual>> entityNodes,
	// OWLClassExpression dlquery) {
	// Set<OWLNamedIndividual> repEntities = Sets.newHashSet();
	// for (Node<OWLNamedIndividual> node : entityNodes) {
	// OWLNamedIndividual repEntity = node.getRepresentativeElement();
	// repEntities.add(repEntity);
	//
	//// if (node.isSingleton()) {
	//// addComponent(getHyperlink(repEntity,
	// manager.getOWLDataFactory().getOWLClassAssertionAxiom(dlquery, repEntity)));
	//// } else {
	//// addComponent(ComponentFactory.makeItalicLabel("{"));
	//// for (OWLNamedIndividual ind : node.getEntities()) {
	//// addComponent(getHyperlink(ind,
	// manager.getOWLDataFactory().getOWLClassAssertionAxiom(dlquery, ind)));
	//// }
	//// addComponent(ComponentFactory.makeItalicLabel("}"));
	//// }
	// }
	// return repEntities;
	// }

}