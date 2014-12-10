package maaartin.collatz;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.math.RoundingMode;
import java.util.Arrays;

import lombok.Getter;

import com.google.common.math.LongMath;

/**
 * A table caching all {@link Collatz#delay}s for odd positive arguments below {@link #readLimit}.
 */
class CollatzTable {
	/**
	 * Create a new table of size computed as 2<sup>logSize</sup>,
	 * implying that the {@link limit} is 2<sup>logSize+1</sup>. TODO
	 * 
	 * <p> All table values but the first are initialized to -1, meaning unset.
	 */
	CollatzTable(int logSize) {
		checkArgument(0 < logSize && logSize < 32);
		data = new short[computeLength(logSize)];
		readLimit = 2;
		writeLimit = 2L * data.length;
		Arrays.fill(data, (short) -1);
		data[0] = 0;
	}

	private CollatzTable(short[] data, long limit) {
		this.data = data;
		this.readLimit = limit;
		writeLimit = 2L * data.length;
	}

	/** Initialize a part of the table. */
	CollatzTable initialize(Collatz initializerCollatz, int logSize) {
		checkNotNull(initializerCollatz);
		final int length = computeLength(logSize);
		checkArgument(length <= data.length);
		return initializeInternal(initializerCollatz, length);
	}

	/** Initialize the whole table. */
	CollatzTable initialize(Collatz initializerCollatz) {
		return initializeInternal(initializerCollatz, data.length);
	}

	@Override public String toString() {
		return getClass().getSimpleName()
				+ "-" + LongMath.log2(readLimit, RoundingMode.UP)
				+ "-" + LongMath.log2(writeLimit, RoundingMode.UP);
	}

	private CollatzTable initializeInternal(Collatz initializerCollatz, int length) {
		final int min = (int) (readLimit/2);
		final int max = Math.min(length, data.length) - 1;
		for (int i=min; i<=max; ++i) data[i] = (short) initializerCollatz.delay(2L*i + 1);
		return new CollatzTable(data, 2L * max);
	}

	private int computeLength(int logSize) {
		return logSize==31 ? Integer.MAX_VALUE - 8 : 1 << logSize;
	}

	/** Return true, if this table has a <em>non-empty</em> entry for the argument, which must be an odd number. */
	boolean hasEntry(long n) {
		assert (n&1) != 0 & n > 0;
		return 0 <= n & n < readLimit;
	}

	private int readIndex(long n) {
		assert hasEntry(n);
		return writeIndex(n);
	}

	private int writeIndex(long n) {
		assert (n&1) != 0 & n > 0 && n < 2L * data.length;
		return (int) n >>> 1;
	}

	/**
	 * Return the previously stored value of {@link Collatz#delay} for the argument, or -1 if not set.
	 * 
	 * @param n an odd number between 1 and {@link #readLimit}.
	 */
	int get(long n) {
		assert n > 0;
		return data[readIndex(n)];
	}

	/**
	 * Store a value of {@link Collatz#delay} for the argument.
	 * 
	 * @param n an odd number between 1 and {@link #readLimit}.
	 */
	void set(long n, int value) {
		data[writeIndex(n)] = (short) value;
	}

	@Getter private final long readLimit;
	@Getter private final long writeLimit;
	private final short[] data;
}
