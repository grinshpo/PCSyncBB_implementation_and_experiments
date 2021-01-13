package ext.sim.modules;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import ext.sim.tools.graph.Graph;
import ext.sim.tools.graph.Vertex;
import ext.sim.tools.graph.gen.RandomGraphGenerator;
import bgu.dcr.az.api.ano.Register;
import bgu.dcr.az.api.ano.Variable;
import bgu.dcr.az.api.prob.Problem;
import bgu.dcr.az.api.prob.ProblemType;
import bgu.dcr.az.api.tools.Assignment;
import bgu.dcr.az.exen.pgen.AbstractProblemGenerator;

@Register(name = "gc-dcop")
public class GraphColoringDCOP extends AbstractProblemGenerator {
	
	private static final int SAME_COLOR_COST = 1;

	@Variable(name = "n", description = "number of agents", defaultValue = "10")
	private int n = 10;
	
	@Variable(name = "d", description = "number of colors", defaultValue = "4")
	private int d = 4;

	@Variable(name = "p1", description = "edge probability", defaultValue = "0.2")
	private float p1 = 0.2f;
	
	@Variable(name = "max-cost", description = "maximal cost", defaultValue = "1")
	private int maxCost = SAME_COLOR_COST;
	
	private Graph networkTopology;
	private int[][] neighbores;

	public Graph getGraph() {
		return networkTopology;
	}

	public int[] getNeigbores(int aid) {
		return neighbores[aid];
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

		neighbores = new int[n][];
		for (int i = 0; i < n; i++) {
			Set<Vertex> neighbours = networkTopology.getNeighbours(vertices.get(i));
			neighbours.add(vertices.get(i));
			neighbores[i] = new int[neighbours.size()];
			int j = 0;
			for (Vertex v : neighbours) {
				neighbores[i][j++] = vertexToIndex.get(v);
				
		
			}
		}

		for (int i = 0; i < n; i++) {
			for(int j = 0; j < neighbores[i].length ; j++){
				for(int k = 0 ; k < d ; k++){
					p.setConstraintCost(i, k, neighbores[i][j], k, SAME_COLOR_COST);
				}
			}
		}

	}

	public int getConstraintCost(int aid, Assignment cpa){
		int cost = 0;
		for(int j = 0; j < neighbores[aid].length ; j++){
			//Check for each neighbor of aid if the colors assigned are the same:
			if(cpa.getAssignment(aid).equals(cpa.getAssignment(neighbores[aid][j])) ){
				cost += SAME_COLOR_COST;
			}
		}
		
		return cost;
	}


}