package de.dagere.peass.validate_rca;

import picocli.CommandLine.Option;

public class SlowerNodeInfos {
   @Option(names = { "-treeDepth", "--treeDepth" }, description = "Depth of tree", required = false)
   private int treeDepth = 3;

   @Option(names = { "-slowerLevel", "--slowerLevel" }, description = "Level, where execution gets slower", required = false)
   private int slowerLevel = 2;

   @Option(names = { "-slowerIndex", "--slowerIndex" }, description = "Index, where execution gets slower", required = false)
   private int slowerIndex = 0;

   public SlowerNodeInfos() {
      
   }
   
   public SlowerNodeInfos(int treeDepth, int slowerLevel, int slowerIndex) {
      this.treeDepth = treeDepth;
      this.slowerLevel = slowerLevel;
      this.slowerIndex = slowerIndex;
   }

   public int getTreeDepth() {
      return treeDepth;
   }

   public int getSlowerLevel() {
      return slowerLevel;
   }

   public int getSlowerIndex() {
      return slowerIndex;
   }
   
   
}
