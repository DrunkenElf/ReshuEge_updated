package com.ilnur;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;


public class SaveStatistics extends AsyncTask<String, Integer,JSONArray> {

    ProgressDialog progress;
    Context context;
    String subject_prefix;
    String query;
    boolean serverError = false;


    public SaveStatistics(Context context, String subject_prefix, String query)
    {
        this.context = context;
        this.subject_prefix = subject_prefix;
        this.query = query;
    }

    protected void onPreExecute() {
        progress = new ProgressDialog(context);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        progress.setMessage("Сохранение статистики");

        try {
            progress.show();
            progress.setCancelable(false);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected JSONArray doInBackground(String... strings) {


        ArrayList<Integer> questions = new ArrayList<Integer>();
        Log.d("ANSWERS: ", query);
        try {
            byte[] performedQuery = query.getBytes("UTF-8");
            query = new String(performedQuery, "UTF-8");
            String response = loadAPI("check_answer&", query);

            JSONArray taskNumbers = (new JSONObject(response)).getJSONArray("data");
            return taskNumbers;
        } catch(Exception e) {
            e.printStackTrace();
            Log.d("myLogs", e.toString());
            serverError = true;
        }

        return null;
    }

    protected void onPostExecute(JSONArray results) {
        super.onPostExecute(results);
        if (!serverError) {
            try {

                progress.dismiss();
                if (results.length() == 0) {
                    Toast.makeText(context, "Произошла ошибка, повторите позже", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Статистика успешно сохранена", Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                e.printStackTrace();
                Log.d("myLogs", e.toString());
            }
        } else {
            progress.dismiss();
            showMessage("Сервер РЕШУ ЕГЭ временно недоступен.", context.getString(R.string.error));
        }
    }

    public String loadAPI(String APIRequest, String query) {

        String str = null;
        int tryesCount = 0;
        while (str == null && tryesCount != 10)
            try {
                tryesCount++;
                URL url;
                url = new URL("https://" + subject_prefix + "-ege.sdamgia.ru/api?type=" + APIRequest + query + "&" + Protocol.INSTANCE.getProtocolVersion());//URLEncoder.encode(query, "UTF-8"));
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                Log.d("API SAVESTATS: ", url.toString());
                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuffer sBuffer = new StringBuffer("");
                String line = "";
                while ((line = in.readLine()) != null) {
                    sBuffer.append(line + "\n");

                }

                in.close();
                str = sBuffer.toString();


            }
            catch (Exception e) {
                e.printStackTrace();
                Log.d("myLogs", APIRequest + " " + query + " " + e.toString());
            }

        return str;
    }

    public void showMessage(String title, String message) {

        AlertDialog.Builder ad = new androidx.appcompat.app.AlertDialog.Builder(context);
        ad.setTitle(title);
        ad.setMessage(message);

        ad.setPositiveButton("Продолжить", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {

            }
        });

        ad.setCancelable(true);
        ad.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {

            }
        });

        ad.show();
    }

}
