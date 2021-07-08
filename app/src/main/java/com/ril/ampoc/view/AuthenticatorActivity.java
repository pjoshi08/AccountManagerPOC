package com.ril.ampoc.view;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.ril.ampoc.R;
import com.ril.ampoc.util.AccountUtils;
import com.ril.ampoc.util.TaskRunner;

import java.util.concurrent.Callable;

import static com.ril.ampoc.util.AccountUtils.ARG_ACCOUNT_NAME;
import static com.ril.ampoc.util.AccountUtils.ARG_ACCOUNT_TYPE;
import static com.ril.ampoc.util.AccountUtils.ARG_AUTH_TYPE;
import static com.ril.ampoc.util.AccountUtils.KEY_ERROR_MESSAGE;
import static com.ril.ampoc.util.AccountUtils.PARAM_USER_PASS;
import static com.ril.ampoc.util.AccountUtils.signIn;

public class AuthenticatorActivity extends AccountAuthenticatorActivity {

    private final int REQ_SIGNUP = 1;
    private final String TAG = "AuthActivity";

    private AccountManager accountManager;

    private String username;
    private String password;
    private String authTokenType;

    private EditText edEmail;
    private EditText edPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        accountManager = AccountManager.get(this);

        String accountName = getIntent().getStringExtra(ARG_ACCOUNT_NAME);
        authTokenType = getIntent().getStringExtra(ARG_AUTH_TYPE);

        if (authTokenType == null)
            authTokenType = AccountUtils.AUTHTOKEN_TYPE_FULL_ACCESS;

        edEmail = findViewById(R.id.username);
        edPassword = findViewById(R.id.password);

        findViewById(R.id.signIn).setOnClickListener(v -> attemptLogin());
    }

    public void attemptLogin() {
        username = edEmail.getText().toString();
        password = edPassword.getText().toString();

        final String accountType = getIntent().getStringExtra(ARG_ACCOUNT_TYPE);

        TaskRunner runner = new TaskRunner();
        runner.executeAsync(new LongRunningTask(accountType), (intent) -> {
            Log.d(TAG, "> Authentication Complete");
            if (intent.hasExtra(KEY_ERROR_MESSAGE)) {
                Toast.makeText(getBaseContext(), intent.getStringExtra(KEY_ERROR_MESSAGE), Toast.LENGTH_SHORT).show();
            } else {
                finishLogin(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_SIGNUP && resultCode == RESULT_OK) {
            finishLogin(data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void finishLogin(Intent intent) {
        Log.d(TAG, "finishLogin");

        final String accountName  = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        final String accPassword = intent.getStringExtra(PARAM_USER_PASS);
        final Account account = new Account(accountName,
                intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));
        String authToken = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);

        if (getIntent().getBooleanExtra(AccountUtils.ARG_IS_ADDING_NEW_ACCOUNT, false)) {
            Log.d(TAG, "finishLogin > addAccountExplicityly");
            accountManager.addAccountExplicitly(account, accPassword, null);
            accountManager.setAuthToken(account, authTokenType, authToken);
        } else {
            Log.d(TAG, "> finishLogin > setPassword");
            accountManager.setPassword(account, accPassword);
        }

        setAccountAuthenticatorResult(intent.getExtras());
        setResult(AccountAuthenticatorActivity.RESULT_OK, intent);

        finish();
    }

    public class LongRunningTask implements Callable<Intent> {
        private final String accountType;
        public LongRunningTask(String accountType) {
            this.accountType = accountType;
        }

        @Override
        public Intent call() {
            Log.d(TAG, "> Started Authenticating");
            String authToken;
            Bundle data = new Bundle();
            try {
                authToken = signIn(username, password);

                data.putString(AccountManager.KEY_ACCOUNT_NAME, username);
                data.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
                data.putString(AccountManager.KEY_AUTHTOKEN, authToken);
                data.putString(PARAM_USER_PASS, password);

                Log.d(TAG, "Background Task: " + authToken);
            } catch (Exception e) {
                data.putString(KEY_ERROR_MESSAGE, e.getMessage());
            }

            final Intent res = new Intent();
            res.putExtras(data);
            return res;
        }
    }
}