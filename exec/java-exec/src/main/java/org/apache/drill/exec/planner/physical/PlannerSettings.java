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

import net.hydromatic.optiq.tools.FrameworkContext;

public class PlannerSettings implements FrameworkContext{
  static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PlannerSettings.class);

  private boolean singleMode;
  private int numEndPoints = 0;

  public boolean isSingleMode() {
    return singleMode;
  }

  public int numEndPoints() {
    return numEndPoints;  
  }
  
  public void setSingleMode(boolean singleMode) {
    this.singleMode = singleMode;
  }
  
  public void setNumEndPoints(int numEndPoints) {
    this.numEndPoints = numEndPoints;
  }

  @Override
  public <T> T unwrap(Class<T> clazz) {
    if(clazz == PlannerSettings.class){
      return (T) this;
    }else{
      return null;
    }
  }


}
