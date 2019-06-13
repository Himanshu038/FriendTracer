package com.anbg.sanjeev.friendtracer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.anbg.sanjeev.friendtracer.R;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.auth.api.Auth;

public class MainActivity extends AppCompatActivity {
    Button sign_in_btn;
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    //used to unmute the phone's audio system
    public static AudioManager mAudioManager;

    private static final int LOGIN_PERMISSION = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sign_in_btn = findViewById(R.id.signin);
        sign_in_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(
                        AuthUI.getInstance().createSignInIntentBuilder()
                        .setAllowNewEmailAccounts(true).build(),LOGIN_PERMISSION
                );
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        stopService(new Intent(this, ListenService.class));
    }

    //start the service by clicking the "start" button
    public void onClickStartService(View V)
    {
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
            return;
        }else {
            //used to start the recorder intent this will allow the app to continually listen for the phrase
            Intent i = new Intent(this, ListenService.class);
            startService(i);
        }
    }

    //Stop the started service with this code
    public void onClickStopService(View V)
    {
        //activate the audiomanager in order to control the audio of the system
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        //Stop the running service from here
        stopService(new Intent(this, ListenService.class));
//unmutes any sound that might have been muted in the process of this application
        mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false  );
        mAudioManager.setStreamSolo(AudioManager.STREAM_MUSIC, false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOGIN_PERMISSION){
            startNewActivity(resultCode,data);
        }
    }

    private void startNewActivity(int resultCode, Intent data) {
        if (resultCode == RESULT_OK){
            Intent intent = new Intent(MainActivity.this,OnlineListActivity.class);
            startActivity(intent);
            finish();   
        }else{
            Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show();
        }
    }
}
