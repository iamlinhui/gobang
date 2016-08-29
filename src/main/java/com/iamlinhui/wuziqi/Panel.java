package com.iamlinhui.wuziqi;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by iamli on 2016-08-28.
 */
public class Panel extends View {

    private int mPanelWidth;
    private float mLineHeigth;
    private int Max_Line = 15;
    private int Max_Count = 5;
    private Paint mPaint = new Paint();

    private Bitmap mWhitePiece;
    private Bitmap mBlackPiece;

    private float ratioPieceOfLineHight = 3 * 1.0f / 4;

    private ArrayList<Point> mWhiteArray = new ArrayList<>();
    private ArrayList<Point> mBlackArray = new ArrayList<>();
    private boolean mIsWhite = true;//标识哪种颜色的正在下棋子；白棋先手

    private boolean gameIsOver;
    private boolean whiteIsWinner;

    public Panel(Context context, AttributeSet attrs) {
        super(context, attrs);

//        setBackgroundColor(0x44FFFFCC);

        init();
    }

    private void init() {
        /*setAntiAlias(true);防锯齿，抗锯齿是依赖于算法的，算法决定抗锯齿的效率，在我们绘制棱角分明
        的图像时，比如一个矩形、一张位图，我们不需要打开抗锯齿。
        setDither(true);防抖动，设定是否使用图像抖动处理，会使绘制出来的图片颜色更加平滑和饱满，
        图像更加清晰。
        paint.setStyle(Paint.Style.STROKE);描边*/
        mPaint.setColor(0x88000000);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);

        mWhitePiece = BitmapFactory.decodeResource(getResources(),R.drawable.stone_w2);
        mBlackPiece = BitmapFactory.decodeResource(getResources(), R.drawable.stone_b1);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        int width = Math.min(widthSize, heightSize);

        if (widthMode == MeasureSpec.UNSPECIFIED){
            width = heightSize;
        }else if (heightMode == MeasureSpec.UNSPECIFIED){
            width = widthSize;
        }

        setMeasuredDimension(width,width);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mPanelWidth = w;
        mLineHeigth = mPanelWidth*1.0f/Max_Line;

        int pieceWidth = (int) (mLineHeigth * ratioPieceOfLineHight);
        mBlackPiece = Bitmap.createScaledBitmap(mBlackPiece,pieceWidth,pieceWidth, false);
        mWhitePiece = Bitmap.createScaledBitmap(mWhitePiece, pieceWidth, pieceWidth, false);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (gameIsOver)return false;

        int action = event.getAction();
        if (action == MotionEvent.ACTION_UP){
            int x = (int) event.getX();
            int y = (int) event.getY();

//            Point point = new Point(x, y);
            Point point = getValidPoint(x ,y);

            if (mWhiteArray.contains(point)||mBlackArray.contains(point)){
                return false;
            }

            if (mIsWhite){
                mWhiteArray.add(point);
            }
            else {
                mBlackArray.add(point);
            }
            invalidate();
            mIsWhite = !mIsWhite;
        }
        return true;
    }

    private Point getValidPoint(int x, int y) {

        return new Point((int) (x / mLineHeigth), (int) (y / mLineHeigth));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawBoard(canvas);
        drawPieces(canvas);
        checkGameIsOver();
    }

    private void checkGameIsOver() {
        boolean whiteWin = checkFiveInLine(mWhiteArray);
        boolean blackWin = checkFiveInLine(mBlackArray);

        if (whiteWin || blackWin){
            gameIsOver = true;
            whiteIsWinner = whiteWin;

            String text = whiteIsWinner ? "白棋获胜":"黑棋获胜";
            Toast.makeText(getContext(), text, Toast.LENGTH_LONG).show();
        }

    }

    private boolean checkFiveInLine(List<Point> points) {
        for (Point point:points){
            int x = point.x;
            int y = point.y;

            boolean win = checkHorizontal(x, y,points);
            if (win)return true;
             win = checkVertical(x, y,points);
            if (win)return true;
             win = checkLeftDiagonal(x, y,points);
            if (win)return true;
             win = checkRightDiagonal(x, y,points);
            if (win)return true;
        }
        return false;
    }

    /*
    判断x,y位置的棋子是否横向五连珠
     */
    private boolean checkHorizontal(int x, int y, List<Point> points) {
        int count = 1;

        //判断左边连珠个数
        //注意循环中i=1；若i=0则会将本身这枚棋子计算三次，所以三字连珠就会提示胜利
        for (int i = 1;i < Max_Count;i++){
            if(points.contains(new Point(x - i,y))){
                count++;
            }
            else break;
        }
        if(count == Max_Count)return true;
        //判断右边连珠个数
        for (int i = 1;i < Max_Count;i++){
            if(points.contains(new Point(x + i,y))){
                count++;
            }
            else break;
        }

        if(count == Max_Count)return true;

        return false;
    }

    /*
    判断x,y位置的棋子是否纵向五连珠
     */
    private boolean checkVertical(int x, int y, List<Point> points) {
        int count = 1;

        //判断上边连珠个数
        for (int i = 1;i < Max_Count;i++){
            if(points.contains(new Point(x ,y - i))){
                count++;
            }
            else break;
        }
        if(count == Max_Count)return true;
        //判断下边连珠个数
        for (int i = 1;i < Max_Count;i++){
            if(points.contains(new Point(x ,y+ i))){
                count++;
            }
            else break;
        }

        if(count == Max_Count)return true;

        return false;
    }


    /*
    判断x,y位置的棋子是否左斜向五连珠
     */
    private boolean checkLeftDiagonal(int x, int y, List<Point> points) {
        int count = 1;

        //判断斜下边连珠个数
        for (int i = 1;i < Max_Count;++i){
            if(points.contains(new Point(x-i ,y+ i))){
                count++;
            }
            else break;
        }
        if(count == Max_Count)return true;
        //判断斜上边连珠个数
        for (int i = 1;i < Max_Count;++i){
            if(points.contains(new Point(x+i ,y- i))){
                count++;
            }
            else break;
        }

        if(count == Max_Count)return true;

        return false;
    }

    /*
    判断x,y位置的棋子是否右斜向五连珠
     */
    private boolean checkRightDiagonal(int x, int y, List<Point> points) {
        int count = 1;

        //判断斜上边连珠个数
        for (int i = 1;i < Max_Count;i++){
            if(points.contains(new Point(x-i ,y- i))){
                count++;
            }
            else break;
        }
        if(count == Max_Count)return true;
        //判断斜下边连珠个数
        for (int i = 1;i < Max_Count;++i){
            if(points.contains(new Point(x+i ,y+ i))){
                count++;
            }
            else break;
        }

        if(count == Max_Count)return true;

        return false;
    }



    private void drawPieces(Canvas canvas) {
        for (int i = 0,n = mWhiteArray.size();i < n;++i){
            Point whitePoint = mWhiteArray.get(i);
            canvas.drawBitmap(mWhitePiece,
                    (whitePoint.x + (1 - ratioPieceOfLineHight) / 2) * mLineHeigth,
                    (whitePoint.y + (1 - ratioPieceOfLineHight) / 2) * mLineHeigth ,null);
        }
        for (int i = 0,n = mBlackArray.size();i < n;++i){
            Point blackPoint = mBlackArray.get(i);
            canvas.drawBitmap(mBlackPiece,
                    (blackPoint.x + (1 - ratioPieceOfLineHight) / 2) * mLineHeigth,
                    (blackPoint.y + (1 - ratioPieceOfLineHight) / 2) * mLineHeigth ,null);
        }

    }

    private void drawBoard(Canvas canvas) {
        int w = mPanelWidth;
        float lineHeight = mLineHeigth;
        for (int i = 0; i < Max_Line; i++) {
            int startX = (int) (lineHeight / 2);
            int endX = (int) (w - lineHeight / 2);
            int y = (int) ((0.5 + i) * lineHeight);
            canvas.drawLine(startX, y, endX, y, mPaint);
            canvas.drawLine(y, startX, y, endX, mPaint);

        }
    }

    public void restart(){
        mWhiteArray.clear();
        mBlackArray.clear();
        gameIsOver = false;
        whiteIsWinner = false;
        invalidate();
        mIsWhite = true;
    }

    public void regret(){
        if(mIsWhite==false&&mWhiteArray.size()>=1){
            mWhiteArray.remove(mWhiteArray.size()-1);
            invalidate();
        }
        else if (mIsWhite&&mBlackArray.size()>=1){
            mBlackArray.remove(mBlackArray.size()-1);
            invalidate();
        }
        mIsWhite = !mIsWhite;
        gameIsOver = false;
    }

    private static final String INSTANCE = "instance";
    private static final String INSTANCE_GAME_OVER = "instance_game_over";
    private static final String INSTANCE_WHITE_ARRAY = "instance_white_array";
    private static final String INSTANCE_BLACK_ARRAY = "instance_black_array";

    @Override
    protected Parcelable onSaveInstanceState() {

        Bundle bundle = new Bundle();
        bundle.putParcelable(INSTANCE, super.onSaveInstanceState());

        bundle.putBoolean(INSTANCE_GAME_OVER, gameIsOver);
        bundle.putParcelableArrayList(INSTANCE_WHITE_ARRAY, mWhiteArray);
        bundle.putParcelableArrayList(INSTANCE_BLACK_ARRAY, mBlackArray);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle){
            Bundle bundle = (Bundle) state;
            gameIsOver = bundle.getBoolean(INSTANCE_GAME_OVER);
            mWhiteArray = bundle.getParcelableArrayList(INSTANCE_WHITE_ARRAY);
            mBlackArray = bundle.getParcelableArrayList(INSTANCE_BLACK_ARRAY);

            super.onRestoreInstanceState(bundle.getParcelable(INSTANCE));

            return;
        }
        super.onRestoreInstanceState(state);
    }
}
