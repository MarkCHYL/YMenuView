package com.yanzhikai.ymenuview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;

import java.util.ArrayList;

/**
 * @author Yanzhikai
 * Description: 一个可以弹出收回菜单栏的自定义View，带有动画效果
 */

public class YMenuView2 extends RelativeLayout {
    public final static String TAG = "ymenuview";

    private Context mContext;
    private Button mYMenuButton;

    private int drawableIds[] = {R.drawable.zero,R.drawable.one, R.drawable.two, R.drawable.three,
            R.drawable.four, R.drawable.five,R.drawable.six,R.drawable.seven};
    private ArrayList<OptionButton2> optionButtonList;
    //    private ArrayList<OnShowDisappearListener> listenerList;
    //“选项”占格个数
    private int optionPositionCount = 8;
    //“选项”占格列数
    private int optionColumns = 1;
    private int[] banArray = {};
    private ArrayList<Boolean> banList ;
    //MenuButton宽高
    private int mYMenuButtonWidth = 80, mYMenuButtonHeight = 80;
    //OptionButton宽高
    private int mYOptionButtonWidth = 80, mYOptionButtonHeight = 80;
    //MenuButton的X方向边距和Y方向边距（距离父ViewGroup边界）
    private int mYMenuToParentXMargin = 50, mYMenuToParentYMargin = 50;
    //第一个OptionButton的X方向间隔和Y方向间隔
    private int mYOptionYMargin = 15, mYOptionXMargin = 15;
    //第一个OptionButton的X方向边距和Y方向边距（距离父ViewGroup边界）
    private int mYOptionToParentYMargin = 160, mYOptionToParentXMargin = 50;
    private @DrawableRes int mMenuButtonBackGroundId = R.drawable.setting;
    private @DrawableRes int mOptionsBackGroundId = R.drawable.null_drawable;
    private boolean isShowMenu = false;
    private Animation menuOpenAnimation, menuCloseAnimation;
    private Animation.AnimationListener animationListener;
    private int mOptionSD_AnimationMode = OptionButton2.FROM_BUTTON_TOP;
    private int mOptionSD_AnimationDuration = 600;
    private OnOptionsClickListener mOnOptionsClickListener;

    private YMenuSetting mSetting;

    public YMenuView2(Context context) {
        super(context);
        init(context);
    }

    public YMenuView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttr(context, attrs);
        init(context);
    }

    public YMenuView2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttr(context, attrs);
        init(context);
    }

    private void initAttr(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.YMenuView, 0, 0);
        mYMenuButtonWidth = typedArray.getDimensionPixelSize(R.styleable.YMenuView_menuButtonWidth, mYMenuButtonWidth);
        mYMenuButtonHeight = typedArray.getDimensionPixelSize(R.styleable.YMenuView_menuButtonHeight, mYMenuButtonHeight);
        mYOptionButtonWidth = typedArray.getDimensionPixelSize(R.styleable.YMenuView_optionButtonWidth, mYOptionButtonWidth);
        mYOptionButtonHeight = typedArray.getDimensionPixelSize(R.styleable.YMenuView_optionButtonHeight, mYOptionButtonHeight);
        mYMenuToParentXMargin = typedArray.getDimensionPixelSize(R.styleable.YMenuView_menuButtonRightMargin, mYMenuToParentXMargin);
        mYMenuToParentYMargin = typedArray.getDimensionPixelSize(R.styleable.YMenuView_menuButtonBottomMargin, mYMenuToParentYMargin);
        optionPositionCount = typedArray.getInteger(R.styleable.YMenuView_optionPositionCounts, optionPositionCount);
        optionColumns = typedArray.getInteger(R.styleable.YMenuView_optionColumns, optionColumns);
        mYOptionToParentYMargin = typedArray.getDimensionPixelSize(R.styleable.YMenuView_optionToMenuBottomMargin, mYOptionToParentYMargin);
        mYOptionToParentXMargin = typedArray.getDimensionPixelSize(R.styleable.YMenuView_optionToMenuRightMargin, mYOptionToParentXMargin);
        mYOptionYMargin = typedArray.getDimensionPixelSize(R.styleable.YMenuView_optionVerticalMargin, mYOptionYMargin);
        mYOptionXMargin = typedArray.getDimensionPixelSize(R.styleable.YMenuView_optionHorizontalMargin, mYOptionXMargin);
        mMenuButtonBackGroundId = typedArray.getResourceId(R.styleable.YMenuView_menuButtonBackGround, mMenuButtonBackGroundId);
        mOptionsBackGroundId = typedArray.getResourceId(R.styleable.YMenuView_optionsBackGround,R.drawable.null_drawable);
        mOptionSD_AnimationMode = typedArray.getInt(R.styleable.YMenuView_sd_animMode,mOptionSD_AnimationMode);
        mOptionSD_AnimationDuration = typedArray.getInt(R.styleable.YMenuView_sd_duration,mOptionSD_AnimationDuration);
        isShowMenu = typedArray.getBoolean(R.styleable.YMenuView_isShowMenu,isShowMenu);
    }

    private void init(Context context) {
        mContext = context;
        mSetting = new DefaultYMenuSetting(this);
        initMenuAnim();
        setMenuButton();

        //在获取到宽高参数之后再进行初始化
        ViewTreeObserver viewTreeObserver = getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (getWidth() != 0 && getHeight() != 0) {
                    try {
                        Log.d(TAG, "onGlobalLayout: ");
                        setOptionButtons();
                        setOptionBackGrounds(mOptionsBackGroundId);
                        setOptionsImages(drawableIds);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //设置完后立刻注销，不然会不断回调，浪费很多资源
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }

            }
        });


    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.d(TAG, "onMeasure: ");
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.d(TAG, "onLayout: ");
        super.onLayout(changed, l, t, r, b);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d(TAG, "onDraw: ");
        super.onDraw(canvas);
    }

    //初始化MenuButton的点击动画
    private void initMenuAnim() {
        menuOpenAnimation = AnimationUtils.loadAnimation(mContext, R.anim.rotate_open);
        menuCloseAnimation = AnimationUtils.loadAnimation(mContext, R.anim.rotate_close);
        animationListener = new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mYMenuButton.setClickable(false);

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mYMenuButton.setClickable(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        };
        menuOpenAnimation.setDuration(mOptionSD_AnimationDuration);
        menuCloseAnimation.setDuration(mOptionSD_AnimationDuration);
        menuOpenAnimation.setAnimationListener(animationListener);
        menuCloseAnimation.setAnimationListener(animationListener);
    }


    private void initBan() throws Exception {
        banList = new ArrayList<>(optionPositionCount);
        for (int i = 0; i < optionPositionCount; i++){
            banList.add(false);
        }
        for (Integer i:banArray){
            if (i >= 0 && i < optionPositionCount){
                Log.d(TAG, "initBan: i " + i);
                banList.set(i,true);
            }else {
                throw new Exception("Ban数组设置不合理，含有负数或者超出范围");
            }
            Log.d(TAG, "initBan: size " + banList.size());
        }
    }

    private void setMenuButton() {
        mYMenuButton = new Button(mContext);
//        setMenuPosition(mYMenuButton);
        mSetting.setMenuPosition(mYMenuButton);
        //设置MenuButton的大小位置
//        LayoutParams layoutParams = new LayoutParams(mYMenuButtonWidth, mYMenuButtonHeight);
//        layoutParams.setMarginEnd(mYMenuToParentXMargin);
//        layoutParams.bottomMargin = mYMenuToParentYMargin;
//        layoutParams.addRule(ALIGN_PARENT_RIGHT);
//        layoutParams.addRule(ALIGN_PARENT_BOTTOM);
//        //生成ID
//        mYMenuButton.setId(generateViewId());
//
//        mYMenuButton.setLayoutParams(layoutParams);
        //设置打开关闭事件
        mYMenuButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isShowMenu) {
                    showMenu();
                } else {
                    closeMenu();
                }
            }
        });
        mYMenuButton.setBackgroundResource(mMenuButtonBackGroundId);
        addView(mYMenuButton);
    }

    private void setMenuPosition(View menuButton){
        LayoutParams layoutParams = new LayoutParams(mYMenuButtonWidth, mYMenuButtonHeight);
        layoutParams.setMarginEnd(mYMenuToParentXMargin);
        layoutParams.bottomMargin = mYMenuToParentYMargin;
        layoutParams.addRule(ALIGN_PARENT_RIGHT);
        layoutParams.addRule(ALIGN_PARENT_BOTTOM);
        menuButton.setLayoutParams(layoutParams);
    }

    //设置选项按钮
    private void setOptionButtons() throws Exception {
        optionButtonList = new ArrayList<>(optionPositionCount);
        initBan();
        for (int i = 0; i < optionPositionCount; i++) {
//            if (isBan && banArray.length > 0) {
//                //Ban判断
//                if (i > banArray[n] || banArray[n] > optionPositionCount - 1) {
//                    throw new Exception("Ban数组设置不合理，含有负数、重复数字或者超出范围");
//                } else if (i == banArray[n]) {
//                    if (n < banArray.length - 1) {
//                        n++;
//                    }else {
//                        isBan = false;
//                    }
//                    continue;
//                }
//            }
            if (!banList.get(i)) {
                OptionButton2 optionButton = new OptionButton2(mContext,mSetting);
//                setOptionPosition(optionButton, mYMenuButton, i);
                mSetting.setOptionPosition(optionButton, mYMenuButton, i);
                addView(optionButton);
                optionButtonList.add(optionButton);
            }
        }
    }



    public void setOptionPosition(OptionButton2 optionButton, View menuButton, int index){
            Log.d(TAG, "setOptionPosition: " + menuButton.getX());
            //设置动画模式和时长
            optionButton.setSD_Animation(mOptionSD_AnimationMode);
            optionButton.setDuration(mOptionSD_AnimationDuration);
            int btnId = generateViewId();
            optionButton.setId(btnId);

            RelativeLayout.LayoutParams layoutParams = new LayoutParams(mYOptionButtonWidth, mYOptionButtonHeight);

            //计算OptionButton的位置
            int position = index % optionColumns;

            layoutParams.rightMargin = mYOptionToParentXMargin
                    + mYOptionXMargin * position
                    + mYOptionButtonWidth * position;

            layoutParams.bottomMargin = mYOptionToParentYMargin
                    + (mYOptionButtonHeight + mYOptionYMargin) * (index / optionColumns);
            layoutParams.addRule(ALIGN_PARENT_BOTTOM);
            layoutParams.addRule(ALIGN_PARENT_RIGHT);

            optionButton.setLayoutParams(layoutParams);
    }



    //设置选项按钮的background
    public void setOptionBackGrounds(@DrawableRes Integer drawableId){
        for (int i = 0; i < optionButtonList.size(); i++) {
            if (drawableId == null){
                optionButtonList.get(i).setBackground(null);
            }else {
                optionButtonList.get(i).setBackgroundResource(drawableId);
            }

        }

    }

    //设置选项按钮的图片资源，顺便设置点击事件
    private void setOptionsImages(int... drawableIds) throws Exception {
        this.drawableIds = drawableIds;
        if (optionPositionCount > drawableIds.length + banArray.length) {
            throw new Exception("Drawable资源数量不足");
        }

        for (int i = 0; i < optionButtonList.size(); i++) {
            optionButtonList.get(i).setOnClickListener(new MyOnClickListener(i));
            if (drawableIds == null){
                optionButtonList.get(i).setImageDrawable(null);
            }else {
                optionButtonList.get(i).setImageResource(drawableIds[i]);
            }

        }
    }

    //弹出菜单
    public void showMenu() {
        if (!isShowMenu) {
            for (OptionButton2 button : optionButtonList) {
                button.onShow();
            }
            if (menuOpenAnimation != null) {
                mYMenuButton.startAnimation(menuOpenAnimation);
            }
            isShowMenu = true;
        }
    }

    //关闭菜单
    public void closeMenu() {
        if (isShowMenu) {
            for (OptionButton2 button : optionButtonList) {
                button.onClose();
            }
            if (menuCloseAnimation != null) {
                mYMenuButton.startAnimation(menuCloseAnimation);
            }
            isShowMenu = false;
        }
    }

    //让OptionButton直接消失，不执行关闭动画
    public void disappearMenu() {
        if (isShowMenu) {
            for (OptionButton2 button : optionButtonList) {
                button.onDisappear();
            }
            isShowMenu = false;
        }
    }


    //清除所有View，用于之后刷新
    private void cleanMenu(){
        removeAllViews();
        if (optionButtonList != null) {
            optionButtonList.clear();
        }
        isShowMenu = false;
    }

    /*
     * 对整个YMenuView进行重新初始化，用于在做完一些设定之后刷新
     */
    public void refresh(){
        cleanMenu();
        init(mContext);
    }

    public Button getYMenuButton() {
        return mYMenuButton;
    }

    public int getOptionColumns() {
        return optionColumns;
    }

    public int[] getDrawableIds() {
        return drawableIds;
    }

    public int getMenuButtonBackGroundId() {
        return mMenuButtonBackGroundId;
    }

    public int getOptionsBackGroundId() {
        return mOptionsBackGroundId;
    }

    public int getOptionSD_AnimationDuration() {
        return mOptionSD_AnimationDuration;
    }

    public @OptionButton2.SD_Animation int getOptionSD_AnimationMode() {
        return mOptionSD_AnimationMode;
    }

    public int getYMenuToParentYMargin() {
        return mYMenuToParentYMargin;
    }

    public int getYMenuToParentXMargin() {
        return mYMenuToParentXMargin;
    }

    public int getYMenuButtonWidth() {
        return mYMenuButtonWidth;
    }

    public int getYMenuButtonHeight() {
        return mYMenuButtonHeight;
    }

    public int getYOptionButtonWidth() {
        return mYOptionButtonWidth;
    }

    public int getYOptionButtonHeight() {
        return mYOptionButtonHeight;
    }

    public int getYOptionXMargin() {
        return mYOptionXMargin;
    }

    public int getYOptionYMargin() {
        return mYOptionYMargin;
    }

    public int getYOptionToParentYMargin() {
        return mYOptionToParentYMargin;
    }

    public int getYOptionToParentXMargin() {
        return mYOptionToParentXMargin;
    }

    public ArrayList<OptionButton2> getOptionButtonList() {
        return optionButtonList;
    }


    public void setOnOptionsClickListener(OnOptionsClickListener onOptionsClickListener) {
        this.mOnOptionsClickListener = onOptionsClickListener;
    }

    private class MyOnClickListener implements OnClickListener {
        private int index;

        public MyOnClickListener(int index) {
            this.index = index;
        }

        @Override
        public void onClick(View v) {
            if (mOnOptionsClickListener != null) {
                mOnOptionsClickListener.onOptionsClick(index);
            }
        }
    }


    /*
     * 下面的set方法需要在View还没有初始化的时候调用，例如Activity的onCreate方法里
     * 如果不在View还没初始化的时候调用，请使用完set这些方法之后调用refresh()方法刷新
     */

    //设置OptionButton的Drawable资源
    public void setOptionDrawableIds(int... drawableIds) {
        this.drawableIds = drawableIds;
    }

    //设置MenuButton弹出菜单选项时候MenuButton自身的动画，默认为顺时针旋转180度，为空则是关闭动画
    public void setMenuOpenAnimation(Animation menuOpenAnimation) {
        menuOpenAnimation.setAnimationListener(animationListener);
        this.menuOpenAnimation = menuOpenAnimation;

    }

    //设置MenuButton收回菜单选项时候MenuButton自身的动画，默认为逆时针旋转180度，为空则是关闭动画
    public void setMenuCloseAnimation(Animation menuCloseAnimation) {
        menuCloseAnimation.setAnimationListener(animationListener);
        this.menuCloseAnimation = menuCloseAnimation;
    }

    //设置禁止放置选项的位置序号，注意不能输入负数、重复数字或者大于等于optionPositionCounts的数，会报错。
    public void setBanArray(int... banArray) {
        this.banArray = banArray;

    }

    //设置“选项”占格个数
    public void setOptionPositionCount(int optionPositionCount) {
        this.optionPositionCount = optionPositionCount;
    }

    //设置一开始的时候是否展开菜单
    public void setIsShowMenu(boolean isShowMenu){
        this.isShowMenu = isShowMenu;
    }

    public void setOptionColumns(int optionColumns) {
        this.optionColumns = optionColumns;
    }


    public void setYMenuButtonWidth(int mYMenuButtonWidth) {
        this.mYMenuButtonWidth = mYMenuButtonWidth;
    }

    public void setYMenuButtonHeight(int mYMenuButtonHeight) {
        this.mYMenuButtonHeight = mYMenuButtonHeight;
    }

    public void setYMenuButtonBottomMargin(int mYMenuButtonBottomMargin) {
        this.mYMenuToParentYMargin = mYMenuButtonBottomMargin;
    }

    public void setYMenuButtonRightMargin(int mYMenuButtonRightMargin) {
        this.mYMenuToParentXMargin = mYMenuButtonRightMargin;
    }

    public void setYOptionButtonWidth(int mYOptionButtonWidth) {
        this.mYOptionButtonWidth = mYOptionButtonWidth;
    }

    public void setYOptionButtonHeight(int mYOptionButtonHeight) {
        this.mYOptionButtonHeight = mYOptionButtonHeight;
    }

    public void setYOptionToParentYMargin(int mYOptionToParentYMargin) {
        this.mYOptionToParentYMargin = mYOptionToParentYMargin;
    }

    public void setYOptionToParentXMargin(int mYOptionToParentXMargin) {
        this.mYOptionToParentXMargin = mYOptionToParentXMargin;
    }

    public void setYOptionXMargin(int mYOptionXMargin) {
        this.mYOptionXMargin = mYOptionXMargin;
    }

    public void setmYOptionYMargin(int mYOptionYMargin) {
        this.mYOptionYMargin = mYOptionYMargin;
    }

    //使用OptionButton里面的静态变量，如OptionButton.FROM_BUTTON_LEFT
    public void setOptionSD_AnimationMode(int optionSD_AnimationMode) {
        this.mOptionSD_AnimationMode = optionSD_AnimationMode;
    }

    public void setOptionSD_AnimationDuration(int optionSD_AnimationDuration) {
        this.mOptionSD_AnimationDuration = optionSD_AnimationDuration;
    }

    public void setMenuButtonBackGroundId(int menuButtonBackGroundId) {
        this.mMenuButtonBackGroundId = menuButtonBackGroundId;
    }

    public void setOptionsBackGroundId(int optionsBackGroundId) {
        this.mOptionsBackGroundId = optionsBackGroundId;
    }

    public void setYMenuSetting(YMenuSetting setting){
        mSetting = setting;
    }


    //用于让用户在外部实现点击事件的接口，index可以区分OptionButton
    public interface OnOptionsClickListener {
        public void onOptionsClick(int index);
    }

    protected interface OnShowDisappearListener {
        public void onShow();

        public void onClose();

        public void onDisappear();
    }
}
