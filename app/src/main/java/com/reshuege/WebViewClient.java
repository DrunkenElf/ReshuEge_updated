package com.reshuege;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import android.view.Window;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.widget.ImageView;

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

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        //gif.setVisibility(View.VISIBLE);
        showProgressDialog();
    }

    @Override
    public void onPageFinished(WebView view, String url) {
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
