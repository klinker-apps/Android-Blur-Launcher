package xyz.klinker.blur.addons.utils;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.text.Html;
import android.text.Spanned;
import xyz.klinker.blur.R;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by luke on 7/30/14.
 */
public class XmlFaqUtils {

    private static final String TAG = "XmlFaqUtils";
    private static final String ns = null;

    private static List items;

    public static final class FAQ {
        public Spanned question;
        public Spanned text;
    }

    public static FAQ[] parse(Context context) {
        try {
            XmlResourceParser parser = context.getResources().getXml(R.xml.faq);
            parser.next();
            parser.nextTag();
            return readFaq(parser);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static FAQ[] readFaq(XmlPullParser parser) throws XmlPullParserException, IOException {
        items = new ArrayList();

        parser.require(XmlPullParser.START_TAG, ns, "faq");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if ("question".equals(name)) {
                items.add(readItem(parser));
            } else {
                skip(parser);
            }
        }

        return (FAQ[]) items.toArray(new FAQ[items.size()]);
    }

    private static FAQ readItem(XmlPullParser parser) throws XmlPullParserException, IOException {
        FAQ faq = new FAQ();
        parser.require(XmlPullParser.START_TAG, ns, "question");
        faq.question = Html.fromHtml(readFaqQuestion(parser));

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();
            if ("text".equals(name)) {
                faq.text = Html.fromHtml(readFaqText(parser));
            } else {
                skip(parser);
            }
        }

        return faq;
    }

    private static String readFaqQuestion(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "question");
        String faqName = parser.getAttributeValue(null, "name");
        String description = parser.getAttributeValue(null, "description");
        String question = (items.size() + 1) + ".) <u><b>" + faqName + "</u></b>";
        if (description != null) {
            question += "<br/>(" + description + ")";
        }
        return question;
    }

    private static String readFaqText(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "text");
        String text = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "text");
        return text.replaceAll("\n", "<br/>");
    }

    private static String readText(XmlPullParser parser) throws XmlPullParserException, IOException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
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
