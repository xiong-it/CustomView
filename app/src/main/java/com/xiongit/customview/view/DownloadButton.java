package com.xiongit.customview.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.TextView;

import com.xiongit.customview.R;

/**
 * Created by michael on 2016/8/23.
 * 下载用的带进度的Button
 * github:https://github.com/xiong-it
 * csdn:http://blog.csdn.net/xiong_it
 */
public class DownloadButton extends TextView {

    private static final String TAG = DownloadButton.class.getSimpleName();

    private int mProgress = 0;// 当前进度
    private int mMaxProgress = 100;// 默认最大值

    private DownloadState mState = DownloadState.STOPPED;// 当前状态

    private Paint mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int mRadius;// 背景圆角半径
    private int mBackgroundColor;
    private int mProgressColor;// 进度条颜色

    private int mProgressTextColor;// 文字进度颜色
    private RectF mProgressRectf;// button的边缘
    private String mDefaultText;// 默认文字

    private String mProgressText;// 进度|状态 文字
    Context context;
    private LinearGradient mProgressBgGradient;
    private float mProgressPercent;
    private LinearGradient mProgressTextGradient;
    private int mTextCoverColor = Color.WHITE;
    private int mTextErrorColor;
    private int mTextWaitingColor;
    private int mTextFinishedColor;

    private OnDownloadStateChangedListener mStateChangedListener;

    public enum DownloadState {
        STOPPED,// 暂停中
        STARTED,// 下载中
        WAITING,// 排队等待中
        ERROR,// 下载失败
        FINISHED// 下载已完成
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DownloadButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context, attrs);
    }

    public DownloadButton(Context context) {
        super(context);
        this.context = context;
        initView(context, null);
    }

    public DownloadButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public DownloadButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet set) {
        TypedArray ta = context.obtainStyledAttributes(set, R.styleable.DownloadButton);
        mProgressColor = ta.getColor(R.styleable.DownloadButton_progress_color, Color.parseColor("#555555"));
        mProgressTextColor = ta.getColor(R.styleable.DownloadButton_progress_text_color, Color.GRAY);
        mProgress = ta.getInteger(R.styleable.DownloadButton_download_progress, 0);
        mMaxProgress = ta.getInteger(R.styleable.DownloadButton_maxprogress, 100);
        setMaxProgress(mMaxProgress);
        mRadius = ta.getDimensionPixelSize(R.styleable.DownloadButton_progress_radius, 6);
        mDefaultText = ta.getString(R.styleable.DownloadButton_progress_default_text);
        setButtonText(mDefaultText);
        ta.recycle();

        //设置背景画笔
        mProgressPaint = new Paint();
        mProgressPaint.setAntiAlias(true);
        mProgressPaint.setStyle(Paint.Style.FILL);

        //设置文字画笔
        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(getTextSize());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            //解决文字有时候画不出问题
            setLayerType(LAYER_TYPE_SOFTWARE, mTextPaint);
        }
    }

    /**
     * 设置下载回调监听
     *
     * @param stateChangedListener
     */
    public void setOnDownloadStateChangedListener(OnDownloadStateChangedListener stateChangedListener) {
        mStateChangedListener = stateChangedListener;
    }

    /**
     * 设置下载进度
     *
     * @param progress
     */
    public synchronized void setProgress(int progress) {
        if (progress > mMaxProgress || progress < 0) {
            return;
        }
        mProgress = progress;
        invalidateDraw();
    }

    /**
     * 获取当前进度
     *
     * @return
     */
    public int getProgress() {
        return mProgress;
    }

    /**
     * 设置按钮上显示的文字
     *
     * @param s
     */
    public synchronized void setButtonText(String s) {
        mProgressText = s;
        invalidateDraw();
    }

    /**
     * 设置按钮上显示的文字
     *
     * @param stringResId
     */
    public synchronized void setButtonText(int stringResId) {
        setButtonText(getResources().getString(stringResId));
    }

    /**
     * 设置按钮上显示的文字
     *
     * @return
     */
    public String getButtonText() {
        return mProgressText;
    }

    /**
     * 获取按钮上的文字
     *
     * @return
     */
    @Override
    public CharSequence getText() {
        return getButtonText();
    }

    /**
     * 设置当前下载状态
     *
     * @param state
     */
    public synchronized void setDownloadState(DownloadState state) {
        if (mStateChangedListener != null && state != mState) {
            mStateChangedListener.onStateChanged(state);
        }
        mState = state;
        switch (mState) {
            case WAITING:
                downloadWaiting();
                break;

            case STARTED:
                downloading();
                break;

            case ERROR:
                setProgress(0);
                downloadFailure();
                break;

            case STOPPED:
                downloadPause();
                break;

            case FINISHED:
                downloadFinished();
                break;

            default:
                setProgress(0);
                setButtonText(mDefaultText);
                break;
        }
    }

    public DownloadState getDownloadState() {
        return mState;
    }

    private void downloadWaiting() {
        setProgress(0);
        this.setBackgroundResource(R.drawable.btn_gray);
        mTextWaitingColor = ContextCompat.getColor(getContext(), R.color.text_gray_general);
        setButtonText("等待中");
    }

    private void downloading() {
        this.setBackgroundResource(R.drawable.btn_download_game);
        invalidateDraw();
    }

    private void downloadPause() {
        this.setBackgroundResource(R.drawable.btn_download_game);
        setButtonText("暂停中");
    }

    private void downloadFailure() {
        setProgress(0);
        this.setBackgroundResource(R.drawable.btn_gray);
        mTextErrorColor = ContextCompat.getColor(getContext(), R.color.text_vrgoods_price_red);
        setButtonText("点击重试");
    }

    private void downloadFinished() {
        setButtonText("点击打开");
        this.setBackgroundResource(R.drawable.btn_download_game);
        mTextFinishedColor = ContextCompat.getColor(getContext(), R.color.text_login);
        setProgress(0);
    }

    @Override
    public void setTextColor(int color) {
        super.setTextColor(color);
        mProgressTextColor = color;
    }

    /**
     * 设置最大进度值
     *
     * @param max
     */
    public synchronized void setMaxProgress(int max) {
        if (mMaxProgress == 0) {
            throw new IllegalArgumentException("非法参数：最大进度必须大于0.max must be greater than 0.");
        }
        mMaxProgress = max;
    }

    public int getMaxProgress() {
        return mMaxProgress;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawProgress(canvas);
        drawProgressText(canvas);
    }

    /**
     * 绘制按钮上的文字：进度/提示语
     *
     * @param canvas
     */
    private void drawProgressText(Canvas canvas) {

        int p = (int) (((float) mProgress / (float) mMaxProgress) * 100);

        String mCurrentText = mProgressText;
        if (mState == DownloadState.STARTED) {
            mCurrentText = p + "%";
        }

        final float y = canvas.getHeight() / 2 - (mTextPaint.descent() / 2 + mTextPaint.ascent() / 2);
        final float textWidth = mTextPaint.measureText(mCurrentText.toString());
        switch (mState) {
            case STARTED:
            case STOPPED:

                //进度条压过距离
                float coverlength = getMeasuredWidth() * mProgressPercent;
                //开始渐变指示器
                float indicator1 = getMeasuredWidth() / 2 - textWidth / 2;
                //结束渐变指示器
                float indicator2 = getMeasuredWidth() / 2 + textWidth / 2;
                //文字变色部分的距离
                float coverTextLength = textWidth / 2 - getMeasuredWidth() / 2 + coverlength;
                float textProgress = coverTextLength / textWidth;
                if (coverlength <= indicator1) {
                    mTextPaint.setShader(null);
                    mTextPaint.setColor(mProgressTextColor);
                } else if (indicator1 < coverlength && coverlength <= indicator2) {
                    mProgressTextGradient = new LinearGradient((getMeasuredWidth() - textWidth) / 2, 0, (getMeasuredWidth() + textWidth) / 2, 0,
                            new int[]{mTextCoverColor, mProgressTextColor},
                            new float[]{textProgress, textProgress + 0.001f},
                            Shader.TileMode.CLAMP);
                    mTextPaint.setColor(mProgressTextColor);
                    mTextPaint.setShader(mProgressTextGradient);
                } else {
                    mTextPaint.setShader(null);
                    mTextPaint.setColor(mTextCoverColor);
                }
                canvas.drawText(mCurrentText.toString(), (getMeasuredWidth() - textWidth) / 2, y, mTextPaint);
                break;

            case ERROR:
                mTextPaint.setShader(null);
                mTextPaint.setColor(mTextErrorColor);
                canvas.drawText(mCurrentText.toString(), (getMeasuredWidth() - textWidth) / 2, y, mTextPaint);
                break;
            case WAITING:
                mTextPaint.setShader(null);
                mTextPaint.setColor(mTextWaitingColor);
                canvas.drawText(mCurrentText.toString(), (getMeasuredWidth() - textWidth) / 2, y, mTextPaint);
                break;
            case FINISHED:
                mTextPaint.setShader(null);
                mTextPaint.setColor(mTextFinishedColor);
                canvas.drawText(mCurrentText.toString(), (getMeasuredWidth() - textWidth) / 2, y, mTextPaint);
                break;
            default:
                mTextPaint.setShader(null);
                mTextPaint.setColor(mProgressTextColor);
                canvas.drawText(mCurrentText.toString(), (getMeasuredWidth() - textWidth) / 2, y, mTextPaint);
                break;
        }
    }

    private void drawProgress(Canvas canvas) {
        switch (mState) {
            case STARTED:
            case STOPPED:
                mProgressPaint.setStyle(Paint.Style.FILL);
                mProgressPaint.setColor(mProgressColor);

                mProgressRectf = new RectF();
                if (mRadius == 0) {
                    mRadius = getMeasuredHeight() / 2;
                }
                mProgressRectf.left = 2;
                mProgressRectf.top = 2;
                mProgressRectf.right = getMeasuredWidth() - 2;
                mProgressRectf.bottom = getMeasuredHeight() - 2;
                mProgressPercent = mProgress / (mMaxProgress + 0f);
                mProgressBgGradient = new LinearGradient(0, 0, getMeasuredWidth(), 0,
                        new int[]{mProgressColor, mBackgroundColor},
                        new float[]{mProgressPercent, mProgressPercent + 0.001f},
                        Shader.TileMode.CLAMP
                );
                mProgressPaint.setColor(mProgressColor);
                mProgressPaint.setShader(mProgressBgGradient);
                canvas.drawRoundRect(mProgressRectf, mRadius, mRadius, mProgressPaint);
                break;
            default:
                break;
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);

        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

        // 处理wrap_content/AT_MOST情况
        if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(240, 100);
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(240, heightSpecSize);
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSpecSize, 100);
        }

    }

    private synchronized boolean isOnMainThread() {
        return Thread.currentThread() == Looper.getMainLooper().getThread();
    }

    /**
     * 刷新view视图
     */
    private synchronized void invalidateDraw() {
        if (isOnMainThread()) {
            invalidate();
        } else {
            postInvalidate();
        }
    }

    public interface OnDownloadStateChangedListener {
        void onStateChanged(DownloadState state);
    }
}
