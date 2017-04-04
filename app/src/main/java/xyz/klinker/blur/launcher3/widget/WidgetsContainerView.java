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

package xyz.klinker.blur.launcher3.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import xyz.klinker.blur.launcher3.BaseContainerView;
import xyz.klinker.blur.launcher3.CellLayout;
import xyz.klinker.blur.launcher3.DeleteDropTarget;
import xyz.klinker.blur.launcher3.DragSource;
import xyz.klinker.blur.launcher3.DropTarget;
import xyz.klinker.blur.launcher3.DropTarget.DragObject;
import xyz.klinker.blur.launcher3.dragndrop.DragOptions;
import xyz.klinker.blur.launcher3.folder.Folder;
import xyz.klinker.blur.launcher3.IconCache;
import xyz.klinker.blur.launcher3.ItemInfo;
import xyz.klinker.blur.launcher3.Launcher;
import xyz.klinker.blur.launcher3.LauncherAppState;
import xyz.klinker.blur.launcher3.PendingAddItemInfo;
import xyz.klinker.blur.R;
import xyz.klinker.blur.launcher3.Utilities;
import xyz.klinker.blur.launcher3.WidgetPreviewLoader;
import xyz.klinker.blur.launcher3.Workspace;
import xyz.klinker.blur.launcher3.dragndrop.DragController;
import xyz.klinker.blur.launcher3.model.WidgetsModel;
import xyz.klinker.blur.launcher3.userevent.nano.LauncherLogProto;
import xyz.klinker.blur.launcher3.userevent.nano.LauncherLogProto.Target;
import xyz.klinker.blur.launcher3.util.Thunk;
import xyz.klinker.blur.launcher3.util.TransformingTouchDelegate;

/**
 * The widgets list view container.
 */
public class WidgetsContainerView extends BaseContainerView
        implements View.OnLongClickListener, View.OnClickListener, DragSource {
    private static final String TAG = "WidgetsContainerView";
    private static final boolean LOGD = false;

    /* Global instances that are used inside this container. */
    @Thunk
    Launcher mLauncher;
    private DragController mDragController;
    private IconCache mIconCache;

    private final Rect mTmpBgPaddingRect = new Rect();

    /* Recycler view related member variables */
    private WidgetsRecyclerView mRecyclerView;
    private WidgetsListAdapter mAdapter;
    private TransformingTouchDelegate mRecyclerViewTouchDelegate;

    /* Touch handling related member variables. */
    private Toast mWidgetInstructionToast;

    /* Rendering related. */
    private WidgetPreviewLoader mWidgetPreviewLoader;

    public WidgetsContainerView(Context context) {
        this(context, null);
    }

    public WidgetsContainerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WidgetsContainerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mLauncher = Launcher.getLauncher(context);
        mDragController = mLauncher.getDragController();
        mAdapter = new WidgetsListAdapter(this, this, context);
        mIconCache = (LauncherAppState.getInstance()).getIconCache();
        if (LOGD) {
            Log.d(TAG, "WidgetsContainerView constructor");
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        getRevealView().getBackground().getPadding(mTmpBgPaddingRect);
        mRecyclerViewTouchDelegate.setBounds(
                mRecyclerView.getLeft() - mTmpBgPaddingRect.left,
                mRecyclerView.getTop() - mTmpBgPaddingRect.top,
                mRecyclerView.getRight() + mTmpBgPaddingRect.right,
                mRecyclerView.getBottom() + mTmpBgPaddingRect.bottom);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mRecyclerView = (WidgetsRecyclerView) getContentView().findViewById(R.id.widgets_list_view);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerViewTouchDelegate = new TransformingTouchDelegate(mRecyclerView);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((View) mRecyclerView.getParent()).setTouchDelegate(mRecyclerViewTouchDelegate);
    }

    //
    // Returns views used for launcher transitions.
    //

    public void scrollToTop() {
        mRecyclerView.scrollToPosition(0);
    }

    //
    // Touch related handling.
    //

    @Override
    public void onClick(View v) {
        // When we have exited widget tray or are in transition, disregard clicks
        if (!mLauncher.isWidgetsViewVisible()
                || mLauncher.getWorkspace().isSwitchingState()
                || !(v instanceof WidgetCell)) return;

        // Let the user know that they have to long press to add a widget
        if (mWidgetInstructionToast != null) {
            mWidgetInstructionToast.cancel();
        }

        CharSequence msg = Utilities.wrapForTts(
                getContext().getText(R.string.long_press_widget_to_add),
                getContext().getString(R.string.long_accessible_way_to_add));
        mWidgetInstructionToast = Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT);
        mWidgetInstructionToast.show();
    }

    @Override
    public boolean onLongClick(View v) {
        if (LOGD) {
            Log.d(TAG, String.format("onLonglick [v=%s]", v));
        }
        // Return early if this is not initiated from a touch
        if (!v.isInTouchMode()) return false;
        // When we have exited all apps or are in transition, disregard long clicks
        if (!mLauncher.isWidgetsViewVisible() ||
                mLauncher.getWorkspace().isSwitchingState()) return false;
        // Return if global dragging is not enabled
        if (!mLauncher.isDraggingEnabled()) return false;

        boolean status = beginDragging(v);
        if (status && v.getTag() instanceof PendingAddWidgetInfo) {
            WidgetHostViewLoader hostLoader = new WidgetHostViewLoader(mLauncher, v);
            boolean preloadStatus = hostLoader.preloadWidget();
            if (LOGD) {
                Log.d(TAG, String.format("preloading widget [status=%s]", preloadStatus));
            }
            mLauncher.getDragController().addDragListener(hostLoader);
        }
        return status;
    }

    private boolean beginDragging(View v) {
        if (v instanceof WidgetCell) {
            if (!beginDraggingWidget((WidgetCell) v)) {
                return false;
            }
        } else {
            Log.e(TAG, "Unexpected dragging view: " + v);
        }

        // We don't enter spring-loaded mode if the drag has been cancelled
        if (mLauncher.getDragController().isDragging()) {
            // Go into spring loaded mode (must happen before we startDrag())
            mLauncher.enterSpringLoadedDragMode();
        }

        return true;
    }

    private boolean beginDraggingWidget(WidgetCell v) {
        // Get the widget preview as the drag representation
        WidgetImageView image = (WidgetImageView) v.findViewById(R.id.widget_preview);
        PendingAddItemInfo createItemInfo = (PendingAddItemInfo) v.getTag();

        // If the ImageView doesn't have a drawable yet, the widget preview hasn't been loaded and
        // we abort the drag.
        if (image.getBitmap() == null) {
            return false;
        }

        // Compose the drag image
        Bitmap preview;
        final float scale;
        final Rect bounds = image.getBitmapBounds();

        if (createItemInfo instanceof PendingAddWidgetInfo) {
            // This can happen in some weird cases involving multi-touch. We can't start dragging
            // the widget if this is null, so we break out.

            PendingAddWidgetInfo createWidgetInfo = (PendingAddWidgetInfo) createItemInfo;
            int[] size = mLauncher.getWorkspace().estimateItemSize(createWidgetInfo, true);

            Bitmap icon = image.getBitmap();
            float minScale = 1.25f;
            int maxWidth = Math.min((int) (icon.getWidth() * minScale), size[0]);

            int[] previewSizeBeforeScale = new int[1];
            preview = getWidgetPreviewLoader().generateWidgetPreview(mLauncher,
                    createWidgetInfo.info, maxWidth, null, previewSizeBeforeScale);

            if (previewSizeBeforeScale[0] < icon.getWidth()) {
                // The icon has extra padding around it.
                int padding = (icon.getWidth() - previewSizeBeforeScale[0]) / 2;
                if (icon.getWidth() > image.getWidth()) {
                    padding = padding * image.getWidth() / icon.getWidth();
                }

                bounds.left += padding;
                bounds.right -= padding;
            }
            scale = bounds.width() / (float) preview.getWidth();
        } else {
            PendingAddShortcutInfo createShortcutInfo = (PendingAddShortcutInfo) v.getTag();
            Drawable icon = mIconCache.getFullResIcon(createShortcutInfo.activityInfo);
            preview = Utilities.createIconBitmap(icon, mLauncher);
            createItemInfo.spanX = createItemInfo.spanY = 1;
            scale = ((float) mLauncher.getDeviceProfile().iconSizePx) / preview.getWidth();
        }

        // Since we are not going through the workspace for starting the drag, set drag related
        // information on the workspace before starting the drag.
        mLauncher.getWorkspace().prepareDragWithProvider(
                new PendingItemPreviewProvider(v, createItemInfo, preview));

        // Start the drag
        mDragController.startDrag(image, preview, this, createItemInfo,
                bounds, scale, new DragOptions());
        return true;
    }

    //
    // Drag related handling methods that implement {@link DragSource} interface.
    //

    @Override
    public boolean supportsFlingToDelete() {
        return true;
    }

    @Override
    public boolean supportsAppInfoDropTarget() {
        return true;
    }

    /*
     * Both this method and {@link #supportsFlingToDelete} has to return {@code false} for the
     * {@link DeleteDropTarget} to be invisible.)
     */
    @Override
    public boolean supportsDeleteDropTarget() {
        return false;
    }

    @Override
    public float getIntrinsicIconScaleFactor() {
        return 0;
    }

    @Override
    public void onFlingToDeleteCompleted() {
        // We just dismiss the drag when we fling, so cleanup here
        mLauncher.exitSpringLoadedDragModeDelayed(true,
                Launcher.EXIT_SPRINGLOADED_MODE_SHORT_TIMEOUT, null);
        mLauncher.unlockScreenOrientation(false);
    }

    @Override
    public void onDropCompleted(View target, DropTarget.DragObject d, boolean isFlingToDelete,
                                boolean success) {
        if (LOGD) {
            Log.d(TAG, "onDropCompleted");
        }
        if (isFlingToDelete || !success || (target != mLauncher.getWorkspace() &&
                !(target instanceof DeleteDropTarget) && !(target instanceof Folder))) {
            // Exit spring loaded mode if we have not successfully dropped or have not handled the
            // drop in Workspace
            mLauncher.exitSpringLoadedDragModeDelayed(true,
                    Launcher.EXIT_SPRINGLOADED_MODE_SHORT_TIMEOUT, null);
        }
        mLauncher.unlockScreenOrientation(false);

        // Display an error message if the drag failed due to there not being enough space on the
        // target layout we were dropping on.
        if (!success) {
            boolean showOutOfSpaceMessage = false;
            if (target instanceof Workspace) {
                int currentScreen = mLauncher.getCurrentWorkspaceScreen();
                Workspace workspace = (Workspace) target;
                CellLayout layout = (CellLayout) workspace.getChildAt(currentScreen);
                ItemInfo itemInfo = d.dragInfo;
                if (layout != null) {
                    showOutOfSpaceMessage =
                            !layout.findCellForSpan(null, itemInfo.spanX, itemInfo.spanY);
                }
            }
            if (showOutOfSpaceMessage) {
                mLauncher.showOutOfSpaceMessage(false);
            }
            d.deferDragViewCleanupPostAnimation = false;
        }
    }

    /**
     * Initialize the widget data model.
     */
    public void addWidgets(WidgetsModel model) {
        mRecyclerView.setWidgets(model);
        mAdapter.setWidgetsModel(model);
        mAdapter.notifyDataSetChanged();

        View loader = getContentView().findViewById(R.id.loader);
        if (loader != null) {
            ((ViewGroup) getContentView()).removeView(loader);
        }
    }

    public boolean isEmpty() {
        return mAdapter.getItemCount() == 0;
    }

    private WidgetPreviewLoader getWidgetPreviewLoader() {
        if (mWidgetPreviewLoader == null) {
            mWidgetPreviewLoader = LauncherAppState.getInstance().getWidgetCache();
        }
        return mWidgetPreviewLoader;
    }

    @Override
    public void fillInLaunchSourceData(View v, ItemInfo info, Target target, Target targetParent) {
        targetParent.containerType = LauncherLogProto.WIDGETS;
    }
}