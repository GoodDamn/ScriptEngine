package good.damn.scriptengine.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import good.damn.scriptengine.R;
import good.damn.scriptengine.adapters.recycler_view.PiecesAdapter;
import good.damn.scriptengine.models.Piece;
import good.damn.scriptengine.utils.FileReaderUtils;

public class PiecesListFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_pieces_list, container, false);

        Context context = getContext();

        RecyclerView piecesRecyclerView = v.findViewById(R.id.f_pieces_list_recyclerView);
        piecesRecyclerView.setHasFixedSize(true);
        piecesRecyclerView.setLayoutManager(new LinearLayoutManager(context));

        InputStream inputStream = context.getResources().openRawResource(R.raw.text);
        ArrayList<Piece> pieces = null;
        try {
            pieces = FileReaderUtils.Txt(inputStream);
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

        if (pieces == null)
            return v;

        piecesRecyclerView.setAdapter(new PiecesAdapter(pieces));

        return v;
    }
}
