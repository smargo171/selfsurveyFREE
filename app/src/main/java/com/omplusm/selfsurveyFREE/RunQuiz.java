package com.omplusm.selfsurveyFREE;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.ActionMode;
import android.view.Menu;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.view.View;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Toast;

import au.com.bytecode.opencsv.CSVWriter;

public class RunQuiz extends Activity implements InAppBilling.InAppBillingListener, SearchView.OnQueryTextListener {


    private ListView MyListView;
    private Cursor Cursor;
    private Toolbar toolbar;
    protected Object mActionMode;
    public static int selectedItem = -1;

    public static DatabaseHandler db;

    private boolean eulaDialogShown = false;

    public static ArrayList<String> setArrayList;

    QuerySetListCellArrayAdapter adapter;

    EditText searchQuest;

    public FileDialog fileDialog;
    private ProgressDialog pd;
    private Context context;

    //
    private Bundle savedInstanceState;


    //1= free version
    public Boolean freeVersion;



    public final static String EXTRA_MESSAGE = "com.omplusm.selfsurveyFREE.SELECTED_QUIZ";

    public final static int debugFlag = 0;
    public final static int debugBillingFlag = 0;

    private static final int RESULT_SETTINGS = 1;

    public File sdCardRoot;
    public String sdCardDirectory;
    public String exportDirLoc;

    //billing
    private InAppBilling	inAppBilling;

    // purchase request code
    private static final int	PURCHASE_REQUEST_CODE = 1;

    // product code
    private static final String	PRODUCT_SKU = "selfsurvey.blah";

    // google assigned application public key
    // NOTE: You must replace this key with your own key
    private static final String	applicationPublicKey =
            "blah";
    //end billing

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        this.savedInstanceState = savedInstanceState;
        setContentView(R.layout.activity_run_quiz);

        context = this ;
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        //removing eula dialog by setting shown flag
        eulaDialogShown = true;
        if (!eulaDialogShown) {
            eulaDialogShown = true;
            new SimpleEula(this).show();
        }



        //in app billing
        // first time we need to check if the product was already owned.
        // this should be done by the respective method which we had to implement in this activity because
        // we defined that this activity implements the billing class

        freeVersion = false;
        checkProduct();

        //makes no sense to check the status here as the call is async and the actual status change to true after the check is done a bit later
        //showToast("is free version?" + freeVersion);

        //get app name to tell if it's free or payed
        // to change between free and payed change
        //

        final PackageManager pm = getApplicationContext().getPackageManager();
        ApplicationInfo ai;
        try {
            ai = pm.getApplicationInfo( this.getPackageName(), 0);
        } catch (final PackageManager.NameNotFoundException e) {
            ai = null;
        }
        final String applicationName = (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");

        //freeVersion = applicationName.contains("FREE");






     afterCreate();
    }


    private void afterCreate(){

		//DatabaseHandler db = new DatabaseHandler(this);
        sdCardRoot = Environment.getExternalStorageDirectory();
        sdCardDirectory = sdCardRoot + "/Android/data/" + getApplicationContext().getPackageName();

        //init preferences - load default export directory
        initPref();


        //app directory is correct
        if (debugFlag == 1){
            Toast.makeText(RunQuiz.this,
                    sdCardDirectory, Toast.LENGTH_LONG).show();
            Log.d("QUIZREC: afterCreate get app directory ", sdCardDirectory);
        }

        db = new DatabaseHandler(this);



        setArrayList = (ArrayList<String>) db.getAllSets();

        adapter = new QuerySetListCellArrayAdapter(this,setArrayList,R.layout.list_row);

        if (db.getAllQuests().size() == 0){


            //new SimpleEula(this).show();

            /**
         * CRUD Operations
         * */
        // Inserting questions
            //tododo 6.05.14 we need to remove this and start from the empty db if needed, otherwise we need to create quest in other table ?
            // no, addQuest is taking care about it. We leave it to be on the safe side
            //so far we just make this insertion same as adding a new set
            ////Quest(int id, String set, String question, String answer, String status, int rank)
            //Result = db.addQuest((new Quest("New Set",
            Toast.makeText(RunQuiz.this,
                    "Initializing sample data...", Toast.LENGTH_LONG).show();

        //db.addQuest(new Quest("Sample", "How are you today?", "Perfect!","1",0));
        db.addQuest((new Quest("New Set", "New Question", "New Answer"," ",0)));

        //load sample set
        //first we need to decompress the quest file from apk asset folder
            File f = new File(getCacheDir()+"/Questions Daily Health.txt");
            if (!f.exists()) try {

                InputStream is = getAssets().open("Questions Daily Health.txt");
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();


                FileOutputStream fos = new FileOutputStream(f);
                fos.write(buffer);
                fos.close();
            } catch (Exception e) { throw new RuntimeException(e); }

            //can't use async task here as we close db somewhere in this thread and in the async import thread we can't access it
            //and get an exception
            //ImportDatabaseCSVTask task=new ImportDatabaseCSVTask();

            if (debugFlag == 1){
                Log.d("path to asset", f.getPath());
            }
            //task.execute(f.getPath(),"Import");
            ImportFile(f.getPath());
        }
        else {
           // new SimpleEula(this).show();

        }
 
        // Reading all questions
        if (debugFlag == 1){
            Log.d("Reading: ", "Reading all questions..");
        }
        List<Quest> quests = db.getAllQuests();

        if (debugFlag == 1){
        for (Quest cn : quests) {
            String log = "Id: "+cn.getID()+" ,Set: " + cn.getSet() + " ,Question: " + cn.getQuestion() + ", Answer: " + cn.getAnswer() + " Rank: " + String.valueOf(cn.getRank());
                // Writing questions to log
        Log.d("QUIZREC: ", log);

                }
        }

        //Cursor = db.getAllSetNamesCursor();
        //Log.d("QUIZREC: ", "getAllSetNamesCursor");

        MyListView = (ListView) findViewById(R.id.list);
        MyListView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        //MyListView.setFastScrollEnabled(true);
        ///Log.d("QUIZREC: app context", getApplicationContext().getPackageName());
        ///MyListView.setAdapter(new QuerySetListCellArrayAdapter()Adapter(getApplicationContext(), Cursor,db));
        MyListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        MyListView.setItemsCanFocus(false);
        
		
		//http://www.androidhive.info/2011/10/android-listview-tutorial/
		//http://stackoverflow.com/questions/2468100/android-listview-click-howto 
		// http://stackoverflow.com/questions/9097723/adding-a-onclicklistener-to-listview-android
		MyListView.setClickable(true);
		//?
		//MyListView.setLongClickable(true);


        //12.2.14 todo done ? we have exception that db or cursor is not closed
        //but here is wrong place to close the cursor
        //Cursor.close();
        db.close();



        //getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
        MyListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {

                if (debugFlag == 1){
                    Log.d("QUIZREC: long click happened", String.valueOf(position));
                }

                MyListView.setItemChecked(position, true);
                adapter.setSelected(position);
                //MyListView.getChildAt(position).setBackgroundColor(Color.parseColor("#33ffe5"));
                selectedItem = position;
                if (mActionMode != null) {
                    // I have handled the event and should not process t more. Otherwise normal click kicks-in??
                    return true;
                }


                // Start the CAB using the ActionMode.Callback defined above
                mActionMode = RunQuiz.this
                        .startActionMode(mActionModeCallback);
                view.setSelected(true);
                return true;
            }
        });



        MyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long arg) {
                if (debugFlag == 1){
                    Log.d("QUIZREC: short click", String.valueOf(position));
                }
                selectedItem = position;
                //MyListView.getChildAt(position).setBackgroundColor(Color.parseColor("#33b5e5"));
                MyListView.setItemChecked(position, true);
                doRunOne();


            }
        });

        //searchable
        //MyListView.setTextFilterEnabled(true);

    }

    @Override
    protected void onDestroy() {

        //billing
        if(inAppBilling != null) inAppBilling.dispose();
        // end billing method
        super.onDestroy();
    }

    @Override
    public void onResume() {
        // in we don't do this, the values which are not taken from the cursor in the list row are not refreshed
        //datasource.open();                      //change datasource to your own database class's object
        super.onResume();
        onCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.setting, menu);

        /*
        //searchable
        SearchManager searchManager = (SearchManager) getSystemService( Context.SEARCH_SERVICE );


        SearchView searchView = (SearchView) menu.findItem(R.id.menu_item_search).getActionView();

        //searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        //searchView.setSubmitButtonEnabled(true);
        //searchView.setOnQueryTextListener(this);
        */

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onQueryTextChange(String newText)
    {
        // this is your adapter that will be filtered
        if (TextUtils.isEmpty(newText))
        {
           // MyListView.clearTextFilter();
        }
        else
        {
           // MyListView.setFilterText(newText.toString());
        }

        return true;
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        // TODO Auto-generated method stub
        return false;
    }

    public void initPref (){
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);

        //SharedPreferences sharedPrefs = getSharedPreferences("settings", MODE_PRIVATE);

        String Exdir = sharedPrefs.getString("prefExportDir", "NULL");
        if (Exdir.equals("NULL")){

            if (debugFlag == 1){
                Log.d("QUIZREC: setting default pref ", sdCardDirectory);
            }
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString("prefExportDir",sdCardDirectory);
            editor.commit();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        initPref();


       // does not work - UserSettingFragment.onSharedPreferenceChanged(sharedPrefs, "prefExportDir");
        //String Exdir = sharedPrefs.getString("prefExportDir", "NULL");
        //Log.d("QUIZREC: setting default pref load", Exdir);

        /*Preference exportDirPref = findPreference(key);
        // Set summary to be the user-description for the selected value
        connectionPref.setSummary(sharedPreferences.getString(key, ""));
        */

        switch (item.getItemId()) {
            case R.id.menu_settings:
                Intent i = new Intent(this, SetPreferenceActivity.class);
                startActivityForResult(i, RESULT_SETTINGS);

                break;

        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // purchase request code
        if(inAppBilling != null && requestCode == PURCHASE_REQUEST_CODE)
        {
            // Pass on the activity result to inAppBilling for handling
            inAppBilling.onActivityResult(resultCode, data);
            showToast("Returned from billing activity GUI");
            //freeVersion=false;
        }
        else {
            switch (requestCode) {
                case RESULT_SETTINGS:
                    processUserSettings();
                    break;

            }
        }

    }


    private void processUserSettings() {
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);

        //SharedPreferences sharedPrefs = getSharedPreferences("settings", MODE_PRIVATE);

        if (debugFlag == 1){
        Toast.makeText(RunQuiz.this,
                sharedPrefs.getString("prefExportDir", "NULL"), Toast.LENGTH_LONG).show();
            if (debugFlag == 1){
                Log.d("QUIZREC: setting default pref new ", sharedPrefs.getString("prefExportDir", "NULL"));
            }
        }

        exportDirLoc = sharedPrefs.getString("prefExportDir", "NULL");

    }
    //http://www.vogella.com/articles/AndroidListView/article.html#listview_actionbar

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            // Assumes that you have "contexual.xml" menu resources
            inflater.inflate(R.menu.rowselection , menu);
            toolbar.setVisibility(View.GONE);
            return true;
        }

        // Called each time the action mode is shown. Always called after
        // onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_edit:
                    doEdit();
                    mode.finish();
                    return true;
                case R.id.action_run:
                    doRunOne();
                    mode.finish();
                    //MyListView.getAdapter();
                    return true;
                case R.id.action_showone:
                    doShowOne();
                    mode.finish();
                    return true;
                case R.id.action_export_results:
                    showToast("trying to export results. is free version? " + freeVersion);

                    //buyProductExport();

                    //showToast("after buy_product. is free version? " + freeVersion);

                    if (freeVersion) {


                    /*
                        if (freeVersion) {


                        String title = getString(R.string.not_in_free);

                        String message = "Upgrade to the full version?";

                        AlertDialog.Builder builder = new AlertDialog.Builder(RunQuiz.this)
                                .setTitle(title)
                                .setMessage(message)
                                .setPositiveButton(android.R.string.ok, new Dialog.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        //implement billing
                                        buyProduct();
                                        dialogInterface.dismiss();
                                    }
                                })
                                .setNegativeButton(android.R.string.cancel, new Dialog.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Close the dialog
                                    }

                                });
                        builder.create().show();


                        */
                        Toast.makeText(RunQuiz.this,
                                getString(R.string.not_in_free), Toast.LENGTH_LONG).show();
                    }
                    else {
                    doExportResults();
                    }
                    mode.finish();
                    return true;
                case R.id.action_export_set:
                    if (freeVersion) {Toast.makeText(RunQuiz.this,
                            getString(R.string.not_in_free), Toast.LENGTH_LONG).show();
                    }
                    else {
                        doExportQuestions();
                        mode.finish();
                    }
                    return true;
                case R.id.action_chart:
                    doChart();
                    // Action picked, so close the CAB
                    mode.finish();
                    return true;
                case R.id.action_rename_set:
                    doRename();
                    // Action picked, so close the CAB
                    mode.finish();
                    return true;
                case R.id.action_delete_set:
                    doDelete();
                    // Action picked, so close the CAB
                    mode.finish();
                    return true;
                case R.id.action_share_set:
                    //doExportResults();
                    //doExportQuestions();
                    doEmail();
                    // Action picked, so close the CAB
                    mode.finish();
                    return true;
                case R.id.action_settings:
                    show();
                    // Action picked, so close the CAB
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            doUncheck();
            selectedItem = -1;
            toolbar.setVisibility(View.VISIBLE);
        }
    };


    public void doEmail(){

        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);

        exportDirLoc = sharedPrefs.getString("prefExportDir", "NULL");
        //File exportDir = new File(exportDirLoc, "Export");

        String setName = setArrayList.get(selectedItem);

        ArrayList<Uri> uris = new ArrayList<Uri>();
        Uri uri_res;
        Uri uri_quests;

        File file_res = new File(exportDirLoc + "/Export","Results " + setName + ".txt");
        if(file_res.exists()){
            uri_res = Uri.fromFile(file_res);
            uris.add(uri_res);
        }

        File file_quests = new File(exportDirLoc + "/Export","Questions " + setName + ".txt");
        if(file_quests.exists()){
            uri_quests = Uri.fromFile(file_quests);
            uris.add(uri_quests);
        }


        if (uris.size()==0) {
            Toast.makeText(RunQuiz.this,
                    "Nothing to share, "+ setName + " has to be exported first!", Toast.LENGTH_LONG).show();

        }
        else {
            if (debugFlag == 1){
                Toast.makeText(RunQuiz.this,
                        "Exporting" + file_res + " " + file_quests, Toast.LENGTH_LONG).show();
                Log.d("QUIZREC: exporting " + file_res + " " + file_quests, String.valueOf(selectedItem));
            }

        }

        Intent i = new Intent(Intent.ACTION_SEND_MULTIPLE);
        i.putExtra(Intent.EXTRA_SUBJECT, "Title");
        i.putExtra(Intent.EXTRA_TEXT, "Content");
        i.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        i.setType("text/plain");
        //i.setType("message/rfc822");
        //startActivity(Intent.createChooser(i, "Send mail"));
        startActivityForResult(Intent.createChooser(i, getString(R.string.send_questions_and_results_by_email_chooser)), 12345);
    }


    public void upgrade(View view) {
        showToast("going to upgrade");
        buyProductExport();
    }

    public void addNewSet(View view) {

        String Result;

        //Result = db.addQuest((new Quest("New Set", " ", " "," ",0)));
        Result = db.addQuest((new Quest("New Set", "New Question", "New Answer"," ",0)));
        //Quest(int id, String set, String question, String answer, String status, int rank){

        if (Result.equals("DUPLICATE")){
            Toast.makeText(RunQuiz.this,
                    getString(R.string.add_set_warning), Toast.LENGTH_LONG).show();

        }
        afterCreate();
    }

    public void importSet(View view) {

        if (freeVersion) {

            Toast.makeText(RunQuiz.this,
                    getString(R.string.not_in_free), Toast.LENGTH_LONG).show();
        }
        else {
        doImport();
        //afterCreate();
        }
    }

    private void show() {
        if (debugFlag == 1){
        Toast.makeText(RunQuiz.this,
                String.valueOf(selectedItem), Toast.LENGTH_LONG).show();
        }
    }

    private void doUncheck() {
        if (debugFlag == 1){
        Toast.makeText(RunQuiz.this,
                String.valueOf(selectedItem), Toast.LENGTH_LONG).show();
        Log.d("QUIZREC: action uncheck", String.valueOf(selectedItem));
        }
        MyListView.setItemChecked(selectedItem, false);
    }

    private void doEdit() {
        if (debugFlag == 1){
        Toast.makeText(RunQuiz.this,
                String.valueOf(selectedItem), Toast.LENGTH_LONG).show();
        }
        //String selectedQuiz = (String) getListAdapter().getItem(selectedItem);
        Intent intent = new Intent(getApplicationContext(), EditQuiz.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NO_ANIMATION);

        if (debugFlag == 1){
        Log.d("QUIZREC: action edit", String.valueOf(selectedItem));
        }

        intent.putExtra(EXTRA_MESSAGE, String.valueOf(selectedItem));
        startActivity(intent);
    }

    private void doRunOne() {
        if (debugFlag == 1){
        Toast.makeText(RunQuiz.this,
                String.valueOf(selectedItem), Toast.LENGTH_LONG).show();
        }
        //String selectedQuiz = (String) getListAdapter().getItem(selectedItem);
        Intent intent = new Intent(getApplicationContext(), RunOne.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NO_ANIMATION);

        if (debugFlag == 1){
        Log.d("QUIZREC: action run one", String.valueOf(selectedItem));
        }

        intent.putExtra(EXTRA_MESSAGE, String.valueOf(selectedItem));
        startActivity(intent);
    }

    public void showHelp(View view) {

        if (debugBillingFlag == 1){
            showToast("calling upgrade reset");
            //consumeProduct();
            //checkProduct();
        }

        if (debugFlag == 1){
        Toast.makeText(RunQuiz.this,
                String.valueOf(selectedItem), Toast.LENGTH_LONG).show();
        }
        //String selectedQuiz = (String) getListAdapter().getItem(selectedItem);
        Intent intent = new Intent(getApplicationContext(), ShowHelp.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NO_ANIMATION);

        if (debugFlag == 1){
        Log.d("QUIZREC: action show help", String.valueOf(selectedItem));
        }

        intent.putExtra(EXTRA_MESSAGE, String.valueOf(selectedItem));
        startActivity(intent);
    }

    public void showPref(View view) {
        if (debugFlag == 1){
            Toast.makeText(RunQuiz.this,
                    String.valueOf(selectedItem), Toast.LENGTH_LONG).show();
        }

        Intent i = new Intent(this, SetPreferenceActivity.class);
        startActivityForResult(i, RESULT_SETTINGS);

        if (debugFlag == 1){
            Log.d("QUIZREC: action show pref", String.valueOf(selectedItem));
        }
    }



    private void doImport() {

        if (debugFlag == 1){
        Toast.makeText(RunQuiz.this,
                String.valueOf(selectedItem), Toast.LENGTH_LONG).show();
        }
        /*
        //String selectedQuiz = (String) getListAdapter().getItem(selectedItem);
        Intent intent = new Intent(getApplicationContext(), ImportFile.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NO_ANIMATION);
        Log.d("QUIZREC: action import", String.valueOf(selectedItem));
        intent.putExtra(EXTRA_MESSAGE, String.valueOf(selectedItem));
        startActivity(intent);
        */
        //Activity thisActivity = (Activity) RunQuiz.this;
        onCreateFileDialog();

    }
    private void doShowOne() {

        if (debugFlag == 1){
        Toast.makeText(RunQuiz.this,
                String.valueOf(selectedItem), Toast.LENGTH_LONG).show();
        }

        //String selectedQuiz = (String) getListAdapter().getItem(selectedItem);
        Intent intent = new Intent(getApplicationContext(), ShowOne.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NO_ANIMATION);

        if (debugFlag == 1){
        Log.d("QUIZREC: action show one", String.valueOf(selectedItem));
        }

        intent.putExtra(EXTRA_MESSAGE, String.valueOf(selectedItem));
        startActivity(intent);
    }

    private void doChart() {
        if (debugFlag == 1){
        Toast.makeText(RunQuiz.this,
                String.valueOf(selectedItem), Toast.LENGTH_LONG).show();
        }
        //String selectedQuiz = (String) getListAdapter().getItem(selectedItem);

        //Intent intent = new Intent(getApplicationContext(), DrawChart.class);
        Intent intent = new Intent(getApplicationContext(), SelectQuestionsForChart.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NO_ANIMATION);

        if (debugFlag == 1){
        Log.d("QUIZREC: action draw chart", String.valueOf(selectedItem));
        }

        intent.putExtra(EXTRA_MESSAGE, String.valueOf(selectedItem));
        startActivity(intent);
    }

    private void doExportResults () {

        if (debugFlag == 1){
        Toast.makeText(RunQuiz.this,
                String.valueOf(selectedItem), Toast.LENGTH_LONG).show();
        }
        //boolean res;
        //res = ExportDatabaseCSVTask.doInBackground(db.getQuizName(selectedItem));
        //ExportDatabaseCSVTask task=new ExportDatabaseCSVTask();
        //task.execute(setArrayList.get(selectedItem),"Results");

        showExportDir(setArrayList.get(selectedItem),"Results");
    }

    private void doDelete () {
        if (debugFlag == 1){
        Toast.makeText(RunQuiz.this,
                String.valueOf(selectedItem), Toast.LENGTH_LONG).show();
        }
        //db.deleteQuestSet(db.getQuizName(selectedItem + 1));
        db.deleteQuestSet(setArrayList.get(selectedItem));
        afterCreate();

        //setArrayList = (ArrayList<String>) db.getAllSets();
        //adapter.notifyDataSetChanged();
        //todo test this and better replace this with something no depreciated
        //the reason is we need to update cursor after set deletion / renaming / Imprting


    }

    private void doRename () {
        if (debugFlag == 1){
            Toast.makeText(RunQuiz.this,
                    String.valueOf(selectedItem), Toast.LENGTH_LONG).show();
        }
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Edit Question");

// Set an EditText view to get user input
        final EditText input = new EditText(this);
        alert.setView(input);
        input.setText(setArrayList.get(selectedItem));
        final String SetNameOld = input.getText().toString();

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String SetNameNew = input.getText().toString();

                if (debugFlag == 1){
                    Log.d("QUIZREC: rename question set new: ",SetNameNew + " old:" + SetNameOld);
                }
                //db.renameAnswer(CurrentSetNameEdit, CurrentQuestionNameEdit, AnswerNew, AnswerToEdit);
                int res= db.renameSet(SetNameNew,SetNameOld);

                if (res == -1) {
                    Toast.makeText(RunQuiz.this,
                            getString(R.string.cant_rename_set_warning), Toast.LENGTH_LONG).show();
                }

                if (res == 0) {
                    Toast.makeText(RunQuiz.this,
                            getString(R.string.set_rename_failed), Toast.LENGTH_LONG).show();
                }

                if (debugFlag == 1){
                    Log.d("QUIZREC: rename question set new", "");
                }

                afterCreate();

            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //re-read the question

            }
        });

        alert.show();



    }

    private void doExportQuestions () {

        if (debugFlag == 1){
        Toast.makeText(RunQuiz.this,
                String.valueOf(selectedItem), Toast.LENGTH_LONG).show();
        }
        //boolean res;
        //res = ExportDatabaseCSVTask.doInBackground(db.getQuizName(selectedItem));

        showExportDir(setArrayList.get(selectedItem),"Questions");

        //ExportDatabaseCSVTask task=new ExportDatabaseCSVTask();
        //task.execute(setArrayList.get(selectedItem),"Questions");

    }

public void showExportDir(final String SetName, final String ExportWhat){

    //File sdCardRoot = Environment.getExternalStorageDirectory();
    //String sdCardDirectory = sdCardRoot + "/Android/data/" + getApplicationContext().getPackageName();

    SharedPreferences sharedPrefs = PreferenceManager
            .getDefaultSharedPreferences(this);


    if (debugFlag == 1){
    Toast.makeText(RunQuiz.this,
            sharedPrefs.getString("prefExportDir", "NULL"), Toast.LENGTH_LONG).show();
    Log.d("QUIZREC: setting default pref new ", sharedPrefs.getString("prefExportDir", "NULL"));
    }

    exportDirLoc = sharedPrefs.getString("prefExportDir", "NULL");

    new AlertDialog.Builder(this)
            .setTitle("Export Data")
            .setMessage("Data export directory: " + exportDirLoc)
            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    ExportDatabaseCSVTask task = new ExportDatabaseCSVTask();
                    task.execute(SetName, ExportWhat);// continue with export
                }
            })

            //.setIcon(R.drawable.ic_dialog_alert)
            .show();

}

    /**/
        public void onCreateFileDialog() {
        //super.onCreate(savedInstanceState);
        File mPath = new File(Environment.getExternalStorageDirectory() + "//DIR//");
        fileDialog = new FileDialog(this, mPath);
        fileDialog.setFileEndsWith(".txt");
        fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
            public void fileSelected(File file) {
                Log.d(getClass().getName(), "selected file " + file.toString());
                //16.03.14
                //either this in main thread
                //ImportFile(file.toString());

                // or that in backgroud. Problem here is to refresh the main UI
                ImportDatabaseCSVTask task=new ImportDatabaseCSVTask();
                task.execute(file.toString(),"Import");


            }
        });
        fileDialog.addDirectoryListener(new FileDialog.DirectorySelectedListener() {
          public void directorySelected(File directory) {
              Log.d(getClass().getName(), "selected dir " + directory.toString());
          }
        });
        fileDialog.setSelectDirectoryOption(false);
        fileDialog.showDialog();
    }

    public void ImportFile(String fileName) {

        Toast.makeText(RunQuiz.this,
                "Loading surveys. Please wait.", Toast.LENGTH_SHORT).show();

        FileReader file = null;
        try {
            file = new FileReader(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader buffer = new BufferedReader(file);
        String line = "";

        try {
            int NotImportedCounter = 0;
            while ((line = buffer.readLine()) != null) {

                //db.addQuest(new Quest("art", "how are you today?", "quite ok","1",0));
                String[] str = line.split(",");

                if (str.length == 4){
                    if (debugFlag == 1){
                        Log.d(getClass().getName(), "going to import " + file.toString() + " " + str[0]+ " " + str[1]+ " " +  str[2]+ " " + str[3]);
                    }
                    //"SELECT _id,set_name,question,answer,status,rank FROM
                    ////db.addQuest(new Quest("art", "how are you today?", "quite ok","1",0));
                    //rank exported as a string, we need to remove cahrs and import as int

                    //Toast.makeText(RunQuiz.this,
                    //        "First time app run. Loading survey: " + str[0], Toast.LENGTH_SHORT).show();

                    db.addQuest(new Quest(str[0], str[1], str[2], "0", Integer.parseInt(str[3].replaceAll("[\\D]", ""))));
                }
                else {
                    NotImportedCounter ++;
                    Toast.makeText(RunQuiz.this,
                            "Could not import " + String.valueOf(NotImportedCounter) + " strings", Toast.LENGTH_LONG).show();
                    if (debugFlag == 1){
                        Log.e("not imported: ",line);
                    }

                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //16.03.13
        //afterCreate();

    }

    private class ExportDatabaseCSVTask extends AsyncTask<String, Integer, Boolean>
    {
        private final ProgressDialog dialog = new ProgressDialog(RunQuiz.this);
        private File file;

        //File sdCardRoot = Environment.getExternalStorageDirectory();
        //String sdCardDirectory = sdCardRoot + "/Android/data/" + getApplicationContext().getPackageName();

        //protected void

        @Override
        protected void onPreExecute() {

            pd = new ProgressDialog(context);
            pd.setMessage("Exporting database. Please wait...");
            pd.setIndeterminate(false);
            pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            //pd.setCancelable(true);

            /*
            pd.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    pd.dismiss();
                    return;
                }
            });
            */



            pd.show();

        }

        @Override
        protected Boolean doInBackground(final String... args){

            String ExportWhat = args[1];

            pd.getButton(ProgressDialog.BUTTON_POSITIVE).setVisibility(View.INVISIBLE);
            pd.getButton(ProgressDialog.BUTTON_NEGATIVE).setVisibility(View.VISIBLE);

            /*
            if (ExportWhat.equals("Import")){
                Log.v("QUIZREC", "file for import: "+args[0]+ " Operation:"+ args[1]);  //get the path of db
                ImportFile(args[0]);
                return true;
            }
            */

            String SelectedItemName = args[0];

            File dbFile=getDatabasePath("QuizSet");

            if (debugFlag == 1){
                Log.v("QUIZREC", "Db for export path is: "+dbFile);  //get the path of db
            }

            //File sdCardRoot = Environment.getExternalStorageDirectory();
            //String sdCardDirectory = sdCardRoot + "/Android/data/" + getApplicationContext().getPackageName();

            File exportDir = new File(exportDirLoc, "Export");
            if (debugFlag == 1){
                Log.v("QUIZREC", "Dir for export path is: "+exportDir);
            }
            if (!exportDir.exists()) {
                if (debugFlag == 1){
                    Log.v("QUIZREC", "Going to create export dir: "+exportDir);
                }
                exportDir.mkdirs();
            }

            if (ExportWhat.equals("Results")){
            file = new File(exportDir, "Results " + SelectedItemName + ".txt");}
            else {
            file = new File(exportDir, "Questions " + SelectedItemName + ".txt");}


            String[] arrStr;

            try {

                file.createNewFile();
                CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
                Cursor CursorResults;

                //Log.v("QUIZREC", "export selectedItem: "+RunQuiz.selectedItem );
                //String SelectedItemName = db.getQuizName(RunQuiz.selectedItem+1);
                //Log.v("QUIZREC", "Going to export "+SelectedItemName);
                //String SelectedItemName = args[0];
                //String ExportWhat = args[1];


                if (ExportWhat.equals("Results")){
                    if (debugFlag == 1){
                        Log.v("QUIZREC", "Going to export Results "+SelectedItemName);
                    }
                CursorResults = db.getAllQuestResults(SelectedItemName);
                }
                else {
                    if (debugFlag == 1){
                        Log.v("QUIZREC", "Going to export Questions "+SelectedItemName);
                    }
                CursorResults = db.getAllQuestions(SelectedItemName);
                }

                // looping through all rows and adding to list
                if (CursorResults.moveToFirst()) {
                    //create a file


                    do {
                        if (ExportWhat.equals("Results")){
                        //export one result db row
                        arrStr = new String[5];
                        arrStr[0] = String.valueOf(CursorResults.getInt(0)); //index
                        arrStr[1] = String.valueOf(CursorResults.getString(1)); //set name
                        arrStr[2] = String.valueOf(CursorResults.getString(2)); //question name
                        arrStr[3] = String.valueOf(CursorResults.getInt(3)); //answer rank
                        arrStr[4] = String.valueOf(CursorResults.getString(4)); //answer time
                        }
                        else {

                        ////db.addQuest(new Quest("art", "how are you today?", "quite ok","1",0));
                        //"SELECT _id,set_name,question,answer,status,rank FROM
                        arrStr = new String[4];
                        //arrStr[0] = String.valueOf(CursorResults.getInt(0)); //index is generated
                        arrStr[0] = String.valueOf(CursorResults.getString(1)); //set name
                        arrStr[1] = String.valueOf(CursorResults.getString(2)); //question name
                        arrStr[2] = String.valueOf(CursorResults.getString(3)); //answer
                        //arrStr[4] = String.valueOf(CursorResults.getString(4)); //status not used now
                        arrStr[3] = String.valueOf(CursorResults.getInt(5)); //rank

                        }
                        csvWrite.writeNext(arrStr);



                    } while (CursorResults.moveToNext());
                }
                else
                {
                    //no results for this set

                }
                csvWrite.close();
                CursorResults.close();

                //todo sure? guess location to scan is wrong here
                //25.05.14 - no it's correct

                String ExportDirScan= sdCardDirectory + "/Export";
                MediaScannerConnection.scanFile(context, new String[]{ExportDirScan}, null, null);
                return true;
            }
            catch (IOException e){
                if (debugFlag == 1){
                    Log.e("QUIZREC", e.getMessage(), e);
                }
                return false;
            }
        }



        @Override
        protected void onPostExecute(final Boolean success) {

            if (pd!=null) {
                pd.dismiss();
            }
            if (success){
                Toast.makeText(RunQuiz.this, "Export Success!", Toast.LENGTH_SHORT).show();
                String ExportDirScan= sdCardDirectory + "/Export";
                MediaScannerConnection.scanFile(context, new String[]{ExportDirScan}, null, null);

                //does not work as expected
                /*
                pd.setMessage("Exported to " + ExportDirScan);
                pd.setProgress(100);
                pd.getButton(ProgressDialog.BUTTON_POSITIVE).setVisibility(View.VISIBLE);
                pd.getButton(ProgressDialog.BUTTON_NEGATIVE).setVisibility(View.INVISIBLE);
                */

                //not actually needed, left from import
                adapter.notifyDataSetChanged();
                afterCreate();
            }
            else {
                Toast.makeText(RunQuiz.this, "Export Failed!", Toast.LENGTH_SHORT).show();
                //pd.dismiss();
            }
        }
    }

    private class ImportDatabaseCSVTask extends AsyncTask<String, Integer, Boolean>
    {
        private final ProgressDialog dialog = new ProgressDialog(RunQuiz.this);
        private File file;

        @Override
        protected void onPreExecute() {



            pd = new ProgressDialog(context);
            pd.setMessage("Importing database...");
            pd.show();

        }

        @Override
        protected Boolean doInBackground(final String... args){

            String ExportWhat = args[1];

            if (ExportWhat.equals("Import")){
                if (debugFlag == 1){
                Log.v("QUIZREC", "file for import: "+args[0]+ " Operation:"+ args[1]);  //get the path of db
                }
                ImportFile(args[0]);
                return true;
            }
            else {return false;}
        }

        @Override
        protected void onPostExecute(final Boolean success) {

            if (pd!=null) {
                pd.dismiss();
            }
            if (success){
                Toast.makeText(RunQuiz.this, "Import Success!", Toast.LENGTH_SHORT).show();
                adapter.notifyDataSetChanged();
                afterCreate();
            }
            else {
                Toast.makeText(RunQuiz.this, "Import Failed!", Toast.LENGTH_SHORT).show();
            }
        }
    }


    //billing methods
    private void buyProductExport()
    {
        showToast("stared buying product");

        // first time
        if(inAppBilling == null)
        {
            // create in-app billing object
            inAppBilling = new InAppBilling(this, this, applicationPublicKey, PURCHASE_REQUEST_CODE);
        }

        inAppBilling.checkOnly=false;
        showToast("stared buying product, checkonly flag " + inAppBilling.checkOnly);

        // InAppBilling initialization
        // NOTE: if inAppBilling is already active, the call is ignored
        // you can use isActive() to test for class active,
        // or test the result value of the call to find out
        // if start service connection was done (true) or the class was active (false)

        showToast("inAppBilling is active? this should not be the case " + inAppBilling.isActive());

        inAppBilling.startServiceConnection(InAppBilling.ITEM_TYPE_ONE_TIME_PURCHASE,
                PRODUCT_SKU, InAppBilling.ACTIVITY_TYPE_PURCHASE);

        // exit
        return;
    }

    private void checkProduct()
    {
        // first time
        if(inAppBilling == null)
        {
            // create in-app billing object
            inAppBilling = new InAppBilling(this, this, applicationPublicKey, PURCHASE_REQUEST_CODE);

        }

        inAppBilling.checkOnly=true;

        showToast("started checking the product, checkonly flag " + inAppBilling.checkOnly);
        showToast("inAppBilling is active? this should not be the case " + inAppBilling.isActive());

        // InAppBilling initialization
        // NOTE: if inAppBilling is already active, the call is ignored
        // you can use isActive() to test for class active,
        // or test the result value of the call to find out
        // if start service connection was done (true) or the class was active (false)
        inAppBilling.startServiceConnection(InAppBilling.ITEM_TYPE_ONE_TIME_PURCHASE,
                PRODUCT_SKU, InAppBilling.ACTIVITY_TYPE_PURCHASE);

        // exit
        return;
    }


    private void consumeProduct(){
    // for testing only - in order to reset
    // first time
        showToast("init consume product - for testing");
        if(inAppBilling == null){
            // create in-app billing object
            inAppBilling = new InAppBilling(this, this, applicationPublicKey, PURCHASE_REQUEST_CODE);
        }

    // InAppBilling initialization
    // NOTE: if inAppBilling is already active, the call is ignored
    // you can use isActive() to test for class active,
    // or test the result value of the call to find out
    // if start service connection was done (true) or the class was active (false)
        inAppBilling.startServiceConnection(InAppBilling.ITEM_TYPE_ONE_TIME_PURCHASE,PRODUCT_SKU, InAppBilling.ACTIVITY_TYPE_CONSUME);
    // exit
        return;
    }

    @Override
    public void inAppBillingBuySuccsess()
    {
        showToast("In App purchase successful");
        View btn = findViewById(R.id.upgrade_button);
        btn.setEnabled(false);
        this.freeVersion=false;
        return;
    }

    @Override
    public void inAppBillingItemAlreadyOwned()
    {
        showToast("Product is already owned.\nPurchase was not initiated.");
        View btn =  findViewById(R.id.upgrade_button);
        btn.setEnabled(true);
        this.freeVersion=false;
        return;
    }

    @Override
    public void inAppBillingCanceled()
    {
        showToast("Purchase was canceled by user");
        return;
    }

    @Override
    public void inAppBillingConsumeSuccsess()
    {
        showToast("In App consume product successful");
        return;
    }

    @Override
    public void inAppBillingItemNotOwned()
    {
        showToast("Product is not owned.\nConsume failed.");
        View btn =  findViewById(R.id.upgrade_button);
        btn.setEnabled(true);
        return;
    }

    @Override
    public void inAppBillingFailure(String errorMessage)
    {
        showToast("Purchase or consume process failed.\n" + errorMessage);
        return;
    }

    // user pressed back button
    @Override
    public void onBackPressed()
    {
        // terminate activity and return RESULT_CANCELED
        if(inAppBilling != null) inAppBilling.dispose();
        finish();
        return;
    }


    public void showToast(CharSequence toaststring){

        if (debugBillingFlag == 1) {
            Toast toast = Toast.makeText(RunQuiz.this, toaststring, Toast.LENGTH_LONG);
            toast.show();
            Log.v("QUIZREC", " toast text: " + toaststring);
        }

    }


}
