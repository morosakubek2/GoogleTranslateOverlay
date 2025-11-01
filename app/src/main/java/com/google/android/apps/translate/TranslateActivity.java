package com.google.android.apps.translate;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;

public class TranslateActivity extends Activity {
    private static final String TARGET_PACKAGE = "dev.davidv.translator";
    private static final String TARGET_ACTIVITY = ".ProcessTextActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleIntent(getIntent());
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        String text = null;
        Intent newIntent = new Intent(Intent.ACTION_PROCESS_TEXT);
        newIntent.setComponent(new ComponentName(TARGET_PACKAGE, TARGET_PACKAGE + TARGET_ACTIVITY));
        newIntent.setType("text/plain");
        newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        if (Intent.ACTION_PROCESS_TEXT.equals(action)) {
            text = intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT);
            boolean readonly = intent.getBooleanExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, false);
            newIntent.putExtra(Intent.EXTRA_PROCESS_TEXT, text);
            if (readonly) newIntent.putExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, true);
        } else if (Intent.ACTION_SEND.equals(action) && "text/plain".equals(intent.getType())) {
            text = intent.getStringExtra(Intent.EXTRA_TEXT);
            newIntent.putExtra(Intent.EXTRA_PROCESS_TEXT, text);
        } else {
            text = intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT);
            if (text == null) text = intent.getStringExtra(Intent.EXTRA_TEXT);
            newIntent.putExtra(Intent.EXTRA_PROCESS_TEXT, text);
        }

        try {
            startActivity(newIntent);
        } catch (Exception e) {
            // Nie pokazuj Toast – żeby nie migało
        }
    }
}
