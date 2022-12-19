package com.grampower.survey.Syncing;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.grampower.survey.GramPowerSurvey;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

/**
 * Handle the transfer of data between a server and an
 * app, using the Android sync adapter framework.
 *
 * @author hemant
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    /**
     * Constant string for sync adapter that use for Log information
     */
    private static final String TAG = "GP SyncAdapter";

    /**
     * Server URL where sync data
     */
    String upLoadServerUri = null;

    /**
     * Response code from server.
     */
    int serverResponseCode = 0;

    /**
     * preferences manager
     */
    SharedPreferences sharedPreferences;

    /**
     * String that store login user
     */
    String mAccessUser;

    /**
     * Define a variable to contain a content resolver instance
     */
    ContentResolver mContentResolver;

    /**
     * Set up the sync adapter
     */
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */

        System.out.println("sync adapter constructor called");
        mContentResolver = context.getContentResolver();
    }

    /**
     * Set up the sync adapter. This form of the
     * constructor maintains compatibility with Android 3.0
     * and later platform versions
     */
    public SyncAdapter(
            Context context,
            boolean autoInitialize,
            boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        System.out.println("sync adapter second constructor called");
        mContentResolver = context.getContentResolver();

    }

    /**
     * Specify the code you want to run in the sync adapter. The entire
     * sync adapter runs in a background thread, so you don't have to set
     * up your own background processing.
     */
    @Override
    public void onPerformSync(
            Account account,
            Bundle extras,
            String authority,
            ContentProviderClient provider,
            SyncResult syncResult) {

        Log.d(TAG, "onPerformSync called.");

        //System.out.println("On perform Sync method called and perform data tarnsfer task");
        upLoadServerUri = "http://192.168.1.205:8000/gp_dongle/upload_survey";

        /**
          * Put the data transfer code here.
          */

//        StoreLogs sl= new StoreLogs(getContext());
        ContextWrapper contextWrapper = new ContextWrapper(getContext());
        sharedPreferences = contextWrapper.getSharedPreferences(GramPowerSurvey.PREFERENCES_STRING, Context.MODE_MULTI_PROCESS);
        mAccessUser = getAccessUser();

        try{
            File directory = new File(Environment.getExternalStorageDirectory() + "/GramPowerSurvey");
            String message;
            System.out.println("Syncing directory"+directory);
            System.out.println("get access user "+mAccessUser);

//            // FileFilter for user sync only your log files //
//            FilenameFilter mEndsWith = new FilenameFilter()
//            {
//                public boolean accept(File directory, String filename) {
//                    return filename.endsWith(".csv");
//                }
//            };
//            System.out.println("begin with " + mBeginsWith);
            File[] files = directory.listFiles();
            Calendar cal =Calendar.getInstance();
            int response;

            for (File file : files) {
                System.out.println("Syncing filename"+file);
//                String filename =   mAccessUser +"_"+ getDate() + ".log";
              //  String filename = "hemant";
               // if(!file.toString().endsWith(filename)) { // upload all data to server except today's data  and delete the files //
                    message = "";
                    message = reading(file);

                    response = uploadFile(file, message);
                    if(response == 200) {
                        System.out.println("file is uploaded");
                    }
                //}

            }

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

    }

    /**
     * that function read data from internal storage(device memory)
     * @param write_file filename which read the logs
     * @return log data
     */
    public String reading(File write_file) {
        String aDataRow = "";
        String aBuffer = "";//store reading data into buffer //

        try {

            FileInputStream fis = new FileInputStream(write_file);
            DataInputStream in = new DataInputStream(fis);
            BufferedReader myReader = new BufferedReader(new InputStreamReader(in));

            while ((aDataRow = myReader.readLine()) != null) {
                aBuffer += aDataRow + "\n";
            }
            myReader.close();
            fis.close();

//            ctx.deleteFile(FILENAME);
            System.out.println("read log data");
//            boolean be = deleteFile(write_file);// delete that file after reading data //
//            Toast.makeText(StoreLogs.this, "read Log data ", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            //Toast.makeText(StoreLogs.this, "exception occured Log reading", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        System.out.println("Data are "+ aBuffer);
        return aBuffer;
    }

    /**
     *Get the login user from session
     * @return String
     */
    public String getAccessUser()
    {
        String mUser = sharedPreferences.getString(GramPowerSurvey.User,"");
//        System.out.println("get User are "+mUser);
        return mUser;
    }

    /**
     *  Upload log data to server and get response from server
     * @param file log filename
     * @param data string data of log file
     * @return Integer response
     */
    public int uploadFile(File file,String data) {

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        DataInputStream dis = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead;

        int maxBufferSize = 1 * 1024 * 1024;
        byte[] buffer=new byte[maxBufferSize];

        try {
            System.out.println("file is  "+file);

            // open a URL connection to the Servlet
            URL url = new URL(upLoadServerUri);

            // Open a HTTP  connection to  the URL
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true); // Allow Inputs
            conn.setDoOutput(true); // Allow Outputs
            conn.setUseCaches(false); // Don't use a Cached Copy
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("ENCTYPE", "multipart/form-data");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            conn.setRequestProperty("uploaded_file", file.toString());

            dos=new DataOutputStream(conn.getOutputStream());

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name="+"uploaded_file"
                    +";filename="+ file + "" + lineEnd);

            dos.writeBytes(lineEnd);
            buffer=data.getBytes();
            maxBufferSize=buffer.length;
            dos.write(buffer, 0, maxBufferSize);

//                System.out.println("write buffer size is    " + maxBufferSize);
//
            System.out.println("write buffer    " + new String(buffer));

            // send multipart form data necesssary after file data...
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            dos.flush();
            dos.close();

            dis=new DataInputStream(conn.getInputStream());
            byte[]buffer1 = new byte[6];
            bytesRead= dis.read(buffer1);
            System.out.println("\nserver data are " + new String(buffer1));
            System.out.println("\nserver data length   " + bytesRead);
            dis.close();

            // Responses from the server (code and message)
            serverResponseCode = conn.getResponseCode();
            String serverResponseMessage = conn.getResponseMessage();


            Log.i("\nuploadFile", "HTTP Response is : "
                    + serverResponseMessage + ": " + serverResponseCode);

        } catch (MalformedURLException ex) {

            ex.printStackTrace();
            Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
        }
        catch (Exception ex) {

            ex.printStackTrace();
            Log.e("Upload file to server", "error message" + ex.getMessage(), ex);

        }
        return serverResponseCode;

    } // End else block

}