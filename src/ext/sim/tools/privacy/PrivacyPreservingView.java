package ext.sim.tools.privacy;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import bgu.dcr.az.api.Agent;

public class PrivacyPreservingView implements Iterable<PrivateIdAndDomainAgentView> {
	private final Random rnd;
	private final Agent agent;
	private final List<PrivateIdAndDomainAgentView> views;
	
	public PrivacyPreservingView(Agent agent) {		
		this.agent = agent;
		this.rnd = new Random(agent.getId());
		views = new LinkedList<>();
	}
	
	public PrivateIdAndDomainAgentView generateView() {
		PrivateIdAndDomainAgentView view = new PrivateIdAndDomainAgentView(agent, rnd.nextLong());
		views.add(view);
		return view;
	}
	
	public PrivateIdAndDomainAgentView getView(Integer id) {
		for (PrivateIdAndDomainAgentView v : views) {
			if (v.equals(id)) {
				return v;
			}
		}
		return null;
	}
	
	public boolean containsView(Integer id) {
		return getView(id) != null;
	}

	@Override
	public Iterator<PrivateIdAndDomainAgentView> iterator() {
		return views.iterator();
	}
}