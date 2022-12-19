package com.grampower.survey.Syncing;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.os.Bundle;

/**
 * That is require for Authenticator service class.
 * Implement AbstractAccountAuthenticator and stub out all
 * of its methods
 *
 * @author hemant
 */
public class Authenticator extends AbstractAccountAuthenticator {

    /**
     * Simple constructor
     * @param context Base context instance
     */
    public Authenticator(Context context) {
        super(context);
    }

    /**
     * Editing properties is not supported
     * @param r response instance
     * @param s string
     * @return Bundle instance
     */
    @Override
    public Bundle editProperties(
            AccountAuthenticatorResponse r, String s) {
        throw new UnsupportedOperationException();
    }

    /**
     * Don't add additional accounts
     * @param r Response
     * @param s string
     * @param s2 string
     * @param strings String array
     * @param bundle Bundle instance
     * @return Bundle instance
     * @throws NetworkErrorException
     */
    @Override
    public Bundle addAccount(
            AccountAuthenticatorResponse r,
            String s,
            String s2,
            String[] strings,
            Bundle bundle) throws NetworkErrorException {
        return null;
    }

    /**
     * Ignore attempts to confirm credentials
     * @param r Response
     * @param account Account instance
     * @param bundle Bundle instance
     * @return Bundle instance
     * @throws NetworkErrorException
     */
    @Override
    public Bundle confirmCredentials(
            AccountAuthenticatorResponse r,
            Account account,
            Bundle bundle) throws NetworkErrorException {
        return null;
    }

    /**
     *  Getting an authentication token is not supported
     * @param r Response
     * @param account Account instance
     * @param s String
     * @param bundle Budle instance
     * @return Bundle instance
     * @throws NetworkErrorException
     */
    @Override
    public Bundle getAuthToken(
            AccountAuthenticatorResponse r,
            Account account,
            String s,
            Bundle bundle) throws NetworkErrorException {
        throw new UnsupportedOperationException();
    }

    /**
     * Getting a label for the auth token is not supported
     * @param s String
     * @return String
     */
    @Override
    public String getAuthTokenLabel(String s) {
        throw new UnsupportedOperationException();
    }

    /**
     *  Updating user credentials is not supported
     * @param r Response
     * @param account Account instance
     * @param s string
     * @param bundle Bundle instance
     * @return Bundle instance
     * @throws NetworkErrorException
     */
    @Override
    public Bundle updateCredentials(
            AccountAuthenticatorResponse r,
            Account account,
            String s, Bundle bundle) throws NetworkErrorException {
        throw new UnsupportedOperationException();
    }

    /**
     * Checking features for the account is not supported
     * @param r Response
     * @param account Account instance
     * @param strings string array
     * @return Bundle instance
     * @throws NetworkErrorException
     */
    @Override
    public Bundle hasFeatures(
            AccountAuthenticatorResponse r,
            Account account, String[] strings) throws NetworkErrorException {
        throw new UnsupportedOperationException();
    }
}