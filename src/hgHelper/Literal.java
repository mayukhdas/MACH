package hgHelper;

import java.util.List;

public class Literal {
	
	String predicateName;
	Term[] Arguments;

	/**
	 * @param predicateName
	 * @param arguments
	 */
	
	
	public Literal(String predicateName, String... arguments) {
		super();
		this.predicateName = predicateName;
		this.Arguments = new Term[arguments.length];
		int i =0;
		for(String a:arguments)
		{
			Term t = new Term(true,a);
			this.Arguments[i] = t;
			i++;
		}
	}

	/**
	 * @return the predicateName
	 */
	public String getPredicateName() {
		return predicateName;
	}

	/**
	 * @param predicateName the predicateName to set
	 */
	public void setPredicateName(String predicateName) {
		this.predicateName = predicateName;
	}

	/**
	 * @return the arguments
	 */
	public Term[] getArguments() {
		return Arguments;
	}

	/**
	 * @param arguments the arguments to set
	 */
	public void setArguments(Term[] arguments) {
		Arguments = arguments;
	}

	/**
	 * @param predicateName
	 * @param arguments
	 */
	public Literal(String predicateName, Term[] arguments) {
		super();
		this.predicateName = predicateName;
		Arguments = arguments;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
