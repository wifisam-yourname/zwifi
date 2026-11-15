package com.zwifi.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

public class MainActivity extends Activity {
    private static final int PORT = 8042;
    private static final String URL = "http://127.0.0.1:" + PORT + "/";
    private WebView web;

    private static final String ERROR_HTML =
        "<html dir='rtl'><head><meta name='viewport' content='width=device-width,initial-scale=1'></head>"
        + "<body style='background:#050806;color:#e8f5ec;font-family:sans-serif;text-align:center;padding:48px 20px'>"
        + "<h2 style='color:#22e05c'>Z-WIFI</h2>"
        + "<p>تعذر تشغيل الخدمة الداخلية.</p>"
        + "<p style='color:#8aa395'>أغلق التطبيق وافتحه من جديد.</p>"
        + "<button onclick='location.href=\"" + URL + "\"' style='margin-top:16px;padding:14px 28px;border:0;border-radius:14px;background:#07a368;color:#fff;font-size:17px'>إعادة المحاولة</button>"
        + "<script>setInterval(function(){fetch('" + URL + "manifest.json').then(function(r){if(r.ok)location.href='" + URL + "'}).catch(function(){})},2000)</script>"
        + "</body></html>";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        web = new WebView(this);
        setContentView(web);

        // تشغيل Python والخادم داخل التطبيق، بدون Termux
        try {
            if (!Python.isStarted()) Python.start(new AndroidPlatform(this));
            Python.getInstance().getModule("android_main")
                .callAttr("start", getFilesDir().getAbsolutePath(), PORT);
        } catch (Exception e) {
            // يظهر الخطأ في صفحة إعادة المحاولة
        }

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setCacheMode(WebSettings.LOAD_NO_CACHE);

        web.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView v, WebResourceRequest req, WebResourceError err) {
                if (req.isForMainFrame()) {
                    v.loadDataWithBaseURL(URL, ERROR_HTML, "text/html", "UTF-8", null);
                }
            }
        });

        // الصفحة تستخدم confirm() و alert()، وبدون هذا الجزء لا تعمل داخل WebView
        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView v, String url, String msg, final JsResult r) {
                new AlertDialog.Builder(MainActivity.this)
                    .setMessage(msg)
                    .setPositiveButton("حسنًا", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface d, int w) { r.confirm(); }
                    })
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        public void onCancel(DialogInterface d) { r.cancel(); }
                    })
                    .show();
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView v, String url, String msg, final JsResult r) {
                new AlertDialog.Builder(MainActivity.this)
                    .setMessage(msg)
                    .setPositiveButton("متابعة", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface d, int w) { r.confirm(); }
                    })
                    .setNegativeButton("إلغاء", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface d, int w) { r.cancel(); }
                    })
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        public void onCancel(DialogInterface d) { r.cancel(); }
                    })
                    .show();
                return true;
            }
        });

        web.loadUrl(URL);
    }

    @Override
    public void onBackPressed() {
        if (web.canGoBack()) web.goBack();
        else super.onBackPressed();
    }
}
