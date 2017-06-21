package com.omplusm.selfsurveyFREE;

/**
 * Created by s3rg on 26/03/14.
 */
import android.app.Activity;
import android.os.Bundle;

public class SetPreferenceActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        //File sdCardRoot = Environment.getExternalStorageDirectory();
        //String sdCardDirectory = sdCardRoot + "/Android/data/" + getApplicationContext().getPackageName();

        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new UserSettingFragment()).commit();




    }

}
