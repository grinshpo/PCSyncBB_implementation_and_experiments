package ext.sim.tools.table;

import java.util.Map.Entry;

import bgu.dcr.az.api.tools.Assignment;

public final class ImmutableAssignment extends Assignment {

	public static class Builder {
		private Builder() { }
		
		public ImmutableAssignment build() {
			if (ass.getNumberOfAssignedVariables() == 0) {
				return ImmutableAssignment.EMPTY_ASS;
			} 
			return new ImmutableAssignment(ass);
		}
		
		public Builder assign(int var, int val) {
			ass.assign(var, val);
			return this;
		}
		
		public Builder assignAll(Assignment a) {
			for (Entry<Integer, Integer> entry : a.getAssignments()) {
				ass.assign(entry.getKey(), entry.getValue());
			}
			return this;
		}
		
		public Builder unassign(int var) {
			ass.unassign(var);
			return this;
		}
		
		private Assignment ass = new Assignment();
	}
	
	public static final ImmutableAssignment EMPTY_ASS = new ImmutableAssignment();

	private ImmutableAssignment() {
		// the empty immutable assignment.  A singleton.
	}
	
	private ImmutableAssignment(Assignment a) {
		for (Entry<Integer, Integer> entry : a.getAssignments()) {
			super.assign(entry.getKey(), entry.getValue());
		}
	}
	
	public static Builder builder() {
		return new Builder();
	}

	public static ImmutableAssignment copyOf(Assignment a) {
		if (a.getNumberOfAssignedVariables() == 0) {
			return ImmutableAssignment.EMPTY_ASS;
		}
		return new ImmutableAssignment(a);
	}
	
	@Override
	public ImmutableAssignment deepCopy() {
		return this;
	}

	/**
	 * Assignment is not allowed.  Throws a <code>UnsupportedOperationException</code>. 
	 */
	@Deprecated
	@Override
	public final void assign(int var, int val) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("assign not allowed on ImmutableAssignment.");
	}
	
	/**
	 * Unassign is not allowed.  Throws a <code>UnsupportedOperationException</code>.
	 */
	@Deprecated
	@Override
	public final void unassign(int var) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("unassign not allowed on ImmutableAssignment.");
	}
	
	private static final long serialVersionUID = 1L;

}
