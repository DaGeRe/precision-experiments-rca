package de.peass.validate_rca;

public class DurationCreator {

   private int fastParameter, slowParameter;
   private boolean leafsHaveWorkloads;
   private int childCount;
   private SlowerNodeInfos nodeInfos;

   public DurationCreator(int fastParameter, int slowParameter, boolean leafsHaveWorkloads, int childCount, SlowerNodeInfos nodeInfos) {
      this.fastParameter = fastParameter / childCount;
      this.slowParameter = this.fastParameter + (slowParameter - fastParameter);
      this.leafsHaveWorkloads = leafsHaveWorkloads;
      this.childCount = childCount;
      this.nodeInfos = nodeInfos;
   }

   public int[] getDuration(int treeLevel, int classIndex, int slowLevel) {
      final int[] durations = new int[childCount];
      if (treeLevel == nodeInfos.getTreeDepth() - 1) {
         if (leafsHaveWorkloads) {
            setMeasureableDuration(treeLevel, classIndex, slowLevel, durations);
         } else {
            if (classIndex == nodeInfos.getSlowerIndex()) {
               setMeasureableDuration(treeLevel, classIndex, slowLevel, durations);
            } else {
               setNoDuration(childCount, durations);
            }

         }
      } else {
         setNoDuration(childCount, durations);
      }

      return durations;
   }

   private void setMeasureableDuration(int treeLevel, int classIndex, int slowLevel, final int[] durations) {
      for (int i = 0; i < durations.length; i++) {
         durations[i] = fastParameter;
      }
      if (classIndex == nodeInfos.getSlowerIndex() && treeLevel == slowLevel) {
         durations[0] = slowParameter;
      }
   }

   private void setNoDuration(int childCount, final int[] durations) {
      for (int i = 0; i < childCount; i++) {
         durations[i] = 0;
      }
   }
}
