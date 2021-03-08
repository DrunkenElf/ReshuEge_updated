package com.reshuege.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.reshuege.Adapters.TeacherMenuAdapter;
import com.reshuege.R;
import com.reshuege.Session.Settings;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TeacherFragment extends Fragment {
    final private int[] imgs = {

    };
    final static private String[] hrefs = {
            "/test_editor",
            "?a=tests",
            "/pupil_stats",
            "?a=users",
            "?a=journal",
            "/own_problems",
            "/course"
    };

    public static String href;
    public static boolean logged = false;
    public static Map<String, String> cookies;

    public void setArgs(String href) {
        this.href = href;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            href = savedInstanceState.getString("href");
            logged = savedInstanceState.getBoolean("logged", false);
            cookies = listToMap(savedInstanceState.getStringArrayList("keys"),
                    savedInstanceState.getStringArrayList("values"));

        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("href", href);
        outState.putBoolean("logged", logged);
        outState.putStringArrayList("keys", new ArrayList<>(cookies.keySet()));
        outState.putStringArrayList("values", new ArrayList<>(cookies.values()));
    }

    private Map<String, String> listToMap(ArrayList<String> keys, ArrayList<String> values) {
        Map<String, String> map = new HashMap<>();
        if (keys != null && values != null) {
            Iterator i = keys.iterator();
            Iterator j = values.iterator();
            while (i.hasNext() || j.hasNext()) map.put(i.next().toString(), j.next().toString());
        }
        return map;
    }



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_teacher, container, false);
        setRetainInstance(true);
        ListView lv = root.findViewById(R.id.menu_list);
        href = TeacherFragmentArgs.fromBundle(getArguments()).getHref();

        String[] names = getResources().getStringArray(R.array.teach_menu_names);

        TeacherMenuAdapter adapter = new TeacherMenuAdapter(imgs, names, hrefs, root.getContext(), this, savedInstanceState);
        if (cookies == null && !logged) {
            Log.d("getCookies", "started");
            Settings settings = new Settings();
            new getCookies(root.getContext(), settings.getLogin(root.getContext()),
                    settings.getPassword(root.getContext())).execute();
        }

        lv.setAdapter(adapter);


        return root;
    }

    private class getCookies extends AsyncTask<Void, Void, Map<String, String>> {
        private Context context;
        private String login;
        private String password;

        getCookies(Context context, String login, String password) {
            this.context = context;
            this.login = login;
            this.password = password;
        }

        @Override
        protected Map<String, String> doInBackground(Void... avoid) {

            org.jsoup.Connection.Response resp = null;

            try {
                resp = Jsoup
                        .connect("https://ege.sdamgia.ru/")
                        .userAgent("Mozilla/5.0 (Linux; Android 8.0.0; Pixel 2 XL Build/OPD1.170816.004)" +
                                " AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.120 Mobile Safari/537.36")
                        .data("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                        .data("accept-encoding", "gzip, deflate, br")
                        .data("accept-language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7")
                        .data("cache-control", "max-age=0")
                        .data("content-length", "54")
                        .data("content-type", "application/x-www-form-urlencoded")
                        .data("upgrade-insecure-requests", "1")
                        .data("user", login)
                        .data("password", password)
                        .data("la", "login")
                        .ignoreContentType(true)
                        .ignoreHttpErrors(true)
                        .method(Connection.Method.POST)
                        .execute();

            } catch (IOException e) {
                e.printStackTrace();
            }

            if (resp != null) {
                Log.d("CODE", "" + resp.statusCode());
                Log.d("CODE", resp.statusMessage());
            }
            if (resp != null)
                return resp.cookies();
            return null;
        }

        @Override
        protected void onPostExecute(Map<String, String> stringStringMap) {
            super.onPostExecute(stringStringMap);
            if (stringStringMap == null) {
                showMessage("Что-то не так", "Проверьте подключение к сети", context);
            } else {
                cookies = stringStringMap;
                logged = true;
                Toast.makeText(context, "Пользовательские данные получены", Toast.LENGTH_SHORT).show();
            }
        }

    }

    public void showMessage(String title, String message, Context context) {
        AlertDialog.Builder ad = new AlertDialog.Builder(context);
        ad.setTitle(title);
        ad.setMessage(message);

        ad.setNegativeButton("Да", new DialogInterface.OnClickListener() {
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
