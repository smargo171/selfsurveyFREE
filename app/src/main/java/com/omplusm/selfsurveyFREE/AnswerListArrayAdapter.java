package com.omplusm.selfsurveyFREE;


import android.graphics.Color;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ArrayAdapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by s3rg on 7/14/13.
 */

//http://www.vogella.com/articles/AndroidListView/ 2.2. Example Adapter

public class AnswerListArrayAdapter extends ArrayAdapter<String> {
    private final Context context;
    //private final String[] values;
    private final ArrayList<String> values;

    public int Layout;

    public final static int debugFlag = 0;

    DatabaseHandler db = RunQuiz.db;

    public String questName;
    public String setName;



    int [] redColors;
    int [] greenColors;


    //public AnswerListArrayAdapter(Context context, String[] values) {
        public AnswerListArrayAdapter(Context context, ArrayList<String> values, int Layout) {

        //super(context, R.layout.list_row_edit, values);
        super(context, Layout, values);

        this.context = context;
        this.values = values;
        this.Layout = Layout;

        redColors = new int[values.size()];
        greenColors = new int[ values.size()];

    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {


        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View rowView = inflater.inflate(R.layout.edit_aswer_row, parent, false);
        //View rowView = inflater.inflate(Layout, parent, false);
        TextView EditAnswerView = (TextView) rowView.findViewById(R.id.editAnswerText);
        if (debugFlag == 1){
        Log.d("QUIZREC: edit position ", String.valueOf(position));
        }
        EditAnswerView.setText(values.get(position));
        EditAnswerView.setFocusable(false);
        EditAnswerView.setEnabled(false);

        TextView RankText = (TextView) rowView.findViewById(R.id.rankShow);
        int RankFromZero = Integer.valueOf(db.rankGetAnswer(setName, values.get(position), questName))-1;
        // we want to start ranking the answers from 0 now - in GUI only, db is still from 1
        //RankText.setText(db.rankGetAnswer(setName, values.get(position), questName));
        RankText.setText(String.valueOf(RankFromZero));

        //trying to resolve samsung bug
        //final EditText editInput = (EditText)convertView.findViewById(R.id.editAnswerText);
        //editInput.requestFocusFromTouch();

        makeCellColor();
        // set cell backgroud color
//        rowView.setBackgroundColor(Color.rgb(redColors[position], greenColors[position], 0));

        //11.05.14
        //TextView EditTextRankColor = (TextView) rowView.findViewById(R.id.editAnswerText);

        //EditTextRankColor.setBackgroundColor(Color.rgb(redColors[position], greenColors[position], 0));

        //set font color
        int d = 0;            // Counting the perceptive luminance - human eye favors green color...
        double a = 1 - ( 0.299 * redColors[position] + 0.587 * greenColors[position] + 0.114 * 0)/255;
        if (a < 0.5){d = 0;} // bright colors - black font
        else { d = 255;} // dark colors - white font

        RankText.setTextColor(Color.rgb(redColors[position], greenColors[position], 0));
        rowView.findViewById(R.id.indicator).setBackgroundColor(Color.rgb(redColors[position], greenColors[position], 0));
        final GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
            public void onLongPress(MotionEvent e) {
                Log.e("qwe", "Longpress detected");
                longpressed = rowView;
                longPessedPos = position;
            }

        });
        rowView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                gestureDetector.onTouchEvent(motionEvent);
                return false;
            }
        });

        return rowView;

    }
    public View longpressed = null;
    public Integer longPessedPos = null;
    public View getLongpressed() { return longpressed;}
    public Integer getLongPessedPos() {return longPessedPos;}

    public void dragEnd() {
        if (longpressed != null) {
            longpressed.findViewById(R.id.startEditButton).setVisibility(View.VISIBLE);
            longpressed.findViewById(R.id.deleteButton).setVisibility(View.VISIBLE);
            longpressed.findViewById(R.id.drag).setVisibility(View.GONE);
            longpressed = null;
            longPessedPos = null;
        }
    }

    public void makeCellColor() {
        int red =0;//i.e. FF
        int green =255;
        int stepSize=255/((values.size()+1)/2);
        int position =0;

        while(position < (values.size())/2)
        {
            greenColors[position] = green;
            redColors[position] = red;
            if (debugFlag == 1){
            Log.d("QUIZREC: color position green  ", String.valueOf(position) + " size " + String.valueOf(values.size()));
            }
                red += stepSize;
            position++;
            if (debugFlag == 1){
            Log.d("QUIZREC: color position RGB : position:", String.valueOf(position) + " RED:" + String.valueOf(red) + " GREEN " + String.valueOf(green));
            }
        }

        red=255;
        while(position <= (values.size())-1) {
            redColors[position] = red;
            greenColors[position] = green;
            if (debugFlag == 1){
            Log.d("QUIZREC: color position red  ", String.valueOf(position));
            }
            green -= stepSize;
            position++;
            if (debugFlag == 1){
            Log.d("QUIZREC: color position RGB : position:", String.valueOf(position) + " RED:" + String.valueOf(red) + " GREEN" + String.valueOf(green));
            }
        }

        //hack, sorry
        if ((values.size() > 1)){

            position = (values.size() - 1);
            redColors[position] = 255;
            greenColors[position] = 0;

        }


}

    public void dragStart(int position) {
        longpressed.findViewById(R.id.startEditButton).setVisibility(View.GONE);
        longpressed.findViewById(R.id.deleteButton).setVisibility(View.GONE);
        longpressed.findViewById(R.id.drag).setVisibility(View.VISIBLE);
    }
}

