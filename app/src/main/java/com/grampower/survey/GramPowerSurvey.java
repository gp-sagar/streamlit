package com.grampower.survey;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Application level context for the app.
 *
 * @author diadatp
 */
public class GramPowerSurvey extends Application{

    public static final String PREFERENCES_STRING = "grampowersurvey";

    /**
     * this check that user is logged in or not
     */
    public static final String IS_LOGIN = "isUserSignedIn";

    /**
     * initially value of username save into session
     */
    public static final String User = "username";

    /**
     * initially value of password save into session
     */
    public static final String Pass = "password";


    /**
     * Makes this class a singleton. Easy to access this way. No messy globals.
     */
    private static GramPowerSurvey singleton;

    /**
     * Getter for the singleton reference.
     *
     * @return the only instance of the application
     */
    public static GramPowerSurvey getInstance(){
        return singleton;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        singleton = this;
    }

    public void logout() {
        SharedPreferences sharedPreferences = getSharedPreferences(GramPowerSurvey.PREFERENCES_STRING, Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor =  sharedPreferences.edit();
        editor.putBoolean("isUserSignedIn", false);
        editor.putString("username", "");
        editor.putString("password", "");
        editor.commit();
    }
}
