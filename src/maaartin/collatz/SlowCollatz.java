package maaartin.collatz;

import static com.google.common.base.Preconditions.checkArgument;

/** The most straightforward and stupid implementation. */
class SlowCollatz extends Collatz {
	@Override public long limit() {
		return 1410123943;
	}

	@Override public int delay(long n) {
		checkArgument(n > 0 && n < limit());
		int result = 0;
		while (n>1) {
			if ((n&1) == 0) {
				n >>>= 1;
			} else {
				assert n <= Long.MAX_VALUE / 3 * 2;
				n = 3*n + 1;
			}
			++result;
		}
		return result;
	}

	@Override public long maximize(long min, long max) {
		checkArgument(0 < min && min <= max && max < limit());
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
