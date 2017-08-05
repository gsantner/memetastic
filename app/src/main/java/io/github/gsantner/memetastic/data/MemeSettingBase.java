package io.github.gsantner.memetastic.data;

import java.io.Serializable;

/**
 * Created by gregor on 05.08.17.
 */
@SuppressWarnings("WeakerAccess")
public class MemeSettingBase implements Serializable {
    public interface OnMemeSettingChangedListener {
        void onMemeSettingChanged(MemeSettingBase memeSetting);
    }

    protected MemeSetting.OnMemeSettingChangedListener _memeSettingChangedListener;

    public MemeSetting.OnMemeSettingChangedListener getMemeSettingChangedListener() {
        return _memeSettingChangedListener;
    }

    public MemeSettingBase setMemeSettingChangedListener(MemeSetting.OnMemeSettingChangedListener _settingChangedListener) {
        _memeSettingChangedListener = _settingChangedListener;
        return this;
    }

    public void notifyChangedListener() {
        if (_memeSettingChangedListener != null) {
            _memeSettingChangedListener.onMemeSettingChanged(this);
        }
    }
}
