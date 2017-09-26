package io.github.gsantner.memetastic.data;

import java.io.Serializable;

@SuppressWarnings("WeakerAccess")
public class MemeEditorElementBase implements Serializable {
    public interface OnMemeEditorObjectChangedListener {
        void onMemeEditorObjectChanged(MemeEditorElementBase memeEditorObject);
    }

    protected OnMemeEditorObjectChangedListener _changedListener;

    public OnMemeEditorObjectChangedListener getChangedListener() {
        return _changedListener;
    }

    public MemeEditorElementBase setChangedListener(OnMemeEditorObjectChangedListener changedListener) {
        _changedListener = changedListener;
        return this;
    }

    public void notifyChangedListener() {
        if (_changedListener != null) {
            _changedListener.onMemeEditorObjectChanged(this);
        }
    }
}
