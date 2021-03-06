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
package com.android.tools.idea.editors.gfxtrace.actions;

import com.android.ddmlib.IDevice;
import com.android.tools.idea.ddms.EdtExecutor;
import com.android.tools.idea.editors.gfxtrace.DeviceInfo;
import com.android.tools.idea.editors.gfxtrace.GfxTracer;
import com.android.tools.idea.editors.gfxtrace.forms.ActivitySelector;
import com.android.tools.idea.editors.gfxtrace.forms.TraceDialog;
import com.android.tools.idea.editors.gfxtrace.gapi.GapiPaths;
import com.android.tools.idea.monitor.gpu.GpuMonitorView;
import com.intellij.concurrency.JobScheduler;
import com.android.tools.idea.profiling.capture.CaptureService;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import icons.AndroidIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.concurrent.TimeUnit;
import java.awt.*;

public abstract class GfxTraceCaptureAction extends ToggleAction {
  @NotNull protected final GpuMonitorView myView;
  @NotNull protected final String myText;
  private static JDialog sActiveForm = null;
  private JDialog myActiveForm = null;

  public static class Listen extends GfxTraceCaptureAction {
    public Listen(@NotNull GpuMonitorView view) {
      super(view, "Listen", "Listen for GFX traces", AndroidIcons.GfxTrace.ListenForTrace);
    }

    @Override
    void start(@NotNull final Container window, @NotNull final IDevice device) {
      final TraceDialog dialog = new TraceDialog();
      dialog.setListener(new TraceDialog.Listener() {
        private GfxTracer myTracer = null;

        @Override
        public void onStartTrace(@NotNull String name) {
          GfxTracer.Options options = GfxTracer.Options.fromRunConfiguration(getSelectedRunConfiguration(myView));
          options.myTraceName = name;
          myTracer = GfxTracer.listen(myView.getProject(), device, options, bindListener(dialog));
        }

        @Override
        public void onStopTrace() {
          myTracer.stop();
          onStop();
        }

        @Override
        public void onCancelTrace() {
          onStop();
        }
      });
      CaptureService service = CaptureService.getInstance(myView.getProject());
      String name = service.getSuggestedName(myView.getDeviceContext().getSelectedClient());
      dialog.setLocationRelativeTo(window);
      dialog.setDefaultName(name);
      dialog.setVisible(true);
      setActiveForm(dialog);
    }
  }

  public static class Launch extends GfxTraceCaptureAction {
    private static final String NOTIFICATION_GROUP = "GPU trace";
    private static final String NOTIFICATION_LAUNCH_REQUIRES_ROOT_TITLE = "Rooted device required";
    private static final String NOTIFICATION_LAUNCH_REQUIRES_ROOT_CONTENT =
      "The device needs to be rooted in order to launch an application for GPU tracing.<br/>" +
      "To trace your own application on a non-rooted device, build your application with the GPU tracing library.";

    public Launch(@NotNull GpuMonitorView view) {
      super(view, "Launch", "Launch in GFX trace mode", AndroidIcons.GfxTrace.InjectSpy);
    }

    @Override
    void start(@NotNull final Container window, @NotNull final IDevice device) {
      ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
        private static final int ROOT_QUERY_TIMEOUT = 3000;
        private static final int ROOT_QUERY_INTERVAL = 250;

        @Override
        public void run() {
          try {
            if (device.root()) {
              rootingSucceeded();
            }
            else {
              rootingFailed();
            }
            return;
          }
          catch (Exception ignored) {
          }

          // adb root may need some time to restart. Keep on trying to query for a few seconds.
          new Runnable() {
            long start = System.currentTimeMillis();

            @Override
            public void run() {
              try {
                if (device.isRoot()) {
                  rootingSucceeded();
                }
                else {
                  rootingFailed();
                }
              }
              catch (Exception ignored) {
                if ((System.currentTimeMillis() - start) < ROOT_QUERY_TIMEOUT) {
                  JobScheduler.getScheduler().schedule(this, ROOT_QUERY_INTERVAL, TimeUnit.MILLISECONDS);
                }
                else {
                  rootingFailed();
                }
              }
            }
          }.run();
        }

        private void rootingFailed() {
          // Failed to restart adb as root.
          // Display message and abort.
          ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
              Notifications.Bus.notify(
                new Notification(NOTIFICATION_GROUP, NOTIFICATION_LAUNCH_REQUIRES_ROOT_TITLE, NOTIFICATION_LAUNCH_REQUIRES_ROOT_CONTENT,
                                 NotificationType.ERROR));
            }
          });
          onStop();
        }

        private void rootingSucceeded() {
          showLauncher(window, device, getSelectedRunConfiguration(myView));
        }
      });
    }

    private void showLauncher(final Component owner, final IDevice device, final RunConfiguration runConfig) {
      DeviceInfo.Provider provider = new DeviceInfo.PkgInfoProvider(device);
      final ActivitySelector selector = new ActivitySelector(provider);
      selector.setListener(new ActivitySelector.Listener() {
        @Override
        public void OnLaunch(DeviceInfo.Package pkg, DeviceInfo.Activity act, String name) {
          showTraceDialog(selector, device, pkg, act, runConfig, name);
        }

        @Override
        public void OnCancel() {
          onStop();
        }
      });
      selector.setLocationRelativeTo(owner);
      selector.setTitle("Launch activity...");
      selector.setVisible(true);
      setActiveForm(selector);
    }

    private void showTraceDialog(final Component owner,
                                 final IDevice device,
                                 final DeviceInfo.Package pkg,
                                 final DeviceInfo.Activity act,
                                 final RunConfiguration runConfig,
                                 String name) {
      final TraceDialog dialog = new TraceDialog();
      dialog.setListener(new TraceDialog.Listener() {
        private GfxTracer myTracer = null;

        @Override
        public void onStartTrace(@NotNull String name) {
          GfxTracer.Options options = GfxTracer.Options.fromRunConfiguration(runConfig);
          options.myTraceName = name;
          myTracer = GfxTracer.launch(myView.getProject(), device, pkg, act, options, bindListener(dialog));
        }

        @Override
        public void onStopTrace() {
          myTracer.stop();
          onStop();
        }

        @Override
        public void onCancelTrace() {
          onStop();
        }
      });

      dialog.setLocationRelativeTo(owner);
      // Use the package name as the suggested trace name if none was provided.
      dialog.setDefaultName(name.isEmpty() ? pkg.getDisplayName() : name);
      dialog.setVisible(true);
      setActiveForm(dialog);
      dialog.onBegin();
    }
  }

  public GfxTraceCaptureAction(@NotNull GpuMonitorView view,
                               @Nullable final String text,
                               @Nullable final String description,
                               @Nullable final Icon icon) {
    super(text, description, icon);
    myView = view;
    myText = text;
  }

  @Override
  public boolean isSelected(AnActionEvent e) {
    return sActiveForm != null && sActiveForm == myActiveForm;
  }

  @Override
  public final void setSelected(AnActionEvent e, boolean state) {
    IDevice device = myView.getDeviceContext().getSelectedDevice();
    if (device == null) {
      return; // Button shouldn't be enabled, but let's play safe.
    }
    if (myActiveForm == sActiveForm) {
      if (sActiveForm != null) {
        myActiveForm.setVisible(true); // Bring-to-front
      }
      else {
        Container window = ((JComponent)(e.getInputEvent().getSource())).getTopLevelAncestor();
        start(window, device);
      }
    }
  }

  @Override
  public final void update(AnActionEvent e) {
    super.update(e);
    Presentation presentation = e.getPresentation();
    if (!GapiPaths.isValid()) {
      presentation.setEnabled(false);
      presentation.setText(myText + " : GPU debugger tools not installed");
    }
    else {
      presentation.setEnabled(isEnabled());
      presentation.setText(myText);
    }
  }

  /**
   * Returns the currently selected {@link RunConfiguration}, or null if there
   * is no selected run configuration.
   * <p>
   * Note: The GPU tracer UI doesn't belong in the Android Monitor, instead it should be an trace
   * toggle option in the run configuration settings. Once moved, this method should be deleted.
   */
  @Nullable
  private static RunConfiguration getSelectedRunConfiguration(@NotNull GpuMonitorView view) {
    Project project = view.getProject();
    RunManager runMgr = RunManager.getInstance(project);
    RunnerAndConfigurationSettings selected = runMgr.getSelectedConfiguration();
    if (selected == null) {
      return null;
    }
    return selected.getConfiguration();
  }

  private IDevice getDevice() {
    return myView.getDeviceContext().getSelectedDevice();
  }

  boolean isEnabled() {
    if (sActiveForm == null || sActiveForm == myActiveForm) {
      return getDevice() != null;
    }
    return false;
  }

  protected void setActiveForm(JDialog form) {
    myActiveForm = form;
    sActiveForm = form;
  }

  /**
   * Called by the derived class when the trace has finished.
   */
  protected void onStop() {
    setActiveForm(null);
  }

  /**
   * bindListener returns a {@link GfxTracer.Listener} that will forward update the specified
   * {@link TraceDialog}.
   */
  protected GfxTracer.Listener bindListener(final TraceDialog dialog) {
    return new GfxTracer.Listener() {
      @NotNull private String myCurrentAction = "";
      private long mySizeInBytes = 0;

      @Override
      public void onAction(final @NotNull String name) {
        EdtExecutor.INSTANCE.execute(new Runnable() {
          @Override
          public void run() {
            myCurrentAction = name;
            update();
          }
        });
      }

      @Override
      public void onProgress(final long sizeInBytes) {
        EdtExecutor.INSTANCE.execute(new Runnable() {
          @Override
          public void run() {
            mySizeInBytes = sizeInBytes;
            update();
          }
        });
      }

      @Override
      public void onStopped() {
        dialog.onStop();
      }

      @Override
      public void onError(@NotNull String error) {
        dialog.onError(error);
      }

      private void update() {
        final long KB = 1024;
        final long MB = KB * KB;
        final long GB = MB * KB;

        String details = "";
        if (mySizeInBytes > 0) {
          if (mySizeInBytes < KB) {
            details = String.format("%d bytes", mySizeInBytes);
          }
          else if (mySizeInBytes < MB) {
            details = String.format("%.2f KB", (float)mySizeInBytes / KB);
          }
          else if (mySizeInBytes < GB) {
            details = String.format("%.2f MB", (float)mySizeInBytes / MB);
          }
          else {
            details = String.format("%.2f GB", (float)mySizeInBytes / GB);
          }
        }
        dialog.onProgress(myCurrentAction, details);
      }
    };
  }

  abstract void start(@NotNull Container window, @NotNull IDevice device);
}
