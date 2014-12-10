package maaartin.collatz;

import java.math.BigInteger;
import java.util.Random;

import com.google.caliper.BeforeExperiment;

import com.google.caliper.Benchmark;

import com.google.caliper.runner.CaliperMain;

import de.grajcar.dout.Dout;

class MutableLong128Benchmark {
	public static void main(String[] args) {
		Dout.a("STARTED");
		CaliperMain.main(MutableLong128Benchmark.class, args);
	}

	@BeforeExperiment public void setUp() {
		final Random random = new Random(0);
		for (int i=0; i<SIZE; ++i) {
			xl[i] = (random.nextInt() >> 33);
			xb[i] = BigInteger.valueOf(xl[i]);
			ym[i] = new MutableLong128(random.nextLong(), random.nextInt() >> 33);
			yb[i] = ym[i].toBigInteger();
		}
	}

	@Benchmark public long timeBigInteger(int reps) {
		long result = 0;
		while (reps-->0) {
			for (int i=0; i<SIZE; ++i) {
				final BigInteger x = xb[i];
				for (int j=0; j<SIZE; ++j) {
					final BigInteger y = yb[j];
					result += y.multiply(x).hashCode();
				}
			}
		}
		return result;
	}

	@Benchmark public long timeUsingSimplified(int reps) {
		return measureMutableLong128(reps, true);
	}

	@Benchmark public long timeWithoutSimplified(int reps) {
		return measureMutableLong128(reps, false);
	}

	private long measureMutableLong128(int reps, boolean useSimplifiedMultiply) {
		MutableLong128.useSimplifiedMultiply = useSimplifiedMultiply;
		long result = 0;
		while (reps-->0) {
			for (int i=0; i<SIZE; ++i) {
				final long x = xl[i];
				for (int j=0; j<SIZE; ++j) {
					final MutableLong128 y = ym[j];
					final long low = y.low();
					final long high = y.high();
					y.multiply(x);
					result += y.hashCode();
					y.setTo(low, high); // restore old values
				}
			}
		}
		return result;
	}

	private static final int SIZE = 100;

	private final long[] xl = new long[SIZE];
	private final MutableLong128[] ym = new MutableLong128[SIZE];

	private final BigInteger[] xb = new BigInteger[SIZE];
	private final BigInteger[] yb = new BigInteger[SIZE];
}
