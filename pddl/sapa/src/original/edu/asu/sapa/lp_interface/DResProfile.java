/*****************************************************************
       Author: Minh B. Do - Arizona State University
******************************************************************/
package edu.asu.sapa.lp_interface;

import java.util.*;

/**
 * DResProfile.java: Store the resource profile for actions in the parallel p.c
 * plan returned by Sapa. NOTE: This class can help put extra information in the
 * GUI
 */

public class DResProfile {
	ArrayList[] dResList; // Set of (dynamic - lhs) resID appear in the rhs of precond/effect (Test/Set)
	ArrayList[] dResValue; // Values of those (rhs) resIDs at the action's start execution time points

	public DResProfile(int planSize) {
		dResList = new ArrayList[planSize];
		dResValue = new ArrayList[planSize];
		for (int i = 0; i < planSize; i++) {
			dResList[i] = new ArrayList();
			dResValue[i] = new ArrayList();
		}
	}

	public void addResValue(int actIndex, Integer resID, float value) {
		if (!dResList[actIndex].contains(resID)) {
			dResList[actIndex].add(resID);
			dResValue[actIndex].add(new Float(value));
		}
	}

	public int numResValue(int actIndex) {
		return dResList[actIndex].size();
	}

	public Integer getResID(int actIndex, int resIndex) {
		return (Integer) dResList[actIndex].get(resIndex);
	}

	public float getResValue(int actIndex, int resIndex) {
		return ((Float) dResValue[actIndex].get(resIndex)).floatValue();
	}

	public float getResValue2(int actIndex, int resID) {
		for (int i = 0; i < dResList[actIndex].size(); i++)
			if (((Integer) dResList[actIndex].get(i)).intValue() == resID)
				return ((Float) dResValue[actIndex].get(i)).floatValue();

		// Check for error
		System.out.println("DResProfile.getResValue2: resID=" + resID + " not on the list (actIndex:" + actIndex
				+ ", dResList[actIndex].size()=" + dResList[actIndex].size() + ")");
		System.exit(1);

		return 0;
	}

	// Function to print out the resource information stored in this DResProfile
	// instance
	public void printResInformation(int planSize) {
		ArrayList resID;
		ArrayList resValue;

		System.out.println("DResProfile (rhs):");
		for (int i = 0; i < planSize; i++) {
			resID = dResList[i];
			resValue = dResValue[i];

			System.out.print("\nA" + i + ". ");
			for (int j = 0; j < resID.size(); j++) {
				System.out.print("(ID:" + (Integer) resID.get(j) + ",V:" + (Float) resValue.get(j) + ") ");
			}
		}
	}
}
