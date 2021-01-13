package ext.sim.agents;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import bgu.dcr.az.api.agt.*;
import bgu.dcr.az.api.ano.*;
import bgu.dcr.az.api.tools.*;
import ext.sim.tools.privacy.Paillier;

/**
 * This is an implementation of the PC-SyncBB algorithm, which is a privacy-preserving version of SyncBB that
 * is immune to colluder coalitions of up to half the number of agents.
 * This implementation does not include the implementation of the compare_CPA_cost_to_upper_bound() sub-protocol,
 * since compare_CPA_cost_to_upper_bound() is executed separately over LAN with EC2 machines of type 
 * c5.large in Amazon's North Virginia data center, with every agent running on a separate machine, in order to
 * faithfully simulate a really distributed environment.
 * Because compare_CPA_cost_to_upper_bound() is not implemented in AgentZero, the algorithm's flow is maintained
 * by SyncBB (which is perfectly emulated by PC-SyncBB in the first place).
 * The number of calls to compare_CPA_cost_to_upper_bound() is collected by counter2, in order to compute all the
 * statistics (runtime, number of messages, network load).
 * 
 * The counter variables are used for computing the network load (there is no such statistic available in AgentZero).
 * 
 * Please note that for running meeting scheduling (MS) problems the first lines of start() should be changed because
 * the MAX-COST is computed differently in MS problems (lines 76-84 should be commented-out, whereas line 87 should
 * be placed in comments).
 * 
 * @author Tal Grinshpoun
 *
 */

@Algorithm(name="PCSyncBB", useIdleDetector=false)
public class PCSyncBBAgent extends SimpleAgent {

	private static final int EMPTY = -1;
	
	public static int maxCost;
	private BigInteger[] shareCPA;
	private BigInteger shareUpperBound;
	private BigInteger[] vectorZ;
	private int pk;
	private int optimalSetting;
	private Assignment cpa;
	private List<Integer> currentDomain;
	private int ub;		// The current upper bound is not part of the algorithm (it is actually from SyncBB in order to maintain the flow of the algorithm)
	private int decisionCounter; // A counter used by the first agent to count the number of received final decisions
	private int numOfPredecessorNeighbors;
	private Paillier paillier; // A Paillier cryptosystem used by the agent
	private Paillier[] cryptoSystems; // Array of cryptosystems held by each agent for its preceding agents as a substitute for public keys
	private int N;
	private int M;
	private int MIN_MEETING_IMPORTANCE; 
	private int MEETING_IMPORTANCE_RANGE; 
	private int MIN_TIME_COST;
	private int TIME_COST_RANGE;
	private int MAX_COST_SOFT;
	private int MAX_COST;
	private int S_DIGIT;
	private int S_SIZE;
	private int vectorZCounter; // A counter used in the update_shares_inCPA procedure to count the number of received Z vectors
	private int oldVal; // Remember in which index in vectorZcounter the encrypted value is ONE
	
    public int counter1 = 0; // Counting the number of messages containing Paillier public keys
    public int counter2 = 0; // Counting the number of calls to compare_CPA_cost_to_upper_bound()
    public int counter3 = 0; // Counting the number of encrypted messages
    
    public int prune_counter = 0;
    
    
    @Override
    public void start() {
    	// MAX_COST in MS problems:
    	// ------------------------
 /*   	N = 3;
    	M = getNumberOfVariables()/2;
    	MIN_MEETING_IMPORTANCE = 5;
    	MEETING_IMPORTANCE_RANGE = 5;
    	MIN_TIME_COST = 0;
    	TIME_COST_RANGE = 4;
    	MAX_COST_SOFT = Math.max(2*N, MIN_MEETING_IMPORTANCE + MEETING_IMPORTANCE_RANGE) + MIN_TIME_COST + TIME_COST_RANGE;
    	// This is the cost of a hard constraint (MAX_COST_SOFT times the combinations of meetings and the num of agents)
    	MAX_COST = MAX_COST_SOFT * M * M * N;   */
    	// MAX_COST in non-MS problems is just max-cost from the problem meta-data:
    	// ------------------------------------------------------------------------
    	MAX_COST = (Integer) getProblem().getMetadata().get("max-cost");
    	
    	S_DIGIT = (int) (Math.ceil(Math.log(getNumberOfVariables() * (getNumberOfVariables() -1) * MAX_COST) / Math.log(2)));
    	S_SIZE = (int) (Math.pow(2.0, S_DIGIT));
    	
    	
    	//System.out.println("S_DIGIT: "+S_DIGIT+", S_SIZE: "+S_SIZE);
    	
    	paillier = new Paillier();
    	cryptoSystems = new Paillier[getNumberOfVariables()];
    	
    	shareCPA = new BigInteger[getNumberOfVariables()];
    	for (int t=0; t<shareCPA.length; t++)
    		shareCPA[t] = BigInteger.ZERO;
    	
    	vectorZ = new BigInteger[getDomainOf(getId()).size()];
    	vectorZ[0] = paillier.Encryption(BigInteger.ONE);
    	oldVal = 0;
    	for (int t=1; t<vectorZ.length; t++)
    		vectorZ[t] = paillier.Encryption(BigInteger.ZERO);
    	
    	pk = EMPTY;
    	ub = Integer.MAX_VALUE;
    	numOfPredecessorNeighbors = 0;
    	
		for (int t : getNeighbors())
			if (t > getId()) {		
				counter1++;
				send("PUBLIC_KEY", getId(), paillier).to(t);
			}
    	
        if (isFirstAgent()) {
        	decisionCounter = 0;
        	//maxCost = (Integer) getProblem().getMetadata().get("max-cost");
			int qInfinity = MAX_COST*((getNumberOfVariables()*(getNumberOfVariables()-1))/2)+1;
			shareUpperBound = new BigInteger(Integer.toString(qInfinity));
			cpa = new Assignment();
			assignCPA();
        }
        else {
        	shareUpperBound = BigInteger.ZERO;
			for (int t : getNeighbors())
				if (t < getId())
					numOfPredecessorNeighbors++;	
        }
    }
     
	private void assignCPA() {
		if (pk == EMPTY) {
			currentDomain = new ArrayList<>(getDomainOf(getId()));
			java.util.Collections.shuffle(currentDomain);
		}
		pk++;
		
		if (pk >= getDomainOf(getId()).size())
			backtrack();
		else {
			Integer newVal = currentDomain.get(pk);
			cpa.assign(getId(), newVal);
			counter3 += (numOfPredecessorNeighbors*getDomainOf(getId()).size());
			// Run update_shares_inCPA(getId(), newVal);
			vectorZCounter = 0;
			for (int t : getNeighbors())
				if (t < getId()) {			
					vectorZCounter++;
					send("REQUEST_VECTOR_Z", getId()).to(t);
				}
			if (vectorZCounter == 0)
				updateSharesInCPAFisinshed();
		}
	}
	
	private void updateSharesInCPAFisinshed() {
		//System.out.println("updateSharesInCPAFisinshed by agent "+getId());
		int lb = cpa.calcCost(getProblem());
		
		if (isLastAgent()) {
			counter2++;
			// Run compare_CPA_cost_to_upper_bound();
			// The answer of compare_CPA_cost_to_upper_bound() should be checked, but instead we use here the standard check of SyncBB to maintain the flow of the algorithm
			if (lb < ub) {
				broadcast("NEW_OPTIMUM_FOUND", lb);
				handleNEWOPTIMUMFOUND(lb);
			}
			assignCPA();
		}
		else {
			// lines 7-8 in sub-protocol update_shares_in_CPA - more efficient version in which the old encryptions are shuffled
			int val = cpa.getAssignment(getId());
			// In reality I also need to shuffle the ZEROs, but I only move the ONE here
			BigInteger temp = vectorZ[oldVal];
			vectorZ[oldVal] = vectorZ[val];
			vectorZ[val] = temp;

			oldVal = val;
			
			counter2++;
			// Run compare_CPA_cost_to_upper_bound();
			// The answer of compare_CPA_cost_to_upper_bound() should be checked, but instead we use here the standard check of SyncBB to maintain the flow of the algorithm
			if (lb >= ub) {
				prune_counter++;
				assignCPA();
			}
			else
				send("CPA", cpa).toNextAgent();
		}
	}
	
	private void backtrack() {
		if (isFirstAgent()) {
			broadcast("COMPLETE");
			handleCOMPLETE();
		}
		else {
			for (int t : getNeighbors()) {
				if (t < getId()) {
					shareCPA[t] = BigInteger.ZERO;
					send("ZERO_SHARE", getId()).to(t);
				}
			}
			cpa.unassign(this);
			send("BACKTRACK").toPreviousAgent();
		}
	}	
	
	@WhenReceived("PUBLIC_KEY")
	public void handlePUBLICKEY(int sender, Paillier cryptoSystem){
		cryptoSystems[sender] = cryptoSystem;
	}

	@WhenReceived("REQUEST_VECTOR_Z")
	public void handleREQUESTVECTORZ(int sender){
		send("REPLY_VECTOR_Z", getId(), vectorZ).to(sender);
	}
	
	@WhenReceived("REPLY_VECTOR_Z")
	public void handleREPLYVECTORZ(int sender, BigInteger[] vectorZt){
		vectorZCounter--;	
		BigInteger rho,yt;
		Random rand = new Random();
		rho = new BigInteger(S_DIGIT,rand);
		yt = computeYt(vectorZt, sender);
		counter3++;
		send("Y_SHARE", getId(), yt).to(sender);
		shareCPA[sender] = rho;
		if (vectorZCounter == 0)
			updateSharesInCPAFisinshed();
	}
	
	private BigInteger computeYt(BigInteger[] vectorZt, int agentT) {
		// In the real computation it should also use rho and the values from the constraint matrix, but we use rand.nextInt(S_SIZE) to simulate the power size just to calculate the runtime 
		Random rand = new Random();
		BigInteger yt = BigInteger.ONE;
    	for (int i=0; i<vectorZt.length; i++) {
    		yt = yt.multiply(vectorZt[i].modPow(new BigInteger(Integer.toString(rand.nextInt(MAX_COST))), cryptoSystems[agentT].nsquare));
    	}
    	// Chose here vectorZt[0] but it should be the mult of all the vectorZt vals, but since it can be computed once for each agentT, I just use vectorZ[0]
    	yt.multiply(vectorZt[0].modPow(new BigInteger(Integer.toString(rand.nextInt(S_SIZE))), cryptoSystems[agentT].nsquare));
		return yt;
	}
	
	@WhenReceived("Y_SHARE")
	public void handleYSHARE(int sender, BigInteger yt){
		shareCPA[sender] = paillier.Decryption(yt);
	}	

	@WhenReceived("NEW_OPTIMUM_FOUND")
	public void handleNEWOPTIMUMFOUND(int newUb){
		ub = newUb;  // The upper bound update is not part of the algorithm (it is actually from SyncBB in order to maintain the flow of the algorithm)		
		shareUpperBound = BigInteger.ZERO;
		for (int t : getNeighbors())
			shareUpperBound.add(shareCPA[t]);
		optimalSetting = currentDomain.get(pk);
	}
		
	@WhenReceived("CPA")
	public void handleCPA(Assignment pa){
		cpa = pa.deepCopy();
		pk = EMPTY;
		assignCPA();
	}
	
	@WhenReceived("ZERO_SHARE")
	public void handleZEROSHARE(int senderId){
		shareCPA[senderId] = BigInteger.ZERO;
	}	

	@WhenReceived("BACKTRACK")
	public void handleBACKTRACK(){
		assignCPA();
	}

	@WhenReceived("COMPLETE")
	public void handleCOMPLETE(){
		// finish(optimalSetting);
		// Would like to finish privately as above, but the simulator forces to finish with a complete CPA, thus compromising decision privacy
		send("NON_PRIVATE_FINISH", getId(), optimalSetting).toFirstAgent();
	}	

	@WhenReceived("NON_PRIVATE_FINISH")
	public void handleNONPRIVATEFINISH(int finalId, int finalValue){
		decisionCounter++;
		cpa.assign(finalId, finalValue);
		if (decisionCounter == getNumberOfVariables())
			finish(cpa);
	}
	
}