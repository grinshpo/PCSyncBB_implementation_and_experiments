package ext.sim.tools;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

import bgu.dcr.az.api.ano.Register;
import bgu.dcr.az.api.prob.Problem;
import bgu.dcr.az.api.prob.ProblemType;

/**
 * Problem generator for meeting scheduling DCOP problems.  Uses the "peav events as variables" (PEAV) DCOP
 * representation (Maheswaran, et al., "Taking DCOP to the Real World: Efficient Complete Solutions for
 * Distributed Multi-Event Scheduling." AAMAS-04, 2004). PEAV extends the EAV representation in which there is a
 * DCOP variable for each meeting and domain-level agents are implicitly represented by costs between meetings 
 * that share participants and costs reflecting time preferences. In PEAV, each meeting is duplicated so that
 * each participating agent holds a copy of that meeting. PEAV was originally designed for agents with multiple
 * variables. Since such agents are currently NOT supported by AgentZero, we create an "agent" for each meeting
 * of each participant.
 * <br>
 * Meeting scheduling instances have agents (aka "resources"), meetings that the agents must participate in, and 
 * time slots when the meetings must be scheduled.  Every meeting has a duration and a preference vector of costs 
 * over time slots; this can be viewed as the sum of individual preference vectors of the participants of that 
 * meeting.  While this is a unary constraint, it is implemented as a binary constraint by summing the unary 
 * costs for every pair of meetings and splitting this on the binary cost between them.  Because this uses 
 * integer division, there are discretization effects.   For every pair of meetings there are symmetric travel 
 * times between the meetings.  
 * <br>
 * There are two ways to choose the participants to each meeting: either choosing the number of participants 
 * required for a meeting and then selecting these randomly from the agents, or choosing the number of meetings 
 * an agent is required to participate in and then selecting these randomly from the meetings.
 * <br> 
 * There are five ways to choose binary constraint costs for scheduling conflicts: counting number of conflicts,
 * counting number of agents participating in conflicting meetings, counting number of conflicting (i.e., 
 * overscheduled) agents, counting number of agents participating in the smaller of two conflicting meetings,
 * and considering the importance of the least important meeting of the two.
 * <br>
 * Most integer values are chosen uniformly at randomly from a range of consecutive integers [min..min+range-1].  
 * These ranges can be set using two Agent Zero parameter variables, one for the minimum value and one for the 
 * range (i.e., number of possible values), which should be strictly positive.
 * 
 * @author Tal Grinshpoun
 *
 */
@Register(name="peav-meeting-scheduling")

public class MeetingSchedulingPEAVGen extends MeetingSchedulingDCOPGen {
	
	// This is the maximal possible cost of a constraint (maximal meeting conflict+time constraints)
	private int maxCost = Math.max(2*n, minMeetingImportance + meetingImportanceRange) + minTimeCost + timeCostRange;
	// This is the cost of a hard constraint (maxCost times the combinations of meetings and the num of agents)
	private int hardConstraint = maxCost * m * m * n;
	
	@Override
	public void __generate(Problem prob, Random rand) {
		// first we create the meeting scheduling problem, then we will create the PEAV DCOP

		// parse the participant generation string and conflict cost string so we know how 
		// to choose participants for meetings and set costs for conflicting meeting times
		participantGeneration = ParticipantGeneration.valueOf(participantGenerationString.toUpperCase());
		conflictCost = ConflictCost.valueOf(conflictCostString.toUpperCase());

		// create the array of meetings
		Meeting [] meetings = new Meeting[m];
		// symmetric array of travel times between meetings
		int [][] travelTimes = new int[m][m];
		// Count the overall number of variables in the PEAV representation (a variable for each participant
		// in each meeting)
		int countPEAVVariables = 0;
		int [] meetingIndexes = new int[m];

		// we create the meetings, making sure that the shortest duration meeting is at index 0, so that
		// the first variable has the largest domain.
		// this is necessary because Agent Zero seems to use the first domain it sees to calculate a
		// backing array for the constraint tables.
		int shortestMeetingIndex = 0;
		for (int i = 0; i < meetings.length; i++) {
			meetings[i] = new Meeting(rand);
			meetingIndexes[i] = countPEAVVariables;
			countPEAVVariables += meetings[i].getNumParticipants();
			if (meetings[i].duration < meetings[shortestMeetingIndex].duration) {
				shortestMeetingIndex = i;
			}
			for (int j = i + 1; j < meetings.length; j++) {
				travelTimes[i][j] = travelTimes[j][i] = valueInRange(rand, minTravelTime, travelTimeRange);
			}
		}
		// now we swap the shortest meeting into the 0th index 
		if (shortestMeetingIndex != 0) {
			Meeting shortestMeeting = meetings[shortestMeetingIndex];
			meetings[shortestMeetingIndex] = meetings[0];
			meetings[0] = shortestMeeting;
		}
		// compute the meeting participants
		switch (participantGeneration) {
		case ATTENDANCE:
			// nothing to do; the meeting constructors created the participants
			break;
		case LOAD:
			initMeetingsByLoad(meetings, rand);
			break;
		default:
			throw new AssertionError("Unsupported participant generation type " + participantGeneration);
		}
	
		// then we create the DCOP representation as a PEAV
		// there is a variable for each participant in each meeting, with domain [0..t-duration] so that meetings 
		// cannot be scheduled at a time when they cannot finish before the last time slot
		ArrayList<Set<Integer>> domains = new ArrayList<Set<Integer>>(countPEAVVariables);
		for (Meeting meeting : meetings) {
			for (int var = 0; var < meeting.getNumParticipants(); var++) {
				LinkedHashSet<Integer> domain = new LinkedHashSet<Integer>();
				for (int i = 0; i < t - meeting.duration + 1; i++) {
					domain.add(i);
				}
				domains.add(domain);
			}	
		}
		prob.initialize(ProblemType.DCOP, domains);
		
		
		// now we add constraints.  
		for (int i = 0; i < meetings.length; i++) {
			Meeting meeting = meetings[i];
			// impose hard equality constraints between copies of the same meeting (for the different participants)
			for (int var1 = meetingIndexes[i]; var1 < (meetingIndexes[i]+meeting.getNumParticipants()-1); var1++) {
				for (int var2 = var1+1; var2 < (meetingIndexes[i]+meeting.getNumParticipants()); var2++) {
					for (int time1 : domains.get(var1)) {
						for (int time2 : domains.get(var2)) {
							if (time1 != time2) {
								prob.setConstraintCost(var1, time1, var2, time2, hardConstraint);
								prob.setConstraintCost(var2, time2, var1, time1, hardConstraint);
							}
						}
					}
				}
			}
		}
			
		// impose constraints between different meetings of the same participant
		for (int i = 0; i < meetings.length; i++) {
			Meeting meeting1 = meetings[i];
			for (int j = i + 1; j < meetings.length; j++) {
				Meeting meeting2 = meetings[j];
				for (int participant : meeting1.participants) {
					if (meeting2.participants.contains(participant)) {
						int var1 = meetingIndexes[i] + meeting1.participants.indexOf(participant);
						int var2 = meetingIndexes[j] + meeting2.participants.indexOf(participant);
						for (int time1 : domains.get(var1)) {
							// conflict window begins at earliest starting time for the meeting2 when 
							// participants in both meetings cannot make it to the meeting1 before the scheduled
							// start of meeting1
							int conflictWindowStart = Math.max(0, time1 - (meeting2.duration - 1 + travelTimes[j][i]));
							// conflict window ends when participants in both meeting can make it from meeting1 to
							// meeting2 before the scheduled start of meeting2.  this is the last (i.e., inclusive)
							// time slot with a conflict
							int conflictWindowEnd = Math.min(time1 + meeting1.duration + travelTimes[i][j] - 1, t-1);
							// compute cost of a conflict
							int cost = computeConflictCost(meeting1, meeting2);
							for (int time2 : domains.get(var2)) {
								// add in the unary cost; as in Hilla's code we take the integer average so that
								// the total cost is spread over the two constraints
								int unaryCost = (meeting1.getTimeCost(time1) + meeting2.getTimeCost(time2)) / 2;
								// we only add the conflict cost if the meeting times conflict
								int totalCost = unaryCost + (time2 >= conflictWindowStart && time2 <= conflictWindowEnd ? cost : 0);
								if (totalCost > 0) {
									prob.setConstraintCost(var1, time1, var2, time2, totalCost);
									prob.setConstraintCost(var2, time2, var1, time1, totalCost);
								}
							}
						}						
					}
						
				}
			}
		}
	}

}
