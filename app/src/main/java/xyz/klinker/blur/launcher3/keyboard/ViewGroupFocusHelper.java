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

package xyz.klinker.blur.launcher3.keyboard;

import android.graphics.Rect;
import android.view.View;
import android.view.View.OnFocusChangeListener;

import xyz.klinker.blur.launcher3.PagedView;

/**
 * {@link FocusIndicatorHelper} for a generic view group.
 */
public class ViewGroupFocusHelper extends FocusIndicatorHelper {

    private final View mContainer;

    public ViewGroupFocusHelper(View container) {
        super(container);
        mContainer = container;
    }

    @Override
    public void viewToRect(View v, Rect outRect) {
        outRect.left = 0;
        outRect.top = 0;

        computeLocationRelativeToContainer(v, outRect);

        // If a view is scaled, its position will also shift accordingly. For optimization, only
        // consider this for the last node.
        outRect.left += (1 - v.getScaleX()) * v.getWidth() / 2;
        outRect.top += (1 - v.getScaleY()) * v.getHeight() / 2;

        outRect.right = outRect.left + (int) (v.getScaleX() * v.getWidth());
        outRect.bottom = outRect.top + (int) (v.getScaleY() * v.getHeight());
    }

    private void computeLocationRelativeToContainer(View child, Rect outRect) {
        View parent = (View) child.getParent();
        outRect.left += child.getLeft();
        outRect.top += child.getTop();

        if (parent != mContainer) {
            if (parent instanceof PagedView) {
                PagedView page = (PagedView) parent;
                outRect.left -= page.getScrollForPage(page.indexOfChild(child));
            }

            computeLocationRelativeToContainer(parent, outRect);
        }
    }

    /**
     * Sets the alpha of this FocusIndicatorHelper to 0 when a view with this listener
     * receives focus.
     */
    public View.OnFocusChangeListener getHideIndicatorOnFocusListener() {
        return new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    endCurrentAnimation();
                    setCurrentView(null);
                    setAlpha(0);
                    invalidateDirty();
                }
            }
        };
    }
}
