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

package com.android.tools.idea.sdk.remote.internal;

import com.android.tools.idea.sdk.remote.internal.DownloadCache;

/**
 * Exception thrown by {@link DownloadCache} and {@link com.android.tools.idea.sdk.remote.internal.UrlOpener} when a user
 * cancels an HTTP Basic authentication or NTML authentication dialog.
 */
public class CanceledByUserException extends Exception {
    private static final long serialVersionUID = -7669346110926032403L;

    public CanceledByUserException(String message) {
        super(message);
    }
}
