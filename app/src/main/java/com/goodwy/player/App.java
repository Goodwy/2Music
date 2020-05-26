package com.goodwy.player;

import android.app.Application;
import android.os.AsyncTask;
import android.os.Build;

import androidx.annotation.NonNull;
import android.os.Handler;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.goodwy.player.appshortcuts.DynamicShortcutManager;

import java.lang.ref.WeakReference;


/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class App extends Application {

    public static final String GOOGLE_PLAY_LICENSE_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApu0LzpBoTapmzqhzjN6MAXukOcHeu+CqUydizibzKUPIbvfSnCD7Vgog+dSHieXwG/OGKt5ZazmUs1it7XuyyYWoGIpubs5BuhS1BvKR7wrITV0BTnHzKXzrGHqQS85XByB7zWFrT+otooAxo+1OQoJu4kZ8sragpQwYIvoaRk4InkMeFE68k4NLVY9cA6fjv0wPhcLayMTv/x2R0X6La4Yl9mE4azzKKJOWcmgqThhNrCXOk+WzBNCcyM+nW/KpyfMc1f1qVEYeiuDfjtQH1q8zKOivR0hMWTDcJ3NE4dPgJzVM8NrvfYdJqk0h3v3uQd4TdYI313pRTp4NFGAzUQIDAQAB";
    public static final String PRO_VERSION_PRODUCT_ID = "pro_version";

    private static App app;

    private BillingProcessor billingProcessor;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;

        // default theme
        if (!ThemeStore.isConfigured(this, 1)) {
            ThemeStore.editTheme(this)
                    .primaryColorRes(R.color.md_grey_50)
                    .accentColorRes(R.color.green)
                    .commit();
        }

        // Set up dynamic shortcuts
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            new DynamicShortcutManager(this).initDynamicShortcuts();
        }

        // automatically restores purchases
        billingProcessor = new BillingProcessor(this, App.GOOGLE_PLAY_LICENSE_KEY, new BillingProcessor.IBillingHandler() {
            @Override
            public void onProductPurchased(@NonNull String productId, TransactionDetails details) {
            }

            @Override
            public void onPurchaseHistoryRestored() {
                if (App.isProVersion()) {
                    App.notifyProVersionChanged();
                }
            }

            @Override
            public void onBillingError(int errorCode, Throwable error) {
            }

            @Override
            public void onBillingInitialized() {
                App.loadPurchases(); // runs in background
            }
        });
    }

    public static boolean isProVersion() {
        return BuildConfig.DEBUG || app.billingProcessor.isPurchased(PRO_VERSION_PRODUCT_ID);
    }

    private static OnProVersionChangedListener onProVersionChangedListener;
    public static void setOnProVersionChangedListener(OnProVersionChangedListener listener) {
        onProVersionChangedListener = listener;
    }
    public static void notifyProVersionChanged() {
        if (onProVersionChangedListener != null) {
            onProVersionChangedListener.onProVersionChanged();
        }
    }
    public interface OnProVersionChangedListener {
        void onProVersionChanged();
    }

    public static App getInstance() {
        return app;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        billingProcessor.release();
    }

    private static LoadOwnedPurchasesFromGoogleAsyncTask loadOwnedPurchasesFromGoogleAsyncTask;
    public static void loadPurchases() { // currently a bit unnecessary since it is only executed once and not outside of this class
        if (loadOwnedPurchasesFromGoogleAsyncTask == null || loadOwnedPurchasesFromGoogleAsyncTask.getStatus() == AsyncTask.Status.FINISHED) {
            loadOwnedPurchasesFromGoogleAsyncTask = new LoadOwnedPurchasesFromGoogleAsyncTask(App.getInstance().billingProcessor);
            loadOwnedPurchasesFromGoogleAsyncTask.execute();
        }
    }

    private static class LoadOwnedPurchasesFromGoogleAsyncTask extends AsyncTask<Void, Void, Void> {
        private final WeakReference<BillingProcessor> billingProcessorWeakReference;
        private boolean wasPro;

        LoadOwnedPurchasesFromGoogleAsyncTask(BillingProcessor billingProcessor) {
            this.billingProcessorWeakReference = new WeakReference<>(billingProcessor);
        }

        @Override
        protected void onPreExecute() {
            wasPro = App.isProVersion();
        }

        @Override
        protected Void doInBackground(Void... params) {
            BillingProcessor billingProcessor = billingProcessorWeakReference.get();
            if (billingProcessor != null) {
                // The Google billing library has it's own cache for about 8 - 12 hours.
                // The following only updates the billing processors cache if the Google billing library returns a value.
                // Therefore, even if the user is longer than 8 - 12 hours without internet the purchase is cached.
                billingProcessor.loadOwnedPurchasesFromGoogle();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (wasPro != App.isProVersion()) {
                App.notifyProVersionChanged();
            }
        }
    }
}
