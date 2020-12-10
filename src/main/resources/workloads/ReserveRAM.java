package de.peass;

import java.util.Random;

/**
 * Reserves RAM and fills it for benchmarking
 * 
 * @author reichelt
 *
 */
public class ReserveRAM {

	private static final Random RANDOM = new Random();

	int[][] ram;

	public ReserveRAM(final int size) {
		ram = new int[size][];
	}

	public void doSomething() {
		for (int i = 0; i < ram.length; i++) {
			ram[i] = new int[20 + RANDOM.nextInt(5)];
			for (int j = 0; j < ram[i].length; j++) {
				ram[i][j] = RANDOM.nextInt();
			}
		}
	}

	public int[][] getInts() {
		return ram;
	}
}
