package com.parcial.firebase.test;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.Toast;



import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.parcial.firebase.test.Networking.WebService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements BroadCastReceiverInterfaceCaller, View.OnClickListener {

    InterCommunicationsManager interCommunicationsManager=null;
    Activity thisActivity = this;
    String ip = "192.168.0.20";
    Button download;
    private final String CHANNEL_ID = "personal_notification";
    private final int NOTIFICATION_ID = 001;
    private NotificationManagerCompat notificationManager;
    private int count;
    private Bitmap[] thumbnails;
    private boolean[] thumbnailsselection;
    private String[] arrPath;
    private ImageAdapter imageAdapter;
    ArrayList<String> f = new ArrayList<String>();// list of file paths
    File[] listFile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);
        //Toolbar toolbar = findViewById(R.id.toolbar);
        notificationManager = NotificationManagerCompat.from(this);
        createNotificationChannel();
        //setSupportActionBar(toolbar);
        checkPermissions();
        GridView imagegrid = (GridView) findViewById(R.id.PhoneImageGrid);
        imageAdapter = new ImageAdapter();
        imagegrid.setAdapter((ListAdapter) imageAdapter);
        try {
            getAllImages();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        getFromSdcard();

        //FloatingActionButton fab = findViewById(R.id.fab);
        //fab.setOnClickListener(new View.OnClickListener() {
            //@Override
            //public void onClick(View view) {
              //  Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
            //}
        //});
        interCommunicationsManager=new InterCommunicationsManager(this,this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            GetToken();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void GetToken(){
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("GetToken", "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();

                        // Log and toast
                        String msg = "Token:"+ token;
                        Log.d("GetToken", msg);
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void intentHasBeenReceivedThroughBroadcast(Intent intent) {
        //Toast.makeText(this,"Received",Toast.LENGTH_LONG).show();
        //Toast.makeText(this,intent.getStringExtra("Push"),Toast.LENGTH_LONG).show();
        try{
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(),CHANNEL_ID);
            builder.setSmallIcon(R.drawable.common_full_open_on_phone);
            builder.setContentTitle("Nueva imagen añadida");
            builder.setContentText(intent.getStringExtra("Push"));
            builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
            notificationManagerCompat.notify(NOTIFICATION_ID,builder.build());

        }catch (Exception e){
            System.out.println(e.getMessage());
        }



        try{
            new Download("http://"+ip+":8080/WebServiceP2/webresources/generic/download/"+intent.getStringExtra("url"),ip);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

    }
    public void getFromSdcard()
    {
        File file = new File(
                Environment.getExternalStorageDirectory() + "/"
                        + "parcial2");

        if (file.isDirectory())
        {
            listFile = file.listFiles();


            for (int i = 0; i < listFile.length; i++)
            {

                f.add(listFile[i].getAbsolutePath());

            }
        }
    }
    public void getAllImages() throws IOException, JSONException, ExecutionException, InterruptedException {
        String respuesta = "";
        WebService webService = new WebService();
        respuesta = webService.execute("http://"+ip+":8080/WebServiceP2/webresources/generic/download/all").get();
        JSONArray jsonArray = new JSONArray(respuesta);
        for (int i = 0; i < jsonArray.length(); i++) {
            String name = jsonArray.getString(i);
            try{
                new Download("http://"+ip+":8080/WebServiceP2/webresources/generic/download/"+name,ip);
            }catch (Exception e){
                System.out.println(e.getMessage());
            }
        }
    }
    public class ImageAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        public ImageAdapter() {
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            return f.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(
                        R.layout.gallerylayout, null);
                holder.imageview = (ImageView) convertView.findViewById(R.id.thumbImage);

                convertView.setTag(holder);
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }


            Bitmap myBitmap = BitmapFactory.decodeFile(f.get(position));
            holder.imageview.setImageBitmap(myBitmap);
            return convertView;
        }
    }
    class ViewHolder {
        ImageView imageview;


    }
    public void checkPermissions(){
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(
                    "We need access to the internal storage and other permissions, please grant all the permissions...");
            builder.setTitle("Permissions granting");
            builder.setPositiveButton("acept",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(thisActivity,
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET}, 1227);
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
            return;
        }
    }

    public void createNotificationChannel(){
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
            NotificationChannel channel1 = new NotificationChannel(CHANNEL_ID,"Channel1",NotificationManager.IMPORTANCE_HIGH);
            channel1.setDescription("channel1");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
        }

    }

    @Override
    public void onClick(View v) {

    }
}
