package mt.uol.smi.attempto.aceview.planner;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.output.ThresholdingOutputStream;
import org.apache.log4j.Logger;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.change.AddAxiomData;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomChange;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.RemoveOntologyAnnotation;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import ch.uzh.ifi.attempto.ace.ACESentence;
import ch.uzh.ifi.attempto.ace.ACESplitter;
import ch.uzh.ifi.attempto.aceview.ACESnippet;
import ch.uzh.ifi.attempto.aceview.ACESnippetImpl;
import ch.uzh.ifi.attempto.aceview.ACEText;
import ch.uzh.ifi.attempto.aceview.ACETextImpl;
import ch.uzh.ifi.attempto.aceview.ACETextManager;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerActionModel;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerConditionModel;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerProblemModel;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerEffectTuple;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerGoalModel;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerModel;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerParameter;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerSnippet;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerSnippetsUsedModel;
import mt.uol.smi.attempto.aceview.planner.model.OWLIndividualChanges;
import mt.uol.smi.attempto.aceview.planner.model.event.ACEPlannerEvent;
import mt.uol.smi.attempto.aceview.planner.model.event.ACEPlannerListener;
import mt.uol.smi.attempto.aceview.planner.model.event.ModelEventType;

public class ACEPlannerManagerDefault implements IACEPlannerManager {

	private static ACEPlannerManagerDefault instance;
	private ACEText<OWLEntity, OWLLogicalAxiom> acetext;
	private ACEPlannerModel currentlySavedACEPlannerActionListModel;
	private ACEPlannerModel currentlyBeingModifiedACEPlannerActionListModel;
	private static final Logger logger = Logger.getLogger(ACEPlannerManager.class);
	private final List<ACEPlannerListener<ACEPlannerEvent<ModelEventType>>> acePlannerChangeListeners = Lists
			.newArrayList();

	private final static String ACEPlanner_BASE_IRI = "http://attempto.ifi.uzh.ch/ace-Planner";
	private final static String Planner_DOMAIN_IRI = "/domain";
	private final static String ACTION_IRI = "/action";
	private final static String ACTION_PROPERTY_IRI = "/hasAction";
	private final static String PROBLEM_SENTENCES_IRI = "/problem";
	private final static String GOAL_IRI = "/goal";
	private final static String PROBLEM_SENTENCES_PROPERTY_IRI = "/has-problem";
	private final static String GOAL_PROPERTY_IRI = "/hasGoal";
	private final static String SENTENCES_USED_IRI = "/sentences-used";
	private final static String SENTENCES_USED_PROPERTY_IRI = "/has-sentences-used";
	private final static String CONDITION_PROPERTY_IRI = "/has-condition";
	private final static String EFFECT_PROPERTY_IRI = "/has-effect";
	private final static String PARAMETER_PROPERTY_IRI = "/has-parameter";
	private final static String CONDITION_IRI = "/condition";
	private final static String EFFECT_IRI = "/effect";
	private final static String PARAMETER_IRI = "/action-parameter";
	private final static String SENTENCE_IRI = "/sentence";
	private final static String ACTION_NAME_IRI = "/action-name";
	private final static String DOMAIN_NAME_IRI = "/domain-name";
	private final static String QUESTION_IRI = "/question";
	private final static String PARAMETER_NAME_IRI = "/parameter-name";
	private final static String PARAMETER_VALUE_IRI = "/parameter-value";
	private final static String IDENTIFIER_IRI = "/object-identifier";
	private final static String NEGATED_IRI = "/negated";

	private OWLClass clsPlannerDomain;
	private OWLClass clsAction;
	private OWLClass clsCondition;
	private OWLClass clsEffect;
	private OWLClass clsParameter;
	private OWLClass clsProblem;
	private OWLClass clsSentencesUsed;
	private OWLClass clsGoal;

	private OWLAnnotation annoSentence;
	private OWLAnnotation annoActionName;
	private OWLAnnotation annoDomainName;
	private OWLAnnotation annoQuestion;
	private OWLAnnotation annoParameterName;
	private OWLAnnotation annoParameterValue;
	private OWLAnnotation annoIdentifier;
	private OWLAnnotation annoNegated;

	private OWLObjectProperty objPropAction;
	private OWLObjectProperty objPropCondition;
	private OWLObjectProperty objPropEffect;
	private OWLObjectProperty objPropParameter;
	private OWLObjectProperty objPropProblem;
	private OWLObjectProperty objPropSentencesUsed;
	private OWLObjectProperty objPropGoal;

	private OWLDataProperty dataPropSentence;
	private OWLDataProperty dataPropQuestion;
	private OWLDataProperty dataPropActonName;
	private OWLDataProperty dataPropDomainName;
	private OWLDataProperty dataPropParameterName;
	private OWLDataProperty dataPropParameterValue;
	private OWLDataProperty dataPropIdentifier;
	private OWLDataProperty dataPropNegated;

	private OWLClassExpression clsExprAction;
	private OWLClassExpression clsExprProblem;
	private OWLClassExpression clsExprGoal;
	private OWLClassExpression clsExprSentencesUsed;
	private OWLClassExpression clsExprCondition;
	private OWLClassExpression clsExprEffectExpression;
	private OWLClassExpression clsExprParameterExpression;

	private OWLDataFactory factory;
	private OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();

	public ACEPlannerManagerDefault() {
		init();
	}

	public ACEPlannerManagerDefault(OWLDataFactory factorIn) {
		init(factorIn);
	}

	@Override
	public void init() {
		factory = getModelManager().getOWLDataFactory();
		init(factory);
	}

	@Override
	public void init(OWLDataFactory factoryIn) {
		currentlySavedACEPlannerActionListModel = new ACEPlannerModel();
		currentlyBeingModifiedACEPlannerActionListModel = new ACEPlannerModel();
		if (factory == null) {
			logger.info("Factory is null");
		} else if (!isClassesExist()) {
			logger.info("Initialising ACE Planner Classes");
			String base = ACEPlanner_BASE_IRI;
			PrefixManager pm = new DefaultPrefixManager(base);

			clsPlannerDomain = factoryIn.getOWLClass(Planner_DOMAIN_IRI, pm);
			clsAction = factoryIn.getOWLClass(ACTION_IRI, pm);
			clsCondition = factoryIn.getOWLClass(CONDITION_IRI, pm);
			clsEffect = factoryIn.getOWLClass(EFFECT_IRI, pm);
			clsParameter = factoryIn.getOWLClass(PARAMETER_IRI, pm);
			clsProblem = factoryIn.getOWLClass(PROBLEM_SENTENCES_IRI, pm);
			clsSentencesUsed = factoryIn.getOWLClass(SENTENCES_USED_IRI, pm);
			clsGoal = factory.getOWLClass(GOAL_IRI, pm);

			annoQuestion = factoryIn.getOWLAnnotation(factoryIn.getOWLAnnotationProperty(pm.getIRI(QUESTION_IRI)),
					factoryIn.getOWLLiteral("A question for ACE Planner", "en"));
			annoDomainName = factoryIn.getOWLAnnotation(factoryIn.getOWLAnnotationProperty(pm.getIRI(DOMAIN_NAME_IRI)),
					factoryIn.getOWLLiteral("The domain name for ACE Planner ontology", "en"));
			annoSentence = factoryIn.getOWLAnnotation(factoryIn.getOWLAnnotationProperty(pm.getIRI(SENTENCE_IRI)),
					factoryIn.getOWLLiteral("A sentence for ACE Planner", "en"));
			annoActionName = factoryIn.getOWLAnnotation(factoryIn.getOWLAnnotationProperty(pm.getIRI(ACTION_NAME_IRI)),
					factoryIn.getOWLLiteral("The name for an ACE Planner action", "en"));
			annoParameterName = factoryIn.getOWLAnnotation(
					factoryIn.getOWLAnnotationProperty(pm.getIRI(PARAMETER_NAME_IRI)),
					factoryIn.getOWLLiteral("The name of a parameter", "en"));
			annoParameterValue = factoryIn.getOWLAnnotation(
					factoryIn.getOWLAnnotationProperty(pm.getIRI(PARAMETER_VALUE_IRI)),
					factoryIn.getOWLLiteral("The value of a parameter", "en"));
			annoIdentifier = factoryIn.getOWLAnnotation(factoryIn.getOWLAnnotationProperty(pm.getIRI(IDENTIFIER_IRI)),
					factoryIn.getOWLLiteral("The unique identifier of an object", "en"));
			annoNegated = factoryIn.getOWLAnnotation(factoryIn.getOWLAnnotationProperty(pm.getIRI(NEGATED_IRI)),
					factoryIn.getOWLLiteral("Whether the effect is a negation or not", "en"));

			objPropAction = factoryIn.getOWLObjectProperty(IRI.create(base + ACTION_PROPERTY_IRI));
			objPropCondition = factoryIn.getOWLObjectProperty(IRI.create(base + CONDITION_PROPERTY_IRI));
			objPropEffect = factoryIn.getOWLObjectProperty(IRI.create(base + EFFECT_PROPERTY_IRI));
			objPropParameter = factoryIn.getOWLObjectProperty(IRI.create(base + PARAMETER_PROPERTY_IRI));
			objPropProblem = factoryIn.getOWLObjectProperty(IRI.create(base + PROBLEM_SENTENCES_PROPERTY_IRI));
			objPropSentencesUsed = factoryIn.getOWLObjectProperty(IRI.create(base + SENTENCES_USED_PROPERTY_IRI));
			objPropGoal = factoryIn.getOWLObjectProperty(IRI.create(base + GOAL_PROPERTY_IRI));

			dataPropParameterName = factoryIn.getOWLDataProperty(IRI.create(base + PARAMETER_NAME_IRI));
			dataPropParameterValue = factoryIn.getOWLDataProperty(IRI.create(base + PARAMETER_VALUE_IRI));
			dataPropIdentifier = factoryIn.getOWLDataProperty(IRI.create(base + IDENTIFIER_IRI));
			dataPropSentence = factoryIn.getOWLDataProperty(IRI.create(base + SENTENCE_IRI));
			dataPropQuestion = factoryIn.getOWLDataProperty(IRI.create(base + QUESTION_IRI));
			dataPropActonName = factoryIn.getOWLDataProperty(IRI.create(base + ACTION_NAME_IRI));
			dataPropDomainName = factoryIn.getOWLDataProperty(IRI.create(base + DOMAIN_NAME_IRI));
			dataPropNegated = factoryIn.getOWLDataProperty(IRI.create(base + NEGATED_IRI));

			clsExprCondition = factoryIn.getOWLObjectSomeValuesFrom(objPropCondition, clsCondition);
			clsExprEffectExpression = factoryIn.getOWLObjectSomeValuesFrom(objPropEffect, clsEffect);
			clsExprParameterExpression = factoryIn.getOWLObjectSomeValuesFrom(objPropParameter, clsParameter);
			clsExprAction = factoryIn.getOWLObjectSomeValuesFrom(objPropAction, clsAction);
			clsExprProblem = factoryIn.getOWLObjectSomeValuesFrom(objPropProblem, clsProblem);
			clsExprGoal = factoryIn.getOWLObjectSomeValuesFrom(objPropGoal, clsGoal);
			clsExprSentencesUsed = factoryIn.getOWLObjectSomeValuesFrom(objPropSentencesUsed, clsSentencesUsed);
		}
	}

	@Override
	public HashSet<OWLOntologyChange> generatePlannerClasses() {
		return generatePlannerClasses(getACEPlannerActiveOntology(), factory);
	}

	@Override
	public HashSet<OWLOntologyChange> generatePlannerClasses(OWLOntology ontology, OWLDataFactory factoryIn) {
		logger.info("Creating ACE Planner Classes");
		init(factoryIn);

		HashSet<OWLOntologyChange> changes = new HashSet<OWLOntologyChange>();
		boolean remove = false;

		changes.add(getChange(ontology, factoryIn.getOWLAnnotationAssertionAxiom(clsCondition.getIRI(), annoQuestion),
				remove));
		changes.add(getChange(ontology, factoryIn.getOWLAnnotationAssertionAxiom(clsEffect.getIRI(), annoSentence),
				remove));
		changes.add(
				getChange(ontology, factoryIn.getOWLAnnotationAssertionAxiom(clsEffect.getIRI(), annoNegated), remove));
		changes.add(getChange(ontology, factoryIn.getOWLAnnotationAssertionAxiom(clsProblem.getIRI(), annoSentence),
				remove));
		changes.add(getChange(ontology,
				factoryIn.getOWLAnnotationAssertionAxiom(clsSentencesUsed.getIRI(), annoSentence), remove));
		changes.add(
				getChange(ontology, factoryIn.getOWLAnnotationAssertionAxiom(clsGoal.getIRI(), annoSentence), remove));
		changes.add(getChange(ontology, factoryIn.getOWLAnnotationAssertionAxiom(clsEffect.getIRI(), annoActionName),
				remove));
		changes.add(getChange(ontology,
				factoryIn.getOWLAnnotationAssertionAxiom(clsParameter.getIRI(), annoParameterName), remove));
		changes.add(getChange(ontology,
				factoryIn.getOWLAnnotationAssertionAxiom(clsParameter.getIRI(), annoParameterValue), remove));

		changes.add(getChange(ontology, factoryIn.getOWLSubClassOfAxiom(clsAction, clsExprCondition), remove));
		changes.add(getChange(ontology, factoryIn.getOWLSubClassOfAxiom(clsAction, clsExprEffectExpression), remove));
		changes.add(getChange(ontology, factoryIn.getOWLSubClassOfAxiom(clsPlannerDomain, clsExprAction), remove));
		changes.add(
				getChange(ontology, factoryIn.getOWLSubClassOfAxiom(clsCondition, clsExprParameterExpression), remove));
		changes.add(
				getChange(ontology, factoryIn.getOWLSubClassOfAxiom(clsEffect, clsExprParameterExpression), remove));
		changes.add(getChange(ontology, factoryIn.getOWLSubClassOfAxiom(clsPlannerDomain, clsExprProblem), remove));
		changes.add(
				getChange(ontology, factoryIn.getOWLSubClassOfAxiom(clsPlannerDomain, clsExprSentencesUsed), remove));

		changes.add(getChange(ontology, annoSentence, remove));
		changes.add(getChange(ontology, annoQuestion, remove));
		changes.add(getChange(ontology, annoActionName, remove));
		changes.add(getChange(ontology, annoParameterName, remove));
		changes.add(getChange(ontology, annoParameterValue, remove));
		changes.add(getChange(ontology, annoIdentifier, remove));
		changes.add(getChange(ontology, annoDomainName, remove));

		changes.add(getChange(ontology, factoryIn.getOWLDeclarationAxiom(clsPlannerDomain), remove));
		changes.add(getChange(ontology, factoryIn.getOWLDeclarationAxiom(clsGoal), remove));
		changes.add(getChange(ontology, factoryIn.getOWLDeclarationAxiom(clsAction), remove));
		changes.add(getChange(ontology, factoryIn.getOWLDeclarationAxiom(clsCondition), remove));
		changes.add(getChange(ontology, factoryIn.getOWLDeclarationAxiom(clsEffect), remove));
		changes.add(getChange(ontology, factoryIn.getOWLDeclarationAxiom(clsParameter), remove));
		changes.add(getChange(ontology, factoryIn.getOWLDeclarationAxiom(clsProblem), remove));
		changes.add(getChange(ontology, factoryIn.getOWLDeclarationAxiom(clsSentencesUsed), remove));
		changes.add(getChange(ontology, factoryIn.getOWLDeclarationAxiom(objPropCondition), remove));
		changes.add(getChange(ontology, factoryIn.getOWLDeclarationAxiom(objPropGoal), remove));
		changes.add(getChange(ontology, factoryIn.getOWLDeclarationAxiom(objPropAction), remove));
		changes.add(getChange(ontology, factoryIn.getOWLDeclarationAxiom(objPropEffect), remove));
		changes.add(getChange(ontology, factoryIn.getOWLDeclarationAxiom(objPropParameter), remove));
		changes.add(getChange(ontology, factoryIn.getOWLDeclarationAxiom(objPropProblem), remove));
		changes.add(getChange(ontology, factoryIn.getOWLDeclarationAxiom(objPropSentencesUsed), remove));
		changes.add(getChange(ontology, factoryIn.getOWLDeclarationAxiom(dataPropSentence), remove));
		changes.add(getChange(ontology, factoryIn.getOWLDeclarationAxiom(dataPropIdentifier), remove));
		changes.add(getChange(ontology, factoryIn.getOWLDeclarationAxiom(dataPropQuestion), remove));
		changes.add(getChange(ontology, factoryIn.getOWLDeclarationAxiom(dataPropActonName), remove));
		changes.add(getChange(ontology, factoryIn.getOWLDeclarationAxiom(dataPropDomainName), remove));
		changes.add(getChange(ontology, factoryIn.getOWLDeclarationAxiom(dataPropParameterName), remove));
		changes.add(getChange(ontology, factoryIn.getOWLDeclarationAxiom(dataPropParameterValue), remove));
		changes.add(getChange(ontology, factoryIn.getOWLDeclarationAxiom(dataPropNegated), remove));

		OWLDataExactCardinality exact = factoryIn.getOWLDataExactCardinality(1, dataPropDomainName);
		changes.add(getChange(ontology, factory.getOWLObjectPropertyRangeAxiom(objPropProblem, clsProblem), remove));
		changes.add(getChange(ontology, factory.getOWLObjectPropertyRangeAxiom(objPropGoal, clsGoal), remove));
		changes.add(getChange(ontology, factory.getOWLObjectPropertyRangeAxiom(objPropSentencesUsed, clsSentencesUsed),
				remove));
		changes.add(getChange(ontology, factory.getOWLObjectPropertyRangeAxiom(objPropAction, clsAction), remove));
		changes.add(
				getChange(ontology, factory.getOWLObjectPropertyRangeAxiom(objPropCondition, clsCondition), remove));
		changes.add(
				getChange(ontology, factory.getOWLObjectPropertyRangeAxiom(objPropParameter, clsParameter), remove));
		changes.add(getChange(ontology, factory.getOWLObjectPropertyRangeAxiom(objPropEffect, clsEffect), remove));

		return changes;
	}

	private OWLAxiomChange getChange(OWLOntology ontology, OWLAxiom axiom, boolean remove) {
		if (remove) {
			return getRemoveChange(ontology, axiom);
		}
		return getAddChange(ontology, axiom);
	}

	private OWLOntologyChange getChange(OWLOntology ontology, OWLAnnotation annotation, boolean remove) {
		if (remove) {
			return new RemoveOntologyAnnotation(ontology, annotation);
		}
		return new AddOntologyAnnotation(ontology, annotation);
	}

	private OWLAxiomChange getAddChange(OWLOntology ontology, OWLAxiom axiom) {
		return new AddAxiom(ontology, axiom);
	}

	private OWLAxiomChange getRemoveChange(OWLOntology ontology, OWLAxiom axiom) {
		return new RemoveAxiom(ontology, axiom);
	}

	@Override
	public void updateOntology(Set<OWLOntologyChange> changes) {
		getModelManager().applyChanges(new ArrayList<OWLOntologyChange>(changes));
	}

	@Override
	public void updateModel() {
		checkAndInit();
		logger.info("\n++++++++++++++++++++++ Updating acePlannerModel ++++++++++++++++++++++++++++++\n");
		logger.info("Trying to remove acePlannerModel " + getCurrentlySavedACEPlannerModel().toString());
		OWLIndividualChanges changes = getDomain(getACEPlannerActiveOntology(), getCurrentlySavedACEPlannerModel(),
				true);
		if (changes != null && changes.getChanges().size() > 0) {
			logger.info("\n---Removing PlannerModel " + getCurrentlySavedACEPlannerModel().toString());
			updateOntology(changes.getChanges());
		} else {
			logger.error("!!acePlannerModel couldn't be removed.");
		}
		logger.info("\n+++Saving " + getCurrentlyBeingModifiedACEPlannerModel().toString());
		changes = getDomain(getACEPlannerActiveOntology(), getCurrentlyBeingModifiedACEPlannerModel(), false);
		if (changes != null && changes.getChanges().size() > 0) {
			logger.info("+++Adding acePlannerModel");
			updateOntology(changes.getChanges());
		} else {
			logger.info("!!acePlannerModel couldn't be added.");
		}
		loadPlannerDomain();
		fireEvent(ModelEventType.MODEL_CHANGED);
	}

	// private void updateActions(OWLOntology ontology, List<ACEPlannerActionModel>
	// list) {
	// for (ACEPlannerActionModel model : list) {
	// // add the action to ontology
	// addAction(ontology, model);
	// }
	// }

	@Override
	public ACEPlannerActionModel addAction() {
		ACEPlannerActionModel model = new ACEPlannerActionModel();
		getCurrentlyBeingModifiedACEPlannerModel().getActionListModel().getList().add(model);
		fireEvent(ModelEventType.ACTION_LIST_CHANGED);
		return model;
	}

	private ACEPlannerActionModel addActionToOntology(OWLOntology ontology, ACEPlannerActionModel action) {
		checkAndInit();
		this.currentlyBeingModifiedACEPlannerActionListModel.getActionListModel().getList().add(action);
		logger.info("Adding action " + action.getId());
		Set<OWLOntologyChange> allChanges = new HashSet<OWLOntologyChange>();
		OWLIndividualChanges actionChanges = getActon(ontology, action, false);
		OWLIndividualChanges changesDomain = getDomain(getACEPlannerActiveOntology(),
				getCurrentlyBeingModifiedACEPlannerModel(), false);
		allChanges.addAll(actionChanges.getChanges());
		allChanges.addAll(changesDomain.getChanges());
		allChanges.add(getActionPropertyChange(getACEPlannerActiveOntology(), changesDomain.getIndividual(),
				actionChanges.getIndividual(), false));
		updateOntology(allChanges);
		logger.info("New action added.");
		loadPlannerDomain();
		fireEvent(ModelEventType.ACTION_ADDED);
		action = getCurrentlyBeingModifiedACEPlannerModel().findActionById(action.getId());
		return action;
	}

	@Override
	public ACEPlannerActionModel removeConditionFromModel(ACEPlannerActionModel model, ACEPlannerSnippet condition) {
		ACEPlannerActionModel currentActionModel = getCurrentlyBeingModifiedACEPlannerModel()
				.findActionById(model.getId());
		if (currentActionModel != null) {
			currentActionModel.removeCondition(condition.getAceSnippet());
		}
		model.removeCondition(condition.getAceSnippet());
		fireEvent(ModelEventType.CONDITION_CHANGED);
		return model;
	}

	private ACEPlannerActionModel removeConditionFromOntology(OWLOntology ontology, ACEPlannerActionModel model,
			ACEPlannerSnippet condition) {
		logger.info("Removing condition " + condition.getId());
		ACEPlannerActionModel currentAction = findActionInCurrentlySavedActions(model.getId());
		if (currentAction != null) {
			logger.info("Action found. Removing current condition");
			ACEPlannerConditionModel currentCondition = model.findCondition(condition.getAceSnippet());
			currentAction.removeCondition(currentCondition.getAcePlannerSnippet().getAceSnippet());
			removeActionFromOntology(ontology, currentAction.getId());
			removeConditionFromModel(model, condition);
		} else {
			logger.error("!!! The action to remove wasn't found.");
		}
		return findActionInCurrentlySavedActions(model.getId());
	}

	@Override
	public ACEPlannerActionModel updateActionFromModel(ACEPlannerActionModel model) {
		ACEPlannerActionModel newModel = updateActionFromOntology(getACEPlannerActiveOntology(), model);
		fireEvent(ModelEventType.ACTION_CHANGED);
		return newModel;
	}

	private ACEPlannerActionModel updateActionFromOntology(OWLOntology ontology, ACEPlannerActionModel model) {
		logger.info("Updating action " + model.getId());
		if (model.getOWLIndividual() != null) {
			logger.info("Removing action " + model.getId());
			removeActionFromOntology(ontology, model.getId());
		}
		logger.info("Adding action");
		Set<OWLOntologyChange> allChanges = new HashSet<OWLOntologyChange>();
		OWLIndividualChanges changes = getActon(ontology, model, false);
		allChanges.addAll(changes.getChanges());
		allChanges.add(getActionPropertyChange(getACEPlannerActiveOntology(),
				getCurrentlyBeingModifiedACEPlannerModel().getOWLIndividual(), changes.getIndividual(), false));
		updateOntology(allChanges);
		return model;
	}

	@Override
	public void removeActionFromModel(ACEPlannerActionModel action) {
		ACEPlannerActionModel currentActionModel = getCurrentlyBeingModifiedACEPlannerModel()
				.findActionById(action.getId());
		if (currentActionModel != null) {
			getCurrentlyBeingModifiedACEPlannerModel().removeAction(currentActionModel);
		}
		fireEvent(ModelEventType.ACTION_LIST_CHANGED);
	}

	private void removeActionFromOntology(OWLOntology ontology, String actionId) {
		checkAndInit();
		ACEPlannerActionModel current = findActionInCurrentlySavedActions(actionId);
		if (current != null) {
			logger.info("Current matching action found. This will be removed " + current.getId());

			Set<OWLOntologyChange> allChanges = new HashSet<OWLOntologyChange>();
			OWLIndividualChanges changes = getActon(ontology, current, true);
			allChanges.addAll(changes.getChanges());
			allChanges.add(getActionPropertyChange(getACEPlannerActiveOntology(),
					getCurrentlyBeingModifiedACEPlannerModel().getOWLIndividual(), changes.getIndividual(), true));

			updateOntology(allChanges);
			removeActionFromModel(current);
		} else {
			logger.error("The action to remove does not exist.");
		}
	}

	private ACEPlannerActionModel findActionInCurrentlySavedActions(String id) {
		for (ACEPlannerActionModel model : this.getCurrentlySavedACEPlannerModel().getActionListModel().getList()) {
			if (model.getId().equals(id)) {
				return model;
			}
		}
		return null;
	}

	private ACEPlannerActionModel findActionInCurrentlyBeingModifiedActions(String id) {
		for (ACEPlannerActionModel model : this.getCurrentlyBeingModifiedACEPlannerModel().getActionListModel()
				.getList()) {
			if (model.getId().equals(id)) {
				return model;
			}
		}
		return null;
	}

	private String formatString(String text) {
		if (text == null) {
			return "";
		}
		return text.replace('?', ' ').trim().replaceAll(" +", "-").toLowerCase();
	}

	private OWLIndividualChanges getProblem(OWLOntology ontology, ACEPlannerProblemModel domainModel, boolean remove) {

		HashSet<OWLOntologyChange> changes = new HashSet<OWLOntologyChange>();
		String base = ACEPlanner_BASE_IRI;

		OWLIndividual individualPlannerDomain = factory
				.getOWLNamedIndividual(IRI.create(base + PROBLEM_SENTENCES_IRI + "-" + domainModel.getId()));
		OWLAxiom identifier = factory.getOWLDataPropertyAssertionAxiom(dataPropIdentifier, individualPlannerDomain,
				domainModel.getId());
		changes.add(getChange(ontology, identifier, remove));
		for (ACEPlannerSnippet domainSnippet : domainModel.getSnippets()) {

			logger.info("Adding domain snippet " + domainSnippet.getSentence());
			OWLAxiom domainAxiom = factory.getOWLDataPropertyAssertionAxiom(dataPropSentence, individualPlannerDomain,
					domainSnippet.getSentence());
			changes.add(getChange(ontology, domainAxiom, remove));
		}

		changes.add(
				getChange(ontology, factory.getOWLClassAssertionAxiom(clsProblem, individualPlannerDomain), remove));
		return new OWLIndividualChanges(individualPlannerDomain, changes);
	}

	private OWLIndividualChanges getPlannerSnippetsUsed(OWLOntology ontology,
			ACEPlannerSnippetsUsedModel snippetsUsedModel, boolean remove) {

		HashSet<OWLOntologyChange> changes = new HashSet<OWLOntologyChange>();
		String base = ACEPlanner_BASE_IRI;

		OWLIndividual individualSnippetUsed = factory
				.getOWLNamedIndividual(IRI.create(base + SENTENCES_USED_IRI + "-" + snippetsUsedModel.getId()));
		OWLAxiom identifier = factory.getOWLDataPropertyAssertionAxiom(dataPropIdentifier, individualSnippetUsed,
				snippetsUsedModel.getId());
		changes.add(getChange(ontology, identifier, remove));
		for (ACEPlannerSnippet domainSnippet : snippetsUsedModel.getSnippets()) {

			logger.info("Adding domain snippet " + domainSnippet.getSentence());
			OWLAxiom domainAxiom = factory.getOWLDataPropertyAssertionAxiom(dataPropSentence, individualSnippetUsed,
					domainSnippet.getSentence());
			changes.add(getChange(ontology, domainAxiom, remove));
		}

		changes.add(getChange(ontology, factory.getOWLClassAssertionAxiom(clsSentencesUsed, individualSnippetUsed),
				remove));
		return new OWLIndividualChanges(individualSnippetUsed, changes);
	}

	private OWLIndividualChanges getPlannerGoal(OWLOntology ontology, ACEPlannerGoalModel goalModel, boolean remove) {

		HashSet<OWLOntologyChange> changes = new HashSet<OWLOntologyChange>();
		String base = ACEPlanner_BASE_IRI;

		OWLIndividual individualGoal = factory
				.getOWLNamedIndividual(IRI.create(base + GOAL_IRI + "-" + goalModel.getId()));
		OWLAxiom identifier = factory.getOWLDataPropertyAssertionAxiom(dataPropIdentifier, individualGoal,
				goalModel.getId());
		changes.add(getChange(ontology, identifier, remove));
		for (ACEPlannerSnippet domainSnippet : goalModel.getSnippets()) {

			logger.info("Adding goal snippet " + domainSnippet.getSentence());
			OWLAxiom goalAxiom = factory.getOWLDataPropertyAssertionAxiom(dataPropSentence, individualGoal,
					domainSnippet.getSentence());
			changes.add(getChange(ontology, goalAxiom, remove));
		}

		changes.add(getChange(ontology, factory.getOWLClassAssertionAxiom(clsGoal, individualGoal), remove));
		return new OWLIndividualChanges(individualGoal, changes);
	}

	private OWLIndividualChanges getActon(OWLOntology ontology, ACEPlannerActionModel actionModel, boolean remove) {

		HashSet<OWLOntologyChange> changes = new HashSet<OWLOntologyChange>();
		String base = ACEPlanner_BASE_IRI;

		OWLIndividual individualAction = factory.getOWLNamedIndividual(IRI.create(
				base + ACTION_IRI + "-" + formatString(actionModel.getActionName() + "-" + actionModel.getId())));
		OWLAxiom actionIdentifier = factory.getOWLDataPropertyAssertionAxiom(dataPropIdentifier, individualAction,
				actionModel.getId());
		OWLAxiom actionName = factory.getOWLDataPropertyAssertionAxiom(dataPropActonName, individualAction,
				actionModel.getActionName());
		changes.add(getChange(ontology, actionIdentifier, remove));
		changes.add(getChange(ontology, actionName, remove));
		for (ACEPlannerConditionModel conditionSnippet : actionModel.getConditionsAsList()) {

			// building condition
			String conditionIRIString = base + CONDITION_IRI + "-" + formatString(actionModel.getActionName()) + "-"
					+ formatString(conditionSnippet.getSentence());
			IRI conditionIRI = IRI.create(conditionIRIString);
			conditionSnippet.setIri(conditionIRI);

			// Building objects
			OWLIndividual individualCondition = factory.getOWLNamedIndividual(conditionIRI);
			OWLAxiom questionAxiom = factory.getOWLDataPropertyAssertionAxiom(dataPropQuestion, individualCondition,
					conditionSnippet.getSentence());
			OWLAxiom identifier = factory.getOWLDataPropertyAssertionAxiom(dataPropIdentifier, individualCondition,
					conditionSnippet.getId());

			// Building Parameters
			for (ACEPlannerParameter param : new ArrayList<ACEPlannerParameter>(conditionSnippet.getParameters())) {
				IRI parameterIRI = IRI.create(conditionIRIString + "-param-" + formatString(param.getParameterValue()));
				OWLIndividual individualParameter = factory.getOWLNamedIndividual(parameterIRI);

				OWLAxiom paramIdentifier = factory.getOWLDataPropertyAssertionAxiom(dataPropIdentifier,
						individualParameter, param.getId());
				changes.add(getChange(ontology, paramIdentifier, remove));

				// adding to param object
				OWLAxiom nameAxiom = factory.getOWLDataPropertyAssertionAxiom(dataPropParameterName,
						individualParameter, param.getParameterName());
				OWLAxiom valueAxiom = factory.getOWLDataPropertyAssertionAxiom(dataPropParameterValue,
						individualParameter, param.getParameterValue());
				changes.add(getChange(ontology, nameAxiom, remove));
				changes.add(getChange(ontology, valueAxiom, remove));

				// adding to condition
				OWLObjectPropertyAssertionAxiom paramPropertyAssertion = factory
						.getOWLObjectPropertyAssertionAxiom(objPropParameter, individualCondition, individualParameter);
				changes.add(getChange(ontology, paramPropertyAssertion, remove));
			}

			OWLObjectPropertyAssertionAxiom conditionPropertyAssertion = factory
					.getOWLObjectPropertyAssertionAxiom(objPropCondition, individualAction, individualCondition);

			// adding to changes list
			changes.add(getChange(ontology, identifier, remove));
			changes.add(getChange(ontology, questionAxiom, remove));
			changes.add(getChange(ontology, conditionPropertyAssertion, remove));
			changes.add(
					getChange(ontology, factory.getOWLClassAssertionAxiom(clsCondition, individualCondition), remove));
		}

		// building effects
		for (ACEPlannerEffectTuple effectSnippet : actionModel.getEffects()) {
			OWLIndividual individualEffect = factory.getOWLNamedIndividual(
					IRI.create(base + EFFECT_IRI + "-" + formatString(actionModel.getActionName()) + "-"
							+ formatString(effectSnippet.getUnformattedResult().getSentence())));
			OWLObjectPropertyAssertionAxiom effectPropertyAssertion = factory
					.getOWLObjectPropertyAssertionAxiom(objPropEffect, individualAction, individualEffect);

			OWLAxiom effectIdentifier = factory.getOWLDataPropertyAssertionAxiom(dataPropIdentifier, individualEffect,
					effectSnippet.getId());
			OWLAxiom sentenceAxiom = factory.getOWLDataPropertyAssertionAxiom(dataPropSentence, individualEffect,
					effectSnippet.getUnformattedResult().getSentence());
			OWLAxiom negatedAxiom = factory.getOWLDataPropertyAssertionAxiom(dataPropNegated, individualEffect,
					effectSnippet.isNegated());

			changes.add(getChange(ontology, effectIdentifier, remove));
			changes.add(getChange(ontology, sentenceAxiom, remove));
			changes.add(getChange(ontology, negatedAxiom, remove));

			changes.add(getChange(ontology, effectPropertyAssertion, remove));
			changes.add(getChange(ontology, factory.getOWLClassAssertionAxiom(clsEffect, individualEffect), remove));
		}

		changes.add(getChange(ontology, factory.getOWLClassAssertionAxiom(clsAction, individualAction), remove));
		return new OWLIndividualChanges(individualAction, changes);
	}

	private OWLIndividualChanges getProblemDomainObject(OWLOntology ontology, ACEPlannerModel problemDomainModel,
			boolean remove) {

		HashSet<OWLOntologyChange> changes = new HashSet<OWLOntologyChange>();
		String base = ACEPlanner_BASE_IRI;

		OWLIndividual individualDomain = factory.getOWLNamedIndividual(IRI.create(base + Planner_DOMAIN_IRI + "-"
				+ formatString(problemDomainModel.getName() + "-" + problemDomainModel.getId())));
		OWLAxiom domainIdentifier = factory.getOWLDataPropertyAssertionAxiom(dataPropIdentifier, individualDomain,
				problemDomainModel.getId());
		OWLAxiom domainName = factory.getOWLDataPropertyAssertionAxiom(dataPropDomainName, individualDomain,
				problemDomainModel.getName());
		changes.add(getChange(ontology, domainIdentifier, remove));
		changes.add(getChange(ontology, domainName, remove));
		changes.add(getChange(ontology, factory.getOWLClassAssertionAxiom(clsPlannerDomain, individualDomain), remove));
		return new OWLIndividualChanges(individualDomain, changes);
	}

	private OWLIndividualChanges getDomain(OWLOntology ontology, ACEPlannerModel problemDomainModel, boolean remove) {
		logger.info("Getting domain");
		OWLIndividualChanges domainChanges = getProblemDomainObject(ontology, problemDomainModel, remove);
		HashSet<OWLOntologyChange> changes = new HashSet<OWLOntologyChange>();
		changes.addAll(domainChanges.getChanges());

		OWLIndividual individualDomain = domainChanges.getIndividual();

		// PlannerDomain
		OWLIndividualChanges getProblemChanges = getProblem(ontology, problemDomainModel.getProblem(), remove);
		changes.add(
				getPlannerDomainPropertyChange(ontology, individualDomain, getProblemChanges.getIndividual(), remove));
		changes.addAll(getProblemChanges.getChanges());

		// snippets used Domain
		OWLIndividualChanges getSnippedUsedChanges = getPlannerSnippetsUsed(ontology,
				problemDomainModel.getSentencesUsed(), remove);
		changes.add(getSnippetsUsedDomainPropertyChange(ontology, individualDomain,
				getSnippedUsedChanges.getIndividual(), remove));
		changes.addAll(getSnippedUsedChanges.getChanges());

		// goal for domain
		OWLIndividualChanges getGoalChanges = getPlannerGoal(ontology, problemDomainModel.getGoal(), remove);
		changes.add(getGoalDomainPropertyChange(ontology, individualDomain, getGoalChanges.getIndividual(), remove));
		changes.addAll(getGoalChanges.getChanges());

		// actions
		for (ACEPlannerActionModel actionModel : problemDomainModel.getActionListModel().getList()) {
			OWLIndividualChanges change = getActon(ontology, actionModel, remove);
			changes.add(getActionPropertyChange(ontology, individualDomain, change.getIndividual(), remove));
			changes.addAll(change.getChanges());
		}

		return new OWLIndividualChanges(individualDomain, changes);
	}

	// private Set<OWLOntologyChange> getActionsChanges(OWLOntology ontology,
	// OWLIndividual individualPlannerDomain, boolean remove) {
	//
	// Set<OWLOntologyChange> changes = new HashSet<OWLOntologyChange>();
	// // actions
	// for (ACEPlannerActionModel actionModel : problemDomainModel.getActionList())
	// {
	// OWLIndividualChanges change = getActon(ontology, actionModel, remove);
	// changes.add(getActionPropertyChange(ontology, individualPlannerDomain,
	// change.getIndividual(), remove));
	// changes.addAll(change.getChanges());
	// }
	// return changes;
	// }

	@Override
	public OWLAxiomChange getActionPropertyChange(OWLOntology ontology, OWLIndividual individualDomain,
			OWLIndividual individualAction, boolean remove) {
		if (individualDomain == null) {
			logger.error("individualDomain is null");
			return null;
		}
		if (individualAction == null) {
			logger.error("individualAction is null");
			return null;
		}
		OWLObjectPropertyAssertionAxiom actionPropertyAssertion = factory
				.getOWLObjectPropertyAssertionAxiom(objPropAction, individualDomain, individualAction);
		return getChange(ontology, actionPropertyAssertion, remove);
	}

	@Override
	public OWLAxiomChange getPlannerDomainPropertyChange(OWLOntology ontology, OWLIndividual individualDomain,
			OWLIndividual individualProblem, boolean remove) {
		if (individualDomain == null) {
			logger.error("individualDomain is null");
			return null;
		}
		if (individualProblem == null) {
			logger.error("individualPlannerDomain is null");
			return null;
		}
		OWLObjectPropertyAssertionAxiom PlannerDomainPropertyAssertion = factory
				.getOWLObjectPropertyAssertionAxiom(objPropProblem, individualDomain, individualProblem);
		return getChange(ontology, PlannerDomainPropertyAssertion, remove);
	}

	@Override
	public OWLAxiomChange getSnippetsUsedDomainPropertyChange(OWLOntology ontology, OWLIndividual individualDomain,
			OWLIndividual individualSnippetUsed, boolean remove) {
		if (individualDomain == null) {
			logger.error("individualDomain is null");
			return null;
		}
		if (individualSnippetUsed == null) {
			logger.error("individualPlannerDomain is null");
			return null;
		}
		OWLObjectPropertyAssertionAxiom PlannerDomainPropertyAssertion = factory
				.getOWLObjectPropertyAssertionAxiom(objPropSentencesUsed, individualDomain, individualSnippetUsed);
		return getChange(ontology, PlannerDomainPropertyAssertion, remove);
	}

	@Override
	public OWLAxiomChange getGoalDomainPropertyChange(OWLOntology ontology, OWLIndividual individualDomain,
			OWLIndividual individualGoal, boolean remove) {
		if (individualDomain == null) {
			logger.error("individualDomain is null");
			return null;
		}
		if (individualGoal == null) {
			logger.error("individualPlannerDomain is null");
			return null;
		}
		OWLObjectPropertyAssertionAxiom goalPropertyAssertion = factory.getOWLObjectPropertyAssertionAxiom(objPropGoal,
				individualDomain, individualGoal);
		return getChange(ontology, goalPropertyAssertion, remove);
	}

	@Override
	public OWLOntology getACEPlannerActiveOntology() {
		return ACETextManager.getInstance().getOWLModelManager().getActiveOntology();
	}

	public static void main(String[] args) {
		// example();
		test();
	}

	private static void test() {
		try {
			// Create an ontology manager to work with
			OWLOntologyManager manager;
			// OWLOntology ontology;
			OWLDataFactory factoryIn;

			manager = OWLManager.createOWLOntologyManager();
			OWLOntology ontology = manager.loadOntologyFromOntologyDocument(
					new File("D:\\User Data\\Documents\\Thesis_Offline\\_Ontologies\\_x.owl"));
			factoryIn = manager.getOWLDataFactory();

			// ACEPlannerManager instance = ACEPlannerManager.getInstance(factoryIn);
			// Set<OWLOntologyChange> result = instance.generatePlannerClasses(ontology,
			// factoryIn);
			// instance.updateOntology(result);
			// manager.applyChanges(new ArrayList<OWLOntologyChange>(result));
			manager.saveOntology(ontology);

			// instance.loadProblemDomain(ontology);

		} catch (OWLOntologyCreationException e) {
			System.out.println("Could not create the ontology: " + e.getMessage());
		} catch (OWLOntologyStorageException e) {
			System.out.println("Could not save ontology: " + e.getMessage());
		}
	}

	@Override
	public ACEPlannerModel reloadProblemDomain() {
		ACEPlannerModel model = loadPlannerDomain();
		fireEvent(ModelEventType.MODEL_CHANGED);
		return model;
	}

	private ACEPlannerModel loadPlannerDomain() {
		setInitCompleted(false);
		logger.info("\n++++++++++++++++++++++ Loading acePlannerModel ++++++++++++++++++++++++++++++\n");
		logger.info("\nLoading problem domain for internal save");
		this.currentlySavedACEPlannerActionListModel = loadProblemDomain(getACEPlannerActiveOntology());
		logger.info("\n+++SSSSSSSS+++++++++++++++++++ Loading acePlannerModel ++++++++++++++++++++++++++++++\n");
		logger.info("Loaded internal save: " + this.currentlySavedACEPlannerActionListModel.toString());
		logger.info("\nDeep copy problem domain for modification");
		this.currentlyBeingModifiedACEPlannerActionListModel = new ACEPlannerModel(
				this.currentlySavedACEPlannerActionListModel);
		logger.info("\n+++++MMMMMMMMMM+++++++++++++++++ Loading acePlannerModel ++++++++++++++++++++++++++++++\n");
		logger.info("Copied for modification: " + this.currentlyBeingModifiedACEPlannerActionListModel.toString());
		logger.info("Finished loading domains.\n");
		setInitCompleted(true);
		fireEvent(ModelEventType.MODEL_LOADED);
		return currentlyBeingModifiedACEPlannerActionListModel;
	}

	private ACEPlannerModel loadProblemDomain(OWLOntology ontology) {
		OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);
		ACEPlannerModel loadedModel = new ACEPlannerModel();
		logger.info("Starting to load domain from " + ontology.getOntologyID());
		for (OWLClass c : clsPlannerDomain.getClassesInSignature()) {
			NodeSet<OWLNamedIndividual> instances = reasoner.getInstances(c, false);

			// problem domain
			for (OWLNamedIndividual problemDomain : instances.getFlattened()) {
				logger.info(">> Problem domain found ");
				loadedModel.setId(getPlannerIdentifier(problemDomain, ontology));
				logger.info("Problem domain Identifier " + loadedModel.getId());
				loadedModel.setName(getOWLString(problemDomain, dataPropDomainName, ontology));
				logger.info("Problem domain name " + loadedModel.getName());
				loadedModel.setOWLIndividual(problemDomain);

				// PlannerDomain
				for (OWLIndividual PlannerDomainOWLIndividual : EntitySearcher.getObjectPropertyValues(problemDomain,
						objPropProblem, ontology)) {
					// problemDomain.getObjectPropertyValues(objPropPlannerDomain, ontology)) {
					logger.info("---------------");
					logger.info(">>Knowledgebase found ");
					loadedModel.getProblem().setId(getPlannerIdentifier(PlannerDomainOWLIndividual, ontology));
					logger.info("Problem domain Identifier " + loadedModel.getProblem().getId());
					for (String sentenceText : getOWLStrings(PlannerDomainOWLIndividual, dataPropSentence, ontology)) {
						ACESnippet snippet = addSentencesIfMissing(sentenceText, ontology);
						loadedModel.getProblem().getSnippets().add(new ACEPlannerSnippet(snippet));
						logger.info("Added snippet to domain : " + sentenceText);
					}
				}

				// snippetsUsed
				for (OWLIndividual snippetUsedOWLIndividual : EntitySearcher.getObjectPropertyValues(problemDomain,
						objPropSentencesUsed, ontology)) { // ;
															// problemDomain.getObjectPropertyValues(objPropSentencesUsed,
															// ontology)) {
					logger.info("---------------");
					logger.info(">>Snippet used found ");
					loadedModel.getSentencesUsed().setId(getPlannerIdentifier(snippetUsedOWLIndividual, ontology));
					logger.info("SnippetUsed Identifier " + loadedModel.getSentencesUsed().getId());
					for (String sentenceText : getOWLStrings(snippetUsedOWLIndividual, dataPropSentence, ontology)) {
						ACESnippet snippet = addSentencesIfMissing(sentenceText, ontology);
						loadedModel.getSentencesUsed().getSnippets().add(new ACEPlannerSnippet(snippet));
						logger.info("Added snippet to used : " + sentenceText);
					}
				}

				// goal
				for (OWLIndividual goalOWLIndividual : EntitySearcher.getObjectPropertyValues(problemDomain,
						objPropGoal, ontology)) {
					// problemDomain.getObjectPropertyValues(objPropGoal, ontology)) {
					logger.info("---------------");
					logger.info(">>Goal found ");
					loadedModel.getGoal().setId(getPlannerIdentifier(goalOWLIndividual, ontology));
					logger.info("SnippetUsed Identifier " + loadedModel.getGoal().getId());
					for (String sentenceText : getOWLStrings(goalOWLIndividual, dataPropSentence, ontology)) {
						ACESnippet snippet = addSentencesIfMissing(sentenceText, ontology);
						loadedModel.getGoal().getSnippets().add(new ACEPlannerSnippet(snippet));
						logger.info("Added snippet to goal : " + sentenceText);
					}
				}

				// actions
				for (OWLIndividual actionOWLIndividual : EntitySearcher.getObjectPropertyValues(problemDomain,
						objPropAction, ontology)) {
					// problemDomain.getObjectPropertyValues(objPropAction, ontology)) {
					logger.info("---------------");
					logger.info(">>Action found ");

					ACEPlannerActionModel actionModel = new ACEPlannerActionModel();
					actionModel.setId(getPlannerIdentifier(actionOWLIndividual, ontology));
					logger.info("Action Identifier " + actionModel.getId());
					actionModel.setActionName(getOWLString(actionOWLIndividual, dataPropActonName, ontology));
					logger.info("Action Name " + actionModel.getActionName());
					actionModel.setOWLIndividual(actionOWLIndividual);

					loadedModel.getActionListModel().getList().add(actionModel);

					// effects
					for (OWLIndividual effectOWLIndividual : EntitySearcher.getObjectPropertyValues(actionOWLIndividual,
							objPropEffect, ontology)) {// actionOWLIndividual.getObjectPropertyValues(objPropEffect,
														// ontology)) {
						ACEPlannerEffectTuple effectModel = new ACEPlannerEffectTuple();
						effectModel.setId(getPlannerIdentifier(effectOWLIndividual, ontology));
						logger.info("Effect Identifier " + effectModel.getId());
						effectModel.setNegated(getOWLBoolean(effectOWLIndividual, dataPropNegated, ontology));
						String sentence = getOWLString(effectOWLIndividual, dataPropSentence, ontology);
						if (sentence != null) {

							logger.info("Sentence : " + sentence);
							List<String> text = new ArrayList<String>();
							text.add(sentence);
							addACEText(text, ModelEventType.NEW_SNIPPETS_LOADED);
							ACESnippet sentenceSnippet = addSentencesIfMissing(sentence, ontology);
							if (sentenceSnippet != null) {
								effectModel.setUnformattedResult(new ACEPlannerSnippet(sentenceSnippet));
								logger.info("Added effect sentence to model");
							}
						}
						if (effectModel.getUnformattedResult() == null) {
							logger.error("!!!effect sentence is null" + effectModel);
						} else {
							effectModel.setOWLIndividual(effectOWLIndividual);
							logger.info("++effect found " + effectModel);
							actionModel.getEffects().add(effectModel);
						}
					}

					// condition
					for (OWLIndividual conditionOWLIndividual : EntitySearcher
							.getObjectPropertyValues(actionOWLIndividual, objPropCondition, ontology)) {// actionOWLIndividual.getObjectPropertyValues(objPropCondition,
																										// ontology)) {
						logger.info(">> Condition found ");

						ACEPlannerConditionModel conditionModel = new ACEPlannerConditionModel();
						actionModel.getConditions().add(conditionModel);

						conditionModel.setId(getPlannerIdentifier(conditionOWLIndividual, ontology));
						logger.info("Condition Identifier " + conditionModel.getId());
						conditionModel.setOWLIndividual(conditionOWLIndividual);
						String question = getOWLString(conditionOWLIndividual, dataPropQuestion, ontology);
						if (question != null) {

							logger.info("Question : " + question);
							List<String> text = new ArrayList<String>();
							text.add(question);
							updateACEText(text, ModelEventType.NEW_SNIPPETS_LOADED);
							ACESnippet questionSnippet = addSentencesIfMissing(question, ontology);
							if (questionSnippet != null) {
								conditionModel.setAcePlannerSnippet(new ACEPlannerSnippet(questionSnippet));
								logger.info("Added question to model");
							}
						}

						// parameters
						for (OWLIndividual parameterOWLIndividual : EntitySearcher
								.getObjectPropertyValues(conditionOWLIndividual, objPropParameter, ontology)) {
							// conditionOWLIndividual.getObjectPropertyValues(objPropParameter, ontology)) {
							ACEPlannerParameter parameterModel = new ACEPlannerParameter();
							parameterModel.setId(getPlannerIdentifier(parameterOWLIndividual, ontology));
							parameterModel.setParameterName(
									getOWLString(parameterOWLIndividual, dataPropParameterName, ontology));
							parameterModel.setParameterValue(
									getOWLString(parameterOWLIndividual, dataPropParameterValue, ontology));
							parameterModel.setOWLIndividual(parameterOWLIndividual);
							logger.info("++parameter found " + parameterModel);
							conditionModel.getParameters().add(parameterModel);
						}
					}
					logger.info("Action finished");
					logger.info("---------------");
				}
			}
		}
		return loadedModel;
	}

	@Override
	public List<ACESnippet> getSnippetMissing(List<ACESentence> aceSentences) {
		List<ACESnippet> aceSnippets = new ArrayList<ACESnippet>();
		for (ACESentence sentence : aceSentences) {
			ACESnippet snippet = addSentencesIfMissing(sentence.toSimpleString(), getACEPlannerActiveOntology());
			if (snippet != null) {
				aceSnippets.add(snippet);
			}
		}
		return aceSnippets;
	}

	private ACESnippet addSentencesIfMissing(String sentencesText, OWLOntology ontology) {
		List<ACESentence> sentences = ACESplitter.getSentences(sentencesText);
		if (sentences.isEmpty()) {
			logger.info("Not added. There are no sentences.");
			return null;
		} else {
			OWLOntologyID oid = ontology.getOntologyID();
			ACEText<OWLEntity, OWLLogicalAxiom> acetext = ACETextManager.getInstance().getACEText(oid);
			ACESnippet oldSnippet = acetext.find(sentences);
			if (oldSnippet == null && sentences != null) {
				ACESnippetImpl newSnippet = new ACESnippetImpl(oid, sentences);
//				ACETextManager.getInstance().addSnippet(newSnippet);
				return newSnippet;
			} else {
				logger.info("Not added. These sentences are already in the text.");
				return oldSnippet;
			}
		}
	}

	@Override
	public ACESnippet getACESnippets(List<ACESentence> sentences) {
		return new ACESnippetImpl(getACEPlannerActiveOntology().getOntologyID(), sentences);
	}

	private boolean addACEText(List<String> text, ModelEventType type) {
		Set<List<ACESentence>> textareaSentenceLists = new HashSet<List<ACESentence>>();
		for (String t : text) {
			textareaSentenceLists.addAll(ACESplitter.getParagraphs(t));
		}
		updateActiveACEText(textareaSentenceLists, type);
		return true;
	}

	@Override
	public boolean updateACEText(List<String> text, ModelEventType type) {
		ACEText<OWLEntity, OWLLogicalAxiom> acetext = new ACETextImpl();

		ACEPlannerModel model = this.getCurrentlySavedACEPlannerModel();
		for (ACEPlannerSnippet plannerSnippet : model.getGoal().getSnippets()) {
			acetext.add(plannerSnippet.getAceSnippet());
		}

		for (ACEPlannerSnippet plannerSnippet : model.getProblem().getSnippets()) {
			acetext.add(plannerSnippet.getAceSnippet());
		}

		for (ACEPlannerSnippet plannerSnippet : model.getSentencesUsed().getSnippets()) {
			acetext.add(plannerSnippet.getAceSnippet());
		}

		logger.info("Updating active knowledge base.");

		final Set<List<ACESentence>> newSentenceLists = new LinkedHashSet<List<ACESentence>>();
		final Set<ACESnippet> removedSnippets = Sets.newHashSet();
		Set<List<ACESentence>> oldSentenceLists = Sets.newHashSet();

		List<List<ACESentence>> textareaSentenceLists = new ArrayList<List<ACESentence>>();
		for (String t : text) {
			textareaSentenceLists.addAll(ACESplitter.getParagraphs(t));
		}
		for (List<ACESentence> sentences : textareaSentenceLists) {
			if (acetext.contains(sentences)) {
				oldSentenceLists.add(sentences);
			} else {
				newSentenceLists.add(sentences);
			}
		}

		logger.info("Add: " + newSentenceLists);

		for (ACESnippet s : acetext.getSnippets()) {
			if (!oldSentenceLists.contains(s.getSentences())) {
				removedSnippets.add(s);
			}
		}

		logger.info("Del: " + removedSnippets);

		logger.info("Adding " + newSentenceLists.size() + " and deleting " + removedSnippets.size() + " snippet(s)");

		updateActiveACEText(newSentenceLists, type);
		return true;
	}

	private void updateActiveACEText(final Set<List<ACESentence>> addedSentenceLists, ModelEventType eventType) {
		// final BackgroundTask task =
		// ProtegeApplication.getBackgroundTaskManager().startTask("updating the active
		// ACE text");

		// Runnable runnable = new Runnable() {
		// public void run() {
		Date dateBegin = new Date();
//		ACETextManager.getInstance().addAndRemoveItems(addedSentenceLists, new HashSet<ACESnippet>());
		Date dateEnd = new Date();
		double duration = (dateEnd.getTime() - dateBegin.getTime()) / 1000;

		// ProtegeApplication.getBackgroundTaskManager().endTask(task);

		logger.info("Updated in " + duration + " seconds");
		fireEvent(eventType);
		// }
		// };
		// Thread t = new Thread(runnable, "Update the active ACE text");
		// t.setPriority(Thread.MIN_PRIORITY);
		// t.start();
	}

	private String getPlannerIdentifier(OWLIndividual individual, OWLOntology ontology) {
		return getOWLString(individual, dataPropIdentifier, ontology);
	}

	private boolean getOWLBoolean(OWLIndividual individual, OWLDataPropertyExpression expression,
			OWLOntology ontology) {
		String value = getOWLString(individual, expression, ontology);
		return Boolean.valueOf(value);
	}

	private String getOWLString(OWLIndividual individual, OWLDataPropertyExpression expression, OWLOntology ontology) {
		Collection<OWLLiteral> literals = EntitySearcher.getDataPropertyValues(individual, expression, ontology);// individual.getDataPropertyValues(expression,
																													// ontology);
		for (OWLLiteral literal : literals) {
			return literal.getLiteral();
		}
		return "";
	}

	private List<String> getOWLStrings(OWLIndividual individual, OWLDataPropertyExpression expression,
			OWLOntology ontology) {
		List<String> strings = new ArrayList<String>();
		Collection<OWLLiteral> literals = EntitySearcher.getDataPropertyValues(individual, expression, ontology);// individual.getDataPropertyValues(expression,
																													// ontology);
		for (OWLLiteral literal : literals) {
			strings.add(literal.getLiteral());
		}
		return strings;
	}

	@Override
	public ACEPlannerModel getCurrentlySavedACEPlannerModel() {
		if (currentlySavedACEPlannerActionListModel == null) {
			logger.info("Initialising currentlySavedACEPlannerActionListModel");
			currentlySavedACEPlannerActionListModel = new ACEPlannerModel();
		}
		return currentlySavedACEPlannerActionListModel;
	}

	@Override
	public ACEPlannerModel getCurrentlyBeingModifiedACEPlannerModel() {
		if (currentlyBeingModifiedACEPlannerActionListModel == null) {
			logger.info("Initialising currentlyBeingModifiedACEPlannerActionListModel");
			currentlyBeingModifiedACEPlannerActionListModel = new ACEPlannerModel();
		}
		return currentlyBeingModifiedACEPlannerActionListModel;
	}

	@Override
	public void checkAndInit() {
		logger.info("Checking for classes");
		if (!checkIfExists(clsPlannerDomain)) {
			logger.info("+++ Creating classes");
			updateOntology(generatePlannerClasses());
		}
	}

	@Override
	public boolean isSavedPlannerLoaded() {
		return getCurrentlyBeingModifiedACEPlannerModel().getOWLIndividual() != null;
	}

	@Override
	public boolean isPlannerDataExists() {
		return isClassesExist() && isPlannerIndividualExists();
	}

	private boolean isPlannerIndividualExists() {
		OWLReasoner reasoner = reasonerFactory.createReasoner(getACEPlannerActiveOntology());
		return reasoner.getInstances(clsPlannerDomain, false).getFlattened().size() > 0;
	}

	private boolean isClassesExist() {
		return checkIfExists(clsPlannerDomain);
	}

	private boolean checkIfExists(OWLClass owlClass) {
		if (owlClass == null)
			return false;

		OWLOntology ontology = getACEPlannerActiveOntology();
		return ontology.containsClassInSignature(owlClass.getIRI());
	}

	@Override
	public void addListener(ACEPlannerListener<ACEPlannerEvent<ModelEventType>> listener) {
		acePlannerChangeListeners.add(listener);
	}

	@Override
	public void removeListener(ACEPlannerListener<ACEPlannerEvent<ModelEventType>> listener) {
		acePlannerChangeListeners.remove(listener);
	}

	private boolean isInitCompleted = false;

	@Override
	public void setInitCompleted(boolean b) {
		isInitCompleted = b;
	}

	@Override
	public synchronized void fireEvent(ModelEventType type) {
		if (isInitCompleted) {
			ACEPlannerEvent<ModelEventType> event = new ACEPlannerEvent<ModelEventType>(type);
			logger.info("ACEPlanner Event: " + event.getType());
			if (acePlannerChangeListeners != null && acePlannerChangeListeners.size() > 0) {
				List<ACEPlannerListener<ACEPlannerEvent<ModelEventType>>> toRemove = new ArrayList<ACEPlannerListener<ACEPlannerEvent<ModelEventType>>>();
				for (ACEPlannerListener<ACEPlannerEvent<ModelEventType>> listener : acePlannerChangeListeners) {
					try {
						if (listener != null)
							listener.handleChange(event);
					} catch (Exception e) {
						logger.error(
								"Detaching " + listener.getClass().getName() + " because it threw " + e.toString());
						for(StackTraceElement s : e.getStackTrace())
						{
							logger.error(s.toString());
						}
						// ProtegeApplication.getErrorLog().logError(e);
						toRemove.add(listener);
					}
				}
				for (ACEPlannerListener<ACEPlannerEvent<ModelEventType>> listener : toRemove) {
					acePlannerChangeListeners.remove(listener);
				}
			}
		}
	}

	@Override
	public OWLModelManager getModelManager() {
		return ACETextManager.getInstance().getOWLModelManager();
	}

	@Override
	public OWLDataFactory getDataFactory() {
		return getModelManager().getOWLDataFactory();
	}

}