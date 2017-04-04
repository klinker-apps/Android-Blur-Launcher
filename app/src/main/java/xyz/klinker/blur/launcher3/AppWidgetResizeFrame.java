package xyz.klinker.blur.launcher3;

import xyz.klinker.blur.launcher3.dragndrop.DragLayer;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import xyz.klinker.blur.R;
import xyz.klinker.blur.launcher3.accessibility.DragViewStateAnnouncer;
import xyz.klinker.blur.launcher3.util.FocusLogic;

public class AppWidgetResizeFrame extends FrameLayout implements View.OnKeyListener {
    private static final int SNAP_DURATION = 150;
    private static final float DIMMED_HANDLE_ALPHA = 0f;
    private static final float RESIZE_THRESHOLD = 0.66f;

    private static final Rect sTmpRect = new Rect();

    // Represents the cell size on the grid in the two orientations.
    private static Point[] sCellSize;

    private final Launcher mLauncher;
    private final LauncherAppWidgetHostView mWidgetView;
    private final CellLayout mCellLayout;
    private final DragLayer mDragLayer;

    private final ImageView mLeftHandle;
    private final ImageView mRightHandle;
    private final ImageView mTopHandle;
    private final ImageView mBottomHandle;

    private final Rect mWidgetPadding;

    private final int mBackgroundPadding;
    private final int mTouchTargetWidth;

    private final int[] mDirectionVector = new int[2];
    private final int[] mLastDirectionVector = new int[2];
    private final int[] mTmpPt = new int[2];

    private final DragViewStateAnnouncer mStateAnnouncer;

    private boolean mLeftBorderActive;
    private boolean mRightBorderActive;
    private boolean mTopBorderActive;
    private boolean mBottomBorderActive;

    private int mBaselineWidth;
    private int mBaselineHeight;
    private int mBaselineX;
    private int mBaselineY;
    private int mResizeMode;

    private int mRunningHInc;
    private int mRunningVInc;
    private int mMinHSpan;
    private int mMinVSpan;
    private int mDeltaX;
    private int mDeltaY;
    private int mDeltaXAddOn;
    private int mDeltaYAddOn;

    private int mTopTouchRegionAdjustment = 0;
    private int mBottomTouchRegionAdjustment = 0;

    public AppWidgetResizeFrame(Context context,
            LauncherAppWidgetHostView widgetView, CellLayout cellLayout, DragLayer dragLayer) {

        super(context);
        mLauncher = Launcher.getLauncher(context);
        mCellLayout = cellLayout;
        mWidgetView = widgetView;
        LauncherAppWidgetProviderInfo info = (LauncherAppWidgetProviderInfo)
                widgetView.getAppWidgetInfo();
        mResizeMode = info.resizeMode;
        mDragLayer = dragLayer;

        mMinHSpan = info.minSpanX;
        mMinVSpan = info.minSpanY;

        mStateAnnouncer = DragViewStateAnnouncer.createFor(this);

        setBackgroundResource(R.drawable.widget_resize_shadow);
        setForeground(getResources().getDrawable(R.drawable.widget_resize_frame));
        setPadding(0, 0, 0, 0);

        final int handleMargin = getResources().getDimensionPixelSize(R.dimen.widget_handle_margin);
        LayoutParams lp;
        mLeftHandle = new ImageView(context);
        mLeftHandle.setImageResource(R.drawable.ic_widget_resize_handle);
        lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                Gravity.LEFT | Gravity.CENTER_VERTICAL);
        lp.leftMargin = handleMargin;
        addView(mLeftHandle, lp);

        mRightHandle = new ImageView(context);
        mRightHandle.setImageResource(R.drawable.ic_widget_resize_handle);
        lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        lp.rightMargin = handleMargin;
        addView(mRightHandle, lp);

        mTopHandle = new ImageView(context);
        mTopHandle.setImageResource(R.drawable.ic_widget_resize_handle);
        lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                Gravity.CENTER_HORIZONTAL | Gravity.TOP);
        lp.topMargin = handleMargin;
        addView(mTopHandle, lp);

        mBottomHandle = new ImageView(context);
        mBottomHandle.setImageResource(R.drawable.ic_widget_resize_handle);
        lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
        lp.bottomMargin = handleMargin;
        addView(mBottomHandle, lp);

        if (!info.isCustomWidget) {
            mWidgetPadding = AppWidgetHostView.getDefaultPaddingForWidget(context,
                    widgetView.getAppWidgetInfo().provider, null);
        } else {
            Resources r = context.getResources();
            int padding = r.getDimensionPixelSize(R.dimen.default_widget_padding);
            mWidgetPadding = new Rect(padding, padding, padding, padding);
        }

        if (mResizeMode == AppWidgetProviderInfo.RESIZE_HORIZONTAL) {
            mTopHandle.setVisibility(GONE);
            mBottomHandle.setVisibility(GONE);
        } else if (mResizeMode == AppWidgetProviderInfo.RESIZE_VERTICAL) {
            mLeftHandle.setVisibility(GONE);
            mRightHandle.setVisibility(GONE);
        }

        mBackgroundPadding = getResources()
                .getDimensionPixelSize(R.dimen.resize_frame_background_padding);
        mTouchTargetWidth = 2 * mBackgroundPadding;

        // When we create the resize frame, we first mark all cells as unoccupied. The appropriate
        // cells (same if not resized, or different) will be marked as occupied when the resize
        // frame is dismissed.
        mCellLayout.markCellsAsUnoccupiedForView(mWidgetView);

        setOnKeyListener(this);
    }

    public boolean beginResizeIfPointInRegion(int x, int y) {
        boolean horizontalActive = (mResizeMode & AppWidgetProviderInfo.RESIZE_HORIZONTAL) != 0;
        boolean verticalActive = (mResizeMode & AppWidgetProviderInfo.RESIZE_VERTICAL) != 0;

        mLeftBorderActive = (x < mTouchTargetWidth) && horizontalActive;
        mRightBorderActive = (x > getWidth() - mTouchTargetWidth) && horizontalActive;
        mTopBorderActive = (y < mTouchTargetWidth + mTopTouchRegionAdjustment) && verticalActive;
        mBottomBorderActive = (y > getHeight() - mTouchTargetWidth + mBottomTouchRegionAdjustment)
                && verticalActive;

        boolean anyBordersActive = mLeftBorderActive || mRightBorderActive
                || mTopBorderActive || mBottomBorderActive;

        mBaselineWidth = getMeasuredWidth();
        mBaselineHeight = getMeasuredHeight();
        mBaselineX = getLeft();
        mBaselineY = getTop();

        if (anyBordersActive) {
            mLeftHandle.setAlpha(mLeftBorderActive ? 1.0f : DIMMED_HANDLE_ALPHA);
            mRightHandle.setAlpha(mRightBorderActive ? 1.0f :DIMMED_HANDLE_ALPHA);
            mTopHandle.setAlpha(mTopBorderActive ? 1.0f : DIMMED_HANDLE_ALPHA);
            mBottomHandle.setAlpha(mBottomBorderActive ? 1.0f : DIMMED_HANDLE_ALPHA);
        }
        return anyBordersActive;
    }

    /**
     *  Here we bound the deltas such that the frame cannot be stretched beyond the extents
     *  of the CellLayout, and such that the frame's borders can't cross.
     */
    public void updateDeltas(int deltaX, int deltaY) {
        if (mLeftBorderActive) {
            mDeltaX = Math.max(-mBaselineX, deltaX); 
            mDeltaX = Math.min(mBaselineWidth - 2 * mTouchTargetWidth, mDeltaX);
        } else if (mRightBorderActive) {
            mDeltaX = Math.min(mDragLayer.getWidth() - (mBaselineX + mBaselineWidth), deltaX);
            mDeltaX = Math.max(-mBaselineWidth + 2 * mTouchTargetWidth, mDeltaX);
        }

        if (mTopBorderActive) {
            mDeltaY = Math.max(-mBaselineY, deltaY);
            mDeltaY = Math.min(mBaselineHeight - 2 * mTouchTargetWidth, mDeltaY);
        } else if (mBottomBorderActive) {
            mDeltaY = Math.min(mDragLayer.getHeight() - (mBaselineY + mBaselineHeight), deltaY);
            mDeltaY = Math.max(-mBaselineHeight + 2 * mTouchTargetWidth, mDeltaY);
        }
    }

    public void visualizeResizeForDelta(int deltaX, int deltaY) {
        visualizeResizeForDelta(deltaX, deltaY, false);
    }

    /**
     *  Based on the deltas, we resize the frame, and, if needed, we resize the widget.
     */
    private void visualizeResizeForDelta(int deltaX, int deltaY, boolean onDismiss) {
        updateDeltas(deltaX, deltaY);
        DragLayer.LayoutParams lp = (DragLayer.LayoutParams) getLayoutParams();

        if (mLeftBorderActive) {
            lp.x = mBaselineX + mDeltaX;
            lp.width = mBaselineWidth - mDeltaX;
        } else if (mRightBorderActive) {
            lp.width = mBaselineWidth + mDeltaX;
        }

        if (mTopBorderActive) {
            lp.y = mBaselineY + mDeltaY;
            lp.height = mBaselineHeight - mDeltaY;
        } else if (mBottomBorderActive) {
            lp.height = mBaselineHeight + mDeltaY;
        }

        resizeWidgetIfNeeded(onDismiss);
        requestLayout();
    }

    /**
     *  Based on the current deltas, we determine if and how to resize the widget.
     */
    private void resizeWidgetIfNeeded(boolean onDismiss) {
        int xThreshold = mCellLayout.getCellWidth() + mCellLayout.getWidthGap();
        int yThreshold = mCellLayout.getCellHeight() + mCellLayout.getHeightGap();

        int deltaX = mDeltaX + mDeltaXAddOn;
        int deltaY = mDeltaY + mDeltaYAddOn;

        float hSpanIncF = 1.0f * deltaX / xThreshold - mRunningHInc;
        float vSpanIncF = 1.0f * deltaY / yThreshold - mRunningVInc;

        int hSpanInc = 0;
        int vSpanInc = 0;
        int cellXInc = 0;
        int cellYInc = 0;

        int countX = mCellLayout.getCountX();
        int countY = mCellLayout.getCountY();

        if (Math.abs(hSpanIncF) > RESIZE_THRESHOLD) {
            hSpanInc = Math.round(hSpanIncF);
        }
        if (Math.abs(vSpanIncF) > RESIZE_THRESHOLD) {
            vSpanInc = Math.round(vSpanIncF);
        }

        if (!onDismiss && (hSpanInc == 0 && vSpanInc == 0)) return;


        CellLayout.LayoutParams lp = (CellLayout.LayoutParams) mWidgetView.getLayoutParams();

        int spanX = lp.cellHSpan;
        int spanY = lp.cellVSpan;
        int cellX = lp.useTmpCoords ? lp.tmpCellX : lp.cellX;
        int cellY = lp.useTmpCoords ? lp.tmpCellY : lp.cellY;

        int hSpanDelta = 0;
        int vSpanDelta = 0;

        // For each border, we bound the resizing based on the minimum width, and the maximum
        // expandability.
        if (mLeftBorderActive) {
            cellXInc = Math.max(-cellX, hSpanInc);
            cellXInc = Math.min(lp.cellHSpan - mMinHSpan, cellXInc);
            hSpanInc *= -1;
            hSpanInc = Math.min(cellX, hSpanInc);
            hSpanInc = Math.max(-(lp.cellHSpan - mMinHSpan), hSpanInc);
            hSpanDelta = -hSpanInc;

        } else if (mRightBorderActive) {
            hSpanInc = Math.min(countX - (cellX + spanX), hSpanInc);
            hSpanInc = Math.max(-(lp.cellHSpan - mMinHSpan), hSpanInc);
            hSpanDelta = hSpanInc;
        }

        if (mTopBorderActive) {
            cellYInc = Math.max(-cellY, vSpanInc);
            cellYInc = Math.min(lp.cellVSpan - mMinVSpan, cellYInc);
            vSpanInc *= -1;
            vSpanInc = Math.min(cellY, vSpanInc);
            vSpanInc = Math.max(-(lp.cellVSpan - mMinVSpan), vSpanInc);
            vSpanDelta = -vSpanInc;
        } else if (mBottomBorderActive) {
            vSpanInc = Math.min(countY - (cellY + spanY), vSpanInc);
            vSpanInc = Math.max(-(lp.cellVSpan - mMinVSpan), vSpanInc);
            vSpanDelta = vSpanInc;
        }

        mDirectionVector[0] = 0;
        mDirectionVector[1] = 0;
        // Update the widget's dimensions and position according to the deltas computed above
        if (mLeftBorderActive || mRightBorderActive) {
            spanX += hSpanInc;
            cellX += cellXInc;
            if (hSpanDelta != 0) {
                mDirectionVector[0] = mLeftBorderActive ? -1 : 1;
            }
        }

        if (mTopBorderActive || mBottomBorderActive) {
            spanY += vSpanInc;
            cellY += cellYInc;
            if (vSpanDelta != 0) {
                mDirectionVector[1] = mTopBorderActive ? -1 : 1;
            }
        }

        if (!onDismiss && vSpanDelta == 0 && hSpanDelta == 0) return;

        // We always want the final commit to match the feedback, so we make sure to use the
        // last used direction vector when committing the resize / reorder.
        if (onDismiss) {
            mDirectionVector[0] = mLastDirectionVector[0];
            mDirectionVector[1] = mLastDirectionVector[1];
        } else {
            mLastDirectionVector[0] = mDirectionVector[0];
            mLastDirectionVector[1] = mDirectionVector[1];
        }

        if (mCellLayout.createAreaForResize(cellX, cellY, spanX, spanY, mWidgetView,
                mDirectionVector, onDismiss)) {
            if (mStateAnnouncer != null && (lp.cellHSpan != spanX || lp.cellVSpan != spanY) ) {
                mStateAnnouncer.announce(
                        mLauncher.getString(R.string.widget_resized));
            }

            lp.tmpCellX = cellX;
            lp.tmpCellY = cellY;
            lp.cellHSpan = spanX;
            lp.cellVSpan = spanY;
            mRunningVInc += vSpanDelta;
            mRunningHInc += hSpanDelta;

            if (!onDismiss) {
                updateWidgetSizeRanges(mWidgetView, mLauncher, spanX, spanY);
            }
        }
        mWidgetView.requestLayout();
    }

    static void updateWidgetSizeRanges(AppWidgetHostView widgetView, Launcher launcher,
            int spanX, int spanY) {
        getWidgetSizeRanges(launcher, spanX, spanY, sTmpRect);
        widgetView.updateAppWidgetSize(null, sTmpRect.left, sTmpRect.top,
                sTmpRect.right, sTmpRect.bottom);
    }

    public static Rect getWidgetSizeRanges(Context context, int spanX, int spanY, Rect rect) {
        if (sCellSize == null) {
            InvariantDeviceProfile inv = LauncherAppState.getInstance().getInvariantDeviceProfile();

            // Initiate cell sizes.
            sCellSize = new Point[2];
            sCellSize[0] = inv.landscapeProfile.getCellSize();
            sCellSize[1] = inv.portraitProfile.getCellSize();
        }

        if (rect == null) {
            rect = new Rect();
        }
        final float density = context.getResources().getDisplayMetrics().density;

        // Compute landscape size
        int landWidth = (int) ((spanX * sCellSize[0].x) / density);
        int landHeight = (int) ((spanY * sCellSize[0].y) / density);

        // Compute portrait size
        int portWidth = (int) ((spanX * sCellSize[1].x) / density);
        int portHeight = (int) ((spanY * sCellSize[1].y) / density);
        rect.set(portWidth, landHeight, landWidth, portHeight);
        return rect;
    }

    /**
     * This is the final step of the resize. Here we save the new widget size and position
     * to LauncherModel and animate the resize frame.
     */
    public void commitResize() {
        resizeWidgetIfNeeded(true);
        requestLayout();
    }

    public void onTouchUp() {
        int xThreshold = mCellLayout.getCellWidth() + mCellLayout.getWidthGap();
        int yThreshold = mCellLayout.getCellHeight() + mCellLayout.getHeightGap();

        mDeltaXAddOn = mRunningHInc * xThreshold; 
        mDeltaYAddOn = mRunningVInc * yThreshold; 
        mDeltaX = 0;
        mDeltaY = 0;

        post(new Runnable() {
            @Override
            public void run() {
                snapToWidget(true);
            }
        });
    }

    public void snapToWidget(boolean animate) {
        final DragLayer.LayoutParams lp = (DragLayer.LayoutParams) getLayoutParams();
        int newWidth = mWidgetView.getWidth() + 2 * mBackgroundPadding
                - mWidgetPadding.left - mWidgetPadding.right;
        int newHeight = mWidgetView.getHeight() + 2 * mBackgroundPadding
                - mWidgetPadding.top - mWidgetPadding.bottom;

        mTmpPt[0] = mWidgetView.getLeft();
        mTmpPt[1] = mWidgetView.getTop();
        mDragLayer.getDescendantCoordRelativeToSelf(mCellLayout.getShortcutsAndWidgets(), mTmpPt);

        int newX = mTmpPt[0] - mBackgroundPadding + mWidgetPadding.left;
        int newY = mTmpPt[1] - mBackgroundPadding + mWidgetPadding.top;

        // We need to make sure the frame's touchable regions lie fully within the bounds of the
        // DragLayer. We allow the actual handles to be clipped, but we shift the touch regions
        // down accordingly to provide a proper touch target.
        if (newY < 0) {
            // In this case we shift the touch region down to start at the top of the DragLayer
            mTopTouchRegionAdjustment = -newY;
        } else {
            mTopTouchRegionAdjustment = 0;
        }
        if (newY + newHeight > mDragLayer.getHeight()) {
            // In this case we shift the touch region up to end at the bottom of the DragLayer
            mBottomTouchRegionAdjustment = -(newY + newHeight - mDragLayer.getHeight());
        } else {
            mBottomTouchRegionAdjustment = 0;
        }

        if (!animate) {
            lp.width = newWidth;
            lp.height = newHeight;
            lp.x = newX;
            lp.y = newY;
            mLeftHandle.setAlpha(1.0f);
            mRightHandle.setAlpha(1.0f);
            mTopHandle.setAlpha(1.0f);
            mBottomHandle.setAlpha(1.0f);
            requestLayout();
        } else {
            PropertyValuesHolder width = PropertyValuesHolder.ofInt("width", lp.width, newWidth);
            PropertyValuesHolder height = PropertyValuesHolder.ofInt("height", lp.height,
                    newHeight);
            PropertyValuesHolder x = PropertyValuesHolder.ofInt("x", lp.x, newX);
            PropertyValuesHolder y = PropertyValuesHolder.ofInt("y", lp.y, newY);
            ObjectAnimator oa =
                    LauncherAnimUtils.ofPropertyValuesHolder(lp, this, width, height, x, y);
            ObjectAnimator leftOa = LauncherAnimUtils.ofFloat(mLeftHandle, ALPHA, 1.0f);
            ObjectAnimator rightOa = LauncherAnimUtils.ofFloat(mRightHandle, ALPHA, 1.0f);
            ObjectAnimator topOa = LauncherAnimUtils.ofFloat(mTopHandle, ALPHA, 1.0f);
            ObjectAnimator bottomOa = LauncherAnimUtils.ofFloat(mBottomHandle, ALPHA, 1.0f);
            oa.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    requestLayout();
                }
            });
            AnimatorSet set = LauncherAnimUtils.createAnimatorSet();
            if (mResizeMode == AppWidgetProviderInfo.RESIZE_VERTICAL) {
                set.playTogether(oa, topOa, bottomOa);
            } else if (mResizeMode == AppWidgetProviderInfo.RESIZE_HORIZONTAL) {
                set.playTogether(oa, leftOa, rightOa);
            } else {
                set.playTogether(oa, leftOa, rightOa, topOa, bottomOa);
            }

            set.setDuration(SNAP_DURATION);
            set.start();
        }

        setFocusableInTouchMode(true);
        requestFocus();
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        // Clear the frame and give focus to the widget host view when a directional key is pressed.
        if (FocusLogic.shouldConsume(keyCode)) {
            mDragLayer.clearAllResizeFrames();
            mWidgetView.requestFocus();
            return true;
        }
        return false;
    }
}
