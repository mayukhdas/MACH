package hgHelper;

public class Term {

	boolean varIndicator;
	String value;
	/**
	 * @param varIndicator
	 * @param value
	 */
	public Term(boolean varIndicator, String value) {
		super();
		this.varIndicator = varIndicator;
		this.value = value;
	}
	/**
	 * @return the varIndicator
	 */
	public boolean isVar() {
		return varIndicator;
	}
	/**
	 * @param varIndicator the varIndicator to set
	 */
	public void setVarIndicator(boolean varIndicator) {
		this.varIndicator = varIndicator;
	}
	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

}
