package com.klinker.android.launcher.addons.settings.card_picker;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.klinker.android.launcher.R;
import com.klinker.android.launcher.addons.settings.SettingsActivity;
import com.klinker.android.launcher.addons.utils.AnimationUtils;
import com.klinker.android.launcher.addons.utils.Utils;
import com.klinker.android.launcher.api.Card;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

import java.util.ArrayList;
import java.util.List;

public class CardPickerActivity extends Activity {

    private SharedPreferences sharedPrefs;
    private Context context;

    private ArrayList<Card> content;

    private DragSortListView listView;
    private ScrollView scrollView;
    private TextView addButton;
    private TextView footerButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        setUpWindow();

        setContentView(R.layout.card_picker_activity);

        ActionBar actionBar = getActionBar();
        actionBar.setTitle(getString(R.string.card_picker));

        setArrayContent();

        listView = (DragSortListView) findViewById(R.id.list_view);
        scrollView = (ScrollView) findViewById(R.id.add_new);
        addButton = (TextView) findViewById(R.id.add_card_button);

        View footer = getLayoutInflater().inflate(R.layout.card_picker_footer, null);
        listView.addFooterView(footer);
        listView.setFooterDividersEnabled(false);

        footerButton = (TextView) footer.findViewById(R.id.add_card_button_footer);

        adapter = new DragDropAdapter(this, content);
        final ConfigurationDragSortController dragSortController = new ConfigurationDragSortController();
        final SwipeDismissListViewTouchListener mSwipeDismissTouchListener = new SwipeDismissListViewTouchListener(
                listView,
                new SwipeDismissListViewTouchListener.DismissCallbacks() {

                    public boolean canDismiss(int position) {
                        return position < adapter.getCount();
                    }

                    public void onDismiss(ListView lv, int[] reverseSortedPositions) {
                        for (int position : reverseSortedPositions) {
                            content.remove(position);
                            adapter.notifyDataSetChanged();
                        }

                        // we want to fade in the add button
                        if (content.isEmpty()) {
                            listView.setVisibility(View.GONE);

                            Animation fadeIn = new AlphaAnimation(0, 1);
                            fadeIn.setInterpolator(new DecelerateInterpolator());
                            fadeIn.setDuration(750);

                            AnimationSet animation = new AnimationSet(false);
                            animation.addAnimation(fadeIn);
                            scrollView.setAnimation(animation);

                            scrollView.setVisibility(View.VISIBLE);
                        }

                        SettingsActivity.prefChanged = true;
                    }
                }
        );

        listView.setDropListener(mDropListener);
        listView.setFloatViewManager(dragSortController);
        listView.setAdapter(adapter);
        listView.setOnScrollListener(mSwipeDismissTouchListener.makeScrollListener());
        listView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return dragSortController.onTouch(view, motionEvent)
                        || (!dragSortController.isDragging
                        && mSwipeDismissTouchListener.onTouch(view, motionEvent));
            }
        });

        addButton.setOnTouchListener(mHapticFeedbackTouchListener);
        addButton.setOnClickListener(add);
        footerButton.setOnClickListener(add);

        if (!content.isEmpty()) {
            listView.setVisibility(View.VISIBLE);
            scrollView.setVisibility(View.GONE);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showAnimation(addButton);
            }
        }, 1500);
    }

    private void showAnimation(final View v) {
        AnimationUtils.tada(v).start();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showAnimation(v);
            }
        }, 4000);
    }

    View.OnTouchListener mHapticFeedbackTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            }
            return false;
        }
    };

    View.OnClickListener add = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            final Card[] items = Utils.getPackageCards(context, content);

            if (items.length == 0) {
                Toast.makeText(context, R.string.nothing_to_add, Toast.LENGTH_SHORT).show();
                return;
            }

            final ListAdapter a = Utils.getCardsAdapter(context, items);

            AlertDialog.Builder attachBuilder = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_DARK);
            attachBuilder.setAdapter(a, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
                    boolean isEmpty = content.isEmpty();

                    content.add(new Card(items[i].getPackage(), items[i].getClassPath(), items[i].getIcon(), items[i].getTitle()));
                    adapter.notifyDataSetChanged();

                    // we want to fade in the add button
                    if (isEmpty) {

                        Animation fadeIn = new AlphaAnimation(0, 1);
                        fadeIn.setInterpolator(new DecelerateInterpolator());
                        fadeIn.setStartOffset(300);
                        fadeIn.setDuration(300);

                        Animation fadeOut = new AlphaAnimation(1, 0);
                        fadeOut.setInterpolator(new AccelerateInterpolator());
                        fadeOut.setDuration(300);

                        scrollView.setAnimation(fadeOut);
                        listView.setAnimation(fadeIn);

                        scrollView.setVisibility(View.GONE);
                        listView.setVisibility(View.VISIBLE);
                    }

                    SettingsActivity.prefChanged = true;

                    dialog.dismiss();
                }

            });

            attachBuilder.create().show();
        }
    };

    public void setUpWindow() {
        //requestWindowFeature(Window.FEATURE_ACTION_BAR);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        // Params for the window.
        // You can easily set the alpha and the dim behind the window from here
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.alpha = 1.0f;    // lower than one makes it more transparent
        params.dimAmount = .87f;  // set it higher if you want to dim behind the window
        getWindow().setAttributes(params);

        // Gets the display size so that you can set the window to a percent of that
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        // You could also easily used an integer value from the shared preferences to set the percent
        if (height > width) {
            getWindow().setLayout((int) (width * .85), (int) (height * .7));
        } else {
            getWindow().setLayout((int) (width * .5), (int) (height * .8));
        }
    }

    @Override
    public void finish() {
        if (SettingsActivity.prefChanged) {
            SharedPreferences.Editor e = PreferenceManager.getDefaultSharedPreferences(context).edit();

            e.putInt("launcher_number_of_cards", content.size());
            for (int i = 0; i < content.size(); i++) {
                Card c = content.get(i);

                e.putString("launcher_card_package_name_" + i, c.getPackage());
                e.putString("launcher_card_class_path_" + i, c.getClassPath());
            }

            e.commit();
        }

        super.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.frag_picker, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_done:

                SettingsActivity.prefChanged = true;
                finish();

                return super.onOptionsItemSelected(item);

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private DragDropAdapter adapter;

    private DragSortListView.DropListener mDropListener =
            new DragSortListView.DropListener() {
                public void drop(int from, int to) {
                    Card temp = content.get(from);
                    content.remove(from);
                    content.add(to, temp);
                    adapter.notifyDataSetChanged();

                    SettingsActivity.prefChanged = true;
                }
            };

    private class ConfigurationDragSortController extends DragSortController {
        private int mPos;
        public boolean isDragging = false;

        public ConfigurationDragSortController() {
            super(listView, R.id.drag_handle,
                    DragSortController.ON_DOWN, 0);
            setRemoveEnabled(false);
        }

        @Override
        public int startDragPosition(MotionEvent ev) {
            int res = super.dragHandleHitPosition(ev);
            if (res >= adapter.getCount()) {
                return DragSortController.MISS;
            }

            return res;
        }

        @Override
        public View onCreateFloatView(int position) {
            Vibrator v = (Vibrator) listView.getContext().getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(10);
            mPos = position;

            isDragging = true;

            return adapter.getView(position, null, listView);
        }

        private int origHeight = -1;

        @Override
        public void onDragFloatView(View floatView, Point floatPoint, Point touchPoint) {
            final int addPos = adapter.getCount();
            final int first = listView.getFirstVisiblePosition();
            final int lvDivHeight = listView.getDividerHeight();

            if (origHeight == -1) {
                origHeight = floatView.getHeight();
            }

            View div = listView.getChildAt(addPos - first);

            if (touchPoint.x > listView.getWidth() / 2) {
                float scale = touchPoint.x - listView.getWidth() / 2;
                scale /= (float) (listView.getWidth() / 5);
                ViewGroup.LayoutParams lp = floatView.getLayoutParams();
                lp.height = Math.max(origHeight, (int) (scale * origHeight));
                //Log.d("mobeta", "setting height " + lp.height);
                floatView.setLayoutParams(lp);
            }

            if (div != null) {
                if (mPos > addPos) {
                    // don't allow floating View to go above
                    // section divider
                    final int limit = div.getBottom() + lvDivHeight;
                    if (floatPoint.y < limit) {
                        floatPoint.y = limit;
                    }
                } else {
                    // don't allow floating View to go below
                    // section divider
                    final int limit = div.getTop() - lvDivHeight - floatView.getHeight();
                    if (floatPoint.y > limit) {
                        floatPoint.y = limit;
                    }
                }
            }
        }

        @Override
        public void onDestroyFloatView(View floatView) {
            //do nothing; block super from crashing
            isDragging = false;
        }
    }

    public void setArrayContent() {
        // get the cards the user has selected and add them to the list
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        content = new ArrayList<Card>();
        ArrayList<String> packages = new ArrayList<String>();

        int extra = 0;

        for (int i = 0; i < sharedPrefs.getInt("launcher_number_of_cards", 0); i++) {
            String pack = sharedPrefs.getString("launcher_card_package_name_" + i, "");
            String path =  sharedPrefs.getString("launcher_card_class_path_" + i, "");

            content.add(new Card(pack, path));
            packages.add(pack);

        }

        // now we need to fill in the title and the drawable
        final PackageManager pm = context.getPackageManager();
        final List<ApplicationInfo> packs = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (int j = 0; j < packages.size(); j++) {
            String s = packages.get(j);

            if (s.equals("com.klinker.android.launcher")) {
                // we will handle it internally
                content.get(j).setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
                String path = content.get(j).getClassPath();
                if (path.contains("WeatherCard")) {
                    content.get(j).setTitle(getResources().getString(R.string.weather_card));
                } else if (path.contains("NextEventCard")) {
                    content.get(j).setTitle(getString(R.string.calendar_card));
                } else if (path.contains("NextAlarmCard")) {
                    content.get(j).setTitle(getString(R.string.alarm_card));
                }
            } else {
                // use package manager to get the title and the drawable
                for (int i = 0; i < packs.size(); i++) {
                    String name = packs.get(i).packageName;

                    if (s.equals(name)) {
                        content.get(j).setIcon(pm.getApplicationIcon(packs.get(i)));
                        content.get(j).setTitle(packs.get(i).metaData.getString("launcher_card_title"));
                        break;
                    }
                }
            }
        }
    }
}
