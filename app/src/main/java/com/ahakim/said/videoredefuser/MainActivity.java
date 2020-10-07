package com.ahakim.said.videoredefuser;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        else
        {
            Log.d("MAIN_ACTIVITY", "WRITE PERMISSION ALREADY GRANTED");
        }
    }

    public void goToServerActivity(View view)
    {
        Intent intent = new Intent(this, ServerActivity.class);
        startActivity(intent);
    }

    public void goToClientActivity(View view)
    {
        Intent intent = new Intent(this, BluetoothConnectionActivity.class);
        startActivity(intent);
    }
}
