package com.ril.childapp1;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.ril.ampoc.IPCAidlInterface;
import com.ril.childapp1.R;
import com.ril.ampoc.model.User;

public class ChildOneMainActivity extends AppCompatActivity {

    private IPCAidlInterface remoteService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_one_main);

        triggerBroadcast();
        findViewById(R.id.broadcastBtn).setOnClickListener(v -> fetchData());
    }

    @Override
    protected void onResume() {
        super.onResume();
        //EventBus.getDefault().register(this);
        triggerBroadcast();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //EventBus.getDefault().unregister(this);
    }

    private void triggerBroadcast() {
        /*Intent superIntent = new Intent();
        superIntent.setAction("com.ril.ampoc.SUPER_INTENT_ACTION");
        superIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        superIntent.setComponent(
                new ComponentName("com.ril.ampoc",
                        "com.ril.ampoc.broadcast.SuperBroadcastReceiver")
        );
        sendBroadcast(superIntent);*/
        if (remoteService == null) {
            Intent intent = new Intent(IPCAidlInterface.class.getName());
            intent.setAction("aidlExample");
            intent.setPackage("com.ril.ampoc");
            bindService(intent, connection, Service.BIND_AUTO_CREATE);
        }
    }

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("ChildOneMainActivity", "Service >>> onServiceConnected");
            remoteService = IPCAidlInterface.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("ChildOneMainActivity", "Service >>> onServiceDisConnected");
            remoteService = null;
        }
    };

    private void fetchData() {
        try {
            User user = remoteService.getUser();
            Toast.makeText(this, user.getUsername(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }
}