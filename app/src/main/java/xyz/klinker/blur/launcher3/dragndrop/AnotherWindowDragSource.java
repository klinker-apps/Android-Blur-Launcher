/*
 * Copyright (C) 2016 The Android Open Source Project
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

package xyz.klinker.blur.launcher3.dragndrop;

import android.content.Context;
import android.view.View;

import xyz.klinker.blur.launcher3.DragSource;
import xyz.klinker.blur.launcher3.DropTarget.DragObject;
import xyz.klinker.blur.launcher3.ItemInfo;
import xyz.klinker.blur.launcher3.Launcher;
import xyz.klinker.blur.launcher3.userevent.nano.LauncherLogProto.Target;

/**
 * DragSource used when the drag started at another window.
 */
public class AnotherWindowDragSource implements DragSource {

    private final Context mContext;

    AnotherWindowDragSource(Context context) {
        mContext = context;
    }

    @Override
    public boolean supportsFlingToDelete() {
        return false;
    }

    @Override
    public boolean supportsAppInfoDropTarget() {
        return false;
    }

    @Override
    public boolean supportsDeleteDropTarget() {
        return false;
    }

    @Override
    public float getIntrinsicIconScaleFactor() {
        return 1;
    }

    @Override
    public void onFlingToDeleteCompleted() {
    }

    @Override
    public void onDropCompleted(View target, DragObject d,
            boolean isFlingToDelete, boolean success) {
        if (!success) {
            Launcher.getLauncher(mContext).exitSpringLoadedDragModeDelayed(false, 0, null);
        }

    }

    @Override
    public void fillInLaunchSourceData(View v, ItemInfo info, Target target, Target targetParent) {
        // TODO: Probably log something
    }
}
