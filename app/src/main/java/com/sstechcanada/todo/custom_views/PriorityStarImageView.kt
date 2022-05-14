package com.sstechcanada.todo.custom_views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.sstechcanada.todo.R

class PriorityStarImageView : AppCompatImageView {
    constructor(context: Context?) : super(context!!) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        style(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        style(context, attrs)
    }

    private fun style(context: Context, attrs: AttributeSet?) {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.PriorityStarImageView)
        val priority = attributes.getInteger(R.styleable.PriorityStarImageView_priority, HIGH)
        setPriority(priority)
        attributes.recycle()
    }

    private fun setPriority(priority: Int) {
        val res = resources
        var star = 0
        var contentDescription = 0
        when (priority) {
            HIGH -> {
                star = R.drawable.ic_star_red_24dp
                contentDescription = R.string.high_priority_task_red_star
            }
            MEDIUM -> {
                star = R.drawable.ic_star_orange_24dp
                contentDescription = R.string.medium_priority_task_orange_star
            }
            LOW -> {
                star = R.drawable.ic_star_yellow_24dp
                contentDescription = R.string.low_priority_task_yellow_star
            }
            COMPLETED -> {
                star = R.drawable.ic_star_grey_24dp
                contentDescription = R.string.completed_task_grey_star
            }
        }
        background = res.getDrawable(star)
        setContentDescription(res.getString(contentDescription))
    }

    companion object {
        const val HIGH = 0
        const val MEDIUM = 1
        const val LOW = 2
        const val COMPLETED = 3
    }
}