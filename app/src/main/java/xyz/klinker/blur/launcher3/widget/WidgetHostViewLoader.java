package xyz.klinker.blur.launcher3.widget;

import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import xyz.klinker.blur.launcher3.AppWidgetResizeFrame;
import xyz.klinker.blur.launcher3.DragSource;
import xyz.klinker.blur.launcher3.DropTarget;
import xyz.klinker.blur.launcher3.ItemInfo;
import xyz.klinker.blur.launcher3.Launcher;
import xyz.klinker.blur.launcher3.LauncherAppWidgetProviderInfo;
import xyz.klinker.blur.launcher3.Utilities;
import xyz.klinker.blur.launcher3.compat.AppWidgetManagerCompat;
import xyz.klinker.blur.launcher3.dragndrop.DragController;
import xyz.klinker.blur.launcher3.dragndrop.DragLayer;
import xyz.klinker.blur.launcher3.dragndrop.DragOptions;
import xyz.klinker.blur.launcher3.util.Thunk;

public class WidgetHostViewLoader implements DragController.DragListener {
    private static final String TAG = "WidgetHostViewLoader";
    private static final boolean LOGD = false;

    /* Runnables to handle inflation and binding. */
    @Thunk
    Runnable mInflateWidgetRunnable = null;
    private Runnable mBindWidgetRunnable = null;

    // TODO: technically, this class should not have to know the existence of the launcher.
    @Thunk
    Launcher mLauncher;
    @Thunk Handler mHandler;
    @Thunk final View mView;
    @Thunk final PendingAddWidgetInfo mInfo;

    // Widget id generated for binding a widget host view or -1 for invalid id. The id is
    // not is use as long as it is stored here and can be deleted safely. Once its used, this value
    // to be set back to -1.
    @Thunk int mWidgetLoadingId = -1;

    public WidgetHostViewLoader(Launcher launcher, View view) {
        mLauncher = launcher;
        mHandler = new Handler();
        mView = view;
        mInfo = (PendingAddWidgetInfo) view.getTag();
    }

    @Override
    public void onDragStart(DropTarget.DragObject dragObject, DragOptions options) { }

    @Override
    public void onDragEnd() {
        if (LOGD) {
            Log.d(TAG, "Cleaning up in onDragEnd()...");
        }

        // Cleanup up preloading state.
        mLauncher.getDragController().removeDragListener(this);

        mHandler.removeCallbacks(mBindWidgetRunnable);
        mHandler.removeCallbacks(mInflateWidgetRunnable);

        // Cleanup widget id
        if (mWidgetLoadingId != -1) {
            mLauncher.getAppWidgetHost().deleteAppWidgetId(mWidgetLoadingId);
            mWidgetLoadingId = -1;
        }

        // The widget was inflated and added to the DragLayer -- remove it.
        if (mInfo.boundWidget != null) {
            if (LOGD) {
                Log.d(TAG, "...removing widget from drag layer");
            }
            mLauncher.getDragLayer().removeView(mInfo.boundWidget);
            mLauncher.getAppWidgetHost().deleteAppWidgetId(mInfo.boundWidget.getAppWidgetId());
            mInfo.boundWidget = null;
        }
    }

    /**
     * Start preloading the widget.
     */
    public boolean preloadWidget() {
        final LauncherAppWidgetProviderInfo pInfo = mInfo.info;

        if (pInfo.isCustomWidget) {
            return false;
        }
        final Bundle options = getDefaultOptionsForWidget(mLauncher, mInfo);

        // If there is a configuration activity, do not follow thru bound and inflate.
        if (pInfo.configure != null) {
            mInfo.bindOptions = options;
            return false;
        }

        mBindWidgetRunnable = new Runnable() {
            @Override
            public void run() {
                mWidgetLoadingId = mLauncher.getAppWidgetHost().allocateAppWidgetId();
                if (LOGD) {
                    Log.d(TAG, "Binding widget, id: " + mWidgetLoadingId);
                }
                if(AppWidgetManagerCompat.getInstance(mLauncher).bindAppWidgetIdIfAllowed(
                        mWidgetLoadingId, pInfo, options)) {

                    // Widget id bound. Inflate the widget.
                    mHandler.post(mInflateWidgetRunnable);
                }
            }
        };

        mInflateWidgetRunnable = new Runnable() {
            @Override
            public void run() {
                if (LOGD) {
                    Log.d(TAG, "Inflating widget, id: " + mWidgetLoadingId);
                }
                if (mWidgetLoadingId == -1) {
                    return;
                }
                AppWidgetHostView hostView = mLauncher.getAppWidgetHost().createView(
                        (Context) mLauncher, mWidgetLoadingId, pInfo);
                mInfo.boundWidget = hostView;

                // We used up the widget Id in binding the above view.
                mWidgetLoadingId = -1;

                hostView.setVisibility(View.INVISIBLE);
                int[] unScaledSize = mLauncher.getWorkspace().estimateItemSize(mInfo, false);
                // We want the first widget layout to be the correct size. This will be important
                // for width size reporting to the AppWidgetManager.
                DragLayer.LayoutParams lp = new DragLayer.LayoutParams(unScaledSize[0],
                        unScaledSize[1]);
                lp.x = lp.y = 0;
                lp.customPosition = true;
                hostView.setLayoutParams(lp);
                if (LOGD) {
                    Log.d(TAG, "Adding host view to drag layer");
                }
                mLauncher.getDragLayer().addView(hostView);
                mView.setTag(mInfo);
            }
        };

        if (LOGD) {
            Log.d(TAG, "About to bind/inflate widget");
        }
        mHandler.post(mBindWidgetRunnable);
        return true;
    }

    public static Bundle getDefaultOptionsForWidget(Context context, PendingAddWidgetInfo info) {
        Bundle options = null;
        if (Utilities.ATLEAST_JB_MR1) {
            Rect rect = new Rect();
            AppWidgetResizeFrame.getWidgetSizeRanges(context, info.spanX, info.spanY, rect);
            Rect padding = AppWidgetHostView.getDefaultPaddingForWidget(context,
                    info.componentName, null);

            float density = context.getResources().getDisplayMetrics().density;
            int xPaddingDips = (int) ((padding.left + padding.right) / density);
            int yPaddingDips = (int) ((padding.top + padding.bottom) / density);

            options = new Bundle();
            options.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,
                    rect.left - xPaddingDips);
            options.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,
                    rect.top - yPaddingDips);
            options.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH,
                    rect.right - xPaddingDips);
            options.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT,
                    rect.bottom - yPaddingDips);
        }
        return options;
    }
}
