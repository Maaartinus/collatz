package maaartin.collatz;

import java.util.List;
import java.util.Random;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Ints;

import junit.framework.TestCase;

public class CollatzTest extends TestCase {
	public void testDelay() {
		for (final Collatz c : collatzs) {
			checkDelay(c, 1, 0);
			checkDelay(c, 4, 2);
			checkDelay(c, 5, 5);
			checkDelay(c, 17, 12);
			checkDelay(c, 6171, 261);
			checkDelay(c, 9257, 259);
			checkDelay(c, 8400511, 685);
			checkDelay(c, 9973919, 662);
			checkDelay(c, 123456789, 177);

			if (c.limit() <= 1410123943L) {
				assert c instanceof SlowCollatz;
				continue;
			}

			checkDelay(c, 1410123943L, 770);

			if (c.limit() <= 8528817511L) {
				continue;
			}

			assert c instanceof JumpingCollatz;

			checkDelay(c, 8528817511L, 726);
			checkDelay(c, 9780657630L, 1132);
			checkDelay(c, 9780657631L, 1132);
			checkDelay(c, 9912672363L, 1039);

			checkDelay(c, 12327829503L, 543);
			checkDelay(c, 23035537407L, 836);
			checkDelay(c, 100123456789L, 440);

			checkDelay(c, 10000123456789L, 413);
			checkDelay(c, 1000000123456789L, 443);
			checkDelay(c, 100000000123456789L, 509);
			checkDelay(c, 1000000000123456789L, 378);
		}
	}

	public void testDelay_forClassRecords() {
		for (final Collatz c : collatzs) {
			for (int delay=1; delay<=Records.maxKnownClassRecordDelay(); ++delay) {
				final long classRecord = Records.getClassRecord(delay);
				if (c.limit() <= classRecord) break;
				checkDelay(c, classRecord, delay);
			}
		}
	}

	private void checkDelay(Collatz collatz, long n, int expected) {
		final int actual = collatz.delay(n);
		if (expected==actual) return;
		final String msg = collatz + " " + n;
		assertEquals(msg, expected, actual);
	}

	public void testMaximize() {
		for (final Collatz c : collatzs) {
			checkMaximize(c, 1, 1, 1);
			checkMaximize(c, 1, 2, 2);
			checkMaximize(c, 1, 3, 3);
			checkMaximize(c, 1, 4, 3);
			checkMaximize(c, 1, 5, 3);
			checkMaximize(c, 1, 6, 6);
			checkMaximize(c, 1, 7, 7);
			checkMaximize(c, 1, 8, 7);
			checkMaximize(c, 1, 9, 9);
			checkMaximize(c, 1, 10, 9);
			checkMaximize(c, 1, 11, 9);
			checkMaximize(c, 1, 12, 9);
			checkMaximize(c, 1, 13, 9);
			checkMaximize(c, 1, 14, 9);
			checkMaximize(c, 1, 15, 9);
			checkMaximize(c, 1, 16, 9);
			checkMaximize(c, 1, 17, 9);
			checkMaximize(c, 1, 18, 18);
			checkMaximize(c, 1, 19, 18);
			checkMaximize(c, 1, 20, 18);
			checkMaximize(c, 1, 21, 18);
			checkMaximize(c, 1, 22, 18);
			checkMaximize(c, 1, 23, 18);
			checkMaximize(c, 1, 24, 18);
			checkMaximize(c, 1, 25, 25);
			checkMaximize(c, 1, 26, 25);
			checkMaximize(c, 1, 1000, 871);
			checkMaximize(c, 872, 1000, 937);
			checkMaximize(c, 9000, 9999, 9257);
			checkMaximize(c, 1, 9999, 6171);
			checkMaximize(c, 1, 123456, 106239);
			checkMaximize(c, 106240, 123456, 115547);
		}
	}

	public void testMaximize_randomized() {
		final Random random = new Random(4);
		for (int i=0; i<2000; ++i) {
			final int max = random.nextInt(Ints.checkedCast(slowCollatz.limit()));
			final boolean small = random.nextBoolean();
			final int min = Math.max(1, max - random.nextInt(small ? 10 : 1000));
			checkMaximize(min, max);
		}
	}

	public void testMaximize_small() {
		for (int max=1; max<100; ++max) {
			for (int min=1; min<=max; ++min) checkMaximize(min, max);
		}
	}

	private void checkMaximize(int min, int max) {
		final long expected = collatzs.get(0).maximize(min, max);
		for (final Collatz c : collatzs.subList(1, collatzs.size())) {
			assertEquals(expected, c.maximize(min, max));
		}
	}

	private void checkMaximize(Collatz collatz, long min, long max, long expected) {
		assertEquals(collatz.toString(), expected, collatz.maximize(min, max));
	}

	private static final SlowCollatz slowCollatz = new SlowCollatz();
	private static final SimpleCollatz simpleCollatz = new SimpleCollatz();

	private static List<Collatz> collatzs = ImmutableList.of(
			slowCollatz,
			//			new FiddlingCollatz(),
			new JumpingCollatz(JumpingTable.newTable(17), new CollatzTable(17).initialize(simpleCollatz)),
			new JumpingCollatz(JumpingTable.newTable(18), new CollatzTable(18).initialize(simpleCollatz)),
			simpleCollatz);
}
