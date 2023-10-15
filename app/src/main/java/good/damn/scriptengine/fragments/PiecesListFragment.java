package good.damn.scriptengine.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Movie;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import good.damn.scriptengine.R;
import good.damn.scriptengine.activities.MainActivity;
import good.damn.scriptengine.activities.PreviewActivity;
import good.damn.scriptengine.adapters.FilesAdapter;
import good.damn.scriptengine.adapters.recycler_view.PiecesAdapter;
import good.damn.scriptengine.engines.script.ScriptEngine;
import good.damn.scriptengine.engines.script.ScriptReader;
import good.damn.scriptengine.engines.script.interfaces.OnCreateScriptTextViewListener;
import good.damn.scriptengine.engines.script.interfaces.OnReadCommandListener;
import good.damn.scriptengine.engines.script.models.ScriptGraphicsFile;
import good.damn.scriptengine.engines.script.models.ScriptTextConfig;
import good.damn.scriptengine.engines.script.utils.ToolsScriptEngine;
import good.damn.scriptengine.interfaces.OnClickTextPiece;
import good.damn.scriptengine.interfaces.OnFileResourceListener;
import good.damn.scriptengine.interfaces.ScriptReaderListener;
import good.damn.scriptengine.models.Piece;
import good.damn.scriptengine.utils.FileOutputUtils;
import good.damn.scriptengine.utils.FileReaderUtils;
import good.damn.scriptengine.utils.FileUtils;
import good.damn.scriptengine.utils.ToolsUtilities;
import good.damn.scriptengine.utils.Utilities;
import good.damn.traceview.models.FileSVC;

public class PiecesListFragment extends Fragment {

    private static final String TAG = "PiecesListFragment";

    private final ArrayList<Piece> mPieces = new ArrayList<>();

    private DisplayMetrics metrics;

    private OnClickTextPiece mOnClickTextPiece;
    private View.OnClickListener mOnResFolderClickListener;

    private View mResView;

    private String[] mClipData;

    private String mFileNameSKC = null;
    private String mTempScriptCode = "";

    public void setOnClickTextPieceListener(OnClickTextPiece mOnClickTextPiece) {
        this.mOnClickTextPiece = mOnClickTextPiece;
    }

    // Need to put with view directly, because it passes once
    // inside onCreateView() method
    public void setOnClickResFolderListener(View.OnClickListener clickListener) {
        mOnResFolderClickListener = clickListener;
        if (mResView != null) {
            mResView.setOnClickListener(mOnResFolderClickListener);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_pieces_list, container, false);

        Activity context = getActivity();

        if (context == null) {
            return null;
        }

        metrics = context.getResources().getDisplayMetrics();

        RecyclerView piecesRecyclerView = v.findViewById(R.id.f_pieces_list_recyclerView);
        piecesRecyclerView.setHasFixedSize(false);
        piecesRecyclerView.setLayoutManager(new LinearLayoutManager(context));

        mResView = v.findViewById(R.id.f_pieces_list_resources_page);
        mResView.setOnClickListener(mOnResFolderClickListener);

        PiecesAdapter piecesAdapter = new PiecesAdapter(mPieces, mOnClickTextPiece);

        piecesRecyclerView.setAdapter(piecesAdapter);

        Dialog dialogPaste = new Dialog(context);
        dialogPaste.setCancelable(true);
        dialogPaste.setContentView(R.layout.dialog_how_to_paste);

        dialogPaste.findViewById(R.id.dialog_paste_btn_top)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        for (byte i = (byte) (mClipData.length - 1); i >= 0; i--) {
                            mPieces.add(0, new Piece(
                                    FileReaderUtils.BlankChunk(mClipData[i].getBytes(StandardCharsets.UTF_8)),
                                    mClipData[i]));
                        }
                        piecesAdapter.setPieces(mPieces);
                        piecesAdapter.notifyItemRangeInserted(0, mClipData.length);

                        dialogPaste.cancel();
                    }
                });

        dialogPaste.findViewById(R.id.dialog_paste_btn_replace)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mPieces.clear();
                        for (String mClipDatum : mClipData) {
                            mPieces.add(new Piece(
                                    FileReaderUtils.BlankChunk(mClipDatum.getBytes(StandardCharsets.UTF_8)),
                                    mClipDatum));
                        }
                        piecesAdapter.setPieces(mPieces);
                        piecesAdapter.notifyDataSetChanged();
                        dialogPaste.cancel();
                    }
                });

        dialogPaste.findViewById(R.id.dialog_paste_btn_bottom)
                .setOnClickListener(view -> {
                    int oldLength = mClipData.length;
                    for (byte i = 0; i < mClipData.length; i++) {
                        mPieces.add(new Piece(
                                FileReaderUtils.BlankChunk(mClipData[i].getBytes(StandardCharsets.UTF_8)),
                                mClipData[i]));
                    }
                    piecesAdapter.setPieces(mPieces);
                    piecesAdapter.notifyItemRangeInserted(oldLength, oldLength + mClipData.length);
                    dialogPaste.cancel();
                });

        v.findViewById(R.id.f_pieces_list_open_scripts)
                .setOnClickListener(view -> ToolsUtilities.startFileManager(context,
                        MainActivity.PATH_SCRIPT_PROJECTS,
                        new FilesAdapter.OnFileClickListener() {
                            @Override
                            public void onFile(File file, String extension) {
                                if (!extension.equals("skc")) {
                                    Utilities.showMessage("INCORRECT FILE (NEED .skc FILE)", context);
                                    return;
                                }

                                // Clearing res folder

                                File resFolder = new File(context.getCacheDir() + FileUtils.RES_DIR);

                                if (!resFolder.exists()) {
                                    try {
                                        if (!resFolder.createNewFile()) {
                                            return;
                                        }
                                        Log.d(TAG, "onFile: RESOURCES FOLDER IS CREATED!");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        return;
                                    }
                                }

                                File[] resourcesFiles = resFolder.listFiles();
                                if (resourcesFiles != null) {
                                    for (File ff : resourcesFiles) {
                                        if (ff.delete()) {
                                            Log.d(TAG, "onFile: FILE FROM RESOURCES=" + ff.getName() + " IS DELETED!");
                                        }
                                    }
                                }

                                final ToolsScriptEngine scriptEngine = new ToolsScriptEngine(new OnReadCommandListener() {
                                    @Override
                                    public void onBackground(int color) {
                                        mTempScriptCode += "\nbg #" + Integer.toHexString(color);
                                        Log.d(TAG, "onBackground: SCRIPT: " + mTempScriptCode);
                                    }

                                    @Override
                                    public void onImage(Bitmap bitmap, ScriptGraphicsFile graphicsFile) {
                                        mTempScriptCode += "\nimg " + graphicsFile.fileName
                                                + graphicsFile.xyString(metrics)
                                                + graphicsFile.sizeString(metrics);
                                        Log.d(TAG, "onImage: SCRIPT: " + mTempScriptCode);
                                    }

                                    @Override
                                    public void onGif(Movie movie, ScriptGraphicsFile gifScript) {
                                        mTempScriptCode += "\ngif " + gifScript.fileName + gifScript.xyString(metrics);
                                        Log.d(TAG, "onGif: SCRIPT: " + mTempScriptCode);
                                    }

                                    @Override
                                    public void onSFX(ScriptEngine.ResourceFile<Byte> sfx, SoundPool soundPool) {
                                        mTempScriptCode += "\nsfx " + sfx.fileName;
                                        Log.d(TAG, "onSFX: SCRIPT: " + mTempScriptCode);
                                    }

                                    @Override
                                    public void onAmbient(ScriptEngine.ResourceFile<MediaPlayer> amb) {
                                        mTempScriptCode += "\namb " + amb.fileName;
                                        Log.d(TAG, "onAmbient: SCRIPT: " + mTempScriptCode);
                                    }

                                    @Override
                                    public void onError(String errorMsg) {
                                        Log.d(TAG, "onError: SCRIPT: ");
                                    }

                                    @Override
                                    public void onVector(ScriptEngine.ResourceFile<FileSVC> vect, String[] advancedText) {
                                        mTempScriptCode += "\nvect " + vect.fileName;
                                        Log.d(TAG, "onVector: SCRIPT: " + mTempScriptCode);
                                    }
                                });

                                final ScriptReader scriptReader = new ScriptReader(scriptEngine, file);
                                mPieces.clear();

                                scriptEngine.setOnCreateViewListener(new OnCreateScriptTextViewListener() {
                                    @Override
                                    public void onCreate(ScriptTextConfig text) {
                                        String textPiece = text.spannableString.toString();
                                        Log.d(TAG, "onCreate: SCRIPT: " + textPiece);

                                        if (text.textColor != 0xff000000) {
                                            mTempScriptCode += "\nfont #" + Integer.toHexString(text.textColor);
                                        }

                                        Piece piece = new Piece(
                                                FileReaderUtils.BlankChunk(textPiece.getBytes(StandardCharsets.UTF_8)),
                                                textPiece);
                                        ScriptEditorFragment.CompileScript(
                                                textPiece,
                                                mTempScriptCode,
                                                piece,
                                                scriptEngine,
                                                context);
                                        Log.d(TAG, "onCreate: SCRIPT: " + piece.getSourceCode());
                                        mPieces.add(piece);
                                        mTempScriptCode = "";
                                        scriptReader.next();
                                    }
                                });

                                scriptReader.setScriptReaderListener(new ScriptReaderListener() {
                                    @Override
                                    public void onReadFinish() {
                                        Log.d(TAG, "onReadFinish: SCRIPT_READER FINISHED READING");
                                        piecesAdapter.setPieces(mPieces);
                                        piecesAdapter.notifyDataSetChanged();

                                        mFileNameSKC = file.getName();
                                    }
                                });

                                scriptEngine.setFileResourceListener(new OnFileResourceListener() {
                                    @Override
                                    public void onFileResource(byte[] fileBytes,
                                                               byte resID,
                                                               String extension) {
                                        File f = new File(context.getCacheDir() + FileUtils.RES_DIR, resID + "." + extension);
                                        try {
                                            if (f.createNewFile()) {
                                                FileOutputStream fos = new FileOutputStream(f);
                                                fos.write(fileBytes);
                                                fos.close();
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });

                                scriptEngine.loadResources(file, context);

                                scriptReader.next();
                            }
                        }));

        ClipboardManager clipboardManager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);

        v.findViewById(R.id.f_pieces_list_paste_text)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        mFileNameSKC = null;

                        Utilities.showMessage("PASTED", context);
                        String data = clipboardManager
                                .getPrimaryClip()
                                .getItemAt(0)
                                .getText()
                                .toString();

                        mClipData = data.split("\\n+");

                        dialogPaste.show();
                    }
                });

        v.findViewById(R.id.f_pieces_list_save)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mFileNameSKC != null) {
                            FileOutputUtils.mkSKCFile(mFileNameSKC,
                                    Environment.getExternalStorageDirectory()
                                            .getAbsolutePath() + "/ScriptProjects",
                                    mPieces,
                                    getActivity());
                            return;
                        }
                        Dialog dialog = new Dialog(context);
                        dialog.setCancelable(true);
                        dialog.setContentView(R.layout.dialog_save_as);

                        EditText et = dialog.findViewById(R.id.dialog_save_et_fileName);

                        dialog.findViewById(R.id.dialog_save_btn_save)
                                .setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        String name = et.getText().toString().trim();
                                        if (name.isEmpty()) {
                                            return;
                                        }
                                        mFileNameSKC = name + ".skc";
                                        dialog.dismiss();
                                        FileOutputUtils.mkSKCFile(mFileNameSKC,
                                                Environment.getExternalStorageDirectory()
                                                        .getAbsolutePath() + "/ScriptProjects",
                                                mPieces,
                                                getActivity());

                                    }
                                });

                        dialog.show();
                    }
                });

        v.findViewById(R.id.f_pieces_list_start_debug)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        assert context != null;
                        //Utilities.showMessage("LINKING...",context);

                        long current = System.currentTimeMillis();
                        new Thread(() -> {
                            String path = FileOutputUtils.mkSKCFile(mPieces, context);
                            if (path == null) {
                                Utilities.showMessage("ERROR IS OCCURRED DURING LINKING PROCESS",
                                        context);
                                return;
                            }

                            new Handler(Looper.getMainLooper())
                                    .post(() -> {
                                        //Utilities.showMessage("STARTING PREVIEW PROCESS AFTER " + (System.currentTimeMillis()-current) + "ms", context);
                                        Intent intent = new Intent(getActivity(), PreviewActivity.class);
                                        intent.putExtra("dumbPath", path);
                                        startActivity(intent);
                                    });

                            Thread.currentThread().interrupt();
                        }).start();
                    }
                });

        return v;
    }
}
