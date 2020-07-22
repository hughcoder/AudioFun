/////////////////////////////////////////////////////////////////////////////
///
/// Example Android Application/Activity that allows processing WAV 
/// audio files with SoundTouch library
///
/// Copyright (c) Olli Parviainen
///
////////////////////////////////////////////////////////////////////////////////

package com.hugh.sound;

import java.io.File;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hugh.sound.util.AudioPlayerHelper;
import com.hugh.sound.util.ContentUtil;

import androidx.annotation.Nullable;

public class ExampleActivity extends Activity implements OnClickListener {
    TextView textViewConsole = null;
    EditText editSourceFile = null;
    EditText editOutputFile = null;
    EditText editTempo = null;
    EditText editPitch = null;
    CheckBox checkBoxPlay = null;
    private Button mBtnChoose;

    StringBuilder consoleText = new StringBuilder();
    private AudioPlayerHelper mAudioPlayerHelper;
    private static final String TAG = "aaa";


    /// Called when the activity is created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example);
        mAudioPlayerHelper = new AudioPlayerHelper();

        textViewConsole = (TextView) findViewById(R.id.textViewResult);
        editSourceFile = (EditText) findViewById(R.id.editTextSrcFileName);
        editOutputFile = (EditText) findViewById(R.id.editTextOutFileName);

        editTempo = (EditText) findViewById(R.id.editTextTempo);
        editPitch = (EditText) findViewById(R.id.editTextPitch);

        Button buttonFileSrc = (Button) findViewById(R.id.buttonSelectSrcFile);
        Button buttonFileOutput = (Button) findViewById(R.id.buttonSelectOutFile);
        Button buttonProcess = (Button) findViewById(R.id.buttonProcess);
        mBtnChoose = findViewById(R.id.btn_choose_file);
        buttonFileSrc.setOnClickListener(this);
        buttonFileOutput.setOnClickListener(this);
        buttonProcess.setOnClickListener(this);

        checkBoxPlay = (CheckBox) findViewById(R.id.checkBoxPlay);
        checkBoxPlay.setChecked(true);
        mBtnChoose.setOnClickListener(this);

        // Check soundtouch library presence & version
        checkLibVersion();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && data.getData() != null) {
            String filePath = ContentUtil.getPath(this, data.getData());
            Log.i(TAG, "filePath=" + filePath);
            onSelectedFile(filePath);
        }
    }

    protected void selectFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        this.startActivityForResult(intent, 123);
    }


    void onSelectedFile(String filePath) {
        editSourceFile.setText(filePath);
    }

    /// Function to append status text onto "console box" on the Activity
    public void appendToConsole(final String text) {
        // run on UI thread to avoid conflicts
        runOnUiThread(new Runnable() {
            public void run() {
                consoleText.append(text);
                consoleText.append("\n");
                textViewConsole.setText(consoleText);
            }
        });
    }


    /// print SoundTouch native library version onto console
    protected void checkLibVersion() {
        String ver = SoundTouch.getVersionString();
        appendToConsole("SoundTouch native library version = " + ver);
    }


    /// Button click handler
    @Override
    public void onClick(View arg0) {
        int id = arg0.getId();
        if (id == R.id.buttonSelectSrcFile || id == R.id.buttonSelectOutFile) {// one of the file select buttons clicked ... we've not just implemented them ;-)
            Toast.makeText(this, "File selector not implemented, sorry! Enter the file path manually ;-)", Toast.LENGTH_LONG).show();
        } else if (id == R.id.buttonProcess) {// button "process" pushed
            process();
        } else if (id == R.id.btn_choose_file) {
            selectFile();
        }

    }


    /// Play audio file
    protected void playWavFile(String fileName) {
//        File file2play = new File(fileName);
//        Intent i = new Intent();
//        i.setAction(Intent.ACTION_VIEW);
//        i.setDataAndType(Uri.fromFile(file2play), "audio/wav");
//        startActivity(i);
        mAudioPlayerHelper.playSameLocalAudio(editOutputFile.getText().toString());
    }


    /// Helper class that will execute the SoundTouch processing. As the processing may take
    /// some time, run it in background thread to avoid hanging of the UI.
    protected class ProcessTask extends AsyncTask<ProcessTask.Parameters, Integer, Long> {
        /// Helper class to store the SoundTouch file processing parameters
        public final class Parameters {
            String inFileName;
            String outFileName;
            float tempo;
            float pitch;
        }


        /// Function that does the SoundTouch processing
        public final long doSoundTouchProcessing(Parameters params) {

            SoundTouch st = new SoundTouch();
            st.setTempo(params.tempo);
            st.setPitchSemiTones(params.pitch);
            Log.i("SoundTouch", "process file " + params.inFileName);
            long startTime = System.currentTimeMillis();
            int res = st.processFile(params.inFileName, params.outFileName);
            long endTime = System.currentTimeMillis();
            float duration = (endTime - startTime) * 0.001f;

            Log.i("SoundTouch", "process file done, duration = " + duration);
            appendToConsole("Processing done, duration " + duration + " sec.");
            if (res != 0) {
                String err = SoundTouch.getErrorString();
                appendToConsole("Failure: " + err);
                return -1L;
            }

            // Play file if so is desirable
            if (checkBoxPlay.isChecked()) {
                playWavFile(params.outFileName);
            }
            return 0L;
        }


        /// Overloaded function that get called by the system to perform the background processing
        @Override
        protected Long doInBackground(Parameters... aparams) {
            return doSoundTouchProcessing(aparams[0]);
        }

    }


    /// process a file with SoundTouch. Do the processing using a background processing
    /// task to avoid hanging of the UI
    protected void process() {
        try {
            ProcessTask task = new ProcessTask();
            ProcessTask.Parameters params = task.new Parameters();
            // parse processing parameters
            params.inFileName = editSourceFile.getText().toString();
            params.outFileName = editOutputFile.getText().toString();
            params.tempo = 0.01f * Float.parseFloat(editTempo.getText().toString());
            params.pitch = Float.parseFloat(editPitch.getText().toString());

            // update UI about status
            appendToConsole("Process audio file :" + params.inFileName + " => " + params.outFileName);
            appendToConsole("Tempo = " + params.tempo);
            appendToConsole("Pitch adjust = " + params.pitch);

            Toast.makeText(this, "Starting to process file " + params.inFileName + "...", Toast.LENGTH_SHORT).show();

            // start SoundTouch processing in a background thread
            task.execute(params);
//			task.doSoundTouchProcessing(params);	// this would run processing in main thread

        } catch (Exception exp) {
            exp.printStackTrace();
        }

    }
}