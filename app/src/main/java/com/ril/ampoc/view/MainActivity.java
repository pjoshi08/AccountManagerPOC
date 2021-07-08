package com.ril.ampoc.view;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.OperationCanceledException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.ril.ampoc.R;
import com.ril.ampoc.util.AccountUtils;
import com.ril.ampoc.util.AuthPrefs;
import com.ril.ampoc.util.LongRunningTask;
import com.ril.ampoc.util.TaskRunner;

import java.util.function.Function;

import static com.ril.ampoc.util.AccountUtils.ACCOUNT_TYPE;
import static com.ril.ampoc.util.AccountUtils.AUTHTOKEN_TYPE_FULL_ACCESS;

public class MainActivity extends AppCompatActivity {

    private static final String STATE_DIALOG = "state_dialog";
    private static final String STATE_INVALIDATE = "state_invalidate";

    private String TAG = "MainActivity";
    public static final int REQ_SIGNUP =  1;

    private AccountManager accountManager;
    private AuthPrefs authPrefs;

    private TextView text1;
    private TextView text2;
    private TextView text3;
    private AlertDialog alertDialog;
    private boolean invalidate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text1 = findViewById(R.id.text1);
        text2 = findViewById(R.id.text2);
        text3 = findViewById(R.id.text3);

        authPrefs = new AuthPrefs(this);
        accountManager = AccountManager.get(this);

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

        if (savedInstanceState != null) {
            boolean showDialog = savedInstanceState.getBoolean(STATE_DIALOG);
            boolean invalidate = savedInstanceState.getBoolean(STATE_INVALIDATE);
            if (showDialog) {
                showAccountPicker(AUTHTOKEN_TYPE_FULL_ACCESS, invalidate);
            }
        }

        /*accountManager.getAuthTokenByFeatures(
                AccountUtils.ACCOUNT_TYPE,
                AccountUtils.AUTH_TOKEN_TYPE,
                null,
                this,
                null,
                null,
                new GetAuthTokenCallback(),
                null
        );*/
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (alertDialog != null && alertDialog.isShowing()) {
            outState.putBoolean(STATE_DIALOG, true);
            outState.putBoolean(STATE_INVALIDATE, invalidate);
        }
    }

    private void getTokenForAccountCreateIfNeeded(String accountType, String authTokenType) {
        accountManager.getAuthTokenByFeatures(
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
        this.invalidate = invalidate;

        final Account[] availableAccounts = accountManager.getAccountsByType(ACCOUNT_TYPE);

        int len = availableAccounts.length;
        String[] names = new String[len];
        if (len == 0) {
            showMessage("No Accounts");
            return;
        } else {
            for (int i = 0; i < len; i++) {
                names[i] = availableAccounts[i].name;
            }
        }

        alertDialog = new AlertDialog.Builder(this)
                .setTitle("Pick Account")
                .setAdapter(
                        new ArrayAdapter<>(
                                getBaseContext(), android.R.layout.simple_list_item_1, names
                        ),
                        (dialog, which) -> {
                            if (invalidate) {
                                invalidateAuthToken(availableAccounts[which], authTokenType);
                            } else {
                                getExistingAuthToken(availableAccounts[which], authTokenType);
                            }
                        })
                .create();
        alertDialog.show();
    }

    private void getExistingAuthToken(Account availableAccount, String authTokenType) {
        final AccountManagerFuture<Bundle> future = accountManager.getAuthToken(
                availableAccount, authTokenType,
                null, this,
                null, null);

        new TaskRunner()
                .executeAsync(
                        new LongRunningTask( // DoInBackground
                                (Function<AccountManagerFuture<Bundle>, String>) input -> getAuthTokenFromAccountManagerFuture(input),
                                future
                        ), (authToken) -> { // OnComplete
                            showMessage("Existing Auth Token: " + authToken);
                            Log.d(TAG, "Existing Auth Token: " + authToken);
                        });
    }

    private void invalidateAuthToken(Account account, String authTokenType) {
        final AccountManagerFuture<Bundle> future = accountManager.getAuthToken(
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
                            accountManager.invalidateAuthToken(account.type, (String) authToken);
                            showMessage(account.name + " invalidated!");
                            Log.d(TAG, account.name + " invalidated!");
                        });
    }

    private void addNewAccount(String accountType, String authTokenType) {
        accountManager.addAccount(
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

    private class GetAuthTokenCallback implements AccountManagerCallback<Bundle> {

        @Override
        public void run(AccountManagerFuture<Bundle> result) {
            Bundle bundle;

            try {
                bundle = result.getResult();

                final Intent intent = (Intent) bundle.get(AccountManager.KEY_INTENT);
                if (null != intent) {
                    startActivityForResult(intent, REQ_SIGNUP);
                } else {
                    String authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
                    final String accountName = bundle.getString(AccountManager.KEY_ACCOUNT_NAME);

                    // Save session username & auth token
                    authPrefs.setAuthToken(authToken);
                    authPrefs.setUserName(accountName);

                    text1.setText("Retrieved auth token: " + authToken);
                    text2.setText("Saved account name:" + authPrefs.getAccountName());
                    text3.setText("Saved auth token: " + authPrefs.getAuthToken());

                    // If the logged account didn't exist, we need to create it on the device
                    Account account = AccountUtils.getAccount(MainActivity.this, accountName);
                    if (null == account) {
                        account = new Account(accountName, AccountUtils.ACCOUNT_TYPE);
                        accountManager.addAccountExplicitly(account,
                                bundle.getString(AccountUtils.PARAM_USER_PASS),
                                null
                        );
                        accountManager.setAuthToken(account, AccountUtils.AUTH_TOKEN_TYPE, authToken);
                    }
                }

            } catch (OperationCanceledException e) {
                finish();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}