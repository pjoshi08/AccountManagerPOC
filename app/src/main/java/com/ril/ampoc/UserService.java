package com.ril.ampoc;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import com.ril.ampoc.model.User;
import com.ril.ampoc.view.MainActivity;

public class UserService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private final IPCAidlInterface.Stub binder = new IPCAidlInterface.Stub() {
        @Override
        public int getPid() throws RemoteException {
            return Process.myPid();
        }

        @Override
        public User getUser() throws RemoteException {
            return MainActivity.getUser();
        }

        @Override
        public void setUser(User user) {

        }

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }
    };
}
