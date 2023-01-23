package com.harry9425.mpmc;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Image;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity  implements SensorEventListener {

    ImageView dir;
    ImageButton lock;
    DatabaseReference databaseReference;
    WebView wv;
    boolean lockval=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        wv=(WebView) findViewById(R.id.web_check);
        wv.getSettings().setJavaScriptEnabled(true);
        lock=(ImageButton) findViewById(R.id.lockbtn);
        lock.setImageResource(R.drawable.ic_baseline_lock_24);
        lockval=true;
        lock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lockval==false){
                    lockval=true;
                    lock.setImageResource(R.drawable.ic_baseline_lock_24);
                }
                else {
                    lockval=false;
                    lock.setImageResource(R.drawable.ic_baseline_lock_open_24);
                }
            }
        });
        wv.loadUrl("http://192.168.43.114");
        dir=(ImageView) findViewById(R.id.dir_check);
        databaseReference= FirebaseDatabase.getInstance().getReference();
        SensorManager sensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);
        if(sensorManager!=null) {
            Sensor acc=sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if(acc!=null){
                sensorManager.registerListener(this,acc,SensorManager.SENSOR_DELAY_GAME);
            }
        }
        else{
            Toast.makeText(this,"sensor not found",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int accuracy = event.accuracy;

        if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER && lockval==false) {
               if (event.values[2] > 6) {
                   if (event.values[0] > 2) {
                       databaseReference.child("move dir").setValue(-1);
                       dir.setImageResource(R.drawable.ic_baseline_arrow_circle_left_24);
                   } else if (event.values[0] < -2) {
                       databaseReference.child("move dir").setValue(1);
                       dir.setImageResource(R.drawable.ic_baseline_arrow_circle_right_24);
                   } else if (event.values[1] < -2) {
                       databaseReference.child("move dir").setValue(-2);
                       dir.setImageResource(R.drawable.ic_baseline_arrow_circle_up_24);
                   } else if (event.values[1] > 2) {
                       databaseReference.child("move dir").setValue(2);
                       dir.setImageResource(R.drawable.ic_baseline_arrow_circle_down_24);
                   } else {
                       databaseReference.child("move dir").setValue(0);
                       dir.setImageResource(R.drawable.ic_baseline_swap_horizontal_circle_24);
                   }
               }
               else {
                      if(event.values[1]>-8 && event.values[1]<8 && event.values[0]>-8 && event.values[0]<8) {
                        //  dir.setImageResource(R.drawable.ic_baseline_arrow_circle_left_24);
                          dir.setImageResource(R.drawable.ic_baseline_swap_horizontal_circle_24);
                      }
               }
           }
        else if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER ){
            databaseReference.child("move dir").setValue(0);
            dir.setImageResource(R.drawable.ic_baseline_swap_horizontal_circle_24);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void  callbox(View v){
        lockval=true;
        lock.setImageResource(R.drawable.ic_baseline_lock_24);
        databaseReference.child("move dir").setValue(0);
        dir.setImageResource(R.drawable.ic_baseline_swap_horizontal_circle_24);
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        View view=getLayoutInflater().inflate(R.layout.sendmessage,null);
        builder.setView(view);
        AlertDialog alertDialog =builder.create();
        EditText editText=view.findViewById(R.id.chatmessage);
        ImageButton send=view.findViewById(R.id.chatsendbtn);
        ImageButton close=view.findViewById(R.id.closebtn);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.hide();
                alertDialog.cancel();
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s =editText.getText().toString();
                if(s.isEmpty()){
                    Toast.makeText(MainActivity.this,"Empty...",Toast.LENGTH_LONG).show();
                }
                else {
                    databaseReference.child("message").setValue(s).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(MainActivity.this,"Message sent....",Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
        alertDialog.show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        databaseReference.child("move dir").setValue(0);
        databaseReference.child("message").setValue("@@##").addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

            }
        });
    }
}