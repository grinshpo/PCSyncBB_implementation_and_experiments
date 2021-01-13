package ext.sim.modules;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ext.sim.tools.graph.Graph;
import ext.sim.tools.graph.Vertex;
import ext.sim.tools.graph.gen.RandomGraphGenerator;
import bgu.dcr.az.api.ano.Register;
import bgu.dcr.az.api.ano.Variable;
import bgu.dcr.az.api.prob.Problem;
import bgu.dcr.az.api.prob.ProblemType;
import bgu.dcr.az.exen.pgen.AbstractProblemGenerator;

@Register(name = "gc-costs-dcop")
public class GraphColoringCostsDCOP extends AbstractProblemGenerator {
	
	//private static final int SAME_COLOR_COST = 1;

	@Variable(name = "n", description = "number of agents", defaultValue = "10")
	private int n = 10;
	
	@Variable(name = "d", description = "number of colors", defaultValue = "3")
	private int d = 3;

	@Variable(name = "p1", description = "edge probability", defaultValue = "0.2")
	private float p1 = 0.2f;
	
	@Variable(name = "max-cost", description = "maximal cost", defaultValue = "10")
	private int maxCost = 10;
	
	private Graph networkTopology;

	public Graph getGraph() {
		return networkTopology;
	}

	@Override
	public void generate(Problem p, Random rand) {
		p.initialize(ProblemType.DCOP, n, d);
		p.getMetadata().put("max-cost", maxCost);
		p.getMetadata().put("p1", p1);
		p.getMetadata().put("n", n);
		p.getMetadata().put("d", d);
		
		networkTopology = new RandomGraphGenerator(p1).generate(n, rand);

		List<Vertex> vertices = new LinkedList<>(networkTopology.getVertices());
		final Map<Vertex, Integer> vertexToIndex = new HashMap<>();

		for (int i = 0; i < n; i++) {
			vertexToIndex.put(vertices.get(i), i);
		}
		
		for (Vertex u : networkTopology.getVertices()) {
			for (Vertex v : networkTopology.getNeighbours(u)) {
				for (int d1 = 0; d1 < d; d1++) {
					int val =rand.nextInt(maxCost);
					p.setConstraintCost(vertexToIndex.get(u), d1, vertexToIndex.get(v), d1, val);					
					p.setConstraintCost(vertexToIndex.get(v), d1, vertexToIndex.get(u), d1, val);					
				}
			}
		}

	}
	
}