package xyz.klinker.blur.extra_pages;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

/**
 * A simple frame layout that adds the launcher's expected background behind the content,
 * so that we can change the alpha on it.
 */
public class LauncherPageLayout extends FrameLayout {

    public static final int BACKGROUND_ID = 0x00ff0004;

    private View background;

    public LauncherPageLayout(Context context) {
        this(context, null);
    }

    public LauncherPageLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LauncherPageLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        background = new View(context);
        background.setId(BACKGROUND_ID);
        background.setBackgroundColor(context.getResources().getColor(R.color.background_grey));
        background.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        addView(background, 0);
    }
}
