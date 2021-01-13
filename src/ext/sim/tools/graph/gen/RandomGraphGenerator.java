package ext.sim.tools.graph.gen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import bgu.dcr.az.api.exp.UnsupportedOperationException;
import ext.sim.tools.graph.Graph;
import ext.sim.tools.graph.Vertex;
import ext.sim.tools.graph.Graph.GraphGenerator;

public class RandomGraphGenerator implements GraphGenerator {
	private final double edgeProbability;

	public RandomGraphGenerator(double edgeProbability) {
		this.edgeProbability = edgeProbability;
	}

	@Override
	public Graph generate(int n, Random rand) {
		Graph g = new Graph(n);

		Map<Integer, Vertex> indexToVertex = new HashMap<>();

		int numEdges = (int) (edgeProbability * n * (n - 1) / 2.0);

		if (numEdges < n - 1)
			throw new UnsupportedOperationException("The graph cannot be connected");

		int index = 0;
		for (Vertex v : g.getVertices()) {
			indexToVertex.put(index, v);
			index++;
		}
		
		List<Vertex> C = new ArrayList<Vertex>();
		List<Vertex> U = new ArrayList<Vertex>();
		
		C.add(indexToVertex.get(rand.nextInt(n)));
		U.addAll(g.getVertices());
		U.remove(C.get(0));

		while (numEdges > 0) {
			Vertex v1 = C.get(rand.nextInt(C.size()));
			
			List<Vertex> next = new ArrayList<Vertex>();
			next.addAll(U.isEmpty() ? C : U);
			next.remove(v1);
			Vertex v2 = next.get(rand.nextInt(next.size()));
			if (!C.contains(v2)) C.add(v2);
			if (U.contains(v2)) U.remove(v2);
			
			if (!g.getNeighbours(v1).contains(v2)) {
				g.addUndirectedEdge(v1, v2);
				numEdges--;
			}
		}

		return g;
	}

}
