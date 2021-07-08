package com.ril.ampoc.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import com.ril.ampoc.authenticator.IServerAuthenticator;
import com.ril.ampoc.authenticator.ServerAuthenticator;

public class AccountUtils {
    public static final String ACCOUNT_TYPE = "com.ril.ampoc";
    public static final String AUTH_TOKEN_TYPE = "com.ril.atp";

    public final static String ARG_ACCOUNT_TYPE = "ACCOUNT_TYPE";
    public final static String ARG_AUTH_TYPE = "AUTH_TYPE";
    public final static String ARG_ACCOUNT_NAME = "ACCOUNT_NAME";
    public final static String ARG_IS_ADDING_NEW_ACCOUNT = "IS_ADDING_ACCOUNT";
    public static final String KEY_ERROR_MESSAGE = "ERR_MSG";
    public final static String PARAM_USER_PASS = "USER_PASS";

    public static final String AUTHTOKEN_TYPE_READ_ONLY = "Read only";
    public static final String AUTHTOKEN_TYPE_READ_ONLY_LABEL = "Read only access to an account";

    public static final String AUTHTOKEN_TYPE_FULL_ACCESS = "Full access";
    public static final String AUTHTOKEN_TYPE_FULL_ACCESS_LABEL = "Full access to an account";

    private static final IServerAuthenticator serverAuthenticator = new ServerAuthenticator();

    public static Account getAccount(Context context, String accountName) {
        AccountManager accountManager = AccountManager.get(context);
        Account[] accounts = accountManager.getAccountsByType(ACCOUNT_TYPE);

        for (Account account : accounts) {
            if (account.name.equalsIgnoreCase(accountName)) {
                return account;
            }
        }

        return null;
    }

    public static String signIn(String email, String password) {
        return serverAuthenticator.signIn(email, password);
    }
}
