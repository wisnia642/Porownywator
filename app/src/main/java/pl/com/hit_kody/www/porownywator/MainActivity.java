package pl.com.hit_kody.www.porownywator;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ToneGenerator;
import android.os.Environment;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity {

    public Button button1,button2;
    public EditText editText1;
    public TextView textView;
    public String kod_1,kod_2,kody_sql;
    public Vibrator v;
    public String dane,data;
    public boolean status=false;
    ToneGenerator toneG;

    Calendar c = Calendar.getInstance();
    SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss");

    private static final String SAMPLE_DB_NAME = "Baza";

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(),
                message,
                Toast.LENGTH_LONG).show();
    }

    //create database
    private void ToDataBaseSqllight() {

        try {
            SQLiteDatabase sampleDB = this.openOrCreateDatabase(SAMPLE_DB_NAME, MODE_PRIVATE, null);
            sampleDB.execSQL("CREATE TABLE IF NOT EXISTS kody (id VARCHAR,data_godz VARCHAR,kody VARCHAR);");
            Log.i("test","good read");
        } catch (Exception e) {
            Log.i("test","Error database"+e);
        }
    }

    public void get_data() {

        SQLiteDatabase sampleDB = this.openOrCreateDatabase(SAMPLE_DB_NAME, MODE_PRIVATE, null);

        try {
            Cursor c = sampleDB.rawQuery("select * from kody where kody = '"+kod_2+"' ", null);

            while (c.moveToNext()) {
                String zm = String.valueOf(c.getString(1));
                if (zm != null) {
                    kody_sql = String.valueOf(c.getString(2));
                }
            }
            sampleDB.close();
        } catch (Exception e) {
            Log.i("test","sometching wrong" +e);
        }

    }

    public void send_data()
    {
        data = df.format(c.getTime());
        try {
            SQLiteDatabase sampleDB1 = this.openOrCreateDatabase(SAMPLE_DB_NAME, MODE_PRIVATE, null);
            sampleDB1.execSQL("INSERT INTO kody (data_godz,kody) VALUES ('"+data+"','"+kod_2+"')");
            sampleDB1.close();
        } catch (Exception f) {
            Log.i("test","blad"+f);
        }

    }

    public void delete_data()
    {

        try {
            SQLiteDatabase sampleDB1 = this.openOrCreateDatabase(SAMPLE_DB_NAME, MODE_PRIVATE, null);
            sampleDB1.execSQL("DELETE FROM kody");
            sampleDB1.close();
        } catch (Exception f) {
            Log.i("test","blad"+f);
        }

    }

    public void sprawdzanie()
    {

        //function to comare code
        kod_1 = textView.getText().toString();
        kod_2 = editText1.getText().toString();

        //   Log.i("test",kod_1+"   "+kod_2);

        if(kod_1.equals("")) {

            if (!kod_2.equals("")) {

                textView.setText(kod_2);
                editText1.setText("");
            }

        }else if (!kod_2.equals("")){

            //add code to dynamic tab sql
             send_data();

            if (kod_1.equals(kod_2)) {
                //pozytywny odczyt
               // Log.i("test", "pozytywny odczyt");

                //get data from database
                get_data();

                if(kod_2.equals(kody_sql)) {

                    // the same code
                    Log.i("test","Jest");
                    toneG.startTone(ToneGenerator.TONE_CDMA_CONFIRM, 200);
                    toneG.startTone(ToneGenerator.TONE_CDMA_CONFIRM, 200);
                }
                else{
                    //not the same code
                    Log.i("test","nie jest");
                    toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
                }

            } else {
                //negatywny odczyt
               // Log.i("test", "negatywny odczyt");

                toneG.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 600);

                //wibrate
                v.vibrate(600);

            }
            //write data to file
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss");
            data = df.format(c.getTime());
            dane = String.valueOf(data)+"   /   "+kod_1 +"    /   "+ kod_2;
            writeToFile(dane);

            //clean column
            textView.setText(kod_2);
            editText1.setText("");


        }

    }


    //class to save data in file
    public void writeToFile(String dane)
    {
        //save data in download file
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

      //  if(path.exists()){

            try {

                File file = new File(path, "logi.txt");

                FileOutputStream fileOutputStream = new FileOutputStream(file,true);

                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);

                outputStreamWriter.append(dane +"\n");
              //  outputStreamWriter.close();

             //   Log.i("test", "Data Saved");
            } catch (Exception e) {
                Log.e("test", "Could not write file " + e.getMessage());
            }
        }
      //  else {
       //     Toast.makeText(MainActivity.this, "brak plik", Toast.LENGTH_LONG).show();
     //   }

   // }

    // Storage Permissions to save data on device
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * Checks if the app has permission to write to device storage
     * <p>
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //crate table if not exist
        ToDataBaseSqllight();

        //definicja layout
        button1 = (Button) findViewById(R.id.button);
        button2 = (Button) findViewById(R.id.button2);

        editText1 = (EditText) findViewById(R.id.editText);
        editText1.setInputType(0);

        textView = (TextView) findViewById(R.id.textView);

        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        verifyStoragePermissions(MainActivity.this);

        //sound
        toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);


        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //set coursor in edittext
                editText1.setSelection(0);

                //cean array tab
                delete_data();

                //clean textview
                textView.setText("");
                editText1.setText("");

                showToast("Baza została wyczyszczona");
            }
        });


        editText1.post(new Runnable() {
            @Override
            public void run() {
                editText1.setSelection(0);
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               sprawdzanie();

            }
        });
/*

        editText1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(-1 != editText1.getText().toString().indexOf(".\n"))
                {
                    //If user press enter
                    sprawdzanie();
                    editText1.setSelection(0);
                }
                editText1.setSelection(0);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

*/

                //keypress enter run

                editText1.setOnEditorActionListener(new TextView.OnEditorActionListener() {

                    @Override
                    public boolean onEditorAction(TextView v, int actionId,
                                                  KeyEvent event) {
                        if (event != null&& (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                            InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                            sprawdzanie();

                            return true;

                        }
                        return false;
                    }
                });


    }
}
