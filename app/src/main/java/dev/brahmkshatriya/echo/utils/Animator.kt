package dev.brahmkshatriya.echo.utils

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.view.ViewPropertyAnimator
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.google.android.material.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.motion.MotionUtils
import com.google.android.material.transition.MaterialArcMotion
import com.google.android.material.transition.MaterialContainerTransform

object Animator {

    private fun View.startAnimation(animation: ViewPropertyAnimator) {
        clearAnimation()
        val interpolator = MotionUtils.resolveThemeInterpolator(
            context,
            R.attr.motionEasingStandardInterpolator,
            FastOutSlowInInterpolator()
        )
        val duration = MotionUtils.resolveThemeDuration(
            context,
            R.attr.motionDurationLong1,
            500
        ).toLong()
        animation.setInterpolator(interpolator).setDuration(duration).start()
    }

    fun View.animateTranslation(isRail: Boolean, isVisible: Boolean, action: () -> Unit) {
        val value = if (isVisible) 0f else if (isRail) -width.toFloat() else height.toFloat()
        if (animations) {
            val animation =
                if (isRail) animate().translationX(value).withEndAction { translationX = value }
                else animate().translationY(value).withEndAction { translationY = value }
            startAnimation(
                if (isVisible) animation.withStartAction(action)
                else animation.withEndAction(action)
            )
        }
        else {
            if (isRail) translationX = value
            else translationY = value
            action()
        }
    }

    fun View.animateVisibility(isVisible: Boolean) {
        if (animations) startAnimation(
            animate().alpha(if (isVisible) 1f else 0f)
                .withEndAction { alpha = if (isVisible) 1f else 0f }
        )
        else alpha = if (isVisible) 1f else 0f
    }

    fun BottomSheetBehavior<View>.animatePeekHeight(view: View, newHeight: Int) = view.run {
        if (view.animations) {
            clearAnimation()
            view.translationY = newHeight.toFloat() - peekHeight
            peekHeight = newHeight
            startAnimation(animate().translationY(0f))
        } else peekHeight = newHeight
    }

    private val View.animations
        get() = context.applicationContext.run {
            getSharedPreferences(packageName, Context.MODE_PRIVATE).getBoolean("animations", true)
        }

    fun Fragment.setupTransition(view: View) {
        if (view.animations) {
            val value = TypedValue()
            val theme = requireContext().theme
            theme.resolveAttribute(dev.brahmkshatriya.echo.R.attr.echoBackground, value, true)
            sharedElementEnterTransition = MaterialContainerTransform().apply {
                drawingViewId = dev.brahmkshatriya.echo.R.id.navHostFragment
                setAllContainerColors(resources.getColor(value.resourceId, theme))
                setPathMotion(MaterialArcMotion())
            }
            postponeEnterTransition()
            view.doOnPreDraw { startPostponedEnterTransition() }
        }
    }
}