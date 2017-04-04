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

package xyz.klinker.blur.launcher3.accessibility;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.text.TextUtils;
import android.view.View;

import xyz.klinker.blur.launcher3.AppInfo;
import xyz.klinker.blur.launcher3.CellLayout;
import xyz.klinker.blur.launcher3.FolderInfo;
import xyz.klinker.blur.launcher3.ItemInfo;
import xyz.klinker.blur.launcher3.Launcher;
import xyz.klinker.blur.launcher3.accessibility.LauncherAccessibilityDelegate.DragType;
import xyz.klinker.blur.R;
import xyz.klinker.blur.launcher3.ShortcutInfo;
import xyz.klinker.blur.launcher3.dragndrop.DragLayer;

/**
 * Implementation of {@link DragAndDropAccessibilityDelegate} to support DnD on workspace.
 */
public class WorkspaceAccessibilityHelper extends DragAndDropAccessibilityDelegate {

    private final Rect mTempRect = new Rect();
    private final int[] mTempCords = new int[2];

    public WorkspaceAccessibilityHelper(CellLayout layout) {
        super(layout);
    }

    /**
     * Find the virtual view id corresponding to the top left corner of any drop region by which
     * the passed id is contained. For an icon, this is simply
     */
    @Override
    protected int intersectsValidDropTarget(int id) {
        int mCountX = mView.getCountX();
        int mCountY = mView.getCountY();

        int x = id % mCountX;
        int y = id / mCountX;
        LauncherAccessibilityDelegate.DragInfo dragInfo = mDelegate.getDragInfo();

        if (dragInfo.dragType == LauncherAccessibilityDelegate.DragType.WIDGET && mView.isHotseat()) {
            return INVALID_POSITION;
        }

        if (dragInfo.dragType == LauncherAccessibilityDelegate.DragType.WIDGET) {
            // For a widget, every cell must be vacant. In addition, we will return any valid
            // drop target by which the passed id is contained.
            boolean fits = false;

            // These represent the amount that we can back off if we hit a problem. They
            // get consumed as we move up and to the right, trying new regions.
            int spanX = dragInfo.info.spanX;
            int spanY = dragInfo.info.spanY;

            for (int m = 0; m < spanX; m++) {
                for (int n = 0; n < spanY; n++) {

                    fits = true;
                    int x0 = x - m;
                    int y0 = y - n;

                    if (x0 < 0 || y0 < 0) continue;

                    for (int i = x0; i < x0 + spanX; i++) {
                        if (!fits) break;
                        for (int j = y0; j < y0 + spanY; j++) {
                            if (i >= mCountX || j >= mCountY || mView.isOccupied(i, j)) {
                                fits = false;
                                break;
                            }
                        }
                    }
                    if (fits) {
                        return x0 + mCountX * y0;
                    }
                }
            }
            return INVALID_POSITION;
        } else {
            // For an icon, we simply check the view directly below
            View child = mView.getChildAt(x, y);
            if (child == null || child == dragInfo.item) {
                // Empty cell. Good for an icon or folder.
                return id;
            } else if (dragInfo.dragType != LauncherAccessibilityDelegate.DragType.FOLDER) {
                // For icons, we can consider cells that have another icon or a folder.
                ItemInfo info = (ItemInfo) child.getTag();
                if (info instanceof AppInfo || info instanceof FolderInfo ||
                        info instanceof ShortcutInfo) {
                    return id;
                }
            }
            return INVALID_POSITION;
        }
    }

    @Override
    protected String getConfirmationForIconDrop(int id) {
        int x = id % mView.getCountX();
        int y = id / mView.getCountX();
        LauncherAccessibilityDelegate.DragInfo dragInfo = mDelegate.getDragInfo();

        View child = mView.getChildAt(x, y);
        if (child == null || child == dragInfo.item) {
            return mContext.getString(R.string.item_moved);
        } else {
            ItemInfo info = (ItemInfo) child.getTag();
            if (info instanceof AppInfo || info instanceof ShortcutInfo) {
                return mContext.getString(R.string.folder_created);

            } else if (info instanceof FolderInfo) {
                return mContext.getString(R.string.added_to_folder);
            }
        }
        return "";
    }

    @Override
    protected void onPopulateNodeForVirtualView(int id, AccessibilityNodeInfoCompat node) {
        super.onPopulateNodeForVirtualView(id, node);


        // ExploreByTouchHelper does not currently handle view scale.
        // Update BoundsInScreen to appropriate value.
        DragLayer dragLayer = Launcher.getLauncher(mView.getContext()).getDragLayer();
        mTempCords[0] = mTempCords[1] = 0;
        float scale = dragLayer.getDescendantCoordRelativeToSelf(mView, mTempCords);

        node.getBoundsInParent(mTempRect);
        mTempRect.left = mTempCords[0] + (int) (mTempRect.left * scale);
        mTempRect.right = mTempCords[0] + (int) (mTempRect.right * scale);
        mTempRect.top = mTempCords[1] + (int) (mTempRect.top * scale);
        mTempRect.bottom = mTempCords[1] + (int) (mTempRect.bottom * scale);
        node.setBoundsInScreen(mTempRect);
    }

    @Override
    protected String getLocationDescriptionForIconDrop(int id) {
        int x = id % mView.getCountX();
        int y = id / mView.getCountX();
        LauncherAccessibilityDelegate.DragInfo dragInfo = mDelegate.getDragInfo();

        View child = mView.getChildAt(x, y);
        if (child == null || child == dragInfo.item) {
            if (mView.isHotseat()) {
                return mContext.getString(R.string.move_to_hotseat_position, id + 1);
            } else {
                return mContext.getString(R.string.move_to_empty_cell, y + 1, x + 1);
            }
        } else {
            return getDescriptionForDropOver(child, mContext);
        }
    }

    public static String getDescriptionForDropOver(View overChild, Context context) {
        ItemInfo info = (ItemInfo) overChild.getTag();
        if (info instanceof ShortcutInfo) {
            return context.getString(R.string.create_folder_with, info.title);
        } else if (info instanceof FolderInfo) {
            if (TextUtils.isEmpty(info.title)) {
                // Find the first item in the folder.
                FolderInfo folder = (FolderInfo) info;
                ShortcutInfo firstItem = null;
                for (ShortcutInfo shortcut : folder.contents) {
                    if (firstItem == null || firstItem.rank > shortcut.rank) {
                        firstItem = shortcut;
                    }
                }

                if (firstItem != null) {
                    return context.getString(R.string.add_to_folder_with_app, firstItem.title);
                }
            }
            return context.getString(R.string.add_to_folder, info.title);
        }
        return "";
    }
}
