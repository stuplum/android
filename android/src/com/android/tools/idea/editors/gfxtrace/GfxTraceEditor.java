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
package com.android.tools.idea.editors.gfxtrace;

import com.android.tools.idea.editors.gfxtrace.controllers.MainController;
import com.android.tools.idea.editors.gfxtrace.gapi.GapisConnection;
import com.android.tools.idea.editors.gfxtrace.gapi.GapisFeatures;
import com.android.tools.idea.editors.gfxtrace.gapi.GapisProcess;
import com.android.tools.idea.editors.gfxtrace.gapi.GapiPaths;
import com.android.tools.idea.editors.gfxtrace.service.*;
import com.android.tools.idea.editors.gfxtrace.service.atom.AtomMetadata;
import com.android.tools.idea.editors.gfxtrace.service.path.*;
import com.android.tools.idea.editors.gfxtrace.service.stringtable.Info;
import com.android.tools.idea.editors.gfxtrace.service.stringtable.StringTable;
import com.android.tools.rpclib.rpccore.Rpc;
import com.android.tools.rpclib.rpccore.RpcException;
import com.android.tools.rpclib.schema.ConstantSet;
import com.android.tools.rpclib.schema.Dynamic;
import com.android.tools.rpclib.schema.Message;
import com.android.tools.rpclib.schema.Entity;
import com.google.common.util.concurrent.*;
import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.icons.AllIcons;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.LoadingDecorator;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.StandardFileSystems;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.panels.NonOpaquePanel;
import com.intellij.util.ui.AsyncProcessIcon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class GfxTraceEditor extends UserDataHolderBase implements FileEditor {
  @NotNull public static final String LOADING_CAPTURE = "Loading capture...";
  @NotNull public static final String SELECT_ATOM = "Select a frame or command";
  @NotNull public static final String SELECT_MEMORY = "Select a memory range in the command list";
  @NotNull public static final String SELECT_TEXTURE = "Select a texture";
  @NotNull public static final String NO_TEXTURES = "No textures have been created by this point";

  @NotNull private static final Logger LOG = Logger.getInstance(GfxTraceEditor.class);

  private static final int FETCH_SCHEMA_TIMEOUT_MS = 3000;
  private static final int FETCH_FEATURES_TIMEOUT_MS = 3000;
  private static final int FETCH_STRING_TABLE_TIMEOUT_MS = 3000;
  private static final int FETCH_REPLAY_DEVICE_TIMEOUT_MS = 3000;
  private static final int FETCH_REPLAY_DEVICE_RETRY_DELAY_MS = 3000;
  private static final int FETCH_REPLAY_DEVICE_MAX_RETRIES = 30;
  private static final int FETCH_TRACE_TIMEOUT_MS = 30000;

  @NotNull private static final String ERR_INIT_GAPIS_CONNECTION = "Error communicating with the graphics server";

  @NotNull private final Project myProject;
  @NotNull private TraceLoadingDecorator myLoadingDecorator;
  @NotNull private JBPanel myView = new JBPanel(new BorderLayout());
  @NotNull private final ListeningExecutorService myExecutor = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
  private GapisConnection myGapisConnection;
  private ServiceClient myClient;

  @NotNull private List<PathListener> myPathListeners = new ArrayList<PathListener>();
  @NotNull private PathStore<Path> myLastActivatadPath = new PathStore<Path>();

  public static boolean isEnabled() {
    return true;
  }

  public GfxTraceEditor(@NotNull final Project project, @SuppressWarnings("UnusedParameters") @NotNull final VirtualFile file) {
    myProject = project;
    myLoadingDecorator = new TraceLoadingDecorator(myView, this, 0);
    myLoadingDecorator.setLoadingText("Initializing GFX Trace System");
    myLoadingDecorator.startLoading(false);

    final JComponent mainUi = MainController.createUI(GfxTraceEditor.this);

    // Attempt to start/connect to the server on a separate thread to reduce the IDE from stalling.
    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      @Override
      public void run() {
        if (!isEnabled()) {
          setLoadingErrorTextOnEdt("GFX Trace System not enabled on this host");
          return;
        }

        if (!GapiPaths.isValid()) {
          setLoadingErrorTextOnEdt("GPU debugging SDK not installed");
          return;
        }

        if (!connectToServer()) {
          setLoadingErrorTextOnEdt("Unable to connect to server");
          return;
        }

        try {
          myClient = new ServiceClientCache(myGapisConnection.createServiceClient(myExecutor), myExecutor);
        }
        catch (IOException e) {
          setLoadingErrorTextOnEdt("Unable to talk to server");
          return;
        }

        GapisFeatures features = myGapisConnection.getFeatures();

        String status = "";
        try {
          status = "fetch schema";
          fetchSchema();

          status = "fetch feature list";
          fetchFeatures(features);

          if (features.hasRpcStringTables()) {
            status = "fetch string table";
            fetchStringTable();
          }

          status = "fetch replay device list";
          fetchReplayDevice();

          status = "load trace";
          fetchTrace(file);

          ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
              myView.add(mainUi, BorderLayout.CENTER);
              myLoadingDecorator.stopLoading();
            }
          });
        }
        catch (Exception e) {
          LOG.error("Failed to " + status, e);
          setLoadingErrorTextOnEdt(ERR_INIT_GAPIS_CONNECTION);
          return;
        }
      }
    });
  }

  /**
   * Requests and blocks for the schema from the server.
   */
  private void fetchSchema() throws ExecutionException, RpcException, TimeoutException {
    Message schema = Rpc.get(myClient.getSchema(), FETCH_SCHEMA_TIMEOUT_MS, TimeUnit.MILLISECONDS);
    LOG.info("Schema with " + schema.entities.length + " classes, " + schema.constants.length + " constant sets");
    int atoms = 0;
    for (Entity type : schema.entities) {
      // Find the atom metadata, if present
      if (AtomMetadata.find(type) != null) {
        atoms++;
      }
      Dynamic.register(type);
    }
    LOG.info("Schema with " + atoms + " atoms");
    for (ConstantSet set : schema.constants) {
      ConstantSet.register(set);
    }
  }

  /**
   * Requests and blocks for the features list from the server.
   */
  private void fetchFeatures(GapisFeatures features) throws ExecutionException, RpcException, TimeoutException {
    String[] list = Rpc.get(myClient.getFeatures(), FETCH_FEATURES_TIMEOUT_MS, TimeUnit.MILLISECONDS);
    features.setFeatureList(list);
    LOG.info("GAPIS features: " + list.toString());
  }

  /**
   * Requests, blocks, and then makes current the string table from the server.
   */
  private void fetchStringTable() throws ExecutionException, RpcException, TimeoutException {
    Info[] infos = Rpc.get(myClient.getAvailableStringTables(), FETCH_STRING_TABLE_TIMEOUT_MS, TimeUnit.MILLISECONDS);
    if (infos.length == 0) {
      LOG.warn("No string tables available");
      return;
    }
    Info info = infos[0];
    StringTable table = Rpc.get(myClient.getStringTable(info), FETCH_STRING_TABLE_TIMEOUT_MS, TimeUnit.MILLISECONDS);
    table.setCurrent();
  }

  /**
   * Requests and blocks for the schema from the server.
   */
  private void fetchReplayDevice() throws ExecutionException, RpcException, TimeoutException {
    for (int i = 0; i < FETCH_REPLAY_DEVICE_MAX_RETRIES; i++) {
      DevicePath[] devices = Rpc.get(getClient().getDevices(), FETCH_REPLAY_DEVICE_TIMEOUT_MS, TimeUnit.MILLISECONDS);
      if (devices != null && devices.length >= 1) {
        activatePath(devices[0], GfxTraceEditor.this);
        return;
      }
      try {
        Thread.sleep(FETCH_REPLAY_DEVICE_RETRY_DELAY_MS);
      }
      catch (InterruptedException e) {
      }
    }
    throw new RuntimeException("Couldn't find replay device");
  }

  /**
   * Uploads or requests the capture path from the server and then activates the path.
   */
  private void fetchTrace(VirtualFile file) throws ExecutionException, RpcException, TimeoutException, IOException {
    final ListenableFuture<CapturePath> captureF;
    if (file.getFileSystem().getProtocol().equals(StandardFileSystems.FILE_PROTOCOL)) {
      LOG.info("Load gfxtrace in " + file.getPresentableName());
      if (file.getLength() == 0) {
        throw new RuntimeException("Empty trace file");
      }
      captureF = myClient.loadCapture(file.getCanonicalPath());
    }
    else {
      // Upload the trace file
      byte[] data = file.contentsToByteArray();
      LOG.info("Upload " + data.length + " bytes of gfxtrace as " + file.getPresentableName());
      if (data.length == 0) {
        throw new RuntimeException("Empty trace file");
      }
      captureF = myClient.importCapture(file.getPresentableName(), data);
    }

    CapturePath path = Rpc.get(captureF, FETCH_TRACE_TIMEOUT_MS, TimeUnit.MILLISECONDS);
    if (path == null) {
      throw new RuntimeException("Invalid capture file " + file.getPresentableName());
    }

    activatePath(path, GfxTraceEditor.this);
  }

  @NotNull
  public Project getProject() {
    return myProject;
  }

  @NotNull
  @Override
  public JComponent getComponent() {
    return myLoadingDecorator.getComponent();
  }

  @Nullable
  @Override
  public JComponent getPreferredFocusedComponent() {
    return null;
  }

  @NotNull
  @Override
  public String getName() {
    return "GfxTraceView";
  }

  public void activatePath(@NotNull final Path path, final Object source) {
    synchronized (myLastActivatadPath) {
      if (!myLastActivatadPath.update(path)) {
        return;
      }
    }

    final PathListener.PathEvent event = new PathListener.PathEvent(path, source);
    // All path notifications are executed in the editor thread
    Runnable eventDispatch = new Runnable() {
      @Override
      public void run() {
        LOG.info("Activate path " + path + ", source: " + source.getClass().getName());
        for (PathListener listener : myPathListeners) {
          listener.notifyPath(event);
        }
      }
    };
    Application application = ApplicationManager.getApplication();
    if (application.isDispatchThread()) {
      eventDispatch.run();
    } else {
      application.invokeLater(eventDispatch);
    }
  }

  public void addPathListener(@NotNull PathListener listener) {
    myPathListeners.add(listener);
  }

  @NotNull
  @Override
  public FileEditorState getState(@NotNull FileEditorStateLevel level) {
    return FileEditorState.INSTANCE;
  }

  @Override
  public void setState(@NotNull FileEditorState state) {

  }

  @Override
  public boolean isModified() {
    return false;
  }

  @Override
  public boolean isValid() {
    return true;
  }

  @Override
  public void selectNotify() {
  }

  @Override
  public void deselectNotify() {
  }

  @Override
  public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {
  }

  @Override
  public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {
  }

  @Nullable
  @Override
  public BackgroundEditorHighlighter getBackgroundHighlighter() {
    return null;
  }

  @Nullable
  @Override
  public FileEditorLocation getCurrentLocation() {
    return null;
  }

  @Nullable
  @Override
  public StructureViewBuilder getStructureViewBuilder() {
    return null;
  }

  @NotNull
  public ServiceClient getClient() {
    return myClient;
  }

  @NotNull
  public ListeningExecutorService getExecutor() {
    return myExecutor;
  }

  @Override
  public void dispose() {
    shutdown();
  }

  private boolean connectToServer() {
    assert !ApplicationManager.getApplication().isDispatchThread();

    myGapisConnection = GapisProcess.connect();
    return myGapisConnection.isConnected();
  }

  private void setLoadingErrorTextOnEdt(@NotNull final String error) {
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      @Override
      public void run() {
        myLoadingDecorator.setErrorMessage(error);
      }
    });
  }

  private void shutdown() {
    if (myGapisConnection != null) {
      myGapisConnection.close();
      myGapisConnection = null;
    }

    myExecutor.shutdown();
  }

  private static class TraceLoadingDecorator extends LoadingDecorator {
    private JPanel iconPanel;

    public TraceLoadingDecorator(JComponent content, @NotNull Disposable parent, int startDelayMs) {
      super(content, parent, startDelayMs);
    }

    @Override
    protected NonOpaquePanel customizeLoadingLayer(JPanel parent, JLabel text, final AsyncProcessIcon icon) {
      NonOpaquePanel result = super.customizeLoadingLayer(parent, text, icon);

      // Replace the icon with a panel where we can switch it out.
      result.remove(0);
      result.add(iconPanel = new JPanel(), 0);
      iconPanel.add(icon);

      return result;
    }

    public void setErrorMessage(String text) {
      iconPanel.removeAll();
      iconPanel.add(new JBLabel(AllIcons.General.ErrorDialog));
      setLoadingText(text);
      startLoading(false);
    }
  }
}
