package io.github.gsantner.memetastic.data;

import java.io.Serializable;

/**
 * Created by gregor on 05.08.17.
 */
@SuppressWarnings("WeakerAccess")
public class MemeEditorObjectBase implements Serializable {
    public interface OnMemeEditorObjectChangedListener {
        void onMemeEditorObjectChanged(MemeEditorObjectBase memeEditorObject);
    }

    protected OnMemeEditorObjectChangedListener _changedListener;

    public OnMemeEditorObjectChangedListener getChangedListener() {
        return _changedListener;
    }

    public MemeEditorObjectBase setChangedListener(OnMemeEditorObjectChangedListener _settingChangedListener) {
        _changedListener = _settingChangedListener;
        return this;
    }

    public void notifyChangedListener() {
        if (_changedListener != null) {
            _changedListener.onMemeEditorObjectChanged(this);
        }
    }
}
