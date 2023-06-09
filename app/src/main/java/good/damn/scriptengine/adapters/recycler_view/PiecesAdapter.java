package good.damn.scriptengine.adapters.recycler_view;

import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import good.damn.scriptengine.models.Piece;

public class PiecesAdapter extends RecyclerView.Adapter<PiecesAdapter.PiecesViewHolder> {

    private static final String TAG = "PiecesAdapter";

    private final ArrayList<Piece> mPieces;

    public PiecesAdapter(ArrayList<Piece> pieces) {
        Log.d(TAG, "PiecesAdapter: ADAPTER_SIZE:" + pieces.size());
        mPieces = pieces;
    }

    @NonNull
    @Override
    public PiecesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TextView textView = new TextView(parent.getContext());
        textView.setTextColor(Color.WHITE);
        textView.setGravity(Gravity.CENTER);
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 200));
        return new PiecesViewHolder(textView);
    }

    @Override
    public void onBindViewHolder(@NonNull PiecesViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: POSITION: " + position);
        holder.mTextView.setText(mPieces.get(position).getString());
    }

    @Override
    public int getItemCount() {
        return mPieces.size();
    }

    public static class PiecesViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;

        public PiecesViewHolder(@NonNull View itemView) {
            super(itemView);

            mTextView = (TextView) itemView;
        }
    }
}
