package ext.sim.agents;

import java.util.HashSet;
import java.util.Set;

import bgu.dcr.az.api.agt.*;
import bgu.dcr.az.api.ano.*;
import bgu.dcr.az.api.tools.*;

/**
 * This is an implementation of the (non-privacy-preserving) SyncBB algorithm.
 * 
 * The counter variable is used for computing the network load (there is no such statistic available in AgentZero).
 * 
 * @author Tal Grinshpoun
 *
 */

@Algorithm(name="SyncBB", useIdleDetector=false)
public class SyncBBAgent extends SimpleAgent {

	private double lb;
	private Set<Integer> currentDomain;
	private Assignment bestSolution;
	
	public int counter2 = 0; // total number of assignments within sent CPAs (not including backtrack)
	
    @Override
    public void start() {
        if (isFirstAgent()) {
            handleCPA(new Assignment(), 0, Double.MAX_VALUE);
        }
    }
    
    
    private Integer chooseNextValue(Assignment cpa, double ub) {
    	if (!currentDomain.isEmpty()) {
    		Integer val = cpa.findMinimalCostValue(getId(), currentDomain, getProblem());
    		currentDomain.remove(val);
    		
    		if (cpa.calcAddedCost(getId(), val, getProblem()) + lb < ub) {
    			return val;
    		}
    	}
    	
    	return null;
    }

	private void processCPA(Assignment cpa, double ub) {
		Integer val = chooseNextValue(cpa, ub);
		
		if (val == null) {
			if (isFirstAgent()) {
				finish(bestSolution);
			} else {
				cpa.unassign(this);
				
				send("BACKTRACK", cpa, ub).toPreviousAgent();
			}
		} else {
			cpa.assign(getId(), val);
			
			if (isLastAgent()) {
				counter2 += (getId()+1);
				send("BEST_SOLUTION", cpa).toFirstAgent();
				
				cpa.unassign(this);
				
				processCPA(cpa, lb + cpa.calcAddedCost(getId(), val, getProblem()));
			} else {
				counter2 += (getId()+1);
				send("CPA", cpa, lb + cpa.calcAddedCost(getId(), val, getProblem()), ub).toNextAgent();
			}
		}
	}

	@WhenReceived("CPA")
	public void handleCPA(Assignment cpa, double lb, double ub) {
		currentDomain = new HashSet<>(getDomainOf(getId()));
	
		
		this.lb = lb;
		
		processCPA(cpa, ub);
	}
	
	

	@WhenReceived("BACKTRACK")
	public void handleBACKTRACK(Assignment cpa, double ub) {
		processCPA(cpa, ub);
	}

	@WhenReceived("BEST_SOLUTION")
	public void handleBESTSOLUTION(Assignment cpa) {
		bestSolution = cpa;
	}

}
