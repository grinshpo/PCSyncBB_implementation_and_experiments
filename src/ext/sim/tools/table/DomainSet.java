package ext.sim.tools.table;

import java.util.Collection;
import java.util.Set;

/**
 * The domain of a variable as an immutable set.  The mutation methods, <code>add</code>, 
 * <code>addAll</code>, <code>clear</code>, <code>remove</code>, <code>removeAll</code>, and 
 * <code>retainAll</code>, are all guaranteed to throw <code>UnsupportedOperationException</code>.
 * The <code>iterator</code> method also returns an <code>UnmodifiableIterator</code> that 
 * cannot be used to modify the set.
 *  
 * @author Steven
 *
 */
public abstract class DomainSet implements Set<Integer> {

	/**
	 * Guaranteed to throw an <code>UnsupportedOperationException</code> and leave the set unchanged.
	 * @throws UnsupportedOperationException always.
	 * @deprecated Unsupported operation.
	 */
	@Deprecated
	@Override
	public final boolean add(Integer e) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("DomainSet is immutable.");
	}

	/**
	 * Guaranteed to throw an <code>UnsupportedOperationException</code> and leave the set unchanged.
	 * @throws UnsupportedOperationException always.
	 * @deprecated Unsupported operation.
	 */
	@Deprecated
	@Override
	public boolean remove(Object o) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("DomainSet is immutable.");
	}

	/**
	 * Guaranteed to throw an <code>UnsupportedOperationException</code> and leave the set unchanged.
	 * @throws UnsupportedOperationException always.
	 * @deprecated Unsupported operation.
	 */
	@Deprecated
	@Override
	public boolean addAll(Collection<? extends Integer> c) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("DomainSet is immutable.");
	}

	/**
	 * Guaranteed to throw an <code>UnsupportedOperationException</code> and leave the set unchanged.
	 * @throws UnsupportedOperationException always.
	 * @deprecated Unsupported operation.
	 */
	@Deprecated
	@Override
	public boolean retainAll(Collection<?> c) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("DomainSet is immutable.");
	}

	/**
	 * Guaranteed to throw an <code>UnsupportedOperationException</code> and leave the set unchanged.
	 * @throws UnsupportedOperationException always.
	 * @deprecated Unsupported operation.
	 */
	@Deprecated
	@Override
	public boolean removeAll(Collection<?> c) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("DomainSet is immutable.");
	}

	/**
	 * Guaranteed to throw an <code>UnsupportedOperationException</code> and leave the set unchanged.
	 * @throws UnsupportedOperationException always.
	 * @deprecated Unsupported operation.
	 */
	@Deprecated
	@Override
	public void clear() {
		throw new UnsupportedOperationException("DomainSet is immutable.");
	}

}
