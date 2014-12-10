package maaartin.collatz;

abstract class Collatz {
	@Override public String toString() {
		return getClass().getSimpleName().replace("Collatz", "");
	}

	/** Return the value below which the implementation is guaranteed to work. */
	public abstract long limit();

	/**
	 * Return the length of the Collatz sequence starting at {@code start}.
	 *
	 * <p>Examples: delay(1) = 0, delay(4) = 2.
	 */
	// Implementation copied to all child classes in order to avoid the virtual call overhead.
	public abstract int delay(long start);

	/**
	 * Return the number between {@code min} and {@code max} maximizing the {@link #delay}.
	 */
	public abstract long maximize(long min, long max);
}
