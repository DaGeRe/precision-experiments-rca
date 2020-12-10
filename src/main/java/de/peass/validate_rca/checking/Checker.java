package de.peass.validate_rca.checking;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.distribution.TDistribution;

import de.peass.measurement.rca.data.CauseSearchData;
import de.peass.measurement.rca.serialization.MeasuredNode;
import de.peass.validate_rca.SlowerNodeInfos;

public class Checker {

   private final CauseSearchData data;
   private final SlowerNodeInfos nodeInfos;

   private final List<MeasuredNode> incorrectlyChanged = new LinkedList<>();
   private final List<MeasuredNode> incorrectlyUnchanged = new LinkedList<>();

   final double type_1_error = 0.01;

   public Checker(CauseSearchData data, SlowerNodeInfos nodeInfos) {
      this.data = data;
      this.nodeInfos = nodeInfos;
   }

   public List<MeasuredNode> getIncorrectlyChanged() {
      return incorrectlyChanged;
   }
   
   public List<MeasuredNode> getIncorrectlyUnchanged() {
      return incorrectlyUnchanged;
   }
   
   public boolean check() {
      checkChangedNodes();
      checkUnchangedNodes(data.getNodes().getChilds().get(0));
      for (int childIndex = 2; childIndex < data.getNodes().getChilds().size(); childIndex++) {
         MeasuredNode child = data.getNodes().getChilds().get(childIndex);
         checkUnchangedNodes(child);
      }

      return incorrectlyChanged.size() == 0 && incorrectlyUnchanged.size() == 0;
   }

   private void checkUnchangedNodes(MeasuredNode node) {
      boolean isChange = isNodeChange(node);
      if (isChange) {
         incorrectlyChanged.add(node);
      }
      List<MeasuredNode> childs = node.getChilds();
      if (childs.size() != 0) {
         checkUnchangedNodes(childs.get(0));
         for (int childIndex = 2; childIndex < childs.size(); childIndex++) {
            MeasuredNode child = childs.get(childIndex);
            checkUnchangedNodes(child);
         }
      }

   }

   private void checkChangedNodes() {
      MeasuredNode node = data.getNodes();
      System.out.println(nodeInfos.getSlowerLevel());
      checkNode(node, true);
      for (int i = 0; i < nodeInfos.getSlowerLevel(); i++) {
         node = node.getChilds().get(1);
         checkNode(node, true);
      }

      MeasuredNode expectedSlowerNode = node.getChilds().get(nodeInfos.getSlowerIndex() + 1);
      boolean isChange = isNodeChange(expectedSlowerNode);
      if (!isChange) {
         incorrectlyUnchanged.add(node);
      }
   }

   private void checkNode(MeasuredNode node, boolean expected) {
      boolean isChange = isNodeChange(node);
      if (isChange != expected) {
         if (expected == true) {
            incorrectlyUnchanged.add(node);
         } else {
            incorrectlyChanged.add(node);
         }
      }
   }

   private boolean isNodeChange(MeasuredNode node) {
      final long degreesOfFreedom = node.getStatistic().getVMs() * 2 - 2;
      final TDistribution distribution = new TDistribution(null, degreesOfFreedom);
      double pValue = 2.0 * distribution.cumulativeProbability(-Math.abs(node.getStatistic().getTvalue()));
      boolean isChange = pValue < type_1_error;
//      System.out.println(node.getCall() + " " + node.getStatistic().getTvalue() + " " + degreesOfFreedom + " " + pValue + " " + isChange);

      return isChange;
   }
}
