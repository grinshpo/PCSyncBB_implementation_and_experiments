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
import bgu.dcr.az.api.Message;
import bgu.dcr.az.api.ano.Register;
import bgu.dcr.az.api.exen.Execution;
import bgu.dcr.az.api.exen.Test;
import bgu.dcr.az.api.exen.stat.DBRecord;
import bgu.dcr.az.api.exen.stat.Database;
import bgu.dcr.az.api.exen.stat.VisualModel;
import bgu.dcr.az.api.exen.stat.vmod.LineVisualModel;
import bgu.dcr.az.exen.stat.AbstractStatisticCollector;
import bgu.dcr.az.exen.stat.NCCCStatisticCollector;

/**
 * This class collects the Non-Concurrent Runtime statistic.
 * DCOP runtime is usually measured in logical operations (e.g, NCCC, NCLO), rather than PCU runtime.
 * However, for algorithms that use other computationally expensive operations (e.g., cryptographic operations),
 * there is no choice but to measure the Non-Concurrent Runtime.
 *
 * @author Benny Lutati
 */
@Register(name = "ncr-sc")
public class NCRuntime extends AbstractStatisticCollector<NCRuntime.NCRRecord> {

    private long[] ncr;
    private long[] lastKnownCR;
    private String runningVar;
//    private Agent[] agents;
    private long counterResult = -1;

    @Override
    public VisualModel analyze(Database db, Test r) {
        String query = "select AVG(value) as avg, rVar, ALGORITHM_INSTANCE from NCR where TEST = '" + r.getName() + "' group by ALGORITHM_INSTANCE, rVar order by rVar";
        LineVisualModel line = new LineVisualModel(runningVar, "Avg(NCR)", "NCR");
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
//        this.agents = agents;
        System.out.println("NCR Statistic Collector registered");
        counterResult = 0;
        ncr = new long[agents.length];
        lastKnownCR = new long[agents.length];
        long currentTime = System.nanoTime();
        for (int i =0; i<lastKnownCR.length; i++) lastKnownCR[i] = currentTime;
        
        runningVar = ex.getTest().getRunningVarName();

        new Hooks.ReportHook("counter") {
			
			@Override
			public void hook(Agent arg0, Object[] arg1) {
				counterResult = (Long)arg1[0];
				System.out.println("reported: " + counterResult);
				
			}
		}.hookInto(ex);
        
        new Hooks.BeforeMessageProcessingHook() {
            @Override
            public void hook(Agent a, Message msg) {
                if (msg.getMetadata().containsKey("ncr")) { //can be system message or something...
                    long newNcr = (Long) msg.getMetadata().get("ncr");
                    
                    lastKnownCR[a.getId()] = System.nanoTime();
                    updateCurrentNccc(a.getId());
                    ncr[a.getId()] = max(newNcr, ncr[a.getId()]);
                }
                
                
            }
        }.hookInto(ex);
        
        new Hooks.AfterMessageProcessingHook() {
			
			@Override
			public void hook(Agent a, Message msg) {
				updateCurrentNccc(a.getId());
			}
		};
        
        new Hooks.BeforeMessageSentHook() {
            @Override
            public void hook(int sender, int recepiennt, Message msg) {
                if (sender >= 0) { //not system or something..
                    updateCurrentNccc(sender);
                    msg.getMetadata().put("ncr", ncr[sender]);
                }
            }
        }.hookInto(ex);

        new Hooks.TerminationHook() {
            @Override
            public void hook() {            	
            	NCRRecord r = new NCRRecord(ex.getTest().getCurrentExecutedAlgorithmInstanceName(), ex.getTest().getCurrentVarValue(), (max(ncr)) + counterResult);
            	submit(r);
            	System.out.println(r);
            }
        }.hookInto(ex);

    }

    @Override
    public String getName() {
        return "Number Of Concurrent Runtime";
    }

    private void updateCurrentNccc(int aid) {
        long last = lastKnownCR[aid];
        lastKnownCR[aid] = System.nanoTime();
        ncr[aid] = ncr[aid] + lastKnownCR[aid] - last;
    }

    public long currentNcccOf(int agent) {
        return ncr[agent];
    }

    public static class NCRRecord extends DBRecord {

    	String name;
        double rVar;
        double value;

        public NCRRecord(String name, double rVar, double value) {
        	this.name = name;
            this.rVar = rVar;
            this.value = value;
        }

        @Override
        public String provideTableName() {
            return "NCR";
        }

		@Override
		public String toString() {
			return "NCRRecord [name=" + name + ", rVar=" + rVar + ", value=" + value + "]";
		}
        
        
    }
}