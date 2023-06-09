package good.damn.scriptengine.adapters.recycler_view;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import good.damn.scriptengine.models.Piece;

public class PiecesAdapter extends RecyclerView.Adapter<PiecesAdapter.PiecesViewHolder> {

    private final Piece[] mPieces;

    public PiecesAdapter(Piece[] pieces) {
        mPieces = pieces;
    }

    @NonNull
    @Override
    public PiecesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PiecesViewHolder(new TextView(parent.getContext()));
    }

    @Override
    public void onBindViewHolder(@NonNull PiecesViewHolder holder, int position) {
        holder.mTextView.setText(mPieces[position].getString());
    }

    @Override
    public int getItemCount() {
        return mPieces.length;
    }

    public static class PiecesViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;

        public PiecesViewHolder(@NonNull View itemView) {
            super(itemView);

            mTextView = (TextView) itemView;
        }
    }
}
