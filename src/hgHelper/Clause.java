package hgHelper;

public class Clause {

	public static Literal[] generateClause(String clause)
	{
		try {
			String[] lits = clause.split("\\^");
			Literal[] cl = new Literal[lits.length];
			int idx = 0;
			for(String l:lits)
			{
				String[] litSplit = l.split("\\(");
				String predName = litSplit[0];
				String[] argList = litSplit[1].replaceAll("\\)", "").split(",");
				Term[] args = new Term[argList.length];
				for(int i=0;i<argList.length;i++)
				{
					String arg = argList[i].trim();
					boolean v = Character.isUpperCase(arg.charAt(0));
					args[i] = new Term(v,arg);
				}
				Literal x = new Literal(predName,args);
				cl[idx] = x;
				idx++;
			}
			return cl;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
}
