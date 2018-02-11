package Helper;

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
