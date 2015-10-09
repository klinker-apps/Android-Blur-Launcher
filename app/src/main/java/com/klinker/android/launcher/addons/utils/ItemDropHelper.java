package com.klinker.android.launcher.addons.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.klinker.android.launcher.R;
import com.klinker.android.launcher.addons.settings.IconPickerActivity;
import com.klinker.android.launcher.launcher3.BubbleTextView;
import com.klinker.android.launcher.launcher3.DragView;
import com.klinker.android.launcher.launcher3.ItemInfo;
import com.klinker.android.launcher.launcher3.ShortcutInfo;

import java.util.Arrays;
import java.util.List;

public class ItemDropHelper {
    private Context context;

    private Intent originalIntent;
    private String originalTitle;
    private int originalX;
    private int originalY;

    public ItemDropHelper(Context context) {
        this.context = context;
    }

    public void setItemBeingDragged(ItemInfo info) {
        if (info == null) {
            this.originalIntent = null;
            this.originalTitle = "";
            this.originalX = -1;
            this.originalY = -1;
        } else {
            this.originalIntent = info.getIntent();
            this.originalTitle = info.title + "";
            this.originalX = info.cellX;
            this.originalY = info.cellY;
        }
    }

    public void displayPopupIfNoChange(ItemInfo newLocation, View currentDragView) {
        if (newLocation == null) {
            return;
        }

        View icon = currentDragView;
        if (icon instanceof FrameLayout) {
            icon = ((ViewGroup) icon).getChildAt(0);
        }

        if (originalX == newLocation.cellX &&
                originalY == newLocation.cellY) {
            getPopupMenu(context, icon).show();
        }
    }

    public void displayPopupIfNoChange(int newX, int newY, View currentDragView) {
        View icon = currentDragView;
        if (icon instanceof FrameLayout) {
            icon = ((ViewGroup) icon).getChildAt(0);
        }

        Log.v("folder_drag", "orgX: " + originalX + ", orgY: " + originalY);
        Log.v("folder_drag", "newX: " + newX + ", newY: " + newY);

        if (originalX == newX && originalY == newY) {
            getPopupMenu(context, icon).show();
        }
    }

    private PopupMenu getPopupMenu(final Context context, final View icon) {

        final int CHANGE_NAME = 3;
        final int CHANGE_ICON = 2;
        final int REMOVE_CUSTOMS = 1;

        PackageManager pm = context.getPackageManager();
        final ActivityInfo info = originalIntent.resolveActivityInfo(pm, PackageManager.GET_ACTIVITIES);

        final PopupMenu menu = new PopupMenu(context, icon);
        menu.getMenu().add(Menu.NONE, REMOVE_CUSTOMS, Menu.NONE, context.getString(R.string.restore_defaults));
        //menu.getMenu().add(Menu.NONE, CHANGE_ICON, Menu.NONE, context.getString(R.string.custom_icon));
        menu.getMenu().add(Menu.NONE, CHANGE_NAME, Menu.NONE, context.getString(R.string.custom_name));
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case CHANGE_NAME:
                        // Creating alert Dialog for editing the name
                        AlertDialog.Builder alertDialog = changeNameDialog(context, icon);
                        alertDialog.show();
                        break;
                    case CHANGE_ICON:
                        String pack = PreferenceManager.getDefaultSharedPreferences(context).getString("icon_pack", "");
                        if (pack.isEmpty()) {
                            // then they don't have a custom icon pack so they can't change the icon
                            Toast.makeText(context, R.string.no_icon_pack_set, Toast.LENGTH_SHORT).show();
                        } else {
                            // here we will start the activity to choose the icon from the pack.
                            Intent picker = new Intent(context, IconPickerActivity.class);
                            picker.putExtra("package", pack);
                            picker.putExtra("icon_name", info.packageName.toLowerCase() + "." + info.name.toLowerCase());
                            context.startActivity(picker);
                        }
                        break;
                    case REMOVE_CUSTOMS:
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                        SharedPreferences.Editor e = sharedPreferences.edit();

                        e.remove(info.packageName.toLowerCase() + "." + info.name.toLowerCase());
                        e.remove(originalTitle);

                        String names = sharedPreferences.getString("launcher_custom_app_names", "");
                        names = names.replace(originalTitle + ":|:", "");

                        String icons = sharedPreferences.getString("launcher_custom_icon_names", "");
                        icons = icons.replace(info.packageName.toLowerCase() + "." + info.name.toLowerCase() + ":|:", "");

                        e.putString("launcher_custom_app_names", names);
                        e.putString("launcher_custom_icon_names", icons);

                        e.commit();

                        // completely restart the Launcher
                        android.os.Process.killProcess(android.os.Process.myPid());
                        break;
                }

                return false;
            }
        });

        return menu;
    }

    private AlertDialog.Builder changeNameDialog(final Context context, final View view) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

        // Setting Dialog Message
        alertDialog.setMessage(context.getString(R.string.custom_name) + ": " + originalTitle);
        final EditText input = new EditText(context);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setHint(R.string.type_name);
        input.requestFocus();
        input.setLayoutParams(lp);
        alertDialog.setView(input);

        alertDialog.setPositiveButton(context.getString(R.string.change), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
                String currentList = sharedPrefs.getString("launcher_custom_app_names", "");
                SharedPreferences.Editor e = sharedPrefs.edit();

                List<String> customNames = Arrays.asList(sharedPrefs.getString("launcher_custom_app_names", "").split(":|:"));
                if (!customNames.contains(originalTitle)) {
                    e.putString("launcher_custom_app_names", currentList + originalTitle + ":|:");
                }

                e.putString(originalTitle, input.getText().toString());
                e.commit();

                if (view != null) {
                    if (view instanceof BubbleTextView) {
                        ((BubbleTextView) view).setText(input.getText().toString());
                    } else {
                        // completely restart the Launcher
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                }
            }
        });

        alertDialog.setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        return alertDialog;
    }
}
