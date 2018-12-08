package fr.uga.pddl4j.util;

import mt.uol.smi.attempto.aceview.pddl.model.ACEPlannerActionModel;

public class ACEPlannerBitOp extends BitOp{

    /**
     * The serial id of the class.
     */
    private static final long serialVersionUID = 2L;
    
	private ACEPlannerActionModel action;

	public ACEPlannerBitOp(ACEPlannerBitOp bitOp) {
		super(bitOp);
		this.setAction(bitOp.getAction());
	}

	public ACEPlannerBitOp(String name, int arity) {
		super(name,arity);
	}	


	public ACEPlannerActionModel getAction() {
		return action;
	}


	public void setAction(ACEPlannerActionModel action) {
		this.action = action;
	}
	
	
	
}
