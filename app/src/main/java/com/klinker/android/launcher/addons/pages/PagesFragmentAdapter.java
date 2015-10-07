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

package com.klinker.android.launcher.addons.pages;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class PagesFragmentAdapter extends FragmentStatePagerAdapter {

    private static final int NUM_PAGES = 5;

    private Object[] pages;
    private ArrayList<LauncherFrag> frags = new ArrayList<LauncherFrag>();

    class LauncherFrag {

        public String classPath;
        public String packageName;

        LauncherFrag(String classPath, String packageName) {
            this.classPath = classPath;
            this.packageName = packageName;
        }
    }

    public PagesFragmentAdapter(FragmentManager fm, Context context) {
        super(fm, context);
        this.pages = new Object[NUM_PAGES];

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        for (int i = 0; i < 5; i++) {
            String pack = sharedPrefs.getString("launcher_package_name_" + i, "");
            String path =  sharedPrefs.getString("launcher_class_path_" + i, "");

            if (!path.isEmpty()) {
                frags.add(new LauncherFrag(path, pack));
            }
        }

        if (frags.size() == 0) {
            frags.add(new LauncherFrag("com.klinker.android.launcher", ".addons.pages.HolderPage"));
        }
    }

    @Override
    public Fragment getItem(int position) {
        // Here we are using reflection to read the class from its name and then displaying that fragment accordingly
        // by invoking the constructor and calling the get fragment which must be implemented since the fragment extends
        // base page. if it fails for some reason, print the error and display the normal holder fragment instead

        String packageName = frags.get(position).packageName;
        String className = frags.get(position).classPath;

        try {
            Context classContext = context.createPackageContext(packageName,
                    Context.CONTEXT_INCLUDE_CODE + Context.CONTEXT_IGNORE_SECURITY);
            ClassLoader loader = classContext.getClassLoader();
            Class c = loader.loadClass(packageName + className);
            Constructor constructor = c.getConstructor();
            Object classInstance = constructor.newInstance();
            Method method = classInstance.getClass().getMethod("getFragment", int.class);
            Object fragment = method.invoke(classInstance, position);
            pages[position] = fragment;
            return (Fragment) fragment;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("PagesFragmentAdapter", "failed to instantiate class");
        }

        Object obj = (new HolderPage()).getFragment(position);
        pages[position] = obj;
        return (Fragment) obj;
    }

    @Override
    public int getCount() {
        return frags.size();
    }

    public void adjustFragmentBackgroundAlpha(int currentPosition, float alpha) {
        try {
            Method method = pages[currentPosition].getClass().getMethod("getBackground");
            View[] backgrounds = ((View[]) method.invoke(pages[currentPosition]));
            for (View view : backgrounds) {
                if (view != null) {
                    view.setAlpha(alpha);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Test to see if all of the pages are filled up
     * @return true if there isn't any "transparent pages"
     */
    public boolean testFullAdapter() {
        for (int i = 0; i < frags.size(); i++) {
            if (pages[i] == null) {
                return false;
            }
        }

        return true;
    }
}
