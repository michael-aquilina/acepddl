package mt.uol.smi.attempto.aceview.planner;

import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.OWLDataFactory;

import ch.uzh.ifi.attempto.aceview.lexicon.TokenMapper;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerModel;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerSolutionModel;

public interface IACEPlannerRunner {

	ACEPlannerModel getDomain();
	
	TokenMapper getAceLexicon();

	OWLModelManager getModelManager();

	OWLDataFactory getDataFactory();

	ACEPlannerSolutionModel getSolution(ACEPlannerModel model, OWLModelManager modelManager,
			OWLDataFactory dataFactory);
}
