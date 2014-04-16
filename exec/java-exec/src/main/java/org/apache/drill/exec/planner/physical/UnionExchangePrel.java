/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.drill.exec.planner.physical;

import java.io.IOException;
import java.util.List;

import org.apache.drill.exec.physical.base.PhysicalOperator;
import org.apache.drill.exec.physical.config.Screen;
import org.apache.drill.exec.physical.config.SelectionVectorRemover;
import org.apache.drill.exec.physical.config.UnionExchange;
import org.apache.drill.exec.planner.cost.DrillCostBase;
import org.apache.drill.exec.record.BatchSchema.SelectionVectorMode;
import org.eigenbase.rel.RelNode;
import org.eigenbase.rel.SingleRel;
import org.eigenbase.rel.metadata.RelMetadataQuery;
import org.eigenbase.relopt.RelOptCluster;
import org.eigenbase.relopt.RelOptCost;
import org.eigenbase.relopt.RelOptPlanner;
import org.eigenbase.relopt.RelTraitSet;

public class UnionExchangePrel extends SingleRel implements Prel {

  public UnionExchangePrel(RelOptCluster cluster, RelTraitSet traitSet, RelNode input) {
    super(cluster, traitSet, input);
    assert input.getConvention() == Prel.DRILL_PHYSICAL;
  }

  /**    
   * A UnionExchange processes a total of M rows coming from N senders and 
   * combines them into a single output stream.  Note that there is 
   * no sort or merge operation going on. For costing purposes, we can
   * assume each sender is sending M/N rows to a single receiver. 
   * (See DrillCostBase for symbol notations)
   * C =  CPU cost of SV remover for M/N rows 
   *      + Network cost of sending M/N rows to 1 destination. 
   * So, C = (s * M/N) + (w * M/N) 
   * Total cost = N * C
   */    
  @Override
  public RelOptCost computeSelfCost(RelOptPlanner planner) {
    if (DrillCostBase.useDefaultCosting) {
      return super.computeSelfCost(planner).multiplyBy(.1); 
    }
    
    RelNode child = this.getChild();
    double inputRows = RelMetadataQuery.getRowCount(child);
    int  rowWidth = child.getRowType().getPrecision();    
    double svrCpuCost = DrillCostBase.svrCpuCost * inputRows;
    double networkCost = DrillCostBase.byteNetworkCost * inputRows * rowWidth;
    return new DrillCostBase(inputRows, svrCpuCost, 0, networkCost);   
  }

  @Override
  public RelNode copy(RelTraitSet traitSet, List<RelNode> inputs) {
    return new UnionExchangePrel(getCluster(), traitSet, sole(inputs));
  }

  public PhysicalOperator getPhysicalOperator(PhysicalPlanCreator creator) throws IOException {
    Prel child = (Prel) this.getChild();

    PhysicalOperator childPOP = child.getPhysicalOperator(creator);

    if(PrelUtil.getSettings(getCluster()).isSingleMode()) return childPOP;

    //Currently, only accepts "NONE". For other, requires SelectionVectorRemover
    childPOP = PrelUtil.removeSvIfRequired(childPOP, SelectionVectorMode.NONE, SelectionVectorMode.TWO_BYTE);

    UnionExchange g = new UnionExchange(childPOP);
    return g;
  }

}
