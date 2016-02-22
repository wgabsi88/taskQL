package com.example.beuth.taskql.activities;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import com.example.beuth.taskql.helperClasses.Connection;
import com.example.beuth.taskql.helperClasses.ApplicationParameters;
import com.example.beuth.taskql.viewClasses.CustomNavRow;
import com.example.beuth.tasql.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
/**
 * Project Activity to list projects for an registered user
 * @author Wael Gabsi, Stefan Völkel
 */
public class ProjectActivity extends AppCompatActivity {
    private final static String NANOME_SESSIONID = "nanomeSessionId";
    private static final String PREFS_NAME = "LoginPrefs";
    private List<JSONObject> projects = new ArrayList<JSONObject>();
    private List<String> projectNames = new ArrayList<String>();
    private Connection connection;
    private ArrayAdapter<String> adapter;
    private GetProjectsTask getProjectsTask;
    private ListView listView;
    private ApplicationParameters applicationParameters;
    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private ArrayAdapter<String> mAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private String mActivityTitle;
    String[] osArray =  new String[10];
    AlertDialog.Builder builder ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projects);
        listView = (ListView) findViewById(R.id.projectListView);
        applicationParameters = ApplicationParameters.getInstance();
        mDrawerList = (ListView)findViewById(R.id.navList);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        connection = new Connection(getApplicationContext());
        mActivityTitle = getTitle().toString();
        builder = new AlertDialog.Builder(this);
        addDrawerItems();
        setupDrawer();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, projectNames);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    String projectId = projects.get(position).getString("id");
                    navigateToSubProjectActivity(projectId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        getProjectsTask = new GetProjectsTask();
        getProjectsTask.execute();
    }

    /**
     * Represents an asynchronous getProjects task to get all projects from
     * the logged-in user.
     */
    private class GetProjectsTask extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... params) {
            String sessionId = applicationParameters.getSessionId();
            String serverResponse = null;
            try {
                serverResponse = connection.doPostRequestWithAdditionalHeader("https://" + applicationParameters.getServerUrl() + "/rest/api/1/project/getAll", NANOME_SESSIONID + "=" + sessionId);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return serverResponse;
        }



        protected void onPostExecute(String results) {
            if (results != null) {
                try {
                    JSONObject project = new JSONObject(results);
                    JSONArray projectsFromJson = project.getJSONArray("projects");
                    adapter.clear();
                    projectNames.clear();
                    for (int i = 0; i < projectsFromJson.length(); i++) {
                        JSONObject projectsJson = projectsFromJson.getJSONObject(i);
                        projects.add(projectsJson);
                        projectNames.add(projectsJson.getString("title"));
                    }
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Navigate from ProjectActivity to SubProjectActivity
     * @param projectId
     */
    private void navigateToSubProjectActivity(String projectId){
        Intent subProjectIntent = new Intent(getApplicationContext(),SubProjectActivity.class);
        subProjectIntent.putExtra("projectId", projectId);
        subProjectIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(subProjectIntent);
    }

    /**
     * Add drawer items for logout and imprint
     */
    private void addDrawerItems() {
        osArray = new String[]{"Abmelden", "Impressum"};
        Integer[] imageId = {
                R.drawable.logouticon,
                R.drawable.info


        };
        CustomNavRow adapter = new
                CustomNavRow(ProjectActivity.this, osArray, imageId);

        mDrawerList.setAdapter(adapter);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch(position) {
                    case 0:
                            builder.setTitle(getString(R.string.log_out_dialog_title))
                                .setMessage(getString(R.string.log_out_dialog_message))
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                                        SharedPreferences.Editor editor = settings.edit();
                                        editor.clear();
                                        editor.commit();
                                        Intent homeIntent = new Intent(getApplicationContext(),LoginActivity.class);
                                        startActivity(homeIntent);

                                    }
                                })
                                .setNegativeButton("Nein", null)
                                .show();

                        break;
                    case 1:
                        Toast.makeText(ProjectActivity.this, getString(R.string.impressum), Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(ProjectActivity.this, "you didnt clicked", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    /**
     * Setup drawer for logout and imprint dialogs
     */
    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("TaskQl");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mActivityTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        // Activate the navigation drawer toggle
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}