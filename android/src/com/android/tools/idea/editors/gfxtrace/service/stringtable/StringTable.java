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
package com.android.tools.idea.editors.gfxtrace.service.stringtable;

import com.intellij.util.containers.hash.LinkedHashMap;
import org.jetbrains.annotations.NotNull;

import com.android.tools.rpclib.binary.*;
import com.android.tools.rpclib.schema.*;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

public final class StringTable implements BinaryObject {
  private static AtomicReference<StringTable> sCurrent = new AtomicReference<StringTable>();

  /**
   * Changes the current string table to this instance.
   */
  public void setCurrent() {
    sCurrent.set(this);
  }

  /**
   * Returns the current string table.
   */
  public static synchronized StringTable getCurrent() {
    return sCurrent.get();
  }

  /**
   * Returns the entry with the specified identifier, or null if the table does not contain
   * an entry with specified identifier.
   */
  public Node get(String id) {
    return myEntries.get(id);
  }

  //<<<Start:Java.ClassBody:1>>>
  private Info myInfo;
  private LinkedHashMap<String, Node> myEntries;

  // Constructs a default-initialized {@link StringTable}.
  public StringTable() {}


  public Info getInfo() {
    return myInfo;
  }

  public StringTable setInfo(Info v) {
    myInfo = v;
    return this;
  }

  public LinkedHashMap<String, Node> getEntries() {
    return myEntries;
  }

  public StringTable setEntries(LinkedHashMap<String, Node> v) {
    myEntries = v;
    return this;
  }

  @Override @NotNull
  public BinaryClass klass() { return Klass.INSTANCE; }


  private static final Entity ENTITY = new Entity("stringtable", "StringTable", "", "");

  static {
    ENTITY.setFields(new Field[]{
      new Field("Info", new Struct(Info.Klass.INSTANCE.entity())),
      new Field("Entries", new Map("", new Primitive("string", Method.String), new Interface("Node"))),
    });
    Namespace.register(Klass.INSTANCE);
  }
  public static void register() {}
  //<<<End:Java.ClassBody:1>>>
  public enum Klass implements BinaryClass {
    //<<<Start:Java.KlassBody:2>>>
    INSTANCE;

    @Override @NotNull
    public Entity entity() { return ENTITY; }

    @Override @NotNull
    public BinaryObject create() { return new StringTable(); }

    @Override
    public void encode(@NotNull Encoder e, BinaryObject obj) throws IOException {
      StringTable o = (StringTable)obj;
      e.value(o.myInfo);
      e.uint32(o.myEntries.size());
      for (java.util.Map.Entry<String, Node> entry : o.myEntries.entrySet()) {
        e.string(entry.getKey());
        e.object(entry.getValue().unwrap());
      }
    }

    @Override
    public void decode(@NotNull Decoder d, BinaryObject obj) throws IOException {
      StringTable o = (StringTable)obj;
      o.myInfo = new Info();
      d.value(o.myInfo);
      o.myEntries = new LinkedHashMap<String, Node>();
      int size = d.uint32();
      for (int i = 0; i < size; i++) {
        String key = d.string();
        Node value = Node.wrap(d.object());
        o.myEntries.put(key, value);
      }
    }
    //<<<End:Java.KlassBody:2>>>
  }
}
