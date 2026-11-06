package com.example.remsymes;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewScraper {

    public interface ScrapeCallback {
        void onSuccess(String scrapedText);
        void onError(String errorMsg);
    }

    public static void scrapeTextFromUrl(Context context, String url, ScrapeCallback callback) {
        Handler mainHandler = new Handler(Looper.getMainLooper());

        mainHandler.post(() -> {
            try {
                WebView webView = new WebView(context.getApplicationContext());
                WebSettings settings = webView.getSettings();
                settings.setJavaScriptEnabled(true);
                settings.setDomStorageEnabled(true);
                settings.setUserAgentString("Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36");

                final boolean[] completed = {false};

                // Safety Timeout (6 seconds)
                Runnable timeoutRunnable = () -> {
                    if (!completed[0]) {
                        completed[0] = true;
                        try {
                            webView.stopLoading();
                            webView.destroy();
                        } catch (Exception ignored) {}
                        callback.onError("Scrape timeout");
                    }
                };
                mainHandler.postDelayed(timeoutRunnable, 6000);

                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String loadedUrl) {
                        if (!completed[0]) {
                            // Extract visible innerText from the DOM
                            view.evaluateJavascript(
                                    "(function() { return document.body ? document.body.innerText : ''; })();",
                                    value -> {
                                        if (value != null && !value.equals("null") && !value.isEmpty()) {
                                            String text = value;
                                            if (text.startsWith("\"") && text.endsWith("\"") && text.length() > 2) {
                                                text = text.substring(1, text.length() - 1);
                                            }
                                            text = text.replace("\\n", "\n").replace("\\r", "\r").replace("\\\"", "\"").replace("\\t", " ");

                                            if (!completed[0] && !text.trim().isEmpty()) {
                                                completed[0] = true;
                                                mainHandler.removeCallbacks(timeoutRunnable);
                                                final String finalText = text;
                                                mainHandler.post(() -> {
                                                    try {
                                                        webView.destroy();
                                                    } catch (Exception ignored) {}
                                                    callback.onSuccess(finalText);
                                                });
                                            }
                                        }
                                    }
                            );
                        }
                    }
                });

                webView.loadUrl(url);

            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }
}