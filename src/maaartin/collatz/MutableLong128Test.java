package maaartin.collatz;

import java.math.BigInteger;
import java.util.Random;

import junit.framework.TestCase;

public class MutableLong128Test extends TestCase {
	public void testMultiply() {
		final Random random = new Random(0);
		for (int i=0; i<500_000; ++i) {
			final int shift = 1 + random.nextInt(61);
			final long x = 1 + (random.nextLong() >>> shift);
			final long y0 = random.nextLong();
			final long y1 = random.nextBoolean() ? 0 : Long.MAX_VALUE / 2 / x;
			final MutableLong128 z = new MutableLong128(y0, y1);
			final BigInteger expected = new MutableLong128(y0, y1).toBigInteger().multiply(BigInteger.valueOf(x));
			z.multiply(x);
			final BigInteger actual = z.toBigInteger();
			assertEquals(expected, actual);
		}
	}

	public void testToBigInteger() {
		final Random random = new Random(0);
		for (int i=0; i<20_000; ++i) {
			final long low = random.nextLong();
			final long high = random.nextLong() >>> 1;
			final MutableLong128 z = new MutableLong128(low, high);
			@SuppressWarnings("boxing")
			final BigInteger expected = new BigInteger(String.format("%016X%016X", high, low), 16);
			final BigInteger actual = z.toBigInteger();
			assertEquals(expected, actual);
		}
	}

	public void testShiftRight() {
		final Random random = new Random(0);
		for (int i=0; i<200_000; ++i) {
			final long low = random.nextLong();
			final long high = random.nextLong() >>> 1;
			final int distance = random.nextInt(128);
			final MutableLong128 z = new MutableLong128(low, high);

			final BigInteger expected = z.toBigInteger().shiftRight(distance);
			z.shiftRight(distance);
			final BigInteger actual = z.toBigInteger();
			assertEquals(expected, actual);
		}
	}

	public void testAdd() {
		final Random random = new Random(0);
		for (int i=0; i<200_000; ++i) {
			final long low = random.nextLong();
			final long high = random.nextLong() >>> 2;
			final long addend = random.nextLong() >>> 1;
			final MutableLong128 z = new MutableLong128(low, high);
			final BigInteger expected = z.toBigInteger().add(BigInteger.valueOf(addend));
			z.add(addend);
			final BigInteger actual = z.toBigInteger();
			assertEquals(expected, actual);
		}
	}
}
