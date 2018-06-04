package by.sevenlis.rss.reader.fragments;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import by.sevenlis.rss.reader.R;

public class ViewEntityFragment extends Fragment {
    private ProgressBar progressBar;
    private WebView webView;
    private String entityUrl;
    
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.feed_entity_web_view,container,false);
        
        if (getArguments() != null) {
            entityUrl = getArguments().getString("entityUrl");
        }
        
        progressBar = view.findViewById(R.id.progressBar);
        
        webView = view.findViewById(R.id.webView);
        
        return view;
    }
    
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (entityUrl != null) {
            loadEntityUrl();
        }
    }
    
    @SuppressLint("SetJavaScriptEnabled")
    private void loadEntityUrl() {
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);
        
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setIndeterminate(false);
                progressBar.setProgress(newProgress);
                super.onProgressChanged(view, newProgress);
            }
        });
        
        WebViewClient webViewClient = new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
            }
        };
        webView.setWebViewClient(webViewClient);
        
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        
        webView.loadUrl(entityUrl);
    }
    
    
}
