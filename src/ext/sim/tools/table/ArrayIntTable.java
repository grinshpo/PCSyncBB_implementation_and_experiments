package ext.sim.tools.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import bgu.dcr.az.api.DeepCopyable;
import bgu.dcr.az.api.prob.ImmutableProblem;
import bgu.dcr.az.api.tools.Assignment;

import com.google.common.collect.ImmutableList;

public class ArrayIntTable implements IntTable, DeepCopyable {
	/**
	 * Creates a new instance of <code>ArrayIntTable</code> indexed by variable ordinals from 
	 * domains in a constraint problem.
	 * @param prob The problem.
	 * @param vars An array of variables in the canonical order.
	 * @param vars2 Additional variables, following <code>vars</code> in the canonical order.
	 */
	public ArrayIntTable(ImmutableProblem prob, int [] vars, int ... vars2) {
		ImmutableList.Builder<Integer> builder = ImmutableList.builder();
		ImmutableList.Builder<Integer> dimsBuilder = ImmutableList.builder();
		ImmutableList.Builder<DomainSet> domsBuilder = ImmutableList.builder();
		for (int i = 0; i < vars.length; i++) {
			int domSize = prob.getDomainSize(vars[i]);
			builder.add(vars[i]);
			dimsBuilder.add(domSize);
			domsBuilder.add(RangeDomainSet.createRangeDomainSet(0, domSize - 1));
		}
		for (int i = 0; i < vars2.length; i++) {
			int domSize = prob.getDomainSize(vars2[i]);
			builder.add(vars2[i]);
			dimsBuilder.add(domSize);
			domsBuilder.add(RangeDomainSet.createRangeDomainSet(0, domSize - 1));
		}
		canonicalOrder = builder.build();
		dims = dimsBuilder.build();
		domains = domsBuilder.build();
		builder = ImmutableList.builder();
		int mult = 1;
		for (Integer dim : dims.reverse()) {
			builder.add(mult);
			mult = mult * dim;
		}
		idxMultipliers = builder.build().reverse();
		int maxSize = idxMultipliers.get(0) * dims.get(0);
		array = new int[maxSize];
	}

	/**
	 * Creates a new instance of <code>ArrayIntTable</code> indexed by variable ordinals from 
	 * domains in a constraint problem.
	 * @param prob The problem.
	 * @param vars The variables, in canonical order.
	 */
	public ArrayIntTable(ImmutableProblem prob, List<Integer> vars) {
		canonicalOrder = ImmutableList.copyOf(vars);
		ImmutableList.Builder<Integer> builder = ImmutableList.builder();
		ImmutableList.Builder<DomainSet> domBuilder = ImmutableList.builder();
		for (Integer var : canonicalOrder) {
			int domSize = prob.getDomainSize(var);
			builder.add(domSize);
			domBuilder.add(RangeDomainSet.createRangeDomainSet(0, domSize - 1));
		}
		dims = builder.build();
		domains = domBuilder.build();
		builder = ImmutableList.builder();
		int mult = 1;
		
		for (Integer dim : dims.reverse()) {
			builder.add(mult);
			mult = mult * dim;
		}
		idxMultipliers = builder.build().reverse();
		int tableSize = idxMultipliers.get(0) * dims.get(0);
		array = new int[tableSize];
	}
	
	/**
	 * Creates a new instance of <code>ArrayIntTable</code> indexed by variable values within 
	 * specified ranges between 0, inclusive, and domain sizes, exclusive, for each. 
	 * @param vars The array of variables, in canonical order.
	 * @param domainSizes The array of domain sizes for the variables, in canonical order.
	 */
	public ArrayIntTable(int [] vars, int [] domainSizes) {
		if (vars.length != domainSizes.length){
			throw new IllegalArgumentException("Number of vars=" + vars.length + " does not match number of domain sizes=" + domainSizes.length);
		}
		ImmutableList.Builder<Integer> builder = ImmutableList.builder();
		ImmutableList.Builder<Integer> dimsBuilder = ImmutableList.builder();
		ImmutableList.Builder<DomainSet> domsBuilder = ImmutableList.builder();
		for (int i = 0; i < vars.length; i++) {
			builder.add(vars[i]);
			dimsBuilder.add(domainSizes[i]);
			domsBuilder.add(RangeDomainSet.createRangeDomainSet(0, domainSizes[i] - 1));
		}
		canonicalOrder = builder.build();
		dims = dimsBuilder.build();
		domains = domsBuilder.build();
		builder = ImmutableList.builder();
		int mult = 1;
		for (Integer dim : dims.reverse()) {
			builder.add(mult);
			mult = mult * dim;
		}
		idxMultipliers = builder.build().reverse();
		int tableSize = idxMultipliers.get(0) * dims.get(0);
		array = new int[tableSize];
		
	}

	/**
	 * Creates a new instance of <code>ArrayIntTable</code> indexed by variable values within 
	 * specified ranges between 0, inclusive, and domain sizes, exclusive, for each. 
	 * @param vars The list of variables, in canonical order.
	 * @param domainSizes The list of domain sizes for the variables, in canonical order.
	 */
	public ArrayIntTable(List<Integer> vars, List<Integer> domainSizes) {
		if (vars.size() != domainSizes.size()){
			throw new IllegalArgumentException("Number of vars=" + vars.size() + " does not match number of domain sizes=" + domainSizes.size());
		}
		ImmutableList.Builder<Integer> builder = ImmutableList.builder();
		ImmutableList.Builder<Integer> dimsBuilder = ImmutableList.builder();
		ImmutableList.Builder<DomainSet> domsBuilder = ImmutableList.builder();
		for (int i = 0; i < vars.size(); i++) {
			builder.add(vars.get(i));
			dimsBuilder.add(domainSizes.get(i));
			domsBuilder.add(RangeDomainSet.createRangeDomainSet(0, domainSizes.get(i) - 1));
		}
		canonicalOrder = builder.build();
		dims = dimsBuilder.build();
		domains = domsBuilder.build();
		builder = ImmutableList.builder();
		int mult = 1;
		for (Integer dim : dims.reverse()) {
			builder.add(mult);
			mult = mult * dim;
		}
		idxMultipliers = builder.build().reverse();
		int tableSize = idxMultipliers.get(0) * dims.get(0);
		array = new int[tableSize];
		
	}

	/**
	 * Private copy constructor, used by <code>deepCopy()</code>.
	 * @param table The table to be copied.
	 */
	private ArrayIntTable(ArrayIntTable table) {
		canonicalOrder = table.canonicalOrder;
		dims = table.dims;
		idxMultipliers = table.idxMultipliers;
		domains = table.domains;
		array = new int[table.array.length];
		System.arraycopy(table.array, 0, array, 0, array.length);
	}

	@Override
	public Object deepCopy() {
		return new ArrayIntTable(this);
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || !(o instanceof IntTable)) {
			return false;
		}
		IntTable table = (IntTable) o;
		if (!canonicalOrder.equals(table.getCanonicalOrder()) || !domains.equals(table.getDomains())) {
			return false;
		}
		if (table instanceof ArrayIntTable) {
			ArrayIntTable arrayIntTable = (ArrayIntTable) table;
			return Arrays.equals(array, arrayIntTable.array);
		}
		IntTableIterator iter1 = iterator();
		IntTableIterator iter2 = table.iterator();
		while (iter1.hasNext()) {	// checking iter1.hasNext() or iter2.hasNext() is equivalent
			int val1 = iter1.next();
			int val2 = iter2.next();
			if (val1 != val2) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		if (hashCode == null) {
			int hash = 1;
			for (IntTableIterator iter = iterator(); iter.hasNext(); ) {
				hash = 31 * hash + iter.next();
			}
			hashCode = hash;
		}
		return hashCode;
	}

	@Override
	public ImmutableList<Integer> getCanonicalOrder() {
		return canonicalOrder;
	}

	@Override
	public int getEntry(Assignment a) throws IllegalArgumentException {
		return array[calcIdx(a)];
	}

	@Override
	public int getEntry(int... idxs) throws IllegalArgumentException {
		return array[calcIdx(idxs)];
	}

	@Override
	public ImmutableList<Integer> getDims() {
		return dims;
	}

	@Override
	public ImmutableList<DomainSet> getDomains() {
		return domains;
	}

	@Override
	public IntTableIterator iterator() {
		return new ArrayIntTableIterator();
	}

	@Override
	public void setEntry(int entryVal, Assignment a)
			throws UnsupportedOperationException {
		int idx = calcIdx(a);
		if (array[idx] != entryVal) {
			array[idx] = entryVal;
			hashCode = null;
		}
	}

	@Override
	public void setEntry(int entryVal, int... idxs)
			throws IllegalArgumentException, UnsupportedOperationException {
		int idx = calcIdx(idxs);
		if (array[idx] != entryVal) {
			array[idx] = entryVal;
			hashCode = null;
		}
	}
	
	@Override
	public int size() {
		return array.length;
	}

	/**
	 * Package-private method creating a copy of the array.
	 * @return A copy of the backing array.
	 */
	final int [] copyArray() {
		return array.clone();
	}
	/**
	 * Package-private method for comparing an array to this table's backing array.
	 * @param arr The array to compare to this array.
	 * @return <code>true</code> if <code>arr</code> is equal to this table's backing array,
	 * <code>false</code> otherwise.
	 */
	boolean equalsArray(int [] arr) {
		return Arrays.equals(array, arr);
	}
	
	private int calcIdx(Assignment ass) {
		int idx = 0;
		for (int i = 0; i < canonicalOrder.size(); i++) {
			int var = canonicalOrder.get(i);
			idx += ass.getAssignment(var) * idxMultipliers.get(i);
		}
		return idx;
	}
	
	private int calcIdx(int [] idxs) {
		int idx = 0;
		for (int i = 0; i < canonicalOrder.size(); i++) {
			idx += idxs[i] * idxMultipliers.get(i);
		}
		return idx;
	}
	
	private Assignment calcAss(int idx, Assignment ass) {
		for (int i= 0; i < idxMultipliers.size(); i++) {
			int var = canonicalOrder.get(i);
			int mult = idxMultipliers.get(i);
			int val = idx / mult;
			ass.assign(var, val);
			idx = idx % mult;
		}
		return ass;
	}
	
	private List<Integer> calcAss(int idx, List<Integer> idxs) {
		idxs.clear();
		for (int i = 0; i < idxMultipliers.size(); i++) {
			int mult = idxMultipliers.get(i);
			int val = idx / mult;
			idxs.add(val);
			idx = idx % mult;
		}
		return idxs;
	}
	
	private int [] calcAss(int idx, int [] idxs) {
		if (idxs.length < canonicalOrder.size()) {
			idxs = new int[canonicalOrder.size()];
		}
		for (int i = 0; i < idxMultipliers.size(); i++) {
			int mult = idxMultipliers.get(i);
			int val = idx / mult;
			idxs[i] = val;
			idx = idx % mult;
		}
		return idxs;
	}
	
	private final ImmutableList<Integer> canonicalOrder;
	private final ImmutableList<Integer> dims;
	private final ImmutableList<Integer> idxMultipliers;
	private final ImmutableList<DomainSet> domains;  
	private final int [] array;
	/**
	 * Cached hash code, initialized lazily.
	 */
	private Integer hashCode;
	
	
	private class ArrayIntTableIterator implements IntTableIterator {

		@Override
		public boolean hasNext() {
			return idx < array.length;
		}

		@Override
		public Integer next() {
			lastIdx = idx;
			return array[idx++];
		}

		@Override
		public Assignment getIdxAss() throws IllegalStateException {
			if (lastIdx < 0) {
				throw new IllegalStateException();
			}
			return calcAss(lastIdx, new Assignment());
		}

		@Override
		public Assignment getIdxAss(Assignment idxAss) throws IllegalStateException {
			if (lastIdx < 0) {
				throw new IllegalStateException();
			}
			return calcAss(lastIdx, idxAss);
		}

		@Override
		public List<Integer> getIdxList() throws IllegalStateException {
			if (lastIdx < 0) {
				throw new IllegalStateException();
			}
			return calcAss(lastIdx, new ArrayList<Integer>(canonicalOrder.size()));
		}

		@Override
		public List<Integer> getIdxList(List<Integer> idxList) throws IllegalStateException {
			if (lastIdx < 0) {
				throw new IllegalStateException();
			}
			return calcAss(lastIdx, idxList);
		}

		@Override
		public int[] getIdxArray(int[] idxArray) throws IllegalStateException,
				IllegalArgumentException {
			if (lastIdx < 0) {
				throw new IllegalStateException();
			}
			return calcAss(lastIdx, idxArray);
		}

		@Override
		public boolean hasPrev() {
			return idx > 0;
		}

		@Override
		public int prev() throws NoSuchElementException {
			if (!hasPrev()) {
				throw new NoSuchElementException();
			}
			lastIdx = --idx; 
			return array[idx];
		}
		
		@Override
		public void remove() throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void set(int entryVal) throws IllegalStateException, UnsupportedOperationException {
			if (lastIdx < 0) {
				throw new IllegalStateException();
			}
			if (array[lastIdx] != entryVal) {
				array[lastIdx] = entryVal;
				hashCode = null;
			}
		}

		private int idx;
		private int lastIdx = -1;
	}
}
