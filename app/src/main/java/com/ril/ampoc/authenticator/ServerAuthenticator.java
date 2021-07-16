package com.ril.ampoc.authenticator;

import android.annotation.SuppressLint;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ServerAuthenticator implements IServerAuthenticator {

    private static final Map<String, String> credentials;
    public static final Map<String, String> loggedInUsers;

    static {
        credentials = new HashMap<>();
        credentials.put("demo@example.com", "demo");
        credentials.put("foo@example.com", "foobar");
        credentials.put("user@example.com", "pass");

        loggedInUsers = new HashMap<>();
    }

    @Override
    public String signUp(String email, String username, String password) {
        // TODO: register new user on the server and return its auth token
        return null;
    }

    @SuppressLint("NewApi")
    @Override
    public String signIn(String email, String password) {
        String authToken = null;
        final DateFormat df = SimpleDateFormat.getDateInstance();

        if (credentials.containsKey(email) &&
                password.equals(credentials.getOrDefault(email, ""))) {
            authToken = email + "-" + df.format(new Date());
        }

        return authToken;
    }
}
