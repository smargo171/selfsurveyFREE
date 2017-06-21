package com.omplusm.selfsurveyFREE;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import java.text.ParseException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import at.grabner.circleprogress.CircleProgressView;

/**
 * Created by s3rg on 8/31/13.
 */
public class RunOne extends EditQuiz {


    CircleProgressView circleProgressView;
    public final static int debugFlag = 0;

    private  ResultAnswerArrayAdapter adapter;

    // todo can't return to the quiz list with UP button, back physical button OK


    public Date date;
    public String CurQuizName;

    private RadioButton listRadioButton =null;
    int listIndex =-1;


    public void displayQuestion(int position){

        if (debugFlag == 1){
        Toast.makeText(this, "Going to run one question", Toast.LENGTH_SHORT).show();
        }

        int progress = 0;

        if (debugFlag == 1){
        Log.d("QUIZREC: displayQuestion   ", "position " + position + " Id " + myId + " number of questions " + String.valueOf(questList.size()));
        }

        //done TODO: exception here 01.03.14 after deleting the last answer in a set
        if (questList.size() >0){
        CurrentQuestionName = questList.get(position);
        }
        else {
            Toast.makeText(this, getString(R.string.cant_display_empty_quest_warning), Toast.LENGTH_SHORT).show();
            return;
        }

        //lollipop 22.06.15
        //TextView CurrentQuizQuestion = (TextView)this.findViewById(R.id.QuestionNameTextView);
        TextView CurrentQuizQuestion = (TextView)view.findViewById(R.id.QuestionNameTextView);


        //16.03.13
        CurrentQuizQuestion.setFocusable(true);
        CurrentQuizQuestion.setEnabled(true);
        CurrentQuizQuestion.setText(CurrentQuestionName);

        if (debugFlag == 1) {
            Log.d("QUIZREC: CurrentQuestionName  ", CurrentQuestionName);
        }

        //11.11
        //questArrayList = (ArrayList<String>) db.getAnswersOneSet(db.getQuizName(myId),CurrentQuestionName);
        questArrayList = (ArrayList<String>) db.getAnswersOneSet(CurQuizName,CurrentQuestionName);

        //questArrayList = new ArrayList<String>(Arrays.asList(questArray));

        if (debugFlag == 1) {
            Log.d("QUIZREC: edit answers array size  ", String.valueOf(questList.size()));
        }

        //adapter = new AnswerListArrayAdapter(this,questArray);
        //R.layout.list_row_edit

        //3.09.13
        //adapter = new AnswerListArrayAdapter(this,questArrayList,R.layout.result_row);
        adapter = new ResultAnswerArrayAdapter(this,questArrayList);


        //ResultAnswerArrayAdapter.SelectedAnswer = -1;
        adapter.SelectedAnswer = -1;

        //04.02.14 to show the result of previos selection if we go back in the same quiz
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //adapter.SelectedAnswer = Integer.parseInt(db.getResult(db.getQuizName(myId), CurrentQuestionName, dateFormat.format(date)))-1;
        adapter.SelectedAnswer = Integer.parseInt(db.getResult(CurQuizName, CurrentQuestionName, dateFormat.format(date)))-1;

        //lollipop 22.06.15
        //answerListView = (ListView) findViewById(android.R.id.list);
        answerListView = (ListView) view.findViewById(android.R.id.list);
        answerListView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        //lollipop 22.06.15
        //ProgressBar mProgressBar = (ProgressBar)findViewById(R.id.progressBar);
        ProgressBar mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        int done = position+1;
        int total = questList.size();
        progress = ((done*100)/total);

        if (circleProgressView != null) {
            circleProgressView.setText(String.valueOf(position + 1));
            circleProgressView.setMaxValue(total);
            circleProgressView.setValue(position+1);
        }
        if (debugFlag == 1) {
            Log.d("QUIZREC: progress  " + String.valueOf(progress) + " done = ", done + " total = " + total);
        }

        mProgressBar.setMax(100);
        mProgressBar.setProgress(progress);

    }

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.quiz);
        TextView CurrentQuiz = (TextView)view.findViewById(R.id.textView);
        CurrentQuiz.setText(RunQuiz.setArrayList.get(myId));
        circleProgressView = (CircleProgressView)findViewById(R.id.circleView);
        circleProgressView.setText(String.valueOf(1));
        circleProgressView.setValue(1);
        circleProgressView.setMaxValue(questList.size());
        displayQuestion(0);
//        circleProgressView.setBarColor(R.color.progres1, R.color.progres2,R.color.progres3, R.color.progres4);
    }

    @Override
    public void afterMyCreate(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        date = new Date();

        //select date
        selectDate();

        /* lillipop 22.06.15
        ImageButton curButton = (ImageButton) this.findViewById(R.id.addAnswerButton);
        curButton.setVisibility(View.INVISIBLE);
        ImageButton curButton1 = (ImageButton) this.findViewById(R.id.imageButtonAddQuestion);
        curButton1.setVisibility(View.GONE);
        ImageButton curButton2 = (ImageButton) this.findViewById(R.id.imageButtonDelQuestion);
        curButton2.setVisibility(View.GONE);
        ImageButton curButton3 = (ImageButton) this.findViewById(R.id.imageButtonEditQuestion);
        curButton3.setVisibility(View.GONE);
        */


        ImageButton curButton = (ImageButton) view.findViewById(R.id.addAnswerButton);
        curButton.setVisibility(View.INVISIBLE);
        ImageButton curButton1 = (ImageButton) view.findViewById(R.id.imageButtonAddQuestion);
        curButton1.setVisibility(View.GONE);
        ImageButton curButton2 = (ImageButton) view.findViewById(R.id.imageButtonDelQuestion);
        curButton2.setVisibility(View.GONE);
        ImageButton curButton3 = (ImageButton) view.findViewById(R.id.imageButtonEditQuestion);
        curButton3.setVisibility(View.GONE);

        //CurQuizName = db.getQuizName(myId);
        CurQuizName = RunQuiz.setArrayList.get(myId);

        questList = db.getOneSet(CurQuizName);

    }

    public void selectAnswer (View view) {
        final int position = getListView().getPositionForView((RelativeLayout)view.getParent());

        //  no need
        // answerListView.setSelection(position);

        if (debugFlag == 1) {
            Log.d("QUIZREC: selectAnswer RunOne position:  ", String.valueOf(position));
        }

        //ResultAnswerArrayAdapter.SelectedAnswer=position;
        adapter.SelectedAnswer=position;

        //View o = getListView().getChildAt(position - getListView().getFirstVisiblePosition()).findViewById(R.id.editAnswerText);

        //EditAnswer = (EditText) getListView().getChildAt(position).findViewById(R.id.editAnswerText);
        //EditAnswer = (EditText) o.findViewById(R.id.editAnswerText);
        
        View vMain =((View) view.getParent());// getParent() must be added 'n' times,
        // where 'n' is the number of RadioButtons' nested parents
        // in your case is one.
        // uncheck previous checked button.
        if(listRadioButton !=null) listRadioButton.setChecked(false);
        // assign to the variable the new one
        listRadioButton =(RadioButton) view;
        // find if the new one is checked or not, and set "listIndex"
        if(listRadioButton.isChecked())
        {listIndex =((ViewGroup) vMain.getParent()).indexOfChild(vMain);
        }else{            
            listRadioButton =null;            
            listIndex =-1;}

        // need to do it because otherwise prev. selected items stays afrer scrolling
        // we invalidate datasourse and make ds adapter not to use cash and forget about prev. selection


        //11.11
        //questList = db.getOneSet(db.getQuizName(myId));
        //questList = db.getOneSet(CurQuizName);


        String CurrentQuestionName = questList.get(quest_position);

        //
        //String CurrentSetName = db.getQuizName(myId);
        String CurrentSetName = CurQuizName;


        if (debugFlag == 1) {
            Log.d("QUIZREC: add result , position  ", quest_position + " set " + CurrentSetName + " question " + CurrentQuestionName + " rank " + position + 1);
            //exception here?
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //Date date = new Date();

        db.addResult(CurrentSetName, CurrentQuestionName,position+1,dateFormat.format(date));


        adapter.notifyDataSetChanged();

        //10.10.15 we move to next question right after we selected an answer, no pressing next button needed
        nextQuestion(view);
    }


    @Override
    public void postLastQuestion() {
    String meanResult;
    String meanResultVal;
    String resultText ="";

    int red, green, blue;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    meanResult=    db.calcRunOneResult(dateFormat.format(date),CurQuizName);
    meanResultVal=    db.calcRunOneResultMean(dateFormat.format(date), CurQuizName);

        if (debugFlag == 1){
        Log.d("QUIZREC: mean result  ",  meanResult);
        Toast.makeText(this, "mean result " + meanResult, Toast.LENGTH_SHORT).show();
        }

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Result");

        if (Integer.valueOf(meanResult) <= 30){
            resultText = getString(R.string.res_nok);
            }
        if ((Integer.valueOf(meanResult) <= 60)&& (Integer.valueOf(meanResult) > 30)){
            resultText = getString(R.string.res_not_perfect);
        }
        if ((Integer.valueOf(meanResult) <= 90)&& (Integer.valueOf(meanResult) > 60)){
            resultText = getString(R.string.res_quite_ok);
        }
        if ((Integer.valueOf(meanResult) <= 100)&& (Integer.valueOf(meanResult) > 90)){
            resultText = getString(R.string.res_ok);
        }



// Set an EditText view to get user input
        final TextView textResult = new TextView(this);
        alert.setView(textResult);
        //textResult.setText("Your "+CurQuizName+ " survey result is " + meanResult + " % optimal values.");
        textResult.setText(resultText);
                textResult.setPadding(30,30,30,30);
        textResult.setGravity(1);

        if (Integer.valueOf(meanResult) <= 50){
        red= 255;
        green = Math.round(255*(Integer.valueOf(meanResult))/100);
        blue = 0;
        textResult.setBackgroundColor(Color.rgb(red, green, blue));}
        else {
        red= 255-Math.round(255*(Integer.valueOf(meanResult))/100);
        green = 255;
        blue = 0;
        textResult.setBackgroundColor(Color.rgb(red,green,blue));}

        if (debugFlag == 1){
        Log.d("QUIZREC: mean result, color red ", String.valueOf(red) + " green " + String.valueOf(green) + " blue " + String.valueOf(blue));
        }


        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            finish();

            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        alert.show();


    }

   public void showDatePicker(){



   }


   public void selectDate (){


       SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

       final String selected_date;

       //final Date CurDate = new Date();
       Date CurDate;

       //String CurrentSetName = db.getQuizName(myId);
       String CurrentSetName = RunQuiz.setArrayList.get(myId);

       //Dialog dialog = new Dialog(this);
       //dialog.setContentView(R.layout.result_spinner);

       AlertDialog.Builder builder;
       AlertDialog alertDialog;


       //Context mContext = getApplicationContext();
       Context mContext = RunOne.this;
       LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
       View layout = inflater.inflate(R.layout.result_spinner,null);

       ArrayList<String> arraySpinner;

       arraySpinner=(ArrayList<String>) db.getAllDates(CurrentSetName);

       //add current date
       //arraySpinner.add(arraySpinner.size(),getString(R.string.new_run_date));
       arraySpinner.add(0,getString(R.string.new_run_date));

       //to be done
       //arraySpinner.add(1,getString(R.string.set_run_date));

       final Spinner s = (Spinner) layout.findViewById(R.id.spinner);

       if(arraySpinner.size()==0) {return;}

       ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_spinner_item, arraySpinner);
       adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
       s.setAdapter(adapter);
       //s.setSelection(arraySpinner.size()-1,true);
       s.setSelection(0,true);

       builder = new AlertDialog.Builder(mContext);
       builder.setView(layout);


       AlertDialog.Builder builder1 = builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int whichButton) {

               if (s.getSelectedItem().toString().equals(getString(R.string.set_run_date))) {
                   //we enter a free input date
                   showDatePicker();

               }


               if (s.getSelectedItem().toString().equals(getString(R.string.new_run_date))) {
                   //current date
                   //we don't change global date

               } else {
                   String date_string = s.getSelectedItem().toString();
                   SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                   //dateFormat.format(date)
                   try {
                       date = dateFormat.parse(date_string);


                       if (debugFlag == 1){
                           Log.d("QUIZREC: spinner date selected  ", date_string);
                       }
                       displayQuestion(quest_position);

                   } catch (ParseException e) {

                       if (debugFlag == 1){
                           Log.d("QUIZREC: spinner date not date format ", date_string);
                       }
                   }


               }


           }
       });

       builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int whichButton) {
            finish();
           }
       });


       alertDialog = builder.create();
       alertDialog.setTitle("Select Date:");

       alertDialog.show();
   }

}
