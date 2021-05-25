package com.citaq.citaqfactory;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.citaq.util.AnimationUtil;
import com.citaq.util.SoundManager;
import com.citaq.view.PaintView;
import com.citaq.view.PaintViewMy;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Presentation;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class PresentationTestActivity extends FullActivity
        implements OnClickListener, OnTouchListener,
        OnGestureListener{
    Context mContext;
    private ImageView btnClear,btnSave,btnBack;
    private PaintViewMy mPaintView;
    private LayoutInflater mInfater;
    private View touchView;
    private View touchbarView;

    private AnimationUtil mAnimationUtil;

    private static Handler sHandler;

    DifferentDislay mPresentation;
    DisplayManager mDisplayManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

		/*requestWindowFeature(Window.FEATURE_NO_TITLE);
		  getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		*/

		/*Intent intent = new Intent();
		intent.setAction("com.android.action.hide_navigationbar");
		sendBroadcast(intent);*/
        mContext = this;
        mInfater = LayoutInflater.from(this);
        touchView = mInfater.inflate(R.layout.activity_touch, null);
        this.setContentView(touchView);
        initView(); //

        dubleShow();
    }


    private void initView(){
        btnClear=(ImageView)this.findViewById(R.id.button_clear);
        btnClear.setOnClickListener(this);

        btnSave=(ImageView)this.findViewById(R.id.button_save);
        btnSave.setOnClickListener(this);

        btnBack=(ImageView)this.findViewById(R.id.button_back);
        btnBack.setOnClickListener(this);

        mPaintView=(PaintViewMy)this.findViewById(R.id.paintview);

        touchbarView = this.findViewById(R.id.touchBar);
        // 为布局绑定监听
        mPaintView.setOnTouchListener(this);

        mAnimationUtil =new AnimationUtil();

    }

    private void dubleShow(){
        Display[]  displays;//屏幕数组

        mDisplayManager = (DisplayManager)mContext.getSystemService(Context.DISPLAY_SERVICE);

        displays =mDisplayManager.getDisplays();

        mPresentation =new DifferentDislay(getApplicationContext(),displays[1]);//displays[1]是副屏

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){//6.0+
            mPresentation.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        }else {
            mPresentation.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }
        // mPresentation.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

        mPresentation.show();
    }


    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        SoundManager.playSound(0, false,1);
        switch(v.getId())
        {
            case R.id.button_clear:
                mPaintView.clear();
                break;
            case R.id.button_save:
                String name = new SimpleDateFormat("yyyyMMddHHmm", Locale.SIMPLIFIED_CHINESE).format(new Date());
                name = Environment.getExternalStorageDirectory()+"/"+name+".png";
                mPaintView.storeImageToFile(name);
                break;
            case R.id.button_back:
                if(mPresentation != null)
                    mPresentation.dismiss();
                this.finish();

                break;
            default:
                break;
        }

    }

    @Override
    public boolean onDown(MotionEvent arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    private GestureDetector detector = new GestureDetector(this);
    // 限制最小移动像素
    private int FLING_MIN_DISTANCE = 20;
    private int FLING_MAX_DISTANCE = 350;
    // 定义的Toast提示框显示时间
    private int TIME_OUT = 1000;

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                           float velocityY) {
        // X轴的坐标位移大于FLING_MIN_DISTANCE，且移动速度大于FLING_MIN_VELOCITY个像素/秒
        if (e1.getX() - e2.getX() > FLING_MIN_DISTANCE) {
            // 向左滑动
//            Toast.makeText(this, "向左滑动", TIME_OUT).show();
        } else if (e2.getX() - e1.getX() > FLING_MIN_DISTANCE) {
            // 向右滑动
//            Toast.makeText(this, "向右滑动", TIME_OUT).show();
            if(e1.getX()< 50 && e2.getX() - e1.getX()<FLING_MAX_DISTANCE){

                mAnimationUtil.startAnimation(touchbarView, AnimationUtil.show);

            }
//        	 Toast.makeText(this, "--"+(int)(e2.getX() - e1.getX()), TIME_OUT).show();
        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
                            float arg3) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onShowPress(MotionEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onSingleTapUp(MotionEvent arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean onTouch(View arg0, MotionEvent event) {
        detector.onTouchEvent(event);
        return false;
    }

    private class DifferentDislay extends Presentation {
        Context mOuterContext;
        RelativeLayout mRelativeLayout;
        TextView tv_show;
        private int i = 0;
        public DifferentDislay(Context outerContext, Display display) {

            super(outerContext, display);

            //TODOAuto-generated constructor stub
            mOuterContext = outerContext;
        }

        @Override

        protected void onCreate(Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);

            setContentView(R.layout.different_presentation);
            mRelativeLayout = findViewById(R.id.mRelativeLayout);
            tv_show = (TextView) findViewById(R.id.tv_show);
            Button bt_test = (Button) findViewById(R.id.bt_test);
            Button bt_upperLeft = (Button) findViewById(R.id.bt_upperLeft);
            Button lowerRight = (Button) findViewById(R.id.lowerRight);

            bt_test.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tv_show.setText("");
                    Toast.makeText(mOuterContext, "Hello！！", Toast.LENGTH_SHORT).show();
/*                    AlertDialog.Builder builder = new AlertDialog.Builder(mOuterContext).setIcon(R.drawable.ic_launcher).setTitle("Clear")
                            .setMessage("Erase all data(factory reset)").setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                   // doMasterClear();
                                }
                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                    builder.create().show();*/
                    if(i>colors.length-1) {
                        i=0;
                    }else{
                        mRelativeLayout.setBackgroundColor(colors[i++]);
                    }
                }
            });

            bt_upperLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tv_show.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
                    tv_show.setText("I am Upper left corner.");
                }
            });

            lowerRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tv_show.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
                    tv_show.setText("I am Lower right corner.");
                }
            });
        }

        private int[] colors={
//		0x00000000,
                0xff000000,//黑
                0xffffffff,//白
                0xff0000ff,//蓝
                0xff00ff00,//绿
                0xffff0000,//红
//		0xff00ffff,
//		0xffff00ff,
                0xffffff00,
        };

    }

}
