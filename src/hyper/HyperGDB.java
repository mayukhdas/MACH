package hyper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

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
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
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
	Hashtable<String,HGHandle> relTypeHandles;
	Hashtable<String,HGHandle> entityTypeHandles;
	
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
	Hashtable<String,String> qryVars;
	Hashtable<String, Term> varTerms;
	Hashtable<String,SetMultimap<String,ArrayList<String>>> qryIn;
	Hashtable<String,SetMultimap<String,ArrayList<String>>> qryOut;
	
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
		this.summarize();
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
		this.summarize();
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
		this.summarize();
	}
	private void initialize()
	{
		this.typeCounts = new Hashtable<String,Double>();
		this.InCounts = new Hashtable<String,Hashtable<String,Double>>();
		this.outCounts = new Hashtable<String,Hashtable<String,Double>>();
		this.typeInAvg = new Hashtable<String,Double>();
		this.typeOutAvg = new Hashtable<String,Double>();
		this.typeArgs = new Hashtable<String,ArrayList<String>>();
		//this.corrMatrix = new Hashtable<String, Table<String, String, Double>>();
		//this.leftCorr =  new Hashtable<String, Table<String, String, Double>>();
		//this.rightCorr =  new Hashtable<String, Table<String, String, Double>>();
		this.corrMatrix =  HashBasedTable.create();
		this.leftCorr = HashBasedTable.create();
		this.rightCorr = HashBasedTable.create();
	}
	
	private void initializeQuery()
	{
		this.CountTable = new Hashtable<String,Double>();
		this.qryVars = new Hashtable<String,String>();
		this.varTerms = new Hashtable<String, Term>();
		this.qryIn = new Hashtable<String,SetMultimap<String,ArrayList<String>>>();
		this.qryOut = new Hashtable<String,SetMultimap<String,ArrayList<String>>>();
	}
	private void closeQuery()
	{
		this.CountTable.clear();
		this.qryVars.clear();
		this.varTerms.clear();
		this.qryIn.clear();
		this.qryOut.clear();
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
	private void summarize()
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
		

		Set<String> inkeys=this.InCounts.keySet();
		Set<String> outkeys=this.outCounts.keySet();
		Set<String> unionset = inkeys;
		unionset.addAll(inkeys);
		for(String obj:unionset)
		{
			Hashtable<String,Double> in = this.InCounts.get(obj);
			Hashtable<String,Double> out = this.outCounts.get(obj);
			
			for(String inrel:in.keySet())
			{
				for(String outrel:out.keySet())
				{
					if(!inrel.intern().equals(outrel.intern()))
					{
						Double corrtemp = this.corrMatrix.get(inrel, outrel);
						Double leftCorrTemp = this.leftCorr.get(inrel, outrel);
						if(corrtemp==null)
							corrtemp = 0.0;
						if(leftCorrTemp == null)
							leftCorrTemp = 0.0;
						Double inc = in.get(inrel.intern());
						Double outc = out.get(outrel.intern());
						if((inc!=null && outc !=null) || (inc!=0.0 && outc!=0.0))
						{
							corrtemp = corrtemp+1;
							leftCorrTemp = leftCorrTemp + 1;
						}
						if((inc!=null ||inc!=0.0) && (outc==null || outc==0))
						{
							leftCorrTemp = leftCorrTemp + 1;
						}
					}
				}
			}
		}
		//for(String ot:this.entityTypeHandles.keySet())
		//{
			
			/*
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
					String entVal = this.graph.get(ent);
					Table<String,String,Double> tempCorr = this.corrMatrix.get(entVal.intern());
					if(tempCorr==null)
						tempCorr = HashBasedTable.create();
					Table<String,String,Double> tempLeftCorr = this.leftCorr.get(entVal.intern());
					if(tempLeftCorr==null)
						tempLeftCorr = HashBasedTable.create();
					Table<String,String,Double> tempRightCorr = this.rightCorr.get(entVal.intern());
					if(tempRightCorr==null)
						tempRightCorr = HashBasedTable.create();
					if(c1>0 && c2>0)
					{
						//Double currentVal = tempCorr.contains(kRel, lRel)? tempCorr.get(kRel, lRel):0.0;
						//currentVal++;
						//tempCorr.put(kRel, lRel, currentVal);
						Double leftCurrVal = tempLeftCorr.contains(kRel, lRel)? tempLeftCorr.get(kRel, lRel):0.0;
						leftCurrVal++;
						tempLeftCorr.put(kRel, lRel, leftCurrVal);
						Double rightCurrVal =tempRightCorr.contains(kRel, lRel)? tempRightCorr.get(kRel, lRel):0.0;
						rightCurrVal++;
						tempRightCorr.put(kRel, lRel, rightCurrVal);
					}
					else if(c1>0 && c2==0)
					{
						Double leftCurrVal = tempLeftCorr.contains(kRel, lRel)? tempLeftCorr.get(kRel, lRel):0.0;
						leftCurrVal++;
						tempLeftCorr.put(kRel, lRel, leftCurrVal);
					}
					else if(c1==0 && c2>0)
					{
						Double rightCurrVal =0.0;
						rightCurrVal = tempRightCorr.contains(kRel, lRel)? tempRightCorr.get(kRel, lRel):0.0;
						rightCurrVal++;
						tempRightCorr.put(kRel, lRel, rightCurrVal);
					}
				}
			}*/
		//}
		
	}
	
	
	/**
	 * This is the function that should be called using the custom object of this class. 
	 * This will generate the approximate count of true grounding for a given clause. Bitrep bit-string representing the sense of each literal 
	 * in the clause. In current implementation bitrep is a string of all 1's of length equal to the #literals. We are restricted to counting over the 
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
				if(qryArgs.length==1)
				{
					String arg = qryArgs[0].getValue();
					String argType = argTypes.get(0);
					SetMultimap<String,ArrayList<String>> tempIn = this.qryIn.get(arg);
					SetMultimap<String,ArrayList<String>> tempOut = this.qryOut.get(arg);
					if(tempIn==null)
						tempIn = HashMultimap.create();
					if(tempOut==null)
						tempOut = HashMultimap.create();
					ArrayList<String> argL = new ArrayList<String>();
					argL.add(arg.intern());
					tempIn.put(pred, argL);
					tempOut.put(pred, argL);
					this.qryIn.put(arg, tempIn);
					this.qryOut.put(arg, tempOut);
					//-----------------
					this.qryVars.put(arg, argType);
					this.varTerms.put(arg, qryArgs[0]);
					this.CountTable.put(arg, this.typeCounts.get(argType));
				}
				else if(qryArgs.length==2)
				{
					String arg1 = qryArgs[0].getValue();
					String arg2 = qryArgs[1].getValue();
					String argType1 = argTypes.get(0);
					String argType2 = argTypes.get(1);
					SetMultimap<String,ArrayList<String>> tempIn = this.qryIn.get(arg2);
					SetMultimap<String,ArrayList<String>> tempOut = this.qryOut.get(arg1);
					if(tempIn==null)
						tempIn = HashMultimap.create();
					if(tempOut==null)
						tempOut = HashMultimap.create();
					ArrayList<String> argLIn = new ArrayList<String>();
					ArrayList<String> argLOut = new ArrayList<String>();
					argLIn.add(arg1);
					argLOut.add(arg2);
					tempIn.put(pred, argLIn);
					tempOut.put(pred, argLOut);
					this.qryIn.put(arg2, tempIn);
					this.qryOut.put(arg1, tempOut);
					//------------------
					this.qryVars.put(arg1, argType1);
					this.qryVars.put(arg2, argType2);
					this.varTerms.put(arg1, qryArgs[0]);
					this.varTerms.put(arg2, qryArgs[1]);
					this.CountTable.put(arg1, this.typeCounts.get(argType1));
					this.CountTable.put(arg2, this.typeCounts.get(argType2));
				}
				else if(qryArgs.length>2)
				{
					for(int i=0;i<qryArgs.length;i++)
					{
						String arg = qryArgs[i].getValue();
						String argType = argTypes.get(i);
						SetMultimap<String,ArrayList<String>> tempIn = this.qryIn.get(arg);
						SetMultimap<String,ArrayList<String>> tempOut = this.qryOut.get(arg);
						if(tempIn==null)
							tempIn = HashMultimap.create();
						if(tempOut==null)
							tempOut = HashMultimap.create();
						ArrayList<String> argLIn = new ArrayList<String>();
						ArrayList<String> argLOut = new ArrayList<String>();
						for(int j=0;j<qryArgs.length;j++)
						{
							if(j!=i)
								argLIn.add(qryArgs[j].getValue());
								argLOut.add(qryArgs[j].getValue());
						}
						tempIn.put(pred, argLIn);
						tempOut.put(pred, argLOut);
						this.qryIn.put(arg, tempIn);
						this.qryOut.put(arg, tempOut);
						//-----------------
						this.qryVars.put(arg, argType);
						this.varTerms.put(arg, qryArgs[0]);
						this.CountTable.put(arg, this.typeCounts.get(argType));
					}
				}
								
			}
			//----------------Sanitize count table-------------------------------
			for(String k:this.CountTable.keySet())
			{
				if(!this.varTerms.get(k).isVar())
					if(this.InCounts.containsKey(k) || this.outCounts.containsKey(k))
						this.CountTable.put(k, 1.0);
					else
						this.CountTable.put(k, 0.0);
			}
			//-------------------Sanitize complete ------------------
			
			//Start count estimation
			double crossProd = 1.0;
			for(Double val:this.CountTable.values())
				crossProd *= val;
			
			ArrayList<Double> factors = this.induceJoint(Clause);
			Double joint = 1.0;
			for(Double f:factors)
				joint *=f;
			this.closeQuery();
			return joint;
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
	private ArrayList<Double> induceJoint(Literal[] Clause)
	{

		ArrayList<Double> factors = new ArrayList<Double>();
		//Calculate factors
		for(Literal l:Clause)
		{
			String pred = l.getPredicateName();
			Term[] args = l.getArguments();
			int arity = args.length;
			if(arity==1)
			{
				String arg = args[0].getValue();
				SetMultimap<String,ArrayList<String>> inRels = this.qryIn.get(arg.intern());
				int num =0;
				if(inRels!=null)
					for(String r:inRels.keySet())
					{
						if(!r.equals(pred))
						{
							num++;
							//TODO
						}
					}
				if(num>0)
				{
					if(inRels!=null)
						factors.add(1.0);
					else
						factors.add(0.0);
				}
				
			}
			else if(arity==2)
			{
				String arg1 = args[0].getValue();
				String arg2 = args[1].getValue();
				boolean g1 = this.varTerms.get(arg1).isVar();
				boolean g2 = this.varTerms.get(arg2).isVar();
				SetMultimap<String,ArrayList<String>> inRels = this.qryIn.get(arg2.intern());
				if(!g1 && !g2)
				{
					HGHandle h1 = this.graph.getHandle(arg1.intern());
					HGHandle h2 = this.graph.getHandle(arg2.intern());
					List ret = hg.findAll(this.graph, hg.and(
							hg.type(this.relTypeHandles.get(pred.intern())),hg.incidentAt(h1, 0),
							hg.incidentAt(h2, 1)));
					if(ret==null || ret.isEmpty() || ret.size()==0)
					{
						factors.add(0.0);
					}
					else
						factors.add(1.0);
				}
				else if(!g1 && g1)
				{
					Double p = this.outCounts.get(arg1).get(pred);
					Double cross = 1.0 * this.typeCounts.get(this.qryVars.get(arg2));
					p = p/cross;
					factors.add(p);
				}
				else if(g1 && !g2)
				{
					Double p = this.outCounts.get(arg2).get(pred);
					Double cross = 1.0 * this.typeCounts.get(this.qryVars.get(arg1));
					p = p/cross;
					factors.add(p);
					SetMultimap<String,ArrayList<String>> tempIn = this.qryIn.get(arg1);
					for(String rel:tempIn.keySet())
					{
						Double corr = this.corrMatrix.get(rel, pred);
						Double inCorr = this.leftCorr.get(rel, pred);
						factors.add(corr/inCorr);
					}
				}
				else
				{
					SetMultimap<String,ArrayList<String>> tempIn = this.qryIn.get(arg1);
					if(tempIn!=null || !tempIn.isEmpty())
						for(String rel:tempIn.keySet())
						{
							Double corr = this.corrMatrix.get(rel, pred);
							Double inCorr = this.leftCorr.get(rel, pred);
							factors.add(corr/inCorr);
						}
					else
					{
						Double p = this.typeInAvg.get(pred);
						Double cross = this.typeCounts.get(this.qryVars.get(arg1))
								*this.typeCounts.get(this.qryVars.get(arg2));
						factors.add(p/cross);
					}
				}
			}
			else if(arity>2)
			{
				Double cross = 1.0;
				int gr = 0;
				//cross product calc
				for(Term a:args)
				{
					if(a.isVar())
						cross *= this.typeCounts.get(this.qryVars.get(a.getValue()));
					else
					{
						gr++;
						if(this.InCounts.containsKey(a.getValue().intern()) ||
								this.outCounts.containsKey(a.getValue().intern()))
							cross *= 1.0;
						else
							cross *=0.0;
					}
				}
				//factor calc
				if(gr==0)
				{
					Double p=this.typeInAvg.get(pred);
					factors.add(p/cross);
				}
				else
				{
					for(int i=0;i<args.length;i++)
					{
						String arg = args[i].getValue();
						if(!args[i].isVar())
						{
							Double p = this.outCounts.get(arg.intern()).get(pred);
							factors.add(p/cross);
						}
						else
						{
							SetMultimap<String,ArrayList<String>> tempIn = this.qryIn.get(arg);
							if(!(tempIn==null || tempIn.isEmpty() || tempIn.size()==0))
							{
								for(String rel:tempIn.keySet())
								{
									Double corr = this.corrMatrix.get(rel, pred);
									Double inCorr = this.leftCorr.get(rel, pred);
									factors.add(corr/inCorr);
								}
							}
						}
					}
				}
			}
		}
		return factors;
	}
	private void shutdown()
	{
		this.graph.close();
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
