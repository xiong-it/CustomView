package com.xiongit.customview.compositeview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.xiongit.customview.R;

/**
 * Created by Michael on 2016/6/12.
 * 右边带小眼睛的密码输入框
 * github:https://github.com/xiong-it
 * csdn:http://blog.csdn.net/xiong_it
 */
public class PasswordEditText extends LinearLayout {

    /**
     * 密码输入框
     */
    private EditText mPwdEdit;
    /**
     * 小眼睛图片
     */
    private ImageView mEyeImg;
    /**
     * 是否显示密码
     */
    private boolean mShowPwd;
    /**
     * 输入框宽度:px
     */
    private int mEditWidth;
    /**
     * 小眼睛宽度:px
     */
    private int mEyeWidth;
    /**
     * 小眼睛资源id
     */
    private int mEyeResId;
    /**
     * 输入框提示语
     */
    private String mHint;

    private int mDefaultWidth = dp2px(270);
    private int mDefaultHeight = dp2px(40);

    public PasswordEditText(Context context) {
        this(context, null);
    }

    public PasswordEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PasswordEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
        initEvent();
    }

    private void initView(Context context, AttributeSet attrs) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_password_edittext, this, true);
        mPwdEdit = (EditText) view.findViewById(R.id.password_et);
        mPwdEdit.setFocusable(true);
        mPwdEdit.setFocusableInTouchMode(true);

        mEyeImg = (ImageView) view.findViewById(R.id.pwd_eye_view);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PasswordEditText);
        int count = typedArray.getIndexCount();
        for (int i = 0; i < count; i++) {
            int attr = typedArray.getIndex(i);
            switch (attr) {
                case R.styleable.PasswordEditText_edit_width:
                    mEditWidth = typedArray.getDimensionPixelSize(attr, 220);
                    mPwdEdit.setWidth(mEditWidth);
                    break;

                case R.styleable.PasswordEditText_eye_width:
                    mEyeWidth = typedArray.getDimensionPixelSize(attr, 50);
                    mEyeImg.setMaxWidth(mEyeWidth);
                    mEyeImg.setMinimumWidth(mEyeWidth);
                    break;

                case R.styleable.PasswordEditText_eyeIcon:
                    mEyeResId = typedArray.getResourceId(attr, R.drawable.selector_close_eye);
                    mEyeImg.setImageResource(mEyeResId);
                    break;

                case R.styleable.PasswordEditText_hint:
                    mHint = typedArray.getString(attr);
                    setHint(mHint);
                    break;

                case R.styleable.PasswordEditText_passwordVisible:
                    mShowPwd = typedArray.getBoolean(attr, false);
                    setEyeState(mShowPwd);
                    break;

                default:
                    break;
            }
        }
        typedArray.recycle();
    }

    private void initEvent() {
        mEyeImg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mShowPwd) {
                    setEyeState(false);
                } else {
                    setEyeState(true);
                }
            }
        });
    }

    /**
     * 设置眼睛状态,睁眼：显示密码；闭眼：隐藏密码
     *
     * @param openEye 是否显示睁开的眼睛
     */
    public void setEyeState(boolean openEye) {
        if (mEyeImg == null) return;

        mShowPwd = openEye;
        if (openEye) {
            mEyeImg.setImageResource(R.drawable.selector_open_eye);
            showPassword(true);
        } else {
            mEyeImg.setImageResource(R.drawable.selector_close_eye);
            showPassword(false);
        }
    }

    public void setCompoundDrawables(@Nullable Drawable left, @Nullable Drawable top, @Nullable Drawable right, @Nullable Drawable bottom) {
        if (mPwdEdit != null) {
            mPwdEdit.setCompoundDrawables(left, top, right, bottom);
        }
    }

    public void addTextChangedListener(TextWatcher watcher) {
        if (mPwdEdit != null) {
            mPwdEdit.addTextChangedListener(watcher);
        }
    }

    public void removeTextChangedListener(TextWatcher watcher) {
        if (mPwdEdit != null) {
            mPwdEdit.removeTextChangedListener(watcher);
        }
    }

    public void setText(CharSequence content) {
        if (mPwdEdit != null) {
            mPwdEdit.setText(content);
        }
    }

    public Editable getText() {
        if (mPwdEdit == null) {
            return new SpannableStringBuilder();// 相当于返回一个空字符串
        }
        return mPwdEdit.getText();
    }

    public void setHint(CharSequence hint) {
        if (TextUtils.isEmpty(hint)) return;

        if (mPwdEdit != null) {
            mPwdEdit.setHint(hint);
        }
    }

    /**
     * 设置密码显示状态
     *
     * @param show 密码是否显示
     */
    private void showPassword(boolean show) {
        if (mPwdEdit == null) return;

        if (show) {
            mPwdEdit.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        } else {
            mPwdEdit.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }
        mPwdEdit.setSelection(getText().length());
    }

    @Override
    public void setOrientation(int orientation) {
        setOrientation(LinearLayout.HORIZONTAL);
        requestLayout();
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
            setMeasuredDimension(mDefaultWidth, mDefaultHeight);
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(mDefaultWidth, heightSpecSize);
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSpecSize, mDefaultHeight);
        }
    }

    private int dp2px(int dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        float density = displayMetrics.density;
        return (int) (density * dp + 0.5f);
    }

    /**
     * 测量宽
     *
     * @param widthMeasureSpec
     * @return
     */
    @Deprecated
    private int measureWidth(int widthMeasureSpec) {
        int result = 0;

        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);

        if (widthSpecMode == MeasureSpec.EXACTLY) {
            result = widthSpecSize;
        } else {
            result = mDefaultWidth;
            if (widthSpecMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, widthSpecSize);
            }
        }

        return result;
    }

    /**
     * 测量高
     *
     * @param heightMeasureSpec
     * @return
     */
    @Deprecated
    private int measureHeight(int heightMeasureSpec) {
        int result = 0;

        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

        if (heightSpecMode == MeasureSpec.EXACTLY) {
            result = heightSpecSize;
        } else {
            result = mDefaultHeight;
            if (heightSpecMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, heightSpecSize);
            }
        }

        return result;
    }

}
