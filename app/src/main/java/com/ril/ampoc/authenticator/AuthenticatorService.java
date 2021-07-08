package com.ril.ampoc.authenticator;

import android.accounts.AccountManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class AuthenticatorService extends Service {

    private AccountAuthenticator accountAuthenticator;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        IBinder binder = null;
        if (intent.getAction().equals(AccountManager.ACTION_AUTHENTICATOR_INTENT)) {
            binder = getAuthenticator().getIBinder();
        }

        return binder;
    }

    private AccountAuthenticator getAuthenticator() {
        if (null == accountAuthenticator) {
            accountAuthenticator = new AccountAuthenticator(this);
        }

        return accountAuthenticator;
    }
}
