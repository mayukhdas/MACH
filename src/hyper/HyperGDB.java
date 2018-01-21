package hyper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Hashtable;

import org.hypergraphdb.HGHandle;

public class HyperGDB {
	
	public static String dblocation = "./graphs/graph1";
	static boolean verbose = true;
	String schemaloc;
	String factloc;
	String dbName;
	Hashtable<String,HGHandle> relTypeHandles;
	Hashtable<String,HGHandle> entityTypeHandles;
	
	
	public HyperGDB() {
		this.schemaloc = null;
		this.factloc = null;
		this.dbName = "graph1";
		this.relTypeHandles = new Hashtable<String,HGHandle>();
		this.entityTypeHandles = new Hashtable<String,HGHandle>();
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
	}
	public HyperGDB(String schemaloc, String factloc, String dbName) {
		this.schemaloc = schemaloc;
		this.factloc = factloc;
		this.dbName = dbName;
		dblocation = this.genDbLoc();
		this.relTypeHandles = new Hashtable<String,HGHandle>();
		this.entityTypeHandles = new Hashtable<String,HGHandle>();
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
				if(!line.startsWith("//"))
				{
					if(line.startsWith("import:"))
					{
						
					}
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}

	


	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	

}
