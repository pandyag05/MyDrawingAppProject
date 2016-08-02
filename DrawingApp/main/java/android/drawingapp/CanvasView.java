package android.drawingapp;

/**
 * Created by Pandya Family on 1/14/2016.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.HashMap;

public class CanvasView extends View{

    private static final float TouchTorerance = 10;
    private Canvas bitmapDrawView;
    private Bitmap bitmap;
    private Paint paintLine;
    private Paint screenBitmap;
    private HashMap<Integer, Path> pathMap;
    private HashMap<Integer, Point> currentPointMap;

    public CanvasView(Context context, AttributeSet attrs){
        super(context, attrs);
        init();
    }
    public void init(){
        screenBitmap = new Paint();

        paintLine = new Paint();
        paintLine.setAntiAlias(true);
        paintLine.setColor(Color.BLACK);
        paintLine.setStyle(Paint.Style.STROKE);
        paintLine.setStrokeWidth(5);
        paintLine.setStrokeCap(Paint.Cap.ROUND);
        pathMap = new HashMap<Integer, Path>();
        currentPointMap = new HashMap<Integer, Point>();
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh){

        bitmap = Bitmap.createBitmap(getWidth(),getHeight(), Bitmap.Config.ARGB_8888);
        bitmapDrawView = new Canvas(bitmap);
        bitmap.eraseColor(Color.WHITE);
    }
    public void clear(){
        pathMap.clear();
        currentPointMap.clear();
        bitmap.eraseColor(Color.WHITE);
        invalidate();
    }

    public void setDrawingColor(int color){

        paintLine.setColor(color);
    }
    public int getDrawingColor(){

        return paintLine.getColor();
    }
    public void setLineWidth(int Width){
        paintLine.setStrokeWidth(Width);
    }
    public int getLineWidth(){
        return (int) paintLine.getStrokeWidth();
    }
    @Override
    protected void onDraw(Canvas canvas){
        canvas.drawBitmap(bitmap, 0, 0, screenBitmap);

        for(Integer key : pathMap.keySet())
            canvas.drawPath(pathMap.get(key), paintLine);
    }
    private void touchStart(float x, float y, int lineID){
        Path path;
        Point point;

        if(pathMap.containsKey(lineID)){
            path = pathMap.get(lineID);
            path.reset();
            point = currentPointMap.get(lineID);
        }else{
            path = new Path();
            pathMap.put(lineID, path);
            point = new Point();
            currentPointMap.put(lineID, point);
        }

        path.moveTo(x, y);

        point.x = (int)x;
        point.y = (int)y;

    }
    private void touchMove(MotionEvent event){
        for(int i = 0; i < event.getPointerCount(); i++){
            int ponterID = event.getPointerId(i);
            int pointerIndex = event.findPointerIndex(ponterID);
            if(pathMap.containsKey(ponterID)){
                float newX = event.getX(pointerIndex);
                float newY = event.getY(pointerIndex);

                Path path = pathMap.get(ponterID);
                Point point = currentPointMap.get(ponterID);

                float deltaX = Math.abs(newX - point.x);
                float deltaY = Math.abs(newY-point.y);

                if(deltaX >= TouchTorerance || deltaY >= TouchTorerance){
                    path.quadTo(point.x, point.y, (newX+point.x)/2, (newY+point.y)/2 );

                    point.x = (int)newX;
                    point.y = (int)newY;
                }
            }
        }
    }
    private void touchEnd(int lineId){

        Path path = pathMap.get(lineId);
        bitmapDrawView.drawPath(path, paintLine);
        path.reset();
    }
    @Override
    public boolean onTouchEvent(MotionEvent event){

        int action = event.getActionMasked();
        int actionIndex = event.getActionIndex();
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN){
            touchStart(event.getX(actionIndex), event.getY(actionIndex), event.getPointerId(actionIndex));
        }else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP){
            touchEnd(event.getPointerId(actionIndex));
        }else{
            touchMove(event);
        }

        invalidate();
        return true;

    }

}
