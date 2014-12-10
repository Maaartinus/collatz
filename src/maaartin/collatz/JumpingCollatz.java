package maaartin.collatz;

import static com.google.common.base.Preconditions.checkArgument;

class JumpingCollatz extends Collatz {
	private static final class State {
		void update(long n, int delay) {
			if (delay<longest) return;
			if (delay==longest && n >= result) return;
			result = n;
			longest = delay;
		}

		long result = -1;
		int longest = -1;
	}

	JumpingCollatz(JumpingTable jumpingTable, CollatzTable collatzTable) {
		checkArgument(collatzTable.readLimit() >= 1L << jumpingTable.evenSteps());
		this.jumpingTable = jumpingTable;
		this.collatzTable = collatzTable;
	}

	@Override public long limit() {
		return 1_980976_057694_848447L + 1;
	}

	@Override public int delay(long start) {
		checkArgument(start > 0 && start < limit());
		return delayInternal(start, -1);
	}

	@Override public long maximize(long min, long max) {
		checkArgument(0 < min && min <= max && max < limit());
		if (min <= max>>1) return maximize(max);
		final State state = new State();
		final long roundedMin = min/9 * 9 + 9;
		final long roundedMax = max/9 * 9 - 1;
		if (roundedMin >= roundedMax) return maximizeSimply(state, min, max);

		for (long i=roundedMin; i<=roundedMax; i+=3) {
			// Numbers of the form 3*z + 2 can be skipped as they get produced in two steps from
			// numbers of the form 2*z + 1, respectively.
			// Let's see if 2*z + 1 or 4*z + 2 is in range.
			final boolean inRange = 2*i >= 3*min | 4*i <= 3*max - 8;
			update(state, i+0);
			update(state, i+1);
			if (!inRange) update(state, i+2);
		}

		maximizeSimply(state, min, Math.min(max, roundedMin-1));
		maximizeSimply(state, roundedMax+1, max);
		return state.result;
	}

	private long maximize(long max) {
		final long min = (max >> 1);
		final State state = new State();
		final long roundedMin = min/9 * 9 + 9;
		final long roundedMax = max/9 * 9 - 1;
		if (roundedMin >= roundedMax) return maximizeSimply(state, 1, max);
		// The order is unimportant even in case of ties as it gets handled in State#update.
		// Doing the significant part first seems to be more performant.
		maximizeSmart(state, roundedMin, roundedMax);
		maximizeSimply(state, min, Math.min(max, roundedMin-1));
		maximizeSimply(state, roundedMax+1, max);
		return state.result;
	}

	private long maximizeSimply(State state, long min, long max) {
		assert 1 <= min;
		for (long i=min; i<=max; ++i) {
			final int delay = delayInternal(i, state.longest);
			if (delay >= state.longest) state.update(i, delay);
		}
		return state.result;
	}

	private void maximizeSmart(State state, long min, long max) {
		assert min%9 == 0 && max%9 == 8;
		for (long i=max-8; i>=min; i-=9) {
			//		for (long i=min; i<=max; i+=9) {
			update(state, i+0);
			update(state, i+1);
			// skipping i+2 because of 6*z + 1 -> 18*z + 4 -> 9*z + 2
			update(state, i+3);
			// skipping i+4 because of 8*z + 3 -> 24*z + 10 -> 12*z + 5 -> 36*z + 16 -> 18*z + 8 -> 9*z + 4
			// skipping i+5 because of 6*z + 3 -> 18*z + 10 -> 9*z + 5
			update(state, i+6);
			update(state, i+7);
			// skipping i+8 because of 6*z + 5 -> 18*z + 16 -> 9*z + 8
		}
	}

	private void update(State state, long n0) {
		final int delay0 = delayOrZero(state, n0);
		if (delay0 >= state.longest) state.update(n0, delay0);
	}

	private int delayOrZero(State state, long n0) {
		// http://www.cecm.sfu.ca/organics/papers/lagarias/paper/html/node7.html#SECTION00025000000000000000
		// For example the trajectories of 8k + 4 and 8k + 5 coalesce after 3 steps...
		// 8*z + 5 -> 24*z + 16 -> 12*z + 8 -> 6*z + 4
		// 8*z + 4 -> 4*z + 2 -> 2*z + 1 -> 6*z + 4
		if ((n0&7) == 5) return 0;
		return delayInternal(n0, state.longest);
	}

	/**
	 * Return the {@link Collatz#delay}(x), if it's greater than {@code leastInteresting}.
	 * Otherwise, a smaller number may be returned.
	 */
	private int delayInternal(long x, int leastInteresting) {
		return x < LIMIT_FOR_LONG ? delayInternal64(x, leastInteresting) : delayInternal128(new MutableLong128(x), leastInteresting);
	}

	private int delayInternal64(long x, int leastInteresting) {
		int result = 0;
		final int mask = jumpingTable.mask();
		final int evenSteps = jumpingTable.evenSteps();
		final long limit = collatzTable.readLimit();
		while (x>=limit || x<0) { // treat as unsigned
			final int needed = leastInteresting - result;
			if (needed>0) {
				final long classRecord = Records.getClassRecord(needed);
				if (x<classRecord && x>0) return 0;
			}
			final int discriminant = (int) (x & mask);
			final int oddSteps = jumpingTable.oddSteps(discriminant);
			x = jumpingTable.addend(discriminant) + ((x >>> evenSteps) * JumpingTable.oddStepsToMultiplier(oddSteps));
			result += oddSteps + evenSteps;
		}
		final int ntz0 = Long.numberOfTrailingZeros(x);
		return result + ntz0 + collatzTable.get(x>>>ntz0);
	}

	private int delayInternal128(MutableLong128 x, int leastInteresting) {
		int result = 0;
		final int mask = jumpingTable.mask();
		final int evenSteps = jumpingTable.evenSteps();
		while (true) {
			if (x.fitsInLong()) {
				if (x.low() < LIMIT_FOR_LONG) break;
				final int needed = leastInteresting - result;
				if (needed>0) {
					final long lowerBound = Records.getLowerBoundForDelay(needed);
					if (x.low() < lowerBound) return 0;
				}
			}
			final int discriminant = (int) (x.low() & mask);
			final int oddSteps = jumpingTable.oddSteps(discriminant);
			final long multiplier = JumpingTable.oddStepsToMultiplier(oddSteps);

			result += oddSteps + evenSteps;

			x.shiftRight(evenSteps);
			x.multiply(multiplier);
			x.add(jumpingTable.addend(discriminant));
		}
		return result + delayInternal64(x.low(), leastInteresting-result);
	}

	private static final long LIMIT_FOR_LONG = 8528817511L;

	private final JumpingTable jumpingTable;
	private final CollatzTable collatzTable;
}
