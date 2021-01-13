The PC-SyncBB code was implemented using the AgentZero simulator, which is an eclipse add-on. For a tutorial and installation instructions see: [https://docs.google.com/document/d/1B19TNQd8TaoAQVX6njo5v9uR3DBRPmFLhZuK0H9Wiks/view]( https://docs.google.com/document/d/1B19TNQd8TaoAQVX6njo5v9uR3DBRPmFLhZuK0H9Wiks/view) <br>
**Note that AgentZero supports Java versions 9 and earlier, so a Java compiler version 9 or earlier should be chosen in eclipse in order to run AgentZero appropriately.** <br>
**This project should be cloned into the eclipse workspace directory in order to find AgentZero’s libraries.** <br>
The AgentZero implementation of PC-SyncBB does not include the compare_CPA_cost_to_upper_bound() sub-protocol, since compare_CPA_cost_to_upper_bound() is executed separately over LAN with EC2 machines of type c5.large in Amazon's North Virginia data center, with every agent running on a separate machine, in order to faithfully simulate a genuinely distributed environment. This is noted in the paper in lines 937-939. <br>
Because compare_CPA_cost_to_upper_bound() is not implemented in AgentZero, the algorithm's flow is maintained by SyncBB (which is perfectly emulated by PC-SyncBB in the first place). The number of calls to compare_CPA_cost_to_upper_bound() is collected by counter2, in order to compute all the statistics (runtime, number of messages, network load). <br>
The project also includes implementations of P-SyncBB and SyncBB, in order to enable running the full set of experiments that are reported in the paper. <br>
AgentZero does not support computation of network load (it just counts the total number of messages, disregarding the message sizes). Therefore, we added counter1 and counter3 to count the number of messages of non-standard size (number of messages containing Paillier public keys and number of encrypted messages, respectively). Similar counters were added to the implementation of P-SyncBB for the same reason. The implementation of SyncBB includes counter2, which counts the number of assignments within each CPA that is being transmitted from one agent to the next. (Recall that the size of a transmitted CPA depends on the position of the agent that sends it within the fixed ordering of all agents; hence, we compute the overall CPAs' sizes and not just their number.) The counters counter1, counter2, and counter3, appear in AgentZero as statistics CTR1, CTR2, and CTR3, respectively. <br>
In the paper, we consider three statistics – simulated (non-concurrent) runtime, total number of messages, and total network load. Next, we explain how each of the three considered statistics is computed. <br>
___
**Simulated runtime:**
The NCR (non-concurrent runtime) statistic of AgentZero collects the simulated runtime information in all three algorithms. However, for PC-SyncBB the runtime of compare_CPA_cost_to_upper_bound() should be added. This is done by multiplying counter2 by the runtime of each instance of the sub-protocol (see Table 1 in the paper). <br>
___
**Total number of messages:**
The MSG statistic of AgentZero collects the total number of messages in all three algorithms. However, for PC-SyncBB the messages of compare_CPA_cost_to_upper_bound() should be added. This is done by multiplying counter2 by the number of messages of each instance of the sub-protocol (see Table 2 in the paper). <br>
___
**Network load:**
As noted, there is no network load statistic in AgentZero, so we compute it according to the collected results of MSG, CTR1, CTR2, and CTR3. Next, we explain the network load computation for each algorithm. <br>
<br>
*PC-SyncBB:* <br>
MSG\*MIN_MSG_SIZE; // all messages <br>
CTR1\*6\*BIG_INT_SIZE; // sending public keys <br>
CTR2\*PROTOCOL_NETWORK_LOAD; // calling compare_CPA_cost_to_upper_bound() <br>
CTR3\*BIG_INT_SIZE; // sending encrypted values <br>
<br>
*P-SyncBB:* <br>
MSG\*MIN_MSG_SIZE; // all messages <br>
CTR1\*6\*BIG_INT_SIZE; // sending public keys <br>
CTR3\*BIG_INT_SIZE; // sending encrypted values <br>
<br>
*SyncBB:* <br>
MSG\*MIN_MSG_SIZE; // all messages <br>
CTR2\*ASSIGNMENT_SIZE; // sending CPAs (total num of assignments within sent CPAs) <br>
<br>
*Constants:* <br>
MIN_MSG_SIZE = 20 bytes (minimal IP header) <br>
BIG_INT_SIZE = 128 bytes (64 bytes for 512 bit modulus, plus 64 bytes header) <br>
ASSIGNMENT_SIZE = 8 bytes (2 ints for variable and value) <br>
PROTOCOL_NETWORK_LOAD of compare_CPA_cost_to_upper_bound() is computed according to Table 2 in the paper. <br>
