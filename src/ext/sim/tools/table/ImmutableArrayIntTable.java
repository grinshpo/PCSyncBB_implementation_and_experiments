package ext.sim.tools.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import bgu.dcr.az.api.DeepCopyable;
import bgu.dcr.az.api.prob.ImmutableProblem;
import bgu.dcr.az.api.tools.Assignment;

import com.google.common.collect.ImmutableList;

public class ImmutableArrayIntTable extends ImmutableIntTable implements DeepCopyable {
	public static class Builder {
		/**
		 * Creates a new instance of <code>ArrayIntTable</code> indexed by variable ordinals from 
		 * domains in a constraint problem.
		 * @param prob The problem.
		 * @param vars The variables in the canonical order.
		 */
		public Builder(ImmutableProblem prob, int ... vars) {
			ImmutableList.Builder<Integer> builder = ImmutableList.builder();
			ImmutableList.Builder<Integer> dimsBuilder = ImmutableList.builder();
			ImmutableList.Builder<DomainSet> domsBuilder = ImmutableList.builder();
			for (int i = 0; i < vars.length; i++) {
				int domSize = prob.getDomainSize(vars[i]);
				builder.add(vars[i]);
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
		 * @param vars An array of variables in the canonical order.
		 * @param vars2 Additional variables, following <code>vars</code> in the canonical order.
		 */
		public Builder(ImmutableProblem prob, int [] vars, int ... vars2) {
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
		public Builder(ImmutableProblem prob, List<Integer> vars) {
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
			int maxSize = idxMultipliers.get(0) * dims.get(0);
			array = new int[maxSize];
		}
		
		/**
		 * Creates a new instance of <code>ArrayIntTable</code> indexed by variable values within 
		 * specified ranges between 0, inclusive, and domain sizes, exclusive, for each. 
		 * @param vars The array of variables, in canonical order.
		 * @param domainSizes The array of domain sizes for the variables, in canonical order.
		 */
		public Builder(int [] vars, int [] domainSizes) {
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
			int maxSize = idxMultipliers.get(0) * dims.get(0);
			array = new int[maxSize];
			
		}

		/**
		 * Creates a new instance of <code>ArrayIntTable</code> indexed by variable values within 
		 * specified ranges between 0, inclusive, and domain sizes, exclusive, for each. 
		 * @param vars The list of variables, in canonical order.
		 * @param domainSizes The list of domain sizes for the variables, in canonical order.
		 */
		public Builder(List<Integer> vars, List<Integer> domainSizes) {
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
			int maxSize = idxMultipliers.get(0) * dims.get(0);
			array = new int[maxSize];
			
		}
		
		public ImmutableArrayIntTable build() throws IllegalStateException {
			if (array == null) {
				throw new IllegalStateException("build already called for this builder");
			}
			ImmutableArrayIntTable table = new ImmutableArrayIntTable(this);
			array = null;
			return table;
		}
		
		public ImmutableList<Integer> getCanonicalOrder() {
			return canonicalOrder;
		}

		public IntTableIterator iterator() {
			return new BuilderIterator();
		}
		
		public void setEntry(int entryVal, Assignment a) {
			array[calcIdx(a)] = entryVal;
		}

		public void setEntry(int entryVal, int... idxs) {
			array[calcIdx(idxs)] = entryVal;
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

		private final ImmutableList<Integer> canonicalOrder;
		private final ImmutableList<Integer> dims;
		private final ImmutableList<Integer> idxMultipliers;
		private final ImmutableList<DomainSet> domains;  
		private int [] array;
		
		private class BuilderIterator implements IntTableIterator {

			@Override
			public boolean hasNext() {
				return idx < array.length;
			}

			@Override
			public Integer next() throws NoSuchElementException {
				if (idx >= array.length) {
					throw new NoSuchElementException();
				}
				lastIdx = idx;
				idx++;
				return array[lastIdx];
			}

			@Override
			public Assignment getIdxAss() throws IllegalStateException {
				return getIdxAss(new Assignment());
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
				return getIdxList(new ArrayList<Integer>(dims.size()));
			}

			@Override
			public List<Integer> getIdxList(List<Integer> idxList) throws IllegalStateException {
				if (lastIdx < 0) {
					throw new IllegalStateException();
				}
				return calcAss(lastIdx, idxList);
			}

			@Override
			public int[] getIdxArray(int[] idxArray)
					throws IllegalStateException, IllegalArgumentException {
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
				if (idx == 0) {
					throw new NoSuchElementException();
				}
				idx--;
				lastIdx = idx;
				return array[idx];
			}
			
			@Override
			public void remove() throws UnsupportedOperationException {
				throw new UnsupportedOperationException();
			}

			@Override
			public void set(int entryVal) throws IllegalStateException,
					UnsupportedOperationException {
				if (lastIdx < 0) {
					throw new IllegalStateException();
				}
				array[lastIdx] = entryVal;
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

			private int idx = 0;
			private int lastIdx = -1;
		}

	}
	

	/**
	 * Private copy constructor, used by <code>deepCopy()</code>.
	 * @param table The table to be copied.
	 */
	private ImmutableArrayIntTable(ImmutableArrayIntTable table) {
		canonicalOrder = table.canonicalOrder;
		dims = table.dims;
		idxMultipliers = table.idxMultipliers;
		domains = table.domains;
		array = table.array;
		hashCode = table.hashCode;
	}
	
	
	/**
	 * Private constructor, to be used by <code>Builder</code>.
	 * @param builder The builder.
	 */
	private ImmutableArrayIntTable(Builder builder) {
		canonicalOrder = builder.canonicalOrder;
		dims = builder.dims;
		idxMultipliers = builder.idxMultipliers;
		domains = builder.domains;
		array = builder.array;
	}

	
	/**
	 * Private constructor for use with <code>copyOf</code>.
	 * @param table
	 */
	private ImmutableArrayIntTable(ArrayIntTable table) {
		canonicalOrder = table.getCanonicalOrder();
		dims = table.getDims();
		domains = table.getDomains();
		
		ImmutableList.Builder<Integer> multBuilder = ImmutableList.builder();
		int mult = 1;
		for (Integer dim : dims.reverse()) {
			multBuilder.add(mult);
			mult *= dim;
		}

		idxMultipliers = multBuilder.build().reverse();
		array = table.copyArray();
	}

	public static ImmutableArrayIntTable copyOf(ArrayIntTable table) {
		return new ImmutableArrayIntTable(table);
	}
	
	@Override
	public Object deepCopy() {
		return new ImmutableArrayIntTable(this);
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
		if (table instanceof ImmutableArrayIntTable) {
			ImmutableArrayIntTable immTable = (ImmutableArrayIntTable) table;
			if (hashCode != null && immTable.hashCode != null && !hashCode.equals(immTable.hashCode)) {
				return false;				
			}
			return Arrays.equals(array, immTable.array);
		}
		if (table instanceof ArrayIntTable) {
			return ((ArrayIntTable) table).equalsArray(array);
		}
		IntTableIterator iter1 = iterator();
		IntTableIterator iter2 = iterator();
		while (iter1.hasNext()) {
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
			hashCode = super.hashCode();
		}
		return hashCode;
	}

	@Override
	public ImmutableList<Integer> getCanonicalOrder() {
		return canonicalOrder;
	}

	@Override
	public int getEntry(Assignment a) throws IllegalArgumentException {
//		for (int var : canonicalOrder) {
//			if (!fixedAss.isAssigned(var)) {
//				fullAss.assign(var, a.getAssignment(var));				
//			}
//		}
//		return array[calcIdx(fullAss)];
		return array[calcIdx(a)];
	}

	@Override
	public int getEntry(int... idxs) throws IllegalArgumentException {
//		if (idxs.length != canonicalOrder.size()) {
//			throw new IllegalArgumentException("Number of indices=" + idxs.length + " does not match number of dimensions " + canonicalOrder.size());
//		}
//		for (int i = 0; i < canonicalOrder.size(); i++) {
//			if (!domains.get(i).contains(idxs[i])) {
//				throw new IllegalArgumentException("Index = " + idxs[i] + " for var=" + canonicalOrder.get(i) + " is not in domain " + domains.get(i));
//			}
//			fullAss.assign(canonicalOrder.get(i), idxs[i]);
//		}
//		return array[calcIdx(fullAss)];
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
	public ImmutableIntTableIterator iterator() {
		return new Iterator();
	}

	@Override
	public int size() {
		return array.length;
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

	
	/**
	 * Calculates the index into the array associated with an assignment.
	 * @param ass
	 * @return
	 */
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
	
	/**
	 * The canonical order of the table.
	 */
	private final ImmutableList<Integer> canonicalOrder;
	/**
	 * The dimensional sizes of the table, in the canonical order.
	 */
	private final ImmutableList<Integer> dims;
	/**
	 * The domains of the index dimensions of the table, in canonical order.
	 */
	private final ImmutableList<DomainSet> domains;  
	/**
	 * The coefficients used when multiplying with indices in the canonical order to get the arary index. 
	 */
	private final ImmutableList<Integer> idxMultipliers;
	/**
	 * The row-major-addressed (in canonical order) array storing the entries. 
	 */
	private final int [] array;
	/**
	 * Cached hash code with lazy initialization.
	 */
	private Integer hashCode;
	
	private class Iterator extends ImmutableIntTableIterator {		
		@Override
		public boolean hasNext() {
			return idx < array.length;
		}

		@Override
		public Integer next() throws NoSuchElementException {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
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

		private int idx;
		private int lastIdx = -1;
	}	
}
