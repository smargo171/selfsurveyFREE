package com.omplusm.selfsurveyFREE;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mobeta.android.dslv.DragSortListView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by s3rg on 8/31/13.
 */
public class ShowOne extends EditQuiz{


    //private ResultAnswerArrayAdapter adapter;
    private ShowAnswerArrayAdapter adapter;

    static public Date date;

    private RadioButton listRadioButton =null;
    int listIndex =-1;


public void displayQuestion(int position){

    if (debugFlag == 1){
        Toast.makeText(this, "Going to show one quiz", Toast.LENGTH_SHORT).show();
    }

        //questList = db.getOneSet(db.getQuizName(myId));
        questList = db.getOneSet(RunQuiz.setArrayList.get(myId));

        int progress = 0;

    if (debugFlag == 1){
    Log.d("QUIZREC: displayQuestion   ", "position " + position + " Id " + myId + " number of questions " + String.valueOf(questList.size()));
    }

        //TODO: exception here . when????
        CurrentQuestionName = questList.get(position);


        TextView CurrentQuizQuestion = (TextView)this.findViewById(R.id.QuestionNameTextView);

        //16.03.13
        CurrentQuizQuestion.setFocusable(true);
        CurrentQuizQuestion.setEnabled(true);
        CurrentQuizQuestion.setText(CurrentQuestionName);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    if (debugFlag == 1){
    Log.d("QUIZREC: spinner date  ", dateFormat.format(ShowOne.date));
    }

        //questArrayList = (ArrayList<String>) db.getResultsOneSet(db.getQuizName(myId), CurrentQuestionName, dateFormat.format(ShowOne.date));
        //questArrayList = (ArrayList<String>) db.getAnswersOneSet(db.getQuizName(myId),CurrentQuestionName);
        questArrayList = (ArrayList<String>) db.getAnswersOneSet(RunQuiz.setArrayList.get(myId),CurrentQuestionName);

        //questArrayList = new ArrayList<String>(Arrays.asList(questArray));

    if (debugFlag == 1){
    Log.d("QUIZREC: edit answers array size  ", String.valueOf(questList.size()));
    }

        //adapter = new AnswerListArrayAdapter(this,questArray);
        //R.layout.list_row_edit

        //3.09.13
        //adapter = new AnswerListArrayAdapter(this,questArrayList,R.layout.result_row);

        //27.10.13
        //adapter = new ResultAnswerArrayAdapter(this,questArrayList);
        adapter = new ShowAnswerArrayAdapter(this,questArrayList);


        //ResultAnswerArrayAdapter.SelectedAnswer = -1;
    if (debugFlag == 1){
        Log.d("QUIZREC: result to be shown  " ,db.getResult(RunQuiz.setArrayList.get(myId), CurrentQuestionName, dateFormat.format(ShowOne.date)) );
    }
        //adapter.SelectedAnswer = Integer.parseInt(db.getResult(db.getQuizName(myId), CurrentQuestionName, dateFormat.format(ShowOne.date)))-1;
    adapter.SelectedAnswer = Integer.parseInt(db.getResult(RunQuiz.setArrayList.get(myId), CurrentQuestionName, dateFormat.format(ShowOne.date)))-1;
        //todo we need to do something when a question was deleted and the rank can't point to this question

        answerListView = (ListView) findViewById(android.R.id.list);
        answerListView.setAdapter(adapter);
    ((DragSortListView)answerListView).setDragEnabled(false);

        adapter.notifyDataSetChanged();
    ((TextView)findViewById(R.id.title)).setText("SHOW RESULTS");
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

    /*
    public void onCreate(Bundle savedInstanceState) {
        Log.d("QUIZREC: show one on create  " , "");
        super.onCreate(savedInstanceState);
        afterMyCreate();

    }
    */

@Override
public void afterMyCreate(){

    if (debugFlag == 1){
        Log.d("QUIZREC: show one after create  " , "");
    }


    ImageButton curButton = (ImageButton) this.findViewById(R.id.addAnswerButton);
    curButton.setVisibility(View.INVISIBLE);
    ImageButton curButton1 = (ImageButton) this.findViewById(R.id.imageButtonAddQuestion);
    curButton1.setVisibility(View.GONE);
    ImageButton curButton2 = (ImageButton) this.findViewById(R.id.imageButtonDelQuestion);
    curButton2.setVisibility(View.GONE);
    ImageButton curButton3 = (ImageButton) this.findViewById(R.id.imageButtonEditQuestion);
    curButton3.setVisibility(View.GONE);

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    final String selected_date;

    ShowOne.date = new Date();

    //String CurrentSetName = db.getQuizName(myId);
    String CurrentSetName = RunQuiz.setArrayList.get(myId);

            //Dialog dialog = new Dialog(this);
    //dialog.setContentView(R.layout.result_spinner);

    AlertDialog.Builder builder;
    AlertDialog alertDialog;


    //Context mContext = getApplicationContext();
    Context mContext = ShowOne.this;
    LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
    View layout = inflater.inflate(R.layout.result_spinner,null);

    ArrayList<String> arraySpinner;

    arraySpinner=(ArrayList<String>) db.getAllDates(CurrentSetName);

    final Spinner s = (Spinner) layout.findViewById(R.id.spinner);

    if(arraySpinner.size()==0) {return;}

    ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_spinner_item, arraySpinner);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

    s.setAdapter(adapter);

    builder = new AlertDialog.Builder(mContext);
    builder.setView(layout);



    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {

            String date_string = s.getSelectedItem().toString();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //dateFormat.format(date)
            try {
                ShowOne.date = dateFormat.parse(date_string);
                if (debugFlag == 1){
                    Log.d("QUIZREC: spinner date selected  " , date_string);
                }
                displayQuestion(quest_position);
            } catch (ParseException e) {
                if (debugFlag == 1){
                    Log.d("QUIZREC: spinner date not date format " , date_string);
                }
            }
            ;


        }
    });

    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {

        }
    });


    alertDialog = builder.create();
    alertDialog.setTitle("Select Date:");

    alertDialog.show();



    }


}
