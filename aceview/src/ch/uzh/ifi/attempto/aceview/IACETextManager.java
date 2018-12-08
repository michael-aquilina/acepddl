/*
 * This file is part of ACE View.
 * Copyright 2008-2010, Attempto Group, University of Zurich (see http://attempto.ifi.uzh.ch).
 *
 * ACE View is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * ACE View is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with ACE View.
 * If not, see http://www.gnu.org/licenses/.
 */

package ch.uzh.ifi.attempto.aceview;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.protege.editor.core.ProtegeApplication;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.find.OWLEntityFinder;
import org.protege.editor.owl.model.parser.ProtegeOWLEntityChecker;
import org.protege.editor.owl.ui.renderer.OWLRendererPreferences;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.expression.ParserException;
import org.semanticweb.owlapi.io.OWLRendererException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomChange;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import ch.uzh.ifi.attempto.ace.ACESentence;
import ch.uzh.ifi.attempto.aceview.lexicon.EntryType;
import ch.uzh.ifi.attempto.aceview.lexicon.LexiconUtils;
import ch.uzh.ifi.attempto.aceview.lexicon.TokenMapper;
import ch.uzh.ifi.attempto.aceview.lexicon.TokenMapperImpl;
import ch.uzh.ifi.attempto.aceview.model.event.ACEViewEvent;
import ch.uzh.ifi.attempto.aceview.model.event.ACEViewListener;
import ch.uzh.ifi.attempto.aceview.model.event.TextEventType;
import ch.uzh.ifi.attempto.aceview.model.event.SnippetEventType;
import ch.uzh.ifi.attempto.aceview.util.OntologyUtils;
import ch.uzh.ifi.attempto.owl.VerbalizerWebservice;

/**
 * <p>The ACE text manager keeps track of the open ACE texts, how
 * they map to open OWL ontologies, and which one of them is active.</p>
 * 
 * <p>The ACE text manager allows snippets to be added to and removed from
 * the ACE texts so that the corresponding ontology is updated
 * by adding/removing the affected axioms.</p>
 * 
 * <p>Note: selected snippet is independent from the ACE text, e.g. we can select a snippet
 * which does not belong to any text, e.g. entailed snippets are such.</p>
 * 
 * @author Kaarel Kaljurand
 */
public interface IACETextManager {
	
	public IRI getAcetextIRI();
	/**
	 * <p>If <code>acetiveACETextID == null</code> then it means that
	 * no ACE text has been created yet as one ACE text must
	 * always be active. In this case we create a new ACE text
	 * and set it active. Otherwise we change the active ACE text
	 * according to the given ID.</p>
	 * 
	 * @param id
	 */
	public void setActiveACETextID(OWLOntologyID id);


	public ACEText<OWLEntity, OWLLogicalAxiom> getActiveACEText();


	public ACEText<OWLEntity, OWLLogicalAxiom> getACEText(OWLOntologyID id);


	public TokenMapper getActiveACELexicon();


	/**
	 * <p>Returns the ACE lexicon (TokenMapper)
	 * that decides the surface forms of the snippets in this text.</p>
	 * 
	 * @param id
	 * @return Lexicon
	 */
	public TokenMapper getACELexicon(OWLOntologyID id);


	public void setOWLModelManager(OWLModelManager mm) ;


	public OWLModelManager getOWLModelManager();


	/**
	 * <p>Adds the given snippet to the active ACE text, and
	 * adds the axioms of the snippet to the ontology that corresponds
	 * to the ACE text.</p>
	 * 
	 * @param snippet ACE snippet
	 */
	public void addSnippet(ACESnippet snippet);


	/**
	 * <p>Removes the given snippet from the active ACE text, and
	 * removes the axioms of the snippet from the ontology that corresponds
	 * to the ACE text.</p>
	 * 
	 * @param snippet ACE snippet
	 */
	public void removeSnippet(ACESnippet snippet);


	/**
	 * <p>Updates the given snippet in the active text at the given index
	 * by first removing the snippet,
	 * then creating a new snippet out of the set of given sentences, and then
	 * adding the new snippet to the text and setting it as the selected snippet.</p>
	 * 
	 * @param index Index of the snippet in the ACE text
	 * @param snippet Snippet to be updated (i.e replaced)
	 * @param sentences Sentences that form the new snippet
	 */
	public void updateSnippet(int index, ACESnippet snippet, List<ACESentence> sentences);


	/**
	 * <p>Adds a collection of ACE sentences and removes another collection
	 * of ACE sentences to/from the active ACE text.</p>
	 * 
	 * @param addedSentences Collection of ACE sentences
	 * @param removedSentences Collection of ACE sentences
	 */
	public void addAndRemoveSentences(Collection<ACESentence> addedSentences, Collection<ACESentence> removedSentences);


	public void addAndRemoveItems(Collection<List<ACESentence>> addedSentences, Collection<ACESnippet> removedSnippets);

	public void addListener(ACEViewListener<ACEViewEvent<TextEventType>> listener);
	public void removeListener(ACEViewListener<ACEViewEvent<TextEventType>> listener);


	public void addSnippetListener(ACEViewListener<ACEViewEvent<SnippetEventType>> listener);

	public void removeSnippetListener(ACEViewListener<ACEViewEvent<SnippetEventType>> listener);


	// TODO: should be private
	public void fireEvent(TextEventType type);


	// TODO: This is called only from ACEViewTab
	public void addAxiomsToOntology(OWLOntologyManager ontologyManager, OWLOntology ontology, Set<? extends OWLAxiom> axioms);

	/**
	 * <p>Creates a new OWL ontology manager, but uses an existing
	 * OWL data factory.</p>
	 * 
	 * @return OWL ontology manager
	 */
	public OWLOntologyManager createOWLOntologyManager();


	public String wrapInHtml(String body);

	/**
	 * @deprecated
	 * 
	 * <p>Finds (a single) OWL entity based on the <code>EntryType</code> and a lemma of
	 * an ACE word.</p>
	 * 
	 * TODO: "lemma of an ACE word" should really be "IRI of an OWL entity"!
	 * 
	 * FIXED: now using "false" in the getMatching*() calls.
	 * We are interested in an exact match and not a prefix or regexp match.
	 * Note that <code>getEntities(String)</code> does either wildcard or regexp matching,
	 * depending on the preferences. Therefore, we should escape all the
	 * wildcard symbols in the content words before we start matching.
	 * Maybe there is a less powerful entity finder somewhere, we don't really
	 * need regexp support when clicking on the words.
	 * 
	 * TODO: Get rid of this method. It is only used by WordsHyperlinkListener, which
	 * we should also remove, and replace it with a view which can hold the entities and
	 * thus does not have search them via some string-based encoding (which is slow).
	 *
	 * @param type Type (word class) of the lemma
	 * @param lemma Lemma of a word
	 * @return A single OWL entity that corresponds to the type-lemma combination
	 */
	public OWLEntity findEntity(EntryType type, String lemma) ;


	/**
	 * <p>Returns the entity that matches the IRI and the ACE word class
	 * (CN, TV, PN).</p>
	 * 
	 * TODO: Think about object and data properties. If TV corresponds to both
	 * then this method has to deal with the ambiguity.
	 * 
	 * @param type ACE word class (CN, TV, PN)
	 * @param iri OWL entity IRI
	 * @return OWL entity that matches the ACE word class and the IRI
	 */
	public OWLEntity findEntity(EntryType type, IRI iri);


	/**
	 * <p>Creates an ACE snippet from the given OWL axioms,
	 * and selects the created snippet.</p>
	 *
	 * @param axiom OWL axiom
	 * @throws OWLRendererException
	 * @throws OWLOntologyCreationException
	 * @throws OWLOntologyChangeException
	 */
	public void setSelectedSnippet(OWLLogicalAxiom axiom) throws OWLRendererException, OWLOntologyCreationException, OWLOntologyChangeException;


	/**
	 * <p>Selects the given snippet. Note that if the given snippet
	 * is already selected, then it is reselected. This is needed
	 * in order to refresh the views, if the only change was
	 * in terms of axiom annotations added to the snippet's axiom.</p>
	 * 
	 * @param snippet ACE snippet
	 */
	public void setSelectedSnippet(ACESnippet snippet);


	public ACESnippet getSelectedSnippet() ;


	public void setWhySnippet(ACESnippet snippet);


	public ACESnippet getWhySnippet();


	public void setInitCompleted(boolean b) ;


	/**
	 * <p>Remove the set of given logical axioms from the ontology, i.e. generate the respective
	 * list of changes. Note that the removed axioms do not have to match structurally against
	 * the given axioms.
	 * It is only important that the logical part matches, i.e. annotations are ignored.</p>
	 * 
	 * @param ont OWL ontology to be modified
	 * @param axioms Set of logical axioms to be removed
	 * @return List of axiom removal changes
	 */
	public List<? extends OWLAxiomChange> getRemoveChanges(OWLOntology ont, Set<OWLLogicalAxiom> axioms) ;


	/**
	 * <p>Converts the given OWL logical axiom into its corresponding ACE snippet.</p>
	 *  
	 * TODO: We should not necessarily set the namespace to be equivalent to the
	 * active ontology namespace.
	 * 
	 * @param axiom OWL axiom
	 * @return ACE snippet that corresponds to the given OWL axiom
	 * @throws OWLRendererException
	 * @throws OWLOntologyCreationException
	 * @throws OWLOntologyChangeException
	 */
	public ACESnippet makeSnippetFromAxiom(OWLLogicalAxiom axiom) throws OWLRendererException, OWLOntologyCreationException, OWLOntologyChangeException ;
	public void resetSelectedSnippet() ;


	public void processTanglingAxioms(ACEText<OWLEntity, OWLLogicalAxiom> acetext, Set<OWLLogicalAxiom> tanglingAxioms) ;

	/**
	 * <p>Returns a list of annotations that annotate the logical axioms of the given snippet.</p>
	 *  
	 * TODO: Why do we return a list? Because it is simpler to update a table model in this way
	 * 
	 * @param snippet ACE snippet
	 * @return List of annotations for the axiom of the given snippet
	 */
	public List<OWLAnnotation> getAnnotations(ACESnippet snippet);


	/**
	 * @deprecated
	 * 
	 * <p>Returns a list of annotations that annotate the logical axioms of the given snippet.
	 * Only the ACE text annotation is not returned because this is already
	 * explicitly present in the snippet.</p>
	 *  
	 * TODO: Why do we return a list? Because it is simpler to update a table model in this way
	 * 
	 * @param snippet ACE snippet
	 * @return List of annotations for the given snippet
	 */
	public List<OWLAnnotation> getAnnotationsExceptAcetext(ACESnippet snippet);

	/**
	 * <p>Returns the rendering of the entity, as decided by the current renderer
	 * in the model manager.</p>
	 * <p>The renderer adds quotes around strings that contains spaces
	 * (e.g. a label like "Eesti Vabariik"). We remove such quotes,
	 * otherwise it would confuse the sorter.</p>
	 * 
	 * TODO: maybe fall back to: entity.getIRI().getFragment()
	 * 
	 * @param entity OWL entity to be rendered
	 * @return Rendering without quotes
	 */
	public String getRendering(OWLEntity entity) ;


	/**
	 * <p>Interprets the given ACE sentence as an expression in
	 * Manchester OWL Syntax and parses it to an OWL logical axiom.</p>
	 * 
	 * @param sentence
	 * @param base
	 * @return OWL logical axiom
	 * @throws ParserException
	 */
	public OWLLogicalAxiom parseWithMos(ACESentence sentence, String base) throws ParserException ;

	/**
	 * <p>Creates a new snippet from the given OWL axiom, and
	 * adds the snippet to the given ACE text.
	 * See also {@link #addSnippet(ACEText, ACESnippet)}.</p>
	 * 
	 * @param acetext ACE text
	 * @param axiom OWL logical axiom
	 */
	/*
	private void addAxiom(ACEText<OWLEntity, OWLLogicalAxiom> acetext, OWLLogicalAxiom axiom) {		
		AxiomVerbalizer axiomVerbalizer = createAxiomVerbalizer(acetext.getACELexicon());
		OWLModelManager mm = getOWLModelManager();
		OWLOntology ont = mm.getActiveOntology();
		ACESnippet snippet = null;
		try {
			snippet = axiomVerbalizer.verbalizeAxiom(ont.getURI(), axiom);
			addSnippet(acetext, snippet);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	 */


	/**
	 * @deprecated
	 * 
	 * <p>Constructs a new axiom on the basis of the given axiom.
	 * The new axiom will have some additional annotations based on
	 * the given snippet, namely the textual content of the snippet
	 * and the timestamp of the snippet.</p>
	 * 
	 * @param df OWLDataFactory
	 * @param axiom Axiom to be annotated
	 * @param snippet Snippet that provides the content of the annotations
	 * @return New axiom (i.e. old axiom with new annotations)
	 */
	public OWLLogicalAxiom annotateAxiomWithSnippet(OWLDataFactory df, OWLLogicalAxiom axiom, ACESnippet snippet) ;

}