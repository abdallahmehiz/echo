package dev.brahmkshatriya.echo.utils

import android.content.Context.MODE_PRIVATE
import android.util.TypedValue
import android.view.View
import android.view.ViewPropertyAnimator
import androidx.core.view.doOnPreDraw
import androidx.core.view.forEachIndexed
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.google.android.material.R
import com.google.android.material.motion.MotionUtils
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.transition.MaterialArcMotion
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialSharedAxis
import dev.brahmkshatriya.echo.ui.settings.LookFragment.Companion.ANIMATIONS_KEY
import dev.brahmkshatriya.echo.ui.settings.LookFragment.Companion.SHARED_ELEMENT_KEY

object Animator {

    fun startAnimation(
        view: View,
        animation: ViewPropertyAnimator,
        durationMultiplier: Float = 1f
    ) =
        view.run {
            clearAnimation()
            val interpolator = MotionUtils.resolveThemeInterpolator(
                context,
                R.attr.motionEasingStandardInterpolator,
                FastOutSlowInInterpolator()
            )
            val duration = animationDuration * durationMultiplier
            animation.setInterpolator(interpolator).setDuration(duration.toLong()).start()
        }

    fun NavigationBarView.animateTranslation(
        isRail: Boolean, isMainFragment: Boolean, action: () -> Unit
    ) {
        val value = if (isMainFragment) 0f else if (isRail) -width.toFloat() else height.toFloat()
        if (animations) {
            isVisible = true
            var animation = if (isRail) animate().translationX(value)
            else animate().translationY(value)

            animation = if (isMainFragment) animation.withStartAction(action)
                .withEndAction { isVisible = true }
            else animation.withEndAction { action(); isVisible = false }

            startAnimation(this, animation)

            menu.forEachIndexed { index, it ->
                val view = findViewById<View>(it.itemId)
                val dis = value * (index + 1)
                if (isRail) startAnimation(view, view.animate().translationX(dis))
                else startAnimation(view, view.animate().translationY(dis))
            }
        } else {
            isVisible = isMainFragment
            action()
        }
    }

    fun View.animateVisibility(isVisible: Boolean) {
        if (animations) startAnimation(
            this,
            animate().alpha(if (isVisible) 1f else 0f)
                .withEndAction { alpha = if (isVisible) 1f else 0f }
        )
        else alpha = if (isVisible) 1f else 0f
    }

    fun animateTranslation(view: View, old: Int, newHeight: Int) = view.run {
        if (view.animations) {
            clearAnimation()
            view.translationY = newHeight.toFloat() - old
            startAnimation(this, animate().translationY(0f))
        }
    }

    private val View.animationDuration: Long
        get() = context.applicationContext.run {
            MotionUtils.resolveThemeDuration(
                this,
                R.attr.motionDurationMedium1,
                350
            ).toLong()
        }
    private val View.animations
        get() = context.applicationContext.run {
            getSharedPreferences(packageName, MODE_PRIVATE).getBoolean(ANIMATIONS_KEY, true)
        }

    private val View.sharedElementTransitions
        get() = context.applicationContext.run {
            getSharedPreferences(packageName, MODE_PRIVATE).getBoolean(SHARED_ELEMENT_KEY, true)
        }

    fun Fragment.setupTransition(view: View) {
        val background = TypedValue()
        val theme = requireContext().theme
        theme.resolveAttribute(dev.brahmkshatriya.echo.R.attr.echoBackground, background, true)
        val color = resources.getColor(background.resourceId, theme)
        view.setBackgroundColor(color)

        if (view.animations) {
            val transitionName = arguments?.getString("transitionName")
            if (transitionName != null && view.sharedElementTransitions) {
                view.transitionName = transitionName
                val transition = MaterialContainerTransform().apply {
                    drawingViewId = dev.brahmkshatriya.echo.R.id.navHostFragment
                    setAllContainerColors(color)
                    setPathMotion(MaterialArcMotion())
                    duration = view.animationDuration
                }
                sharedElementEnterTransition = transition
            }

            exitTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
            reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false)
            enterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
            returnTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false)

            postponeEnterTransition()
            view.doOnPreDraw { startPostponedEnterTransition() }
        }
    }
}