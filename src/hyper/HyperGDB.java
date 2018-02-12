package hyper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.HGSearchResult;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.atom.HGRel;
import org.hypergraphdb.atom.HGRelType;
import org.hypergraphdb.query.HGQueryCondition;
import org.hypergraphdb.type.HGAtomType;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;

import Helper.Literal;
import Helper.Term;

public class HyperGDB {
	
	public static final double TOL = 0.005;
	public static final int MaxIter = 10;
	
	public static String dblocation = "./graphs/graph1";
	static boolean verbose = true;
	String schemaloc;
	String factloc;
	String dbName;
	HyperGraph graph;
	HyperGraph qryGraph;
	Hashtable<String,HGHandle> relTypeHandles;
	Hashtable<String,HGHandle> entityTypeHandles;
	
	Hashtable<String,HGHandle> QryRelTypeHandles;
	Hashtable<String,HGHandle> QryEntityTypeHandles;
	
	/*
	 * Summary Tables
	 */
	Hashtable<String,Double> typeCounts;
	Hashtable<String,Hashtable<String,Double>> outCounts;
	Hashtable<String,Hashtable<String,Double>> InCounts;
	Hashtable<String,Double> typeInAvg;
	Hashtable<String,Double> typeOutAvg;
	Hashtable<String,Double> predCounts;
	//Hashtable<String,Hashtable<String,Double>> corrMatrix;
	Hashtable<String,ArrayList<String>> typeArgs;
	Table<String, String, Double> corrMatrix; // = HashBasedTable.create();
	Table<String, String, Double> leftCorr;
	Table<String, String, Double> rightCorr;
	
	Hashtable<String,Double> CountTable;
	Hashtable<Term,String> qryVars;
	Hashtable<String,HGHandle> QryObjectHandles;
	
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
		
		this.initialize();
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
		
		this.initialize();
	}
	public HyperGDB(String schemaloc, String factloc, String dbName) {
		this.schemaloc = schemaloc;
		this.factloc = factloc;
		this.dbName = dbName;
		dblocation = this.genDbLoc();
		this.relTypeHandles = new Hashtable<String,HGHandle>();
		this.entityTypeHandles = new Hashtable<String,HGHandle>();
		this.graph = new HyperGraph(dblocation);
		
		this.initialize();
	}
	private void initialize()
	{
		this.typeCounts = new Hashtable<String,Double>();
		this.InCounts = new Hashtable<String,Hashtable<String,Double>>();
		this.outCounts = new Hashtable<String,Hashtable<String,Double>>();
		this.typeInAvg = new Hashtable<String,Double>();
		this.typeOutAvg = new Hashtable<String,Double>();
		this.typeArgs = new Hashtable<String,ArrayList<String>>();
		this.corrMatrix =  HashBasedTable.create();
		this.leftCorr = HashBasedTable.create();
		this.rightCorr = HashBasedTable.create();
	}
	
	private void initializeQuery()
	{
		this.qryGraph = new HyperGraph();
		this.QryEntityTypeHandles = new Hashtable<String,HGHandle>();
		this.QryRelTypeHandles = new Hashtable<String,HGHandle>();
	}
	private void closeQuery()
	{
		this.QryEntityTypeHandles.clear();
		this.QryRelTypeHandles.clear();
		this.qryGraph.close();
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
						ArrayList<String> argList = new ArrayList<String>();
						for(int i =0;i<args.length;i++)
						{
							String a = args[i];
							a = a.replaceAll("\\+", "").replaceAll("-", "").replaceAll("#", "").trim();
							argList.add(a.intern());
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
						this.typeArgs.put(predName.intern(), argList);
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
	
	
	public boolean loadSchemaForQry()
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
						ArrayList<String> argList = new ArrayList<String>();
						for(int i =0;i<args.length;i++)
						{
							String a = args[i];
							a = a.replaceAll("\\+", "").replaceAll("-", "").replaceAll("#", "").trim();
							argList.add(a.intern());
							HGHandle hTemp = this.QryEntityTypeHandles.get(a.intern());
							if(hTemp==null)
							{
								HGRelType objectType = new HGRelType(a.intern());
								hTemp = this.qryGraph.add(objectType);
							}
							h[i] = hTemp;
							this.QryEntityTypeHandles.put(a, hTemp);
						}
						HGRelType relType= new HGRelType(predName.intern(),h);
						//this.typeArgs.put(predName.intern(), argList);
						HGHandle relTypeHandle = this.QryRelTypeHandles.get(predName.intern());
						if(relTypeHandle==null)
						{
							relTypeHandle = this.qryGraph.add(relType);
							this.QryRelTypeHandles.put(predName.intern(), relTypeHandle);
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
	
	
	/*
	 * Build Graph Summaries: Should be called from constructors.
	 */
	public void summarize()
	{
		for(String k: this.entityTypeHandles.keySet())
		{
			long c = hg.count(this.graph, hg.type(this.entityTypeHandles.get(k)));
			this.typeCounts.put(k, 0.0+c);
		}
		Utils.println(this.typeCounts);
		
		//Degree summaries
		for(String k: this.entityTypeHandles.keySet())
		{
			HGSearchResult<HGHandle> rs = this.graph.find(hg.type(this.entityTypeHandles.get(k)));
			try {
				while(rs.hasNext())
				{
					Utils.println("Here");
					HGHandle h = rs.next();
					Hashtable<String,Double> temp = this.InCounts.get(this.graph.get(h));
					Hashtable<String,Double> tempo = this.outCounts.get(this.graph.get(h));
					if(temp ==null)
						temp = new Hashtable<String,Double>();
					if(tempo == null)
						tempo = new Hashtable<String,Double>();
					for(String relt:this.relTypeHandles.keySet())
					{
						HGHandle relTH = this.relTypeHandles.get(relt);
						if(((HGRelType)this.graph.get(relTH)).getArity()==1)
						{
							long c = hg.count(this.graph, hg.and(hg.type(relTH),hg.incidentAt(h, 0)));
							//Utils.println(this.graph.get(h)+"--"+((HGRelType)this.graph.get(relTH)).getName()+"--"+c);
							temp.put(relt, 0.0+c);
							tempo.put(relt, 0.0+c);
						}
						if(((HGRelType)this.graph.get(relTH)).getArity()>=2)
						{
							long c = hg.count(this.graph, hg.and(hg.type(relTH), hg.incidentNotAt(h, 0)));
							long co = hg.count(this.graph, hg.and(hg.type(relTH), hg.incidentAt(h, 0)));
							//Utils.println(this.graph.get(h)+"--"+((HGRelType)this.graph.get(relTH)).getName()+"--"+c);
							temp.put(relt, 0.0+c);
							tempo.put(relt, 0.0+co);
						}
						
					}
					this.InCounts.put(this.graph.get(h), temp);
					this.outCounts.put(this.graph.get(h), tempo);
				}
			}
 			catch(Exception e)
			{
				e.printStackTrace();
			}
			rs.close();
			//this.typeCounts.put(k, 0.0+c);
		}
		//Utils.println(this.typeCounts);
		//Utils.println(this.InCounts);
		//Utils.println(this.outCounts);
		
		//Averages
		for(String k:this.relTypeHandles.keySet())
		{
			HGHandle relTypeHandle = this.relTypeHandles.get(k.intern());
			HGRelType ty = this.graph.get(relTypeHandle);
			int sourceCount = 0;
			int destCount = 0;
			
			for(int i = 0;i<ty.getArity();i++)
			{
				HGHandle atomType = ty.getTargetAt(i);
				HGSearchResult<HGHandle> rs = this.graph.find(hg.type(atomType));
				while(rs.hasNext())
				{
					HGHandle atomH = rs.next();
					if(i==0)
					{
						long c = hg.count(this.graph,hg.and(hg.type(relTypeHandle),hg.incidentAt(atomH, 0)));
						//this.typeInAvg.put(ty.getName(), this.typeInAvg.get(ty.getName())+c);
						sourceCount++;
						if(this.typeOutAvg.get(ty.getName())==null)
							this.typeOutAvg.put(ty.getName(), 0.0+c);
						else
							this.typeOutAvg.put(ty.getName(),this.typeOutAvg.get(ty.getName())+c);
						if(ty.getArity()==1)
						{
							if(this.typeInAvg.get(ty.getName())==null)
								this.typeInAvg.put(ty.getName(), 0.0+c);
							else
								this.typeInAvg.put(ty.getName(),this.typeInAvg.get(ty.getName())+c);
							destCount++;
						}
					}
					else if(i>0)
					{
						long c = hg.count(this.graph,hg.and(hg.type(relTypeHandle),hg.incidentNotAt(atomH, 0)));
						if(this.typeInAvg.get(ty.getName())==null)
							this.typeInAvg.put(ty.getName(), 0.0+c);
						else
							this.typeInAvg.put(ty.getName(),this.typeInAvg.get(ty.getName())+c);
						destCount++;
					}
					
				}
				rs.close();
			}
			//for(String t:this.typeInAvg.keySet())
			if(this.typeInAvg.get(k)!=null)
				this.typeInAvg.put(k, this.typeInAvg.get(k)/(double)destCount);
			//for(String t:this.typeOutAvg.keySet())
			if(this.typeOutAvg.get(k)!=null)
				this.typeOutAvg.put(k, this.typeOutAvg.get(k)/(double)sourceCount);
		}
		Utils.println(this.typeInAvg);
		Utils.println(this.typeOutAvg);
		
		//Correlation
		
		Multimap<String,String> m = ArrayListMultimap.create();
		for(String k:this.relTypeHandles.keySet())
		{
			//HGHandle kRelTypeHandle = this.relTypeHandles.get(k);
			ArrayList<String> argListk = this.typeArgs.get(k);
			for(String l:this.relTypeHandles.keySet())
			{
				//HGHandle lRelTypeHandle = this.relTypeHandles.get(l);
				ArrayList<String> argListl = this.typeArgs.get(l);
				if(k.intern().equals(l.intern()))
					continue;
				else
				{
					for(int i=0;i<argListk.size();i++)
					{
						for(int j=0;j<argListl.size();j++)
						{
							if(argListk.get(i).intern().equals(argListl.get(j).intern()))
							{
								String str = k+","+i+";"+l+","+j;
								m.put(argListk.get(i).intern(), str);
							}
						}
					}
				}
			}
		}

		for(String ot:this.entityTypeHandles.keySet())
		{
			HGHandle entTypeHandle = this.entityTypeHandles.get(ot);
			List<HGHandle> rs = this.graph.findAll(hg.type(entTypeHandle));
			Collection<String> allShapes = m.get(ot);	
			Double sum = 0.0;
			List<HGHandle> entsOfThisType = this.graph.findAll(hg.type(entTypeHandle));
			for(String shape:allShapes)
			{
				String[] pair = shape.split(";");
				String k = pair[0];
				String l = pair[1];
				String kRel = k.split(",")[0];
				int kPos = Integer.parseInt(k.split(",")[1]);
				String lRel = l.split(",")[0];
				int lPos = Integer.parseInt(l.split(",")[1]);
				
				
				Utils.println(shape);
				HGHandle relTypeHandle1 = this.relTypeHandles.get(kRel.intern());
				HGHandle relTypeHandle2 = this.relTypeHandles.get(lRel.intern());
				for(HGHandle ent:entsOfThisType)
				{
					long c1 = hg.count(this.graph, hg.and(hg.type(relTypeHandle1), hg.incidentAt(ent, kPos)));
					long c2 = hg.count(this.graph, hg.and(hg.type(relTypeHandle2), hg.incidentAt(ent, lPos)));
					if(c1>0 && c2>0)
					{
						Double currentVal = this.corrMatrix.contains(kRel, lRel)? this.corrMatrix.get(kRel, lRel):0.0;
						currentVal++;
						this.corrMatrix.put(kRel, lRel, currentVal);
						Double leftCurrVal = this.leftCorr.contains(kRel, lRel)? this.leftCorr.get(kRel, lRel):0.0;
						leftCurrVal++;
						this.leftCorr.put(kRel, lRel, leftCurrVal);
						Double rightCurrVal =this.rightCorr.contains(kRel, lRel)? this.rightCorr.get(kRel, lRel):0.0;
						rightCurrVal++;
						this.rightCorr.put(kRel, lRel, rightCurrVal);
					}
					else if(c1>0 && c2==0)
					{
						Double leftCurrVal = this.leftCorr.contains(kRel, lRel)? this.leftCorr.get(kRel, lRel):0.0;
						leftCurrVal++;
						this.leftCorr.put(kRel, lRel, leftCurrVal);
					}
					else if(c1==0 && c2>0)
					{
						Double rightCurrVal =0.0;
						rightCurrVal = this.rightCorr.contains(kRel, lRel)? this.rightCorr.get(kRel, lRel):0.0;
						rightCurrVal++;
						this.rightCorr.put(kRel, lRel, rightCurrVal);
					}
				}
			}
		}
		
	}
	
	
	/**
	 * This is the function that should be called using the custom object of this class. 
	 * This will generate the approximate count of true grounding for a given clause. Bitrep bit-string representing the sense of each literal 
	 * in the clause. In current implementation bitset is a string of all 1's of length equal to the #literals. We are restricted to counting over the 
	 * true groundings of the body of HORN clause (A conjunction of positive literals). 
	 * 
	 * @param Clause : Array of Literals
	 * @param bitrep : String of length = #Literals
	 * @return Double : Count value for clause.
	 */
	public Double ApproxCount(Literal[] Clause, String bitrep)
	{
		try {
			//------------- Build query graph -------------------------------------
			this.initializeQuery();
			for(Literal lit:Clause)
			{
				String pred = lit.getPredicateName();
				ArrayList<String> argTypes = this.typeArgs.get(pred.intern());
				Term[] qryArgs = lit.getArguments();
				HGHandle qryPredType = this.QryRelTypeHandles.get(pred);
				HGHandle[] args = new HGHandle[qryArgs.length];
				for(int i=0;i<qryArgs.length;i++)
				{
					HGHandle argType = this.QryEntityTypeHandles.get(argTypes.get(i));
					HGHandle varHandle = this.qryGraph.getHandle(qryArgs[i].getValue().intern());
					if(varHandle == null)
					{
						varHandle = this.qryGraph.add(qryArgs[i].getValue(), argType);
					}
					args[i] = varHandle;
					this.QryObjectHandles.put(qryArgs[i].getValue().intern(), varHandle);
					this.qryVars.put(qryArgs[i], argTypes.get(i));
					Double typecount = this.typeCounts.get(argTypes.get(i).intern());
					if(!qryArgs[i].isVar())
						if(this.graph.getHandle(qryArgs[i].getValue().intern())!=null)
							this.CountTable.put(qryArgs[i].getValue().intern(), 1.0);
						else
							this.CountTable.put(qryArgs[i].getValue().intern(), 0.0);
					else
						this.CountTable.put(qryArgs[i].getValue().intern(), typecount);
				}

				HGRel qRel = new HGRel(pred.intern(),args);
				HGHandle qryRelHandle = this.qryGraph.add(qRel, qryPredType);
			}
			//---------------------------------------------------------------------
			
			//-------------- iterate n times over message passing ----------------
			double ub=1.0, lb = 0.0;
			int iter =0;
			while(iter<=MaxIter && Math.abs(ub-lb)<=TOL)
			{
				
			}
			
			this.closeQuery();
		}
		catch(Exception e)
		{
			Utils.println("Problem in query!");
			e.printStackTrace();
			this.shutdown();
			System.exit(1);
		}
		return 0.0;
	}
	
	
	//the recursive method call for message passing
	public Double induceCount(Term currentTerm)
	{
		
		return 0.0;
	}
	private void shutdown()
	{
		
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
		String q = "projectmember";
		HGHandle r = this.relTypeHandles.get(q);
		String atom = "person319";
		HGHandle atomH = this.graph.getHandle(atom.intern());
		Utils.println(q);
		long x =hg.count(this.graph, hg.and(hg.type(r),hg.incidentAt(atomH,0)));
		Utils.println(x);
	}


	public static void main(String[] args) {
		// TODO Auto-generated method stub
		HyperGDB hgdb = new HyperGDB("./data/uw-cse_rdn/train1_advisedby/train1_advisedby_bk.txt",
				"./data/uw-cse_rdn/train1_advisedby/train1_advisedby_facts.txt");
		if(hgdb.loadSchema())
			if(hgdb.loadEvidence())
			{
				Utils.println("Data loaded!!");
			}
			else
				Utils.println("Data not loaded properly!!");
		//hgdb.test();
		//hgdb.close();
		//Utils.println(hgdb.entityTypeHandles);
		hgdb.summarize();
		Utils.println(hgdb.corrMatrix);
		hgdb.close();
		
	}
	
	

}
