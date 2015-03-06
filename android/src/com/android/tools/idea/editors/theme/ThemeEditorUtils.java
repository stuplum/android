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
package com.android.tools.idea.editors.theme;

import com.android.SdkConstants;
import com.android.ide.common.rendering.api.ItemResourceValue;
import com.android.ide.common.resources.ResourceUrl;
import com.android.tools.idea.configurations.Configuration;
import com.android.tools.idea.javadoc.AndroidJavaDocRenderer;
import com.android.tools.idea.rendering.ResourceHelper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import org.jetbrains.android.dom.attrs.AttributeDefinition;
import org.jetbrains.android.dom.attrs.AttributeFormat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Utility class for static methods which are used in different classes of theme editor
 */
public class ThemeEditorUtils {
  private static final Cache<String, String> ourTooltipCache = CacheBuilder.newBuilder()
    .weakValues()
    .maximumSize(30) // To be able to cache roughly one screen of attributes
    .build();

  private ThemeEditorUtils() { }

  @Nullable
  public static String generateToolTipText(final ItemResourceValue resValue, final Module module, final Configuration configuration) {
    String tooltipKey = resValue.toString() + module.toString() + configuration.toString();

    String cachedTooltip = ourTooltipCache.getIfPresent(tooltipKey);
    if (cachedTooltip != null) {
      return cachedTooltip;
    }

    String value = resValue.getValue();
    if (SdkConstants.NULL_RESOURCE.equalsIgnoreCase(value)) {
      return SdkConstants.NULL_RESOURCE;
    }
    final Color color = ResourceHelper.parseColor(value);
    if (color != null) {
      return AndroidJavaDocRenderer.renderColor(module, color);
    }
    ResourceUrl resUrl = ResourceUrl.parse(value);
    if (resUrl == null) {
      return null;
    }
    if (!resUrl.framework && resValue.isFramework()) {
      // sometimes the framework people forgot to put android: in the value, so we need to fix for this.
      // To do that, we just reparse the resource adding the android: namespace.
      resUrl = ResourceUrl.parse(resUrl.toString().replace(resUrl.type.getName(), SdkConstants.PREFIX_ANDROID + resUrl.type.getName()));
    }
    String tooltipContents = AndroidJavaDocRenderer.render(module, configuration, resUrl);
    if (tooltipContents != null) {
      ourTooltipCache.put(tooltipKey, tooltipContents);
    }

    return tooltipContents;
  }

  public static void openThemeEditor(final @NotNull Module module) {
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      @Override
      public void run() {
        ThemeEditorVirtualFile file = null;
        final Project project = module.getProject();
        final FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);

        for (final FileEditor editor : fileEditorManager.getAllEditors()) {
          if (!(editor instanceof ThemeEditor)) {
            continue;
          }

          ThemeEditor themeEditor = (ThemeEditor) editor;
          if (themeEditor.getVirtualFile().getModule() == module) {
            file = themeEditor.getVirtualFile();
            break;
          }
        }

        // If existing virtual file is found, openEditor with created descriptor is going to
        // show existing editor (without creating a new tab). If we haven't found any existing
        // virtual file, we're creating one here (new tab with theme editor will be opened).
        if (file == null) {
          file = ThemeEditorVirtualFile.getThemeEditorFile(module);
        }
        final OpenFileDescriptor descriptor = new OpenFileDescriptor(project, file);
        fileEditorManager.openEditor(descriptor, true);
      }
    });
  }

  public static List<EditedStyleItem> resolveAllAttributes(final ThemeEditorStyle style) {
    final List<EditedStyleItem> allValues = new ArrayList<EditedStyleItem>();
    final Set<String> namesSet = new TreeSet<String>();

    ThemeEditorStyle currentStyle = style;
    while (currentStyle != null) {
      for (final ItemResourceValue value : currentStyle.getValues()) {
        String itemName = StyleResolver.getQualifiedItemName(value);
        if (!namesSet.contains(itemName)) {
          allValues.add(new EditedStyleItem(value, currentStyle));
          namesSet.add(itemName);
        }
      }

      currentStyle = currentStyle.getParent();
    }

    return allValues;
  }

  public static Object extractRealValue(final EditedStyleItem item, final Class<?> desiredClass) {
    String value = item.getValue();
    if (desiredClass == Boolean.class && ("true".equals(value) || "false".equals(value))) {
      return Boolean.valueOf(value);
    }
    if (desiredClass == Integer.class && value != null) {
      try {
        return Integer.parseInt(value);
      } catch (NumberFormatException e) {
        return value;
      }
    }
    return value;
  }

  public static boolean acceptsFormat(@Nullable AttributeDefinition attrDefByName, @NotNull AttributeFormat want) {
    if (attrDefByName == null) {
      return false;
    }
    return attrDefByName.getFormats().contains(want);
  }

}