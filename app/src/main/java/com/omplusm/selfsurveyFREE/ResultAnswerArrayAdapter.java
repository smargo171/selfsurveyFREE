

package com.omplusm.selfsurveyFREE;


import android.graphics.Color;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by s3rg on 7/14/13.
 */

//http://www.vogella.com/articles/AndroidListView/ 2.2. Example Adapter

public class ResultAnswerArrayAdapter extends ArrayAdapter<String> {
    private final Context context;
    //private final String[] values;
    private final ArrayList<String> values;

    public static int SelectedAnswer;

    public final static int debugFlag = 0;

    int [] redColors;
    int [] greenColors;

    private static final int ITEM_VIEW_TYPE_ITEM = 0;
    private static final int ITEM_VIEW_TYPE_SEPARATOR = 1;




    //public AnswerListArrayAdapter(Context context, String[] values) {
    public ResultAnswerArrayAdapter(Context context, ArrayList<String> values) {
        super(context, R.layout.result_row, values);
        this.context = context;
        this.values = values;

        redColors = new int[values.size()];
        greenColors = new int[ values.size()];

    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {



        //TODO answer is the rank now, we need to get answers text from set and question

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.answer_row, parent, false);

        if (values.size()==0) {return rowView; }

        TextView answer = (TextView) rowView.findViewById(R.id.answer);
        answer.setText(values.get(position));

        RadioButton rb = (RadioButton) rowView.findViewById(R.id.radioButton);
        rb.setFocusable(false);
        rb.setFocusableInTouchMode(false);
        rb.setClickable(true);
        rb.setEnabled(true);

        if (debugFlag == 1){
            Log.d("QUIZREC: radiobutton selected answer  ", String.valueOf(SelectedAnswer) + " rb  " + String.valueOf(rb));
        }



        //TODO we need to use rank to mark the position
        if(SelectedAnswer == position)
        {
            rb.setChecked(true);
            if (debugFlag == 1){
                Log.d("QUIZREC: radiobutton selected answer  ", String.valueOf(SelectedAnswer) + " position  " + String.valueOf(position));
            }
        }
        else {rb.setChecked(false);}

        makeCellColor();
        // set cell backgroud color
        //rowView.setBackgroundColor(Color.rgb(redColors[position], greenColors[position], 0));
        View indicator=  rowView.findViewById(R.id.indicator);
        TextView cnt = (TextView) rowView.findViewById(R.id.cnt);
        cnt.setText(position+".");
        indicator.setBackgroundColor(Color.rgb(redColors[position], greenColors[position], 0));
        cnt.setTextColor(Color.rgb(redColors[position], greenColors[position], 0));

        //set font color

        return rowView;

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
}





