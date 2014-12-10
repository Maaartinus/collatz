package maaartin.collatz;

import static com.google.common.base.Preconditions.checkArgument;

/** A simple implementation containing some basic optimizations. */
class SimpleCollatz extends Collatz {
	@Override public long limit() {
		return 8528817511L;
	}

	@Override public int delay(long n) {
		checkArgument(n > 0 && n < limit());
		int result = 0;
		while (true) {
			// Remove all trailing zeros at once.
			final int ntz = Long.numberOfTrailingZeros(n);
			result += ntz;
			n  = (n >>> ntz);
			if (n==1) break;

			assert (n&1) != 0 & n > 0;
			n += (n>>>1) + 1;
			result += 2;
		}
		return result;
	}

	@Override public long maximize(long min, long max) {
		checkArgument(0 < min && min <= max && max < limit());
		// For every number x below max/2, delay(2*x) will dominate delay(x).
		min = Math.max(min, max>>1);
		long result = -1;
		long longest = -1;
		for (long i=min; i<=max; ++i) {
			final long time = delay(i);
			if (time > longest) {
				result = i;
				longest = time;
			}
		}
		return result;
	}
}
