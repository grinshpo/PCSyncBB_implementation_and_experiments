package ext.sim.agents;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import ext.sim.tools.privacy.Paillier;
import bgu.dcr.az.api.agt.*;
import bgu.dcr.az.api.ano.*;
import bgu.dcr.az.api.tools.*;
import bgu.dcr.az.api.ano.WhenReceived;

/**
 * This is an implementation of the P-SyncBB algorithm, which is a privacy-preserving version of SyncBB that
 * is immune only to solitary conduct.
 * 
 * All cryptographic operations are performed in order to faithfully assess the run-time and network load, but
 * the algorithm's flow is maintained by SyncBB (which is perfectly emulated by P-SyncBB in the first place).
 * 
 * The counter variables are used for computing the network load (there is no such statistic available in AgentZero).
 * 
 * @author Tal Grinshpoun
 *
 */

@Algorithm(name="PSyncBB", useIdleDetector=false)
public class PSyncBBAgent extends SimpleAgent {
	private double ub;		// This is the current upper bound, used only by the first agent
	private boolean computedCPA;
	private double CPACost;
	private Assignment cpa;
	private Set<Integer> currentDomain;
	private Assignment bestSolution;
	private int secCounter;	// This is a counter that counts the number of received messages in the secure protocols
	private int protocol4Counter; // This is a counter that counts the number of received messages in protocol 4 by second agent (1)
	private double protocol4ub; // UB used by the second agent (1) in protocol 4
	private int randCounter;
	private double secCost;	// This is the calculated cost in the secure protocols
	private int latestSum;	// This is the ID of the latest sum computation, used only by the first agent
	private int rcaCounter;	// This is a counter that counts the number of received messages in "request current assignment" by the first agent and "TERMINATE" massages by the last agent (only used in the DecisionPrivacy version)
	private boolean currentlyBest; // A Boolean variable used by the first agent to determine whether the new solution is the best found so far (only used in the DecisionPrivacy version)
	private Paillier paillier; // A Paillier cryptosystem used by the agent (only used in the DecisionPrivacy version)
	private BigInteger[] bestSolutionPrivate; // Array of encrypted best values (only used in the DecisionPrivacy version)
	private Paillier[] cryptoSystems; // Array of cryptosystems held by the first agent as a substitute for public keys (only used in the DecisionPrivacy version) 
	private Integer tempVal;
	private int agentNum;
	private BigInteger tempCalc;
	private double bigC; 		// This is the precomputed upper bound used to reduce the number of expensive operations in Protocol5
	private static final int S_SIZE = 256;
	
	public int counter1 = 0; // Counting the number of messages containing Paillier public keys 
	//public int counter2 = 0; // This counter is not used in P-SyncBB
	public int counter3 = 0; // Counting the number of encrypted messages
	
	
    @Override
    public void start() {
    	
    	randCounter = 0;
    	protocol4Counter = 0;
    	paillier = new Paillier();
    	
        if (isFirstAgent()) {	
        	
        	bestSolutionPrivate = new BigInteger[getNumberOfVariables()];
        	cryptoSystems = new Paillier[getNumberOfVariables()];
        	cryptoSystems[0] = paillier;
        	ub = Double.MAX_VALUE;
        	cpa = new Assignment();
        	currentDomain = new HashSet<>(getDomainOf(getId()));
        	computedCPA = true;
        	latestSum = 0;
        	//precomputeBigC(); // precompute the upper bound for the use of "BIG C" in Protocol5
        	assignCPA(); //Should return if precomputation is removed
        }
        else {
        	counter1++;
        	send("PUBLIC_KEY", getId(), paillier).toFirstAgent();
        }
        if (isLastAgent()) {
        	bestSolution = new Assignment();
        	rcaCounter = 0;
        }
    }
    
    

    private Integer chooseNextValue() {
    	if (!currentDomain.isEmpty()) {
    		Integer val = cpa.findMinimalCostValue(getId(), currentDomain, getProblem());
    		currentDomain.remove(val);
    		
    		return val;
    	}
    	
    	return null;
    }
    	
	private void assignCPA() {
		tempVal = chooseNextValue();
		
		if (tempVal == null)
			backtrack();
		else if (isLastAgent()) {
			cpa.assign(getId(), tempVal);
			send("CHECK_SOLUTION").toFirstAgent();
		}
		else {
			// May add another condition to apply a heuristic regarding when to perform the secure summation
			if (computedCPA == false && (getId() > 2)) {
				secCounter = 0;
				secCost = 0;
				for (int i=1; i < getId(); i++)
					send("PROTOCOL3").to(i);
			}
			else
				assignCPAafterSum();
		}
	}	
		
	void assignCPAafterSum() {
		// May add another condition to apply a heuristic regarding when to perform the bound check
		if (computedCPA == true && (getId() > 2)) {
			initiateProtocol4();
		}
		else
			assignCPAafterBoundCheck();
	}
	
	void assignCPAafterBoundCheck() {
		cpa.assign(getId(), tempVal);
		send("CPA", cpa).toNextAgent();
	}
	



	private void backtrack() {
		if (isFirstAgent()) {
			for (int i=0; i<getNumberOfVariables(); i++) {
				BigInteger bi = bestSolutionPrivate[i].multiply(cryptoSystems[i].Encryption(new BigInteger("0")));
				counter3++;
				send("ASSIGNMENT_IN_SOLUTION", bi).to(i);
			}
		}
		else {
			cpa.unassign(this);
			send("BACKTRACK").toPreviousAgent();
		}
	}


	@WhenReceived("PUBLIC_KEY")
	public void handlePUBLICKEY(int sender, Paillier cryptoSystem){
		cryptoSystems[sender] = cryptoSystem;
	}
	
	@WhenReceived("CPA")
	public void handleCPA(Assignment pa){
		cpa = pa.deepCopy();
		currentDomain = new HashSet<>(getDomainOf(getId()));
		
		// This is the place to optimize a bit by pre-ordering the values in the domain
		computedCPA = false;
		assignCPA();
	}

	@WhenReceived("BACKTRACK")
	public void handleBACKTRACK(){
		cpa.unassign(this);
		assignCPA();
	}


	@WhenReceived("CHECK_SOLUTION")
	public void handleCHECKSOLUTION(){
		secCounter = 0;
		secCost = 0;
		latestSum++;
		broadcast("PROTOCOL2", latestSum);
	}

	@WhenReceived("REQUEST_CURRENT_ASSIGNMENT")
	public void handleREQUESTCURRENTASSIGNMENT(){
		BigInteger pVal = paillier.Encryption(new BigInteger(cpa.getAssignment(getId()).toString()));
		counter3++;
		send("CURRENT_ASSIGNMENT",getId(),pVal).toFirstAgent();
		if (isLastAgent())
			backtrack();
	}
	
	@WhenReceived("CURRENT_ASSIGNMENT")
	public void handleCURRENTASSIGNMENT(int sender, BigInteger pVal){
		if (currentlyBest == true)
			bestSolutionPrivate[sender] = pVal;
		
		if (sender < getNumberOfVariables()-1) {
			rcaCounter++;
			if (rcaCounter == (getNumberOfVariables()-2))
				send("REQUEST_CURRENT_ASSIGNMENT").toLastAgent();
		}
	}
	
	

	@WhenReceived("SOLUTION_ANS")
	public void handleSOLUTIONANS(boolean answer){
		if (answer == true)
			bestSolution = cpa.deepCopy();
		backtrack();
	}	
	
	@WhenReceived("ASSIGNMENT_IN_SOLUTION")
	public void handleASSIGNMENTINSOLUTION(BigInteger encryptedVal){
		int val = paillier.Decryption(encryptedVal).intValue();
		send("TERMINATE", getId(), val).toLastAgent();
	}
	

	@WhenReceived("TERMINATE")
	public void handleTERMINATE(int sender, int val){
		rcaCounter++;
		bestSolution.assign(sender, val);
		if (rcaCounter == getNumberOfVariables())
			finish(bestSolution);
	}

	

	@WhenReceived("PROTOCOL2")
	public void handlePROTOCOL2(int sumId){
		tempCalc = new BigInteger("0");
		Random rand = new Random();
		for (int i=1; i<getNumberOfVariables(); i++) {
			if (i != getId()) {
				counter3++;
				send("PROTOCOL2_SEND_RANDOM", sumId, new BigInteger(S_SIZE,rand)).to(i);
			}
		}
		if (getNumberOfVariables() == 2)
			send("PROTOCOL2_RESULT", sumId, cpa.calcAddedCost(getId(), cpa.getAssignment(getId()), getProblem())).toFirstAgent();
	}
	

	
	
	@WhenReceived("PROTOCOL2_SEND_RANDOM")
	public void handlePROTOCOL2SENDRANDOM(int sumId, BigInteger addition){
		randCounter++;

		if (randCounter == (getNumberOfVariables()-2)) {
			randCounter = 0;
			send("PROTOCOL2_RESULT", sumId, cpa.calcAddedCost(getId(), cpa.getAssignment(getId()), getProblem())).toFirstAgent();
		}
	}




	@WhenReceived("PROTOCOL2_RESULT")
	public void handlePROTOCOL2RESULT(int sumId, double addedCost){
		if (sumId == latestSum) {
			secCounter++;
			secCost += addedCost;
			if (secCounter == (getNumberOfVariables()-1)) {
				if (secCost < ub) {
					ub =  secCost;
					currentlyBest = true;
					bestSolutionPrivate[0] = paillier.Encryption(new BigInteger(cpa.getAssignment(getId()).toString()));
				}
				else {
					currentlyBest = false;
				}
				rcaCounter = 0;
				for (int i=1; i<getNumberOfVariables()-1; i++)
					send("REQUEST_CURRENT_ASSIGNMENT").to(i);
				if (getNumberOfVariables() == 2)
					send("REQUEST_CURRENT_ASSIGNMENT").toLastAgent();
			}
		} // else this is an obsolete secure summation that should be disregarded |
	}




	@WhenReceived("PROTOCOL3")
	public void handlePROTOCOL3(){
		int senderId = getCurrentMessage().getSender(); 
		tempCalc = new BigInteger("0");
		Random rand = new Random();
		for (int i=1; i<senderId; i++) {
			if (i != getId()) {
				counter3++;
				send("PROTOCOL3_SEND_RANDOM", senderId, new BigInteger(S_SIZE,rand)).to(i);
			}
		}
	}


	@WhenReceived("PROTOCOL3_SEND_RANDOM")
	public void handlePROTOCOL3SENDRANDOM(int initiatorId, BigInteger addition){
		randCounter++;

		if (randCounter == (initiatorId-2)) {
			randCounter = 0;
			send("PROTOCOL3_RESULT", cpa.calcAddedCost(getId(), cpa.getAssignment(getId()), getProblem())).to(initiatorId);
		}
	}
	

	@WhenReceived("PROTOCOL3_RESULT")
	public void handlePROTOCOL3RESULT(double addedCost){
		secCounter++;
		secCost += addedCost;
		
		if (secCounter == (getId()-1)) {
			CPACost = secCost;
			computedCPA = true;
			assignCPAafterSum();
		}
	}

	public void initiateProtocol4() {
		Random rand = new Random();
		counter3++;
		send("PROTOCOL4_INIT", new BigInteger(S_SIZE,rand)).toFirstAgent(); // Sending dummy value to agent A1 (step 3)
		new BigInteger(S_SIZE,rand); // Dummy random generation of steps 5 and 6
		send("PROTOCOL4_FROM_AK", CPACost + cpa.calcAddedCost(getId(), tempVal, getProblem())).to(1);
	}
	
	@WhenReceived("PROTOCOL4_INIT")
	public void handlePROTOCOL4INIT(BigInteger dummy){
		send("PROTOCOL4_FROM_A1", ub).to(1);
	}


	@WhenReceived("PROTOCOL4_FROM_AK")
	public void handlePROTOCOL4FROMAK(double cost){
		agentNum = getCurrentMessage().getSender();
		secCost = cost;
		protocol4();
	}

	@WhenReceived("PROTOCOL4_FROM_A1")
	public void handlePROTOCOL4FROMA1(double ub){
		protocol4ub = ub;
		protocol4();
	}
	
	public void protocol4() {
		protocol4Counter++;
		if (protocol4Counter == 2) // This means that the two messages of step 5 were already received
			send("PROTOCOL4_STEP5_RESULT").to(agentNum);
		if (protocol4Counter == 4) { // This means that the two messages of step 6 were already received
			protocol4Counter=0; // Reset counter for next instance of protocol4
			send("PROTOCOL4_STEP6_RESULT", secCost >= protocol4ub).to(agentNum);
		}
	}

	@WhenReceived("PROTOCOL4_STEP5_RESULT")
	public void handlePROTOCOL4STEP5RESULT(){
		initiateProtocol4();
	}
	
	@WhenReceived("PROTOCOL4_STEP6_RESULT")
	public void handlePROTOCOL4STEP6RESULT(boolean boundCheckResult){
		if (boundCheckResult == true)
			backtrack();
		else
			assignCPAafterBoundCheck();
	}

}
