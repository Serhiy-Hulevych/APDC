package com.example.myapplication;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.preference.PreferenceManager;
import android.content.SharedPreferences.Editor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;
    private String answer;
    private String stats;
    private String users;
    private String wrong;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        String username = prefs.getString("username", "Default Value if not found");
        String password = prefs.getString("password", "");
        long validade = prefs.getLong("validade",-1);
        String role = prefs.getString("role","");

        long time = System.currentTimeMillis();

        System.out.println("tempo que falta: "+((time - validade) * -1));

        if((time - validade) < 0 && validade != -1) {
            if(role.equalsIgnoreCase("user")) {
                Intent intent = new Intent(this, HomeMap.class);
                startActivity(intent);
            }
            else if(role.equalsIgnoreCase("gbo")) {
                Set<String> temp = prefs.getStringSet("users",null);
                String[] passar = new String[temp.size()];
                System.arraycopy(temp.toArray(),0, passar, 0, temp.size());
                Intent intent = new Intent(this, HomeGBO.class);
                intent.putExtra("Usernames", passar);
                startActivity(intent);
            }
            else if(role.equalsIgnoreCase("gs")) {
                Intent intent = new Intent(this, HomeGS.class);
                startActivity(intent);
            }
        }

        else {
            Editor ed = prefs.edit();
            ed.clear().commit();
        }
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.email || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.login);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        Button mRegisterButton = (Button) findViewById(R.id.register);
        mRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                openRegisterUser();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        mContext = this;
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            //mEmailView.setError(getString(R.string.error_field_required));
            mEmailView.setError("Username required");
            focusView = mEmailView;
            cancel = true;
        }
        else if(TextUtils.isEmpty(password)) {
            mPasswordView.setError("Password required");
            focusView = mPasswordView;
            cancel = true;
        }
        else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(this,email, password);
            mAuthTask.execute((Void) null);
           // if(answer != null && answer.contains("tokenID"))
                //openInformation();
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return true;
        //       return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return true;
        //       return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI, ContactsContract.Contacts.Data.CONTENT_DIRECTORY),
                ProfileQuery.PROJECTION,
                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +  " = ?", new String[]{ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE},
                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }
        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };
        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, String> {

        private final String mEmail;
        private final String mPassword;
        private Context context  = null;

        UserLoginTask(Context context, String email, String password) {
            mEmail = email;
            mPassword = password;
            this.context=context;
        }

        /**
         * Cancel background network operation if we do not have network connectivity.
         */
        @Override
        protected void onPreExecute() {
            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo == null || !networkInfo.isConnected() ||
                    (networkInfo.getType() != ConnectivityManager.TYPE_WIFI
                            && networkInfo.getType() != ConnectivityManager.TYPE_MOBILE)) {
                // If no connectivity, cancel task and update Callback with null data.
                cancel(true);
            }
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                JSONObject credentials = new JSONObject();
                credentials.accumulate("username",mEmail);
                credentials.accumulate("password", mPassword);

                wrong = RequestsREST.doPOST(new URL("https://light-ratio-234221.appspot.com/rest/users/v3"),credentials);
                answer = RequestsREST.doPOST(new URL("https://light-ratio-234221.appspot.com/rest/login/v3"),credentials);
                stats = RequestsREST.doPOST(new URL("https://light-ratio-234221.appspot.com/rest/users/v2"),credentials);
                users = RequestsREST.doGET(new URL("https://light-ratio-234221.appspot.com/rest/users/v1"));
                return RequestsREST.doPOST(new URL("https://light-ratio-234221.appspot.com/rest/login/v3"),credentials);
            } catch (Exception e) {
                return e.toString();
            }
        }


        @Override
        protected void onPostExecute(final String result) {
            mAuthTask = null;
            showProgress(false);
            Log.e("debug: ",this.getClass().getName()+"\n\n\n\n\n");
            //se nao for null e porque login funcionou
            if(answer != null) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                Editor ed = prefs.edit();
                ed.putString("username", mEmail);
                ed.putString("password", mPassword);


                String role = "";

                try {
                    JSONObject statsO = new JSONObject(stats);
                    role = statsO.get("role").toString();

                    JSONObject answerO = new JSONObject(answer);
                    long validade = Long.parseLong(answerO.get("expirationData").toString());

                    JSONObject wrongO = new JSONObject(wrong);
                    long invalids = Long.parseLong(wrongO.get("failedLogs").toString());
                    System.out.println("Tentativas: "+invalids);

                    ed.putString("role", role);
                    ed.putLong("validade",validade);
                    ed.putLong("wrong",invalids);
                    ed.commit();
                }
                catch (Exception e) {
                    System.out.println("Deu erro algures a ir buscar o role\n");
                }

                if(role.equalsIgnoreCase("user")) {
                    System.out.println("Entrou no User\n");
                    try {
                        JSONObject statsO = new JSONObject(stats);
                        String morada = statsO.get("address").toString();
                        ed.putString("role",role);
                        ed.putString("address", morada);
                        ed.commit();
                        openMap(context);
                    }
                    catch (Exception e) {
                        System.out.println("Deu erro algures a ir buscar os stats\n");
                    }
                }

                else if(role.equalsIgnoreCase("gbo")) {
                    System.out.println("Entrou no GBO\n");
                    try {
                        Set<String> guardar = new HashSet<>();
                        JSONObject usersO = new JSONObject(users);
                        JSONArray usersList = usersO.getJSONArray("users");
                        String[] temp = new String[usersList.length()];
                        for(int i=0; i<usersList.length(); i++) {
                            temp[i] = usersList.get(i).toString();
                            guardar.add(temp[i]);
                        }

                        ed.putStringSet("users",guardar);
                        ed.commit();
                        openHomeGBO(context, temp);
                    }
                    catch (Exception e) {
                        System.out.println("Deu erro algures a ir buscar os users\n");
                    }
                }
                else if(role.equalsIgnoreCase("gs")) {
                    System.out.println("Entrou no GS\n");
                    openHomeGS(context);
                }

            }
            if (result != null) {
                JSONObject token = null;
                try  {
                    // We parse the result
                    token = new JSONObject(result);
                    Log.i("LoginActivity", token.toString());
                    SharedPreferences settings = getSharedPreferences("AUTHENTICATION", 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("tokenID", token.getString("tokenID"));
                    //editor.commit();
                    //finish();

                } catch (JSONException e) {
                    // WRONG DATA SENT BY THE SERVER
                    Log.e("Authentication",e.toString());
                }
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }

        private void openMap(Context context) {
            Intent intent = new Intent(context, HomeMap.class);
            //intent.putExtra("Address", morada);
            startActivity(intent);
        }

        private void openHomeGBO(Context context, String[] users) {
            Intent intent = new Intent(context, HomeGBO.class);
            intent.putExtra("Usernames", users);
            startActivity(intent);
        }

        private void openHomeGS(Context context) {
            Intent intent = new Intent(context, HomeGS.class);
            startActivity(intent);
        }

    }

    private void openRegisterUser() {
        Intent intent = new Intent(this, RegisterUserActivity.class);
        startActivity(intent);
    }
}

