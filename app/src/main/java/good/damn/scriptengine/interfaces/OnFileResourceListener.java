package good.damn.scriptengine.interfaces;

public interface OnFileResourceListener {

    void onFileResource(byte[] fileBytes, byte resID, String extension);
}
