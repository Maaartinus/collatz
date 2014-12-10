package maaartin.collatz;

import static com.google.common.base.Preconditions.checkArgument;

/** An implementation using some strange bit-fiddling. */
class FiddlingCollatz extends Collatz {
	@Override public long limit() {
		return 8528817511L;
	}

	@SuppressWarnings("boxing")
	@Override public int delay(long n) {
		checkArgument(n > 0 && n < limit(), "", this, n);
		int result = 0;
		while (n > 1 << 15) {
			long mask;
			final long n0 = n;

			mask = (BITS_0 << n0) >> 63;
			n = (n>>>1) + ((n+1) & mask);
			result -= mask;

			mask = (BITS_1 << n0) >> 63;
			n = (n>>>1) + ((n+1) & mask);
			result -= mask;

			mask = (BITS_2 << n0) >> 63;
			n = (n>>>1) + ((n+1) & mask);
			result -= mask;

			final long n1 = n;
			mask = (BITS_3 << n0) >> 63;
			n = (n>>>1) + ((n+1) & mask);
			result -= mask;

			mask = (BITS_4 << n0) >> 63;
			n = (n>>>1) + ((n+1) & mask);
			result -= mask;

			mask = (BITS_5 << n0) >> 63;
			n = (n>>>1) + ((n+1) & mask);
			result -= mask;

			final long n2 = n;
			mask = (BITS_3 << n1) >> 63;
			n = (n>>>1) + ((n+1) & mask);
			result -= mask;

			mask = (BITS_4 << n1) >> 63;
			n = (n>>>1) + ((n+1) & mask);
			result -= mask;

			mask = (BITS_5 << n1) >> 63;
			n = (n>>>1) + ((n+1) & mask);
			result -= mask;

			final long n3 = n;
			mask = (BITS_3 << n2) >> 63;
			n = (n>>>1) + ((n+1) & mask);
			result -= mask;

			mask = (BITS_4 << n2) >> 63;
			n = (n>>>1) + ((n+1) & mask);
			result -= mask;

			mask = (BITS_5 << n2) >> 63;
			n = (n>>>1) + ((n+1) & mask);
			result -= mask;

			mask = (BITS_3 << n3) >> 63;
			n = (n>>>1) + ((n+1) & mask);
			result -= mask;

			mask = (BITS_4 << n3) >> 63;
			n = (n>>>1) + ((n+1) & mask);
			result -= mask;

			mask = (BITS_5 << n3) >> 63;
			n = (n>>>1) + ((n+1) & mask);
			result -= mask;

			result += 15;
		}
		return result + finisherCollatz.delay(n);
	}

	private final static long BITS_0;
	private final static long BITS_1;
	private final static long BITS_2;
	private final static long BITS_3;
	private final static long BITS_4;
	private final static long BITS_5;
	static {
		final long[] bits = new long[6];
		for (int i=0; i<64; ++i) {
			long n = i;
			for (int j=0; j<bits.length; ++j) {
				bits[j] |= (n & 1) << (63-i);
				n = (n >>> 1) + ((n&1) != 0 ? n+1 : 0);
			}
		}
		BITS_0 = bits[0];
		BITS_1 = bits[1];
		BITS_2 = bits[2];
		BITS_3 = bits[3];
		BITS_4 = bits[4];
		BITS_5 = bits[5];
		//		Dout.f("[X]", bits);
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

	private static final Collatz finisherCollatz = new SimpleCollatz();
}
