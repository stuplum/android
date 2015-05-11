/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.tools.idea.editors.hprof.descriptors;

import com.intellij.debugger.engine.evaluation.EvaluationContextImpl;
import com.intellij.debugger.ui.impl.watch.NodeDescriptorImpl;
import com.intellij.debugger.ui.tree.render.DescriptorLabelListener;

public class ExpansionDescriptorImpl extends NodeDescriptorImpl {
  private final int myStartIndex;
  private final int myTotal;

  public ExpansionDescriptorImpl(int startIndex, int total) {
    myStartIndex = startIndex;
    myTotal = total;
    setLabel(calcRepresentation(null, null));
  }

  public int getStartIndex() {
    return myStartIndex;
  }

  public int getTotal() {
    return myTotal;
  }

  @Override
  protected String calcRepresentation(EvaluationContextImpl context, DescriptorLabelListener labelListener) {
    return String.format("%d of %d remaining. Click row to expand.", myTotal - myStartIndex, myTotal);
  }

  @Override
  public boolean isExpandable() {
    return false;
  }

  @Override
  public void setContext(EvaluationContextImpl context) {

  }
}