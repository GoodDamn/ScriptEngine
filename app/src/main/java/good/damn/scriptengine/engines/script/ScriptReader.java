package good.damn.scriptengine.engines.script;

public class ScriptReader {

    private final ScriptEngine mScriptEngine;

    private byte[] mContent;

    public ScriptReader(ScriptEngine scriptEngine) {
        mScriptEngine = scriptEngine;
    }

    public void setSource(byte[] content) {
        mContent = content;
    }

    public void setCursorTo(short index) {

    }
}
