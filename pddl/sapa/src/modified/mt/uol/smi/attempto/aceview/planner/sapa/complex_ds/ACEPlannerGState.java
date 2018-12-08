/********************************************************************
    Author: Minh B. Do -- Arizona State University
*******************************************************************/
package mt.uol.smi.attempto.aceview.planner.sapa.complex_ds;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.inference.OWLReasonerManagerImpl;
import org.protege.editor.owl.model.inference.ProtegeOWLReasonerInfo;
import org.protege.editor.owl.ui.ontology.wizard.create.CreateOntologyWizard;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomChange;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import ch.uzh.ifi.attempto.ace.ACESentence;
import ch.uzh.ifi.attempto.ace.ACESplitter;
import ch.uzh.ifi.attempto.aceview.ACEAnswer;
import ch.uzh.ifi.attempto.aceview.ACESnippet;
import ch.uzh.ifi.attempto.aceview.ACESnippetImpl;
import ch.uzh.ifi.attempto.aceview.ACEText;
import ch.uzh.ifi.attempto.aceview.AddAxiomByACEView;
import ch.uzh.ifi.attempto.aceview.RemoveAxiomByACEView;
import ch.uzh.ifi.attempto.aceview.lexicon.TokenMapper;
import ch.uzh.ifi.attempto.aceview.model.event.TextEventType;
import ch.uzh.ifi.attempto.aceview.util.OntologyUtils;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerActionModel;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerConditionModel;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerEffectTuple;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerParameter;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerSatisfiedActionModel;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerSatisifiedParameter;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerSnippet;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerSnippetComparator;

public class ACEPlannerGState {
	private final String baseIri = "http://attempto.ifi.uzh.ch/ace-Planner/g-state/";
	private String iri = "";
	private int count;
	private String gStateName = "";
	private ACEPlannerPredDB acetext;
	private static ACEPlannerPredDB allAcetext;
	private OWLOntology ontology;
	private OWLOntologyManager ontologyManager;
	private OWLDataFactory dataFactory;
	private OWLModelManager modelManager;
	private TokenMapper aceLexicon;
	private static final Logger logger = Logger.getLogger(ACEPlannerGState.class);

	public ACEPlannerGState(OWLModelManager modelManager, OWLDataFactory dataFactory, TokenMapper aceLexicon) {
		acetext = new ACEPlannerPredDB();
		this.dataFactory = dataFactory;
		this.modelManager = modelManager;
		this.aceLexicon = aceLexicon;
		// Get the reasoner manager instance

		try {
			createNewOntology();
		} catch (Exception e) {
			this.logger.error("Couldn't create ontology in GState with IRI " + this.iri + " : " + e.getMessage(), e);
		}
	}

	private void createNewOntology() {
		try {

			this.ontologyManager = OWLManager.createOWLOntologyManager();
			this.gStateName = UUID.randomUUID().toString();
			this.iri = baseIri + gStateName;
			OWLOntologyID id = new OWLOntologyID(IRI.create(new URI(this.iri)), IRI.create(new URI(getNewIri())));
			this.ontology = this.ontologyManager.createOntology(id);
		} catch (Exception e) {
			this.logger.error("Couldn't create ontology in GState with IRI " + this.iri + " : " + e.getMessage(), e);
		}
	}

	public TokenMapper getAceLexicon() {
		return aceLexicon;
	}

	public void setAceLexicon(TokenMapper aceLexicon) {
		this.aceLexicon = aceLexicon;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	private String getNewIri() {
		this.count = this.count + 1;
		return this.iri + "-" + this.count;
	}

	public ACEPlannerGState(ACEPlannerGState s) {
		this.dataFactory = s.getDataFactory();
		this.ontologyManager = s.getOntologyManager();
		this.modelManager = s.getModelManager();
		this.ontology = s.getOntology();
		this.dataFactory = s.getDataFactory();
		this.gStateName = UUID.randomUUID().toString();
		this.count = s.getCount();
		this.aceLexicon = s.getAceLexicon();
		setAcetextSnippets(s.getAcetext().getSnippets());
	}

	public String getIri() {
		return iri;
	}

	public void setIri(String iri) {
		this.iri = iri;
	}

	public String getgStateName() {
		return gStateName;
	}

	public void setgStateName(String gStateName) {
		this.gStateName = gStateName;
	}

	public OWLOntology getOntology() {

		return ontology;
	}

	public void setOntology(OWLOntology ontology) {
		this.ontology = ontology;
	}

	public OWLOntologyManager getOntologyManager() {
		return ontologyManager;
	}

	public void setOntologyManager(OWLOntologyManager ontologyManager) {
		this.ontologyManager = ontologyManager;
	}

	public ACEPlannerPredDB getAcetext() {
		return acetext;
	}

	public OWLModelManager getModelManager() {
		return modelManager;
	}

	public void setModelManager(OWLModelManager modelManager) {
		this.modelManager = modelManager;
	}

	public OWLDataFactory getDataFactory() {
		return dataFactory;
	}

	public void setDataFactory(OWLDataFactory dataFactory) {
		this.dataFactory = dataFactory;
	}

	public void setAcetext(List<ACEPlannerSnippet> plannerSnippets) {

		setAcetextSnippets(plannerSnippets);
	}

	public void setAcetextSnippets(List<ACEPlannerSnippet> snippets) {
		acetext = new ACEPlannerPredDB();
		addAndRemoveItems(snippets, null);
	}

	/** Check if an action is applicable to this state */
	public List<ACEPlannerSatisfiedActionModel> applicable(ACEPlannerActionModel a) {

		logger.debug("Checking if action is applicable : " + a.getActionName());
		List<ACEPlannerSatisfiedActionModel> applicableActionsCombinations = new ArrayList<ACEPlannerSatisfiedActionModel>();
		List<ConditionIndividualListPairs> conditionPairs = new ArrayList<>();

		OWLObjectRenderer owlObjectRenderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();

		// we set the ontology before questioning
		this.updateOntologyWithAceText();

		// 1) get hold of the OWLReasonerManager from the OWLOntologyManager,
		OWLReasonerManagerImpl impl = new OWLReasonerManagerImpl(this.modelManager);
		// 2) from this get hold of the current reasoner factory, actually and instance
		// of ProtegeOWLReasonerInfo
		ProtegeOWLReasonerInfo rf = impl.getCurrentReasonerFactory();
		// 3) from the instance of ProtegeOWLReasonerInfo get hold of the
		// OWLReasonerFactory
		// 4) create an instance of the reasoner using OWLReasonerFactory using the
		// createNonBufferingReasoner method
		OWLReasoner owlReasoner = rf.getReasonerFactory().createReasoner(ontology);

		for (ACEPlannerConditionModel condition : a.getConditions()) {

			// check if there is an answer for each condition.
			// if not, then the action is not satisified and is not applicable
			condition.getAcePlannerSnippet().setAceSnippet(getACESnippet(condition.getSentence()));
			ACESnippet question = condition.getAcePlannerSnippet().getAceSnippet();

			ACEPlannerAnswer answer = getAnswersWithIndividuals(question, owlReasoner);
			if (answer == null || answer.getIndividuals() == null || answer.getIndividuals().size() == 0) {
				logger.trace("No answer for this condition. Action is not applicable");
				return null;
			} else {
				logger.trace("Answer for this condition found!");
				List<Individual> individuals = new ArrayList<Individual>();
				for (Node<OWLNamedIndividual> individual : answer.getIndividuals()) {

					// get the name of the parameter to replace
					String individualName = owlObjectRenderer.render(individual.getRepresentativeElement());
					logger.trace("Individual is ->" + individualName + "<- ...");
					individuals.add(new Individual(individual, individualName));
				}
				ConditionIndividualListPairs c = new ConditionIndividualListPairs(
						new ACEPlannerConditionModel(condition), individuals);
				conditionPairs.add(c);
			}
		}

		if (conditionPairs.isEmpty()) {
			return null;
		}

		// we get all combinations
		ConditionsCombinations combinations = getConditionCombinations(conditionPairs);

		logger.debug("Action is applicable : " + a.getActionName() + ". Condition Combinations : "
				+ combinations.Combinations.size());

		for (ConditionsCombination conditionCombination : combinations.Combinations) {

			// for every condition combination, we add a satisifed action
			ACEPlannerSatisfiedActionModel applicableAction = new ACEPlannerSatisfiedActionModel(
					new ACEPlannerActionModel(a));

			for (ConditionIndividualPair pair : conditionCombination.Pairs) {

				Individual individual = pair.Individual;
				ACEPlannerConditionModel satisfiedConditon = pair.Condition;

				// we substitute the parameter of the condition by the individual in the
				// combination
				for (ACEPlannerEffectTuple effect : applicableAction.getAction().getEffects()) {
					ACEPlannerParameter parameter = satisfiedConditon.getParameters().get(0);
					String sentence = effect.getUnformattedResult().getSentence();
					String newSentence = sentence.replaceAll(parameter.getParameterValue(), individual.IndividualName);
					logger.trace("++ Formatted sentence " + (effect.isNegated() ? "to remove" : "to add") + " is "
							+ newSentence);
					// add new sentence as the possible effect
					ACESnippet snippet = getACESnippet(newSentence);
					effect.setFormattedResult(new ACEPlannerSnippet(snippet));
					ACEPlannerSatisifiedParameter satisfiedParam = new ACEPlannerSatisifiedParameter(parameter);
					satisfiedParam.setSatisifiedIndividual(individual.Individual);
					satisfiedParam.setSatisifiedIndividualName(individual.IndividualName);
					effect.getSatisifedParameters().add(satisfiedParam);
				}
			}
			applicableActionsCombinations.add(applicableAction);
		}
		return applicableActionsCombinations;

	}

	private ACESnippet getACESnippet(String sentence) {
		ACEPlannerSnippet plannerSnippet = getAllAcetext().findSentencePhrase(sentence);
		if (plannerSnippet != null && plannerSnippet.getAceSnippet() != null
				&& plannerSnippet.getAceSnippet().hasAxioms()) {
			return plannerSnippet.getAceSnippet();
		} else {
			logger.debug("Generating snippet : " + sentence);
			ACESnippet snippet = new ACESnippetImpl_Planner(getOntology().getOntologyID(),
					ACESplitter.getSentences(sentence), modelManager, aceLexicon);
			if (snippet == null || snippet.getLogicalAxioms() == null) {
				logger.error("Snippet tried to be generated but it was null");
			} else if (snippet.getLogicalAxioms().size() == 0) {
				logger.error("Snippet tried to be generated but it had no logical axioms");
			}
			getAllAcetext().addSnippet(new ACEPlannerSnippet(snippet));
			return snippet;
		}
	}

	public ACEPlannerAnswer getAnswersWithIndividuals(ACESnippet question, OWLReasoner owlReasoner) {
		logger.trace("Checing if condition is applicable : " + question.toString());
		logger.trace(
				"Trying to get answer for question in text in state with snippets : " + acetext.getSnippets().size());
		if (question.hasAxioms()) {
			String text = "Sentences to search through is:";
			for (ACEPlannerSnippet snippet : acetext.getSnippets()) {
				text += "\n" + snippet.getSentence();
			}
			logger.trace(text);
			ACEPlannerAnswer answer = new ACEPlannerAnswer(this.ontologyManager, owlReasoner, question);
			if (answer.isSatisfiable() && answer.getIndividualsCount() > 0) {
				logger.trace("Answer is satisfied with individuals!");
				return answer;
			} else {
				logger.trace("Answer is not satisfiable with individuals...");
			}
		} else {
			logger.error("Question has no axioms...");
		}
		return null;
	}

	/** Update a state with changes upon applying an action */
	public void update(ACEPlannerSatisfiedActionModel a) {
		// this.logger.debug("+*+*+*+*+*+* - Updating state with action -
		// +*+*+*+*+*+*+*+");
		Collection<ACEPlannerSnippet> addedSentences = new ArrayList<ACEPlannerSnippet>();
		Collection<ACEPlannerSnippet> removedSnippets = new ArrayList<ACEPlannerSnippet>();
		for (ACEPlannerEffectTuple tuple : a.getAction().getEffects()) {
			if (tuple.isNegated()) {
				this.logger.trace("Will remove " + tuple.getFormattedResult().getAceSnippet().toString());
				removedSnippets.add(tuple.getFormattedResult());
			} else {
				this.logger.trace("Will add " + tuple.getFormattedResult().getAceSnippet().toString());
				addedSentences.add(tuple.getFormattedResult());
			}
		}
		addAndRemoveItems(addedSentences, removedSnippets);
	}

	private void addAndRemoveItems(Collection<ACEPlannerSnippet> addedSnippets,
			Collection<ACEPlannerSnippet> removedSnippets) {
		if (addedSnippets != null) {
			for (ACEPlannerSnippet snippet : addedSnippets) {
				acetext.addSnippet(snippet);
			}
		}
		if (removedSnippets != null) {
			for (ACEPlannerSnippet oldSnippet : removedSnippets) {
				acetext.removeSnippet(oldSnippet);
			}
		}
		if (!((addedSnippets != null && addedSnippets.isEmpty())
				&& (removedSnippets != null && removedSnippets.isEmpty()))) {
			updateOntologyWithAceText();
		}
	}

	private void updateOntologyWithAceText() {
		List<OWLAxiomChange> changes = Lists.newArrayList();

		logger.trace("Removing all axioms");

		ontologyManager.removeAxioms(this.ontology, this.ontology.getAxioms());
		changes = Lists.newArrayList();
		if (acetext == null)
			acetext = new ACEPlannerPredDB();

		String text = "Updating ontology with snippet : ";
		// Collections.sort(acetext.getSnippets(), new ACEPlannerSnippetComparator());
		for (ACEPlannerSnippet snippet : acetext.getSnippets()) {
			text += "\n" + snippet.getAceSnippet().toString();
			snippet.setAceSnippet(getACESnippet(snippet.getSentence()));
			changes.addAll(getAddChanges(snippet.getAceSnippet()));
			acetext.addSnippet(snippet);
		}

		logger.trace(text);
		ontologyManager.applyChanges(changes);

	}

	private List<? extends OWLAxiomChange> getRemoveChanges(ACESnippet snippet) {
		OWLOntology ont = getOntology();
		List<RemoveAxiom> changes = Lists.newArrayList();
		Set<OWLLogicalAxiom> snippetAxioms = snippet.getLogicalAxioms();

		// If the snippet corresponds to a single axiom, then we
		// annotate this axiom with ACE View specific annotations.
		if (snippetAxioms.size() == 1) {
			OWLLogicalAxiom axiom = snippetAxioms.iterator().next();
			// OWLLogicalAxiom annotatedAxiom =
			// annotateAxiomWithSnippet(owlModelManager.getOWLDataFactory(), axiom,
			// snippet);
			// changes.add(new AddAxiomByACEView(ontology, annotatedAxiom));
			changes.add(new RemoveAxiom(ontology, axiom));
		} else {
			for (OWLLogicalAxiom axiom : snippetAxioms) {
				changes.add(new RemoveAxiom(ontology, axiom));
			}
		}

		if (changes.size() == 0) {
			logger.error("There are 0 axioms to remove for snippet -> " + snippet.toString());
		} else {
			logger.trace("There are " + changes.size() + " axioms to remove for snippet -> " + snippet.toString());
		}

		return changes;
	}

	private List<? extends OWLAxiomChange> getAddChanges(ACESnippet snippet) {
		OWLOntology ont = getOntology();
		List<AddAxiom> changes = Lists.newArrayList();
		Set<OWLLogicalAxiom> snippetAxioms = snippet.getLogicalAxioms();

		for (OWLLogicalAxiom axiom : snippetAxioms) {
			changes.add(new AddAxiom(ontology, axiom));
		}

		if (changes.size() == 0) {
			logger.error("There are 0 axioms to add for snippet -> " + snippet.toString());
		} else {
			logger.trace("There are " + changes.size() + " axioms to add for snippet -> " + snippet.toString());
		}

		return changes;
	}

	public void setAcetext(ACEPlannerPredDB state) {
		this.setAcetextSnippets(state.getSnippets());

	}

	public boolean hasSnippet(ACEPlannerSnippet snippet) {
		return this.acetext.findSnippet(snippet) != null;
	}

	public static ACEPlannerPredDB getAllAcetext() {
		return ACEPlannerGState.allAcetext;
	}

	public static void setAllAcetext(ACEPlannerPredDB allAcetext) {
		ACEPlannerGState.allAcetext = allAcetext;
	}

	public static void main(String[] args) {
		// int[] conditions = { 1, 2, 3 };
		// List<ConditionIndividualPairs> pairs = new
		// ArrayList<ConditionIndividualPairs>(;)
		// List<String> individuals1 = new ArrayList<String>();
		// List<String> individuals2 = new ArrayList<String>();
		//
		// individuals1.add("a");
		// individuals1.add("b");
		//
		// individuals2.add("cc");
		// individuals2.add("bb");
		//
		// pairs.add(new ConditionIndividualPairs("1", individuals1));
		// pairs.add(new ConditionIndividualPairs("2", individuals2));
		//
		// List<String> answers = new ArrayList<String>();
		// String action = "Action A ";
		// for(ConditionIndividualPairs p : pairs) {
		// String answer = "";
		// for(String c: p.Individuals) {
		// answer += (p.Condition + " " + c);
		// }
		// }
		// System.out.println(answer);
		//
		// for(String a : answers) {
		// System.out.println("Answer " + a);
		// }
		List<ConditionIndividualListPairs> pairs = new ArrayList<ConditionIndividualListPairs>();
		List<Individual> ii = new ArrayList<Individual>();
		ii.add(new Individual(null, "Room-one"));
		ii.add(new Individual(null, "Room-two"));
		pairs.add(new ConditionIndividualListPairs(new ACEPlannerConditionModel("Con a", null), ii));
		// pairs.add(new ConditionIndividualListPairs(new ACEPlannerConditionModel("Con
		// b", null), ii2));
		// pairs.add(new ConditionIndividualListPairs("b", new String[] { "66", "77",
		// "88" }));
		// pairs.add(new ConditionIndividualListPairs("c", new String[] { "11", "22"
		// }));

		// List<ConditionIndividualListPairs> lists = new
		// ArrayList<ConditionIndividualListPairs>();
		// for (ConditionIndividualListPairs i : pairs) {
		// List<String> individuals = generateTable(0, i.Individuals, new
		// String[i.Individuals.length]);
		// lists.add(new ConditionIndividualListPairs(i.Condition,
		// (String[]) individuals.toArray(new String[individuals.size()])));
		// }
		// for (ConditionIndividualPairs i : lists) {
		// System.out.println(i.Condition + " ");
		// for (String s : i.Individuals) {
		// System.out.println(s + " ");
		// }
		// System.out.println();
		// }

		// printCombinations(new String[][] {{ "44", "55" },{ "66", "77", "88" },{ "11",
		// "22" }}, 0, "");

		getConditionCombinations(pairs);
	}

	private static ConditionsCombinations getConditionCombinations(List<ConditionIndividualListPairs> pairs) {
		List<String[]> sets = new ArrayList<String[]>();
		for (ConditionIndividualListPairs p : pairs) {
			String[] names = new String[p.Individuals.length];
			for (int i = 0; i < p.Individuals.length; i++) {
				names[i] = p.Individuals[i].IndividualName;
			}
			sets.add(names);
		}

		String[][] arraySets = new String[sets.size()][0];
		for (int i = 0; i < sets.size(); i++) {
			arraySets[i] = sets.get(i);
		}

		Object[][] combinations = getCombinations(arraySets);

		ConditionsCombinations conditionsCombinations = new ConditionsCombinations();

		for (int i = 0; i < combinations.length; i++) {
			ConditionsCombination c = new ConditionsCombination();
			c.Index = i;
			for (int j = 0; j < combinations[0].length; j++) {
				c.Pairs.add(new ConditionIndividualPair(pairs.get(j).Condition,
						new Individual(null, combinations[i][j].toString())));
			}
			conditionsCombinations.Combinations.add(c);
		}

		for (ConditionsCombination combination : conditionsCombinations.Combinations) {
			String message = "Combination " + combination.Index + " => ";
			for (ConditionIndividualPair pair : combination.Pairs) {
				message += "{ " + pair.Condition.getSentence() + " -> " + pair.Individual.IndividualName + " } ";
			}
			logger.trace(message);
		}
		return conditionsCombinations;
	}

	/**
	 * As found on
	 * https://stackoverflow.com/questions/16549831/all-possible-combination-of-n-sets
	 * 
	 * https://stackoverflow.com/a/16551472
	 * 
	 * Accessed March 10th 2018
	 * 
	 * @param sets
	 * @return
	 */
	private static Object[][] getCombinations(Object[][] sets) {

		int[] counters = new int[sets.length];
		int count = 1;
		int count2 = 0;

		for (int i = 0; i < sets.length; i++) {
			count *= sets[i].length;
		}

		Object[][] combinations = new Object[count][sets.length];

		do {
			combinations[count2++] = getCombinationString(counters, sets);
		} while (increment(counters, sets));

		return combinations;
	}

	private static Object[] getCombinationString(int[] counters, Object[][] sets) {

		Object[] o = new Object[counters.length];
		for (int i = 0; i < counters.length; i++) {
			o[i] = sets[i][counters[i]];
		}
		return o;

	}

	private static boolean increment(int[] counters, Object[][] sets) {
		for (int i = counters.length - 1; i >= 0; i--) {
			if (counters[i] < sets[i].length - 1) {
				counters[i]++;
				return true;
			} else {
				counters[i] = 0;
			}
		}
		return false;
	}

	private static List<String> generateTable(int index, String[] values, String[] current) {
		if (index == values.length) { // generated a full "solution"
			String line = "";
			for (int i = 0; i < values.length; i++) {
				line += current[i] + " ";
			}
			List<String> result = new ArrayList<String>();
			result.add(line);
			return result;
		} else {
			List<String> result = new ArrayList<String>();
			for (int i = 0; i < values.length; i++) {
				current[index] = values[i];
				result.addAll(generateTable(index + 1, values, current));
			}
			return result;
		}
	}
}

class ConditionsCombinations {
	public List<ConditionsCombination> Combinations = new ArrayList<ConditionsCombination>();
}

class ConditionsCombination {
	public int Index;
	public List<ConditionIndividualPair> Pairs = new ArrayList<ConditionIndividualPair>();
}

class ConditionIndividualPair {
	public ACEPlannerConditionModel Condition;
	public Individual Individual;

	public ConditionIndividualPair(ACEPlannerConditionModel c, Individual i) {
		Condition = c;
		Individual = i;
	}
}

class ConditionIndividualListPairs {
	public ACEPlannerConditionModel Condition;
	public Individual[] Individuals;

	public ConditionIndividualListPairs(ACEPlannerConditionModel c, List<Individual> i) {
		Condition = c;
		Individuals = new Individual[0];
		Individuals = i.toArray(Individuals);
	}

	public ConditionIndividualListPairs(ACEPlannerConditionModel c, Individual[] i) {
		Condition = c;
		Individuals = i;
	}
}

class Individual {
	public Node<OWLNamedIndividual> Individual;
	public String IndividualName;

	public Individual(Node<OWLNamedIndividual> Individual, String IndividualName) {
		this.Individual = Individual;
		this.IndividualName = IndividualName;
	}
}

// class ConditionIndividualPair {
// public String Condition;
// public String Individual;
//
// public ConditionIndividualPair(String c, String i) {
// Condition = c;
// Individual = i;
// }
// }
//
// class ConditionIndividualListPairs {
// public String Condition;
// public String[] Individuals;
//
// public ConditionIndividualListPairs(String c, List<String> i) {
// Condition = c;
// i.toArray(Individuals);
// }
//
// public ConditionIndividualListPairs(String c, String[] i) {
// Condition = c;
// Individuals = i;
// }
// }