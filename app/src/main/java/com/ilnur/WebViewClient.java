package com.ilnur;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import android.view.Window;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.util.Map;

public class WebViewClient extends android.webkit.WebViewClient {
    private WebView web;
    private ImageView gif;
    private Map<String, String> cookies;
    private static Activity root;
    public Dialog dialog;
    public boolean isFinished = false;

    public WebViewClient(WebView web, ImageView gif, Map<String, String> cookies, Activity root){
        this.web = web;
        this.gif = gif;
        this.cookies = cookies;
        this.root = root;
        dialog = new Dialog(root);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.loader);
        dialog.setCancelable(false);

        ImageView cus = dialog.findViewById(R.id.custom_loading_imageView);

        Glide.with(root)
                .load(R.drawable.ball)
                .centerCrop()
                .into(cus);
        showProgressDialog();

    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Log.d("shouldReq", request.getUrl().toString());
            if (request.getUrl().toString().contains("sdamgia.ru") ||
                    request.getUrl().toString().contains(".yandex.ru")){
                return false;
            } else {
                return true;
            }
        }
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            web.loadUrl(request.getUrl().toString(), cookies);

        }*/
        return false;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Log.d("shouldURl", url);
        //web.loadUrl(url, cookies);
        if (url.contains("sdamgia.ru") || url.contains(".yandex.ru"))
            return false;
        else
            return true;
    }
    private void showProgressDialog() {
        if (dialog == null) {
            dialog = new Dialog(root);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.loader);
            dialog.setCancelable(false);
        }
        dialog.show();
    }

    public void dismissProgressDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }



    /*@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Nullable
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        Log.d("shouldIntReq", request.getUrl().toString());
        if (request.getUrl().toString().contains("sdamgia.ru") || request.getUrl().toString().contains(".yandex.ru"))
            //if (url.contains("sdamgia.ru"))
            return super.shouldInterceptRequest(view, request.getUrl().toString());
        else {
            Log.d("restricted", request.getUrl().toString());
            return new WebResourceResponse("text/plain", "utf-8",
                    new ByteArrayInputStream("".getBytes()));
        }
    }
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        Log.d("shouldIntUrl", url);
        if (url.contains("sdamgia.ru") || url.contains(".yandex.ru"))
        //if (url.contains("sdamgia.ru"))
            return super.shouldInterceptRequest(view, url);
        else {
            Log.d("restricted", url);
            return new WebResourceResponse("text/plain", "utf-8",
                    new ByteArrayInputStream("".getBytes()));
        }
    }*/


    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        //gif.setVisibility(View.VISIBLE);
        showProgressDialog();
    }

    @Override
    public void onPageFinished(WebView view, String url) {
       /* web.loadUrl("javascript:(function() { " +
                "document.querySelectorAll(\'div.mh\').forEach(e => e.parentNode.removeChild(e));"+
                //"document.querySelectorAll(\'ins\').forEach(e => e.parentNode.removeChild(e));"+
                "document.querySelectorAll(\'ins\').forEach(e => e.remove());"+
                "document.querySelectorAll('div[style=\"margin-top:50px\"]').forEach(e => e.remove());"+
                "document.querySelectorAll('div[style=\"margin:auto\"]').forEach(e => e.remove());"+
                "document.querySelectorAll(\'iframe\').forEach(e => e.parentNode.removeChild(e));"+
                "document.querySelector(\'center > font\').style.display=\'none\';"+
                "document.querySelectorAll(\'yatag\').forEach(e => e.parentNode.removeChild(e));"+
                //"document.querySelectorAll(\'canter > font\').forEach(e => e.parentNode.removeChild(e));"+
                "document.querySelectorAll(\'div.scb1dae9c\').forEach(e => e.parentNode.removeChild(e));"+
                "document.querySelectorAll(\'div.scb1dae9c\').forEach(e => e.remove());"+
                            *//*"document.querySelector(\'div.mh\').style.display=\'none\';" +
                            "document.querySelector(\'ins\').style.display=\'none\';"+
                            "document.querySelector(\'center > font\').style.display=\'none\';"+
                            //"document.querySelector(\'iframe\').style.display=\'none\';"+
                            "document.querySelector(\'div.scb1dae9c\').style.display=\'none\';"+*//*
                "})()");*/
        web.loadUrl("javascript:(function() { \n" +
                "document.querySelectorAll('div.mh').forEach(e => e.parentNode.removeChild(e));\n" +
                "document.querySelectorAll('ins').forEach(e => e.remove());\n" +
                "document.querySelectorAll('div[style=\"margin-top: 50px; height: auto !important;\"]').forEach(e => e.remove());\n" +
                "document.querySelectorAll('div[style=\"margin:auto\"]').forEach(e => e.remove());\n" +
                "document.querySelectorAll('iframe').forEach(e => e.parentNode.removeChild(e));\n" +
                "document.querySelectorAll('yatag').forEach(e => e.parentNode.removeChild(e));\n" +
                "document.querySelectorAll('div.scb1dae9c').forEach(e => e.parentNode.removeChild(e));\n" +
                "document.querySelectorAll('div.scb1dae9c').forEach(e => e.remove());\n" +
                "document.querySelector('ins').style.display='none';\n" +
                "document.querySelector('center > font').style.display='none';\n" +
                "document.querySelector('div.scb1dae9c').style.display='none';})()");
        Log.d("PAGE", "FINISHED");
        //gif.setVisibility(View.GONE);
        dismissProgressDialog();
        isFinished = true;
    }
}
