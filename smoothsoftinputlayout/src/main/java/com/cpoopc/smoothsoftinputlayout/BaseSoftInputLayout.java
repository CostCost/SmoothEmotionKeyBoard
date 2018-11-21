package com.cpoopc.smoothsoftinputlayout;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseSoftInputLayout extends LinearLayout implements View.OnClickListener {

    public final static int SHOW_KEYBOARD = 0x01;
    public final static int SHOW_EMOTION = 0x10;
    public final static int SHOW_OTHER = 0x11;

    private View rootView;
    private boolean mIsKeyboardShow;
    private View btnKeyBoard;
    // emotionView,otherView容器
    private View container;
    private int showWhat;
    private int keyboardHeight;
    private int minOtherBoardHeight = 300;
    private List<View> showViewList;
    private Map<View,ViewHolder> viewMapping;
    private View frame;
    private EditText editText;
    private int mNavigationBarHeight = -1;
    private int mHiddenHeight;
    private int mShownHeight;
    private int mLastCoverHeight;
    private int mLastHitBottom;

    public BaseSoftInputLayout(Context context) {
        super(context);
        init(context, null, -1, -1);
    }

    public BaseSoftInputLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, -1, -1);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public BaseSoftInputLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, -1);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BaseSoftInputLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * 初始化
     *
     * @param ctx
     * @param attrs
     * @param defStyleAttr
     * @param defStyleRes
     */
    protected void init(Context ctx, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        viewMapping = new HashMap<>();
        showViewList = new ArrayList<>();

        // 交给子类的用来加载布局的方法
        inflateView();
        final Context context = getContext();
        if (context instanceof Activity) {
            rootView = ((Activity) context).getWindow().getDecorView();
        } else {
            rootView = this;
        }

        // 从子类中获取各个控件
        btnKeyBoard = getBtnKeyBoard();
        editText = getEditText();
        container = getContainer();
        frame = getFrame();

        // 监听 ViewTree
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                detectKeyBoardState();
                if (mIsKeyboardShow) {
                    if (showWhat == SHOW_KEYBOARD) {
                        hideAllViewExceptKeyBoard();
                    }
                    showView(container);
                } else {
                    if (showWhat == 0) {
                        hideView(container);
                    } else {
                        showView(container);
                    }
                }
            }
        });
    }

    protected abstract void inflateView();
    protected abstract View getContainer();
    protected abstract View getFrame();
    public abstract EditText getEditText();
    protected abstract View getBtnKeyBoard();

    /**
     * 检测键盘的弹出状态
     */
    private void detectKeyBoardState() {
        Rect visibleRect = new Rect();
        rootView.getWindowVisibleDisplayFrame(visibleRect);

        Rect hitRect = new Rect();
        rootView.getHitRect(hitRect);

        int coverHeight = hitRect.bottom - visibleRect.bottom;
        if (mLastCoverHeight == coverHeight && mLastHitBottom == hitRect.bottom) { // fix魅族动态显示/隐藏navigationbar没有及时响应
            return;
        }
        mLastHitBottom = hitRect.bottom;
        int deltaCoverHeight = coverHeight - mLastCoverHeight;
        mLastCoverHeight = coverHeight;
        if (coverHeight > mNavigationBarHeight) {
            if ((deltaCoverHeight == mNavigationBarHeight || deltaCoverHeight == -mNavigationBarHeight) && mIsKeyboardShow) {
                // 华为显示/隐藏navigationBar
                mHiddenHeight += deltaCoverHeight;
            }
            mShownHeight = coverHeight - mHiddenHeight;
            int height = mShownHeight;
            int overMinHeight = 0;
            if (height < minOtherBoardHeight) {
                overMinHeight = minOtherBoardHeight - height;
                height = minOtherBoardHeight;
            }
            if (keyboardHeight != height) {
                keyboardHeight = height;
                container.getLayoutParams().height = height;
                container.requestLayout();
            }
            mIsKeyboardShow = true;
            showWhat = SHOW_KEYBOARD;
            refreshFrame(visibleRect.bottom + mShownHeight + overMinHeight);
        } else {
            if ((deltaCoverHeight == mNavigationBarHeight || deltaCoverHeight == -mNavigationBarHeight) && !mIsKeyboardShow) {
                // 华为显示/隐藏navigationBar
                mHiddenHeight += deltaCoverHeight;
            }
            if (coverHeight != mHiddenHeight) {
                mHiddenHeight = coverHeight;
            }
            refreshFrame(visibleRect.bottom);
            mIsKeyboardShow = false;
            if (showWhat == SHOW_KEYBOARD) {
                showWhat = 0;
            }
        }
    }

    /**
     * 刷新frame高度
     *
     * @param bottom
     */
    private void refreshFrame(int bottom) {
        Rect rect = new Rect();
        frame.getHitRect(rect);
        int[] location = new int[2];
        frame.getLocationInWindow(location);
        int height = bottom - rect.top - location[1];
        if (height != frame.getLayoutParams().height) {
            frame.getLayoutParams().height = height;
            frame.requestLayout();
        }
    }

    protected void add2MappingMap(View view, int SHOW_TYPE, View showView) {
        viewMapping.put(view, new ViewHolder(SHOW_TYPE, showView));
    }

    protected void add2ShowViewList(View view) {
        showViewList.add(view);
    }

    @Override
    public void onClick(View v) {
        if (v == btnKeyBoard) {
            // 点击键盘的事件是专门处理的
            if (showWhat == SHOW_KEYBOARD) {
                showWhat = 0;
                hideSoftInput();
            } else if (showWhat == 0) {
                showWhat = SHOW_KEYBOARD;
                showSoftInput();
                showView(container);
            } else {
                showWhat = SHOW_KEYBOARD;
                hideAllViewExceptKeyBoard();
                showSoftInput();
            }
        } else {
            ViewHolder viewHolder = viewMapping.get(v);
            if (viewHolder != null) {
                int show_type = viewHolder.getSHOW_TYPE();
                View showView = viewHolder.getShowView();
                // 点击表情
                if (showWhat == show_type) {
                    // 隐藏表情,隐藏layout
                    showWhat = 0;
                    hideView(showView);
                    hideView(container);
                } else if (showWhat == SHOW_KEYBOARD) {
                    showWhat = show_type;
                    hideSoftInput();
                    showView(showView);
                    showView(container);
                } else {
                    showWhat = show_type;
                    hideAllViewExceptKeyBoard();
                    showView(showView);
                    showView(container);
                }
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mNavigationBarHeight == -1) {
            frame.getLayoutParams().height = getMeasuredHeight();
            mNavigationBarHeight = getNavigationBarHeight(getContext());
        }
    }

    private void hideSoftInput() {
        if(editText == null) return;
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void showSoftInput() {
        if(editText == null) return;
        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }

    private void hideView(View view) {
        view.setVisibility(GONE);
    }

    /**
     * 隐藏除了键盘外的view
     */
    private void hideAllViewExceptKeyBoard() {
        for (int i = 0; i < showViewList.size(); i++) {
            hideView(showViewList.get(i));
        }
    }

    public void hideKeyBoardView() {
        showWhat = 0;
        hideSoftInput();
        hideView(container);
    }

    private void showView(View view) {
        view.setVisibility(VISIBLE);
    }

    private static int getNavigationBarHeight(Context context) {
        int navigationBarHeight = 0;
        try {
            Resources rs = context.getResources();
            int id = rs.getIdentifier("navigation_bar_height", "dimen", "android");
            if (id > 0 ) {
                navigationBarHeight = rs.getDimensionPixelSize(id);
            }
        } catch (Exception e) { /* default 0 */ }
        return navigationBarHeight;
    }

    /**
     * 设置最小高度(除了键盘外的最小高度)
     *
     * @param minOtherBoardHeight
     */
    public void setMinOtherBoardHeight(int minOtherBoardHeight) {
        this.minOtherBoardHeight = minOtherBoardHeight;
    }

    public static class ViewHolder {
        private int SHOW_TYPE;
        private View showView;

        public ViewHolder(int SHOW_TYPE, View showView) {
            this.SHOW_TYPE = SHOW_TYPE;
            this.showView = showView;
        }

        public int getSHOW_TYPE() {
            return SHOW_TYPE;
        }

        public View getShowView() {
            return showView;
        }
    }
}
