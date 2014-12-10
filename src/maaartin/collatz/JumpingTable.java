package maaartin.collatz;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Formatter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor abstract class JumpingTable {
	static class SmallJumpingTable extends JumpingTable {
		SmallJumpingTable(int evenSteps) {
			super(evenSteps, (1 << evenSteps) - 1);
			checkArgument(2 <= evenSteps && evenSteps <= 17);
			data = new int[mask() + 1];
			for (int index=0; index<=mask(); ++index) {
				int n = index;
				int oddSteps = 0;
				for (int i=0; i<evenSteps; ++i) {
					if ((n&1) == 0) {
						n >>>= 1;
					} else {
						n = n + (n>>>1) + 1;
						++oddSteps;
					}
				}
				final int combined = combine(n, oddSteps);
				data[index] = combined;
				assert addend(index) == n;
				assert oddSteps(index) == oddSteps;
				assert oddStepsToMultiplier(oddSteps) > 0; // just checking the bounds
			}
		}

		@Override int oddSteps(int index) {
			return data[index] >>> SHIFT;
		}

		@Override int addend(int index) {
			return data[index] & LOW_MASK;
		}

		private int combine(int addend, int oddSteps) {
			return (oddSteps << SHIFT) + (addend & LOW_MASK);
		}

		private static final int SHIFT = 27;
		private static final int LOW_MASK = (1 << SHIFT) - 1;

		private final int[] data;

	}

	static class BigJumpingTable extends JumpingTable {
		BigJumpingTable(int evenSteps) {
			super(evenSteps, (1 << evenSteps) - 1);
			checkArgument(2 <= evenSteps && evenSteps <= 29);
			data = new int[2*mask() + 2];
			for (int index=0; index<=mask(); ++index) {
				int n = index;
				int oddSteps = 0;
				for (int i=0; i<evenSteps; ++i) {
					if ((n&1) == 0) {
						n >>>= 1;
					} else {
						n = n + (n>>>1) + 1;
						++oddSteps;
					}
				}
				data[2*index+0] = oddSteps;
				data[2*index+1] = n;
				assert addend(index) == n;
				assert oddSteps(index) == oddSteps;
				assert oddStepsToMultiplier(oddSteps) > 0; // just checking the bounds
			}
		}

		@Override int oddSteps(int index) {
			return data[2*index+0];
		}

		@Override int addend(int index) {
			return data[2*index+1];
		}

		private final int[] data;
	}

	static JumpingTable newTable(int evenSteps) {
		return evenSteps<=17 ? new SmallJumpingTable(evenSteps) : new BigJumpingTable(evenSteps);
	}

	@SuppressWarnings("boxing")
	@Override public String toString() {
		if (!verboseToString) return getClass().getSimpleName() + "-" + evenSteps;
		final Formatter result = new Formatter(new StringBuilder());
		result.format("%s-%d(", getClass().getSimpleName(), evenSteps());
		for (int i=0; i<=mask(); ++i) {
			final String sep = (i&7) == 0 ? "\n  " : "  ";
			result.format("%s%04X: %X %07X", sep, i, oddSteps(i), addend(i));
		}
		result.format("\n)");
		return result.toString();
	}

	abstract int addend(int index);
	abstract int oddSteps(int index);

	static long oddStepsToMultiplier(int oddSteps) {
		return POWERS_OF_THREE[oddSteps];
	}

	private static final long[] POWERS_OF_THREE;
	static {
		int length = 1;
		for (long x=1; x<=Long.MAX_VALUE/3; x*=3) ++length;
		POWERS_OF_THREE = new long[length];
		int index = 0;
		for (long x=1; index<POWERS_OF_THREE.length; x*=3) POWERS_OF_THREE[index++] = x;
	}

	static boolean verboseToString = false;

	@Getter private final int evenSteps;
	@Getter private final int mask;
}
