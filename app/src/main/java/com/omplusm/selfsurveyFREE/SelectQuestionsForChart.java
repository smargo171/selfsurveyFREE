package com.omplusm.selfsurveyFREE;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by s3rg on 7/4/13.
 */
public class SelectQuestionsForChart extends ListActivity {

    public ListView answerListView;
    String[] questArray;
    ArrayList<String> questArrayList;
    public static ArrayList<String> questPassToChart;

    public final static String EXTRA_MESSAGE = "com.omplusm.selfsurveyFREE.SELECTED_QUIZ_STRING";
    public final static int debugFlag = 0;

    SelectQForChartArrayAdapter adapter;

    int EditSwitch;
    DatabaseHandler db = RunQuiz.db;
    public int myId;

    //
    private RadioButton listRadioButton =null;
    ArrayList<RadioButton> arrayListRadioButton;

    int listIndex =-1;

    //int myId;
    List<String> questList;

    String CurrentQuestionName;

    int quest_position;

    public static String CurQuizName;

    public void onCreate(Bundle savedInstanceState) {


        //String CurrentQuestionName;

        EditSwitch = 0;

        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String message = intent.getStringExtra(RunQuiz.EXTRA_MESSAGE);

        ///*myId = 0;
        questList = new ArrayList<String>();
        arrayListRadioButton = new ArrayList<RadioButton>();

        quest_position = 0;

        // moved to globals in order to use in displayQuestion method DatabaseHandler db = RunQuiz.db;
        //ListView answerListView;

        //works
        if (message == null) {
            if (debugFlag == 1){
                Log.d("QUIZREC: parsing message, should be empty , quiz name should be known ", CurQuizName);
            }
            //message = String.valueOf(myId);}
        }

        //if message is null, we have returned here from DrawChart and myId should already be filled in
        //if (message != null) {
        else{
        try {

            //in db we start from 1, in the view list we start from 0, I think
            //reverting this back , as we use a list now
            myId = Integer.parseInt(message);
            if (debugFlag == 1){
                Log.d("QUIZREC: parsing message, message  ", message);
            }
            if (debugFlag == 1){
                Log.d("QUIZREC: parsing message for Selected iD  ", String.valueOf(myId));
            }
            //DrawChart.CurrentSetId = myId;
            if (debugFlag == 1){
                Log.d("QUIZREC: after my create in SelectQuestions  ", String.valueOf(myId));
            }
            CurQuizName = RunQuiz.setArrayList.get(myId);
        } catch(NumberFormatException nfe) {
            //System.out.println("Could not parse " + nfe);
            if (debugFlag == 1){
                Log.d("QUIZREC: edit Could not parse  ", String.valueOf(nfe));
            }
        }
        }
        //}

        //
        //DrawChart.CurrentSetName=null;



        setCurrentContentView();

        TextView CurrentQuiz = (TextView)this.findViewById(R.id.textView);
        CurrentQuiz.setText(CurQuizName);


        //setContentView(R.layout.activity_edit_quiz);

        /*
        //EditText CurrentQuiz = (EditText)this.findViewById(R.id.textView);
        //
        EditText CurrentQuiz=new EditText(this);
        CurrentQuiz = (EditText)this.findViewById(R.id.textView);
        CurrentQuiz.setText(db.getQuizName(myId));
        */



        afterMyCreate();

        displayQuestion(quest_position);


    }


public void afterMyCreate(){



    if (debugFlag == 1){
        Log.d("QUIZREC: after my create in SelectQuestions, CurQuizName  from myId ", CurQuizName);
    }

    /*
    if (DrawChart.CurrentSetName != null && !DrawChart.CurrentSetName.isEmpty())
    {
        CurQuizName = DrawChart.CurrentSetName;
        Log.d("QUIZREC: after my create in SelectQuestions, CurQuizName  not null", CurQuizName);}
    else {
    CurQuizName = db.getQuizName(myId);
        Log.d("QUIZREC: after my create in SelectQuestions, CurQuizName  ", CurQuizName);
    }
    */


}

/*
 // when i have resume here, i have exception right when the activity is called for the first time
public void onResume(){
        Log.d("QUIZREC: after my resume in SelectQuestions  ", "");

        if (CurQuizName == null){
            CurQuizName = db.getQuizName(myId);
        }
    quest_position = 0;
    displayQuestion(quest_position);
    }
*/


public void setCurrentContentView() {
    setContentView(R.layout.activity_select_questions_for_chart);
    }

public void displayQuestion (int position){

        //11.11
        //questList = db.getOneSet(db.getQuizName(myId));
        //why are you so stupid? We need to re-read this list as we can add or delete questions inside the editing procedure
        questList = db.getOneSet(CurQuizName);

        int progress = 0;

    if (debugFlag == 1){
        Log.d("QUIZREC: displayQuestion   ", "position " + position + " Id " + myId + " number of questions " + String.valueOf(questList.size()));
    }


        questArrayList = (ArrayList<String>) questList;

        //questArrayList = new ArrayList<String>(Arrays.asList(questArray));
    if (debugFlag == 1){
        Log.d("QUIZREC: edit answers array size  ", String.valueOf(questList.size()));
    }

        //adapter = new AnswerListArrayAdapter(this,questArray);
        //R.layout.list_row_edit
        adapter = new SelectQForChartArrayAdapter(this,questArrayList,R.layout.select_question_for_chart_one_row);

        adapter.questName = CurrentQuestionName;
        adapter.setName = CurQuizName;

        answerListView = (ListView) findViewById(android.R.id.list);
        answerListView.setAdapter(adapter);
        adapter.notifyDataSetChanged();



}

    public void selectQuestionToChart(View view) {
        final int position = getListView().getPositionForView((RelativeLayout)view.getParent());
        if (debugFlag == 1){
            Log.d("QUIZREC: selectQuestion for Chart - adding question, position:  " , String.valueOf(position) );
        }
        //  no need
        // answerListView.setSelection(position);



        //ResultAnswerArrayAdapter.SelectedAnswer=position;


        //View o = getListView().getChildAt(position - getListView().getFirstVisiblePosition()).findViewById(R.id.editAnswerText);

        //EditAnswer = (EditText) getListView().getChildAt(position).findViewById(R.id.editAnswerText);
        //EditAnswer = (EditText) o.findViewById(R.id.editAnswerText);

        View vMain =((View) view.getParent());// getParent() must be added 'n' times,
        // where 'n' is the number of RadioButtons' nested parents
        // in your case is one.
        // uncheck previous checked button.

        //tmo to test 2.2.14
        ///*if(listRadioButton !=null) listRadioButton.setChecked(false);

        // assign to the variable the new one
        listRadioButton =(RadioButton) view;

        // find if the new one is checked or not, and set "listIndex"
        ///*if(listRadioButton.isChecked())
        ///*{
        listIndex =((ViewGroup) vMain.getParent()).indexOfChild(vMain);
            String QuestionToAdd = this.questArrayList.get(position);
        if (debugFlag == 1){
            Log.d("QUIZREC: selectQuestion for Chart - question String:  " , this.questArrayList.get(position) );
        }

            if (!adapter.SelectedQuestionsList.contains(QuestionToAdd)) {
                //Log.d("QUIZREC: selectQuestion for Chart - does not contain this question yet:  "  + QuestionToAdd);
                boolean res = adapter.SelectedQuestionsList.add(QuestionToAdd);
                if (debugFlag == 1){
                    Log.d("QUIZREC: selectQuestion for Chart - adding question:  " , this.questArrayList.get(position) );
                }
            }
        ///* }else{
        ///*listIndex =((ViewGroup) vMain.getParent()).indexOfChild(vMain);
        ///*String QuestionToAdd = this.questArrayList.get(position);
            else{
            boolean res = adapter.SelectedQuestionsList.remove(QuestionToAdd);
                if (debugFlag == 1){
                    Log.d("QUIZREC: selectQuestion for Chart - removing question:  " , this.questArrayList.get(position) );
                }
                ///*listRadioButton =null;
                ///*listIndex =-1;
            }

        // need to do it because otherwise prev. selected items stays afrer scrolling
        // we invalidate datasourse and make ds adapter not to use cash and forget about prev. selection

        if (debugFlag == 1){
            Log.d("QUIZREC: selectQuestion for Chart - size of the selected questions list:  " , String.valueOf(adapter.SelectedQuestionsList.size()));
        }

        adapter.notifyDataSetChanged();

    }


    public void goToChart(View view){

        questPassToChart = new ArrayList<String>();
        questPassToChart = adapter.SelectedQuestionsList;
        Intent intent = new Intent(getApplicationContext(), DrawChart.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NO_ANIMATION);
        if (debugFlag == 1){
            Log.d("QUIZREC: action draw chart", String.valueOf(CurQuizName));
        }
        intent.putExtra(EXTRA_MESSAGE, CurQuizName);
        startActivity(intent);

    }
    //didnt work
    /*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("QUIZREC: getting quiz name after back from chart- starting", "");
        if (resultCode == RESULT_OK) {
            if (data != null) {
                Bundle b = data.getExtras();
                CurQuizName = b.getString("CurQuizName");
                Log.d("QUIZREC: getting quiz name after back from chart", String.valueOf(CurQuizName));

            }
        }
    }
    */

}