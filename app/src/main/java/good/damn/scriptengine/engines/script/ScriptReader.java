package good.damn.scriptengine.engines.script;

import android.util.Log;

import good.damn.scriptengine.views.TextViewPhrase;

public class ScriptReader {

    private static final String TAG = "ScriptReader";

    private final ScriptEngine mScriptEngine;

    private byte[] mContent;

    private int mCurrentIndex;

    public ScriptReader(ScriptEngine scriptEngine, byte[] content) {
        mScriptEngine = scriptEngine;
        mCurrentIndex = 0;
        mContent = content;
    }

    public void next() {
        if (mContent == null) {
            Log.d(TAG, "setCursorTo: CONTENT BYTES ARE NULL");
            return;
        }

        TextViewPhrase textViewPhrase = new TextViewPhrase(mScriptEngine.getContext());

        Log.d(TAG, "next: CURRENT_INDEX: " + mCurrentIndex);
        mCurrentIndex += mScriptEngine.read(mContent,textViewPhrase,mCurrentIndex);
        Log.d(TAG, "next: CURRENT_INDEX AFTER: " + mCurrentIndex);
    }
}
