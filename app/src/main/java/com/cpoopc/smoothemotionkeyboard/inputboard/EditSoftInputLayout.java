package com.cpoopc.smoothemotionkeyboard.inputboard;/**
 * Created by Administrator on 2015-09-01.
 */

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.cpoopc.smoothemotionkeyboard.R;
import com.cpoopc.smoothemotionkeyboard.emotion.data.HahaEmotion;
import com.cpoopc.smoothemotionkeyboard.emotion.view.EmotionPager;
import com.cpoopc.smoothsoftinputlayout.BaseSoftInputLayout;

/**
 * User: cpoopc
 * Date: 2015-09-01
 * Time: 00:18
 */
public class EditSoftInputLayout extends BaseSoftInputLayout {

    private View btnKeyBoard;

    // emotionView,otherView容器
    private View container;

    private View btnEmotion;
    private EmotionPager emotionView;

    private View btnOther;
    private View otherView;

    private View frame;

    private EditText editText;

    public EditSoftInputLayout(Context context) {
        super(context);
    }

    public EditSoftInputLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public EditSoftInputLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public EditSoftInputLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void inflateView() {
        // 获取自定义的布局控件
        View layout = LayoutInflater.from(getContext()).inflate(R.layout.edit_softinput_layout, this, true);
        container = layout.findViewById(R.id.container);
        frame = layout.findViewById(R.id.frame);
        editText = layout.findViewById(R.id.edittext);

        // 设置三个按钮对应的控件
        setupKeyboardView(layout);
        setupEmotionView(layout);
        setupOtherView(layout);
    }

    /**
     * 设置“键盘”相关的控件的逻辑
     *
     * @param layout
     */
    private void setupKeyboardView(View layout) {
        btnKeyBoard = layout.findViewById(R.id.btnKeyBoard);
        btnKeyBoard.setOnClickListener(this);
    }

    /**
     * 设置“其他”按钮相关的逻辑
     *
     * @param layout
     */
    private void setupOtherView(View layout) {
        btnOther = layout.findViewById(R.id.btnOther);
        otherView = layout.findViewById(R.id.otherView);

        // 为控件添加单击事件监听，这个接口的实现在顶层的类中
        btnOther.setOnClickListener(this);

        // 将控件加入到待显示列表中
        add2ShowViewList(otherView);

        // 加入控件加入到映射表中，主要是“按钮-控件类型-控件”的映射关系
        add2MappingMap(btnOther, SHOW_OTHER, otherView);
    }

    /**
     * 设置“表情”相关的逻辑，基本的逻辑与上面的方法相同
     *
     * @param layout
     */
    private void setupEmotionView(View layout) {
        btnEmotion = layout.findViewById(R.id.btnEmotion);
        btnEmotion.setOnClickListener(this);
        emotionView = layout.findViewById(R.id.emotionPager);
        emotionView.bindData(HahaEmotion.DATA);
        add2ShowViewList(emotionView);
        add2MappingMap(btnEmotion, SHOW_EMOTION, emotionView);// btnEmotion-(SHOW_EMOTION-emotionView)
    }

    @Override
    protected View getContainer() {
        return container;
    }

    @Override
    protected View getFrame() {
        return frame;
    }

    @Override
    public EditText getEditText() {
        return editText;
    }

    @Override
    protected View getBtnKeyBoard() {
        return btnKeyBoard;
    }

}
