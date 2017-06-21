package com.omplusm.selfsurveyFREE;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.Shape;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;


import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.graphics.Color;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;

/**
 * Created by s3rg on 9/26/13.
 */

// http://wptrafficanalyzer.in/blog/android-drawing-line-chart-using-achartengine/


public class DrawChart extends Activity {


    private View mChart;
    int myId;
    public static String CurrentSetName;

    public final static int debugFlag = 0;

    //not needed, to be removed
    //public static int CurrentSetId;
    List<String> questList;

    DatabaseHandler db = RunQuiz.db;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        /* 3.2.14
        String message = intent.getStringExtra(RunQuiz.EXTRA_MESSAGE);

        myId = 0;

        try {

            //in db we start from 1, in the view list we start from 0, I think
            myId = Integer.parseInt(message) +1;
            Log.d("QUIZREC: edit Selected iD  ", String.valueOf(myId));
        } catch(NumberFormatException nfe) {
            //System.out.println("Could not parse " + nfe);
            Log.d("QUIZREC: edit Could not parse  ", String.valueOf(nfe));
        }

        */

        String message = intent.getStringExtra(SelectQuestionsForChart.EXTRA_MESSAGE);

        ///*myId = 0;

        try {

            //in db we start from 1, in the view list we start from 0, I think
            CurrentSetName = message;
            if (debugFlag == 1){
            Log.d("QUIZREC: run chart after selecting questions, selected set  ", message);
            }
        } catch(NumberFormatException nfe) {
            //System.out.println("Could not parse " + nfe);
            if (debugFlag == 1){
            Log.d("QUIZREC: run chart after selecting questions, Could not parse  ", String.valueOf(nfe));
            }
        }

        ///* 3.2.14 CurrentSetName = db.getQuizName(myId);


        setContentView(R.layout.chart);
        openChart();
        returnToQuestions();


    }


    /*
    @Override
    public void onBackPressed() {
        String str1 = CurrentSetName;
        Intent data = new Intent();
        data.putExtra("CurQuizName", str1);
        setResult(RESULT_OK, data);
    }
    */
     public void returnToQuestions () {

         SelectQuestionsForChart.CurQuizName = CurrentSetName;

         TextView CurrentQuiz = (TextView)this.findViewById(R.id.chart_title);
         CurrentQuiz.setText(CurrentSetName);

         if (debugFlag == 1){
         Log.d("QUIZREC: returnToQuestions, CurrentSetName   ", CurrentSetName);
         }
         //String str1 = CurrentSetName;
         //Intent data = new Intent();
         //data.putExtra("CurQuizName", str1);
         //setResult(RESULT_OK, data);
     }

    private void drawNewChart() {
        Map<String, Integer> colors = new HashMap<>();
        List<String> dates = db.getAllDates(CurrentSetName);
        questList = SelectQuestionsForChart.questPassToChart;
        List<Line> lines = new ArrayList<Line>();
        int maxValY = Integer.MIN_VALUE;
        for (String question : questList) {
            List<PointValue> values = new ArrayList<PointValue>();
            for (int i=0; i<dates.size(); i++) {
                int curRank = Integer.parseInt(db.getResult(CurrentSetName, question, dates.get(i)));
                if (curRank != -2) {
                    values.add(new PointValue(i, curRank));
                    if (curRank > maxValY) maxValY = curRank;
                }
                Log.d("val", "val " + curRank);
            }
            int color = makeCellColor(questList.indexOf(question));
            colors.put(question, color);
            Line line = new Line(values).setColor(color).setCubic(true).setFilled(true).setHasPoints(false).setStrokeWidth(1);
            lines.add(line);

        }
        List<PointValue> values = new ArrayList<PointValue>();
        values.add(new PointValue(0, maxValY+1));
        Line line = new Line(values).setColor(Color.TRANSPARENT).setCubic(true).setFilled(false).setHasPoints(false).setStrokeWidth(1);
        lines.add(line);
        LineChartView chart = (LineChartView)findViewById(R.id.chart);
        List<AxisValue> avs = new ArrayList<>();
        for (int i=0; i<=maxValY+3; i++)
            avs.add(new AxisValue(i));
        Axis axisY = new Axis(avs);
        axisY.setHasLines(true);
        axisY.setInside(true);
        axisY.setTextColor(Color.BLACK);
        axisY.setHasSeparationLine(false);
        Axis axisX = new Axis();
        axisX.setHasLines(true);
        axisX.setInside(true);
        axisX.setTextColor(Color.TRANSPARENT);
        LineChartData data = new LineChartData();
        data.setAxisYLeft(axisY);
        data.setAxisXBottom(axisX);
        data.setLines(lines);
        chart.setZoomType(ZoomType.HORIZONTAL_AND_VERTICAL);
        chart.setLineChartData(data);
        if (!dates.isEmpty()) {
            SimpleDateFormat from = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat to = new SimpleDateFormat("dd MMMMMMMMMM yyyy");
            try {
                ((TextView) findViewById(R.id.legendStart)).setText(to.format(from.parse(dates.get(0))));
                ((TextView) findViewById(R.id.legendEnd)).setText(to.format(from.parse(dates.get(dates.size() - 1))));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        // legend
        LinearLayout linearLayout = (LinearLayout)findViewById(R.id.legend);
        for (Map.Entry<String, Integer> entry : colors.entrySet()) {
            View v = LayoutInflater.from(this).inflate(R.layout.legend_entry, linearLayout, false);
            ((TextView)v.findViewById(R.id.txt)).setText(entry.getKey());
            ((ImageView)v.findViewById(R.id.img)).setImageDrawable(TextDrawable.builder()
                    .buildRound("", entry.getValue()));
            linearLayout.addView(v);
        }
    }

    private void openChart(){
        drawNewChart();
        //int[] income = { 2000,2500,2700,3000,2800,3500,3700,3800};
        //int[] expense = {2200, 2700, 2900, 2800, 2600, 3000, 3300, 3400 };

//        List<Integer> rank;
//
//        ArrayList<String> arraySpinner;
//
//        ///*questList = db.getOneSet(db.getQuizName(myId));
//        questList = SelectQuestionsForChart.questPassToChart;
//
//        arraySpinner=(ArrayList<String>) db.getAllDates(CurrentSetName);
//
//        // Creating an  XYSeries for Income
//        List<XYSeries> Series = new ArrayList<XYSeries>();
//        List<XYSeriesRenderer> Renderer = new ArrayList<XYSeriesRenderer>();
//
//        // Creating an  XYSeries for Expense
//        //XYSeries expenseSeries = new XYSeries("Expense");
//        // Adding data to Income and Expense Series
//
//        // Creating a dataset to hold each series
//        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
//
//        //looping through questions
//        for(int q=0;q< questList.size();q++){
//            XYSeries curQuest = new XYSeries(questList.get(q));
//            Log.d("QUIZREC: chart current question ", questList.get(q));
//            Series.add(curQuest);
//
//            //looping through dates
//            for(int i=0;i< arraySpinner.size();i++){
//                ///*int curRank = Integer.parseInt(db.getResult(db.getQuizName(myId), questList.get(q), arraySpinner.get(i)));
//                int curRank = Integer.parseInt(db.getResult(CurrentSetName, questList.get(q), arraySpinner.get(i)));
//                Log.d("QUIZREC: chart current series element ", questList.get(q) + " date: " + arraySpinner.get(i)  + " i:" + i + " rank:" + curRank);
//                if (curRank != -2) {Series.get(q).add(i, curRank);}
//                //expenseSeries.add(i,expense[i]);
//            }
//            //// Adding Series for a single question to the dataset
//            dataset.addSeries(Series.get(q));
//        }
//
//
//        for(int q=0;q< questList.size();q++){
//        // Creating XYSeriesRenderer to customize incomeSeries
//        XYSeriesRenderer curRenderer = new XYSeriesRenderer();
//
//            //curRenderer.setColor(makeCellColor(q, questList));
//            curRenderer.setColor(makeCellColor(q));
//            //curRenderer.setColor(Color.WHITE);
//            curRenderer.setPointStyle(PointStyle.CIRCLE);
//            curRenderer.setFillPoints(false);
//            curRenderer.setPointStrokeWidth(7);
//            curRenderer.setChartValuesTextSize(7);
//            curRenderer.setLineWidth(5);
//            curRenderer.setDisplayChartValues(false);
//            Renderer.add(curRenderer);
//        }
//
//
//
//        // Creating a XYMultipleSeriesRenderer to customize the whole chart
//        XYMultipleSeriesRenderer multiRenderer = new XYMultipleSeriesRenderer();
//        //multiRenderer.setXLabels(0);
//        //multiRenderer.setYLabels(0);
//        //multiRenderer.setXLabelsAngle(45);
//        //multiRenderer.setXLabelsAlign(Paint.Align.LEFT);
//        multiRenderer.setXLabelsColor(Color.BLUE);
//        multiRenderer.setXLabelsPadding(10);
//        multiRenderer.setYLabelsColor(0,Color.BLUE);
//        multiRenderer.setLegendTextSize(12);
//        //multiRenderer.setChartTitle("Quiz: " + (CurrentSetName));
//        //multiRenderer.setBackgroundColor(Color.parseColor("#fff3f3f3"));
//        multiRenderer.setYAxisAlign(Paint.Align.LEFT, 0);
//        multiRenderer.setApplyBackgroundColor(true);
//        multiRenderer.setBackgroundColor(Color.parseColor("#87cefa"));
//        multiRenderer.setMarginsColor(Color.parseColor("#5e90af"));
//        multiRenderer.setMargins(new int[] {25, 25, 25, 25});
//
//        //done - todo Exception here if no results available
//        if (arraySpinner.size() >= 1)
//        {
//        multiRenderer.setXTitle("Dates: " + arraySpinner.get(0)+ " - " + arraySpinner.get(arraySpinner.size()-1));
//        }
//        else {
//            multiRenderer.setXTitle("Dates: " + " ");
//        }
//        multiRenderer.setYTitle("Answer Rank");
//        multiRenderer.setZoomButtonsVisible(true);
//
//
//        //
//        //multiRenderer.setLegendTextSize(20);
//
//        multiRenderer.setAxisTitleTextSize(14);
//        multiRenderer.setAxesColor(Color.BLACK);
//        multiRenderer.setChartTitleTextSize(28);
//        multiRenderer.setLabelsTextSize(14);
//        multiRenderer.setLegendTextSize(14);
//        //multiRenderer.setLegendHeight(5);
//        // for x axis
//        //multiRenderer.setXLabelsAlign(Align.CENTER);
//
//        multiRenderer.setLabelsColor(Color.BLACK);
//
//
//
//
//        //for y axis
//        multiRenderer.setYLabelsAlign(Paint.Align.RIGHT);
//        //multiRenderer.setYAxisMax(ymax);
//
//        // main axis
//
//
//
//        //multiRenderer.setFitLegend(true);
//        //multiRenderer.setZoomRate(0.2f);
//        //multiRenderer.setMargins(new int[] { 70, 50, 50, 30 });
//        //multiRenderer.setBarSpacing(0.2f);
//
//        //
//
//        // probably will not be used
//        for(int i=0;i<arraySpinner.size();i++){
//            //multiRenderer.addXTextLabel(i, arraySpinner.get(i));
//            multiRenderer.addXTextLabel(i, String.valueOf(i));
//        }
//
//        //This method accept maximum number of label that should be displayed on X axis
//        multiRenderer.setXLabels(RESULT_OK);
//        multiRenderer.clearXTextLabels();
//
//        //set end of time scale to 30 days from the be beginning - not to have ugly one straight line
//        // if we have more measurements, DaysToShow should be equal to number of measurements, add this rule
//        //todo distance between nodes in graph
//        int Step=5;
//        int DaysToShow = Step * arraySpinner.size();
//
//        if (debugFlag == 1){
//        Log.d("QUIZREC: DaysToShow ", String.valueOf(DaysToShow));
//        }
//
//        //may be we need to get the difference between the dates
//
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        ParsePosition pos = new ParsePosition(0);
//
//        //done - todo if the answer set is empty, we have an exception 25.02.14
//        Date stringDate;
//        if (arraySpinner.size() > 0){
//        stringDate = dateFormat.parse(arraySpinner.get(0), pos);
//        }
//        else {
//        stringDate = new Date();
//        }
//
//        Calendar cal = Calendar.getInstance();
//        cal.setTime(stringDate);
//        //cal.add(Calendar.DAY_OF_YEAR, DaysToShow);
//        //multiRenderer.setXAxisMax(cal.getTime().getTime());
//        //end set end of time scale
//
//        // Adding incomeRenderer and expenseRenderer to multipleRenderer
//        // Note: The order of adding dataseries to dataset and renderers to multipleRenderer
//        // should be same
//        for(int q=0;q< questList.size();q++){
//        multiRenderer.addSeriesRenderer(Renderer.get(q));
//        }
//
//        // Getting a reference to LinearLayout of the MainActivity Layout
//        LinearLayout chartContainer = (LinearLayout) findViewById(R.id.chart_container);
//
//
//        // Creating a Line Chart
//        mChart = ChartFactory.getLineChartView(getBaseContext(), dataset, multiRenderer);
//        mChart.setBackgroundColor(Color.parseColor("#fff3f3f3"));
//
//        // Adding the Line Chart to the LinearLayout
//        chartContainer.addView(mChart);
    }

    public int makeCellColor(Integer position, List<String> values) {
        int red =0;//i.e. FF
        int green =255;
        int stepSize=255/((values.size()+1)/2);
        Color myColor = null;


        while(position < (values.size())/2)
        {

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

            if (debugFlag == 1){
            Log.d("QUIZREC: color position red  ", String.valueOf(position));
            }

            green -= stepSize;
            position++;

            if (debugFlag == 1){
            Log.d("QUIZREC: color position RGB : position:", String.valueOf(position) + " RED:" + String.valueOf(red) + " GREEN" + String.valueOf(green));
            }
            }

        return Color.rgb(red, green, 0);
    }

    public int makeCellColor(Integer position) {

        if (mod(position,8)==0) {return Color.GREEN;}
        if (mod(position,8)==1) {return Color.BLUE;}
        if (mod(position,8)==2) {return Color.RED;}
        if (mod(position,8)==3) {return Color.YELLOW;}
        if (mod(position,8)==4) {return Color.MAGENTA;}
        if (mod(position,8)==5) {return Color.CYAN;}
        if (mod(position,8)==6) {return Color.BLACK;}
        if (mod(position,8)==7) {return Color.DKGRAY;}
        return Color.BLACK;
    }

    private int mod(int x, int y)
    {
        int result = x % y;
        if (result < 0)
            result += y;
        return result;
    }


    }