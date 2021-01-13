package ext.sim.tools.table;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import bgu.dcr.az.api.tools.Assignment;

/**
 * An iterator over an <code>IntTable</code>.  In addition to the usual methods, <code>IntTableIterator</code>
 * has methods for retrieving the indices of the current element.
 * 
 * Removal is not supported.
 * @author Steven
 *
 */
public interface IntTableIterator extends Iterator<Integer> {
	/**
	 * Gets the indices of the element returned by the most recent call to <code>next</code>.  Indices
	 * are returned as an <code>Assignment</code>.
	 * @return Assignment with the indices.
	 * @throws IllegalStateException if <code>next</code> has not been called.
	 */
	public Assignment getIdxAss() throws IllegalStateException;
	/**
	 * Gets the indices of the element returned by the most recent call to <code>next</code> and stores
	 * them in a given assignment.  This assignment is also returned.  Any value assignment to variables
	 * not in the table's dimensions are left alone.
	 * @param idxAss The assignment. 
	 * @return Assignment with the indices.
	 * @throws IllegalStateException if <code>next</code> has not been called.
	 */
	public Assignment getIdxAss(Assignment idxAss) throws IllegalStateException;
	/**
	 * Gets the indices of the element returned by the most recent call to <code>next</code>.  Indices are 
	 * in the canonical order.
	 * @return List of the indices.
	 * @throws IllegalStateException if <code>next</code> has not been called. 
	 */
	public List<Integer> getIdxList() throws IllegalStateException;
	/**
	 * Gets the indices of element returned by the most recent call to <code>next</code>. 
	 * The indices are in the canonical order.
	 * @param idxList List to be used to store the indices to avoid reallocation.
	 * @return The indices stored in <code>vals</code>. 
	 * @throws IllegalStateException if <code>next</code> has not been called.
	 */
	public List<Integer> getIdxList(List<Integer> idxList) throws IllegalStateException;
	/**
	 * Gets the indices of the element returned by the most recent call to <code>next</code>, 
	 * in the canonical order.
	 * @param idxArray Array to be used to store the indices.  Can be <code>null</code>, in which case
	 * a new array is allocated. 
	 * @return Array of the indices.  If <code>vals != null</code>, this is <code>vals</code>, otherwise
	 * it is a newly-allocated array.
	 * @throws IllegalStateException if there has not been a call to <code>next</code>.
	 * @throws IllegalArgumentException if <code>vals != null</code> but its length does not match the
	 * dimensionality of the table. 
	 */
	public int [] getIdxArray(int [] idxArray) throws IllegalStateException, IllegalArgumentException;
	/**
	 * Checks if there is a previous element in the table.
	 * @return <code>true</code> if there is a previous element, <code>false</code> otherwise.
	 */
	public boolean hasPrev();
	/**
	 * Gets the previous element in the table.
	 * @return The previous element.
	 * @throws NoSuchElementException if there is no previous element.
	 */
	public int prev() throws NoSuchElementException;

	/**
	 * Always throws an <code>UnsupportedOperationException</code>.
	 */
	@Override
	public void remove() throws UnsupportedOperationException;
	
	/**
	 * Replaces the entry of the element returned by the most recent call to <code>next</code> (optional
	 * operation).
	 * @param entryVal The new value for the element.
	 * @throws IllegalStateException if <code>next</code> has not been called.
	 * @throws UnsupportedOperationException if the <code>set</code> method is not supported by this
	 * <code>TableIterator</code>.
	 */
	public void set(int entryVal) throws IllegalStateException, UnsupportedOperationException;
}
