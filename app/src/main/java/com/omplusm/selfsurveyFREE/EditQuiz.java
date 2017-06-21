package com.omplusm.selfsurveyFREE;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;

import com.mobeta.android.dslv.DragSortListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by s3rg on 7/4/13.
 */
public class EditQuiz extends ListActivity {


    //todo seems to ba a problem with deleting answer - it's disappearing after delete, but appearign again in run mode????
    //when ranks were broken

    //todo why it's possible to add to blank answers?


    public ListView answerListView;
    String[] questArray;
    ArrayList<String> questArrayList;

    AnswerListArrayAdapter adapter;

    int EditSwitch;
    DatabaseHandler db = RunQuiz.db;

    int myId;
    List<String> questList;

    String CurrentQuestionName;

    View view;

    int quest_position;

    public String CurQuizName;

    public final static int debugFlag = 0;

    public View progressView;
    public TextView progressText;

    public void onCreate(Bundle savedInstanceState) {


        //String CurrentQuestionName;

        EditSwitch = 0;

        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String message = intent.getStringExtra(RunQuiz.EXTRA_MESSAGE);

        myId = 0;
        questList = new ArrayList<String>();
        quest_position = 0;

        // moved to globals in order to use in displayQuestion method DatabaseHandler db = RunQuiz.db;
        //ListView answerListView;

        try {

            //in db we start from 1, in the view list we start from 0, I think
            //myId = Integer.parseInt(message) +1;

            //changing to myId, as now we operate on list, not on db
            myId = Integer.parseInt(message);

            if (debugFlag == 1){
            Log.d("QUIZREC: edit Selected iD  ", String.valueOf(myId));
            }

        } catch(NumberFormatException nfe) {
            //System.out.println("Could not parse " + nfe);

            if (debugFlag == 1){
            Log.d("QUIZREC: edit Could not parse  ", String.valueOf(nfe));
            }
        }


        setCurrentContentView();

        if (findViewById(R.id.progress) != null ) {
            progressView = findViewById(R.id.progress);
            progressText = (TextView) findViewById(R.id.progressText);
        }

        //setContentView(R.layout.activity_edit_quiz);

        //EditText CurrentQuiz = (EditText)this.findViewById(R.id.textView);
        //EditText CurrentQuiz=new EditText(this);

        //lollipop
        //TextView CurrentQuiz=new TextView(this);

        //22.06.15
        //TextView CurrentQuiz = (TextView)this.findViewById(R.id.textView);

        view= getWindow().getDecorView().findViewById(android.R.id.content);

        TextView CurrentQuiz = (TextView)view.findViewById(R.id.textView);

        /*
        LayoutInflater inflater = getLayoutInflater();
        TextView CurrentQuiz = inflater.inflate(R.id.textView,null);
        */

        //09.03.14
        //CurrentQuiz.setText(db.getQuizName(myId));
        CurrentQuiz.setText(RunQuiz.setArrayList.get(myId));

        afterMyCreate();

        displayQuestion(quest_position);


    }


public void afterMyCreate(){

    if (debugFlag == 1){
    Log.d("QUIZREC: after my create in EditQuiz  ", "");
    }

    //09.03.14
    //CurQuizName = db.getQuizName(myId);
    //CurQuizName = (db.getAllSets().get(myId));
    CurQuizName = RunQuiz.setArrayList.get(myId);

    //11.11 going to replace questlist building in each method by this variable
    // this was wrong because questlist is updated dynamicatty after add question and del question
    //questList = db.getOneSet(CurQuizName);

}

public void setCurrentContentView() {

    //lollipop
    //22.06.15
    //changing back does not work, need to chande to this from view eberywhere
    setContentView(R.layout.activity_edit_quiz);

    //LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    //view = inflater.inflate(R.layout.activity_edit_quiz, null);



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

        //TODO: exception here, as well 11.11, when there was one question and I have added one more, message: "Invalid index 1, size is one"
    //if (position < 0) {position =0;}
    //if (position >= questList.size()) {position =questList.size()-1;}
        //todo: problem adding question after ? this hack


    if (questList.size() >0){
        CurrentQuestionName = questList.get(position);
    }
    else {
        Toast.makeText(this, getString(R.string.cant_edit_empty_set_warning), Toast.LENGTH_SHORT).show();
        return;
    }

        //EditText CurrentQuizQuestion = (EditText)this.findViewById(R.id.QuestionNameTextView);

    //lollipop 22.06.15
    //TextView CurrentQuizQuestion = (TextView)this.findViewById(R.id.QuestionNameTextView);
    TextView CurrentQuizQuestion = (TextView)view.findViewById(R.id.QuestionNameTextView);

        //15.03.13 CurrentQuizQuestion.setEnabled(false);
        //CurrentQuizQuestion.setFocusable(true);
        //CurrentQuizQuestion.setEnabled(true);
        CurrentQuizQuestion.setText(CurrentQuestionName);

    if (debugFlag == 1){
        Log.d("QUIZREC: CurrentQuestionName  ", CurrentQuestionName);
    }

        //we can't use array here, as we want to change the size, but with normal array it's not possible
        // we need to use array list
        //http://stackoverflow.com/questions/16996462/listview-how-to-add-data-on-custom-arrayadapter?lq=1

        //String[] questArray
        //array not needed, to delete
        //questArray = db.getAnswersOneSet(db.getQuizName(myId),CurrentQuestionName).toArray(new String[questList.size()]);

        //11.11.
        questArrayList = (ArrayList<String>) db.getAnswersOneSet(CurQuizName,CurrentQuestionName);

        //questArrayList = new ArrayList<String>(Arrays.asList(questArray));

    if (debugFlag == 1){
    Log.d("QUIZREC: edit answers array size  ", String.valueOf(questList.size()));
    }

        //adapter = new AnswerListArrayAdapter(this,questArray);
        //R.layout.list_row_edit
        adapter = new AnswerListArrayAdapter(this,questArrayList,R.layout.list_row_edit);

        adapter.questName = CurrentQuestionName;
        adapter.setName = CurQuizName;

        answerListView = (ListView) findViewById(android.R.id.list);
        answerListView.setAdapter(adapter);
    ((DragSortListView)answerListView).setDragListener(new DragSortListView.DragListener() {
        @Override
        public void drag(int from, int to) {
            Log.d("asd", "drag " + from + " " + to);
//            if (from < to) {
//                //rankup
//                for (int i=from; i<=to; i++)
//                    rankUp(adapter.getLongpressed());
//            }
//            else {
//                for (int i=to; i>=from; i++)
//                    rankDown(adapter.getLongpressed());
//            }
//            adapter.dragEnd();
        }

    });
    ((DragSortListView)answerListView).setDropListener(new DragSortListView.DropListener() {
        @Override
        public void drop(int from, int to) {
            Log.d("asd", "drop " + from + " " + to);
            int pos = adapter.getLongPessedPos();
            if (from < to) {
                //rankup
                for (int i = from; i < to; i++)
                    rankDown(adapter.getLongpressed(), pos++);
            } else if (from > to) {
                for (int i = to; i < from; i++)
                    rankUp(adapter.getLongpressed(), pos--);
            }
            adapter.dragEnd();
        }

        @Override
        public void start(int position) {
            Log.d("asd", "start");
            adapter.dragStart(position);
        }
    });


        adapter.notifyDataSetChanged();

    /*
    //try to resolve keyboard hiding edit text bug, does not work
    answerListView.postDelayed(new Runnable() {
        @Override
        public void run() {
            answerListView.setSelection(answerListView.getCount());
            answerListView.smoothScrollToPosition(answerListView.getCount());
        }
    }, 100);
    //
    */

    ProgressBar mProgressBar = (ProgressBar)findViewById(R.id.progressBar);
    int done = position+1;
    int total = questList.size();
    progress = ((done*100)/total);

    if (debugFlag == 1){
    Log.d("QUIZREC: progress  " + String.valueOf(progress) + " done = ", done  + " total = " + total );
    }

    mProgressBar.setMax(100);
    mProgressBar.setProgress(progress);

    if (progressView != null) {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)progressView.getLayoutParams();
        lp.weight = (float)progress / 100;
        progressView.setLayoutParams(lp);
        progressText.setText(String.valueOf(position+1));
    }

}

public void delAnswer(View view){

    //view.setItemChecked(position, true);

    //11.11
    //questList = db.getOneSet(CurQuizName);

    if (debugFlag == 1){
    Log.d("QUIZREC: delete answer , getting question and set names  ", "position " + quest_position + " Id " + myId + " number of questions " + String.valueOf(questList.size()));
    }
    //exception here?
    String CurrentQuestionNameDelete = questList.get(quest_position);

    //11.11 see RinObe
    String CurrentSetNameDelete = CurQuizName;

    if (debugFlag == 1){

        Toast.makeText(this, "Deleting answer", Toast.LENGTH_SHORT).show();
    }
        String AnswerToDelete = "NULL";
    final int position = getListView().getPositionForView((RelativeLayout)view.getParent());

    if (debugFlag == 1){

        Log.d("QUIZREC: edit answers delete position  ", String.valueOf(position));
    }

        if (position >= 0) {
        AnswerToDelete = questArrayList.get(position);

        if (debugFlag == 1){

        Toast.makeText(this, "going to delete answer " + AnswerToDelete, Toast.LENGTH_SHORT).show();
        Log.d("QUIZREC: edit answers delete name  ", AnswerToDelete);
        }

        db.deleteAnswer(CurrentSetNameDelete,AnswerToDelete,CurrentQuestionNameDelete);
        //re-read the question
        displayQuestion(quest_position);
    }
}

    public void rankUp (View view, int pos){

        //view.setItemChecked(position, true);

        //11.11
        //questList = db.getOneSet(CurQuizName);

        int Answers = questArrayList.size();

        if (debugFlag == 1){
            Log.d("QUIZREC: change rank of an answer ", "position " + quest_position + " Id " + myId + " number of questions " + String.valueOf(questList.size()));
        }

        //exception here?
        String CurrentQuestionName = questList.get(quest_position);
        String CurrentSetName = RunQuiz.setArrayList.get(myId);
        String curRank;

        if (debugFlag == 1){

            Toast.makeText(this, "Change rank of an answer", Toast.LENGTH_SHORT).show();
        }

        String Answer = "NULL";
//        final int position = getListView().getPositionForView((RelativeLayout)view.getParent());
        final int position = pos;

        if (debugFlag == 1){

            Log.d("QUIZREC: edit answers delete position  ", String.valueOf(position));

        }

        if (position > 0) {
            Answer = questArrayList.get(position);

            if (debugFlag == 1){

                Toast.makeText(this, "going to change rank up" + Answer, Toast.LENGTH_SHORT).show();
            }

            curRank = db.rankChangeAnswer(CurrentSetName, Answer, CurrentQuestionName, 1);
            curRank = db.rankChangeAnswer(CurrentSetName,questArrayList.get(position-1),CurrentQuestionName,2);
            //re-read the question
            //displayQuestion(quest_position);

            if (debugFlag == 1){

                Toast.makeText(this, "Current rank of an answer: " + curRank, Toast.LENGTH_SHORT).show();
            }

        }
        else {
            Answer = questArrayList.get(position);
            if (debugFlag == 1){

                Toast.makeText(this, "can't change rank, already on top " + Answer, Toast.LENGTH_SHORT).show();
            }
        }
        displayQuestion(quest_position);
    }


    public void rankDown (View view, int pos){

        //view.setItemChecked(position, true);

        //11.11
        //questList = db.getOneSet(CurQuizName);

        int Answers = questArrayList.size();

        if (debugFlag == 1){
        Log.d("QUIZREC: change rank of an answer ", "position " + quest_position + " Id " + myId + " number of questions " + String.valueOf(questList.size()));
        }

        //exception here?


        String CurrentQuestionName = questList.get(quest_position);
        String CurrentSetName = CurQuizName;
        String curRank;

        if (debugFlag == 1){

            Toast.makeText(this, "Change rank of an answer", Toast.LENGTH_SHORT).show();
        }
        String Answer = "NULL";
//        final int position = getListView().getPositionForView((RelativeLayout)view.getParent());
        final int position = pos;

        if (debugFlag == 1){

            Log.d("QUIZREC: edit answers delete position  ", String.valueOf(position));
        }

        if (position < Answers -1) {
            Answer = questArrayList.get(position);

            if (debugFlag == 1){

                Toast.makeText(this, "going to change rank down" + Answer, Toast.LENGTH_SHORT).show();
            }
            curRank = db.rankChangeAnswer(CurrentSetName,Answer,CurrentQuestionName,2);
            curRank = db.rankChangeAnswer(CurrentSetName,questArrayList.get(position+1),CurrentQuestionName,1);
            //re-read the question
            //displayQuestion(quest_position);
            if (debugFlag == 1){

                Toast.makeText(this, "Current rank of an answer: " + curRank, Toast.LENGTH_SHORT).show();
            }

        }
        else {
            Answer = questArrayList.get(position);

            if (debugFlag == 1){

                Toast.makeText(this, "can't change rank, already at bottom " + Answer, Toast.LENGTH_SHORT).show();
            }
        }
        displayQuestion(quest_position);
    }

    public static String[] addArray(String[] array, String string){
        ArrayList<String> lst = new ArrayList<String>();
        for (String s : array){
            lst.add(s);
        }
        lst.add(string);
        return (String[]) lst.toArray();
    }

    public void addAnswer(View view){

        if (debugFlag == 1){

            Toast.makeText(this, "adding answer", Toast.LENGTH_SHORT).show();
        }

        //11.11
        //questList = db.getOneSet(CurQuizName);

        if (debugFlag == 1){

            Log.d("QUIZREC: adding answer , getting question and set names  ", "position " + quest_position + " Id " + myId + " number of questions " + String.valueOf(questList.size()));
        //exception here?
        }
        String CurrentQuestionNameAdd = questList.get(quest_position);
        String CurrentSetNameAdd = CurQuizName;

        //09.03.14
        //String AnswerToAdd = " ";

        //25.04.14
        String AnswerToAdd = "New Answer";

        String addResult = db.addQuest(new Quest(CurrentSetNameAdd, CurrentQuestionNameAdd, AnswerToAdd,"1",0));

        if (debugFlag == 1){

            Toast.makeText(this, "going to add answer " + AnswerToAdd, Toast.LENGTH_SHORT).show();
        }
        if (addResult.equals("DUPLICATE")){
            Toast.makeText(this, getString(R.string.answer_already_exists_warning) + AnswerToAdd, Toast.LENGTH_SHORT).show();
        }

        //re-read the question
        displayQuestion(quest_position);
        answerListView.setSelection(answerListView.getAdapter().getCount()-1);



        /*
        questArrayList.add(0,"");
        adapter.notifyDataSetChanged();
        */
/*
        View o = getListView().getChildAt(0).findViewById(R.id.editAnswerText);

        final EditText EditAnswer;
        EditAnswer = (EditText) o.findViewById(R.id.editAnswerText);
        EditAnswer.setFocusableInTouchMode(true);
        EditAnswer.setFocusable(true);
        EditAnswer.setActivated(true);
        EditAnswer.setEnabled(true);
        EditAnswer.requestFocus();

        editAnswer(view);
*/

}



    public void nextQuestion (View view){

    int questions_in_quiz = db.getQuestsCountInSet("set_name",RunQuiz.setArrayList.get(myId));

        if (quest_position < questions_in_quiz-1) {

        quest_position++;

            if (debugFlag == 1){

                Toast.makeText(this, "move to next question " + quest_position + "from totally " + questions_in_quiz + " " + myId, Toast.LENGTH_SHORT).show();
            }
                displayQuestion(quest_position);
        } else {


            ImageButton nextButton = (ImageButton) view.findViewById(R.id.imageButtonNext);

            //works, but all buttons get moved as this disappears
            //nextButton.setVisibility(View.GONE);
            if (debugFlag == 1){

                Toast.makeText(this, "can't move to next question " + quest_position + "from totally " + questions_in_quiz + " " + myId, Toast.LENGTH_SHORT).show();
            }
                postLastQuestion();
        }
    }

    public void postLastQuestion(){

    }

    public void prevQuestion (View view){

        //11.11
        int questions_in_quiz = db.getQuestsCountInSet("set_name",CurQuizName);

        //int questions_in_quiz = db.getQuestsCountInSet("set_name",db.getQuizName(myId));

        if (quest_position > 0) {

            quest_position--;

            if (debugFlag == 1){

                Toast.makeText(this, "move to prev question " + quest_position + "from totally " + questions_in_quiz + " " + myId, Toast.LENGTH_SHORT).show();

            }
                displayQuestion(quest_position);
        }
        else {
            ImageButton nextButton = (ImageButton) this.findViewById(R.id.imageButtonPrev);
            //nextButton.setFocusable(false);
            //nextButton.setFocusableInTouchMode(false);
            //nextButton.setActivated(false);
            //nextButton.setEnabled(false);

            //works, but all buttons get moved as this disappears
            //nextButton.setVisibility(View.GONE);

            if (debugFlag == 1){

                Toast.makeText(this, "can't move to prev question " + quest_position + "from totally " + questions_in_quiz + " " + myId, Toast.LENGTH_SHORT).show();
            }
        }
    }


    public void delQuestion (View view){

        int questions_in_quiz = db.getQuestsCountInSet("set_name",RunQuiz.setArrayList.get(myId));

        //11.11
        final String CurrentSetName = CurQuizName;
        final String CurrentQuestionName = questList.get(quest_position);

        if (quest_position >= 0) {

            //EditText curQuestion = (EditText) this.findViewById(R.id.QuestionNameTextView);


            if (debugFlag == 1){

            Toast.makeText(this, "going to delete question " + CurrentQuestionName + "from totally " + questions_in_quiz + " " + myId, Toast.LENGTH_SHORT).show();
            Log.d("QUIZREC: going to delete question ",CurrentQuestionName + " position "+ quest_position + "from totally " + questions_in_quiz + " ");
            }

            if((quest_position == questions_in_quiz-1) && (quest_position !=0)){
                //last, not first: delete and move 1 back
                if (debugFlag == 1){

                    Log.d("QUIZREC: going to delete question last, not first: delete and move 1 back, position ", String.valueOf(quest_position));
                }
                    db.deleteQuestion(CurrentSetName,CurrentQuestionName);
                quest_position--;

                displayQuestion(quest_position);
                return;
            }
            // not last not first: delete and move forward
            if((quest_position != questions_in_quiz-1) && (quest_position !=0)){

                if (debugFlag == 1){

                    Log.d("QUIZREC: going to delete question ","not last not first: delete and move forward");
                }
                    db.deleteQuestion(CurrentSetName,CurrentQuestionName);

                // todo test with below exception if deleting middle question
                // displayQuestion(quest_position+1);
                //09.02 seems ok
                displayQuestion(quest_position);
                return;
            }
            // not last but first: delete and display the first still
            if((quest_position != questions_in_quiz-1) && (quest_position ==0)){

                if (debugFlag == 1){

                    Log.d("QUIZREC: going to delete question ","not last but first: delete and move forward");
                }
                    db.deleteQuestion(CurrentSetName,CurrentQuestionName);

                displayQuestion(quest_position);
                return;
            }
            // first and last
            if((quest_position == questions_in_quiz-1) && (quest_position ==0)){

                if (debugFlag == 1){

                    Log.d("QUIZREC: going to delete question can't delete","first and last");
                Toast.makeText(this, "cant delete only one question in a set" + CurrentQuestionName + "from totally " + questions_in_quiz + " " + myId, Toast.LENGTH_SHORT).show();
                Log.d("QUIZREC: going to delete question - can't delete, going to display","first and last");
                }

                    displayQuestion(quest_position);
                return;
            }

            //nextQuestion(findViewById(R.id.imageButtonNext));

        }
        else {

            //todo  string resourse ???
            Toast.makeText(this, "can't delete question " + RunQuiz.setArrayList.get(myId) + "from totally " + questions_in_quiz + " " + myId, Toast.LENGTH_SHORT).show();
        }
    }

    /* not used 11.05.14

    public void editNameQuestion (View view){

        //not used any more

        int questions_in_quiz = db.getQuestsCountInSet("set_name",CurQuizName);

        if (debugFlag == 1){
        Log.d("QUIZREC: newquestion editNameQuestion, position ", String.valueOf(quest_position) );
        }

        if (quest_position >= 0) {

            final String CurrentSetNameEdit = RunQuiz.setArrayList.get(myId);
            final String CurrentQuestionNameEdit = questList.get(quest_position);



            //to have "DONE" button: add android:singleLine="true" to xml and android:imeOptions="actionDone"
            //EditText curQuestion = (EditText) findViewById(R.id.QuestionNameTextView);
            TextView curQuestion = (TextView) findViewById(R.id.QuestionNameTextView);


            if (debugFlag == 1){
            Toast.makeText(this, "edit question name " + curQuestion.getText().toString() + " from set " + CurrentSetNameEdit, Toast.LENGTH_SHORT).show();
            }
            curQuestion.setFocusableInTouchMode(true);
            curQuestion.setFocusable(true);
            curQuestion.setActivated(true);
            curQuestion.setEnabled(true);
            curQuestion.requestFocus();

            //db.renameQuestion(CurrentSetNameEdit,CurrentQuestionNameEdit,curQuestion.getText().toString());

            //todo BIG bug here, we only update question name if we had a soft keyboard. If we had HW keyboard, it's not updated
            //possible solution: switch to rename in Dialog, in the same way as rename answer

            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(curQuestion, InputMethodManager.SHOW_IMPLICIT);
            Log.d("QUIZREC: edit going to update db show keyboard",curQuestion.getText().toString());

            //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

            curQuestion.setOnEditorActionListener(new TextView.OnEditorActionListener() {

                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    //if ((actionId & EditorInfo.IME_MASK_ACTION) != 0) {

                    int keyCode = 0;

                    if ((actionId == EditorInfo.IME_ACTION_DONE) || (event.getKeyCode() == 66
                    //KeyEvent.KEYCODE_ENTER))
                    {

                        //Log.d("QUIZREC: rename_question key pressed", String.valueOf(event.getKeyCode()));
                        //if I leave getKeyCode, but no Enter done, we have exception on getKeyCode
                        Log.d("QUIZREC: rename_question key pressed", "done or enter");

                        //todo or to use a dialog for question name editting, see above

                        //remove keyboard
                        EditText curQuestion = (EditText) findViewById(R.id.QuestionNameTextView);
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(curQuestion.getWindowToken(), 0);

                        Log.d("QUIZREC: rename_question edit going to update db rename question, new:", curQuestion.getText().toString() + " old " + CurrentQuestionNameEdit);

                        db.renameQuestion(CurrentSetNameEdit,curQuestion.getText().toString(),CurrentQuestionNameEdit);

                        curQuestion.setFocusableInTouchMode(false);
                        curQuestion.setFocusable(false);
                        curQuestion.setActivated(false);
                        curQuestion.setEnabled(false);
                        curQuestion.requestFocus();

                        Log.d("QUIZREC: newquestion edit done, goind to show position ", String.valueOf(quest_position) );

                        questList = db.getOneSet(CurQuizName);
                        int PositionAdded = questList.indexOf(curQuestion.getText().toString());
                        quest_position = PositionAdded;
                        Log.d("QUIZREC: newquestion edit sorted, position ", String.valueOf(quest_position) );
                        displayQuestion(quest_position);

                        return true;
                    }
                    else {
                        return false;

                }

        }
            });
        }
        else {

            Toast.makeText(this, "can't edit question name" +  RunQuiz.setArrayList.get(myId)+ "from totally " + questions_in_quiz + " " + myId, Toast.LENGTH_SHORT).show();
        }

    }

*/

    public void addQuestion (View view){

        //11.11
        final String CurrentSetName = CurQuizName;

        if (debugFlag == 1){
            Log.d("QUIZREC: going to add blank question to the set: ", CurrentSetName);
            Toast.makeText(this, "going to add blank question to the set: " + CurrentSetName, Toast.LENGTH_SHORT).show();
        }
        //String addResult = db.addQuest(new Quest(CurrentSetName, " ", " ","1",0));
        String addResult = db.addQuest(new Quest(CurrentSetName, "New Question", "New Answer","1",0));
        //Quest(int id, String set, String question, String answer, String status, int rank){

        if (addResult.equals("DUPLICATE")){
            Toast.makeText(this, getString(R.string.question_already_exists_warning) + CurrentSetName, Toast.LENGTH_SHORT).show();
            displayQuestion(quest_position);
        }
        else {
        //int  questions_in_quiz = db.getQuestsCountInSet("set_name",CurrentSetName);
        //done 12.2.14 ? if there was one question and I have added one more, there is an exception invalid index 1, size 1
        // but for some wired reason questions are sorted alfa in the list and when we edit a question we stay on 0 pos, but the question moves
            //according to its alfanumeric sort order

            //quest_position ++;

            questList = db.getOneSet(CurQuizName);
            //int PositionAdded = questList.indexOf(" ");

            //looks like lifter having a method to clean up the strings from spaces, we have this problem that one space
            //which used to denote a new question, cleaned up to empty field
            int PositionAdded = questList.indexOf("New Question");
            quest_position = PositionAdded;

            if (debugFlag == 1){

                Log.d("QUIZREC: newquestion addQuestion, position ", String.valueOf(quest_position) );
            }
            displayQuestion(quest_position);


            //displayQuestion(questions_in_quiz-1);
            //1.12
            //quest_position = questions_in_quiz-1;
        }

    }


    public void editAnswerD(View view){

        //final EditText EditAnswer;
        final TextView EditAnswer;


        if (debugFlag == 1){
        Log.d("QUIZREC: edit answers button pressed: ", String.valueOf(EditSwitch));
        }

        final int position = getListView().getPositionForView((RelativeLayout)view.getParent());


        //String CurrentQuestionNameEdit = questList.get(quest_position);
        String CurrentQuestionNameEdit = questList.get(quest_position);

        if (debugFlag == 1){
        Log.d("QUIZREC: Trying to add. Please set the question name first: ", CurrentQuestionNameEdit + " quest_position " + quest_position);
        }
        if (CurrentQuestionNameEdit.equals(" ")){
            Toast.makeText(this, getString(R.string.cant_add_answer_to_empty_question), Toast.LENGTH_SHORT).show();

            return;
        }

        if (CurrentQuestionNameEdit.equals("New Question")){
            Toast.makeText(this, getString(R.string.change_default_question_name_warning), Toast.LENGTH_SHORT).show();

            return;
        }

        //if we have not pressed edit button yet

        //remove change icon
        //if (EditSwitch == 0) {

        if (debugFlag == 1){

        Toast.makeText(this, "Editing answer", Toast.LENGTH_SHORT).show();
        Log.d("QUIZREC: edit answers edit position  ", String.valueOf(position));
        }
        if (position >= 0) {
            final String AnswerToEdit = questArrayList.get(position);

            if (debugFlag == 1){
                Log.d("QUIZREC: edit going to edit answer",AnswerToEdit);
            }

            View o = getListView().getChildAt(position - getListView().getFirstVisiblePosition()).findViewById(R.id.editAnswerText);

            //EditAnswer = (EditText) o.findViewById(R.id.editAnswerText);
            EditAnswer = (TextView) o.findViewById(R.id.editAnswerText);

            AlertDialog.Builder alert = new AlertDialog.Builder(this);

            alert.setTitle("Edit Answer");
            //alert.setMessage("Message");

// Set an EditText view to get user input
            final EditText input = new EditText(this);
            alert.setView(input);
            input.setText(AnswerToEdit);

            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String AnswerNew = input.getText().toString();
                    String CurrentQuestionNameEdit = questList.get(quest_position);
                    String CurrentSetNameEdit = CurQuizName;

                    //if (CurrentQuestionNameEdit != ""){

                    if (debugFlag == 1){
                        Log.d("QUIZREC: edit going to update db ",EditAnswer.getText().toString());
                    }
                    db.renameAnswer(CurrentSetNameEdit, CurrentQuestionNameEdit, AnswerNew, AnswerToEdit);

                    if (debugFlag == 1){
                        Log.d("QUIZREC: edit db updated", "");
                    }
                    //}
                    //else {
                        //todo first we need to change question if it's blank
                    //}
                    //re-read the question
                    displayQuestion(quest_position);


                }
            });

            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    //re-read the question
                    displayQuestion(quest_position);
                }
            });

            alert.show();


    }
    }

    public void editNameQuestionD(View view){

        final TextView curQuestion = (TextView) findViewById(R.id.QuestionNameTextView);

        if (quest_position >= 0) {

            final String CurrentSetNameEdit = RunQuiz.setArrayList.get(myId);
            final String CurrentQuestionNameEdit = questList.get(quest_position);

        if (debugFlag == 1){
        Log.d("QUIZREC: edit question dialog", CurrentQuestionNameEdit + " quest_position " + quest_position);
        }


        //24.04.14
        /*
            if (CurrentQuestionNameEdit.equals("New Question")){
            Toast.makeText(this, getString(R.string.question_name_cant_be_empty_warning), Toast.LENGTH_SHORT).show();

            return;
        }
        */


        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Edit Question");

// Set an EditText view to get user input
        final EditText input = new EditText(this);
        alert.setView(input);
        input.setText(CurrentQuestionNameEdit);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String QuestionNew = input.getText().toString();
                String CurrentQuestionNameEdit = questList.get(quest_position);
                String CurrentSetNameEdit = CurQuizName;

                //if (CurrentQuestionNameEdit != ""){
                if (debugFlag == 1){
                Log.d("QUIZREC: edit question going to update db ",curQuestion.getText().toString());
                }
                //db.renameAnswer(CurrentSetNameEdit, CurrentQuestionNameEdit, AnswerNew, AnswerToEdit);
                if (db.questionExists(CurrentSetNameEdit, QuestionNew)) {
                    Toast.makeText(EditQuiz.this, "Question already exists", Toast.LENGTH_SHORT).show();
                } else {
                    db.renameQuestion(CurrentSetNameEdit, QuestionNew, CurrentQuestionNameEdit);

                    if (debugFlag == 1) {
                        Log.d("QUIZREC: edit question db updated", "");
                    }
                    //}
                    //else {
                    //todo first we need to change question if it's blank
                    //}
                    //re-read the question
                    displayQuestion(quest_position);
                }

            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //re-read the question
                displayQuestion(quest_position);
            }
        });

        alert.show();


    }
}

    public void editAnswer(View view){

        final EditText EditAnswer;

        if (debugFlag == 1){
        Log.d("QUIZREC: edit answers button pressed: ", String.valueOf(EditSwitch));
        }

        final int position = getListView().getPositionForView((RelativeLayout)view.getParent());

        //if we have not pressed edit button yet

        //remove change icon
        //if (EditSwitch == 0) {

        if (debugFlag == 1){
        Toast.makeText(this, "Editing answer", Toast.LENGTH_SHORT).show();
        //final String AnswerToEdit;
        //final int position = getListView().getPositionForView((RelativeLayout)view.getParent());
        Log.d("QUIZREC: edit answers edit position  ", String.valueOf(position));
        }
        if (position >= 0) {
            final String AnswerToEdit = questArrayList.get(position);

            if (debugFlag == 1){
            Log.d("QUIZREC: edit going to edit answer",AnswerToEdit);
            }


        //if the cell is well off the screen, I get exception
        // to solve:    View o = listView.getChildAt(position - listView.getFirstVisiblePosition()).findViewById(R.id.blocCheck);
        // http://stackoverflow.com/questions/11609693/listview-with-arrayadapter-exception-when-i-click-on-a-row

        View o = getListView().getChildAt(position - getListView().getFirstVisiblePosition()).findViewById(R.id.editAnswerText);

        //EditAnswer = (EditText) getListView().getChildAt(position).findViewById(R.id.editAnswerText);
        EditAnswer = (EditText) o.findViewById(R.id.editAnswerText);
        EditAnswer.setFocusableInTouchMode(true);
        EditAnswer.setFocusable(true);
        EditAnswer.setActivated(true);
        EditAnswer.setEnabled(true);
        EditAnswer.requestFocus();
        EditAnswer.requestFocusFromTouch();


        // on samsung edittext scrolls down after keyboard appears
        // on emulator it's fine
        // this does not help
        //answerListView.setSelection(answerListView.getCount());
        //answerListView.smoothScrollToPosition(answerListView.getCount());
        //answerListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);


            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

            //try to show KB as soon as edit strated
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(EditAnswer, InputMethodManager.SHOW_IMPLICIT);

            if (debugFlag == 1){
            Log.d("QUIZREC: edit going to edit answer in edittext",EditAnswer.getText().toString());
            }


            //String NewAnswer = EditAnswer.getText().toString();

            //questArray[position]= NewAnswer;


            //if I have this here, the field never gets editable ???
            //adapter.notifyDataSetChanged();


        //remove change icon
        //ImageButton EditIcon = (ImageButton) getListView().getChildAt(position).findViewById(R.id.startEditButton);
        //EditIcon.setBackgroundResource(R.drawable.ic_navigation_accept);



            EditAnswer.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        //questArrayList.add(position, EditAnswer.getText().toString());
                        //adapter.notifyDataSetChanged();

                        String CurrentQuestionNameEdit = questList.get(quest_position);
                        String CurrentSetNameEdit = RunQuiz.setArrayList.get(myId);

                        //remove keyboard
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(EditAnswer.getWindowToken(), 0);

                        Log.d("QUIZREC: edit going to update db ",EditAnswer.getText().toString());
                        db.renameAnswer(CurrentSetNameEdit, CurrentQuestionNameEdit, EditAnswer.getText().toString(), AnswerToEdit);

                        if (debugFlag == 1){
                        Log.d("QUIZREC: edit db updated", "");
                        }

                        //re-read the question
                        displayQuestion(quest_position);

                        return true;
                    }
                    else {

                        if (debugFlag == 1){
                        Log.d("QUIZREC: edit not DONE 1", "");
                        }
                        return false;
                    }
                }
            });


        }
    }



    }