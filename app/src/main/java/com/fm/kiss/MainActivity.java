package com.fm.kiss;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.gauravk.audiovisualizer.visualizer.CircleLineVisualizer;


public class MainActivity extends AppCompatActivity {
    public static final String PREFS_NAME = "Kiss969";
    private static final int PERM_REQ_CODE = 12332;
    private CircleLineVisualizer circleLineVisualizer;
    private BroadcastReceiver broadcastReceiver;
    private LocalBroadcastManager localBroadcastManager;
    private ImageButton startStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startStop = findViewById(R.id.start_stop);

        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int id = intent.getIntExtra("KissAudioSessionId", -1);
                if (id != -1 && checkAudioPermission()) {
                    circleLineVisualizer.setAudioSessionId(id);
                }
                boolean stopped = intent.getBooleanExtra("StreamingStopped", false);
                if (stopped) {
                    startStop.setBackgroundResource(R.drawable.play_rounded);
                } else {
                    startStop.setBackgroundResource(R.drawable.stop_rounded);
                }

            }
        };
        localBroadcastManager.registerReceiver(broadcastReceiver, new IntentFilter("KissAudioSession"));

        if (!isFirstTimeLaunch()) {
            showDialog();
        }
        circleLineVisualizer = findViewById(R.id.circular);


        startStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MusicService.running) {
                    MusicService.running = false;
                    startStop.setBackgroundResource(R.drawable.play_rounded);
                    Intent stopIntent = new Intent(MainActivity.this, MusicService.class);
                    stopIntent.setAction(MusicService.STOP);
                    startService(stopIntent);
                } else {
                    MusicService.running = true;
                    startStop.setBackgroundResource(R.drawable.stop_rounded);
                    Intent startIntent = new Intent(MainActivity.this, MusicService.class);
                    startIntent.setAction(MusicService.PLAY);
                    startService(startIntent);
                }

            }
        });
    }

    private boolean isFirstTimeLaunch() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getBoolean("isFirstTimeLaunch", false);
    }

    private void setAsFirstTimeLaunched() {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putBoolean("isFirstTimeLaunch", true);
        editor.apply();

    }

    private void showDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permission Request for Animation");
        builder.setMessage("This Permission is Required for Animations, But Denying permission will not prevent you from listening to Radio.");
        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                setAsFirstTimeLaunched();
                requestAudioPermission();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERM_REQ_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "You have granted permission!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean checkAudioPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestAudioPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERM_REQ_CODE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (localBroadcastManager != null && broadcastReceiver != null) {
            localBroadcastManager.unregisterReceiver(broadcastReceiver);
        }
        if (circleLineVisualizer != null) {
            circleLineVisualizer.release();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkAudioPermission() && MusicService.kissAudioSessionId != -1) {
            circleLineVisualizer.setAudioSessionId(MusicService.kissAudioSessionId);
        }


        if (MusicService.running && !MusicService.isPaused) {
            startStop.setBackgroundResource(R.drawable.stop_rounded);
        } else {
            startStop.setBackgroundResource(R.drawable.play_rounded);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.settings) {
            requestAudioPermission();
        }
        if (item.getItemId() == R.id.about) {
            startActivity(new Intent(this, AboutActivity.class));
        }


        return super.onOptionsItemSelected(item);
    }
}