package com.ahakim.said.videoredefuser;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class ServerActivity extends AppCompatActivity
{

    private final static int REQUEST_ENABLE_BT = 1;
    private static final String TAG = "DownloadService";
    private static final String OUTPUT_FILE_NAME = "DownloadedVideo.mp4";
    private static final String VIDEO_URL = "https://ia600208.us.archive.org/26/items/ace_200911_04/00130d67f8ea0c43de91d30cdc13386c.mts-mp430-272.mp4";
    private static final float VIDEO_SIZE = 3200f;
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private ProgressBar progressBar;
    private LocalBroadcastManager localBroadcastManager;
    private Thread sThread;
    private Button listenButton;
    private Button downloadButton;

    static String convertStreamToString(java.io.InputStream is)
    {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.server_layout);

        String aDiscoverable = BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE;
        startActivityForResult(new Intent(aDiscoverable), 1);

        listenButton = findViewById(R.id.enable_n_listen_button);
        listenButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {

                if (bluetoothAdapter == null)
                {
                    System.out.println("This device doesn't support bluetooth");
                }

                if (!bluetoothAdapter.isEnabled())
                {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
                AcceptThread cth = new AcceptThread();
                cth.start();
            }
        });

        downloadButton = findViewById(R.id.downloadButton);
        downloadButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Toast.makeText(ServerActivity.this, "Download clicked", Toast.LENGTH_SHORT).show();
                new DownloadVideoThread().execute();
            }
        });
        this.progressBar = findViewById(R.id.downloadProgressBar);
    }

    private byte[] getByte(String path)
    {
        byte[] getBytes = {};
        try
        {
            File file = new File(path);
            getBytes = new byte[(int) file.length()];
            InputStream is = new FileInputStream(file);
            is.read(getBytes);
            is.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return getBytes;
    }

    public void updateProgressBar(int progress)
    {
        Integer intProgress =(int) (100 *(progress/VIDEO_SIZE));
        this.progressBar.setProgress(intProgress);
    }

    private class AcceptThread extends Thread
    {

        private static final String APP_NAME = "BTChat";
        private static final String TAG = "SERVER SOCKET: ";
        private final UUID MY_UUID = UUID.fromString("4c1d12ac-08c9-11eb-adc1-0242ac120002");
        private final BluetoothServerSocket mServerSocket;
        OutputStream mOutStream = null;

        public AcceptThread()
        {
            BluetoothServerSocket tmp = null;
            try
            {
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, MY_UUID);
            }
            catch (IOException e)
            {
                Log.e(TAG, "Socket's listen() method failed", e);
            }
            mServerSocket = tmp;
        }

        public void run()
        {
            BluetoothSocket socket = null;
            while (true)
            {
                try
                {
                    socket = mServerSocket.accept();
                }
                catch (IOException e)
                {
                    Log.e(TAG, "Socket's accept() method failed", e);
                    break;
                }

                if (socket != null)
                {
                    sendBytesTo(socket);
                    try
                    {
                        mServerSocket.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        private void sendBytesTo(BluetoothSocket socket)
        {
            try
            {
                mOutStream = socket.getOutputStream();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            try
            {
                String rootDir = Environment.getExternalStorageDirectory() + File.separator + "Video";
                File rootFile = new File(rootDir);
                rootFile.mkdir();
                byte[] bytes = getByte(Environment.getExternalStorageDirectory() + File.separator + "Video/" + OUTPUT_FILE_NAME);

                try
                {
                    mOutStream.write(bytes);
                    try
                    {
                        sleep(100);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                    mOutStream.close();

                }
                catch (IOException e)
                {
                    Log.e(TAG, "Error occurred when sending data", e);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public class DownloadVideoThread extends AsyncTask<Void, Void, Void>
    {
        String outputFile;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            Toast.makeText(getApplicationContext(), "Begin Downloading ...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(Void... arg0)
        {
            try
            {
                String rootDir = Environment.getExternalStorageDirectory() + "/" + "Video";
                File rootFile = new File(rootDir);
                rootFile.mkdir();
                URL url = new URL(VIDEO_URL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.connect();
                int status = httpURLConnection.getResponseCode();
                if (status != HttpURLConnection.HTTP_OK)
                {
                    Log.e(TAG, "Server returned HTTP " + status + " " + httpURLConnection.getResponseMessage());
                }

                File localFile = new File(rootFile, OUTPUT_FILE_NAME);
                outputFile = "PATH OF LOCAL FILE : " + localFile.getPath();
                if (rootFile.exists())
                {
                    Log.d(TAG, outputFile);
                }
                if (!localFile.exists())
                {
                    localFile.createNewFile();
                }
                FileOutputStream fos = new FileOutputStream(localFile);
                InputStream in = httpURLConnection.getInputStream();
                byte[] buffer = new byte[1024];
                int nbOfPaquetsReceived = 0;
                int len1 = 0;

                try
                {
                    final int[] prog = {0};

                    while ((len1 = in.read(buffer)) > 0)
                    {
                        nbOfPaquetsReceived++;
                        fos.write(buffer, 0, len1);
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N)
                        {
                            prog[0] = nbOfPaquetsReceived;
                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    updateProgressBar(prog[0]);
                                }
                            });
                        }
                    }
                }
                catch (IOException se)
                {
                    Log.d(TAG, "Hmmmmm");
                }
                fos.close();
                in.close();


            }
            catch (IOException e)
            {
                Log.d("Error....", e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            try
            {
                if (outputFile != null)
                {
                    Toast.makeText(getApplicationContext(), "Download Completed", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Download Failed", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Download Failed");

                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.e(TAG, "Download Failed with Exception - " + e.getLocalizedMessage());

            }
            super.onPostExecute(result);
        }
    }
}
