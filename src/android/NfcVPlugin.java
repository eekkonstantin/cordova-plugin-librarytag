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

public class NfcVPlugin extends CordovaPlugin {
    private static final String TAG = "NfcVPlugin";
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
        stopNfc();
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

        if (action.equals("connect")) {
            this.connect(callbackContext);
            return true;
        } else if (action.equals("disconnect")) {
            this.disconnect(callbackContext);
            return true;
        } else if (action.equals("isConnected")) {
            this.isConnected(callbackContext);
            return true;
        } else if (action.equals("read")) {
            this.read(callbackContext);
            return true;
        // } else if (action.equals("write")) {
        //     final String arg0 = args.getString(0);
        //     final int pageOffset = Integer.parseInt(arg0);
        //     final byte[] data = jsonToByteArray(args.getJSONArray(1));
        //     this.write(callbackContext, pageOffset, data);
        //     return true;
        // } else if (action.equals("unlock")) {
        //     final String arg0 = args.getString(0);
        //     final int pin = Integer.parseInt(arg0);
        //     this.unlock(callbackContext, pin);
        //     return true;
        } else if (action.equals("echo")) {
          final String arg0 = args.getString(0);
          // final PluginResult result = new PluginResult(PluginResult.Status.OK, "Received: " + arg0);
          // callbackContext.sendPluginResult(result);
          this.echo(arg0, callbackContext);
          return true;
        }

        return false;
    }

    private void echo(String phrase, CallbackContext callbackContext) {
      if (phrase != null && phrase.length() > 0) {
        callbackContext.success("Received: " + phrase);
      } else {
        callbackContext.error("Expected one non-empty string argument.");
      }
    }

    @Override
    public void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent " + intent);
        super.onNewIntent(intent);
        setIntent(intent);
        savedIntent = intent;
        parseMessage();
    }

    private void connect(final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                if (getIntent() == null) { // Lost Tag
                    clean(callbackContext, "No tag available.");
                    return;
                }

                final Tag tag = savedIntent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                if (tag == null) {
                    clean(callbackContext, "No tag available.");
                    return;
                }
                try {
                    Log.i(TAG, "Tag is: " + tag);
                    nfcv.connect(tag);
                    callbackContext.success();
                } catch (final Exception e) {
                    clean(callbackContext, e);
                }
            }
        });
    }

    private void disconnect(final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                if (getIntent() == null) { // Lost Tag
                    clean(callbackContext, "No tag available.");
                    return;
                }
                try {
                    nfcv.disconnect();
                    callbackContext.success();
                } catch (final Exception e) {
                    clean(callbackContext, e);
                }
            }
        });
    }

    private void isConnected(final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                if (getIntent() == null) { // Lost Tag
                    clean(callbackContext, "No tag available.");
                    return;
                }
                try {
                    final boolean isConnected = nfcv.isConnected();
                    final JSONObject result = new JSONObject();
                    result.put("connected", isConnected);
                    callbackContext.success(result);
                } catch (final Exception e) {
                    clean(callbackContext, e);
                }
            }
        });
    }

    private void read(final CallbackContext callbackContext) {
      cordova.getThreadPool().execute(new Runnable() {
        @Override
        public void run() {
          if (getIntent() == null) { // Lost Tag
            clean(callbackContext, "No tag available.");
            return;
          }
          try {
            // String result = nfcv.read(tag);
            // final byte[] data = mifareUltralight.read(pageOffset);
            // final JSONObject result = new JSONObject();
            // result.put("data", bytesToHex(data));
            callbackContext.success(nfcv.read(tag));
          } catch (final Exception e) {
            clean(callbackContext, e);
          }
        }
      });
    }

    private void fireTagEvent(Tag tag, String name) {
      JSONArray json = new JSONArray();
      json.put(tag.getTechList());
      String command = MessageFormat.format(javaScriptEventTemplate, name, json);
      this.webView.sendJavascript(command);
    }

    private void clean(final CallbackContext callbackContext, Exception e) {
        clean(callbackContext, "Error: " + e);
    }

    private void clean(final CallbackContext callbackContext, String error) {
        tag = null;
        callbackContext.error("Error: " + error);
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

    private void stopNfc() {
        Log.d(TAG, "stopping NfcV service");
        getActivity().runOnUiThread(new Runnable() {
            public void run() {

                NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());

                if (nfcAdapter != null) {
                    try {
                        nfcAdapter.disableForegroundDispatch(getActivity());
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
                    fireTagEvent(tag, "nfcvTagDiscovered");
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
