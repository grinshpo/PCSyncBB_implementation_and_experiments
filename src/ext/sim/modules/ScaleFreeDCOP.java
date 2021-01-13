package ext.sim.modules;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ext.sim.tools.graph.Graph;
import ext.sim.tools.graph.Vertex;
import ext.sim.tools.graph.gen.ScaleFreeGraphGenerator;
import bgu.dcr.az.api.ano.Register;
import bgu.dcr.az.api.ano.Variable;
import bgu.dcr.az.api.prob.Problem;
import bgu.dcr.az.api.prob.ProblemType;
import bgu.dcr.az.exen.pgen.AbstractProblemGenerator;

@Register(name = "sf-dcop")
public class ScaleFreeDCOP extends AbstractProblemGenerator {
	
	//private static final int SAME_COLOR_COST = 1;

	@Variable(name = "n", description = "number of agents", defaultValue = "10")
	private int n = 10;
	
	@Variable(name = "d", description = "domain size", defaultValue = "3")
	private int d = 3;

	@Variable(name = "m0", description = "basic graph size", defaultValue = "4")
	private int m0 = 4;
	
	@Variable(name = "ep", description = "edge probability", defaultValue = "0.75")
	private float ep = 0.75f;
	
	@Variable(name = "m", description = "new vertex links", defaultValue = "1")
	private int m = 1;
	
	@Variable(name = "max-cost", description = "maximal cost", defaultValue = "10")
	private int maxCost = 10;
	
	private Graph networkTopology;

	public Graph getGraph() {
		return networkTopology;
	}

	@Override
	public void generate(Problem p, Random rand) {
		p.initialize(ProblemType.DCOP, n, d);
		p.getMetadata().put("n", n);
		p.getMetadata().put("d", d);
		p.getMetadata().put("m0", m0);
		p.getMetadata().put("ep", ep);
		p.getMetadata().put("m", m);
		p.getMetadata().put("max-cost", maxCost);
		
		
		networkTopology = new ScaleFreeGraphGenerator(m, m0, ep).generate(n, rand);

		List<Vertex> vertices = new LinkedList<>(networkTopology.getVertices());
		final Map<Vertex, Integer> vertexToIndex = new HashMap<>();

		for (int i = 0; i < n; i++) {
			vertexToIndex.put(vertices.get(i), i);
		}
		
		for (Vertex u : networkTopology.getVertices()) {
			for (Vertex v : networkTopology.getNeighbours(u)) {
				for (int d1 = 0; d1 < d; d1++) {
					for (int d2 = 0; d2 < d; d2++) {
						int val =rand.nextInt(maxCost);
						p.setConstraintCost(vertexToIndex.get(u), d1, vertexToIndex.get(v), d2, val);					
						p.setConstraintCost(vertexToIndex.get(v), d2, vertexToIndex.get(u), d1, val);
					}
				}
			}
		}
		
		

	}
	
}