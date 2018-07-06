package com.example.anshsachdeva2.browserb1;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.ClientCertRequest;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions;
import android.webkit.HttpAuthHandler;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.SafeBrowsingResponse;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/* target:

 * desktop mode/ mobileview feature                                         type: needs research, easy
 * handling error codes/ navigation callbacks                               type:needs research, hard
 * android chrome tabs (something like that)                         <HIATUS>has a lot of cool features:gotcha look into it in deep
 *
 * cache : option to clear / view/ set cache mode                           type: needs research(for viewing Cache), easy
 * history database: option to clear/view                                   type: needs research, easy
 * incognito mode switch                                                    type: needs research , medium
 * collapsing action bar browser                                            type: needs research, hard
 * auto complete search bar with google search                              type: needs research, hard
 * save data on configuration change
 * searchbar with google search/ link search without adding https://        type: needs more research, vhard
 * todo COOKIES AND COOKIE MANAGER                                          type: needs research, medium
 * webview can also load user/system files like text/music/images/html files etc #todo type: needs research,hard
 * multiwndows
 * show video in full screen support
 * allow geolocation
 * video streaming : automatically handling when web enabled
 *
 * More javascript handling | recieve browser actions | managing webview | webmessage/webMessage port
                                                                            <DONE 10%: added javascript support>
 * no redirects to chrome for new page launch : on every click,
       the url will be loaded inside our browser only                       <DONE!>
 * home icon                                                                <DONE!>
 * progressbar for showing the page being loaded                            <DONE!>
 * a simple web view / edittext combo for simple search / render            <DONE!>
 * back/ forwrd button                                                      <DONE!>
  * open in chrome feature                                                  <DONE!>
 * zoom in/ out feature                                                 <DONE 50%> zoomin/out possible but no buttons
 * safe browsing                                                            <DONE 90%> only available for Oreo & above
 * html5 video support( only available to devices with hardware rendering enabled)<DONE>

 * ###
 * :( zoom not working
 * :( the collapsing action of toolbar is bad on performance since nested scroll view is being used
 *    on an already scrollable web view( without nested scroll view, colapsing action won't work)
 *
 * :( show mobile version of websites
 * :( Keyboard gets opened by default
 * :( SEARCHBAR IS FUCKING DOWN: how cn i make this shit do: https://goo.gl/Zfm6K2
 * */

public class MainActivity extends AppCompatActivity {
    private static final String TAG = ">>>>TAG>>>>";
    EditText etSearchBar;
    WebView webV;
    ProgressBar pBar;
    SwipeRefreshLayout swipeTorefresh;
    public static final String URL_BEFORE_ORIENTATION_CHANGE = "last loaded url:";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializingComponentsAndUI();
        initializingWebView(savedInstanceState);
        etSearchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() { // for handling the clicks of the keyboard
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Log.e(TAG, "onEditorAction: action ID:" + actionId);
                Log.e(TAG, "onEditorAction: eventDetails:" + event);

                dismissKeyboard(actionId);

                webV.requestFocus();
                Log.e(TAG, "onEditorAction: webview has focus:" + webV.hasFocus());

                //loading in webview
                String search = etSearchBar.getText().toString();

                Log.e(TAG, "onEditorAction: search string:" + search);
                webV.loadUrl(search);
                // this function is so ugly it won't even tell you waether you entered a
                // correct url / search or not. it will just show a fucking white screen :|
                // All this time , i thought its a webview with google's  serchbar as default , :.
                // I can search random stuff too. but this shit takes  some real  url/ uris only.

                return true;
            }
        });


        swipeTorefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                webV.reload();
                etSearchBar.setText(webV.getUrl());
                swipeTorefresh.setRefreshing(false);

                //webV.loadUrl( "javascript:window.location.reload( true )" );
                // ^ use only if java script is enabed . although its beter since it doesn't create unnecessary histories


            }
        });


    }

    private void initializingComponentsAndUI() {
        Toolbar t = findViewById(R.id.toolbar);
        setSupportActionBar(t);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_home);


        etSearchBar = findViewById(R.id.etSearch);
        webV = findViewById(R.id.webview);
        pBar = findViewById(R.id.pbar);
        swipeTorefresh = findViewById(R.id.swipeRefresh);
        swipeTorefresh.setColorSchemeColors(Color.BLUE, Color.RED, Color.MAGENTA, Color.CYAN, Color.YELLOW, Color.GREEN, Color.BLACK);


    }

    private void initializingWebView(Bundle savedInstanceState) {
        //set webview settings



        /*
        WebViewClient: a class which provides a lot of fuctions to handle the default behaviours
        of a user to web interaction via the browser. Its not like a listener interface, .: we got
        to extend it to our custom class and override default functions to implement custom behavior
        Similrly WebChrome Client is a class which provides functions to handle Javascript Actions.
        */
        MyBrowserWebClient bClient = new MyBrowserWebClient();
        MyBrowserChromeClient bCientChrome = new MyBrowserChromeClient();
        webV.setWebViewClient(bClient);
        webV.setWebChromeClient(bCientChrome);

        WebSettings webSettings = webV.getSettings();
        webSettings.setDisplayZoomControls(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setSupportZoom(true);

        webSettings.setJavaScriptEnabled(true);
        webSettings.setUserAgentString(null);
        //for setting webview as  mobile mode by default more changes handled in menu's setDesktopView


        webSettings.setAllowContentAccess(true);//allows sites to make alert dialogues
        webSettings.setAllowFileAccess(true);//allows sites to call system filemanager intent
        webSettings.setAllowFileAccessFromFileURLs(true);//allows files to be opened via file:/// #requires JSenabled

        webSettings.setSupportMultipleWindows(false);
        //        setLoadWithOverviewMode(true) loads the WebView completely zoomed out
        //setUseWideViewPort(true) makes the Webview have a normal viewport (such as a normal desktop browser), while when false the webview will have a viewport constrained to its own dimensions (so if the webview is 50px*50px the viewport will be the same size)
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);

        if (!webV.isHardwareAccelerated()) {
            Log.e(TAG, "initializingWebView:webview is not attached to hardware accelerated window," +
                    "which is needed to support html 5 videos");
            Log.e(TAG, "initializingWebView: Enabling hardware acceleration( if available, else no exceptions raised !. ughh)");
            getWindow()
                    .setFlags(
                            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
                    );
        }
        Log.e(TAG, "initializingWebView: hardware accelerated:" + webV.isHardwareAccelerated());

        //forward/backward moement is handled in on back pressed/ in menuitemSelected
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            webSettings.setSafeBrowsingEnabled(true);
        }

//        if (savedInstanceState != null) {
//            webV.restoreState(savedInstanceState);
//        } else {
//            String homePageURL = "https://google.com";
//            webV.loadUrl(homePageURL);
//            etSearchBar.setText(homePageURL);
//        }
        String homePageURL = "https://google.com";
        if (savedInstanceState != null) {
            homePageURL = savedInstanceState.getString(URL_BEFORE_ORIENTATION_CHANGE);
        }

        webV.loadUrl(homePageURL);
        etSearchBar.setText(homePageURL);

    }


    public void setDesktopMode(boolean enabled) {
        String newUserAgent = webV.getSettings().getUserAgentString();
        Log.e(TAG, "setDesktopMode: current mode:" + newUserAgent);
        if (enabled) {
            try {
                newUserAgent = "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.4) Gecko/20100101 Firefox/4.0";
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            newUserAgent = null;
        }

        webV.getSettings().setUserAgentString(newUserAgent);
        webV.getSettings().setUseWideViewPort(enabled);
        webV.getSettings().setLoadWithOverviewMode(enabled);
        webV.reload();
    }


//        etSearchBar.setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                if(keyCode==KeyEvent.KEYCODE_ENTER){
//                    Toast.makeText(MainActivity.this,"Search!"+keyCode,Toast.LENGTH_SHORT).show();
//
//                }
//
//
//                return true;
//            }
//        });


    private void dismissKeyboard(int actionId) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            //removing edittext focus. we will have an ime of Action Search
            etSearchBar.clearFocus();
            Log.e(TAG, "dismissKeyboard: searchbar has focus:" + etSearchBar.hasFocus());
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                Log.e(TAG, "dismissKeyboard: inputmethodmanager:" + imm);
                imm.hideSoftInputFromWindow(etSearchBar.getWindowToken(), 0);
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                String homePageURL = "https://google.com";
                webV.loadUrl(homePageURL);
                etSearchBar.setText(homePageURL);
                webV.clearHistory();
                return true;
            }
            case R.id.menu_refresh:{
                webV.reload();
                return true;
            }
            case R.id.menu_forward: {
                if (webV.canGoForward()) {
                    webV.goForward();
                }
                return true;

            }
            case R.id.menu_desktopmode: {
                if (item.isChecked()) {
                    setDesktopMode(false);
                    item.setChecked(false);
                } else {
                    setDesktopMode(true);
                    item.setChecked(true);
                }

                return true;
            }
            case R.id.menu_openin_browser: {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(webV.getUrl()));
                startActivity(intent);
                return true;
            }

            case R.id.menu_history : case R.id.menu_incognito :{
                Toast.makeText(MainActivity.this, "implemented when cookies are ienabled",Toast.LENGTH_SHORT).show();
                return true;
            }

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(URL_BEFORE_ORIENTATION_CHANGE, webV.getUrl());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        if (webV.canGoBack()) {
            webV.goBack();
        } else {
            super.onBackPressed();
        }
    }

    public class MyBrowserWebClient extends WebViewClient {
        MyBrowserWebClient() {
            super();

        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // its deprecated .
            //Give the application a chance to take over the control when a new url is about to be
            // loaded in the current WebView. If WebViewClient is not provided, by default WebView
            // will ask Activity Manager to choose the proper handler for the url.

            etSearchBar.setText(url);
            view.loadUrl(url);


            return true;
            //return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                String url = request.getUrl().toString();
                view.loadUrl(url);
                etSearchBar.setText(url);
                return true;
            }
            return super.shouldOverrideUrlLoading(view, request);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
        }

        @Override
        public void onPageCommitVisible(WebView view, String url) {
            super.onPageCommitVisible(view, url);
        }

        @Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            return super.shouldInterceptRequest(view, url);
        }

        @Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            return super.shouldInterceptRequest(view, request);
        }

        @Override
        public void onTooManyRedirects(WebView view, Message cancelMsg, Message continueMsg) {
            super.onTooManyRedirects(view, cancelMsg, continueMsg);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            super.onReceivedHttpError(view, request, errorResponse);
        }

        @Override
        public void onFormResubmission(WebView view, Message dontResend, Message resend) {
            super.onFormResubmission(view, dontResend, resend);
        }

        @Override
        public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
            super.doUpdateVisitedHistory(view, url, isReload);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            super.onReceivedSslError(view, handler, error);
        }

        @Override
        public void onReceivedClientCertRequest(WebView view, ClientCertRequest request) {
            super.onReceivedClientCertRequest(view, request);
        }

        @Override
        public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
            super.onReceivedHttpAuthRequest(view, handler, host, realm);
        }

        @Override
        public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
            return super.shouldOverrideKeyEvent(view, event);
        }

        @Override
        public void onUnhandledKeyEvent(WebView view, KeyEvent event) {
            super.onUnhandledKeyEvent(view, event);
        }

        @Override
        public void onScaleChanged(WebView view, float oldScale, float newScale) {
            super.onScaleChanged(view, oldScale, newScale);
        }

        @Override
        public void onReceivedLoginRequest(WebView view, String realm, @Nullable String account, String args) {
            super.onReceivedLoginRequest(view, realm, account, args);
        }

        @Override
        public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
            return super.onRenderProcessGone(view, detail);
        }

        @Override
        public void onSafeBrowsingHit(WebView view, WebResourceRequest request, int threatType, SafeBrowsingResponse callback) {
            super.onSafeBrowsingHit(view, request, threatType, callback);
        }

    }

    public class MyBrowserChromeClient extends WebChromeClient {
        static final String TAG = ">>MAIN>>CHROME_CLIENT>>";

        MyBrowserChromeClient() {
            super();
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            pBar.setProgress(newProgress);
            if (pBar.getVisibility() == View.GONE) {
                Log.e(TAG, "onProgressChanged: setVisibility :VISIBLE");
                pBar.setVisibility(View.VISIBLE);
            }
            if (newProgress == 100) {
                Log.e(TAG, "onProgressChanged: setVisibility :GONE");
                pBar.setVisibility(View.GONE);
            }

        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
        }

        @Override
        public void onReceivedIcon(WebView view, Bitmap icon) {
            super.onReceivedIcon(view, icon);
        }

        @Override
        public void onReceivedTouchIconUrl(WebView view, String url, boolean precomposed) {
            super.onReceivedTouchIconUrl(view, url, precomposed);
        }

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            super.onShowCustomView(view, callback);
        }

        @Override
        public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) {
            super.onShowCustomView(view, requestedOrientation, callback);
        }

        @Override
        public void onHideCustomView() {
            super.onHideCustomView();
        }

        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
            return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg);
        }

        @Override
        public void onRequestFocus(WebView view) {
            super.onRequestFocus(view);
        }

        @Override
        public void onCloseWindow(WebView window) {
            super.onCloseWindow(window);
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            return super.onJsAlert(view, url, message, result);
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
            return super.onJsConfirm(view, url, message, result);
        }

        @Override
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
            return super.onJsPrompt(view, url, message, defaultValue, result);
        }

        @Override
        public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result) {
            return super.onJsBeforeUnload(view, url, message, result);
        }

        @Override
        public void onExceededDatabaseQuota(String url, String databaseIdentifier, long quota, long estimatedDatabaseSize, long totalQuota, WebStorage.QuotaUpdater quotaUpdater) {
            super.onExceededDatabaseQuota(url, databaseIdentifier, quota, estimatedDatabaseSize, totalQuota, quotaUpdater);
        }

        @Override
        public void onReachedMaxAppCacheSize(long requiredStorage, long quota, WebStorage.QuotaUpdater quotaUpdater) {
            super.onReachedMaxAppCacheSize(requiredStorage, quota, quotaUpdater);
        }

        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            super.onGeolocationPermissionsShowPrompt(origin, callback);
        }

        @Override
        public void onGeolocationPermissionsHidePrompt() {
            super.onGeolocationPermissionsHidePrompt();
        }

        @Override
        public void onPermissionRequest(PermissionRequest request) {
            super.onPermissionRequest(request);
        }

        @Override
        public void onPermissionRequestCanceled(PermissionRequest request) {
            super.onPermissionRequestCanceled(request);
        }

        @Override
        public boolean onJsTimeout() {
            return super.onJsTimeout();
        }

        @Override
        public void onConsoleMessage(String message, int lineNumber, String sourceID) {
            super.onConsoleMessage(message, lineNumber, sourceID);
        }

        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            return super.onConsoleMessage(consoleMessage);
        }

        @Nullable
        @Override
        public Bitmap getDefaultVideoPoster() {
            return super.getDefaultVideoPoster();
        }

        @Nullable
        @Override
        public View getVideoLoadingProgressView() {
            return super.getVideoLoadingProgressView();
        }

        @Override
        public void getVisitedHistory(ValueCallback<String[]> callback) {
            super.getVisitedHistory(callback);
        }

        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            return super.onShowFileChooser(webView, filePathCallback, fileChooserParams);
        }
    }


}


