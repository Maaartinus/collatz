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
		final JumpingTable jumpingTable = JumpingTable.newTable(17);
		final Random random = new Random(0);
		for (int i=0; i<SIZE; ++i) {
			final int oddSteps = jumpingTable.oddSteps(random.nextInt(1 << 17));
			xl[i] = JumpingTable.oddStepsToMultiplier(oddSteps);
			xb[i] = BigInteger.valueOf(xl[i]);
			ym[i] = new MutableLong128(random.nextLong(), random.nextInt(3));
			yb[i] = ym[i].toBigInteger();
		}
	}

	@Benchmark public long timeBigInteger(int reps) {
		long result = 0;
		while (reps-->0) {
			for (int i=0; i<SIZE; ++i) {
				final BigInteger x = xb[i];
				final BigInteger y = yb[i];
				result += y.multiply(x).hashCode();
			}
		}
		return result;
	}

	@Benchmark public long time11UseBoth(int reps) {
		return measureMutableLong128(reps, true, true);
	}

	@Benchmark public long time00Simple(int reps) {
		return measureMutableLong128(reps, false, false);
	}

	@Benchmark public long time01UseSmallValues(int reps) {
		return measureMutableLong128(reps, false, true);
	}

	@Benchmark public long time10UseIntMultiplier(int reps) {
		return measureMutableLong128(reps, true, false);
	}

	private long measureMutableLong128(int reps, boolean useIntMultiplier, boolean useSmallValues) {
		MutableLong128.useIntMultiplier = useIntMultiplier;
		MutableLong128.useSmallValues = useSmallValues;
		long result = 0;
		while (reps-->0) {
			for (int i=0; i<SIZE; ++i) {
				final long x = xl[i];
				final MutableLong128 y = ym[i];
				final long low = y.low();
				final long high = y.high();
				y.multiply(x);
				result += y.hashCode();
				y.setTo(low, high); // restore old value
			}
		}
		return result;
	}

	private static final int SIZE = 1000;

	private final long[] xl = new long[SIZE];
	private final MutableLong128[] ym = new MutableLong128[SIZE];

	private final BigInteger[] xb = new BigInteger[SIZE];
	private final BigInteger[] yb = new BigInteger[SIZE];
}
