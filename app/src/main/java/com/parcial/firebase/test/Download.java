package com.parcial.firebase.test;

import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Download {
    private String downloadUrl = "", downloadFileName = "";

    public Download(String downloadUrl, String ip) {
        this.downloadUrl = downloadUrl;
        downloadFileName = downloadUrl.replace("http://"+ip+":8080/WebServiceP2/webresources/generic/download/","");
        new dowloadingTask().execute();
    }

    private class dowloadingTask extends AsyncTask<Void,Void,Void>{
        File apkStorage = null;
        File outputFile = null;

        @Override
        protected Void doInBackground(Void... voids) {
            try{
                URL url = new URL(downloadUrl);
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("GET");
                c.connect();
                if (c.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    System.out.println("error");
                }
                if(new CheckForSDCard().isSDCardPresent()){
                    apkStorage = new File(
                            Environment.getExternalStorageDirectory() + "/"
                                    + "parcial2");
                }else{
                    System.out.println("no hay sd card");
                }
                if (!apkStorage.exists()) {
                    apkStorage.mkdir();
                    System.out.println("directorio creado");
                }
                outputFile = new File(apkStorage, downloadFileName);
                if (!outputFile.exists()) {
                    outputFile.createNewFile();
                    System.out.println("archivo creado");
                }
                FileOutputStream fos = new FileOutputStream(outputFile);
                InputStream is = c.getInputStream();//Get InputStream for connection

                byte[] buffer = new byte[1024];
                int len1 = 0;
                while ((len1 = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len1);
                }
                fos.close();
                is.close();
            }catch (Exception e){
                e.printStackTrace();
                outputFile = null;
                System.out.println(e.getMessage());
            }
            return null;
        }
    }
}
