/**********************************************************
     Author: Minh B. Do - Arizona State University
***********************************************************/

package edu.asu.sapa.complex_ds;

import java.util.*;

class TemporalOrder {
	public int act;
	public float dur;

	public TemporalOrder(int a, float d) {
		act = a;
		dur = d;
	}
}

/**
 * OCPlan: Datastructure to store the oder contrained (o.c. plan). Specific
 * information stored are: - The set of logical relation: causal link, logical
 * mutex, and resource mutex values. - Set of temporal relation in the Simple
 * Temporal Network (STN) form: temporal relations between the starting time of
 * pair of actions. - Set of earliest starting time allowed by the set of
 * logical/temporal relations.
 */
public class OCPlan {
	int size; // Number of orderings (causal link + log-mutex + res-mutex)
	int lmIndex, rmIndex; // starting index of logical mutex and resource mutex in each array
	int clCount, lmCount, rmCount;
	int[] sa, ea, obj; // Start/end action & object related with the relation
	boolean[] st, et; // Start/end actions's time points connected by the logical relation
	// TRUE: start time point; FALSE: end time point
	float[] sDur; // Temporal duration separated two act's starting times by the logical relation

	ArrayList[] stn; // Simple Temporal Network to store the temporal relations
	// between actions's starting times.

	int planSize;
	// ArrayList actSigs, aDurs;
	float[] est; // Action's earliest starting time according to the logical relations

	static final float EPS = (float) 0.01;
	static final int CL = 1, LM = 2, RM = 3;

	public OCPlan(int numAct, int numCL, int numLM, int numRM) {
		size = numCL + numLM + numRM;
		lmIndex = numCL;
		rmIndex = numCL + numLM;
		clCount = lmCount = rmCount = 0;
		sa = new int[size];
		ea = new int[size];
		obj = new int[size];
		st = new boolean[size];
		et = new boolean[size];
		sDur = new float[size];

		planSize = numAct;
		est = new float[planSize];

		stn = new ArrayList[planSize];
		for (int i = 0; i < planSize; i++)
			stn[i] = new ArrayList();
	}

	public int numCL() {
		return lmIndex;
	}

	public int numLM() {
		return (rmIndex - lmIndex);
	}

	public int numRM() {
		return (size - rmIndex);
	}

	public boolean containsLogOrder(int oType, int sAct, int eAct, int object) {
		int i, j, k;

		switch (oType) {
		case CL:
			i = 0;
			j = lmIndex;
			break;
		case LM:
			i = lmIndex;
			j = rmIndex;
			break;
		default:
			i = rmIndex;
			j = size;
		}

		for (k = i; k < j; k++) {
			if ((sa[k] == sAct) && (ea[k] == eAct) && (obj[k] == object))
				return true;
		}

		return false;
	}

	/**
	 * Add an logical ordering to the o.c. plan
	 */
	public void addLogOrder(int type, int index, int s, int e, boolean tp1, boolean tp2, int o, float dur) {
		int i;

		switch (type) {
		case CL: // causal link
			i = index;
			break;
		case LM: // logical mutex
			i = lmIndex + index;
			break;
		default:
			i = rmIndex + index;
		}

		sa[i] = s;
		ea[i] = e;
		obj[i] = o;
		st[i] = tp1;
		et[i] = tp2;
		sDur[i] = dur;
	}

	public void addLogOrder(int type, int s, int e, boolean tp1, boolean tp2, int o, float dur) {
		int i;

		switch (type) {
		case CL: // causal link
			i = clCount++;
			if (clCount > lmIndex) {
				System.out.println("OCPlan.addLogOrder(): Add more causal link that initialized.");
				return;
			}
			break;
		case LM: // logical mutex
			i = lmIndex + lmCount++;
			if (lmCount > (rmIndex - lmIndex)) {
				System.out.println("OCPlan.addLogOrder(): Add more logical mutex that initialized.");
				return;
			}
			break;
		default:
			i = rmIndex + rmCount++;
			if (rmCount > (size - rmIndex)) {
				System.out.println("OCPlan.addLogOrder(): Add more causal link that initialized.");
				return;
			}
		}

		sa[i] = s;
		ea[i] = e;
		obj[i] = o;
		st[i] = tp1;
		et[i] = tp2;
		sDur[i] = dur;
	}

	/**
	 * Add a temporal ordering
	 */
	public void addTempOrder(int a1, int a2, float sDur) {
		int i, index = -1;
		TemporalOrder to;

		for (i = 0; i < stn[a2].size(); i++) {
			to = (TemporalOrder) stn[a2].get(i);
			if (to.act == a1) {
				if (to.dur < sDur)
					stn[a2].set(i, new TemporalOrder(a1, sDur));

				return;
			}
		}

		stn[a2].add(new TemporalOrder(a1, sDur));
	}

	/**
	 * Specify the earliest starting time for actions in the plan
	 */
	public void setEST(int actIndex, float st) {
		est[actIndex] = st;
	}

	public float getEST(int aIndex) {
		return est[aIndex];
	}

	/**
	 * Compare two o.c. plans according to their *logical ordering* sets
	 */
	public boolean subsummedBy(OCPlan ocPlan) {
		boolean equal = true;
		int i;

		for (i = 0; i < lmIndex; i++) {
			if (!ocPlan.containsLogOrder(CL, sa[i], ea[i], obj[i])) {
				equal = false;
				System.out.println(">>CL: (" + sa[i] + "--" + obj[i] + "-->" + ea[i] + ")");
			}
		}

		for (i = lmIndex; i < rmIndex; i++) {
			if (!ocPlan.containsLogOrder(LM, sa[i], ea[i], obj[i])) {
				if ((sa[i] != 0) && (ea[i] != planSize - 1)) // Disregard mutex involving A_init & A_goal
					equal = false;
				System.out.println(">>LM: (" + sa[i] + "--" + obj[i] + "-->" + ea[i] + ")");
			}
		}

		for (i = rmIndex; i < size; i++) {
			if (!ocPlan.containsLogOrder(RM, sa[i], ea[i], obj[i])) {
				equal = false;
				System.out.println(">>RM: (" + sa[i] + "--" + obj[i] + "-->" + ea[i] + ")");
			}
		}

		return equal;
	}

	/**
	 * Function to check if two OCPlans equals in their STN
	 */
	public boolean equalsSTN(OCPlan ocPlan) {
		return true;
	}

	/**
	 * Function to check if two OCPlans equals in earliest start times
	 */
	public boolean equalsEST(OCPlan ocPlan) {
		boolean equal = true;
		float dif;

		for (int i = 0; i < planSize; i++) {
			dif = ocPlan.getEST(i) - est[i];
			if ((dif > EPS) || (-dif > EPS)) {
				equal = false;
				System.out.println(">> A" + i + ": st1 = " + est[i] + " | st2 = " + ocPlan.getEST(i));
			}
		}

		return equal;
	}

	/**
	 * Function to build the temporal relations between action's starting times
	 * based on the logical orderings between them
	 */
	public void buildTempOrderings() {
		for (int i = 0; i < size; i++) {
			addTempOrder(sa[i], ea[i], sDur[i]);
		}
	}

	/**
	 * Function to sort the temporal orderings in an o.c. plan to find the smallest
	 * starting time for each action in the plan. Note: Not very efficient algorithm
	 * now, just for the purpose of easy to write.
	 */
	public void sort() {
		int i, j, k, numProcessed = 0;
		ArrayList[] tempStn = new ArrayList[planSize], toArray;
		TemporalOrder to;

		boolean[] checked = new boolean[planSize];

		for (i = 0; i < planSize; i++) {
			est[i] = 0;
			tempStn[i] = new ArrayList(stn[i]);
			checked[i] = false;
		}

		while (numProcessed < planSize) {
			for (i = 0; i < planSize; i++) {
				if ((tempStn[i].size() == 0) && (checked[i] == false)) {
					// System.out.print(" P" + i);
					checked[i] = true;
					numProcessed++;

					for (j = 0; j < planSize; j++)
						for (k = 0; k < tempStn[j].size(); k++) {
							to = (TemporalOrder) tempStn[j].get(k);
							if (to.act == i) {
								if (est[j] < est[i] + to.dur)
									est[j] = est[i] + to.dur;

								tempStn[j].remove(k);
								k--;
							}
						}
				}
			}
		}
	}

	/**
	 * Function to return the o.c plan in the form of (i) logical orderings between
	 * actions; (ii) temporal orderings (stn); (iii) earliest starting times
	 */
	public String logOrdersToString() {
		String logOrders = new String();
		int i;

		logOrders += "CausalLinks: ";
		for (i = 0; i < lmIndex; i++) {
			logOrders += "(" + sa[i] + "-" + obj[i] + "->" + ea[i] + ") ";
		}

		logOrders += "\nLogical Mutexes: ";
		for (i = lmIndex; i < rmIndex; i++) {
			logOrders += "(" + sa[i] + "-" + obj[i] + "->" + ea[i] + ") ";
		}

		logOrders += "\nResource Mutexes: ";
		for (i = rmIndex; i < size; i++) {
			logOrders += "(" + sa[i] + "-" + obj[i] + "->" + ea[i] + ") ";
		}

		return logOrders;
	}

	public String stnToString() {
		String stnString = new String();
		TemporalOrder to;
		int i, j;

		for (i = 0; i < planSize; i++) {
			stnString += "A" + i + ": ";
			for (j = 0; j < stn[i].size(); j++) {
				to = (TemporalOrder) stn[i].get(j);
				stnString += "[A" + to.act + "," + to.dur + "] ";
			}
			stnString += "\n";
		}

		return stnString;
	}

	public String estToString() {
		String estString = new String();
		int i;

		for (i = 0; i < planSize; i++) {
			estString += est[i] + ": " + "A" + i + "\n";
		}

		return estString;
	}

	public String pcPlanToString(ArrayList actSigs, ArrayList durs) {
		String signature;
		String s = new String();
		int index, sIndex, oldIndex;

		for (index = 1; index < planSize - 1; index++) {
			signature = (String) actSigs.get(index);
			s += est[index] + ": ";

			sIndex = signature.indexOf('*');
			s += "(" + signature.substring(0, sIndex);
			while (true) {
				oldIndex = sIndex + 1;
				sIndex = signature.indexOf('*', oldIndex);
				if (sIndex < 0)
					break;

				s += " " + signature.substring(oldIndex, sIndex);
			}
			s += ")";
			s += " [" + (Float) durs.get(index) + "]\n";
		}

		return s;
	}

	public float getMakespan() {
		return est[planSize - 1];
	}
}
