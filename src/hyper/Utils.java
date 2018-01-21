/**
 * 
 */
package hyper;

/**
 * @author Mayukh
 *
 */
public class Utils {

	public static void print(Object o)
	{
		if(HyperGDB.verbose)
			System.out.print(o);
	}
	public static void println(Object o)
	{
		if(HyperGDB.verbose)
			System.out.println(o);
	}
}
