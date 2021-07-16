package com.ril.ampoc.view;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.ril.ampoc.R;
import com.ril.ampoc.model.User;
import com.ril.ampoc.util.AuthPrefs;
import com.ril.ampoc.util.LongRunningTask;
import com.ril.ampoc.util.TaskRunner;

import java.util.function.Function;

import static com.ril.ampoc.util.AccountUtils.ACCOUNT_TYPE;
import static com.ril.ampoc.util.AccountUtils.AUTHTOKEN_TYPE_FULL_ACCESS;

public class MainActivity extends AppCompatActivity {

    private String TAG = "MainActivity";
    public static final int REQ_SIGNUP =  1;

    private AccountManager manager;
    private AuthPrefs authPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authPrefs = new AuthPrefs(this);
        manager = AccountManager.get(this);

        findViewById(R.id.btnAddAccount).setOnClickListener(v -> {
            addNewAccount(ACCOUNT_TYPE, AUTHTOKEN_TYPE_FULL_ACCESS);
        });

        findViewById(R.id.btnGetAuthToken).setOnClickListener(v -> {
            showAccountPicker(AUTHTOKEN_TYPE_FULL_ACCESS, false);
        });

        findViewById(R.id.btnGetAuthTokenConvenient).setOnClickListener(v -> {
            getTokenForAccountCreateIfNeeded(ACCOUNT_TYPE, AUTHTOKEN_TYPE_FULL_ACCESS);
        });

        findViewById(R.id.btnInvalidateAuthToken).setOnClickListener(v -> {
            showAccountPicker(AUTHTOKEN_TYPE_FULL_ACCESS, true);
        });

        findViewById(R.id.btnSendData).setOnClickListener(v -> {
            sendAccountDetails(AUTHTOKEN_TYPE_FULL_ACCESS);
        });
    }

    private void getTokenForAccountCreateIfNeeded(String accountType, String authTokenType) {
        manager.getAuthTokenByFeatures(
                accountType,
                authTokenType,
                null,
                null,
                null,
                null,
                future -> {
                    String authToken = getAuthTokenFromAccountManagerFuture(future);
                    Log.d(TAG, "GetTokenForAccountIfNeeded: " + authToken);
                },
                null
        );
    }

    private void showAccountPicker(String authTokenType, boolean invalidate) {

        final Account[] availableAccounts = manager.getAccountsByType(ACCOUNT_TYPE);

        if (availableAccounts.length == 0) {
            showMessage("No Accounts");
            return;
        }

        if (invalidate) {
            invalidateAuthToken(availableAccounts[0], authTokenType);
        } else {
            getExistingAuthToken(availableAccounts[0], authTokenType);
        }
    }

    private void getExistingAuthToken(Account availableAccount, String authTokenType) {
        final AccountManagerFuture<Bundle> future = manager.getAuthToken(
                availableAccount, authTokenType,
                null, this,
                null, null);

        new TaskRunner()
                .executeAsync(
                        new LongRunningTask( // DoInBackground
                                (Function<AccountManagerFuture<Bundle>, String>)
                                        this::getAuthTokenFromAccountManagerFuture,
                                future
                        ), (authToken) -> { // OnComplete
                            showMessage("Existing Auth Token: " + authToken);
                            Log.d(TAG, "Existing Auth Token: " + authToken);
                        });
    }

    private void invalidateAuthToken(Account account, String authTokenType) {
        final AccountManagerFuture<Bundle> future = manager.getAuthToken(
                account,
                authTokenType,
                null,
                this,
                null,
                null
        );

        new TaskRunner()
                .executeAsync(
                        new LongRunningTask(
                                (Function<AccountManagerFuture<Bundle>, String>)
                                        this::getAuthTokenFromAccountManagerFuture,
                                future
                        ),
                        (authToken) -> { // On Complete
                            manager.invalidateAuthToken(account.type, (String) authToken);
                            showMessage(account.name + " invalidated!");
                            Log.d(TAG, account.name + " invalidated!");
                        });
    }

    private void addNewAccount(String accountType, String authTokenType) {
        manager.addAccount(
                accountType,
                authTokenType,
                null,
                null,
                this,
                future -> {
                    try {
                        Bundle bundle = future.getResult();
                        showMessage("Account was created");
                        Log.d(TAG, "AddNewAccount bundle: " + bundle);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                null
        );
    }


    private void showMessage(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_LONG).show());
    }

    private String getAuthTokenFromAccountManagerFuture(AccountManagerFuture<Bundle> future) {
        Bundle bundle;
        try {
            bundle = future.getResult();
            final String authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
            showMessage((authToken != null) ?
                    ("Success!\nToken: " + authToken) : "Fail!");
            return authToken;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void sendAccountDetails(String authTokenType) {
        Account availableAccount = manager.getAccountsByType(ACCOUNT_TYPE)[0];

        final AccountManagerFuture<Bundle> future = manager.getAuthToken(
                availableAccount, authTokenType,
                null, this,
                null, null);
        Log.d(TAG, ">>> Demo Password: " + manager.getPassword(availableAccount));

        new TaskRunner()
                .executeAsync(
                        new LongRunningTask (
                                (Function<AccountManagerFuture<Bundle>, User>)
                                        this::getAccountDetails,
                                future
                        ),
                        (user) -> sendData((User) user)
                );
    }

    private User getAccountDetails(AccountManagerFuture<Bundle> future) {
        Bundle bundle;
        try {
            bundle = future.getResult();
            final String authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
            String accountname = bundle.getString(AccountManager.KEY_ACCOUNT_NAME);
            String accPassword = bundle.getString(AccountManager.KEY_PASSWORD);
            showMessage((accountname != null) ?
                    ("Success!\nAccName: " + accountname) : "Fail!");
            Log.d(TAG, "> Password: " + accPassword);
            Log.d(TAG, "> AuthToken: " + authToken);
            return new User(accountname, accPassword, authToken);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void sendData(User user) {
        Intent intent = new Intent("aidlExample");
        //intent.putExtra("user", user);
        //intent.setPackage(IPCAidlInterface.class.getPackage().getName());
        intent.setComponent(new ComponentName("com.ril.childapp1", "com.ril.childapp1.service.UserService"));
    }

    public static User getUser() {
        return new User("demo@example.com", "demo", "demoAuth");
    }
}