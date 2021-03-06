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
package com.android.tools.idea.editors.gfxtrace.service.path;

import com.android.tools.rpclib.schema.*;
import com.android.tools.idea.editors.gfxtrace.service.image.Format;
import com.android.tools.rpclib.binary.*;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;

public final class ResourcePath extends Path {
  @Override
  public StringBuilder stringPath(StringBuilder builder) {
    return myAfter.stringPath(builder).append(".Resource<").append(myID).append(">");
  }

  @Override
  public Path getParent() {
    return myAfter;
  }

  public ThumbnailPath thumbnail(Dimension dimension, Format fmt) {
    return new ThumbnailPath().setObject(this).setDesiredMaxWidth(dimension.width).setDesiredMaxHeight(dimension.height)
      .setDesiredFormat(fmt);
  }

  //<<<Start:Java.ClassBody:1>>>
  private ResourceID myID;
  private AtomPath myAfter;

  // Constructs a default-initialized {@link ResourcePath}.
  public ResourcePath() {}


  public ResourceID getID() {
    return myID;
  }

  public ResourcePath setID(ResourceID v) {
    myID = v;
    return this;
  }

  public AtomPath getAfter() {
    return myAfter;
  }

  public ResourcePath setAfter(AtomPath v) {
    myAfter = v;
    return this;
  }

  @Override @NotNull
  public BinaryClass klass() { return Klass.INSTANCE; }


  private static final Entity ENTITY = new Entity("path", "Resource", "", "");

  static {
    ENTITY.setFields(new Field[]{
      new Field("ID", new Array("ResourceID", new Primitive("byte", Method.Uint8), 20)),
      new Field("After", new Pointer(new Struct(AtomPath.Klass.INSTANCE.entity()))),
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
    public BinaryObject create() { return new ResourcePath(); }

    @Override
    public void encode(@NotNull Encoder e, BinaryObject obj) throws IOException {
      ResourcePath o = (ResourcePath)obj;
      o.myID.write(e);

      e.object(o.myAfter);
    }

    @Override
    public void decode(@NotNull Decoder d, BinaryObject obj) throws IOException {
      ResourcePath o = (ResourcePath)obj;
      o.myID = new ResourceID(d);

      o.myAfter = (AtomPath)d.object();
    }
    //<<<End:Java.KlassBody:2>>>
  }
}
