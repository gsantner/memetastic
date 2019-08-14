/*#######################################################
 *
 *   Maintained by Gregor Santner, 2016-
 *   https://gsantner.net/
 *
 *   License: Apache 2.0 / Commercial
 *  https://github.com/gsantner/opoc/#licensing
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
#########################################################*/
package net.gsantner.opoc.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

public class TouchImageView extends AppCompatImageView {

    Matrix matrix;
    Context _context;

    // We can be in one of these 3 states
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int _mode = NONE;

    // Remember some things for zooming
    Bitmap _orig;
    PointF _last = new PointF();
    PointF _start = new PointF();
    float _minScale = 1f;
    float _maxScale = 3f;
    float[] _m;

    int _viewWidth, _viewHeight;
    static final int CLICK = 3;
    float _saveScale = 1f;
    protected float _origWidth, _origHeight;
    int _oldMeasuredWidth, _oldMeasuredHeight;

    ScaleGestureDetector _scaleDetector;

    // Rotation
    long _rotationTimestampDebounce = System.currentTimeMillis();
    int _rotationDegrees = 0;
    RotationGestureDetector mRotationGestureDetector;

    @Override
    public void setImageBitmap(Bitmap bm) {
        _orig = bm;
        _rotationDegrees = 0;
        super.setImageBitmap(bm);
    }

    public TouchImageView(Context context) {
        super(context);
        initTouchImageView(context);
    }

    public TouchImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initTouchImageView(context);
    }

    private void initTouchImageView(Context context) {
        super.setClickable(true);
        _context = context;
        _scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        mRotationGestureDetector = new RotationGestureDetector(_rotationListener);
        matrix = new Matrix();
        _m = new float[9];
        setImageMatrix(matrix);
        setScaleType(ScaleType.MATRIX);

        setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                _scaleDetector.onTouchEvent(event);
                mRotationGestureDetector.onTouch(event);
                PointF curr = new PointF(event.getX(), event.getY());

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        _last.set(curr);
                        _start.set(_last);
                        _mode = DRAG;
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (_mode == DRAG) {
                            float deltaX = curr.x - _last.x;
                            float deltaY = curr.y - _last.y;
                            float fixTransX = getFixDragTrans(deltaX, _viewWidth,
                                    _origWidth * _saveScale);
                            float fixTransY = getFixDragTrans(deltaY, _viewHeight,
                                    _origHeight * _saveScale);
                            matrix.postTranslate(fixTransX, fixTransY);
                            fixTrans();
                            _last.set(curr.x, curr.y);
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        _mode = NONE;
                        int xDiff = (int) Math.abs(curr.x - _start.x);
                        int yDiff = (int) Math.abs(curr.y - _start.y);
                        if (xDiff < CLICK && yDiff < CLICK)
                            performClick();
                        break;

                    case MotionEvent.ACTION_POINTER_UP:
                        _mode = NONE;
                        break;
                }

                setImageMatrix(matrix);
                invalidate();
                return true; // indicate event was handled
            }

        });
    }

    public void setMaxZoom(float x) {
        _maxScale = x;
    }

    private class ScaleListener extends
            ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            _mode = ZOOM;
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float mScaleFactor = detector.getScaleFactor();
            float origScale = _saveScale;
            _saveScale *= mScaleFactor;
            if (_saveScale > _maxScale) {
                _saveScale = _maxScale;
                mScaleFactor = _maxScale / origScale;
            } else if (_saveScale < _minScale) {
                _saveScale = _minScale;
                mScaleFactor = _minScale / origScale;
            }

            if (_origWidth * _saveScale <= _viewWidth
                    || _origHeight * _saveScale <= _viewHeight)
                matrix.postScale(mScaleFactor, mScaleFactor, _viewWidth / 2,
                        _viewHeight / 2);
            else
                matrix.postScale(mScaleFactor, mScaleFactor,
                        detector.getFocusX(), detector.getFocusY());

            fixTrans();
            return true;
        }
    }

    void fixTrans() {
        matrix.getValues(_m);
        float transX = _m[Matrix.MTRANS_X];
        float transY = _m[Matrix.MTRANS_Y];

        float fixTransX = getFixTrans(transX, _viewWidth, _origWidth * _saveScale);
        float fixTransY = getFixTrans(transY, _viewHeight, _origHeight
                * _saveScale);

        if (fixTransX != 0 || fixTransY != 0)
            matrix.postTranslate(fixTransX, fixTransY);
    }

    float getFixTrans(float trans, float viewSize, float contentSize) {
        float minTrans, maxTrans;

        if (contentSize <= viewSize) {
            minTrans = 0;
            maxTrans = viewSize - contentSize;
        } else {
            minTrans = viewSize - contentSize;
            maxTrans = 0;
        }

        if (trans < minTrans)
            return -trans + minTrans;
        if (trans > maxTrans)
            return -trans + maxTrans;
        return 0;
    }

    float getFixDragTrans(float delta, float viewSize, float contentSize) {
        if (contentSize <= viewSize) {
            return 0;
        }
        return delta;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        _viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        _viewHeight = MeasureSpec.getSize(heightMeasureSpec);

        //
        // Rescales data on rotation
        //
        if (_oldMeasuredHeight == _viewWidth && _oldMeasuredHeight == _viewHeight
                || _viewWidth == 0 || _viewHeight == 0)
            return;
        _oldMeasuredHeight = _viewHeight;
        _oldMeasuredWidth = _viewWidth;

        if (_saveScale == 1) {
            // Fit to screen.
            float scale;

            Drawable drawable = getDrawable();
            if (drawable == null || drawable.getIntrinsicWidth() == 0
                    || drawable.getIntrinsicHeight() == 0)
                return;
            int bmWidth = drawable.getIntrinsicWidth();
            int bmHeight = drawable.getIntrinsicHeight();

            float scaleX = (float) _viewWidth / (float) bmWidth;
            float scaleY = (float) _viewHeight / (float) bmHeight;
            scale = Math.min(scaleX, scaleY);
            matrix.setScale(scale, scale);

            // Center the data
            float redundantYSpace = (float) _viewHeight
                    - (scale * (float) bmHeight);
            float redundantXSpace = (float) _viewWidth
                    - (scale * (float) bmWidth);
            redundantYSpace /= (float) 2;
            redundantXSpace /= (float) 2;

            matrix.postTranslate(redundantXSpace, redundantYSpace);

            _origWidth = _viewWidth - 2 * redundantXSpace;
            _origHeight = _viewHeight - 2 * redundantYSpace;
            setImageMatrix(matrix);
        }
        fixTrans();
    }

    private final RotationGestureDetector.Listener _rotationListener = new RotationGestureDetector.Listener() {
        @Override
        public void onRotate(float delta) {
            if (((System.currentTimeMillis() - _rotationTimestampDebounce)) > 300 && Math.abs(delta) > 4.6) {
                _rotationDegrees = ((_rotationDegrees + (90 * (delta > 0 ? 1 : -1))) % 360);
                Matrix matrix = new Matrix();
                matrix.postRotate(_rotationDegrees);
                TouchImageView.super.setImageBitmap(Bitmap.createBitmap(_orig, 0, 0, _orig.getWidth(), _orig.getHeight(), matrix, true));
                _rotationTimestampDebounce = System.currentTimeMillis();
            }
        }
    };


    @SuppressWarnings("WeakerAccess")
    static class RotationGestureDetector {
        interface Listener {
            public void onRotate(float delta);
        }

        private float _rotation;
        private Listener _listener;

        public RotationGestureDetector(Listener listener) {
            _listener = listener;
        }

        private float handleRotationEvent(MotionEvent event) {
            double dx = (event.getX(0) - event.getX(1));
            double dy = (event.getY(0) - event.getY(1));
            double rad = Math.atan2(dy, dx);
            return (float) Math.toDegrees(rad);
        }

        public void onTouch(MotionEvent e) {
            if (e.getPointerCount() != 2) {
                return;
            }
            if (e.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN) {
                _rotation = handleRotationEvent(e);
            }

            float delta = handleRotationEvent(e) - _rotation;
            _rotation += delta;
            _listener.onRotate(delta);
        }

    }
}