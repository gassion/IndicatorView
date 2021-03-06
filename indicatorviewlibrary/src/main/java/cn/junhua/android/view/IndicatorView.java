package cn.junhua.android.view;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;


/**
 * @author 林军华
 * 2016年6月3日下午3:31:24
 */
public class IndicatorView extends View {

    /**
     * 可用状态列表
     */
    private final static int[][] STATE_LIST = {
            {-android.R.attr.state_selected, -android.R.attr.state_pressed, -android.R.attr.state_checked, -android.R.attr.state_enabled},
            {android.R.attr.state_selected, android.R.attr.state_pressed, android.R.attr.state_checked, android.R.attr.state_enabled}};
    private final static int DEFAULT_PADDING_TOP_BO = 10;
    private final int defaultWidthHeight;
    private IndicatorViewOnPageChangeListener mIndicatorViewOnPageChangeListener;
    private IndicatorTransformer mIndicatorTransformer;
    private float mPositionOffset;
    private boolean isAnimation = false;
    /**
     * 画笔设置抗锯齿
     */
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    /**
     * 每个绘图单元的个数
     */
    private int mCount;
    /**
     * 被选中绘图单元的索引
     */
    private int mSelect = 0;
    /**
     * 被选中绘图单元的放缩比例
     */
    private float mSelectScale;
    /**
     * 绘图单元的颜色
     */
    private int mColor;
    /**
     * 绘图单元的 Drawable
     */
    private StateListDrawable mUnitDrawable = null;
    /**
     * 绘图单元的Rect
     */
    private Rect mBounds;
    /**
     * 绘图单元的半径
     */
    private float mRadius;
    private float mUnitWidth;
    private float mUnitHeight;
    private float mUnitPadding;
    /**
     * 画笔宽度
     */
    private float mStrokeWidth;
    public IndicatorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IndicatorView(Context context) {
        this(context, null);
    }

    public IndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        defaultWidthHeight = dip2px(context, 10);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.IndicatorView);
        mCount = ta.getInt(R.styleable.IndicatorView_indicator_count, 3);
        mColor = ta.getColor(R.styleable.IndicatorView_indicator_color, Color.RED);
        mRadius = ta.getDimension(R.styleable.IndicatorView_indicator_radius, -1);
        mUnitWidth = ta.getDimension(R.styleable.IndicatorView_indicator_unit_width, -1);
        mUnitHeight = ta.getDimension(R.styleable.IndicatorView_indicator_unit_height, -1);
        mUnitPadding = ta.getDimension(R.styleable.IndicatorView_indicator_padding, dip2px(context, 5));
        Drawable tempDrawable = ta.getDrawable(R.styleable.IndicatorView_indicator_drawable);
        mSelectScale = ta.getFloat(R.styleable.IndicatorView_indicator_select_scale, 1.0f);
        mSelect = ta.getInt(R.styleable.IndicatorView_indicator_select, 0);
        ta.recycle();

        if (tempDrawable instanceof StateListDrawable) {
            mUnitDrawable = (StateListDrawable) tempDrawable;
        }


        setSelect(mSelect);
        mPaint.setColor(mColor);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        handleUnitSize();

        // 得到模式和对应值
        int withMode = MeasureSpec.getMode(widthMeasureSpec);
        int withSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        // 设置默认宽高
        int width = (int) ((mBounds.width() + mUnitPadding * 2) * mCount + getPaddingLeft() + getPaddingRight());
        int height = mBounds.height() + DEFAULT_PADDING_TOP_BO * 2 + getPaddingTop() + getPaddingBottom();

        int w, h;
        if (withMode == MeasureSpec.AT_MOST || withMode == MeasureSpec.UNSPECIFIED) {
            w = width;
        } else {
            w = withSize;
        }
        if (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED) {
            h = height;
        } else {
            h = heightSize;
        }

        setMeasuredDimension(w, h);
    }

    /**
     * 处理尺寸
     */
    private void handleUnitSize() {
        int width, height;
        height = (int) mUnitHeight;
        width = (int) mUnitWidth;

        if (height < 0 && width < 0) {
            height = (int) (mRadius * 2);
            width = (int) (mRadius * 2);
        }

        if (mUnitDrawable != null && height < 0 && width < 0) {
            height = mUnitDrawable.getIntrinsicHeight();
            width = mUnitDrawable.getIntrinsicWidth();
        }
        //默认宽高
        if (height < 0 && width < 0) {
            height = defaultWidthHeight;
            width = defaultWidthHeight;
        }

        mBounds = new Rect(0, 0, width, height);
        if (mUnitDrawable != null) {
            mUnitDrawable.setBounds(mBounds);
        }
        // 设置画笔
        mStrokeWidth = Math.min(mBounds.width(), mBounds.height()) / 20;
        mPaint.setStrokeWidth(mStrokeWidth);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        //处理view的padding
        canvas.translate(getPaddingLeft(), getPaddingTop());
        for (int i = 0; i < mCount; i++) {
            //此处忽略画笔的宽度 移动绘制单元格 并居中
            canvas.save();
            canvas.translate((mUnitPadding * 2 + mBounds.width()) * i + mUnitPadding, DEFAULT_PADDING_TOP_BO);
            if (mUnitDrawable != null) {
                drawDrawableUnit(canvas, !isAnimation && mSelect == i);
            } else {
                drawDefaultUnit(canvas, !isAnimation && mSelect == i);
            }
            canvas.restore();
        }

        drawAnimationUnit(canvas);
        canvas.restore();
    }

    /**
     * 处理动画
     *
     * @param canvas Canvas
     */
    private void drawAnimationUnit(Canvas canvas) {
        if (!isAnimation) return;

        if (mSelect >= 0 && mSelect < mCount) {
            canvas.save();

            float unitWidth = mUnitPadding * 2 + mBounds.width();
            canvas.translate(unitWidth * mSelect + mUnitPadding, DEFAULT_PADDING_TOP_BO);

            //处理动画
            if (mIndicatorTransformer != null) {
                mIndicatorTransformer.transformPage(this, canvas, mSelect, mPositionOffset);
            }

            if (mUnitDrawable != null) {
                drawDrawableUnit(canvas, true);
            } else {
                drawDefaultUnit(canvas, true);
            }
            canvas.restore();
        }
    }

    /**
     * 此时居中
     * 用来绘制特定Drawable指示器单元显示
     */
    private void drawDrawableUnit(Canvas canvas, boolean isSelect) {
        if (isSelect) {
            mUnitDrawable.setState(STATE_LIST[1]);
            canvas.scale(mSelectScale, mSelectScale, mBounds.centerX(), mBounds.centerY());
        } else {
            mUnitDrawable.setState(STATE_LIST[0]);
        }
        mUnitDrawable.draw(canvas);
    }

    /**
     * 此时居中
     * 用来绘制默认指示器单元显示
     */
    private void drawDefaultUnit(Canvas canvas, boolean isSelect) {
        float drawRadius = (Math.min(mBounds.width(), mBounds.height()) - mStrokeWidth) / 2;
        if (isSelect) {
            mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            canvas.drawCircle(mBounds.centerX(), mBounds.centerY(), drawRadius * mSelectScale, mPaint);
        } else {
            mPaint.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(mBounds.centerX(), mBounds.centerY(), drawRadius, mPaint);
        }
    }


    public void setupWithViewPager(ViewPager viewPager) {
        if (mIndicatorViewOnPageChangeListener == null) {
            mIndicatorViewOnPageChangeListener = new IndicatorViewOnPageChangeListener();
        }
        viewPager.removeOnPageChangeListener(mIndicatorViewOnPageChangeListener);
        viewPager.addOnPageChangeListener(mIndicatorViewOnPageChangeListener);
    }

    public void setIndicatorTransformer(IndicatorTransformer indicatorTransformer) {
        isAnimation = indicatorTransformer != null;
        mIndicatorTransformer = indicatorTransformer;
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int color) {
        this.mColor = color;
        mPaint.setColor(mColor);
        invalidate();
    }

    public int getCount() {
        return mCount;
    }

    public void setCount(int count) {
        if (count < 0) {
            this.mCount = 0;
        }
        this.mCount = count;
        invalidate();
    }

    public int getSelect() {
        return mSelect;
    }

    /**
     * 设置选中的unit的index
     *
     * @param select 选中的位置
     */
    public void setSelect(int select) {
        this.mSelect = select;
        invalidate();
    }

    public Rect getUnitBounds() {
        return mBounds;
    }

    public float getUnitPadding() {
        return mUnitPadding;
    }

    public StateListDrawable getUnitDrawable() {
        return mUnitDrawable;
    }

    public void setUnitDrawable(Drawable unitDrawable) {
        if (unitDrawable instanceof StateListDrawable) {
            mUnitDrawable = (StateListDrawable) unitDrawable;
            mUnitDrawable.setBounds(mBounds);
            invalidate();
        }
    }

    public void setRadius(float radius) {
        if (mRadius < 0)
            return;
        this.mRadius = radius;
        // 设置画笔
        mStrokeWidth = mRadius / 10;
        mPaint.setStrokeWidth(mStrokeWidth);
        invalidate();
    }

    public float getSelectScale() {
        return mSelectScale;
    }

    /**
     * @param selectScale
     */
    public void setSelectScale(float selectScale) {
        this.mSelectScale = selectScale;
        invalidate();
    }

    /**
     * 将当前指示器的位置向前移动
     */
    public void next() {
        mSelect = (mSelect + 1) % mCount;
        invalidate();
    }

    /**
     * 将当前指示器的位置向后移动
     */
    public void previous() {
        mSelect = (mSelect - 1 + mCount) % mCount;
        invalidate();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable parcelable = super.onSaveInstanceState();
        SavedState ss = new SavedState(parcelable);
        ss.count = mCount;
        ss.select = mSelect;
        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setCount(ss.count);
        setSelect(ss.select);
    }


    /**
     * 动画接口
     */
    public interface IndicatorTransformer {
        void transformPage(IndicatorView page, Canvas canvas, int position, float positionOffset);
    }

    /**
     * 平移动画
     */
    public static class TranslationIndicatorTransformer implements IndicatorTransformer {

        @Override
        public void transformPage(IndicatorView page, Canvas canvas, int position, float positionOffset) {
            float unitWidth = page.getUnitPadding() * 2 + page.getUnitBounds().width();
            float scrollWidth = unitWidth;
            if (position + 1 >= 0 && position + 1 < page.getCount()) {
                scrollWidth += unitWidth;
            }
            scrollWidth *= positionOffset * 0.5f;
            canvas.translate(scrollWidth, 0);
        }
    }

    /**
     * User interface state that is stored by IndicatorView for implementing
     * {@link View#onSaveInstanceState}.
     */
    public static class SavedState extends BaseSavedState {

        @SuppressWarnings("hiding")
        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        int count;
        int select;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel source) {
            super(source);
            count = source.readInt();
            select = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(count);
            out.writeInt(select);
        }

    }

    private class IndicatorViewOnPageChangeListener extends ViewPager.SimpleOnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (isAnimation) {
                mSelect = position;
                mPositionOffset = positionOffset;
            }
            invalidate();
        }

        @Override
        public void onPageSelected(int position) {
            if (!isAnimation) {
                mSelect = position;
            }
        }
    }

}
