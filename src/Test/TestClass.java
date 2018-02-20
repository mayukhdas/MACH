package Test;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;

import hgHelper.Clause;
import hgHelper.Literal;
import hyper.HyperGDB;
import hyper.GraphUtils;

public class TestClass {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		HyperGDB hgdb = new HyperGDB("./data/uw-cse_rdn/train1_advisedby/train1_advisedby_bk.txt",
				"./data/uw-cse_rdn/train1_advisedby/train1_advisedby_facts.txt");
		
		
		//hasposition(person292, faculty_affiliate).taughtby(course15, person292, winter_0001).
		Literal[] clause = Clause.generateClause("taughtby(Course1,person292,Term1)^hasposition(person292,Fac)");
		GraphUtils.println(clause[0]);
		Double count = hgdb.ApproxCount(clause, "11");
		GraphUtils.println(count);
		
		
		
		//exact count
		HGHandle h = hgdb.graph.getHandle("person292");
		HGHandle ht = hgdb.relTypeHandles.get("taughtby");
		HGHandle hh = hgdb.relTypeHandles.get("hasposition");
		
		//Utils.println(h);
		
		long c = hg.count(hgdb.graph, hg.and(hg.type(ht),hg.incident(h)));
		GraphUtils.println(c);

	}

}
