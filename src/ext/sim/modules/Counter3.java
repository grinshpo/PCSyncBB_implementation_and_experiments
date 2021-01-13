/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ext.sim.modules;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import bgu.dcr.az.api.Agent;
import bgu.dcr.az.api.Hooks;
import bgu.dcr.az.api.ano.Register;
import bgu.dcr.az.api.exen.Execution;
import bgu.dcr.az.api.exen.Test;
import bgu.dcr.az.api.exen.stat.DBRecord;
import bgu.dcr.az.api.exen.stat.Database;
import bgu.dcr.az.api.exen.stat.VisualModel;
import bgu.dcr.az.api.exen.stat.vmod.LineVisualModel;
import bgu.dcr.az.exen.stat.AbstractStatisticCollector;
import bgu.dcr.az.exen.stat.NCCCStatisticCollector;
import ext.sim.agents.PCSyncBBAgent;
import ext.sim.agents.PSyncBBAgent;

/**
 *
 * @author Vadim and Tal
 */
@Register(name = "ctr3-sc")
public class Counter3 extends AbstractStatisticCollector<Counter3.Counter3Record> {

	private String runningVar;

	@Override
	public VisualModel analyze(Database db, Test r) {
		String query = "select AVG(value) as avg, rVar, ALGORITHM_INSTANCE from CTR3 where TEST = '" + r.getName()
				+ "' group by ALGORITHM_INSTANCE, rVar order by rVar";
		LineVisualModel line = new LineVisualModel(runningVar, "Avg(CTR3)", "CTR3");
		try {
			ResultSet rs = db.query(query);
			while (rs.next()) {
				line.setPoint(rs.getString("ALGORITHM_INSTANCE"), rs.getFloat("rVar"), rs.getFloat("avg"));
			}
			return line;
		} catch (SQLException ex) {
			Logger.getLogger(NCCCStatisticCollector.class.getName()).log(Level.SEVERE, null, ex);
		}

		return null;
	}

	@Override
	public void hookIn(final Agent[] agents, final Execution ex) {
		System.out.println("CTR3 Statistic Collector registered");

		runningVar = ex.getTest().getRunningVarName();

		new Hooks.TerminationHook() {
			@Override
			public void hook() {
				Double sum = 0.0;
				if (agents[0] instanceof PCSyncBBAgent) {
					for (Agent agent : agents) {
						sum += ((PCSyncBBAgent)agent).counter3;
					}
					
					
					//sum /= agents.length;
					
					Counter3Record r = new Counter3Record(ex.getTest().getCurrentExecutedAlgorithmInstanceName(),
							ex.getTest().getCurrentVarValue(), sum);
					submit(r);
					System.out.println(r);					
				}
				if (agents[0] instanceof PSyncBBAgent) {
					for (Agent agent : agents) {
						sum += ((PSyncBBAgent)agent).counter3;
					}
					
					//sum /= agents.length;
					
					Counter3Record r = new Counter3Record(ex.getTest().getCurrentExecutedAlgorithmInstanceName(),
							ex.getTest().getCurrentVarValue(), sum);
					submit(r);
					System.out.println(r);					
				}
			}
		}.hookInto(ex);

	}

	@Override
	public String getName() {
		return "Number Of Counter3";
	}

	public static class Counter3Record extends DBRecord {

		String name;
		double rVar;
		double value;

		public Counter3Record(String name, double rVar, double value) {
			this.name = name;
			this.rVar = rVar;
			this.value = value;
		}

		@Override
		public String provideTableName() {
			return "CTR3";
		}

		@Override
		public String toString() {
			return "CTR3Record [name=" + name + ", rVar=" + rVar + ", value=" + value + "]";
		}

	}
}