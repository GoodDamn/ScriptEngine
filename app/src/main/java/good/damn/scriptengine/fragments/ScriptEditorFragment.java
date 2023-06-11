package good.damn.scriptengine.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import good.damn.scriptengine.R;
import good.damn.scriptengine.adapters.FilesAdapter;
import good.damn.scriptengine.engines.script.ScriptEngine;
import good.damn.scriptengine.models.Piece;
import good.damn.scriptengine.utils.ArrayUtils;
import good.damn.scriptengine.utils.ToolsUtilities;
import good.damn.scriptengine.views.TextViewPhrase;

public class ScriptEditorFragment extends Fragment {

    private static final String TAG = "ScriptEditorFragment";

    private boolean isEdited = false;

    private EditText et_phrase;

    private Piece mPiece;

    public void startScript(Piece piece) {
        mPiece = piece;
        et_phrase.setText(piece.getString());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_script_editor, container,false);

        Context context = getContext();

        assert context != null;

        Log.d(TAG, "onCreateView: CREATING THE VIEW...");
        et_phrase = v.findViewById(R.id.personalEditor_editText_phrase);
        EditText editTextScript = v.findViewById(R.id.personalEditor_editText_script);

        ViewGroup root = (ViewGroup) editTextScript.getParent().getParent();

        ScriptEngine scriptEngine = new ScriptEngine(context);
        scriptEngine.setRootViewGroup(root);
        scriptEngine.setSourceEditText(et_phrase);

        v.findViewById(R.id.personalEditor_button_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Spannable spannable = et_phrase.getText();
                et_phrase.setText(spannable.toString());
                String[] arr = editTextScript.getText().toString().split("\n");
                byte[] script = new byte[0];
                byte scriptLength = 0;
                for (byte i = 0; i < arr.length; i++) {
                    byte[] t = scriptEngine.compile(arr[i]);
                    if (t == null)
                        continue;
                    script = ArrayUtils.concatByteArrays(script,t);
                    scriptLength += t.length;
                    Log.d(TAG, "onClick: PARSED_INFO: FOR SCRIPT: " + arr[i] + " SCRIPT_BYTE_LENGTH: " + t.length + " TOTAL_LENGTH:" + script.length);
                }

                String t = et_phrase.getText().toString().trim();
                byte[] text = ArrayUtils.concatByteArrays(t.getBytes(StandardCharsets.UTF_8),
                        new byte[]{0});

                int length = text.length+1+script.length;

                byte[] chunkLength = new byte[]{
                        (byte) (length >> 24),
                        (byte) ((length >> 16) & 0xFF),
                        (byte) ((length >> 8) & 0xFF),
                        (byte) (length & 0xFF)
                };

                byte[] total = ArrayUtils.concatByteArrays(
                        chunkLength,
                        text,
                        new byte[]{scriptLength},
                        script);

                mPiece.setString(et_phrase.getText());
                mPiece.setChunk(total);

                TextViewPhrase textViewPhrase = new TextViewPhrase(context);
                textViewPhrase.setTypeface(et_phrase.getTypeface());
                Log.d(TAG, "onClick: Result: SCRIPT_LENGTH: " + scriptLength + " TEXT_LENGTH: " + text.length + " TEXT_SIZE_BYTES: "+ t.getBytes(StandardCharsets.UTF_8).length + " TOTAL_LENTGH: " + total.length);
                scriptEngine.read(total, textViewPhrase);
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

        v.findViewById(R.id.personalEditor_selectFile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToolsUtilities.startFileManager(getActivity(), new FilesAdapter.Click() {
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

        return v;
    }
}
