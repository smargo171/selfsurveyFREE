package com.omplusm.selfsurveyFREE;

public class Quest {
	//private variables
    int _id;
    String _set;
    String _question;
    String _answer;
    String _status;
    int _rank;
     
    // Empty constructor
    public Quest(){
         
    }
    // constructor to be used to re-create object from db, when id is available ?
    public Quest(int id, String set, String question, String answer, String status, int rank){
        this._id = id;
        this._set = set;
        this._question = question;
        this._answer = answer;
        this._status= status;
        this._rank = rank;
    }
     
    // constructor to be used to create object without id and with rank
    public Quest(String set, String question, String answer, String status,int rank){
        this._set = set;
        this._question = question;
        this._answer = answer;
        this._status= status;
        this._rank = rank;


        
    }
    // getting ID
    public int getID(){
        return this._id;
    }
     
    // setting id
    public void setID(int id){
        this._id = id;
    }
     
    // getting set of questions - quiz
    public String getSet(){
        return this._set;
    }
     
    // setting set
    public void setSet(String set){
        this._set = set;
    }
     
    // getting question
    public String getQuestion(){
        return this._question;
    }
     
    // setting question
    public void setRank(int rank){
        this._rank = rank;
    }

    // getting rank
    public int getRank(){
        return this._rank;
    }

    // setting question
    public void setQuestion(String question){
        this._question = question;
    }
    
 // getting answer
    public String getAnswer(){
        return this._answer;
    }
     
    // setting answer
    public void setAnswer(String answer){
        this._answer = answer;
    }
    
 // getting question
    public String getStatus(){
        return this._status;
    }
     
    // setting question
    public void setStatus(String status){
        this._status = status;
    }
}
