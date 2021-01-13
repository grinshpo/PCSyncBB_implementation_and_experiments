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
import ext.sim.agents.SyncBBAgent;
import ext.sim.agents.PCSyncBBAgent;

/**
 *
 * @author Vadim and Tal
 */
@Register(name = "ctr2-sc")
public class Counter2 extends AbstractStatisticCollector<Counter2.Counter2Record> {

	private String runningVar;

	@Override
	public VisualModel analyze(Database db, Test r) {
		String query = "select AVG(value) as avg, rVar, ALGORITHM_INSTANCE from CTR2 where TEST = '" + r.getName()
				+ "' group by ALGORITHM_INSTANCE, rVar order by rVar";
		LineVisualModel line = new LineVisualModel(runningVar, "Avg(CTR2)", "CTR2");
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
		System.out.println("CTR2 Statistic Collector registered");

		runningVar = ex.getTest().getRunningVarName();

		new Hooks.TerminationHook() {
			@Override
			public void hook() {
				Double sum = 0.0;
				if (agents[0] instanceof PCSyncBBAgent) {
					for (Agent agent : agents) {
						sum += ((PCSyncBBAgent)agent).counter2;
					}
					
					//sum /= agents.length;
					
					Counter2Record r = new Counter2Record(ex.getTest().getCurrentExecutedAlgorithmInstanceName(),
							ex.getTest().getCurrentVarValue(), sum);
					submit(r);
					System.out.println(r);					
				}
				if (agents[0] instanceof SyncBBAgent) {
					for (Agent agent : agents) {
						sum += ((SyncBBAgent)agent).counter2;
					}
					
					//sum /= agents.length;
					
					Counter2Record r = new Counter2Record(ex.getTest().getCurrentExecutedAlgorithmInstanceName(),
							ex.getTest().getCurrentVarValue(), sum);
					submit(r);
					System.out.println(r);					
				}
				if (agents[0] instanceof SyncBBAgent) {
					for (Agent agent : agents) {
						sum += ((SyncBBAgent)agent).counter2;
					}
					
					//sum /= agents.length;
					
					Counter2Record r = new Counter2Record(ex.getTest().getCurrentExecutedAlgorithmInstanceName(),
							ex.getTest().getCurrentVarValue(), sum);
					submit(r);
					System.out.println(r);					
				}
			}
		}.hookInto(ex);

	}

	@Override
	public String getName() {
		return "Number Of Counter2";
	}

	public static class Counter2Record extends DBRecord {

		String name;
		double rVar;
		double value;

		public Counter2Record(String name, double rVar, double value) {
			this.name = name;
			this.rVar = rVar;
			this.value = value;
		}

		@Override
		public String provideTableName() {
			return "CTR2";
		}

		@Override
		public String toString() {
			return "CTR2Record [name=" + name + ", rVar=" + rVar + ", value=" + value + "]";
		}

	}
}