package com.canonfer.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.widget.GridView;
 /**
  * This class extends GridView adding new behaviors to its super class.
  * ExpandableHeightGridView class allows resizing on the heigth of the view according to the number of elements
  * @author fernandocanon
  *
  */
public class ExpandableHeightGridView extends GridView
{

    boolean expanded = false;

    public ExpandableHeightGridView(Context context)
    {
        super(context);
    }

    public ExpandableHeightGridView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public ExpandableHeightGridView(Context context, AttributeSet attrs,
            int defStyle)
    {
        super(context, attrs, defStyle);
    }

    public boolean isExpanded()
    {
        return expanded;
    }
/**
 * Exploids an Android hack to allows the view height's growth
 */
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        // HACK! TAKE THAT ANDROID!
        if (isExpanded())
        {
            // Calculate entire height by providing a very large height hint.
            // View.MEASURED_SIZE_MASK represents the largest height possible.
            int expandSpec = MeasureSpec.makeMeasureSpec(MEASURED_SIZE_MASK,
                    MeasureSpec.AT_MOST);
            super.onMeasure(widthMeasureSpec, expandSpec);

            ViewGroup.LayoutParams params = getLayoutParams();
            params.height = getMeasuredHeight();
        }
        else
        {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
/**
 * Setter method for informing the veiw if expansion is allowed
 * @param expanded
 */
    public void setExpanded(boolean expanded)
    {
        this.expanded = expanded;
    }
}