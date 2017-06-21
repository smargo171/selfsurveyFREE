package com.omplusm.selfsurveyFREE;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {

    public final static int debugFlag = 0;

	// All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 38;
 
    // Database Name
    private static final String DATABASE_NAME = "QuizSet";
 
    // quests table name
    private static final String TABLE_QUIZ_QUESTIONS = "questions";
    private static final String TABLE_QUIZ_NAMES = "set_names";
    private static final String TABLE_RESULTS = "results";
 
    // quests Table Columns names
    private static final String KEY_ID = "_id";
    private static final String KEY_SET = "set_name";
    private static final String KEY_QUESTION = "question";
    private static final String KEY_ANSWER = "answer";
    private static final String KEY_STATUS = "status";
    private static final String KEY_RANK = "rank";


    // results Table Columns names
    private static final String RESULT_ID = "_id";
    private static final String RESULT_SET = "set_name";
    private static final String RESULT_QUESTION = "question";
    private static final String RESULT_ANSWER = "answer";
    private static final String RESULT_TIME = "time";

    
 
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
 
    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_QUESTIONS_TABLE = "CREATE TABLE " + TABLE_QUIZ_QUESTIONS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," + KEY_SET + " TEXT,"
                + KEY_QUESTION + " TEXT," + KEY_ANSWER + " TEXT," +  KEY_STATUS + " TEXT, " +  KEY_RANK + " INTEGER )";
        db.execSQL(CREATE_QUESTIONS_TABLE);

        if (debugFlag == 1){
        Log.d("QUIZREC: going to create table: ",CREATE_QUESTIONS_TABLE);
        }

        String CREATE_SET_NAMES_TABLE = "CREATE TABLE " + TABLE_QUIZ_NAMES + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," + KEY_SET  + ")";
        db.execSQL(CREATE_SET_NAMES_TABLE);

        if (debugFlag == 1){
        Log.d("QUIZREC: going to create table: ",CREATE_QUESTIONS_TABLE);
        }

        String CREATE_RESULTS_TABLE = "CREATE TABLE " + TABLE_RESULTS + "("
                + RESULT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," + RESULT_SET + " TEXT,"
                + RESULT_QUESTION + " TEXT," + RESULT_ANSWER + " INTEGER," +  RESULT_TIME + " DATETIME )";
        db.execSQL(CREATE_RESULTS_TABLE);

        if (debugFlag == 1){
        Log.d("QUIZREC: going to create table: ",CREATE_RESULTS_TABLE);
        }


    }
 
    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        if (debugFlag == 1){
        Log.d("QUIZREC: going to upgrade: "," ");
        }
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUIZ_QUESTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUIZ_NAMES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RESULTS);
        // Create tables again
        onCreate(db);
    }
    
    

    public boolean questionExists(String set, String quest) {
        SQLiteDatabase db = this.getWritableDatabase();
        String checkQuestExistsQuery = "SELECT  * FROM " + TABLE_QUIZ_QUESTIONS
                + " WHERE set_name = '" + cleanSting(set)
                + "'" + "AND question = '" + cleanSting(quest) + "'";
        Cursor cursor = db.rawQuery(checkQuestExistsQuery, null);
        boolean result = cursor.getCount() != 0;
        cursor.close();
        return result;
    }
	/**
	 * All CRUD(Create, Read, Update, Delete) Operations
	 */

	// Adding new quest
	String addQuest(Quest quest) {

        String addResult;


        //new quests will be dded as empty
        /*
        if ((quest.getSet().equals("")) || (quest.getQuestion().equals("")) ||((quest.getAnswer().equals(""))))
        {
        addResult = "EMPTY";
        return addResult;
        }
        */

        SQLiteDatabase db = this.getWritableDatabase();

        String countSetsQuery = "SELECT  * FROM " + TABLE_QUIZ_NAMES + " WHERE set_name = '" + cleanSting(quest.getSet()) + "'";
        if (debugFlag == 1){
        Log.d("QUIZREC: addquest all set names: ",countSetsQuery);
        }

        Cursor cursor = db.rawQuery(countSetsQuery, null);
        if (debugFlag == 1){
        Log.d("QUIZREC: addquest counter of questions in set " + countSetsQuery + " ", String.valueOf(cursor.getCount()));
        }

        //if there is no such set yet, we create one
        if (cursor.getCount() == 0){
            ContentValues SetValues = new ContentValues();
            SetValues.put(KEY_SET, cleanSting(quest.getSet()));
            db.insert(TABLE_QUIZ_NAMES, null, SetValues);
            if (debugFlag == 1){
            Log.d("QUIZREC: addquest creating set in set names table: ",quest.getSet());
            }
        }


        // we don't add duplicate answers to the same set
        String checkQuestExistsQuery = "SELECT  * FROM " + TABLE_QUIZ_QUESTIONS + " WHERE set_name = '" + cleanSting(quest.getSet()) + "'" + "AND question = '" + cleanSting(quest.getQuestion()) + "' AND answer = '" + cleanSting(quest.getAnswer()) + "'";
        cursor = db.rawQuery(checkQuestExistsQuery, null);



        if (cursor.getCount() == 0){

        //check if rank is default 0 or it was already assigned. If it's default we find the max rank for this question and add 1
        if (quest.getRank() == 0)
        {
            String checkCurMaxRank = "SELECT  MAX(rank) FROM " + TABLE_QUIZ_QUESTIONS + " WHERE set_name = '" + quest.getSet() + "'" + " AND question = '" + quest.getQuestion() + "'";
            int MaxRank;
            if (debugFlag == 1){
            Log.d("QUIZREC: addquest max rank for question query: " ,checkCurMaxRank);
            }

            final SQLiteStatement stmt = db
                    .compileStatement(checkCurMaxRank);

            MaxRank= (int) stmt.simpleQueryForLong();

            if (debugFlag == 1){
            Log.d("QUIZREC: max rank for question value: ", String.valueOf(MaxRank));
            }
            quest.setRank(MaxRank+1);

        }


        //11.11
        //cursor.close();

		ContentValues values = new ContentValues();

        values.put(KEY_SET, cleanSting(quest.getSet())); // add Questions Set
		values.put(KEY_QUESTION, cleanSting(quest.getQuestion())); // add Question in a Set
		values.put(KEY_ANSWER, cleanSting(quest.getAnswer()));
		values.put(KEY_STATUS, cleanSting(quest.getStatus()));
        values.put(KEY_RANK, quest.getRank());

         //todo 11.02.14 THIS SEEMS TO BE RESOLVED bug here, for some reason when I add a question, the last is overwritten and new blank question is created instead
         //11.02.14 but when i add a new question after being at the last question it's added ok, but when i edit it's name I have an exception - tested
            //on the device 12.02.14 fine now this is done
            //emulator works fine
            //why question was added on the first place
            if (debugFlag == 1){
            Log.d("QUIZREC: addquest going to insert, set: ", cleanSting(quest.getSet()) + " Question: " + cleanSting(quest.getQuestion()) + " Answer " + cleanSting(quest.getAnswer()) + " Rank" + quest.getRank() + " Status " + quest.getStatus());
            }

		// Inserting Row
		db.insert(TABLE_QUIZ_QUESTIONS, null, values);

        db.close(); // Closing database connection


            addResult = "OK";

        }
        else {

            if (debugFlag == 1){
            Log.d("QUIZREC: addquest adding answer: duplicate answer will not be added","");
            }
            addResult = "DUPLICATE";

        }

    return addResult;

    }


    public String cleanSting(String BadString) {

    String tmp1 = BadString.replaceAll("[^a-zA-Z0-9?>< \\-]+"," ");
    String tmp2 = tmp1.replaceAll("\\s+", " "); //replace multiple spaces with one
    String tmp3 = tmp2.replaceAll("^\\s", ""); //replace starting space with nothing


    return tmp3; //
    }


    String addResult(String set, String question, int answer_rank, String date) {

        String addResult;


        SQLiteDatabase db = this.getWritableDatabase();


        //if during the same run we had an answer, we deleted it
        String checkQuestExistsQuery = "DELETE FROM " + TABLE_RESULTS + " WHERE set_name = '" + set + "'" + " AND question = '" + question + "' AND time = '" + date + "'";
        Cursor cursor = db.rawQuery(checkQuestExistsQuery, null);
        // we need move to first otherwise delete is not executed
        // http://stackoverflow.com/questions/7211158/why-does-a-delete-rawquery-need-a-movetofirst-in-order-to-actually-delete-the-ro
        if (cursor.moveToFirst()) {
            if (debugFlag == 1){
            Log.d("QUIZREC: ADD result - delete answer hopefully happned: ",cursor.getString(0));
            }

        }

        //11.11
        cursor.close();

        if (debugFlag == 1){
        Log.d("QUIZREC: adding result, date", date);
        }

            ContentValues values = new ContentValues();
            values.put(RESULT_SET, set); // add Questions Set


            values.put(RESULT_QUESTION, question); // add Question in a Set
            values.put(RESULT_ANSWER, answer_rank);

            values.put(RESULT_TIME, date);

            // Inserting Row
            db.insert(TABLE_RESULTS, null, values);
            db.close(); // Closing database connection
            addResult = "OK";

        if (debugFlag == 1){
            Log.d("QUIZREC: adding result: ",set + " " + question + " " + String.valueOf(answer_rank));
        }



        return addResult;

    }



	// Getting single quest
	Quest getQuest(int id) {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(TABLE_QUIZ_QUESTIONS, new String[] { KEY_ID,
				KEY_SET, KEY_QUESTION, KEY_ANSWER, KEY_STATUS, KEY_RANK }, KEY_ID + "=?",
				new String[] { String.valueOf(id) }, null, null, null, null);
		if (cursor.moveToFirst()) {
			cursor.moveToFirst(); }

		Quest quest = new Quest(Integer.parseInt(cursor.getString(0)),
				cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4),Integer.parseInt(cursor.getString(5)));
		// return quest
        cursor.close();

        //11.11
        db.close();

		return quest;
	}

    // Getting quiz name from ID
    String getQuizName(int id) {

        //should never be used as we don't know id!!!!
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_QUIZ_NAMES, new String[] { KEY_ID,
                KEY_SET}, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor.moveToFirst()) {
            cursor.moveToFirst(); }

        String QuizName = cursor.getString(1);

        cursor.close();

        //11.11
        db.close();

        return QuizName;
    }
	
	// Getting All quests
    public List<Quest> getAllQuests() {
        List<Quest> questList = new ArrayList<Quest>();
        // Select All Query
        String selectQuery = "SELECT  DISTINCT * FROM " + TABLE_QUIZ_QUESTIONS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Quest quest = new Quest();
                quest.setID(Integer.parseInt(cursor.getString(0)));
                quest.setSet(cursor.getString(1));
                quest.setQuestion(cursor.getString(2));
                quest.setAnswer(cursor.getString(3));
                quest.setStatus(cursor.getString(4));
                quest.setRank(Integer.parseInt(cursor.getString(5)));
                // Adding quest to list
                questList.add(quest);
            } while (cursor.moveToNext());
        }

        // return quest list

        //11.11 otherwise i seem to have messages about leaking
        cursor.close();
        db.close();

        return questList;
    }

    // Getting All quests
    public List<String> getAllSets() {
        List<String> setsList = new ArrayList<String>();
        // Select All Query
        String selectQuery = "SELECT DISTINCT " + KEY_SET + " FROM " + TABLE_QUIZ_NAMES;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                // Adding quest to list
                setsList.add((cursor.getString(0)));
            } while (cursor.moveToNext());
        }

        //todo test
        //11.11
        cursor.close();
        db.close();

        // return quest list
        return setsList;
    }

    // Getting All quests
    public List<String> getAllDates(String setName) {
        List<String> setsList = new ArrayList<String>();
        // Select All Query
        String selectQuery = "SELECT DISTINCT " + RESULT_TIME + " FROM " + TABLE_RESULTS + " WHERE " + RESULT_SET + " = '" + setName + "'";

        if (debugFlag == 1){
        Log.d("QUIZREC: getAllDates query: " ,selectQuery);
        }

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                // Adding quest to list
                setsList.add((cursor.getString(0)));
            } while (cursor.moveToNext());
        }

        //11.11
        cursor.close();
        db.close();

        // return quest list
        return setsList;
    }

    // Getting date of the set last run
    public String getLastDates(String setName) {
        String LastDate = "";
        // Select All Query
        String selectQuery = "SELECT MAX (" + RESULT_TIME + ") FROM " + TABLE_RESULTS + " WHERE " + RESULT_SET + " = '" + setName + "'";

        if (debugFlag == 1){
        Log.d("QUIZREC: getAllDates query: " ,selectQuery);
        }

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                // Adding quest to list
                LastDate=(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        if (LastDate == null) {LastDate = "";}

        //11.11
        cursor.close();
        db.close();

        // return quest list
        return LastDate;
    }

    // Getting number of runs for the set
    public String getNumRuns(String setName) {
        int NumRuns = 0;
        // Select All Query
        String selectQuery = "SELECT COUNT ( DISTINCT " + RESULT_TIME + ") FROM " + TABLE_RESULTS + " WHERE " + RESULT_SET + " = '" + setName + "'";

        if (debugFlag == 1){
        Log.d("QUIZREC: getAllDates query: " ,selectQuery);
        }

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                // Adding quest to list
                NumRuns=(cursor.getInt(0));
            } while (cursor.moveToNext());
        }

        //11.11
        cursor.close();
        db.close();

        // return quest list
        return String.valueOf(NumRuns);
    }

	// Getting Quest(ions) belonging to one single set
		public List<String> getOneSet(String setName) {
			List<String> questList = new ArrayList<String>();
			// Select All Query
			String selectQuery = "SELECT  DISTINCT " + KEY_QUESTION + " FROM " + TABLE_QUIZ_QUESTIONS + " WHERE " + KEY_SET + " ='" + setName + "'";

			SQLiteDatabase db = this.getWritableDatabase();
			Cursor cursor = db.rawQuery(selectQuery, null);

			// looping through all rows and adding to list
			if (cursor.moveToFirst()) {
				do {
					questList.add(cursor.getString(0));
				} while (cursor.moveToNext());
			}

            //11.11
            cursor.close();
            db.close();

			// return quest list
			return questList;
		}

    // Getting Answers belonging to one single set and one question in this set
    public List<String> getAnswersOneSet(String setName, String questName) {
        List<String> answersList = new ArrayList<String>();
        // Select All Query
        //done oder by rank
        String selectQuery = "SELECT " + KEY_ANSWER + " FROM " + TABLE_QUIZ_QUESTIONS + " WHERE " + KEY_SET + " ='" + setName + "'" + " AND " + KEY_QUESTION + " ='" + questName + "'" + "ORDER BY " + KEY_RANK;

        if (debugFlag == 1){
        Log.d("QUIZREC: getAnswersOneSet query: ",selectQuery);
        }

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                // Adding quest to list
                answersList.add(cursor.getString(0));

                if (debugFlag == 1){
                Log.d("QUIZREC: adding answer to the answer list: ",cursor.getString(0));
                }

            } while (cursor.moveToNext());
        }

        // return quest list
        //cursor.close();

        //11.11
        cursor.close();
        db.close();

        return answersList;
    }


    public String getResult (String setName, String questName, String date){
    //public List<String> getAnswersOneSet(String setName, String questName) {
        String answer="";

        String selectQuery = "SELECT " + KEY_ANSWER + " FROM " + TABLE_RESULTS + " WHERE " + RESULT_SET + " ='" + setName + "'" + " AND " + RESULT_QUESTION + " ='" + questName + "'" + " AND " + RESULT_TIME + "= '" + date +"'" + " ORDER BY " + RESULT_ANSWER;

        if (debugFlag == 1){
        Log.d("QUIZREC: getResultsOneSet query: ",selectQuery);
        }

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                // Adding quest to list
                answer= cursor.getString(0);

                if (debugFlag == 1){
                Log.d("QUIZREC: result answer: ",cursor.getString(0));
                }

            } while (cursor.moveToNext());
        }
        else
        {
            //rank which we get here should and which starts with 1 be converted to position which starts with 0
            //therefore we will subtruct 1 from rank to get position
            answer = "-2";
        }

        // return quest list

        //11.11
        cursor.close();
        db.close();

        //cursor.close();

        if (debugFlag == 1){
        Log.d("QUIZREC: getResults result: ",answer);
        }

        return answer;
    }


    // Updating single quest
	public void updateQuest(Quest quest) {
		
		int UpdateRes;
		
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_SET, quest.getSet());
		values.put(KEY_QUESTION, quest.getQuestion());
		values.put(KEY_ANSWER, quest.getAnswer());
		values.put(KEY_STATUS, quest.getStatus());
        values.put(KEY_RANK, quest.getRank());
		
		// updating row
        if (db != null) {
            db.update(TABLE_QUIZ_QUESTIONS, values, KEY_ID + " = ?",
                    new String[] { String.valueOf(quest.getID()) });
        }

        if (debugFlag == 1){
        Log.d("QUIZREC: edit update quest: ", String.valueOf(quest.getID()));
        }

        db.close();
		
	}

	// Deleting single quest
	public void deleteQuest(Quest quest) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_QUIZ_QUESTIONS, KEY_ID + " = ?",
                new String[]{String.valueOf(quest.getID())});

        //not TO DO: take care about deleting empty set from the set names table
        // no, this method id not used


		db.close();
	}

    // Deleting single answer from the selected set and question
    public void deleteAnswer(String setName, String answerName, String questName) {
        ArrayList<String> questArrayList;
        //we need to shift the ranks fo the questions below the deleted
        //first we get the current rank
        String selectRankQuery = "SELECT " +KEY_RANK + " FROM " + TABLE_QUIZ_QUESTIONS + " WHERE " + KEY_SET + " ='" + setName + "'" + " AND " + KEY_ANSWER + " ='" + answerName + "'" + " AND " + KEY_QUESTION + " ='" + questName + "'";

        if (debugFlag == 1){
        Log.d("QUIZREC: delete answer query: ", selectRankQuery);
        }

        String curRank="0";
        int res;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursorRank = db.rawQuery(selectRankQuery, null);

        if (cursorRank.moveToFirst()) {

            if (debugFlag == 1){
            Log.d("QUIZREC: answer rank got: ",String.valueOf(cursorRank.getInt(0)));
            }
            curRank = String.valueOf(cursorRank.getInt(0));
        }

        //db.close();
        //cursorRank.close();


        String selectQuery = "DELETE FROM " + TABLE_QUIZ_QUESTIONS + " WHERE " + KEY_SET + " ='" + setName + "'" + " AND " + KEY_ANSWER + " ='" + answerName + "'" + " AND " + KEY_QUESTION + " ='" + questName + "'";

        if (debugFlag == 1){
        Log.d("QUIZREC: delete answer query: ", selectQuery);
        }

        Cursor cursor = db.rawQuery(selectQuery, null);

        // we need move to first otherwise delete is not executed
        // http://stackoverflow.com/questions/7211158/why-does-a-delete-rawquery-need-a-movetofirst-in-order-to-actually-delete-the-ro
        if (cursor.moveToFirst()) {

            if (debugFlag == 1){
                Log.d("QUIZREC: delete anser hopefully happened: ",cursor.getString(0));
            }

        }

        //11.11
        //cursor.close();

        // we need db to change the ranks of the other answers - see below
        //db.close();

        //now shifting ranks for the answers below
        //todo: need to check if by initailizaing data we increase ranks and that't why i have impossible value e.g. 21
        //may be display rank in the cell - done
        //25.02.14 seems to be done
        questArrayList = (ArrayList<String>) getAnswersOneSet(setName,questName);
        int a=0;
        while (a <= questArrayList.size()-1){
            //get Rank of each answer in a loop

            String selectCurRankQuery = "SELECT " +KEY_RANK + " FROM " + TABLE_QUIZ_QUESTIONS + " WHERE " + KEY_SET + " ='" + setName + "'" + " AND " + KEY_ANSWER + " ='" + questArrayList.get(a) + "'" + " AND " + KEY_QUESTION + " ='" + questName + "'";

            if (debugFlag == 1){
            Log.d("QUIZREC: delete answer query change rank: ", selectCurRankQuery);
            }

            String curCurRank="0";
            SQLiteDatabase db1 = this.getWritableDatabase();
            Cursor cursorCurRank = db1.rawQuery(selectCurRankQuery, null);


            if (cursorCurRank.moveToFirst()) {
                //todo exception here when deleting the last answer. Still bug 25.02.14!!!!

                if (debugFlag == 1){
                Log.d("QUIZREC: answer rank got cursorCurRank: ",String.valueOf(cursorCurRank.getInt(0)));
                Log.d("QUIZREC: answer rank got cursorRank: ",String.valueOf(cursorRank.getInt(0)));
                }

                curCurRank = String.valueOf(cursorCurRank.getInt(0));
            }




            if (cursorCurRank.getInt(0) >cursorRank.getInt(0)){
                String curRankNew;
                curRankNew = String.valueOf(cursorCurRank.getInt(0)-1);
                //res = setRank(setName,questName,answerName,curRankNew);
                res = setRank(setName,questName,questArrayList.get(a),curRankNew);

                if (debugFlag == 1){
                Log.d("QUIZREC: delete answer change rank: answer", questArrayList.get(a) + " new rank " + curRankNew);
                }
            }
            a++;
            //todo: do we really need it?
            //cursorCurRank.deactivate();
        }


        cursor.close();
        cursorRank.close();
        db.close();


    }

    public String rankChangeAnswer(String setName, String answerName, String questName, int up_or_down) {
        String selectQuery = "SELECT " +KEY_RANK + " FROM " + TABLE_QUIZ_QUESTIONS + " WHERE " + KEY_SET + " ='" + setName + "'" + " AND " + KEY_ANSWER + " ='" + answerName + "'" + " AND " + KEY_QUESTION + " ='" + questName + "'";
        if (debugFlag == 1){
        Log.d("QUIZREC: delete answer query: ", selectQuery);
        }
        String curRank="0";
        String curRankNew="0";
        int res;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            if (debugFlag == 1){
            Log.d("QUIZREC: answer rank got: ",String.valueOf(cursor.getInt(0)));
            }
            curRank = String.valueOf(cursor.getInt(0));

            // down is 2 (increase rank)
            if (up_or_down == 2)

            {curRankNew = String.valueOf(cursor.getInt(0)+1);
            res = setRank(setName,questName,answerName,curRankNew);
            }
            else
            {
                if (cursor.getInt(0) > 1){
                curRankNew = String.valueOf(cursor.getInt(0)-1);
                res = setRank(setName,questName,answerName,curRankNew);
                }

            }
        }

        //11.11
        cursor.close();

        db.close();
        return curRank;
    }


    public String rankGetAnswer(String setName, String answerName, String questName) {
        String selectQuery = "SELECT " +KEY_RANK + " FROM " + TABLE_QUIZ_QUESTIONS + " WHERE " + KEY_SET + " ='" + setName + "'" + " AND " + KEY_ANSWER + " ='" + answerName + "'" + " AND " + KEY_QUESTION + " ='" + questName + "'";

        if (debugFlag == 1){
        Log.d("QUIZREC: delete answer query: ", selectQuery);
        }

        String curRank="0";
        String curRankNew="0";
        int res;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {

            if (debugFlag == 1){
            Log.d("QUIZREC: answer rank got: ",String.valueOf(cursor.getInt(0)));
            }

            curRank = String.valueOf(cursor.getInt(0));
        }

        //11.11
        cursor.close();

        db.close();
        return curRank;
    }



    // Deleting single question from the selected set
    public void deleteQuestion(String setName, String questName) {



        String selectQuery = "DELETE FROM " + TABLE_QUIZ_QUESTIONS + " WHERE " + KEY_SET + " ='" + setName + "'" + " AND " + KEY_QUESTION + " ='" + questName + "'";

        if (debugFlag == 1){
        Log.d("QUIZREC: delete question from a set query: ", selectQuery);
        }

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // we need move to first otherwise delete is not executed
        // http://stackoverflow.com/questions/7211158/why-does-a-delete-rawquery-need-a-movetofirst-in-order-to-actually-delete-the-ro
        if (cursor.moveToFirst()) {
            if (debugFlag == 1){
            Log.d("QUIZREC: delete question from a set hopefully happened: ",cursor.getString(0));
            }

        }
        db.close();
    }


    public void deleteQuestSet(String setName) {



        String selectQuery = "DELETE FROM " + TABLE_QUIZ_QUESTIONS + " WHERE " + KEY_SET + " ='" + setName + "'";

        if (debugFlag == 1){
        Log.d("QUIZREC: delete question from a set query: ", selectQuery);
        }

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // we need move to first otherwise delete is not executed
        // http://stackoverflow.com/questions/7211158/why-does-a-delete-rawquery-need-a-movetofirst-in-order-to-actually-delete-the-ro
        if (cursor.moveToFirst()) {

            if (debugFlag == 1){
            Log.d("QUIZREC: delete question from a set hopefully happened: ",cursor.getString(0));
            }

        }
        cursor.close();

        String selectQueryNames = "DELETE FROM " + TABLE_QUIZ_NAMES + " WHERE " + KEY_SET + " ='" + setName + "'";

        if (debugFlag == 1){
        Log.d("QUIZREC: delete question from a set name table query: ", selectQueryNames);
        }

        Cursor cursorNames = db.rawQuery(selectQueryNames, null);

        // we need move to first otherwise delete is not executed
        // http://stackoverflow.com/questions/7211158/why-does-a-delete-rawquery-need-a-movetofirst-in-order-to-actually-delete-the-ro
        if (cursorNames.moveToFirst()) {

            if (debugFlag == 1){
            Log.d("QUIZREC: delete question from a set name table hopefully happened: ",cursorNames.getString(0));
            }

        }
        cursorNames.close();

        db.close();
    }


	// Getting quests Count - total
	public int getQuestsCount() {
		String countQuery = "SELECT  * FROM " + TABLE_QUIZ_QUESTIONS;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		cursor.close();

        //11.11
        db.close();

		// return count
		return cursor.getCount();


	}

    // Getting questions Count in a Set
    public int getQuestsCountInSet(String ColumnName, String ColumnValue) {

        int count;
        //String countQuery = "SELECT  * FROM " + TABLE_QUIZ_QUESTIONS + " WHERE " + ColumnName + "=" + "'"+ColumnValue+"'";
        String countQuery = "SELECT  DISTINCT " + KEY_SET + " , " + KEY_QUESTION + " FROM " + TABLE_QUIZ_QUESTIONS + " WHERE " + ColumnName + "=" + "'"+ColumnValue+"'";

        if (debugFlag == 1){
        Log.d("QUIZREC: all set names: ",countQuery);
        }

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        count = cursor.getCount();

        //11.11
        cursor.close();
        db.close();

        // return count
        //11.11
        //return cursor.getCount();
        return count;

    }

    public int renameSet(String SetNameNew, String SetNameOld) {

        int count;

        String setExists = "SELECT * FROM " + TABLE_QUIZ_QUESTIONS + " WHERE " + KEY_SET + " ='" + SetNameNew + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursorExists = db.rawQuery(setExists, null);
        count = cursorExists.getCount();
        cursorExists.close();

        if (count > 0) {
            count = -1;
            db.close();
            return count;
        }

        else {

        String countQuery = "UPDATE " + TABLE_QUIZ_QUESTIONS + " SET " + KEY_SET + " ='" + SetNameNew + "'" + " WHERE " + KEY_SET + " ='" + SetNameOld + "'";

            if (debugFlag == 1){
        Log.d("QUIZREC: renaming set: ",countQuery);
            }
        Cursor cursor = db.rawQuery(countQuery, null);
        count = cursor.getCount();
        cursor.close();
            if (debugFlag == 1){
        Log.d("QUIZREC: renaming set questions: ", String.valueOf(count) + "questions renamed");
            }

        String countQuerySetTable = "UPDATE " + TABLE_QUIZ_NAMES + " SET " + KEY_SET + " ='" + SetNameNew + "'" + " WHERE " + KEY_SET + " ='" + SetNameOld + "'";
            if (debugFlag == 1){
            Log.d("QUIZREC: renaming set in set table: ",countQuery);
            }
        Cursor cursorSetTable = db.rawQuery(countQuerySetTable, null);
        int countSetTable = cursorSetTable.getCount();
        cursorSetTable.close();

        String cursorRenameResultsQuery = "UPDATE " + TABLE_RESULTS + " SET " + KEY_SET + " ='" + SetNameNew + "'" + " WHERE " + KEY_SET + " ='" + SetNameOld + "'";
            if (debugFlag == 1){
            Log.d("QUIZREC: renaming set in set table: ",cursorRenameResultsQuery);
            }
                Cursor cursorRenameResults = db.rawQuery(cursorRenameResultsQuery, null);
        int countCursorRenameResults = cursorRenameResults.getCount();
        cursorRenameResults.close();
        //11.11






        db.close();

        // return count
        return 1;
        }
    }

    public int renameQuestion(String SetName, String QuestionNameNew, String QuestionNameOld) {

        int count;
        String QuestionNameNewClean = QuestionNameNew.replaceAll("[^a-zA-Z0-9? ]+"," ");

        String countQuery = "UPDATE " + TABLE_QUIZ_QUESTIONS + " SET " + KEY_QUESTION + " ='" + QuestionNameNew + "' " + "WHERE " + KEY_SET + " ='" + SetName + "' AND " + KEY_QUESTION + " ='" + QuestionNameOld + "'";

        if (debugFlag == 1){
        Log.d("QUIZREC: renaming question: ",countQuery);
        }
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        count = cursor.getCount();

        //11.11
        cursor.close();
        db.close();

        // return count
        return count;
    }

    public int setRank(String SetName, String QuestionName, String AnswerName, String newRank) {

        int count;
        String countQuery = "UPDATE " + TABLE_QUIZ_QUESTIONS + " SET " + KEY_RANK + " =" + newRank + " " + "WHERE " + KEY_SET + " ='" + SetName + "' AND " + KEY_QUESTION + " ='" + QuestionName + "' AND " + KEY_ANSWER + " ='" + AnswerName + "'";
        if (debugFlag == 1){
        Log.d("QUIZREC: setting rank: ",countQuery);
        }
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        count  = cursor.getCount();

        //11.11
        cursor.close();
        db.close();
        return count;
    }

    public int renameAnswer(String SetName, String QuestionName, String AnswerNameNew, String AnswerNameOld) {

        //done - exception when I have apostroph : answer ='I don't know   '
        //replace with clean string
        String AnswerNameNewClean = cleanSting(AnswerNameNew);

        String countQuery = "UPDATE " + TABLE_QUIZ_QUESTIONS + " SET " + KEY_ANSWER + " ='" + AnswerNameNewClean + "'" + " WHERE " + KEY_SET + " ='" + SetName + "' AND " + KEY_QUESTION + " ='" + QuestionName + "' AND " + KEY_ANSWER + " ='" + AnswerNameOld + "'";
        int count;

        if (debugFlag == 1){
        Log.d("QUIZREC: edit renaming answer: ",countQuery);
        }
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        count  = cursor.getCount();

        //11.11
        cursor.close();
        db.close();

        return count;
    }

    // Getting 1st question from set
    public String getQuestsWithId(String SetName) {
        String Query = "SELECT * FROM " + TABLE_QUIZ_QUESTIONS + " WHERE " + KEY_SET + " ='" + SetName + "'";
        String FirstQuestion;

        if (debugFlag == 1){
        Log.d("QUIZREC: got question with ID: ",Query);
        }
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(Query, null);

        if (cursor.moveToFirst()) {
            // return Question
            FirstQuestion = cursor.getString(2);
            }
        else {
            FirstQuestion="Shit Happens!!!";

        }

        //11.11
        cursor.close();
        db.close();
        return FirstQuestion;
    }

    public Cursor getAllCursor() {

        //relax, this method is not used - this hopefully is not used, should be avoided passing cursor as I have to take care in one more place when to close it

        if (debugFlag == 1){
        Log.d("QUIZREC: ", "Going to query ");
        }

        String countQuery = "SELECT DISTINCT _id, set_name FROM " + TABLE_QUIZ_QUESTIONS;
        //String countQuery = "SELECT * FROM " + TABLE_QUIZ_QUESTIONS;

        if (debugFlag == 1){
        Log.d("QUIZREC: ", "after select ");
        }
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        //cursor.close();
        return cursor;
    }

    public Cursor getAllQuestResults(String set) {


        if (debugFlag == 1){
        Log.d("QUIZREC: ", "Going to get results of one quest " + set);
        }

        String selectQuery = "SELECT  * FROM " + TABLE_RESULTS + " WHERE set_name = '" + set + "'";

        if (debugFlag == 1){
        Log.d("QUIZREC: ", "after select results");
        }

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        //cursor.close();
        return cursor;
    }

    public Cursor getAllQuestions(String set) {


        if (debugFlag == 1){
        Log.d("QUIZREC: ", "Going to get questions of one quest " + set);
        }
        String selectQuery = "SELECT _id,set_name,question,answer,status,rank FROM " + TABLE_QUIZ_QUESTIONS + " WHERE set_name = '" + set + "'";

        if (debugFlag == 1){
        Log.d("QUIZREC: ", "after select questions");
        }

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        //cursor.close();
        return cursor;
    }

    public Cursor getAllSetNamesCursor() {

        if (debugFlag == 1){
        Log.d("QUIZREC: ", "Going to query ");
        }

        String countQuery = "SELECT DISTINCT _id, set_name FROM " + TABLE_QUIZ_NAMES;

        if (debugFlag == 1){
        Log.d("QUIZREC: ", "after select set names");
        }

        //12.02.14 todo exception when we run for the first time ???
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        //cursor.close();
        return cursor;
    }

    public String calcRunOneResult (String date, String set){

        Integer countResult, curRank, avgPercent;
        float curPercent, totPercent;
        curRank = 0;
        countResult = 1;
        avgPercent = 0;
        totPercent = 0;

        String selectQuery = "SELECT  answer, question FROM " + TABLE_RESULTS + " WHERE time ='" + date + "' AND set_name = '" + set+ "'";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (debugFlag == 1){
        Log.d("QUIZREC: mean query", selectQuery);
        }

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {


                //need to include date, otherwise we have strange results if we had other max number of ranks before and then some answer was deleted
                String checkCurMaxRank = "SELECT  MAX(rank) FROM " + TABLE_QUIZ_QUESTIONS + " WHERE set_name = '" + set + "'" + " AND question = '" + cursor.getString(1) + "'";
                int MaxRank;

                if (debugFlag == 1){
                Log.d("QUIZREC: max rank for question query: " ,checkCurMaxRank);
                }

                final SQLiteStatement stmt = db
                        .compileStatement(checkCurMaxRank);

                MaxRank= (int) stmt.simpleQueryForLong();

                curRank = cursor.getInt(0);

                if (debugFlag == 1){
                Log.d("QUIZREC: max rank for question value: ", String.valueOf(MaxRank) + " curRank " + String.valueOf(curRank) + " question " + cursor.getString(1));
                }

                if (MaxRank != 0){curPercent = ((curRank)*100/MaxRank);}
                else {return "0";//something is wrong
                }

                // always choosing best answer should lead to 100 (and not the upper side of the 1st answer) and always choosing worst should lead to 0
                if (curPercent < 50) {curPercent = curPercent - 100/MaxRank;}
                //if (curPercent > 50) {curPercent = curPercent + 100/MaxRank;}

                totPercent = totPercent + curPercent;
                avgPercent = Math.round((totPercent)/countResult);

                        if (debugFlag == 1){
                Log.d("QUIZREC: max rank curPercent: ", curPercent + " avgPercent " + curPercent + " countResult " + countResult + " totPercent " + totPercent);
                        }

                countResult++;

            } while (cursor.moveToNext());
        }

        //11.11
        cursor.close();
        db.close();

        if (countResult !=0)
        {
    return String.valueOf(100-avgPercent);}
        else {return "No result yet";}


    }

    public String calcRunOneResultMean (String date, String set){

        Integer countResult, curRank, avgPercent;
        float totalValue = 0;
        float avgValue = 0;

        curRank = 0;
        countResult = 1;

        String selectQuery = "SELECT  answer, question FROM " + TABLE_RESULTS + " WHERE time ='" + date + "' AND set_name = '" + set+ "'";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (debugFlag == 1){
            Log.d("QUIZREC: mean query", selectQuery);
        }

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {

                curRank = cursor.getInt(0);

                totalValue = totalValue + curRank;
                avgValue = Math.round((totalValue)/countResult);

                if (debugFlag == 1){
                    Log.d("QUIZREC: avgValue: ", String.valueOf(avgValue) );
                }

                countResult++;

            } while (cursor.moveToNext());
        }

        //11.11
        cursor.close();
        db.close();

        if (countResult !=0)
        {
            return String.valueOf(avgValue-1);}
        else {return "No result yet";}


    }

    
}
