package ext.sim.modules;

import java.util.Random;

import bgu.dcr.az.api.prob.Problem;
import bgu.dcr.az.api.ano.Register;
import bgu.dcr.az.api.ano.Variable;
import bgu.dcr.az.exen.pgen.UnstructuredDCOPGen;

@Register(name = "dcop-connected-f")
public class FixedConnectedProblemGenerator extends UnstructuredDCOPGen {

    @Variable(name = "n", description = "number of variables", defaultValue="2")
    int n = 2;
    @Variable(name = "d", description = "domain size", defaultValue="2")
    int d = 2;
    @Variable(name = "max-cost", description = "maximal cost of constraint", defaultValue="100")
    int maxCost = 100;
    @Variable(name = "p1", description = "probablity of constraint between two variables", defaultValue="0.6")
    float p1 = 0.6f;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Generating : ").append("n = ").append(n).append("\nd = ").append(d).append("\nmaxCost = ").append(maxCost);
        return sb.toString();
    }	
	
    @Override
    public void generate(Problem p, Random rand) {
        super.generate(p, rand);
        while (true) {
            boolean[] connections = new boolean[p.getNumberOfVariables()];
            calcConnectivity(p, 0, connections);
            if (allTrue(connections)) {
                p.getMetadata().put("max-cost", maxCost);
                p.getMetadata().put("p1", p1);
                return;
            }

            connect(0, getUnconnected(connections), p, rand);
        }

    }

    private int getUnconnected(boolean[] connections) {
        for (int i = 0; i < connections.length; i++) {
            if (!connections[i]) {
                return i;
            }
        }
        return -1;
    }

    private void connect(int var1, int var2, Problem p, Random rnd) {
        int val2 = rnd.nextInt(p.getDomainSize(var2));
        int val1 = rnd.nextInt(p.getDomainSize(var1));
        int cost = abs(rnd.nextInt()) % maxCost + 1;
        p.setConstraintCost(var1, val1, var2, val2, cost);
        p.setConstraintCost(var2, val2, var1, val1, cost);
    }

    private void calcConnectivity(Problem p, int root, boolean[] discovered) {
        discovered[root] = true;
        for (int n : p.getNeighbors(root)) {

            if (!discovered[n]) {
                calcConnectivity(p, n, discovered);
            } else if (allTrue(discovered)) {
                return;
            }
        }
    }

    private boolean allTrue(boolean[] a) {
        for (int i = 0; i < a.length; i++) {
            if (!a[i]) {
                return false;
            }
        }
        return true;
    }
}
