package hyper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Hashtable;
import java.util.List;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.atom.HGRel;
import org.hypergraphdb.atom.HGRelType;
import org.hypergraphdb.type.HGAtomType;

public class HyperGDB {
	
	public static String dblocation = "./graphs/graph1";
	static boolean verbose = true;
	String schemaloc;
	String factloc;
	String dbName;
	HyperGraph graph;
	Hashtable<String,HGHandle> relTypeHandles;
	Hashtable<String,HGHandle> entityTypeHandles;
	
	/*
	 * Summary Tables
	 */
	Hashtable<String,Double> typeCounts;
	Hashtable<String,Hashtable<String,Double>> outCounts;
	Hashtable<String,Hashtable<String,Double>> InCounts;
	Hashtable<String,Hashtable<String,Double>> typeInAvg;
	Hashtable<String,Hashtable<String,Double>> typeOutAvg;
	Hashtable<String,Double> predCounts;
	Hashtable<String,Double> corrMatrix;
	
	/**
	 * No Argument constructor
	 */
	public HyperGDB() {
		this.schemaloc = null;
		this.factloc = null;
		this.dbName = "graph1";
		this.relTypeHandles = new Hashtable<String,HGHandle>();
		this.entityTypeHandles = new Hashtable<String,HGHandle>();
		this.graph = new HyperGraph(dblocation);
		
		this.typeCounts = new Hashtable<String,Double>();
	}
	/**
	 * @param schemaloc
	 * @param factloc
	 */
	public HyperGDB(String schemaloc, String factloc) {
		super();
		this.schemaloc = schemaloc;
		this.factloc = factloc;
		this.dbName = "graph1";
		this.relTypeHandles = new Hashtable<String,HGHandle>();
		this.entityTypeHandles = new Hashtable<String,HGHandle>();
		this.graph = new HyperGraph(dblocation);
		
		this.typeCounts = new Hashtable<String,Double>();
	}
	public HyperGDB(String schemaloc, String factloc, String dbName) {
		this.schemaloc = schemaloc;
		this.factloc = factloc;
		this.dbName = dbName;
		dblocation = this.genDbLoc();
		this.relTypeHandles = new Hashtable<String,HGHandle>();
		this.entityTypeHandles = new Hashtable<String,HGHandle>();
		this.graph = new HyperGraph(dblocation);
		
		this.typeCounts = new Hashtable<String,Double>();
	}
		
	/**
	 * @return the schemaloc
	 */
	public String getSchemaloc() {
		return schemaloc;
	}

	/**
	 * @param schemaloc the schemaloc to set
	 */
	public void setSchemaloc(String schemaloc) {
		this.schemaloc = schemaloc;
	}

	/**
	 * @return the factloc
	 */
	public String getFactloc() {
		return factloc;
	}

	/**
	 * @param factloc the factloc to set
	 */
	public void setFactloc(String factloc) {
		this.factloc = factloc;
	}

	/**
	 * @return the dbName
	 */
	public String getDbName() {
		return dbName;
	}

	/**
	 * @param dbName the dbName to set
	 */
	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	/**
	 * @return the relTypeHandles
	 */
	public Hashtable<String, HGHandle> getRelTypeHandles() {
		return relTypeHandles;
	}

	/**
	 * @param relTypeHandles the relTypeHandles to set
	 */
	public void setRelTypeHandles(Hashtable<String, HGHandle> relTypeHandles) {
		this.relTypeHandles = relTypeHandles;
	}

	/**
	 * @return the entityTypeHandles
	 */
	public Hashtable<String, HGHandle> getEntityTypeHandles() {
		return entityTypeHandles;
	}

	/**
	 * @param entityTypeHandles the entityTypeHandles to set
	 */
	public void setEntityTypeHandles(Hashtable<String, HGHandle> entityTypeHandles) {
		this.entityTypeHandles = entityTypeHandles;
	}
	
	/**
	 * Generate dblocation if given
	 * @return String location
	 */
	protected String genDbLoc()
	{
		String path = "";
		path = dblocation.substring(0, dblocation.lastIndexOf('/'));
		path = path + this.dbName;
		Utils.println(path);
		return path;
	}
	

	public boolean loadSchema()
	{
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(new File(this.schemaloc)));
			String line;
			int idx = 0;
			while((line = br.readLine())!=null)
			{
				if(!line.startsWith("//") && !line.isEmpty())
				{
					if(line.lastIndexOf('.')!=-1)
						line = line.substring(0, line.lastIndexOf('.'));
					//Utils.println(line);
					if(line.startsWith("import:"))
					{
						
						String np = line.split(":")[1];
						Utils.println(np);
						np = np.replace('"', ' ').trim();
						if(np.charAt(0)=='.' && np.charAt(1)=='.')
						{
							this.schemaloc = this.schemaloc.substring(0, this.schemaloc.lastIndexOf('/')-1);
							this.schemaloc = this.schemaloc.substring(0, this.schemaloc.lastIndexOf('/'));
							np = np.substring(2);
							this.schemaloc = this.schemaloc + np;
						}
						else if((np.charAt(0)=='.' && np.charAt(1)=='/'))
						{
							this.schemaloc = this.schemaloc.substring(0, this.schemaloc.lastIndexOf('/'));
							np = np.substring(2);
							this.schemaloc = this.schemaloc + np;
						}
						else if(np.charAt(0)=='/')
						{
							this.schemaloc = np;
						}
						else
						{
							this.schemaloc = this.schemaloc.substring(0, this.schemaloc.lastIndexOf('/'));
							this.schemaloc = this.schemaloc + np;
						}
						if(!this.loadSchema())
						{	br.close();
							return false;
						}
						idx++;
						break;
					}
					else if(line.startsWith("mode:"))
					{
						Utils.println(line);
						String pred = line.split(":")[1].trim();
						String[] predArr = pred.split("\\(");
						String predName = predArr[0];
						String[] args = predArr[1].replaceAll("\\)", "").split(",");
						HGHandle[] h = new HGHandle[args.length];
						for(int i =0;i<args.length;i++)
						{
							String a = args[i];
							a = a.replaceAll("\\+", "").replaceAll("-", "").replaceAll("#", "").trim();
							HGHandle hTemp = this.entityTypeHandles.get(a.intern());
							if(hTemp==null)
							{
								HGRelType objectType = new HGRelType(a.intern());
								hTemp = this.graph.add(objectType);
							}
							h[i] = hTemp;
							this.entityTypeHandles.put(a, hTemp);
						}
						HGRelType relType= new HGRelType(predName.intern(),h);
						HGHandle relTypeHandle = this.relTypeHandles.get(predName.intern());
						if(relTypeHandle==null)
						{
							relTypeHandle = this.graph.add(relType);
							this.relTypeHandles.put(predName.intern(), relTypeHandle);
						}
						idx++;
					}
				}
			}
			br.close();
			if(idx<=0)
				return false;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean loadEvidence()
	{
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(this.factloc)));
			String line;
			int idx=0;
			while((line=br.readLine())!=null)
			{
				if(!line.startsWith("//") && !line.isEmpty())
				{
					line=line.substring(0, line.length()-1);
					//Utils.println(line);
					String[] predArr = line.split("\\(");
					String predName = predArr[0];
					HGHandle relTypeHandle = this.relTypeHandles.get(predName);
					if(relTypeHandle==null)
						return false;
					HGRelType relType = this.graph.get(relTypeHandle);
					String[] args = predArr[1].replaceAll("\\)", "").split(",");
					HGHandle[] argHandles = new HGHandle[args.length];
					for(int i=0;i<args.length;i++)
					{
						String a = args[i].trim().replace('"',' ').trim();
						HGHandle entityTypeHandle = relType.getTargetAt(i);
						if(entityTypeHandle==null)
							return false;
						HGHandle objectHandle = this.graph.getHandle(a.intern());
						//Utils.println("ent Type: "+entityTypeHandle);
						if(objectHandle==null)
							objectHandle = this.graph.add(a.intern(), entityTypeHandle);
						argHandles[i] = objectHandle;
					}
					HGRel newRel = new HGRel(predName.intern(),argHandles);
					this.graph.add(newRel,relTypeHandle);
					idx++;
				}
			}
			br.close();
			if(idx<=0)
				return false;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}
	public void close()
	{
		this.graph.close();
	}
	
	public void summarize()
	{
		for(String k: this.entityTypeHandles.keySet())
		{
			long c = hg.count(this.graph, hg.type(this.entityTypeHandles.get(k)));
			this.typeCounts.put(k, 0.0+c);
		}
		Utils.println(this.typeCounts);
	}
	
	public void test()
	{
		/*String testp = "Mayukh,Tuborg,Nicks)";
		testp = testp.replaceAll("\\)", "");
		Utils.println(testp);
		String[] t = testp.split(",");
		for(String x:t)
		{
			String n = x;
			Utils.println(n);
			HGHandle h = this.graph.add(n.intern());
		}*/
		String q = "Person";
		Utils.println(q);
		List x =hg.getAll(this.graph, hg.all());
		Utils.println(x.size());
	}


	public static void main(String[] args) {
		// TODO Auto-generated method stub
		HyperGDB hgdb = new HyperGDB("./data/uw-cse_rdn/train0_advisedby/train0_advisedby_bk.txt",
				"./data/uw-cse_rdn/train0_advisedby/train0_advisedby_facts.txt");
		if(hgdb.loadSchema())
			if(hgdb.loadEvidence())
			{
				Utils.println("Data loaded!!");
			}
			else
				Utils.println("Data not loaded properly!!");
		hgdb.test();
		hgdb.close();
		//hgdb.summarize();
		
	}
	
	

}
