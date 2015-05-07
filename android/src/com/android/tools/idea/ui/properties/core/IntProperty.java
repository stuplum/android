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
package com.android.tools.idea.ui.properties.core;

import com.android.tools.idea.ui.properties.ObservableValue;
import com.android.tools.idea.ui.properties.ObservableProperty;
import com.android.tools.idea.ui.properties.expressions.bool.BooleanExpression;
import com.android.tools.idea.ui.properties.expressions.integer.ComparisonExpression;
import com.android.tools.idea.ui.properties.expressions.integer.IntExpression;
import org.jetbrains.annotations.NotNull;

/**
 * Base class that every integer-type property should inherit from, as it provides useful methods
 * that enable chaining.
 */
public abstract class IntProperty extends ObservableProperty<Integer> implements IntExpression {

  public void increment() {
    set(get() + 1);
  }

  @NotNull
  @Override
  public BooleanExpression isEqualTo(@NotNull ObservableValue<Integer> value) {
    return ComparisonExpression.isEqual(this, value);
  }

  @NotNull
  @Override
  public BooleanExpression isGreaterThan(@NotNull ObservableValue<Integer> value) {
    return ComparisonExpression.isGreaterThan(this, value);
  }

  @NotNull
  @Override
  public BooleanExpression isGreaterThanEqualTo(@NotNull ObservableValue<Integer> value) {
    return ComparisonExpression.isGreaterThanEqual(this, value);
  }

  @NotNull
  @Override
  public BooleanExpression isLessThan(@NotNull ObservableValue<Integer> value) {
    return ComparisonExpression.isLessThan(this, value);
  }

  @NotNull
  @Override
  public BooleanExpression isLessThanEqualTo(@NotNull ObservableValue<Integer> value) {
    return ComparisonExpression.isLessThanEqual(this, value);
  }
}