package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.content.Context;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import org.json.JSONObject;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

public class HomeGBO extends AppCompatActivity {

    private int posDelete;
    private boolean doIt = false;
    private UserDeleteTask delete;
    private List<String> usernames;
    private Context context;
    private boolean atualUser = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_gbo);

        usernames = new LinkedList<>();

        String[] temp = getIntent().getExtras().getStringArray("Usernames");

        for(int i = 0; i< temp.length; i++) {
            usernames.add(temp[i]);
        }


        ListView lv = (ListView) findViewById(R.id.listview);
        lv.setAdapter(new MyListAdaper(this, R.layout.list_item, usernames));
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(HomeGBO.this, "List item was clicked at " + position, Toast.LENGTH_SHORT).show();
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor ed = prefs.edit();
                long newLimit = System.currentTimeMillis() + 60*5*1000;
                ed.putLong("validade",newLimit);
                ed.commit();
            }
        });

        context = this;
       /* Button mEmailSignInButton = (Button) findViewById(R.id.logout);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                redirectLogin(context);
            }
        });*/

        Button mCreateUser = (Button) findViewById(R.id.createUser);
        mCreateUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });

        Button mCreateAuser = (Button) findViewById(R.id.createAuser);
        mCreateAuser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerAuser();
            }
        });

        Button mCreateGS = (Button) findViewById(R.id.createGS);
        mCreateGS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerGS();
            }
        });

        RelativeLayout parent = (RelativeLayout) findViewById(R.id.parent);
        parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetTime();
            }
        });

        ScrollView tela = (ScrollView) findViewById(R.id.tela);
        tela.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetTime();
            }
        });


    }











    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        String nome = (String) item.getTitle();

        resetTime();

        if(nome.equalsIgnoreCase("account"))
            redirectChangeSettings(context);
        else if(nome.equalsIgnoreCase("logout"))
            redirectLogin(context);

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void  redirectChangeSettings(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor ed = prefs.edit();
        long newLimit = System.currentTimeMillis() + 60*5*1000;
        ed.putLong("validade",newLimit);
        ed.commit();
        Intent intent = new Intent(context, ChangeSettingsActivity.class);
        startActivity(intent);
    }

    private class MyListAdaper extends ArrayAdapter<String> {
        private int layout;
        private List<String> mObjects;
        private MyListAdaper(Context context, int resource, List<String> objects) {
            super(context, resource, objects);
            mObjects = objects;
            layout = resource;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder mainViewholder = null;
            if(convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(layout, parent, false);
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.thumbnail = (ImageView) convertView.findViewById(R.id.list_item_thumbnail);
                viewHolder.title = (TextView) convertView.findViewById(R.id.list_item_text);
                viewHolder.button = (Button) convertView.findViewById(R.id.list_item_btn);
                convertView.setTag(viewHolder);
            }
            mainViewholder = (ViewHolder) convertView.getTag();
            mainViewholder.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(getContext(), "Button was clicked for list item " + position, Toast.LENGTH_SHORT).show();
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                    SharedPreferences.Editor ed = prefs.edit();
                    long newLimit = System.currentTimeMillis() + 60*5*1000;
                    ed.putLong("validade",newLimit);
                    ed.commit();
                    deleteUser(position);
                }
            });
            mainViewholder.title.setText(getItem(position));

            return convertView;
        }
    }
    public class ViewHolder {

        ImageView thumbnail;
        TextView title;
        Button button;
    }

    private void deleteUser(int pos) {
        posDelete = pos;
        doIt = true;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        try {
            String parametro = "{ \"username\":"+"\""+usernames.get(pos)+"\" }";
            JSONObject userDelete = new JSONObject(parametro);

            if(prefs.getString("username","").equalsIgnoreCase(usernames.get(pos)))
                atualUser = true;

            usernames.remove(pos);
            delete = new UserDeleteTask(this, userDelete);
            delete.execute((Void) null);
        } catch (Exception e) {
            System.out.print("Erro algures a criar o  user para apagar\n");
        }
    }

    private void  redirectLogin(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor ed = prefs.edit();
        ed.clear().commit();
        Intent intent = new Intent(context, LoginActivity.class);
        startActivity(intent);
    }

    private void registerUser() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor ed = prefs.edit();
        long newLimit = System.currentTimeMillis() + 60*5*1000;
        ed.putLong("validade",newLimit);
        ed.commit();
        Intent intent = new Intent(this, RegisterUserActivity.class);
        startActivity(intent);
    }

    private void registerAuser() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor ed = prefs.edit();
        long newLimit = System.currentTimeMillis() + 60*5*1000;
        ed.putLong("validade",newLimit);
        ed.commit();
        Intent intent = new Intent(this, RegisterAuserActivity.class);
        startActivity(intent);
    }

    private void registerGS() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor ed = prefs.edit();
        long newLimit = System.currentTimeMillis() + 60*5*1000;
        ed.putLong("validade",newLimit);
        ed.commit();
        Intent intent = new Intent(this, RegisterGSActivity.class);
        startActivity(intent);
    }

    private void resetTime() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor ed = prefs.edit();
        long newLimit = System.currentTimeMillis() + 60*5*1000;
        ed.putLong("validade",newLimit);
        ed.commit();
    }












    public class UserDeleteTask extends AsyncTask<Void, Void, String> {

        private JSONObject delete;
        private Context context;

        UserDeleteTask(Context context, JSONObject delete) {
            this.delete = delete;
            this.context = context;
        }

        @Override
        protected String doInBackground(Void... params) {

            try {

                return RequestsREST.doPOST(new URL("https://light-ratio-234221.appspot.com/rest/delete/v1"), delete);

            } catch (Exception e) {
                System.out.println("Erro algures no background\n");
                return e.toString();
            }
        }

        @Override
        protected void onPostExecute(final String result) {
            try {
                if(atualUser)
                    redirectLogin(context);
                else
                    redirectHomeGBO(context);
            }
            catch (Exception e) {
                System.out.println("Erro algures no postExecute\n");
            }
        }

        private void  redirectHomeGBO(Context context) {
            String[] temp = new String[usernames.size()];

            for(int i = 0; i< usernames.size(); i++) {
                temp[i] = usernames.get(i);
            }

            Intent intent = new Intent(context, HomeGBO.class);
            intent.putExtra("Usernames", temp);
            startActivity(intent);
        }

    }

}
