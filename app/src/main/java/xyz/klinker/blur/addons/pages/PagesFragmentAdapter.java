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

package xyz.klinker.blur.addons.pages;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import xyz.klinker.blur.addons.utils.BlurPagesUtils;
import xyz.klinker.blur.extra_pages.BaseLauncherPage;

public class PagesFragmentAdapter extends FragmentPagerAdapter {

    private static final int MAX_PAGES = BlurPagesUtils.getNumPages();

    private List<BaseLauncherPage> pages = new ArrayList<>();

    class LauncherPageDefinition {
        public String classPath;
        public String packageName;

        LauncherPageDefinition(String classPath, String packageName) {
            this.classPath = classPath;
            this.packageName = packageName;
        }
    }

    public PagesFragmentAdapter(FragmentManager fm, Context context) {
        super(fm);

        List<LauncherPageDefinition> pageDefinitions = new ArrayList<>();
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        for (int i = 0; i < MAX_PAGES; i++) {
            String pack = sharedPrefs.getString("launcher_package_name_" + i, "");
            String path =  sharedPrefs.getString("launcher_class_path_" + i, "");

            if (!path.isEmpty()) {
                pageDefinitions.add(new LauncherPageDefinition(path, pack));
            }
        }

        if (pageDefinitions.size() == 0) {
            pageDefinitions.add(new LauncherPageDefinition("xyz.klinker.blur", ".addons.pages.HolderPage"));
        }

        for (int i = 0; i < pageDefinitions.size(); i++) {

            // Here we are using reflection to read the class from its name and then displaying that fragment accordingly
            // by invoking the constructor and calling the get fragment which must be implemented since the fragment extends
            // base page. if it fails for some reason, print the error and display the normal holder fragment instead

            // we do it this way since it is easy to serialize the fragments by their package names from settings
            // Blur needed this in its legacy state (Blur 2), but since there are no longer pages accepted
            // from other packages, we could take it out if we needed to.

            String packageName = pageDefinitions.get(i).packageName;
            String className = pageDefinitions.get(i).classPath;

            try {
                Context classContext = context.createPackageContext(packageName,
                        Context.CONTEXT_INCLUDE_CODE + Context.CONTEXT_IGNORE_SECURITY);
                ClassLoader loader = classContext.getClassLoader();
                Class c = loader.loadClass(packageName + className);
                Constructor constructor = c.getConstructor();
                Object classInstance = constructor.newInstance();
                Method method = classInstance.getClass().getMethod("getFragment", int.class);
                BaseLauncherPage fragment = (BaseLauncherPage) method.invoke(classInstance, i);
                pages.add(fragment);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("PagesFragmentAdapter", "failed to instantiate class");

                BaseLauncherPage errorHolder = (new HolderPage()).getFragment(i);
                pages.add(errorHolder);
            }

        }
    }

    @Override
    public Fragment getItem(int position) {
        return pages.get(position);
    }

    @Override
    public int getCount() {
        return pages.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        BaseLauncherPage fragment = (BaseLauncherPage) super.instantiateItem(container, position);
        pages.add(position, fragment);
        pages.remove(position + 1);

        return fragment;
    }

    public void adjustFragmentBackgroundAlpha(int position, float alpha) {
        View[] backgrounds = pages.get(position).getAlphaChangingViews();

        if (backgrounds != null) {
            for (View view : backgrounds) {
                if (view != null) {
                    view.setAlpha(alpha);
                }
            }
        }
    }

    public void pagesOpened() {
        for (BaseLauncherPage page : pages) {
            page.onFragmentsOpened();
        }
    }

    public void pagesClosed() {
        for (BaseLauncherPage page : pages) {
            page.onFragmentsClosed();
        }
    }
}
