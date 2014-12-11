package maaartin.collatz;

import static com.google.common.base.Preconditions.checkArgument;

import java.math.BigInteger;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import com.google.common.primitives.UnsignedLongs;

/**
 * A class representing a 127 bit positive number
 * (negative numbers are unsupported and the highest bit is currently unusable).
 * Only those operations which were needed for this toy project are implemented.
 *
 * <p>Speed is the goal, general usability is not given.
 */
@Getter @EqualsAndHashCode final class MutableLong128 implements Cloneable {
	MutableLong128() {
		this(0, 0);
	}

	/** Create a new instance from the two argument treated as unsigned. */
	MutableLong128(long low) {
		this(low, 0);
	}

	/**
	 * Compose a new instance from the two parts, where the lower is treated as unsigned
	 * and the upper must be non-negative.
	 */
	MutableLong128(long low, long high) {
		checkArgument(high>=0);
		this.low = low;
		this.high = high;
	}

	/** Create an instance from the lowest 128 bits of the argument. */
	MutableLong128(BigInteger bigInteger) {
		this(bigInteger.longValue(), bigInteger.shiftRight(64).longValue());
	}

	@Override protected MutableLong128 clone() {
		try {
			return (MutableLong128) super.clone();
		} catch (final CloneNotSupportedException e) {
			throw new RuntimeException(e); // impossible
		}
	}

	void setTo(long low, long high) {
		this.low = low;
		this.high = high;
	}

	@SuppressWarnings("boxing") @Override public String toString() {
		return String.format("0x%X%016X", high, low);
	}

	boolean fitsInLong() {
		return high==0 & low>=0;
	}

	void add(long x) {
		final long old = low;
		final long neu = old + x;
		low = neu;
		final int change = UnsignedLongs.compare(neu, old);
		if (x<0) {
			if (change > 0) --high; // borrow
		} else {
			if (change < 0) ++high; // carry
		}
		checkOverflow();
	}

	/**
	 * Multiply {@code this} by the argument whcih must be non-negative.
	 * Overflow may or may not be detected.
	 */
	void multiply(long x) {
		checkArgument(x>=0);
		setToProduct1(x, low, high);
		checkOverflow();
	}

	/** Set {@code this} to the product {@code x * (y0 + 2**64* y1)}. */
	private void setToProduct1(long x, long y0, long y1) {
		long p0 = unsignedLow(x) * unsignedLow(y0);
		long p1 = unsignedLow(x) * unsignedHigh(y0);
		long p2 = unsignedHigh(x) * unsignedHigh(y0);

		p2 += unsignedHigh(p1);
		p1 = unsignedLow(p1) + unsignedHigh(x) * unsignedLow(y0);

		long p3 = unsignedLow(x) * unsignedHigh(y1) + unsignedHigh(x) * unsignedLow(y1);
		p3 += unsignedHigh(p2);
		p2 = unsignedLow(p2) + unsignedLow(x) * unsignedLow(y1);

		assert assertCorrectProduct1(new long[] {p0, p1, p2, p3}, x, y0, y1);

		p3 += unsignedHigh(p2);
		p2 = unsignedLow(p2) + unsignedHigh(p1);
		p1 = unsignedLow(p1) + unsignedHigh(p0);
		p0 = unsignedLow(p0);

		assert assertCorrectProduct1(new long[] {p0, p1, p2, p3}, x, y0, y1);

		low = composeLowHigh(p0, p1);
		p2 += unsignedHigh(p1);
		p3 += unsignedHigh(p2);
		high = composeLowHigh(p2, p3);
	}

	/** Shift {@code this} to the by 0 to 127 bits. */
	void shiftRight(int distance) {
		checkArgument(distance>=0 && distance<128);
		if (distance>=64) {
			low = high >>> distance;
			high = 0;
		} else if (distance>0) {
			low = (low >>> distance) | (high << -distance);
			high >>>= distance;
		}
	}

	BigInteger toBigInteger() {
		BigInteger result = BigInteger.valueOf(low);
		if (low<0) result = result.add(TWO_64);
		if (high==0) return result;
		assert high>0;
		return BigInteger.valueOf(high).shiftLeft(64).add(result);
	}

	/** If an overflow happened, {@code this} has already become unusable, but who cares. */
	private void checkOverflow() {
		if (high<0) throw new ArithmeticException("Overflow or negative number");
	}

	/**
	 * Used to verify the intermediate steps of the multiplication.
	 *
	 * The array represents the product starting with the least significant part.
	 * The parts are threated as unsigned 64 bit quantities.
	 * The weight of each subsequent part is 2**32 times bigger (not 2**64 times).
	 *
	 * The other arguments are just like in {@link #setToProduct1(long, long, long)}.
	 */
	private boolean assertCorrectProduct1(long[] parts, long x, long y0, long y1) {
		final BigInteger expected = new MutableLong128(y0, y1).toBigInteger().multiply(BigInteger.valueOf(x));
		final BigInteger actual = composeBigInteger(parts);
		assert actual.equals(expected) : actual.toString(16) + " " + expected.toString(16);
		return true;
	}

	/**
	 * The array represents the result starting with the least significant part.
	 * The parts are threated as unsigned 64 bit quantities.
	 * The weight of each subsequent part is 2**32 times bigger (not 2**64 times).
	 */
	private BigInteger composeBigInteger(long[] parts) {
		BigInteger result = BigInteger.ZERO;
		for (int i=parts.length; i-->0; ) {
			final long p = parts[i];
			result = result.shiftLeft(32).add(new MutableLong128(p).toBigInteger());
		}
		return result;
	}

	/** Extract the 32 least significant bits treated as unsigned number. */
	private static long unsignedLow(long x) {
		return x & 0xFFFFFFFFL;
	}

	/** Extract the 32 most significant bits treated as unsigned number. */
	private static long unsignedHigh(long x) {
		return x >>> 32;
	}

	private static long composeLowHigh(long low, long high) {
		return (high << 32) + (low & 0xFFFFFFFFL);
	}

	private static final BigInteger TWO_64 = BigInteger.ONE.shiftLeft(64);

	private long low;
	private long high;
}
