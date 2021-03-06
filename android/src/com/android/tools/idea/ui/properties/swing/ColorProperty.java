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
package com.android.tools.idea.ui.properties.swing;

import com.android.tools.idea.ui.properties.ObservableProperty;
import com.android.tools.idea.ui.properties.core.OptionalProperty;
import com.google.common.base.Optional;
import com.intellij.ui.ColorPanel;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * {@link ObservableProperty} that wraps a {@link ColorPanel} and exposes its color.
 */
public final class ColorProperty extends OptionalProperty<Color> implements ActionListener {
  @NotNull private final ColorPanel myColorPanel;

  public ColorProperty(@NotNull ColorPanel colorPanel) {
    myColorPanel = colorPanel;
    myColorPanel.addActionListener(this);
  }

  @Override
  public void actionPerformed(ActionEvent actionEvent) {
    notifyInvalidated();
  }

  @Override
  protected void setDirectly(@NotNull Optional<Color> value) {
    myColorPanel.setSelectedColor(value.orNull());
  }

  @NotNull
  @Override
  public Optional<Color> get() {
    return Optional.fromNullable(myColorPanel.getSelectedColor());
  }
}
