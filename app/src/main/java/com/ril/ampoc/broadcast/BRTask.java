package com.ril.ampoc.broadcast;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.util.Log;

import java.util.concurrent.Callable;

class BRTask implements Callable<Void> {

    private Intent intent;

    public BRTask(Intent intent) {
        this.intent = intent;
    }

    @Override
    public Void call() {
        StringBuilder sb = new StringBuilder();
        sb.append("Action: ").append(intent.getAction()).append("\n")
                .append("URI: ").append(intent.toUri(Intent.URI_INTENT_SCHEME));
        String log = sb.toString();
        Log.d("BRTask", log);

        return null;
    }
}
