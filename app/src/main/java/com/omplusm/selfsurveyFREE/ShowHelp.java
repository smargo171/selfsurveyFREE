package com.omplusm.selfsurveyFREE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;

/**
 * Created by s3rg on 25/04/14.
 */
public class ShowHelp extends Activity {

    private Context context;

    //
    private Bundle savedInstanceState;


    public void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String message = intent.getStringExtra(RunQuiz.EXTRA_MESSAGE);

        this.savedInstanceState = savedInstanceState;
        setContentView(R.layout.show_help);

        context = this;

        WebView webview = (WebView)findViewById(R.id.webView);
        webview.setHorizontalScrollBarEnabled(false);
        webview.loadUrl("file:///android_asset/help.html");


}

}
