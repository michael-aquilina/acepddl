package mt.uol.smi.attempto.aceview.planner;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.OWLAxiomChange;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;

import ch.uzh.ifi.attempto.ace.ACESentence;
import ch.uzh.ifi.attempto.aceview.ACESnippet;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerActionModel;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerModel;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerSnippet;
import mt.uol.smi.attempto.aceview.planner.model.event.ACEPlannerEvent;
import mt.uol.smi.attempto.aceview.planner.model.event.ACEPlannerListener;
import mt.uol.smi.attempto.aceview.planner.model.event.ModelEventType;

public interface IACEPlannerManager {
	void init();

	void init(OWLDataFactory factoryIn);

	HashSet<OWLOntologyChange> generatePlannerClasses();

	HashSet<OWLOntologyChange> generatePlannerClasses(OWLOntology ontology, OWLDataFactory factoryIn);

	void updateOntology(Set<OWLOntologyChange> changes);

	void updateModel();

	ACEPlannerActionModel addAction();

	ACEPlannerActionModel removeConditionFromModel(ACEPlannerActionModel model, ACEPlannerSnippet condition);

	ACEPlannerActionModel updateActionFromModel(ACEPlannerActionModel model);

	void removeActionFromModel(ACEPlannerActionModel action);

	OWLAxiomChange getActionPropertyChange(OWLOntology ontology, OWLIndividual individualProblemDomain, OWLIndividual individualAction, boolean remove);

	OWLAxiomChange getPlannerDomainPropertyChange(OWLOntology ontology, OWLIndividual individualProblemDomain, OWLIndividual individualPlannerDomain, boolean remove);

	OWLAxiomChange getSnippetsUsedDomainPropertyChange(OWLOntology ontology, OWLIndividual individualProblemDomain, OWLIndividual individualSnippetUsed, boolean remove);

	OWLAxiomChange getGoalDomainPropertyChange(OWLOntology ontology, OWLIndividual individualProblemDomain, OWLIndividual individualGoal, boolean remove);

	OWLOntology getACEPlannerActiveOntology();

	ACEPlannerModel reloadProblemDomain();

	List<ACESnippet> getSnippetMissing(List<ACESentence> aceSentences);

	ACESnippet getACESnippets(List<ACESentence> sentences);

	boolean updateACEText(List<String> text, ModelEventType type);

	ACEPlannerModel getCurrentlySavedACEPlannerModel();

	ACEPlannerModel getCurrentlyBeingModifiedACEPlannerModel();

	void checkAndInit();

	boolean isSavedPlannerLoaded();

	boolean isPlannerDataExists();

	void addListener(ACEPlannerListener<ACEPlannerEvent<ModelEventType>> listener);

	void removeListener(ACEPlannerListener<ACEPlannerEvent<ModelEventType>> listener);

	void setInitCompleted(boolean b);

	void fireEvent(ModelEventType type);
	
	OWLModelManager getModelManager();

	OWLDataFactory getDataFactory();

}
