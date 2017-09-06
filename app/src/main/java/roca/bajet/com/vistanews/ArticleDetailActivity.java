package roca.bajet.com.vistanews;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import io.realm.Realm;

/**
 * Created by Arnold on 9/5/2017.
 */

public class ArticleDetailActivity extends AppCompatActivity {

    private WebView mWebView;
    private Realm mRealm;
    private String mArticleUrl;
    private final String LOG_TAG = ArticleDetailActivity.class.getSimpleName();

    public static final String EXTRA_DETAIL_URL = "extra_detail_url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mArticleUrl = getIntent().getExtras().getString(EXTRA_DETAIL_URL);

        setContentView(R.layout.activity_detail_article);
        mWebView = (WebView) findViewById(R.id.article_detail_webview);

        Log.d(LOG_TAG, mArticleUrl);

        //mRealm = Realm.getDefaultInstance();

        mWebView.setWebViewClient(new WebViewClient());

        mWebView.loadUrl(mArticleUrl);

    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if(mRealm != null)
        {
            mRealm.close();
            mRealm = null;
        }
    }
}
