package good.damn.scriptengine.models;

import android.text.SpannableString;

public class Piece {

    private CharSequence mString;
    private byte[] mChunk;

    public Piece(byte[] chunk, CharSequence spannableString) {
        mChunk = chunk;
        mString = spannableString;
    }

    public void setChunk(byte[] mChunk) {
        this.mChunk = mChunk;
    }

    public void setString(CharSequence mString) {
        this.mString = mString;
    }

    public byte[] getChunk() {
        return mChunk;
    }

    public CharSequence getString() {
        return mString;
    }
}
