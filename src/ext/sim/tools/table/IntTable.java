package ext.sim.tools.table;

import com.google.common.collect.ImmutableList;

import bgu.dcr.az.api.tools.Assignment;

/**
 * Interface for a k-dimensional table of <code>int</code>s.  A typical usage would be to store effective 
 * costs or modifiers for DBA, or to store messages to be passed in DPOP or Max-Sum.
 * 
 * Entries are specified by indices in the k-dimensions.  Each dimension is given a numerical label (e.g.,
 * corresponding to a variable number) and a domain (e.g., corresponding to a variable domain).  The order
 * of the dimensions is specified by the <it>canonical order</it>, which is fixed for an <code>IntTable</code>
 * instance. 
 * 
 * @author Steven
 *
 */
public interface IntTable {
	/**
	 * Gets the canonical order of the table.  This is the sequence of variables involved in this table,
	 * in the order specified at creation.  The canonical order of a table is immutable.
	 * @return An immutable list of the variables in the table, in order. 
	 */
	public ImmutableList<Integer> getCanonicalOrder();
	/**
	 * Gets the entry specified by the indices in an assignment.
	 * @param a The assignment.
	 * @return The entry associated with the subset of variables of the assignment that are in this table.
	 * @throws IllegalArgumentException if the assignment does not contain all variables in this table.
	 */
	public int getEntry(Assignment a) throws IllegalArgumentException;
	/**
	 * Gets the entry consistent with an assignment, specified as a list of indices in the canonical order.
	 * @param idxs The indices specifying the entry, in canonical order.
	 * @return The entry associated with the assignment.
	 * @throws IllegalArgumentException if there are an incorrect number of values, or if the one of the
	 * values is not in the range of the table. 
	 */
	public int getEntry(int ... idxs) throws IllegalArgumentException;
	/**
	 * Gets the dimensions (i.e., sizes) of the table, specified in the canonical order.
	 * @return An immutable list of the dimensions.
	 */
	public ImmutableList<Integer> getDims();
	/**
	 * Gets the domains of the dimensions of the table, specified in the canonical order.
	 * @return An immutable list of the domains.
	 */
	public ImmutableList<DomainSet> getDomains();

	/**
	 * Compares the specified object with this table for equality.  Returns <code>true</code> if and
	 * only if the specified object is also an <code>IntTable</code>, both tables have the same canonical
	 * order and domains, and all corresponding entries in the two tables are equal.  This definition 
	 * ensures that the <code>equals</code> method works properly across different implementations of the
	 * <code>IntTable</code> interface.  Note that these conditions may be extremely expensive to check.   
	 * @param o The object to be compared for equality with this table.
	 * @return <code>true</code> if the specified object is equal to this table; false otherwise.
	 */
	@Override
	public boolean equals(Object o);
	/**
	 * Returns the hash code for this table.  The hash code is defined to be the result of the 
	 * <code>hashCode</code> implementation for <code>java.util.List</code> for a row-major linearization
	 * of the table.  This definition ensures that, for any two implementations of the <code>IntTable</code> 
	 * interface, the hash code is consistent with equals, required by the general contract of 
	 * <code>Object.hashCode()</code>. 
	 * 
	 * Note that computing the hash code may be an extremely expensive operation.
	 * 
	 * @return the hash code value for this table.
	 */
	@Override
	public int hashCode();
	
	/**
	 * Returns an iterator over the elements of this table.
	 * @return An iterator over the elements in this table.
	 */
	public IntTableIterator iterator();

	/**
	 * Sets the value of an entry with the indices specified by an assignment (optional operation).
	 * @param entryVal The new value for the entry.
	 * @param a The assignment specifying the indices.
	 * @throws UnsupportedOperationException if the <code>set</code> operation is not supported by this table.
	 */
	public void setEntry(int entryVal, Assignment a) throws UnsupportedOperationException;
	/**
	 * Sets the value of an entry (optional operation).
	 * @param entryVal The new value for the entry.
	 * @param idxs The indices specifying the entry.
	 * @throws IllegalArgumentException if the number of values does not match the dimensionality of the table.
	 * @throws UnsupportedOperationException if the <code>set</code> operation is not supported by this table.
	 */
	public void setEntry(int entryVal, int ... idxs) throws IllegalArgumentException, UnsupportedOperationException;
	/**
	 * Gets the number of entries in the table.
	 * @return The number of entries.
	 */
	public int size();
}
