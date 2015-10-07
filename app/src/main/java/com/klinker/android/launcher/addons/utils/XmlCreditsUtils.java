package com.klinker.android.launcher.addons.utils;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import com.klinker.android.launcher.R;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lucasklinker on 8/7/14.
 */
public class XmlCreditsUtils {
    private static final String TAG = "XmlCreditsUtils";
    private static final String ns = null;

    public static Spanned[] parse(Context context) {
        try {
            XmlResourceParser parser = context.getResources().getXml(R.xml.credits);
            parser.next();
            parser.nextTag();
            return readChangelog(parser);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Spanned[] readChangelog(XmlPullParser parser) throws XmlPullParserException, IOException {
        List items = new ArrayList();

        parser.require(XmlPullParser.START_TAG, ns, "credits");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            Log.v("launcher_credits", name);
            if ("source".equals(name)) {
                items.add(readSource(parser));
            } else if ("library".equals(name)) {
                items.add(readLibrary(parser));
            } else {
                skip(parser);
            }

            int next = parser.next();
        }

        return (Spanned[]) items.toArray(new Spanned[items.size()]);
    }

    private static Spanned readSource(XmlPullParser parser) throws XmlPullParserException, IOException {
        StringBuilder versionInfo = new StringBuilder();
        parser.require(XmlPullParser.START_TAG, ns, "source");
        versionInfo.append(readSourceInfo(parser));

        return Html.fromHtml(versionInfo.toString());
    }

    private static Spanned readLibrary(XmlPullParser parser) throws XmlPullParserException, IOException {
        StringBuilder versionInfo = new StringBuilder();
        parser.require(XmlPullParser.START_TAG, ns, "library");
        versionInfo.append(readLibraryInfo(parser));

        return Html.fromHtml(versionInfo.toString());
    }

    private static String readLibraryInfo(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "library");
        String userName = parser.getAttributeValue(null, "user_name");
        String libName = parser.getAttributeValue(null, "lib_name");
        String license = parser.getAttributeValue(null, "license");
        String version = "&#8226 <b>" + userName + ":</b>";
        if (libName != null) {
            version += " " + libName;
        }
        if (license != null) {
            version += "<br/>\t\t\t&#8226 " + license;
        };
        return version;
    }

    private static String readSourceInfo(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "source");
        String sourceName = parser.getAttributeValue(null, "name");
        String sourceDescription = parser.getAttributeValue(null, "description");
        String version = "&#8226 <b>" + sourceName + ":</b>";
        if (sourceDescription != null) {
            version += " " + sourceDescription;
        };
        return version;
    }

    private static void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}