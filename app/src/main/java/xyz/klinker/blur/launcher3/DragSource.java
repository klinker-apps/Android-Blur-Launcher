/*
 * Copyright (C) 2008 The Android Open Source Project
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

package xyz.klinker.blur.launcher3;

import android.view.View;

import xyz.klinker.blur.launcher3.DropTarget.DragObject;
import xyz.klinker.blur.launcher3.logging.UserEventDispatcher.LaunchSourceProvider;
/**
 * Interface defining an object that can originate a drag.
 */
public interface DragSource extends LaunchSourceProvider {

    /**
     * @return whether items dragged from this source supports
     */
    boolean supportsFlingToDelete();

    /**
     * @return whether items dragged from this source supports 'App Info'
     */
    boolean supportsAppInfoDropTarget();

    /**
     * @return whether items dragged from this source supports 'Delete' drop target (e.g. to remove
     * a shortcut.) If this returns false, the drop target will say "Cancel" instead of "Remove."
     */
    boolean supportsDeleteDropTarget();

    /*
     * @return the scale of the icons over the workspace icon size
     */
    float getIntrinsicIconScaleFactor();

    /**
     * A callback specifically made back to the source after an item from this source has been flung
     * to be deleted on a DropTarget.  In such a situation, this method will be called after
     * onDropCompleted, and more importantly, after the fling animation has completed.
     */
    void onFlingToDeleteCompleted();

    /**
     * A callback made back to the source after an item from this source has been dropped on a
     * DropTarget.
     */
    void onDropCompleted(View target, DropTarget.DragObject d, boolean isFlingToDelete, boolean success);
}
