package mt.uol.smi.attempto.aceview.planner.model;

public class ACEPlannerParameter extends ACEPlannerObject {

	protected String parameterName;
	protected String parameterValue;

	public ACEPlannerParameter() {
		super();
		this.parameterName = "";
		this.parameterValue = "";
	}

	public ACEPlannerParameter(String parameterName, String parameterValue) {
		super();
		this.parameterName = parameterName;
		this.parameterValue = parameterValue;
	}

	public ACEPlannerParameter(String id, String parameterName, String parameterValue) {
		super(id);
		this.parameterName = parameterName;
		this.parameterValue = parameterValue;
	}

	public ACEPlannerParameter(ACEPlannerParameter a) {
		super(a);
		this.parameterName = a.getParameterName();
		this.parameterValue = a.getParameterValue();
	}

	public String getParameterName() {
		return parameterName;
	}

	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}

	public String getParameterValue() {
		return parameterValue;
	}

	public void setParameterValue(String parameterValue) {
		this.parameterValue = parameterValue;
	}

	@Override
	public String toString() {
		return this.parameterName + " -> " + this.parameterValue;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;

		if (!(obj instanceof ACEPlannerParameter))
			return false;

		ACEPlannerParameter param = (ACEPlannerParameter) obj;
		if (param.getParameterValue() == null)
			return false;

		if (this.getParameterValue() == null)
			return false;

		return this.getParameterValue().equals(param.getParameterValue());
	}

}
