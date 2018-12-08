/* ResourceFormula.java
 * Created on May 21, 2004
 */
package edu.asu.sapa.rmtpg;

import edu.asu.sapa.basic_ds.*;

/**
 * Contains information about a resource including:<br/>
 * <ul>
 * <li>Its possible values
 * <li>Its identification number (for referencing it in the GMResDB)
 * <li>A reference to each action that modifies it as an effect (GAction)
 * <li>A reference to each action that requires it as an "at start" precondition
 * (GAction)
 * </ul>
 * Also contains ...
 * 
 * @author J. Benton
 */
public class Resource {
	double possibleValues[];
	int resourceID;
	GAction[] required;
	GAction[] modifies;

	public Resource(int resourceID, GMathForm[] gMathForms, GAction[] required, GAction[] modifies,
			double initialResourceLevel) {
		// initialize given fields.
		this.resourceID = resourceID;
		this.required = required;
		this.modifies = modifies;

		// from grounded math formulas, decide resource levels
		int gMathFormsSize = gMathForms.length;

		// sanity check
		if (gMathFormsSize != modifies.length) {
			System.out.println("Number of modifying resource formulas does not equal the number of given "
					+ "resource modifying actions -- failed grounding of resource levels");
		}

		CalculateResourceLevels(gMathFormsSize, required, initialResourceLevel);
	}

	private double[] CalculateResourceLevels(int numMath, GAction[] gActions, double initValue) {
		// initialize our set with an initial value of 0.
		// this initial value will be the value given to us by the current state
		ResourceValueQueueSet queue = new ResourceValueQueueSet(initValue);

		// discover possible resource values by going through each formula
		// for each resource value
		ResourceValueQueueSetNode node;
		GMySet ms;
		int fID;
		float value;

		float dur;

		while ((node = queue.getNextActiveNode()) != null) {
			// using the given value, calculate the resource values
			// for each given formula
			for (int i = 0; i < numMath; i++) {
				GAction action = gActions[i];
				// this will take a lot of work -- need to have other "resources"
				// as required in here... need to think about it and do
				// some design work for the algorithm to do this properly
				// and need to think about how to not "n x m" the results when
				// more than one resource is involved.
				if (action.getDType()) {
					dur = action.getDStatic();
				} else {
					// dur = action.getDDynamic().value(this, 0);

				}
				// gMathForms[i].;
			}
		}

		// TODO get rid of the line below (when you're done writing this method)
		return new double[1];
	}

	/**
	 * @return Returns the resourceID.
	 */
	public int getResourceID() {
		return resourceID;
	}
}

/**
 * A queue used for calculating the resource values.
 * 
 * @author J. Benton
 */
class ResourceValueQueueSet {
	ResourceValueQueueSetNode head;
	ResourceValueQueueSetNode currentNode;

	/**
	 * Resource value queue set constructor. Takes an initial value (is required --
	 * to perform the algorithm, all resources must have an initial value).
	 * 
	 * @param initValue
	 *            Initial value for the resource.
	 */
	public ResourceValueQueueSet(double initValue) {
		addResource(initValue);
	}

	/**
	 * Adds a resource to the queue if it doesn't already exist.
	 * 
	 * @param resource
	 *            A resource value to be added.
	 * @return True if the resource was added successfully (i.e. it is not already
	 *         in the queue set), false if the resource was not added successfully
	 *         (i.e. it is already in the queue set).
	 */
	public boolean addResource(double resource) {
		if (head == null) { // if this is a new queue
			currentNode = head = new ResourceValueQueueSetNode(resource);
		} else { // otherwise, add the resource at the tail.
			ResourceValueQueueSetNode node = head;

			while ((node = node.next) != null) {
				// the quality below makes this a "set" :)
				if (node.value == resource)
					return false;
			}

			// here we must have node == last node in the queue...
			// the last node in the queue now has a new "next"
			node.next = new ResourceValueQueueSetNode(resource);
		}

		return true;
	}

	/**
	 * Gives the next active node or null if there are no more active nodes. Sets
	 * returned node to inactive. (Looking at this node to calculate its resource
	 * values changes its state.)
	 * 
	 * @return The next active node.
	 */
	public ResourceValueQueueSetNode getNextActiveNode() {

		/*
		 * // may want to use the code below later -- if we find that // quite do what
		 * we want it to do (in terms of behavior).
		 * 
		 * ResourceValueQueueSetNode node = head;
		 * 
		 * // get the next inactive node, or null if there are no more nodes while
		 * ((node = node.next) != null && !node.active) {}
		 */

		// get the current node
		ResourceValueQueueSetNode node = currentNode;
		currentNode.active = false;
		currentNode = node.next;

		return node;
	}
}

/**
 * Represents a node in the queue set.
 * 
 * @author J. Benton
 */
class ResourceValueQueueSetNode {
	// member variables are public for performance
	// they are not intended to be changed outside of the queue,
	// so be gentle.

	/**
	 * This is public for performance reasons. It is not intended to be changed
	 * outside of the queue class.<br/>
	 * Next node in the queue.
	 */
	public ResourceValueQueueSetNode next;
	/**
	 * This is public for performance reasons. It is not intended to be changed
	 * outside of the queue class.<br/>
	 * Resource value that this node represents.
	 */
	public double value;
	/**
	 * This is public for performance reasons. It is not intended to be changed
	 * outside of the queue class.<br/>
	 * True if this node has not been taken from the queue yet, false otherwise.
	 */
	public boolean active;

	public ResourceValueQueueSetNode(double value) {
		active = true;
		this.value = value;
	}
}