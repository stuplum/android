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
 *
 * THIS FILE WAS GENERATED BY codergen. EDIT WITH CARE.
 */
package com.android.tools.idea.editors.gfxtrace.service;

import com.android.tools.rpclib.binary.Decoder;
import com.android.tools.rpclib.binary.Encoder;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public final class WireframeMode {
  public static final WireframeMode NoWireframe = new WireframeMode(0, "NoWireframe");
  public static final int NoWireframeValue = 0;
  public static final WireframeMode WireframeOverlay = new WireframeMode(1, "WireframeOverlay");
  public static final int WireframeOverlayValue = 1;
  public static final WireframeMode AllWireframe = new WireframeMode(2, "AllWireframe");
  public static final int AllWireframeValue = 2;

  private static final ImmutableMap<Integer, WireframeMode> VALUES = ImmutableMap.<Integer, WireframeMode>builder()
    .put(0, NoWireframe)
    .put(1, WireframeOverlay)
    .put(2, AllWireframe)
    .build();

  private final int myValue;
  private final String myName;

  private WireframeMode(int v, String n) {
    myValue = v;
    myName = n;
  }

  public int getValue() {
    return myValue;
  }

  public String getName() {
    return myName;
  }

  public void encode(@NotNull Encoder e) throws IOException {
    e.int32(myValue);
  }

  public static WireframeMode decode(@NotNull Decoder d) throws IOException {
    return findOrCreate(d.int32());
  }

  public static WireframeMode find(int value) {
    return VALUES.get(value);
  }

  public static WireframeMode findOrCreate(int value) {
    WireframeMode result = VALUES.get(value);
    return (result == null) ? new WireframeMode(value, null) : result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || !(o instanceof WireframeMode)) return false;
    return myValue == ((WireframeMode)o).myValue;
  }

  @Override
  public int hashCode() {
    return myValue;
  }

  @Override
  public String toString() {
    return (myName == null) ? "WireframeMode(" + myValue + ")" : myName;
  }
}
