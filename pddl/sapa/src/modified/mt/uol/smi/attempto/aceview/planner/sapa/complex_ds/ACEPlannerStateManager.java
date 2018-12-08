/**********************************************************************
   Author: Minh B. Do (Arizona State University - binhminh@asu.edu)
**********************************************************************/
package mt.uol.smi.attempto.aceview.planner.sapa.complex_ds;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.OWLAxiomChange;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;

import com.google.common.collect.Lists;

import ch.uzh.ifi.attempto.aceview.ACESnippet;
import ch.uzh.ifi.attempto.aceview.lexicon.TokenMapper;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerSatisfiedActionModel;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerSnippet;
import mt.uol.smi.attempto.aceview.planner.model.ACEPlannerSolutionModel;

/**
 * StateManager: StateManager has three elements: initial state, goal state, and
 * a sorted array of intermediate states (StateInfo class) that are reachable
 * from the initial state. The states in the queue are sorted according to their
 * distance values (h+g).
 */
public class ACEPlannerStateManager {
	/* For grounded InitState and GoalState */
	ACEPlannerGState gInitState;
	ACEPlannerGState gGoalState;
	ArrayList initEventQueue; // For exogenous states
	private static final Logger logger = Logger.getLogger(ACEPlannerStateManager.class);

	ACEPlannerStateQueue stateQueue; // Set of all GStateInfo explored. stateSet(0) = InitState

	// Sorted according to the distance to the goal-state
	int STATE_QUEUE_SIZE = 50000; // Limit on the number of states in queue

	public ACEPlannerStateManager(OWLModelManager modelManager, OWLDataFactory dataFactory, TokenMapper aceLexicon) {
		gInitState = new ACEPlannerGState(modelManager, dataFactory, aceLexicon);
		gGoalState = new ACEPlannerGState(modelManager, dataFactory, aceLexicon);
		initEventQueue = new ArrayList();

		stateQueue = new ACEPlannerStateQueue();
	}

	/** Set the initial predicate manager in the initial state */
	public void setInitPredDB(ACEPlannerPredDB initState) {
		gInitState.setAcetext(initState);
	}

	public int numInitEvent() {
		return initEventQueue.size();
	}

	public ArrayList getInitEvents() {
		return initEventQueue;
	}

	/** Set the goal predicate manager */
	public void setGoalPredDB(ACEPlannerPredDB goal) {
		gGoalState.setAcetext(goal);
	}

	public ACEPlannerGState getInitState() {
		return gInitState;
	}

	public ACEPlannerGState getGoalState() {
		return gGoalState;
	}

	/** Add a new (explored) state to the search queue */
	public void addStateInfo(ACEPlannerStateInfo tempSP) {
		int stpSize = stateQueue.size();
		// Check the limit
		/*
		 * if( stpSize > STATE_QUEUE_SIZE ) { stateQueue.remove(--stpSize); }
		 */

		stateQueue.add(tempSP);
	}

	/** Get the first (shortest distance) StateInfo in the queue to explore */
	public ACEPlannerStateInfo getStateInfo() {
		if (stateQueue.size() == 0)
			return null;

		return stateQueue.remove();
	}

	/** Reset the queue to contain only the initial state */
	public void resetStateQueue(ACEPlannerStateInfo initState) {
		stateQueue.clear();
		stateQueue.add(initState);
	}

	public void clearStateQueue() {
		stateQueue.clear();
	}

	public boolean isGoalMet(ACEPlannerStateInfo state) {
		logger.debug("*?*?*?*?*?*?*?*?*?*?*?- Checking goal state -*?*?*?*?*?*?*?*?*?*?*?*?*?*?*?*?*?");
		logger.debug("Checking if gGoalState is met or not the goal snippets : "
				+ gGoalState.getAcetext().getSnippets().size());
		if (state.getCurrentState().getAcetext().getSnippets().size() == 0) {
			logger.debug("The state passed is empty. Goal state is not met.");
			return false;
		}
		logger.debug("Goal states needing to be found : ");
		String snippets = "Goal snippets are ";
		for (ACEPlannerSnippet goalSnippet : gGoalState.getAcetext().getSnippets()) {
			snippets+= "\n" + goalSnippet.getSentence();
		}
		logger.debug(snippets);
		logger.debug("State being checks has the following : ");
		snippets = "State snippet are ";
		for (ACEPlannerSnippet stateSnippet : state.getSortedSnippets()) {
			snippets += "\n" + stateSnippet.getSentence();
		}
		snippets += "\n Actions: ";
		for(ACEPlannerSatisfiedActionModel a: state.getActions()) {
			snippets += "\n" + a.toString();
		}
		logger.debug(snippets);

		boolean goalMet = true;
		for (ACEPlannerSnippet snippet : gGoalState.getAcetext().getSnippets()) {
			logger.debug("Checking if gGoalState has " + snippet.getSentence());
			//
			// // if the axioms increase, then the goal axioms do not exist in the ontology
			// // and so the goal wasnt reached
			// int numberOfAxioms = state.getCurrentState().getOntology().getAxiomCount();
			// List<OWLAxiomChange> changes = Lists.newArrayList();
			// changes.addAll(state.getCurrentState().getAddChanges(snippet));
			// if(changes.size() == 0) {
			// this.logger.error("The goal state changes is empty!");
			// }
			// state.getCurrentState().changeOntology(changes);
			// int newNumberOfAxioms =
			// state.getCurrentState().getOntology().getAxiomCount();
			// goalMet &= numberOfAxioms == newNumberOfAxioms;
			goalMet &= state.getCurrentState().hasSnippet(snippet);
			if (!goalMet) {
				logger.debug("gGoalState does not have the snippet! -> " + snippet.getSentence());
				logger.debug("*?*?*?*?*?*?*?*?*?*?*?- Goal not found...*?*?*?*?*?*?*?*?*?*?*?*?*?*?*?*?*?");
				return false;
			} else {
				logger.debug("gGoalState has the snippet! -> " + snippet.getSentence());
			}
		}
		logger.debug("*?*?*?*?*?*?*?*?*?*?*?- " + (goalMet ? "Goals found!" : "This is odd ... but goal not found...")
				+ " -*?*?*?*?*?*?*?*?*?*?*?*?*?*?*?*?*?");
		return goalMet;
	}
}

class ACEPlannerStateQueue {
	private int size; // the size of this queue
	private int arraySize;
	private static final int DEFSIZE = 10000;
	private static final int GROW_BY = 1000;
	private ACEPlannerStateInfo[] states;

	/**
	 * default constructor.
	 */
	public ACEPlannerStateQueue() {
		states = new ACEPlannerStateInfo[arraySize = DEFSIZE];
	}

	/**
	 * 
	 * @param size
	 *            Estimated size of the queue.
	 */
	public ACEPlannerStateQueue(int size) {
		states = new ACEPlannerStateInfo[arraySize = size];
	}

	/**
	 * A copy constructor. Used to get the ordered array. Not intended for other
	 * uses.
	 * 
	 * @param queue
	 */
	private ACEPlannerStateQueue(ACEPlannerStateQueue queue) {
		this.size = queue.size;
		states = new ACEPlannerStateInfo[queue.states.length];
		arraySize = queue.arraySize;
		System.arraycopy(queue.states, 0, states, 0, queue.states.length);
	}

	/**
	 * Gives the size of this queue.
	 * 
	 * @returns The size of this queue.
	 * @see java.util.Collection#size()
	 */
	public int size() {
		return size;
	}

	/**
	 * We do not reinitialize or shrink the array because it has been observed that
	 * the queue does not change in size widely between states during the A* search.
	 */
	public void clear() {
		// clear states so garbage collector can do its duty
		for (int i = 0; i <= size; i++) {
			states[size] = null;
		}
		size = 0;
	}

	/**
	 * (Javadoc)
	 * 
	 * @see java.util.Collection#isEmpty()
	 */
	public boolean isEmpty() {
		if (size == 0)
			return true;
		return false;
	}

	/**
	 * Adds event into the queue at the appropriate spot.
	 * 
	 * @see java.util.Collection#add(java.lang.Object)
	 */
	public boolean add(ACEPlannerStateInfo si) {
		if (arraySize - 1 == size) {
			growArray(size);
		}

		int i = ++size;
		// "swim" AKA percolate
		for (; i > 1 && si.distance < states[i / 2].distance; i /= 2) {
			states[i] = states[i / 2];
		}
		states[i] = si;

		return true;
	}

	/**
	 * Gives an ordered array from this priority queue. This is a slow method, and
	 * should not be used often.
	 * 
	 * @return An ordered array from this priority queue.
	 */
	public ACEPlannerStateInfo[] toArray() {
		ACEPlannerStateQueue queue = new ACEPlannerStateQueue(this);
		int size = queue.states.length;
		ACEPlannerStateInfo[] array = new ACEPlannerStateInfo[size];
		for (int i = 0; i < size; i++) {
			array[i] = queue.remove();
		}
		return array;
	}

	/**
	 * 
	 * @param size
	 *            The current size of the arrays. Passed in for efficiency
	 *            (method-local parameters are almost always faster to access than
	 *            object-local variables)
	 */
	private void growArray(int size) {
		ACEPlannerStateInfo[] newEvents;
		int[] newOrderArray;
		int arraySize;

		newEvents = new ACEPlannerStateInfo[arraySize = size + GROW_BY + 1];

		System.arraycopy(states, 0, newEvents, 0, size + 1);
		states = newEvents;
		this.arraySize = arraySize;
	}

	/**
	 * Removes a StateInfo from the queue with the smallest time.
	 * 
	 * @return The removed StateInfo object.
	 */
	public ACEPlannerStateInfo remove() {
		if (isEmpty())
			return null;

		ACEPlannerStateInfo event = states[1];

		// "sink" AKA percolate down
		int i = 1, child = 0;
		// move last to beginning of heap array
		ACEPlannerStateInfo down_event;
		try {
			down_event = states[i] = states[size--];
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(i + " and " + size + " and length of " + states.length);
			throw (ArrayIndexOutOfBoundsException) e;
		}
		for (; i * 2 <= size; i = child) {
			child = i * 2;
			if (child != size && states[child + 1].distance < states[child].distance) {
				child++;
			}
			if (states[child].distance < down_event.distance) {
				states[i] = states[child];
			} else {
				break;
			}
		}
		states[i] = down_event;

		return event;
	}

	/**
	 * Gets the current minimum-timed event.
	 */
	public ACEPlannerStateInfo get() {
		return states[1];
	}

	/**
	 * Adds event in priority, where the priority is determined by the time point of
	 * the event.
	 * 
	 * @param ce
	 */
	public void addEvent(ACEPlannerStateInfo ce) {
		add(ce);
	}

	public static void main(String[] args) {
		return; // explicit empty test ... maybe implement one if changes to queue are necessary
	}
}
