package abandonedstudio.app.compassproject.util

import android.view.animation.Animation
import android.view.animation.RotateAnimation

object Animations {

    fun animateCompassRotation(from: Float, to: Float) = RotateAnimation(
            from,
            to,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
    ).also {
        it.duration = 1500
        it.fillAfter = true
    }
}