/**********************************************************************
   Author: Minh B. Do (Arizona State University - binhminh@asu.edu)
**********************************************************************/
package mt.uol.smi.attempto.aceview.pddl.sapa.complex_ds;

import java.util.*;

import mt.uol.smi.attempto.aceview.pddl.sapa.basic_ds.*;

/**
 * StateManager: StateManager has three elements: initial state, goal state, and
 * a sorted array of intermediate states (StateInfo class) that are reachable
 * from the initial state. The states in the queue are sorted according to their
 * distance values (h+g).
 */
public class StateManager {
	/* For grounded InitState and GoalState */
	GState gInitState;
	GState gGoalState;
	ArrayList initEventQueue; // For exogenous states

	StateQueue stateQueue; // Set of all GStateInfo explored. stateSet(0) = InitState

	// Sorted according to the distance to the goal-state
	int STATE_QUEUE_SIZE = 50000; // Limit on the number of states in queue

	public StateManager(int numFunc) {
		gInitState = new GState(numFunc);
		gGoalState = new GState(numFunc);
		initEventQueue = new ArrayList();

		stateQueue = new StateQueue();
	}

	/** Set the initial predicate manager in the initial state */
	public void setInitPredDB(ArrayList predIDs) {
		gInitState.setPredDB(new GPredDB(predIDs, 0));
	}

	/** Set the initial resource manager in the initial state */
	public void setInitMResDB(GMResDB mres) {
		gInitState.setMResDB(mres);
	}

	/** Set the initial set of exogenous states */
	public void addInitEvent(Event e) {
		initEventQueue.add(e);
	}

	public int numInitEvent() {
		return initEventQueue.size();
	}

	public Event getInitEvent(int index) {
		return (Event) initEventQueue.get(index);
	}

	public ArrayList getInitEvents() {
		return initEventQueue;
	}

	/** Set the goal predicate manager */
	public void setGoalPredDB(GPredDB aPredDB) {
		gGoalState.setPredDB(aPredDB);
	}

	public GState getInitState() {
		return gInitState;
	}

	public GState getGoalState() {
		return gGoalState;
	}

	/** Add a new (explored) state to the search queue */
	public void addStateInfo(StateInfo s) {
		int stpSize = stateQueue.size();
		// Check the limit
		/*
		 * if( stpSize > STATE_QUEUE_SIZE ) { stateQueue.remove(--stpSize); }
		 */

		stateQueue.add(s);
	}

	/** Get the first (shortest distance) StateInfo in the queue to explore */
	public StateInfo getStateInfo() {
		if (stateQueue.size() == 0)
			return null;

		return stateQueue.remove();
	}

	/** Reset the queue to contain only the initial state */
	public void resetStateQueue(StateInfo initState) {
		stateQueue.clear();
		stateQueue.add(initState);
	}

	public void clearStateQueue() {
		stateQueue.clear();
	}
}

/**
 * This is a minimum-has-precedence priority queue for use for choosing the
 * minimum-valued state. Many known priority queue algorithms have a trade-off
 * between the "remove" and "insert" methods (e.g. constant time on one gives N
 * time on the other). There are also known priority queue algorithms that give
 * worst-case of O(log n) on all operations. A heap priority queue provides this
 * quality and was chosen for this implementation.
 * 
 * @author J. Daniel Benton
 */
class StateQueue {
	private int size; // the size of this queue
	private int arraySize;
	private static final int DEFSIZE = 10000;
	private static final int GROW_BY = 1000;
	private StateInfo[] states;

	/**
	 * default constructor.
	 */
	public StateQueue() {
		states = new StateInfo[arraySize = DEFSIZE];
	}

	/**
	 * 
	 * @param size
	 *            Estimated size of the queue.
	 */
	public StateQueue(int size) {
		states = new StateInfo[arraySize = size];
	}

	/**
	 * A copy constructor. Used to get the ordered array. Not intended for other
	 * uses.
	 * 
	 * @param queue
	 */
	private StateQueue(StateQueue queue) {
		this.size = queue.size;
		states = new StateInfo[queue.states.length];
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
	public boolean add(StateInfo si) {
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
	public StateInfo[] toArray() {
		StateQueue queue = new StateQueue(this);
		int size = queue.states.length;
		StateInfo[] array = new StateInfo[size];
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
		StateInfo[] newEvents;
		int[] newOrderArray;
		int arraySize;

		newEvents = new StateInfo[arraySize = size + GROW_BY + 1];

		System.arraycopy(states, 0, newEvents, 0, size + 1);
		states = newEvents;
		this.arraySize = arraySize;
	}

	/**
	 * Unimplemented for states. (Always returns false!)
	 * 
	 * @return True if this queue contains this element, false otherwise.
	 */
	public boolean contains(StateInfo ce) {
		return false;
	}

	/**
	 * Removes a StateInfo from the queue with the smallest time.
	 * 
	 * @return The removed StateInfo object.
	 */
	public StateInfo remove() {
		if (isEmpty())
			return null;

		StateInfo event = states[1];

		// "sink" AKA percolate down
		int i = 1, child = 0;
		// move last to beginning of heap array
		StateInfo down_event;
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
	public StateInfo get() {
		return states[1];
	}

	/**
	 * Adds event in priority, where the priority is determined by the time point of
	 * the event.
	 * 
	 * @param ce
	 */
	public void addEvent(StateInfo ce) {
		add(ce);
	}

	public static void main(String[] args) {
		return; // explicit empty test ... maybe implement one if changes to queue are necessary
	}
}
