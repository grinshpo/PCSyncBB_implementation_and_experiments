package ext.sim.tools.table;

import bgu.dcr.az.api.tools.Assignment;

/**
 * Abstract base class for immutable <code>IntTable</code> instances.  Any attempt to call the modifier
 * methods <code>setEntry</code> will result in an <code>UnsupportedOperationException</code> being thrown.
 * In addition, the return type of the <code>iterator</code> is narrowed to an 
 * <code>ImmutableIntTableIterator</code>.   

 * @author Steven
 *
 */
public abstract class ImmutableIntTable implements IntTable {
	/**
	 * Guaranteed to throw <code>UnsupportedOperationException</code>.
	 */
	@Override
	public final void setEntry(int entryVal, Assignment a) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("setEntry is not allowed on ImmutableIntTable.");
	}
	/**
	 * Guaranteed to throw <code>UnsupportedOperationException</code>.
	 */
	@Override
	public final void setEntry(int entryVal, int ... idxs) throws IllegalArgumentException, UnsupportedOperationException {
		throw new UnsupportedOperationException("setEntry is not allowed on ImmutableIntTable.");		
	}
	@Override
	public int hashCode() {
		int hashCode = 1;
		for (IntTableIterator iter = iterator(); iter.hasNext(); ) {
			hashCode = 31 * hashCode + iter.next();
		}
		return hashCode;
	}
	
	/**
	 * Returns an immutable iterator over the elements of this table.
	 * @return An <code>ImmutableIntTableIterator</code> over the elements in this table.
	 */
	@Override
	public abstract ImmutableIntTableIterator iterator();

}
