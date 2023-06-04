package good.damn.scriptengine;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import good.damn.scriptengine.adapters.FilesAdapter;
import good.damn.scriptengine.engines.script.ScriptEngine;
import good.damn.scriptengine.utils.ArrayUtils;
import good.damn.scriptengine.utils.ToolsUtilities;
import good.damn.scriptengine.views.TextViewPhrase;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private FrameLayout mContainer;
    private boolean isEdited = false;

    public void setContainer(FrameLayout container) {
        mContainer = container;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreateView: CREATING THE VIEW...");
        EditText et_phrase = findViewById(R.id.personalEditor_editText_phrase);
        EditText editTextScript = findViewById(R.id.personalEditor_editText_script);

        ScriptEngine scriptEngine = new ScriptEngine(et_phrase);

        ViewGroup root = (ViewGroup) editTextScript.getParent().getParent();
        findViewById(R.id.personalEditor_button_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Spannable spannable = et_phrase.getText();
                et_phrase.setText(spannable.toString());
                String[] arr = editTextScript.getText().toString().split("\n");
                byte[] script = new byte[0];
                byte scriptLength = 0;
                for (byte i = 0; i < arr.length; i++) {
                    byte[] t = scriptEngine.execute(arr[i]);
                    if (t == null)
                        continue;
                    script = ArrayUtils.concatByteArrays(script,t);
                    scriptLength += t.length;
                    Log.d(TAG, "onClick: " + arr[i] + " " + t.length + " " + script.length);
                }

                String t = et_phrase.getText().toString().trim();
                byte[] text = ArrayUtils.concatByteArrays(t.getBytes(StandardCharsets.UTF_8),
                        new byte[]{0});

                byte[] total = ArrayUtils.concatByteArrays(text,
                        new byte[]{scriptLength},
                        script);

                TextViewPhrase textViewPhrase = new TextViewPhrase(MainActivity.this);
                textViewPhrase.setTypeface(et_phrase.getTypeface());
                Log.d(TAG, "onClick: Result: SCRIPT_LENGTH: " + scriptLength + " TEXT_LENGTH: " + text.length + " TEXT_SIZE_BYTES: "+ t.getBytes(StandardCharsets.UTF_8).length + " TOTAL_LENTGH: " + total.length);
                scriptEngine.read(total, textViewPhrase, root);
                Log.d(TAG, "onClick: isEdited: COMPLETED: " + isEdited);
                isEdited = false;
            }
        });
        et_phrase.setText("Simple a piece of text");

        /*findViewById(R.id.view_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: isEdited: " + isEdited);
                if (isEdited) {
                    Utilities.showMessage(this, "Click on START button");
                    return;
                }

                mContainer.setVisibility(View.INVISIBLE);
                textView.setText(et_phrase.getText());
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,et_phrase.getTextSize());
            }
        });*/

        et_phrase.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "afterTextChanged: isEdited: " + isEdited);
                isEdited = true;
            }
        });

        findViewById(R.id.personalEditor_selectFile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToolsUtilities.startFileManager(MainActivity.this, new FilesAdapter.Click() {
                    @Override public void onClickedFolder(String prevFolder, String currentFolder) { }
                    @Override
                    public void onAudioFile(File file) {

                    }

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onImageFile(File file) {
                        String userPath = file.getPath().substring(20);
                        Log.d(TAG, "onImageFile: " + userPath);
                        editTextScript.setText(editTextScript.getText().toString().trim() + " " + userPath);
                        try {
                            Log.d(TAG, "onImageFile: " + file.getCanonicalPath());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }
}