package ext.sim.tools.table;

import java.util.Collection;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.UnmodifiableIterator;

public class GenDomainSet extends DomainSet {

	public GenDomainSet(ImmutableSet<Integer> set) {
		this.set = set;
	}
	
	@Override
	public int size() {
		return set.size();
	}

	@Override
	public boolean isEmpty() {
		return set.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return set.contains(o);
	}

	@Override
	public UnmodifiableIterator<Integer> iterator() {
		return set.iterator();
	}

	@Override
	public Object[] toArray() {
		return set.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return set.toArray(a);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return set.containsAll(c);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || o.getClass() != getClass()) {
			return false;
		}
		return set.equals(((GenDomainSet) o).set);
	}
	
	@Override
	public int hashCode() {
		return set.hashCode();
	}
	
	@Override
	public String toString() {
		return set.toString();
	}

	private final ImmutableSet<Integer> set;
}
