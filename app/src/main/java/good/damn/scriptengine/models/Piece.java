package good.damn.scriptengine.models;

import android.text.SpannableString;

import java.util.ArrayList;
import java.util.LinkedList;

public class Piece {

    private CharSequence mString;
    private byte[] mChunk;
    private LinkedList<ResourceReference> mResPositions;

    public Piece(byte[] chunk, CharSequence spannableString) {
        mChunk = chunk;
        mString = spannableString;
    }

    public void setResRef(LinkedList<ResourceReference> mResPositions) {
        this.mResPositions = mResPositions;
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

    public LinkedList<ResourceReference> getResRef() {
        return mResPositions;
    }

    public CharSequence getString() {
        return mString;
    }
}
