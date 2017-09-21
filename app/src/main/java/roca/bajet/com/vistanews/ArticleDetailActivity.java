package roca.bajet.com.vistanews;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ShareActionProvider;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import roca.bajet.com.vistanews.data.RealmArticle;

/**
 * Created by Arnold on 9/5/2017.
 */

public class ArticleDetailActivity extends AppCompatActivity {

    private WebView mWebView;
    private Toolbar mToolbar;
    private Realm mRealm;
    private String mArticleUrl;
    private RealmArticle mRealmArticle;
    private final String LOG_TAG = ArticleDetailActivity.class.getSimpleName();
    private ShareActionProvider mShareActionProvider;

    public static final String EXTRA_DETAIL_URL = "extra_detail_url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mArticleUrl = getIntent().getExtras().getString(EXTRA_DETAIL_URL);

        setContentView(R.layout.activity_detail_article);
        mWebView = (WebView) findViewById(R.id.article_detail_webview);
        mToolbar = (Toolbar) findViewById(R.id.article_detail_toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);
        //getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_close);


        Log.d(LOG_TAG, mArticleUrl);

        RealmConfiguration config = new RealmConfiguration
                .Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        mRealm = Realm.getInstance(config);
        RealmResults<RealmArticle> results = mRealm.where(RealmArticle.class).equalTo("url", mArticleUrl).findAllAsync();
        mRealmArticle = results.first();

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_detail_article, menu);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Log.d(LOG_TAG, "onOptionsitemSelected: Home button pressed");


                NavUtils.navigateUpFromSameTask(this);
                return true;

            case R.id.action_share:

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, mArticleUrl);
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.menu_detail_send_to)));


                return true;

            case R.id.action_open_browser:

                Intent viewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mArticleUrl));

                startActivity(viewIntent);


                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
