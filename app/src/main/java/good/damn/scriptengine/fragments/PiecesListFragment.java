package good.damn.scriptengine.fragments;

import android.Manifest;
import android.app.Dialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.security.Policy;
import java.util.ArrayList;
import java.util.Collection;

import good.damn.scriptengine.R;
import good.damn.scriptengine.activities.PreviewActivity;
import good.damn.scriptengine.adapters.FilesAdapter;
import good.damn.scriptengine.adapters.recycler_view.PiecesAdapter;
import good.damn.scriptengine.engines.script.ScriptEngine;
import good.damn.scriptengine.interfaces.OnClickTextPiece;
import good.damn.scriptengine.models.Piece;
import good.damn.scriptengine.utils.FileOutputUtils;
import good.damn.scriptengine.utils.FileReaderUtils;
import good.damn.scriptengine.utils.ToolsUtilities;
import good.damn.scriptengine.utils.Utilities;
import good.damn.traceview.utils.ByteUtils;

public class PiecesListFragment extends Fragment {

    private static final String TAG = "PiecesListFragment";

    private OnClickTextPiece mOnClickTextPiece;
    private View.OnClickListener mOnResFolderClickListener;

    private String[] mClipData;

    private ArrayList<Piece> mPieces = null;

    private String mFileNameSSE = null;

    public void setOnClickTextPieceListener(OnClickTextPiece mOnClickTextPiece) {
        this.mOnClickTextPiece = mOnClickTextPiece;
    }

    // Need to put with view directly, because it passes once
    // inside onCreateView() method
    public void setOnClickResFolderListener(View.OnClickListener clickListener) {
        mOnResFolderClickListener = clickListener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_pieces_list, container, false);

        Context context = getContext();

        RecyclerView piecesRecyclerView = v.findViewById(R.id.f_pieces_list_recyclerView);
        piecesRecyclerView.setHasFixedSize(false);
        piecesRecyclerView.setLayoutManager(new LinearLayoutManager(context));

        v.findViewById(R.id.f_pieces_list_resources_page)
                .setOnClickListener(mOnResFolderClickListener);

        InputStream inputStream = context.getResources().openRawResource(R.raw.text);

        try {
            mPieces = FileReaderUtils.Txt(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (mPieces == null)
            return v;

        PiecesAdapter piecesAdapter = new PiecesAdapter(mPieces, mOnClickTextPiece);

        piecesRecyclerView.setAdapter(piecesAdapter);

        Dialog dialogPaste = new Dialog(context);
        dialogPaste.setCancelable(true);
        dialogPaste.setContentView(R.layout.dialog_how_to_paste);

        dialogPaste.findViewById(R.id.dialog_paste_btn_top)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        for (byte i = (byte) (mClipData.length-1); i >= 0; i--) {
                            mPieces.add(0,new Piece(
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
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int oldLength = mClipData.length;
                        for (byte i = 0; i <mClipData.length; i++) {
                            mPieces.add(new Piece(
                                    FileReaderUtils.BlankChunk(mClipData[i].getBytes(StandardCharsets.UTF_8)),
                                    mClipData[i]));
                        }
                        piecesAdapter.setPieces(mPieces);
                        piecesAdapter.notifyItemRangeInserted(oldLength, oldLength+mClipData.length);
                        dialogPaste.cancel();
                    }
                });

        v.findViewById(R.id.f_pieces_list_open_scripts)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ToolsUtilities.startFileManager(getActivity(),
                                new FilesAdapter.OnFileClickListener() {
                                    @Override
                                    public void onFile(File file, String extension) {
                                        Log.d(TAG, "onFile: EXTENSION: " + extension);

                                        if (!extension.equals("sse")) {
                                            Utilities.showMessage("INCORRECT FILE (NEED .sse FILE)",context);
                                            return;
                                        }

                                        try {
                                            FileInputStream fis = new FileInputStream(file);
                                            short size = (short) (fis.read() & 0xff);

                                            mPieces = new ArrayList<>();

                                            byte[] bufShort = new byte[2];

                                            ScriptEngine scriptEngine = new ScriptEngine();

                                            for (short i = 0; i < size; i++) {
                                                fis.read(bufShort); // read text size

                                                byte[] text = new byte[ByteUtils.Short(bufShort,0)];

                                                fis.read(bufShort); // read script size

                                                byte[] script = new byte[ByteUtils.Short(bufShort,0)];

                                                fis.read(text);
                                                fis.read(script);

                                                String t = new String(text, StandardCharsets.UTF_8);
                                                String s = new String(script, StandardCharsets.UTF_8);

                                                Log.d(TAG, "onFile: INDEX " + i + "||||||||||||||||||||||");
                                                Log.d(TAG, "onFile: " + t);
                                                Log.d(TAG, "onFile: "+s);

                                                Piece piece = new Piece(FileReaderUtils.BlankChunk(text),t);
                                                ScriptEditorFragment.CompileScript(t,s,piece,scriptEngine,context);
                                                mPieces.add(piece);
                                            }

                                            fis.close();

                                            piecesAdapter.setPieces(mPieces);
                                            piecesAdapter.notifyDataSetChanged();

                                            mFileNameSSE = file.getName();

                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                }, Environment.getExternalStorageDirectory().getAbsolutePath()+"/ScriptProjects");
                    }
                });

        ClipboardManager clipboardManager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);

        v.findViewById(R.id.f_pieces_list_paste_text)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        mFileNameSSE = null;

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
                        if (mFileNameSSE == null) {
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
                                          mFileNameSSE = name + ".sse";
                                          dialog.dismiss();
                                          FileOutputUtils.mkSSEFile(mFileNameSSE,mPieces,getActivity());
                                      }
                                  });

                            dialog.show();
                            return;
                        }

                        FileOutputUtils.mkSSEFile(mFileNameSSE,mPieces,getActivity());
                    }
                });

        v.findViewById(R.id.f_pieces_list_start_debug)
                .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                assert context != null;
                Utilities.showMessage("LINKING...",context);

                long current = System.currentTimeMillis();
                new Thread(()->{
                    String path = FileOutputUtils.mkSKCFile(mPieces,context);
                    if (path == null) {
                        Utilities.showMessage("ERROR IS OCCURRED DURING LINKING PROCESS",
                                context);
                        return;
                    }

                    new Handler(Looper.getMainLooper())
                            .post(()->{
                                Utilities.showMessage("STARTING PREVIEW PROCESS AFTER " + (System.currentTimeMillis()-current) + "ms", context);
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
