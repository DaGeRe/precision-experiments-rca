package de.peass;

import java.util.Random;

/**
 * Write to System.out for Benchmarking
 * 
 * @author reichelt
 *
 */
public class WriteToSystemOut {
	private static final int MAXIMUM_VALUE = 100;
	int x = 0;

	public void doSomething() {
		x += new Random().nextInt(MAXIMUM_VALUE);
		System.out.println(x);
	}

	public int getValue() {
		return x;
	}
}
