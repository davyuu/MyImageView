package com.davyuu.myimageview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by David Yu on 2016-07-24.
 */
public class MyImageView extends ImageView{
    private static final float MAX_ZOOM = 3;
    private static final float MIN_ZOOM = 1;

    Matrix matrix = new Matrix();

    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    static final int CLICK = 3;
    int mode = NONE;

    PointF last = new PointF();
    PointF start = new PointF();
    float minScale = 1f;
    float maxScale = 4f;
    float[] m;

    float redundantXSpace, redundantYSpace;
    float width, height;
    float saveScaleX = 1f;
    float saveScaleY = 1f;
    float saveScaleRatio = 1f;
    float right, bottom, origWidth, origHeight, imageWidth, imageHeight;

    ScaleGestureDetector mScaleDetector;
    Context context;

    OnTouchListener touchListener = new OnTouchListener()
    {

        @Override
        public boolean onTouch(View v, MotionEvent event)
        {
            mScaleDetector.onTouchEvent(event);

            matrix.getValues(m);
            float x = m[Matrix.MTRANS_X];
            float y = m[Matrix.MTRANS_Y];
            PointF curr = new PointF(event.getX(), event.getY());

            switch (event.getAction())
            {
                //when one finger is touching
                //set the mode to DRAG
                case MotionEvent.ACTION_DOWN:
                    last.set(event.getX(), event.getY());
                    start.set(last);
                    mode = DRAG;
                    break;
                //when two fingers are touching
                //set the mode to ZOOM
                case MotionEvent.ACTION_POINTER_DOWN:
                    last.set(event.getX(), event.getY());
                    start.set(last);
                    mode = ZOOM;
                    break;
                //when a finger moves
                //If mode is applicable move image
                case MotionEvent.ACTION_MOVE:
                    //if the mode is ZOOM or
                    //if the mode is DRAG and already zoomed
                    if (mode == ZOOM || (mode == DRAG && (saveScaleX > minScale || saveScaleY > minScale)))
                    {
                        float deltaX = curr.x - last.x;// x difference
                        float deltaY = curr.y - last.y;// y difference
                        float scaleWidth = Math.round(origWidth * saveScaleX);// width after applying current scale
                        float scaleHeight = Math.round(origHeight * saveScaleY);// height after applying current scale
                        //if scaleWidth is smaller than the views width
                        //in other words if the image width fits in the view
                        //limit left and right movement
                        if (scaleWidth < width)
                        {
                            deltaX = 0;
                            if (y + deltaY > 0)
                                deltaY = -y;
                            else if (y + deltaY < -bottom)
                                deltaY = -(y + bottom);
                        }
                        //if scaleHeight is smaller than the views height
                        //in other words if the image height fits in the view
                        //limit up and down movement
                        else if (scaleHeight < height)
                        {
                            deltaY = 0;
                            if (x + deltaX > 0)
                                deltaX = -x;
                            else if (x + deltaX < -right)
                                deltaX = -(x + right);
                        }
                        //if the image doesnt fit in the width or height
                        //limit both up and down and left and right
                        else
                        {
                            if (x + deltaX > 0)
                                deltaX = -x;
                            else if (x + deltaX < -right)
                                deltaX = -(x + right);

                            if (y + deltaY > 0)
                                deltaY = -y;
                            else if (y + deltaY < -bottom)
                                deltaY = -(y + bottom);
                        }
                        //move the image with the matrix
                        matrix.postTranslate(deltaX, deltaY);
                        //set the last touch location to the current
                        last.set(curr.x, curr.y);
                    }
                    break;
                //first finger is lifted
                case MotionEvent.ACTION_UP:
                    mode = NONE;
                    int xDiff = (int) Math.abs(curr.x - start.x);
                    int yDiff = (int) Math.abs(curr.y - start.y);
                    if (xDiff < CLICK && yDiff < CLICK)
                        performClick();
                    break;
                // second finger is lifted
                case MotionEvent.ACTION_POINTER_UP:
                    mode = NONE;
                    break;
            }

//            limitZoom(matrix);
//            limitDrag(matrix);

            setImageMatrix(matrix);
            invalidate();
            return true;
        }
    };

    private void limitZoom(Matrix m) {

        float[] values = new float[9];
        m.getValues(values);
        float scaleX = values[Matrix.MSCALE_X];
        float scaleY = values[Matrix.MSCALE_Y];
        if(scaleX > MAX_ZOOM) {
            scaleX = MAX_ZOOM;
        } else if(scaleX < MIN_ZOOM) {
            scaleX = MIN_ZOOM;
        }

        if(scaleY > MAX_ZOOM) {
            scaleY = MAX_ZOOM;
        } else if(scaleY < MIN_ZOOM) {
            scaleY = MIN_ZOOM;
        }

        values[Matrix.MSCALE_X] = scaleX;
        values[Matrix.MSCALE_Y] = scaleY;
        m.setValues(values);
    }


    private void limitDrag(Matrix m) {

        float[] values = new float[9];
        m.getValues(values);
        float transX = values[Matrix.MTRANS_X];
        float transY = values[Matrix.MTRANS_Y];
        float scaleX = values[Matrix.MSCALE_X];
        float scaleY = values[Matrix.MSCALE_Y];
//--- limit moving to left ---
        float minX = (-width + 0) * (scaleX-1);
        float minY = (-height + 0) * (scaleY-1);
//--- limit moving to right ---
        float maxX=minX+width*(scaleX-1);
        float maxY=minY+height*(scaleY-1);
        if(transX>maxX){transX = maxX;}
        if(transX<minX){transX = minX;}
        if(transY>maxY){transY = maxY;}
        if(transY<minY){transY = minY;}
        values[Matrix.MTRANS_X] = transX;
        values[Matrix.MTRANS_Y] = transY;
        m.setValues(values);
    }

    public MyImageView(Context context, AttributeSet attr)
    {
        super(context, attr);
        super.setClickable(true);
        this.context = context;
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        matrix.setTranslate(1f, 1f);
        m = new float[9];
        setImageMatrix(matrix);
        setScaleType(ScaleType.MATRIX);
        setOnTouchListener(touchListener);
    }

    public void setMaxZoom(float x)
    {
        maxScale = x;
    }

    @Override
    public void setImageBitmap(Bitmap bm)
    {
        super.setImageBitmap(bm);
        imageWidth = bm.getWidth();
        imageHeight = bm.getHeight();
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener
    {

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector)
        {
            mode = ZOOM;
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector)
        {
            float mScaleFactor = detector.getScaleFactor();
            float origScaleX = saveScaleX;
            float origScaleY = saveScaleY;
            saveScaleX *= mScaleFactor;
            saveScaleY *= mScaleFactor;
            if (saveScaleX > maxScale)
            {
                saveScaleX = maxScale;
                saveScaleY = maxScale * saveScaleRatio;
                mScaleFactor = maxScale / origScaleX;
            }
            else if (saveScaleY < minScale)
            {
                saveScaleY = minScale;
                saveScaleX = minScale / saveScaleRatio;
                mScaleFactor = minScale / origScaleY;
            }
            right = width * saveScaleX - width - (2 * redundantXSpace * saveScaleX);
            bottom = height * saveScaleY - height - (2 * redundantYSpace * saveScaleY);
            if (origWidth * saveScaleX <= width || origHeight * saveScaleY <= height)
            {
                matrix.postScale(mScaleFactor, mScaleFactor, width / 2, height / 2);
                if (mScaleFactor < 1)
                {
                    matrix.getValues(m);
                    float x = m[Matrix.MTRANS_X];
                    float y = m[Matrix.MTRANS_Y];
                    if (mScaleFactor < 1)
                    {
                        if (Math.round(origWidth * saveScaleX) < width)
                        {
                            if (y < -bottom)
                                matrix.postTranslate(0, -(y + bottom));
                            else if (y > 0)
                                matrix.postTranslate(0, -y);
                        }
                        else
                        {
                            if (x < -right)
                                matrix.postTranslate(-(x + right), 0);
                            else if (x > 0)
                                matrix.postTranslate(-x, 0);
                        }
                    }
                }
            }
            else
            {
                matrix.postScale(mScaleFactor, mScaleFactor, detector.getFocusX(), detector.getFocusY());
                matrix.getValues(m);
                float x = m[Matrix.MTRANS_X];
                float y = m[Matrix.MTRANS_Y];
                if (mScaleFactor < 1) {
                    if (x < -right)
                        matrix.postTranslate(-(x + right), 0);
                    else if (x > 0)
                        matrix.postTranslate(-x, 0);
                    if (y < -bottom)
                        matrix.postTranslate(0, -(y + bottom));
                    else if (y > 0)
                        matrix.postTranslate(0, -y);
                }
            }
            return true;
        }
    }

    @Override
    protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
        //Fit to screen.
        float scale;
        float scaleX =  width / imageWidth;
        float scaleY = height / imageHeight;
        scale = scaleX;
        matrix.setScale(scale, scale);
        setImageMatrix(matrix);
        saveScaleX = 1f;
        saveScaleY = scaleX / scaleY;
        saveScaleRatio = scaleX / scaleY;

        // Center the image
        redundantYSpace = height - (scaleY * imageHeight) ;
        redundantXSpace = width - (scaleX * imageWidth);
        redundantYSpace /= 2;
        redundantXSpace /= 2;

        matrix.postTranslate(redundantXSpace, redundantYSpace);

        origWidth = width - 2 * redundantXSpace;
        origHeight = height - 2 * redundantYSpace;
        right = width * saveScaleX - width - (2 * redundantXSpace * saveScaleX);
        bottom = height * saveScaleY - height - (2 * redundantYSpace * saveScaleY);
        setImageMatrix(matrix);
    }
}
