package com.ril.ampoc.authenticator;

public interface IServerAuthenticator {

    String signUp(final String email, final String username, final String password);

    String signIn(final String email, final String password);
}
