package com.omplusm.selfsurveyFREE;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.lang.Object;

//import com.amulyakhare.textdrawable;

/**
 * Created by s3rg on 6/30/13.
 */

/**
 * Adapter that exposes data from a Cursor to a ListView widget. The Cursor must include a column named "_id"
 * or this class will not work.
 */

public class QuerySetListCellArrayAdapter extends ArrayAdapter<String> {

    private final Context context;
    //private final String[] values;
    private final ArrayList<String> values;

    public int Layout;

    public DatabaseHandler db = RunQuiz.db;

    public final static int debugFlag = 0;

    public String questName;
    public String setName;

    public QuerySetListCellArrayAdapter (Context context, ArrayList<String> values, int Layout) {

    super(context, Layout, values);

    this.context = context;
    this.values = values;
    this.Layout = Layout;

}

    /**
     * Bind an existing view to the data pointed to by cursor
     */

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.list_row, parent, false);
        /*
        Log.d("QUIZREC: ", "start bindView");
        TextView SetName=(TextView)view.findViewById(R.id.set_name);
        SetName.setText(cursor.getString(cursor.getColumnIndex("set_name")));
        Log.d("QUIZREC: in bindView, value: ", cursor.getString(cursor.getColumnIndex("set_name")));
        */
        //

        if (selected == position)
            ((CardView)view).setCardBackgroundColor(Color.parseColor("#dcdcdc"));

        if (debugFlag == 1){
        Log.d("QUIZREC: in getView, position ", String.valueOf(position));
        Log.d("QUIZREC: in getView, set ", values.get(position));
        }

        TextView SetName=(TextView)view.findViewById(R.id.set_name);
        SetName.setText(values.get(position));

        String substr=values.get(position).substring(0, 1);

        if (debugFlag == 1){
            Log.d("QUIZREC: in getView, drawable ", substr);
        }

        int IntColor=getIntColor(values.get(position));

        TextDrawable drawable = TextDrawable.builder()
                .buildRect(substr, IntColor);

        ImageView image = (ImageView) view.findViewById(R.id.drawable_image_view);
        image.setImageDrawable(drawable);


        TextView SetCount=(TextView)view.findViewById(R.id.questions_counter);
        int CountInSet=db.getQuestsCountInSet("set_name",values.get(position));
        String StringCountInSet= String.valueOf(CountInSet);
        SetCount.setText(StringCountInSet);

        if (debugFlag == 1){
        Log.d("QUIZREC: in bindView, count queries value: ",StringCountInSet);
        }


        //TextView FirstQuery=(TextView)view.findViewById(R.id.first_question);
        //String FirstInSet=db.getQuestsWithId(values.get(position));
        //FirstQuery.setText(FirstInSet);

        if (debugFlag == 1){
        //Log.d("QUIZREC: in bindView, 1st question in a set: ",FirstInSet);
        }

        TextView LastDate=(TextView)view.findViewById(R.id.lastDate);
        String LastDateVal="";
        String NumRunsVal="";
        LastDateVal= db.getLastDates(values.get(position));

        SimpleDateFormat from = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat to = new SimpleDateFormat("dd MMMMMMMMMM yyyy, HH:mm");
        try {

            //exception here Unparseable date: "" 10.09.15
            LastDateVal = to.format(from.parse(LastDateVal));
        } catch (ParseException e) {
//            e.printStackTrace();
        }

        NumRunsVal=db.getNumRuns(values.get(position));
        //LastDate.setText(LastDateString);
        //Log.d("QUIZREC: in bindView, last date  set: ",LastDateString);


        //todo replaced comparison 25.02.14 to test
        if (LastDateVal.equals("")){

            LastDateVal = "This survey has not been taken yet.";
            LastDate.setVisibility(View.GONE);
            view.findViewById(R.id.triangle).setVisibility(View.GONE);
            view.findViewById(R.id.cnt).setVisibility(View.GONE);
        }
        else {
            ((TextView)view.findViewById(R.id.cnt)).setText(String.valueOf(NumRunsVal));
//            LastDateVal = "Taken last time on " + LastDateVal + ", " + NumRunsVal + " times in total."  ;
            LastDateVal = LastDateVal;
            //LastDate.setTextColor(Color.rgb(0, 153, 0));
//            LastDate.setTextColor(Color.rgb(64, 64, 64));
            LastDate.setText(LastDateVal);
        }

        //TextView NumRuns=(TextView)view.findViewById(R.id.numRuns);
        //String NumRunsString=Mydb.getNumRuns(cursor.getString(cursor.getColumnIndex("set_name")));
        //NumRuns.setText(NumRunsString);
        //Log.d("QUIZREC: in bindView, number of runs of a set: ",NumRunsString);

        //todo
        //add average point (each answer relative to the highest mark)
        //now displaying the avg color of the lats run
        //view= setSetColor(view, values.get(position));


        return view;
    }

    private int selected = -1;
    public void setSelected(int pos) {
        selected = pos;
    }


    public View setSetColor (View view, String curSetName){

        String meanResult;
        int red, green, blue;
        String lastDate;


        TextView SetName=(TextView)view.findViewById(R.id.set_name);

        SetName.setEnabled(true);
        SetName.setTextColor(Color.rgb(0, 0, 0));

        lastDate = db.getLastDates(curSetName);
        if ((lastDate.equals("") ||(lastDate.equals(null))) ){
            //view.setBackgroundColor(Color.parseColor("#ff000000"));
            SetName.setTextColor(Color.parseColor("#ff000000"));
            return view;
        }


        if (debugFlag == 1){
        Log.d("QUIZREC: last date in runquiz  ",  lastDate);
        }

        meanResult=db.calcRunOneResult(lastDate,curSetName);

        if (debugFlag == 1){
            Log.d("QUIZREC: mean result  ",  meanResult);
        }

        if (Integer.valueOf(meanResult) <= 50){
            red= 255;
            green = Math.round(255*(Integer.valueOf(meanResult))/100);
            blue = 0;
            //view.setBackgroundColor(Color.rgb(red, green, blue));
        }
        else {
            red= 255-Math.round(255*(Integer.valueOf(meanResult))/100);
            green = 255;
            blue = 0;
            //view.setBackgroundColor(Color.rgb(red, green, blue));
        }

            SetName.setTextColor(Color.rgb(red, green, blue));
        return view;
    }

    public int getIntColor (String curSetName){

        String meanResult;
        int red, green, blue;
        String lastDate;


        Color.rgb(0, 0, 0);

        lastDate = db.getLastDates(curSetName);
        if ((lastDate.equals("") ||(lastDate.equals(null))) ){
            return Color.parseColor("#ff000000");
        }

        if (debugFlag == 1){
            Log.d("QUIZREC: last date in runquiz  ",  lastDate);
        }

        meanResult=db.calcRunOneResult(lastDate,curSetName);

        if (debugFlag == 1){
            Log.d("QUIZREC: mean result  ",  meanResult);
        }

        if (Integer.valueOf(meanResult) <= 50){
            red= 255;
            green = Math.round(255*(Integer.valueOf(meanResult))/100);
            blue = 0;
            //view.setBackgroundColor(Color.rgb(red, green, blue));
        }
        else {
            red= 255-Math.round(255*(Integer.valueOf(meanResult))/100);
            green = 255;
            blue = 0;
            //view.setBackgroundColor(Color.rgb(red, green, blue));
        }

        return Color.rgb(red, green, blue);
    }

}