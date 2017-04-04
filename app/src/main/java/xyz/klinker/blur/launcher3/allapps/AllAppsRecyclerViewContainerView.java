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
package xyz.klinker.blur.launcher3.allapps;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import xyz.klinker.blur.launcher3.BubbleTextView;
import xyz.klinker.blur.launcher3.BubbleTextView.BubbleTextShadowHandler;
import xyz.klinker.blur.launcher3.ClickShadowView;
import xyz.klinker.blur.launcher3.DeviceProfile;
import xyz.klinker.blur.launcher3.Launcher;
import xyz.klinker.blur.R;

/**
 * A container for RecyclerView to allow for the click shadow view to be shown behind an icon that
 * is launching.
 */
public class AllAppsRecyclerViewContainerView extends FrameLayout
        implements BubbleTextShadowHandler {

    private final ClickShadowView mTouchFeedbackView;

    public AllAppsRecyclerViewContainerView(Context context) {
        this(context, null);
    }

    public AllAppsRecyclerViewContainerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AllAppsRecyclerViewContainerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        Launcher launcher = Launcher.getLauncher(context);
        DeviceProfile grid = launcher.getDeviceProfile();

        mTouchFeedbackView = new ClickShadowView(context);

        // Make the feedback view large enough to hold the blur bitmap.
        int size = grid.allAppsIconSizePx + mTouchFeedbackView.getExtraSize();
        addView(mTouchFeedbackView, size, size);
    }

    @Override
    public void setPressedIcon(BubbleTextView icon, Bitmap background) {
        if (icon == null || background == null) {
            mTouchFeedbackView.setBitmap(null);
            mTouchFeedbackView.animate().cancel();
        } else if (mTouchFeedbackView.setBitmap(background)) {
            View rv = findViewById(R.id.apps_list_view);
            mTouchFeedbackView.alignWithIconView(icon, (ViewGroup) icon.getParent(), rv);
            mTouchFeedbackView.animateShadow();
        }
    }
}
