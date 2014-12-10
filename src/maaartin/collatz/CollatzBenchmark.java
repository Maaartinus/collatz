package maaartin.collatz;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;

import de.grajcar.dout.Dout;

enum CollatzBenchmark {
	SLOW,
	FIDDLING,
	SIMPLE,
	//	JUMPING_12_27,
	//	JUMPING_12_28,
	//	JUMPING_12_29,
	//	JUMPING_12_30,
	//	JUMPING_12_31,
	//	JUMPING_13_30,
	//	JUMPING_13_31,
	//	JUMPING_14_27,
	//	JUMPING_14_28,
	//	JUMPING_14_29,
	//	JUMPING_14_30,
	//	JUMPING_14_31,
	//	JUMPING_15_26,
	//	JUMPING_15_27,
	//	JUMPING_15_28,
	//	JUMPING_15_29,
	//	JUMPING_15_30,
	//	JUMPING_15_31,
	//	JUMPING_16_26,
	//	JUMPING_16_27,
	//	JUMPING_16_28,
	//	JUMPING_16_29,
	//	JUMPING_16_30,
	//	JUMPING_16_31,
	JUMPING_17_20, // best so far
	JUMPING_17_21,
	JUMPING_17_22,
	JUMPING_17_23,
	JUMPING_17_24,
	JUMPING_17_25,
	JUMPING_17_26,
	JUMPING_17_27,
	JUMPING_17_28,
	JUMPING_17_29,
	JUMPING_17_30,
	JUMPING_17_31,
	//	JUMPING_18_26,
	//	JUMPING_18_27,
	//	JUMPING_18_28,
	//	JUMPING_18_29,
	//	JUMPING_18_30,
	//	JUMPING_18_31,
	//	JUMPING_19_27,
	//	JUMPING_19_28,
	//	JUMPING_19_29,
	//	JUMPING_19_30,
	//	JUMPING_19_31,
	//	JUMPING_20_27,
	//	JUMPING_20_28,
	//	JUMPING_20_29,
	//	JUMPING_20_30,
	//	JUMPING_22_28,
	//	JUMPING_24_28,
	//	JUMPING_26_28,
	//	JUMPING_28_28,
	//	JUMPING_28_28,
	//	JUMPING_29_29,
	;

	public static void main(String[] args) {
		Dout.a("STARTED");
		for (final CollatzBenchmark b : CollatzBenchmark.values()) b.go();
		Dout.a("DONE");
	}

	long runJumping(long limit) {
		final int jumpingTableLogSize = Integer.parseInt(name().replaceAll(".*_(\\d+)_(\\d+)", "$1"));
		final int collatzTableLogSize = Integer.parseInt(name().replaceAll(".*_(\\d+)_(\\d+)", "$2"));
		checkArgument(collatzTableLogSize >= jumpingTableLogSize);
		final JumpingTable jumpingTable = JumpingTable.newTable(jumpingTableLogSize);
		Collatz c = new SimpleCollatz();
		final int n = Math.max(jumpingTableLogSize, Math.min(collatzTableLogSize, 20));
		CollatzTable collatzTable = new CollatzTable(collatzTableLogSize).initialize(c, n);
		for (int i = Math.max(27, n+1); i<=collatzTableLogSize; ++i) {
			c = new JumpingCollatz(jumpingTable, collatzTable);
			collatzTable = collatzTable.initialize(c, i);
		}
		c = new JumpingCollatz(jumpingTable, collatzTable);
		initialized();
		return c.maximize(1, limit);
	}

	long runSimple(long limit) {
		final Collatz c = new SimpleCollatz();
		initialized();
		return c.maximize(1, limit);
	}

	long runFiddling(long limit) {
		final Collatz c = new FiddlingCollatz();
		initialized();
		return c.maximize(1, limit);
	}

	@SuppressWarnings("boxing")
	private void go() {
		startTime = now();
		final long result = run(LIMIT);
		endTime = now();
		final double initialization = 1e-9 * (middleTime-startTime);
		final double computation= 1e-9 * (endTime-middleTime);
		final double total = initialization + computation;
		if (initialization < 0 && result == 0) return;
		System.out.format("%20s %9.3f %9.3f %9.3f (%d)\n", this, initialization, computation, total, result);
		checkResult(result);
	}

	@SuppressWarnings("all") private void checkResult(final long result) {
		if (LIMIT == (long) 1e10) verify(result == 9780657630L);
	}

	private long now() {
		return System.nanoTime();
	}

	private static final long LIMIT = (long) 1e10;

	final long run(long limit) {
		//		if (name().startsWith("SIMPLE")) return runSimple(limit);
		if (name().startsWith("JUMPING")) return runJumping(limit);
		//		if (name().startsWith("FIDDLING")) return runFiddling(limit);
		return 0;
	}

	final void initialized() {
		middleTime = now();
	}

	private long startTime;
	private long middleTime;
	private long endTime;
}
