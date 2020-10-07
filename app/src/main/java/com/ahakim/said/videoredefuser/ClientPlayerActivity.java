package com.ahakim.said.videoredefuser;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import androidx.appcompat.app.AppCompatActivity;

public class ClientPlayerActivity extends AppCompatActivity
{

    private static final String TAG = "ClientService";
    private static final String OUTPUT_FILE_NAME = "StreamingVideo.mp4";
    private static final UUID MY_UUID = UUID.fromString("4c1d12ac-08c9-11eb-adc1-0242ac120002");
    VideoView videoView;
    private Thread cThread;
    private Button playButton;

    private static void copyInputStreamToFile(InputStream inputStream, File file) throws IOException
    {
        try (FileOutputStream outputStream = new FileOutputStream(file))
        {
            int read;
            byte[] bytes = new byte[1024];

            while ((read = inputStream.read(bytes)) != -1)
            {
                outputStream.write(bytes, 0, read);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pairing);
        BluetoothDevice bd = getIntent().getExtras().getParcelable("SELECTED DEVICE");
        Toast.makeText(ClientPlayerActivity.this, "Connecting to server : "+bd.getName(), Toast.LENGTH_LONG).show();
        cThread = new ConnectThread(bd);
        cThread.start();
        videoView = findViewById(R.id.videoView);
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
        playButton = findViewById(R.id.play_button);
        playButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                playVideo();
            }
        });
    }

    public void playVideo()
    {
        Uri uri = Uri.parse("/storage/emulated/0/Video/"+OUTPUT_FILE_NAME);
        videoView.setVideoURI(uri);
        videoView.start();
    }

    private class ConnectThread extends Thread
    {
        private final BluetoothSocket mSocket;
        private final BluetoothDevice mDevice;
        private InputStream is;

        public ConnectThread(BluetoothDevice device)
        {
            BluetoothSocket mmSocket1;
            mDevice = device;

            try
            {
                    mmSocket1 = mDevice.createRfcommSocketToServiceRecord(MY_UUID);
            }
            catch (IOException e)
            {
                mmSocket1 = null;
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mSocket = mmSocket1;
        }

        public void run()
        {
            BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
            mAdapter.cancelDiscovery();

            try
            {
                mSocket.connect();
            }
            catch (IOException e)
            {
                try
                {
                    mSocket.close();
                }
                catch (IOException closeException)
                {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }
            connected(mSocket);
        }

        public void cancel()
        {
            try
            {
                mSocket.close();
            }
            catch (IOException e)
            {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }


        private void connected(BluetoothSocket socket)
        {
            try
            {
                String rootDir = Environment.getExternalStorageDirectory() + File.separator + "Video";
                File rootFile = new File(rootDir);
                //rootFile.mkdir();


                File localFile = new File(rootFile, OUTPUT_FILE_NAME);
                String output = "PATH OF LOCAL FILE : " + localFile.getPath();
                if (rootFile.exists())
                {
                    Log.d("SERVICE_ACTIVITY", output);
                }
                if (!localFile.exists())
                {
                    localFile.createNewFile();
                }
                else
                {
                    localFile.delete();
                    localFile.createNewFile();
                }
                is = socket.getInputStream();
                copyInputStreamToFile(is, localFile);
            }
            catch (IOException e)
            {
                Log.d("Error....", e.toString());
            }
        }

        void streamVideo()
        {

            String rootDir = Environment.getExternalStorageDirectory() + File.separator + "Video";
            File rootFile = new File(rootDir);
            File localFile = new File(rootFile, OUTPUT_FILE_NAME);
            playVideo();
        }
    }

}
