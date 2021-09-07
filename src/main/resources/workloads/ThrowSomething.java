package de.dagere.peass;

/**
 * Throws something for benchmarking how much time throwing takes
 * 
 * @author reichelt
 *
 */
public class ThrowSomething {
	public int returnMe(final int whatToDo) {
		if (whatToDo == 1) {
			return 1;
		}
		throw new RuntimeException("Give me one!");
	}
}
