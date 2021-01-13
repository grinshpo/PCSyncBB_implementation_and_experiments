package ext.sim.tools.table;

/**
 * Abstract base class of <code>IntTableIterator</code>s over <code>ImmutableIntTable</code>s.  The
 * <code>set</code> method on these iterators always throw an <code>UnsupportedOperationException</code>

 * @author Steven
 *
 */
public abstract class ImmutableIntTableIterator implements IntTableIterator {
	/**
	 * Guaranteed to throw an <code>UnsupportedOperationException</code>.
	 */
	@Override
	public final void set(int entryVal) throws IllegalStateException, UnsupportedOperationException {
		throw new UnsupportedOperationException("set is not supported on ImmutableIntTableIterator.");
	}

}
