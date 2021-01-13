package ext.sim.agents;

import java.util.Map;

import bgu.dcr.az.api.Agent;

public abstract class AbstractSeedableAgent extends Agent implements SeedableAgent {

	private Long seed = null;

	@Override
	public final void start() {
		Map<String, Object> metadata = getProblem().getMetadata();
		if (metadata.containsKey("alg-seed")) {
			// we need to set the seed.
			seed = ((Long) metadata.get("alg-seed")) + 37 * (getId() + 1);
			randomize(seed);
		} else {
			seed = null;
			randomize((getId() + 1) * 37 + 7919);
		}
//		System.out.println("Agent " + getId() + " has eff seed " + seed);
		
		__start();
	}
	
	protected abstract void __start();
	
	@Override
	public long getSeed() {
		return seed;
	}

	@Override
	public boolean seeded() {
		return seed != null;
	}


}
