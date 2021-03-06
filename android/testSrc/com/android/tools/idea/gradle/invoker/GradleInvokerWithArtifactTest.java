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
package com.android.tools.idea.gradle.invoker;

import com.android.tools.idea.gradle.util.BuildMode;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

// Tests cases here are largely similar to the ones in GradleInvokerTest (comment '+' is used to marked the additional tasks here).
// The different is mostly due to the difference of afterSyncTasks.
public class GradleInvokerWithArtifactTest extends AbstractGradleInvokerTest {
  @Override
  protected boolean loadAllTestArtifacts() {
    return true;
  }

  public void testCleanProject() {
    myInvoker.addBeforeGradleInvocationTask(new GradleInvoker.BeforeGradleInvocationTask() {
      @Override
      public void execute(@NotNull List<String> tasks) {
        assertThat(tasks).containsOnly(CLEAN,
                                       qualifiedTaskName(SOURCE_GEN),
                                       qualifiedTaskName(ANDROID_TEST_SOURCE_GEN),
                                       qualifiedTaskName(MOCKABLE_ANDROID_JAR), // +
                                       qualifiedTaskName(PREPARE_UNIT_TEST_DEPENDENCIES)); // +
        // Make sure clean is first.
        assertEquals(CLEAN, tasks.get(0));
        assertEquals(BuildMode.CLEAN, getBuildMode());
      }
    });
    myInvoker.cleanProject();
  }

  public void testGenerateSources() throws Exception {
    myInvoker.addBeforeGradleInvocationTask(new GradleInvoker.BeforeGradleInvocationTask() {
      @Override
      public void execute(@NotNull List<String> tasks) {
        assertThat(tasks).containsOnly(qualifiedTaskName(SOURCE_GEN),
                                                         qualifiedTaskName(ANDROID_TEST_SOURCE_GEN),
                                                         qualifiedTaskName(MOCKABLE_ANDROID_JAR), // +
                                                         qualifiedTaskName(PREPARE_UNIT_TEST_DEPENDENCIES)); // +
        assertEquals(BuildMode.SOURCE_GEN, getBuildMode());
      }
    });
    myInvoker.generateSources(false);
  }

  public void testCompileJava() throws Exception {
    myInvoker.addBeforeGradleInvocationTask(new GradleInvoker.BeforeGradleInvocationTask() {
      @Override
      public void execute(@NotNull List<String> tasks) {
        assertThat(tasks).containsOnly(qualifiedTaskName(SOURCE_GEN),
                                       qualifiedTaskName(ANDROID_TEST_SOURCE_GEN),
                                       qualifiedTaskName(MOCKABLE_ANDROID_JAR), // +
                                       qualifiedTaskName(PREPARE_UNIT_TEST_DEPENDENCIES), // +
                                       qualifiedTaskName(COMPILE_JAVA),
                                       qualifiedTaskName(COMPILE_ANDROID_TEST_JAVA),
                                       qualifiedTaskName(COMPILE_UNIT_TEST_JAVA)); // +
        assertEquals(BuildMode.COMPILE_JAVA, getBuildMode());
      }
    });
    myInvoker.compileJava(new Module[] { myModule }, GradleInvoker.TestCompileType.NONE);
  }

  public void testRebuild() throws Exception {
    myInvoker.addBeforeGradleInvocationTask(new GradleInvoker.BeforeGradleInvocationTask() {
      @Override
      public void execute(@NotNull List<String> tasks) {
        assertThat(tasks).containsOnly(CLEAN,
                                       qualifiedTaskName(SOURCE_GEN),
                                       qualifiedTaskName(ANDROID_TEST_SOURCE_GEN),
                                       qualifiedTaskName(MOCKABLE_ANDROID_JAR), // +
                                       qualifiedTaskName(PREPARE_UNIT_TEST_DEPENDENCIES), // +
                                       qualifiedTaskName(COMPILE_JAVA),
                                       qualifiedTaskName(COMPILE_ANDROID_TEST_JAVA),
                                       qualifiedTaskName(COMPILE_UNIT_TEST_JAVA)); // +
        // Make sure clean is first.
        assertEquals(CLEAN, tasks.get(0));
        assertEquals(BuildMode.REBUILD, getBuildMode());
      }
    });
    myInvoker.rebuild();
  }
}
