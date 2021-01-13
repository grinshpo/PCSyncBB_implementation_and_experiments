package ext.sim.tools.table;

import java.util.Collection;

import com.google.common.collect.UnmodifiableIterator;

public class RangeDomainSet extends DomainSet {
	public static RangeDomainSet createRangeDomainSet(int min, int max) {
		RangeDomainSet set = checkCache(min, max);
		if (set == null) {
			set = new RangeDomainSet(min, max);
		}
		return set;
	}
	
	private RangeDomainSet(int min, int max)  {
		if (min > max) {
			this.min = 0;
			this.max = -1;
		} else {
			this.min = min;
			this.max = max;
		}
	}

	@Override
	public int size() {
		return max - min + 1;
	}

	@Override
	public boolean isEmpty() {
		return max < min;
	}

	@Override
	public boolean contains(Object o) {
		if (!(o instanceof Integer)) {
			return false;
		}
		int i = (int) o;
		return i >= min && i <= max;
	}

	@Override
	public UnmodifiableIterator<Integer> iterator() {
		return new UnmodifiableIterator<Integer>() {
			@Override
			public boolean hasNext() {
				return i <= max;
			}

			@Override
			public Integer next() {
				return i++;
			}

			private int i = min;
		};
	}

	@Override
	public Object[] toArray() {
		Integer [] array = new Integer[size()];
		for (int i = min; i<= max; i++) {
			array[i - min] = i;
		}
		return array;
	}

	@Override
	public <T> T[] toArray(T[] a) {
		if (!(a instanceof Integer [])) {
			throw new IllegalArgumentException("DomainSet only stores integers.");
		}
		Integer [] arr;
		if (a.length >= size()) {
			arr = (Integer []) a;
		} else {
			arr = new Integer[size()];
		}
		for (int i = min; i <= max; i++) {
			arr[i - min] = i;
		}
		
		return a;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		for (Object o : c) {
			if (!(o instanceof Integer)) {
				return false;
			}
			int i = (int) o;
			if (i < min || i > max) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null || o.getClass() != getClass()) {
			return false;
		}
		RangeDomainSet set = (RangeDomainSet) o;
		return min == set.min && max == set.max; 
	}
	
	@Override
	public int hashCode() {
		return min ^ ((max << 16) | (max >>> 16)); 
	}
	
	@Override
	public String toString() {
		return "[" + min + ".." + max + "]";
	}
	
	private static RangeDomainSet checkCache(int min, int max) {
		// first check if this is the empty set
		if (max < min) {
			return EMPTY_SET;
		}
		// next check if it's outside the cache bounds
		if (min < MIN_CACHED_MIN || min > MAX_CACHED_MIN || (max - min) < MIN_CACHED_MAX_RNG || (max - min) > MAX_CACHED_MAX_RNG) {
			return null;
		}
		int i = min - MIN_CACHED_MIN;
		int j = (max - min) - MIN_CACHED_MAX_RNG;
		RangeDomainSet set = CACHE[i][j];
		if (set == null) {
			// it's not already in the cache, so add it
			set = new RangeDomainSet(min, max);
			CACHE[i][j] = set;
		}
		return set;
	}

	private final int min;
	private final int max;
	
	private static final int MIN_CACHED_MIN = 0;
	private static final int MAX_CACHED_MIN = 10;
	private static final int MIN_CACHED_MAX_RNG = 0;
	private static final int MAX_CACHED_MAX_RNG = 20;
	private static final RangeDomainSet [][] CACHE = new RangeDomainSet[MAX_CACHED_MIN - MIN_CACHED_MIN + 1][MAX_CACHED_MAX_RNG - MIN_CACHED_MAX_RNG + 1];
	private static final RangeDomainSet EMPTY_SET = new RangeDomainSet(0, -1);
}
