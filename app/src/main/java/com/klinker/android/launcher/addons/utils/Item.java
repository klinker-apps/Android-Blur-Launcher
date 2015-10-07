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

package com.klinker.android.launcher.addons.utils;

import android.graphics.drawable.Drawable;

public class Item {

    public String text;
    public int icon;
    public Drawable actualIcon;
    public String classPath;
    public String packageName;

    public Item(String text, Drawable icon, String classPath, String packageName) {
        this.text = text;
        this.actualIcon = icon;
        this.classPath = classPath;
        this.packageName = packageName;
    }
}
