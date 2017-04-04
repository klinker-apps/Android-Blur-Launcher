package xyz.klinker.blur.launcher3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.AttributeSet;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import xyz.klinker.blur.R;
import xyz.klinker.blur.addons.settings.AppSettings;

public class QsbContainerView extends RelativeLayout implements View.OnClickListener {

    private Launcher mLauncher;

    public QsbContainerView(Context context) {
        super(context);
    }

    public QsbContainerView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void setLauncher(Launcher sLauncher) {
        mLauncher = sLauncher;
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();

         View search = findViewById(R.id.now_tab);
         if (search != null) {
         search.setOnClickListener(this);
         }
         View voice = findViewById(R.id.qsb_base);
         if (voice != null) {
         voice.setOnClickListener(this);
         }
        setOnClickListener(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        IntentFilter filter = new IntentFilter(Intent.ACTION_DATE_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        getContext().registerReceiver(mDateReceiver, filter);
        updateDate();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getContext().unregisterReceiver(mDateReceiver);
    }

       @Override
    public void setAlpha(float alpha) {
        super.setAlpha(alpha);
    }

    @Override
    public void onClick(View v) {
        Log.d("TEST", "onClick");
        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        if (v.getId() == R.id.now_tab) {
            if (Utilities.searchActivityExists(Launcher.getInstance())){
            try {
                Launcher.getInstance().showGlobalSearch();
            }  catch (Exception e) {
                int error = R.string.activity_not_found;
                Toast.makeText(Launcher.getInstance(), error, Toast.LENGTH_SHORT).show();
            }
            }
            else{
                int error = R.string.activity_not_found;
                Toast.makeText(Launcher.getInstance(), error, Toast.LENGTH_SHORT).show();
            }
        } else if (v.getId() == R.id.qsb_base) {
           Launcher.getInstance().g();
        }
    }

    private void updateDate() {
        TextView date1 = (TextView) findViewById(R.id.date1);
        TextView date2 = (TextView) findViewById(R.id.date2);
        DateFormat d1 = new SimpleDateFormat("MMMM d", Locale.getDefault());
        DateFormat d2 = new SimpleDateFormat("EEEE, yyyy", Locale.getDefault());
        if (date1 != null) {
            date1.setText(d1.format(Calendar.getInstance().getTime()));
        }
        if (date2 != null) {
            date2.setText(d2.format(Calendar.getInstance().getTime()));
        }
    }

    private BroadcastReceiver mDateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(Intent.ACTION_TIMEZONE_CHANGED)
                    || action.equals(Intent.ACTION_DATE_CHANGED)) {
                updateDate();
            }
        }
    };
}
