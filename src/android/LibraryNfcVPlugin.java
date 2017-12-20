package com.kkontagion;

import org.apache.cordova.*;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;

public class LibraryNfcVPlugin extends CordovaPlugin {
    private static final String TAG = "LibraryNfcVPlugin";
    private final List<IntentFilter> intentFilters = new ArrayList<IntentFilter>();
    private final ArrayList<String[]> techLists = new ArrayList<String[]>();

    private NfcAdapter mAdapter;
    private PendingIntent pendingIntent;
    private Tag tag = null;
    // private MifareUltralight mifareUltralight = new MifareUltralight();
    private NfcVTag nfcv = new NfcVTag();
    private Intent savedIntent = null;

    private String javaScriptEventTemplate =
            "var e = document.createEvent(''Events'');\n" +
                    "e.initEvent(''{0}'');\n" +
                    "e.tag = {1};\n" +
                    "document.dispatchEvent(e);";

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        startNfc();

        mAdapter = NfcAdapter.getDefaultAdapter(this.cordova.getActivity().getApplicationContext());

        intentFilters.add(new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED));
    }

    @Override
    public void onPause(boolean multitasking) {
        super.onPause(multitasking);
        stopNfc(null);
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
        startNfc();
    }

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if (mAdapter == null) {
            callbackContext.error("NO_NFC");
            return true;
        } else if (!mAdapter.isEnabled()) {
            callbackContext.error("NFC_DISABLED");
            return true;
        } else if (action.equals("enabled")) {
            callbackContext.success("NFC_OK");
            return true;
        }

        return false;
    }

    @Override
    public void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent " + intent);
        super.onNewIntent(intent);
        setIntent(intent);
        savedIntent = intent;
        parseMessage();
    }

    private void fireTagEvent(Tag tag, String name) {
      JSONArray json = new JSONArray();

      try {
          Log.i(TAG, "Tag is: " + tag);
          nfcv.connect(tag);
          json.put(nfcv.read(tag));
      } catch (Exception e) {
          Log.e(TAG, e.toString());
      }

      String command = MessageFormat.format(javaScriptEventTemplate, name, json);
      this.webView.sendJavascript(command);
    }

    private IntentFilter[] getIntentFilters() {
        return intentFilters.toArray(new IntentFilter[intentFilters.size()]);
    }

    private void startNfc() {
        createPendingIntent(); // onResume can call startNfc before execute

        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());

                if (nfcAdapter != null && !getActivity().isFinishing()) {
                    try {
                        nfcAdapter.enableForegroundDispatch(getActivity(), pendingIntent, null, null);
                    } catch (IllegalStateException e) {
                        Log.w(TAG, "Illegal State Exception starting NFC. Assuming application is terminating.");
                    }

                }
            }
        });
    }

    private void stopNfc(final CallbackContext cb) {
        Log.d(TAG, "stopping NfcV service");
        getActivity().runOnUiThread(new Runnable() {
            public void run() {

                NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());

                if (nfcAdapter != null) {
                    try {
                        nfcAdapter.disableForegroundDispatch(getActivity());
                        if (cb != null)
                          cb.success("NFC Adapter removed.");
                    } catch (IllegalStateException e) {
                        Log.w(TAG, "Illegal State Exception stopping NFC. Assuming application is terminating.");
                    }
                }
            }
        });
    }

    private void createPendingIntent() {
        if (pendingIntent == null) {
            Activity activity = getActivity();
            Intent intent = new Intent(activity, activity.getClass());
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            pendingIntent = PendingIntent.getActivity(activity, 0, intent, 0);
        }
    }

    private Activity getActivity() {
        return this.cordova.getActivity();
    }

    private void parseMessage() {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "parseMessage " + getIntent());
                Intent intent = getIntent();
                String action = intent.getAction();
                Log.d(TAG, "action " + action);
                if (action == null) {
                    return;
                }

                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                if (action.equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
                    fireTagEvent(tag, "libraryTagDiscovered");
                }
                setIntent(new Intent());
            }
        });
    }

    private Intent getIntent() {
        return getActivity().getIntent();
    }

    private void setIntent(Intent intent) {
        getActivity().setIntent(intent);
    }
}
