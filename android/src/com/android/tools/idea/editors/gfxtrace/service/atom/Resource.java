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
package com.android.tools.idea.editors.gfxtrace.service.atom;

import com.android.tools.rpclib.schema.*;
import com.android.tools.rpclib.binary.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public final class Resource implements BinaryObject {
  //<<<Start:Java.ClassBody:1>>>
  private BinaryID myID;
  private byte[] myData;

  // Constructs a default-initialized {@link Resource}.
  public Resource() {}


  public BinaryID getID() {
    return myID;
  }

  public Resource setID(BinaryID v) {
    myID = v;
    return this;
  }

  public byte[] getData() {
    return myData;
  }

  public Resource setData(byte[] v) {
    myData = v;
    return this;
  }

  @Override @NotNull
  public BinaryClass klass() { return Klass.INSTANCE; }


  private static final Entity ENTITY = new Entity("atom", "Resource", "", "");

  static {
    ENTITY.setFields(new Field[]{
      new Field("ID", new Array("binary.ID", new Primitive("byte", Method.Uint8), 20)),
      new Field("Data", new Slice("", new Primitive("byte", Method.Uint8))),
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
    public BinaryObject create() { return new Resource(); }

    @Override
    public void encode(@NotNull Encoder e, BinaryObject obj) throws IOException {
      Resource o = (Resource)obj;
      o.myID.write(e);

      e.uint32(o.myData.length);
      e.write(o.myData, o.myData.length);

    }

    @Override
    public void decode(@NotNull Decoder d, BinaryObject obj) throws IOException {
      Resource o = (Resource)obj;
      o.myID = new BinaryID(d);

      o.myData = new byte[d.uint32()];
      d.read(o.myData, o.myData.length);

    }
    //<<<End:Java.KlassBody:2>>>
  }
}
