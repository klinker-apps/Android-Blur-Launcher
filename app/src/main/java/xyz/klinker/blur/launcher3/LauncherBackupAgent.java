package xyz.klinker.blur.launcher3;

import android.app.backup.BackupAgent;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.os.ParcelFileDescriptor;

import xyz.klinker.blur.launcher3.provider.RestoreDbTask;

public class LauncherBackupAgent extends BackupAgent {

    @Override
    public void onRestore(
            BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState) {
        // Doesn't do incremental backup/restore
    }

    @Override
    public void onBackup(
            ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) {
        // Doesn't do incremental backup/restore
    }

    @Override
    public void onRestoreFinished() {
        RestoreDbTask.setPending(this, true);
    }
}
