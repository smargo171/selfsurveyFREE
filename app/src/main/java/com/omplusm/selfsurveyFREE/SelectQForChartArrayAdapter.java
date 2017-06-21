package com.omplusm.selfsurveyFREE;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by s3rg on 7/14/13.
 */

//http://www.vogella.com/articles/AndroidListView/ 2.2. Example Adapter

public class SelectQForChartArrayAdapter extends ArrayAdapter<String> {
    private final Context context;
    //private final String[] values;
    private final ArrayList<String> values;

    public ArrayList<String> SelectedQuestionsList;

    public final static int debugFlag = 0;


    public int Layout;

    DatabaseHandler db = RunQuiz.db;

    public String questName;
    public String setName;



    //public AnswerListArrayAdapter(Context context, String[] values) {
        public SelectQForChartArrayAdapter(Context context, ArrayList<String> values, int Layout) {

        //super(context, R.layout.list_row_edit, values);
        super(context, Layout, values);

        this.context = context;
        this.values = values;
        this.Layout = Layout;


            SelectedQuestionsList = new ArrayList<String>();
        }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {




        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.select_question_for_chart_one_row, parent, false);
        TextView EditAnswerView = (TextView) rowView.findViewById(R.id.selectedQuestion);
        EditAnswerView.setText(values.get(position));
        EditAnswerView.setFocusable(false);
        EditAnswerView.setFocusableInTouchMode(false);
        //18.03.14
        EditAnswerView.setEnabled(true);

        RadioButton rb = (RadioButton) rowView.findViewById(R.id.radioButtonSelectedQuestion);
        rb.setFocusable(false);
        rb.setFocusableInTouchMode(false);
        rb.setClickable(true);
        rb.setEnabled(true);
        rb.setChecked(false);

        String curQuest = values.get(position);

        if (SelectedQuestionsList.contains(curQuest)) {
            if (debugFlag == 1){
                Log.d("QUIZREC: radiobutton selected answer in the list ", " question added  " + values.get(position));
            }
            rb.setChecked(true);

        }
        else
        {
            if (debugFlag == 1){
                Log.d("QUIZREC: radiobutton selected answer NOT in the list ", " question added  " + values.get(position));
            }
            rb.setChecked(false);
        }


        if (debugFlag == 1){
            Log.d("QUIZREC: radiobutton selected answer  ", " rb  " + String.valueOf(rb));
        }
        return rowView;

    }




}

