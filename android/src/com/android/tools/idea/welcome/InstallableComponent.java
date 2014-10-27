/*
 * Copyright (C) 2014 The Android Open Source Project
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
package com.android.tools.idea.welcome;

import com.android.tools.idea.wizard.DynamicWizardStep;
import com.android.tools.idea.wizard.ScopedStateStore;
import com.intellij.util.download.DownloadableFileDescription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Component that may be installed by the first run wizard.
 */
public abstract class InstallableComponent {
  @NotNull private final String myName;
  private final long mySize;
  private final String myDescription;
  private final ScopedStateStore.Key<Boolean> myKey;

  public InstallableComponent(@NotNull String name, long size, ScopedStateStore.Key<Boolean> key) {
    this(name, size, inventDescription(name, size), key);
  }

  public InstallableComponent(@NotNull String name, long size, @NotNull String description, ScopedStateStore.Key<Boolean> key) {
    myName = name;
    mySize = size;
    myDescription = description;
    myKey = key;
  }

  @NotNull
  private static String inventDescription(String name, long size) {
    return String.format("<html><p>This is a description for <em>%s</em> component</p>" +
                         "<p>We know is that it takes <strong>%s</strong> disk space</p></html>", name, WelcomeUIUtils.getSizeLabel(size));
  }

  public boolean isOptional() {
    return true;
  }

  public long getSize() {
    return mySize;
  }

  public String getDescription() {
    return myDescription;
  }

  @Nullable
  public InstallableComponent getParent() {
    return null;
  }

  public ScopedStateStore.Key<Boolean> getKey() {
    return myKey;
  }

  @Override
  public String toString() {
    return myName;
  }

  public String getLabel() {
    if (mySize == 0) {
      return myName;
    }
    else {
      String sizeLabel = WelcomeUIUtils.getSizeLabel(mySize);
      return String.format("%s – (%s)", myName, sizeLabel);
    }
  }

  public abstract void perform(@NotNull InstallContext downloaded) throws WizardException;

  @NotNull
  public abstract Set<DownloadableFileDescription> getFilesToDownloadAndExpand();

  public abstract void init(ScopedStateStore state);

  public abstract DynamicWizardStep[] createSteps();

  public abstract boolean shouldSetup();

  public abstract boolean hasVisibleStep();
}