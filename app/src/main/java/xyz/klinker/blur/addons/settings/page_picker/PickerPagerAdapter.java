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

package xyz.klinker.blur.addons.settings.page_picker;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.v13.app.FragmentStatePagerAdapter;

import xyz.klinker.blur.R;
import xyz.klinker.blur.addons.utils.BlurPagesUtils;

public class PickerPagerAdapter extends FragmentStatePagerAdapter {

    public static final int NUM_PAGES = BlurPagesUtils.getNumPages();

    private Context context;

    public PickerPagerAdapter(FragmentManager manager, Context context) {
        super(manager);
        this.context = context;
    }

    @Override
    public Fragment getItem(int i) {
        return (new ChooserFragment()).getFragment(i);
    }

    @Override
    public int getCount() {
        return NUM_PAGES <= 5 ? NUM_PAGES : 5;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return " " + context.getResources().getString(R.string.page) + " " + (NUM_PAGES - position) + " ";
    }
}