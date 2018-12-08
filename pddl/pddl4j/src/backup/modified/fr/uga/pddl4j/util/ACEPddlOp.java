package fr.uga.pddl4j.util;

import fr.uga.pddl4j.parser.Op;
import mt.uol.smi.attempto.aceview.pddl.model.ACEPlannerActionModel;

public class ACEPlannerOp extends Op{

	private ACEPlannerActionModel action;

	public ACEPlannerOp(ACEPlannerOp op) {
		super(op);
		this.setAction(op.getAction());
	}


	public ACEPlannerActionModel getAction() {
		return action;
	}


	public void setAction(ACEPlannerActionModel action) {
		this.action = action;
	}
	
	
}
