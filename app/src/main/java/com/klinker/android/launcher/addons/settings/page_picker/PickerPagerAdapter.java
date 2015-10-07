/*
 * Copyright 2014 Klinker Apps Inc.
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

package com.klinker.android.launcher.addons.settings.page_picker;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;

import com.klinker.android.launcher.R;
import com.klinker.android.launcher.addons.pages.FragmentStatePagerAdapter;

public class PickerPagerAdapter extends FragmentStatePagerAdapter {

    public static final int NUM_PAGES = 5; // change to 6 to be able to add the extra page on the left

    public PickerPagerAdapter(FragmentManager manager, Context context) {
        super(manager, context);
    }

    @Override
    public Fragment getItem(int i) {
        if (i < 5) {
            return (new ChooserFragment()).getFragment(i);
        } else {
            return new ExtraPageFragment();
        }
    }

    @Override
    public int getCount() {
        return NUM_PAGES;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position < 5) {
            return " " + context.getResources().getString(R.string.page) + " " + (NUM_PAGES - position - 1) + " ";
        } else {
            return context.getString(R.string.extra_page);
        }
    }
}