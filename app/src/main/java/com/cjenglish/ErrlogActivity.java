package com.cjenglish;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ErrlogActivity extends AppCompatActivity {

    @Bind(R.id.editText)
    EditText editText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_errlog);
        ButterKnife.bind(this);
        File file = new File(getCacheDir(), "errlog.log");

        if(!file.exists())
            return;

        StringBuilder sb=new StringBuilder();

        BufferedReader reader=null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line=null;

            while( (line=reader.readLine())!=null){
                sb.append(line);
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            if(reader!=null){
                try {
                    reader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

        editText.setText(sb.toString());

    }
}
