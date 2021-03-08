package com.reshuege;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

public class TeacherActivity1 extends Activity {
    private String pref;
    private WebView web;
    private String login;
    private String password;
    private CookieManager manager;

    private void setupAnim(){
        if (Build.VERSION.SDK_INT >= 21) {
            Slide toRight = new Slide();
            toRight.setSlideEdge(Gravity.RIGHT);
            toRight.setDuration(500);

            Slide toLeft = new Slide();
            toLeft.setSlideEdge(Gravity.LEFT);
            toLeft.setDuration(500);

            //когда переходишь на новую
            getWindow().setExitTransition(toLeft);
            getWindow().setEnterTransition(toRight);

            //когда нажимаешь с другого назад и открываешь со старого
            getWindow().setReturnTransition(toRight);
            getWindow().setReenterTransition(toRight);

        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupAnim();
        setContentView(R.layout.activity_teacher);


        Glide.with(this)
                .load(R.drawable.ball)
                .timeout(100)
                .into((ImageView) findViewById(R.id.gif_load));
        web = findViewById(R.id.teacher);
        pref = getIntent().getStringExtra("pref");
        login = getIntent().getStringExtra("login");
        password = getIntent().getStringExtra("password");
        //cont = this;
        new getCookies().execute();

    }

    private class getCookies extends AsyncTask<Void, Void, Map<String, String>> {

        @Override
        protected Map<String, String> doInBackground(Void ...avoid) {
            int count = 0;
            while(count<5) {
                try {
                    org.jsoup.Connection.Response resp = Jsoup
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
                            .method(Connection.Method.POST)
                            .execute();

                    return resp.cookies();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println(e.getMessage());
                    count++;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Map<String, String> stringStringMap) {
            super.onPostExecute(stringStringMap);
            if (stringStringMap == null){
                showMessage("Что-то не так", "Попробуйте зайти снова");
                finish();
            }
            WebSettings settings = web.getSettings();
            settings.setJavaScriptEnabled(true);
            settings.setAppCacheEnabled(true);
            settings.setDomStorageEnabled(true);
            web.getSettings().setLoadWithOverviewMode(false);
            web.getSettings().setUseWideViewPort(false);settings.setUserAgentString("Mozilla/5.0 (Linux; Android 8.0.0; Pixel 2 XL Build/OPD1.170816.004)" +
                    " AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.120 Mobile Safari/537.36");
            settings.setPluginState(WebSettings.PluginState.ON);

            CookieSyncManager syncManager = CookieSyncManager.createInstance(web.getContext());
            manager = CookieManager.getInstance();
            manager.setAcceptCookie(true);
            manager.removeSessionCookie();
            Log.d("syncMan", syncManager.toString());
            web.setWebViewClient(new WebViewClient() {

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        web.loadUrl(request.getUrl().toString(), stringStringMap);
                    }
                    return false;
                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    //Log.d("should", url);
                    web.loadUrl(url, stringStringMap);
                    return false;
                }
                @Override
                public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                    //Log.d("Inter", url);
                    if (url.contains("sdamgia.ru") && !url.contains("yandex.ru"))
                        return super.shouldInterceptRequest(view, url);
                    else {
                        Log.d("restricted", url);
                        return new WebResourceResponse("text/plain", "utf-8",
                                new ByteArrayInputStream("".getBytes()));
                    }
                }

                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                    ImageView gif = findViewById(R.id.gif_load);
                    gif.setVisibility(View.VISIBLE);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    web.loadUrl("javascript:(function() { " +
                            "document.querySelectorAll(\'div.mh\').forEach(e => e.parentNode.removeChild(e));"+
                            "document.querySelectorAll(\'ins\').forEach(e => e.parentNode.removeChild(e));"+
                            "document.querySelectorAll(\'iframe\').forEach(e => e.parentNode.removeChild(e));"+
                            "document.querySelector(\'center > font\').style.display=\'none\';"+
                            //"document.querySelectorAll(\'yatag\').forEach(e => e.parentNode.removeChild(e));"+
                            //"document.querySelectorAll(\'canter > font\').forEach(e => e.parentNode.removeChild(e));"+
                            "document.querySelectorAll(\'div.scb1dae9c\').forEach(e => e.parentNode.removeChild(e));"+
                           /* "document.querySelector(\'div.mh\').style.display=\'none\';" +
                            "document.querySelector(\'ins\').style.display=\'none\';"+
                            "document.querySelector(\'center > font\').style.display=\'none\';"+
                            //"document.querySelector(\'iframe\').style.display=\'none\';"+
                            "document.querySelector(\'div.scb1dae9c\').style.display=\'none\';"+*/
                            "})()");
                    ImageView gif = findViewById(R.id.gif_load);
                    gif.setVisibility(View.GONE);
                }
            });


            for (Map.Entry<String, String> entry: stringStringMap.entrySet()){
                manager.setCookie("https://"+pref+"-ege.sdamgia.ru/teacher",
                        entry.getKey()+"="+entry.getValue()+"; ");
            }
            CookieSyncManager.getInstance().sync();
            web.loadUrl("https://"+pref+"-ege.sdamgia.ru/teacher", stringStringMap);

        }

    }

    @Override
    public void onBackPressed() {
        if (web.canGoBack()){
            web.goBack();
        } else {
            super.onBackPressed();
        }
    }
    public void showMessage(String title, String message) {
        AlertDialog.Builder ad = new AlertDialog.Builder(this);
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