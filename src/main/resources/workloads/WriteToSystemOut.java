package de.peass;

import java.util.Random;

/**
 * Write to System.out for Benchmarking
 * 
 * @author reichelt
 *
 */
public class WriteToSystemOut {
   
   private static final Random RANDOM = new Random();
	private static final int MAXIMUM_VALUE = 100;
	int x = 0;

	public int doSomething(int count) {
	   for (int i = 0; i < count; i++) {
	      x += RANDOM.nextInt(MAXIMUM_VALUE);
	      System.out.println(x);
	   }
	   return x;
	}

	public int getValue() {
		return x;
	}
}
