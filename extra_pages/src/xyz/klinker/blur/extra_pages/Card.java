package xyz.klinker.blur.extra_pages;

import android.graphics.drawable.Drawable;


public class Card {

    private String mPackage;
    private String mClass;
    private Drawable icon;
    private String title;

    public Card(String mPackage, String mClass, Drawable icon, String title) {
        this.mPackage = mPackage;
        this.mClass = mClass;
        this.icon = icon;
        this.title = title;
    }

    public Card(String mPackage, String mClass) {
        this.mPackage = mPackage;
        this.mClass = mClass;
        this.icon = null;
        this.title = "";
    }

    public Card(String mPackage, String mClass, String title) {
        this.mPackage = mPackage;
        this.mClass = mClass;
        this.icon = null;
        this.title = title;
    }

    public String getPackage() {
        return mPackage;
    }

    public void setPackage(String mPackage) {
        this.mPackage = mPackage;
    }

    public String getClassPath() {
        return mClass;
    }

    public void setClass(String mClass) {
        this.mClass = mClass;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}