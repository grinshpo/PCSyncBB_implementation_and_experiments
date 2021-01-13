package ext.sim.tools.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import bgu.dcr.az.api.DeepCopyable;
import bgu.dcr.az.api.prob.ImmutableProblem;
import bgu.dcr.az.api.tools.Assignment;

/**
 * Immutable table of ints for generalized domains by wrapping an <code>ImmutableArrayIntTable</code> 
 * via translation between the general external domain and the [0..size-1] domains of the 
 * <code>ImmutableArrayIntTable</code>.
 * 
 * @author Steven
 *
 */
public class ImmutableGenArrayIntTable extends ImmutableIntTable implements DeepCopyable {
	public static class Builder {
		public Builder(ImmutableProblem prob, int ... vars) {
			tableBuilder = new ImmutableArrayIntTable.Builder(prob, vars);
			transVals = new int[vars.length];
			List<ImmutableBiMap<Integer, Integer>> tempBiMaps = new ArrayList<>(vars.length);
			ImmutableList.Builder<DomainSet> domainsBuilder = ImmutableList.builder();
			for (int i = 0; i < vars.length; i++) {
				Set<Integer> domain = prob.getDomainOf(vars[i]);
				boolean mappingNotNeeded = true;
				for (int val : domain) {
					if (val < 0 || val >= domain.size()) {
						mappingNotNeeded = false;
						break;
					}
				}
				if (mappingNotNeeded) {
					tempBiMaps.add(null);
					domainsBuilder.add(RangeDomainSet.createRangeDomainSet(0, domain.size() - 1));
				} else {
					ImmutableBiMap.Builder<Integer, Integer> biMapBuilder = new ImmutableBiMap.Builder<>();
					int arrVal = 0;
					for (int val : domain) {
						biMapBuilder.put(val, arrVal);
						arrVal++;
					}
					tempBiMaps.add(biMapBuilder.build());
					domainsBuilder.add(new GenDomainSet(ImmutableSet.copyOf(domain)));
				}
			}
			biMaps = Collections.unmodifiableList(tempBiMaps);
			domains = domainsBuilder.build();
		}
		
		public Builder(ImmutableProblem prob, int [] vars, int ... vars2) {
			tableBuilder = new ImmutableArrayIntTable.Builder(prob, vars, vars2);
			int numVars = vars.length + (vars2 == null ? 0 : vars2.length);
			transVals = new int[numVars];
			List<ImmutableBiMap<Integer, Integer>> tempBiMaps = new ArrayList<>(vars.length);
			ImmutableList.Builder<DomainSet> domainsBuilder = ImmutableList.builder();
			for (int i = 0; i < numVars; i++) {
				int var = i < vars.length ? vars[i] : vars2[i - vars.length];
				Set<Integer> domain = prob.getDomainOf(var);
				boolean mappingNotNeeded = true;
				for (int val : domain) {
					if (val < 0 || val >= domain.size()) {
						mappingNotNeeded = false;
						break;
					}
				}
				if (mappingNotNeeded) {
					tempBiMaps.add(null);
					domainsBuilder.add(RangeDomainSet.createRangeDomainSet(0, domain.size() - 1));
				} else {
					ImmutableBiMap.Builder<Integer, Integer> biMapBuilder = new ImmutableBiMap.Builder<>();
					int arrVal = 0;
					for (int val : domain) {
						biMapBuilder.put(val, arrVal);
						arrVal++;
					}
					tempBiMaps.add(biMapBuilder.build());
					domainsBuilder.add(new GenDomainSet(ImmutableSet.copyOf(domain)));
				}
			}
			biMaps = Collections.unmodifiableList(tempBiMaps);
			domains = domainsBuilder.build();
		}
		
		
		public Builder(ImmutableProblem prob, List<Integer> vars) {
			tableBuilder = new ImmutableArrayIntTable.Builder(prob, vars);
			transVals = new int[vars.size()];
			List<ImmutableBiMap<Integer, Integer>> tempBiMaps = new ArrayList<>(vars.size());
			ImmutableList.Builder<DomainSet> domainsBuilder = ImmutableList.builder();
			for (int i = 0; i < vars.size(); i++) {
				Set<Integer> domain = prob.getDomainOf(vars.get(i));
				boolean mappingNotNeeded = true;
				for (int val : domain) {
					if (val < 0 || val >= domain.size()) {
						mappingNotNeeded = false;
						break;
					}
				}
				if (mappingNotNeeded) {
					tempBiMaps.add(null);
					domainsBuilder.add(RangeDomainSet.createRangeDomainSet(0, domain.size() - 1));
				} else {
					ImmutableBiMap.Builder<Integer, Integer> biMapBuilder = new ImmutableBiMap.Builder<>();
					int arrVal = 0;
					for (int val : domain) {
						biMapBuilder.put(val, arrVal);
						arrVal++;
					}
					tempBiMaps.add(biMapBuilder.build());
					domainsBuilder.add(new GenDomainSet(ImmutableSet.copyOf(domain)));
				}
			}
			biMaps = Collections.unmodifiableList(tempBiMaps);
			domains = domainsBuilder.build();
		}
		

		public Builder(List<Integer> vars, List<Set<Integer>> domains) {
			List<Integer> domainSizes = new ArrayList<>(domains.size());
			transVals = new int[vars.size()];
			for (int i = 0; i < domains.size(); i++) {
				domainSizes.add(domains.get(i).size());
			}
			tableBuilder = new ImmutableArrayIntTable.Builder(vars, domainSizes);
			List<ImmutableBiMap<Integer, Integer>> tempBiMaps = new ArrayList<>(vars.size());
			ImmutableList.Builder<DomainSet> domainsBuilder = ImmutableList.builder();
			for (int i = 0; i < vars.size(); i++) {
				Set<Integer> domain = domains.get(i);
				domainSizes.add(domain.size());
				boolean mappingNotNeeded = true;
				for (int val : domain) {
					if (val < 0 || val >= domain.size()) {
						mappingNotNeeded = false;
						break;
					}
				}
				if (mappingNotNeeded) {
					tempBiMaps.add(null);
					domainsBuilder.add(RangeDomainSet.createRangeDomainSet(0, domain.size() - 1));
				} else {
					ImmutableBiMap.Builder<Integer, Integer> biMapBuilder = new ImmutableBiMap.Builder<>();
					int arrVal = 0;
					for (int val : domain) {
						biMapBuilder.put(val, arrVal);
						arrVal++;
					}
					tempBiMaps.add(biMapBuilder.build());
					domainsBuilder.add(new GenDomainSet(ImmutableSet.copyOf(domain)));
				}
			}
			biMaps = Collections.unmodifiableList(tempBiMaps);
			this.domains = domainsBuilder.build();
		}
		
		public ImmutableGenArrayIntTable build() throws IllegalStateException {
			if (this.tableBuilder == null) {
				throw new IllegalStateException("build already called.");
			}
			ImmutableGenArrayIntTable table = new ImmutableGenArrayIntTable(this);
			this.tableBuilder = null;
			return table;
		}
		
		public IntTableIterator iterator() {
			return new BuilderIterator();
		}
		
		public void setEntry(int entryVal, Assignment a) {
			int numDims = tableBuilder.getCanonicalOrder().size();
			for (int i = 0; i < numDims; i++) {
				int var = tableBuilder.getCanonicalOrder().get(i);
				int extVal = a.getAssignment(var);
				transVals[i] = getIntIdx(i, extVal);
			}
			tableBuilder.setEntry(entryVal, transVals);
		}

		public void setEntry(int entryVal, int... vals) {
			for (int i = 0; i < transVals.length; i++) {
				transVals[i] = getIntIdx(i, vals[i]);
			}
			tableBuilder.setEntry(entryVal, transVals);
		}
		
		private int getIntIdx(int canonicalIdx, int extIdx) {
			return biMaps.get(canonicalIdx) != null ? biMaps.get(canonicalIdx).get(extIdx) : extIdx;
		}

		private int getExtIdx(int canonicalIdx, int intIdx) {
			return biMaps.get(canonicalIdx) != null ? biMaps.get(canonicalIdx).inverse().get(intIdx) : intIdx;
		}

		private ImmutableArrayIntTable.Builder tableBuilder;
		private final ImmutableList<DomainSet> domains;
		private final List<ImmutableBiMap<Integer, Integer>> biMaps;
		private final int [] transVals;
		
		private class BuilderIterator implements IntTableIterator {

			@Override
			public boolean hasNext() {
				return iter.hasNext();
			}

			@Override
			public Integer next() {
				return iter.next();
			}

			@Override
			public Assignment getIdxAss() throws IllegalStateException {
				return getIdxAss(new Assignment());
			}

			@Override
			public Assignment getIdxAss(Assignment idxAss)
					throws IllegalStateException {
				if (transAss == null) {
					transAss = new Assignment();
				}
				iter.getIdxAss(transAss);
				for (Map.Entry<Integer, Integer> entry : transAss.getAssignments()) {
					int dim = tableBuilder.getCanonicalOrder().indexOf(entry.getKey());
					idxAss.assign(entry.getKey(), getExtIdx(dim, entry.getValue()));
				}
				return idxAss;
			}

			@Override
			public List<Integer> getIdxList() throws IllegalStateException {
				return getIdxList(new ArrayList<Integer>(domains.size()));
			}

			@Override
			public List<Integer> getIdxList(List<Integer> idxList)
					throws IllegalStateException {
				iter.getIdxList(idxList);
				for (int i = 0; i < idxList.size(); i++) {
					int internalIdx = idxList.get(i);
					int externalIdx = getExtIdx(i, internalIdx);
					idxList.set(i, externalIdx);
				}
				return idxList;			
			}

			@Override
			public int[] getIdxArray(int[] idxArray) throws IllegalStateException, IllegalArgumentException {
				iter.getIdxArray(idxArray);
				for (int i = 0; i < idxArray.length; i++) {
					idxArray[i] = getExtIdx(i, idxArray[i]);
				}
				return idxArray;
			}

			@Override
			public boolean hasPrev() {
				return iter.hasPrev();
			}

			@Override
			public int prev() throws NoSuchElementException {
				return iter.prev();
			}
			
			@Override
			public void remove() throws UnsupportedOperationException {
				throw new UnsupportedOperationException();
			}

			@Override
			public void set(int entryVal) throws IllegalStateException,
					UnsupportedOperationException {
				iter.set(entryVal);
			}
			
			private IntTableIterator iter = tableBuilder.iterator();
			private Assignment transAss;
		}
	}
	
	/**
	 * Private constructor for use with slicing.
	 * @param table
	 * @param biMaps
	 * @param domains
	 */
	private ImmutableGenArrayIntTable(ImmutableArrayIntTable table, List<ImmutableBiMap<Integer, Integer>> biMaps, ImmutableList<DomainSet> domains) {
		this.table = table;
		transVals = new int[table.getCanonicalOrder().size()];
		this.biMaps = Collections.unmodifiableList(biMaps);
		this.domains = domains;
	}
	
	/**
	 * Private constructor for use with <code>copyOf</code>.
	 * @param table The table to be copied.
	 */
	private ImmutableGenArrayIntTable(ArrayIntTable table) {
		this.table = ImmutableArrayIntTable.copyOf(table);
		this.domains = table.getDomains();
		transVals = new int[table.getCanonicalOrder().size()];
		List<ImmutableBiMap<Integer, Integer>> tempBiMaps = new ArrayList<>(table.getCanonicalOrder().size());
		for (int i = 0; i < table.getCanonicalOrder().size(); i++) {
			tempBiMaps.add(null);
		}
		biMaps = Collections.unmodifiableList(tempBiMaps);
	}
	
	private ImmutableGenArrayIntTable(GenArrayIntTable table) {
		this.table = table.immutableTableCopy();
		domains = table.getDomains();
		transVals = new int[table.getCanonicalOrder().size()];
		biMaps = table.getBiMaps();
	}
	
	/**
	 * Private copy constructor for <code>deepCopy</code>.
	 * @param table
	 */
	private ImmutableGenArrayIntTable(ImmutableGenArrayIntTable table) {
		transVals = new int[table.getCanonicalOrder().size()];
		biMaps = table.biMaps;
		domains = table.domains;
		this.table = table.table;
	}
	
	/**
	 * Private constructor for use by <code>Builder</code>.
	 * @param builder
	 */
	private ImmutableGenArrayIntTable(Builder builder) {
		table = builder.tableBuilder.build();
		domains = builder.domains;
		biMaps = builder.biMaps;
		transVals = new int[table.getCanonicalOrder().size()];
	}
	
	public static ImmutableGenArrayIntTable copyOf(ArrayIntTable table) {
		return new ImmutableGenArrayIntTable(table);
	}
	
	public static ImmutableGenArrayIntTable copyOf(GenArrayIntTable table) {
		return new ImmutableGenArrayIntTable(table);
	}
	
	@Override
	public Object deepCopy() {
		return new ImmutableGenArrayIntTable(this);
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || !(o instanceof IntTable)) {
			return false;
		}
		IntTable intTable = (IntTable) o;
		if (!getCanonicalOrder().equals(intTable.getCanonicalOrder()) || !domains.equals(intTable.getDomains())) {
			return false;
		}
		if (intTable instanceof ImmutableGenArrayIntTable) {
			ImmutableGenArrayIntTable immTable = (ImmutableGenArrayIntTable) intTable;
			if (hashCode != null && immTable.hashCode != null && hashCode != immTable.hashCode) {
				return false;
			}
			return table.equals(immTable.table);
		}
		
		IntTableIterator iter1 = iterator();
		IntTableIterator iter2 = intTable.iterator();
		int hash = 1;	// we're going to take the opportunity to compute the hashcode, if necessary
		while (iter1.hasNext()) {
			int val1 = iter1.next();
			int val2 = iter2.next();
			if (val1 != val2) {
				return false;
			}
			if (hashCode == null) {
				hash = 31 * hash + val1;
			}
		}
		assert hashCode == null ? true : hash == hashCode.intValue();
		hashCode = hash;
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
		return table.getCanonicalOrder();
	}

	@Override
	public int getEntry(Assignment a) throws IllegalArgumentException {
		int numDims = table.getCanonicalOrder().size();
		for (int i = 0; i < numDims; i++) {
			int var = table.getCanonicalOrder().get(i);
			int extVal = a.getAssignment(var);
			transVals[i] = getIntIdx(i, extVal);
		}
		return table.getEntry(transVals);
	}

	@Override
	public int getEntry(int... vals) throws IllegalArgumentException {
		if (vals.length != transVals.length) {
			throw new IllegalArgumentException("Number of arguments = " + vals.length + " does not match dimensionality of table = " + transVals.length);
		}
		for (int i = 0; i < transVals.length; i++) {
			transVals[i] = getIntIdx(i, vals[i]);
		}
		return table.getEntry(transVals);
	}

	@Override
	public ImmutableList<Integer> getDims() {
		return table.getDims();
	}

	@Override
	public ImmutableList<DomainSet> getDomains() {
		return domains;
	}


	@Override
	public ImmutableIntTableIterator iterator() {
		return new WrappedIntTableIterator();
	}
	
	@Override
	public int size() {
		return table.size();
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(getCanonicalOrder().toString()).append("\n");
		List<Integer> idxs = new ArrayList<>();
		for (IntTableIterator iter = iterator(); iter.hasNext(); ) {
			int val = iter.next();
			sb.append(iter.getIdxList(idxs)).append("=").append(val);
			if (iter.hasNext()) {
				sb.append("\n");
			}
		}
		return sb.toString();
	}
	
	private int getExtIdx(int canonicalIdx, int intIdx) {
		return biMaps.get(canonicalIdx) != null ? biMaps.get(canonicalIdx).inverse().get(intIdx) : intIdx;
	}
	
	private int getIntIdx(int canonicalIdx, int extIdx) {
		return biMaps.get(canonicalIdx) != null ? biMaps.get(canonicalIdx).get(extIdx) : extIdx;
	}

	/**
	 * The table that is actually storing the data, using [0..domainSize] values.
	 */
	private final ImmutableArrayIntTable table;
	/**
	 * The general domains of dimensions in this table.
	 */
	private final ImmutableList<DomainSet> domains;

	/**
	 * List of BiMaps from external domains to the internal domains [0..domain size - 1] used by 
	 * <code>table</code>, or <code>null</code> if no BiMap is used for a variable.  
	 * These are listed in the canonical order.
	 */
	private final List<ImmutableBiMap<Integer, Integer>> biMaps;
	/**
	 * Array of translated values in the canonical order, used for lookups.  We keep it as a member
	 * instance so we don't constantly have to reallocate it.
	 */
	private final int [] transVals;
	/**
	 * Cached hash code, initialized lazily.
	 */
	private Integer hashCode;
	
	private class WrappedIntTableIterator extends ImmutableIntTableIterator {

		@Override
		public boolean hasNext() {
			return iter.hasNext();
		}

		@Override
		public Integer next() {
			return iter.next();
		}

		@Override
		public Assignment getIdxAss() throws IllegalStateException {
			return getIdxAss(new Assignment());
		}
		
		@Override
		public Assignment getIdxAss(Assignment a) throws IllegalStateException {
			if (arrIdxAss == null) {
				arrIdxAss = new Assignment();
			}
			iter.getIdxAss(arrIdxAss);
			for (Map.Entry<Integer, Integer> entry : arrIdxAss.getAssignments()) {
				int dim = table.getCanonicalOrder().indexOf(entry.getKey());
				a.assign(entry.getKey(), getExtIdx(dim, entry.getValue()));
			}
			return a;
		}
		
		@Override
		public List<Integer> getIdxList() throws IllegalStateException {
			List<Integer> idxList = iter.getIdxList();
			for (int i = 0; i < idxList.size(); i++) {
				int internalIdx = idxList.get(i);
				int externalIdx = getExtIdx(i, internalIdx);
				idxList.set(i, externalIdx);
			}
			return idxList;
		}

		@Override
		public List<Integer> getIdxList(List<Integer> idxList)
				throws IllegalStateException {
			iter.getIdxList(idxList);
			for (int i = 0; i < idxList.size(); i++) {
				int internalIdx = idxList.get(i);
				int externalIdx = getExtIdx(i, internalIdx);
				idxList.set(i, externalIdx);
			}
			return idxList;			
		}

		@Override
		public int[] getIdxArray(int[] idxArray) throws IllegalStateException,
				IllegalArgumentException {
			iter.getIdxArray(idxArray);
			for (int i = 0; i < idxArray.length; i++) {
				idxArray[i] = getExtIdx(i, idxArray[i]);
			}
			return idxArray;
		}

		@Override
		public boolean hasPrev() {
			return iter.hasPrev();
		}

		@Override
		public int prev() throws NoSuchElementException {
			return iter.prev();
		}
		
		@Override
		public void remove() throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}
		
		private IntTableIterator iter = table.iterator();
		private Assignment arrIdxAss;
	}

}
