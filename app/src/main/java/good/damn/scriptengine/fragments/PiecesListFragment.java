package good.damn.scriptengine.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import good.damn.scriptengine.R;
import good.damn.scriptengine.activities.PreviewActivity;
import good.damn.scriptengine.adapters.recycler_view.PiecesAdapter;
import good.damn.scriptengine.interfaces.OnClickTextPiece;
import good.damn.scriptengine.models.Piece;
import good.damn.scriptengine.utils.FileOutputUtils;
import good.damn.scriptengine.utils.FileReaderUtils;
import good.damn.scriptengine.utils.Utilities;
import good.damn.traceview.utils.ByteUtils;

public class PiecesListFragment extends Fragment {

    private static final String TAG = "PiecesListFragment";

    private OnClickTextPiece mOnClickTextPiece;
    private View.OnClickListener mOnResFolderClickListener;

    private ArrayList<Piece> mPieces = null;

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
        piecesRecyclerView.setHasFixedSize(true);
        piecesRecyclerView.setLayoutManager(new LinearLayoutManager(context));

        v.findViewById(R.id.f_pieces_list_save)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            File file = new File(context.getCacheDir()+"/save.sse");

                            if (file.createNewFile()) {
                                Log.d(TAG, "onClick: save.sse IS CREATED!");
                            }

                            FileOutputStream fos = new FileOutputStream(file);

                            fos.write(mPieces.size()); //0-255 pieces

                            byte[] temp = new byte[4];
                            short i = 0;
                            for (;i< mPieces.size();i++) { // write position temp
                                fos.write(temp);
                            }

                            int currentPos = 0;
                            i = 0;
                            FileChannel channel = fos.getChannel();

                            for (Piece piece: mPieces) {
                                byte[] textPiece = piece.getString().toString()
                                        .getBytes(StandardCharsets.UTF_8);

                                Editable source = piece.getSourceCode();
                                byte[] sourceCode = new byte[0];
                                if (source != null) {
                                    sourceCode = source.toString()
                                            .getBytes(StandardCharsets.UTF_8);
                                }

                                fos.write(ByteUtils.Short((short) textPiece.length));
                                fos.write(ByteUtils.Short((short) sourceCode.length));

                                currentPos += textPiece.length+sourceCode.length;

                                fos.write(textPiece);
                                fos.write(sourceCode);

                                channel = channel.position(1+4*i);

                                fos.write(ByteUtils.integer(currentPos));

                                channel = channel.position(1+4L*mPieces.size()+(i+1)*4+currentPos);

                                i++;
                            }

                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
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

        piecesRecyclerView.setAdapter(new PiecesAdapter(mPieces, mOnClickTextPiece));

        return v;
    }
}
