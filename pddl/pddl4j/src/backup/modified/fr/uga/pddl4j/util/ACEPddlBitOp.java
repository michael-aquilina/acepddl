package fr.uga.pddl4j.util;

import mt.uol.smi.attempto.aceview.pddl.model.ACEPlannerActionModel;

public class BitOp extends BitOp{

	private ACEPlannerActionModel action;

	public BitOp(BitOp BitOp) {
		super(BitOp);
		this.setAction(BitOp.getAction());
	}


	public ACEPlannerActionModel getAction() {
		return action;
	}


	public void setAction(ACEPlannerActionModel action) {
		this.action = action;
	}
	
	
}
