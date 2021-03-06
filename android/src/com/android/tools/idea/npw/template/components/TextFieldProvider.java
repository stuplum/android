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
package com.android.tools.idea.npw.template.components;

import com.android.tools.idea.templates.Parameter;
import com.android.tools.idea.ui.properties.ObservableProperty;
import com.android.tools.idea.ui.properties.swing.TextProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Provides a textfield well suited for handling {@link Parameter.Type#STRING} parameters.
 */
public final class TextFieldProvider extends ParameterComponentProvider<JTextField> {
  public TextFieldProvider(@NotNull Parameter parameter) {
    super(parameter);
  }

  @NotNull
  @Override
  protected JTextField createComponent(@NotNull Parameter parameter) {
    return new JTextField();
  }

  @Nullable
  @Override
  public ObservableProperty<?> createProperty(@NotNull JTextField textField) {
    return new TextProperty(textField);
  }
}
