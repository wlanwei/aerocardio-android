package com.uteamtec.heartcool.views.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.uteamtec.heartcool.R;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by liulingfeng on 9/19/15.
 * Rebuilding by wd
 */
public class EcgView extends SurfaceView implements SurfaceHolder.Callback {

    private final static int STAT_GO = 1;
    private final static int STAT_PAUSE = 2;
    private final static int STAT_QUIT = 3;

    private volatile int stat = STAT_PAUSE;

    private final static int DEFAULT_FPS = 100;
    private final static int DEFAULT_SCALE = 10;
    private final static int DEFAULT_BASELINE = 50;

    public final static int DRAWTYPE_1 = 1;
    public final static int DRAWTYPE_3 = 3;

    //    public final static int DEFAULT_PAINT_COLOR = Color.argb(255,113,227,142);
    private final static int DEFAULT_PAINT_COLOR = 0xFFFFFFFF;
    private final static float DEFAULT_PAINT_STROKE_WIDTH = 6.0f;

    public final static int DEFAULT_BGCOLOR = Color.WHITE;

    //    private final static int COLOR = 0xFF46d387;
    private final static int COLOR = 0xFF1AA1AA;

    private BlockingQueue<Float> data = null;
    private boolean isResetData = false;

    private int fps;
    //    private int scale;
    private int baseline;
    private int drawType;

    private Paint paint;
    private DrawThread drawThread;

    public EcgView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (isInEditMode()) {
            return;
        }
        setBackgroundColor(Color.TRANSPARENT);
        setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSPARENT);
        getHolder().addCallback(this);

        TypedArray customSettings = context.obtainStyledAttributes(attrs, R.styleable.EcgView);
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(customSettings.getColor(R.styleable.EcgView_paint_color, DEFAULT_PAINT_COLOR));
        paint.setStrokeWidth(customSettings.getDimension(R.styleable.EcgView_paint_strokewidth, DEFAULT_PAINT_STROKE_WIDTH));
        fps = customSettings.getInteger(R.styleable.EcgView_fps, DEFAULT_FPS);
//        scale = customSettings.getInteger(R.styleable.EcgView_scale, DEFAULT_SCALE);
        baseline = customSettings.getInteger(R.styleable.EcgView_baseline, DEFAULT_BASELINE);

//        this.setBackgroundColor(customSettings.getColor(R.styleable.EcgView_background_color, DEFAULT_BGCOLOR));

        customSettings.recycle();

        resetDrawType(DRAWTYPE_1);
    }

    public void resetDrawType(int type) {
        this.drawType = type;
        this.isResetData = false;
    }

    public int getDrawType() {
        return drawType;
    }

    public void setFps(int fps) {
        this.fps = fps;
    }

    public void setScale(int scale) {
//        if (scale > 10000) {
//            this.scale = 10000;
//        } else if (scale <= 0) {
//            this.scale = 0;
//        } else {
//            this.scale = scale;
//        }
    }

    public void setBaseline(int baseline) {
        if (baseline > 100) {
            this.baseline = 100;
        } else if (baseline < 0) {
            this.baseline = 0;
        } else {
            this.baseline = baseline;
        }
    }

    synchronized public void bindData(BlockingQueue<Float> data) {
        this.data = data;
        this.isResetData = true;
    }

    synchronized public void resetData(int len) {
        if (len <= 0 || this.isResetData) {
            return;
        }
        this.data = new ArrayBlockingQueue<>(len * getDrawType());
        for (int m = len * getDrawType(); m > 0; m--) {
            this.data.add((float) 0);
        }
        this.isResetData = true;
    }

    synchronized public void putData(int[] arr) {
        if (arr == null || arr.length == 0) {
            return;
        }
        this.isResetData = false;
        try {
            for (int i : arr) {
                this.data.take();
                this.data.put((float) i);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    synchronized private Object[] readData() {
        if (this.data != null && this.data.size() > 0) {
            return this.data.toArray();
        }
        return null;
    }

    private void createDraw() {
        if (drawThread != null) {
            drawThread.quit();
        }
        drawThread = new DrawThread(this);
        drawThread.start();
    }

    public void resumeDraw() {
        stat = STAT_GO;
        if (drawThread != null) {
            drawThread.go();
        }
    }

    public void pauseDraw() {
        stat = STAT_PAUSE;
        if (drawThread != null) {
            drawThread.pause();
        }
    }

    private void quitDraw() {
        stat = STAT_QUIT;
        if (drawThread != null) {
            drawThread.quit();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        createDraw();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        switch (stat) {
            case STAT_GO:
                resumeDraw();
                break;
            case STAT_PAUSE:
                pauseDraw();
                break;
            case STAT_QUIT:
                quitDraw();
                break;
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        quitDraw();
    }

    private class DrawThread extends Thread {

        private volatile int stat = STAT_PAUSE;

        private EcgView ecgView = null;

        private DrawThread(EcgView ecgView) {
            this.ecgView = ecgView;
        }

        @Override
        public void run() {
            while (ecgView != null) {
                try {
                    Thread.sleep(1000 / ecgView.fps);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (ecgView == null) {
                    return;
                }

                switch (stat) {
                    case STAT_QUIT:
                        return;
                    case STAT_PAUSE:
                        continue;
                    case STAT_GO:
                        break;
                }

                Object[] array = readData();
                if (array == null || array.length == 0) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }

                Canvas canvas = ecgView.getHolder().lockCanvas();
                if (canvas == null) {
                    continue;
                }

                int width = canvas.getWidth();
                int height = canvas.getHeight();

                Float[] dataArray = new Float[array.length];

                if (drawType == DRAWTYPE_1) {
//                                L.i("<UI> drawing 1 channel ecg");
                    float maxV = 0.0f;
                    float minV = 0.0f;
                    float avgV = 0.0f;
                    for (int m = 0; m < array.length; m++) {
                        if (array[m] == null) {
                            continue;
                        }
                        dataArray[m] = (Float) array[m];
                        maxV = dataArray[m].floatValue() > maxV ? dataArray[m].floatValue() : maxV;
                        minV = dataArray[m].floatValue() > minV ? minV : dataArray[m].floatValue();
//                                    avgV += dataArray[m];
                    }

//                                avgV = avgV / dataArray.length;
                    int dataLen = dataArray.length;

//                                float range = Math.abs(maxV) > Math.abs(minV) ? Math.abs(maxV) : Math.abs(minV);
//                                float scale = height / 2 / range;
                    float range = maxV - minV;

                    avgV = range / 2 + minV;
                    float scale = height * 0.6f / range;


                    float xStep = width / (float) dataArray.length;
                    float dy = height * baseline / 100.0f;
                    float xStart = 0;
                    float yStart = (dataArray[0] - avgV) * scale + dy;
                    float xStop;
                    float yStop;

//                            L.i("<UI> <ECGVIEW> height = " + Integer.toString(height) + " range = " + Float.toString(range) + " scale = " + Float.toString(scale) + " dy = " + Float.toString(dy));

                    //clear previous canvas by canvas.drawPaint
                    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                    canvas.drawPaint(paint);
                    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
                    canvas.drawColor(COLOR);

                    for (int m = 1; m < dataArray.length; m++) {
                        //down sampling the data to speed up the drawing
                        xStop = xStart + xStep;
                        yStop = -(dataArray[m] - avgV) * scale + dy;
                        canvas.drawLine(xStart, yStart, xStop, yStop, paint);
                        xStart = xStop;
                        yStart = yStop;
                    }
                } else if (drawType == DRAWTYPE_3) {
//                                L.i("<UI> drawing 3 channel ecg");
                    int baselines[] = {17, 50, 83};

                    float maxV[] = {((Float) array[0]).floatValue(), ((Float) array[1]).floatValue(), ((Float) array[2]).floatValue()};
                    float minV[] = {((Float) array[0]).floatValue(), ((Float) array[1]).floatValue(), ((Float) array[2]).floatValue()};
                    float avg[] = {0.0f, 0.0f, 0.0f};

                    for (int m = 0; m < array.length - 3; m += 3) {
                        if (array[m] == null || array[m + 1] == null || array[m + 2] == null) {
                            break;
                        }

                        dataArray[m] = (Float) array[m];
                        maxV[0] = dataArray[m].floatValue() > maxV[0] ? dataArray[m].floatValue() : maxV[0];
                        minV[0] = dataArray[m].floatValue() > minV[0] ? minV[0] : dataArray[m].floatValue();

                        dataArray[m + 1] = (Float) array[m + 1];
                        maxV[1] = dataArray[m + 1].floatValue() > maxV[1] ? dataArray[m + 1].floatValue() : maxV[1];
                        minV[1] = dataArray[m + 1].floatValue() > minV[1] ? minV[1] : dataArray[m + 1].floatValue();

                        dataArray[m + 2] = (Float) array[m + 2];
                        maxV[2] = dataArray[m + 2].floatValue() > maxV[2] ? dataArray[m + 2].floatValue() : maxV[2];
                        minV[2] = dataArray[m + 2].floatValue() > minV[2] ? minV[2] : dataArray[m + 2].floatValue();

//                                    avg[0] += dataArray[m];
//                                    avg[1] += dataArray[m+1];
//                                    avg[2] += dataArray[m+2];
                    }
//                                avg[0] = avg[0] / dataArray.length / 3;
//                                avg[1] = avg[1] / dataArray.length / 3;
//                                avg[2] = avg[2] / dataArray.length / 3;

                    int dataLen = dataArray.length / 3;

                    float range[] = new float[3];
//                                range[0] = Math.abs(maxV[0]) > Math.abs(minV[0]) ? Math.abs(maxV[0]) : Math.abs(minV[0]);
//                                range[1] = Math.abs(maxV[1]) > Math.abs(minV[1]) ? Math.abs(maxV[1]) : Math.abs(minV[1]);
//                                range[2] = Math.abs(maxV[2]) > Math.abs(minV[2]) ? Math.abs(maxV[2]) : Math.abs(minV[2]);
                    range[0] = maxV[0] - minV[0];
                    range[1] = maxV[1] - minV[1];
                    range[2] = maxV[2] - minV[2];

                    avg[0] = range[0] / 2 + minV[0];
                    avg[1] = range[1] / 2 + minV[1];
                    avg[2] = range[2] / 2 + minV[2];

                    float scale[] = {height * 0.33f * 0.80f / range[0], height * 0.33f * 0.80f / range[1], height * 0.33f * 0.80f / range[2]};

//                                L.i("<UI> scale = " + Float.toString(scale[0]) + " " + Float.toString(scale[1]) + " " + Float.toString(scale[2]) );
//                                L.i("<UI> range = " + Float.toString(range[0]) + " " + Float.toString(range[1]) + " " + Float.toString(range[2]));
//                                L.i("<UI> avg = " + Float.toString(avg[0]) + " " + Float.toString(avg[1]) + " " + Float.toString(avg[2]));
//                                L.i("<UI> maxV = " + Float.toString(maxV[0]) + " " + Float.toString(maxV[1]) + " " + Float.toString(maxV[2]));
//                                L.i("<UI> minV = " + Float.toString(minV[0]) + " " + Float.toString(minV[1]) + " " + Float.toString(minV[2]));

                    float xStep = width / (float) dataLen;
                    float dy[] = {height * 0.17f, height * 0.5f, height * 0.83f};
                    float xStart[] = {0.0f, 0.0f, 0.0f};
                    float yStart[] = {-(dataArray[0] - avg[0]) * scale[0] + dy[0], -(dataArray[1] - avg[1]) * scale[1] + dy[1], -(dataArray[2] - avg[2]) * scale[2] + dy[2]};

//                                L.i("<UI> is null = " + Boolean.toString(scale == null) + " " + Boolean.toString(dataArray == null) + " " + Boolean.toString(dy == null));
                    float xStop[] = new float[3];
                    float yStop[] = new float[3];

//                                L.i("<UI> dy = " + Float.toString(dy[0]) + " " + Float.toString(dy[1]) + " " + Float.toString(dy[2]));
                    //clear previous canvas by canvas.drawPaint
                    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                    canvas.drawPaint(paint);
                    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
                    canvas.drawColor(COLOR);

                    for (int m = 3; m < dataArray.length - 3; m += 3) {
                        if (dataArray[m] == null || dataArray[m + 1] == null || dataArray[m + 2] == null) {
                            break;
                        }
                        xStop[0] = xStart[0] + xStep;
                        yStop[0] = -(dataArray[m].floatValue() - avg[0]) * scale[0] + dy[0];
                        canvas.drawLine(xStart[0], yStart[0], xStop[0], yStop[0], paint);
                        xStart[0] = xStop[0];
                        yStart[0] = yStop[0];

                        xStop[1] = xStart[1] + xStep;
                        yStop[1] = -(dataArray[m + 1].floatValue() - avg[1]) * scale[1] + dy[1];
                        canvas.drawLine(xStart[1], yStart[1], xStop[1], yStop[1], paint);
                        xStart[1] = xStop[1];
                        yStart[1] = yStop[1];

                        xStop[2] = xStart[2] + xStep;
                        yStop[2] = -(dataArray[m + 2].floatValue() - avg[2]) * scale[2] + dy[2];
                        canvas.drawLine(xStart[2], yStart[2], xStop[2], yStop[2], paint);
                        xStart[2] = xStop[2];
                        yStart[2] = yStop[2];
//                                    L.i("<UI> max Y = " + Float.toString((maxV[2] - avg[2]) * scale[2])+ " height = " + Float.toString(height));
//                                    L.i("<UI> min Y = " + Float.toString((minV[2] - avg[2]) * scale[2])+ " height = " + Float.toString(height));
                    }
                }
                ecgView.getHolder().unlockCanvasAndPost(canvas);
            }
        }

        private void go() {
            stat = STAT_GO;
        }

        private void pause() {
            stat = STAT_PAUSE;
        }

        private void quit() {
            stat = STAT_QUIT;
        }
    }
}
