package ext.sim.tools.privacy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import bgu.dcr.az.api.Agent;
import bgu.dcr.az.api.DeepCopyable;

public class PrivateIdAndDomainAgentView implements DeepCopyable{

	private final int encodedId;
	private final int decodedId;
	private final Map<Integer, Integer> domainEncoding;
	private final Map<Integer, Integer> domainDecoding;

	private PrivateIdAndDomainAgentView(PrivateIdAndDomainAgentView view) {
		encodedId = view.encodedId;
		decodedId = view.decodedId;
		domainEncoding = new HashMap<>(view.domainEncoding);
		domainDecoding = new HashMap<>(view.domainDecoding);
	} 
	
	public PrivateIdAndDomainAgentView(Agent a, long seed) {
		Random rnd = new Random(seed);
		
		encodedId = -Math.abs(rnd.nextInt());
		decodedId = a.getId();
		
		domainEncoding = new HashMap<Integer, Integer>();
		domainDecoding = new HashMap<Integer, Integer>();
		for (Integer v : a.getDomain()) {
			Integer code = rnd.nextInt();
			domainEncoding.put(v,  code);
			domainDecoding.put(code, v);
		}
	}

	public int encodedId() {
		return encodedId;
	}

	public int decodedId() {
		return decodedId;
	}

	public Integer encode(Integer value) {
		return domainEncoding.get(value);
	}
	
	public Set<Integer> encode(Set<Integer> values) {
		Set<Integer> res = new HashSet<Integer>();
		for (Integer v : values) {
			res.add(encode(v));
		}
		return res;
	}

	public Integer decode(Integer code) {
		return domainDecoding.get(code);
	}
	
	public Set<Integer> decode(Set<Integer> codes) {
		Set<Integer> res = new HashSet<Integer>();
		for (Integer c : codes) {
			res.add(decode(c));
		}
		return res;
	}

	@Override
	public Object deepCopy() { 
		return new PrivateIdAndDomainAgentView(this);
	}
}
