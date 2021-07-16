package com.ril.ampoc.broadcast;

import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ril.ampoc.util.TaskRunner;

public class SuperBroadcastReceiver extends BroadcastReceiver {

    private final String TAG = "SuperBR";
    public static final String SUPER_INTENT_ACTION = "com.ril.ampoc.SUPER_INTENT_ACTION";
    private AccountManager accountManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "> Super onReceive.....");
        final PendingResult result = goAsync();
        new TaskRunner()
                .executeAsync(
                        new BRTask(intent), // Background work
                        (Void) -> result.finish() // OnComplete
                );
    }
}
