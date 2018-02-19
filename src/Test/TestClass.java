package Test;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;

import Helper.Clause;
import Helper.Literal;
import hyper.HyperGDB;
import hyper.Utils;

public class TestClass {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		HyperGDB hgdb = new HyperGDB("./data/uw-cse_rdn/train1_advisedby/train1_advisedby_bk.txt",
				"./data/uw-cse_rdn/train1_advisedby/train1_advisedby_facts.txt");
		
		
		//hasposition(person292, faculty_affiliate).taughtby(course15, person292, winter_0001).
		Literal[] clause = Clause.generateClause("taughtby(Course1,person292,Term1)^hasposition(person292,Fac)");
		Utils.println(clause[0]);
		Double count = hgdb.ApproxCount(clause, "11");
		Utils.print(count);
		
		
		
		//exact count
		HGHandle h = hgdb.graph.getHandle("person292");
		HGHandle ht = hgdb.relTypeHandles.get("taughtby");
		HGHandle hh = hgdb.relTypeHandles.get("hasposition");
		
		Utils.println(h);
		
		long c = hg.count(hgdb.graph, hg.and(hg.type(hh),hg.incident(h)));
		Utils.println(c);

	}

}
