package de.dagere.peass;

import java.util.Random;

/**
 * Reserves RAM and fills it for benchmarking
 * 
 * @author reichelt
 *
 */
public class ReserveRAM {

   private static final Random RANDOM = new Random();

   final int sizeFactor = 2;
   
   int[][] ram;

   public ReserveRAM() {
   }

   public int doSomething(int size) {
      ram = new int[size][];
      for (int i = 0; i < ram.length; i++) {
         ram[i] = new int[sizeFactor + RANDOM.nextInt(sizeFactor)];
         for (int j = 0; j < ram[i].length; j++) {
            ram[i][j] = RANDOM.nextInt();
         }
      }
      return ram[0][0];
   }

   public int[] getInts() {
      return ram[0];
   }
}
