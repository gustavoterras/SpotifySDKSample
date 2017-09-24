package com.infoterras.spotifysampleapp.components;

import android.animation.Animator;
import android.transition.Transition;
import android.transition.TransitionValues;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;

/**
 * Created by gustavo.filho on 23/09/17.
 */

public class CircularTransition extends Transition {

    @Override
    public void captureStartValues(TransitionValues transitionValues) {
    }

    @Override
    public void captureEndValues(TransitionValues transitionValues) {
    }

    @Override
    public Animator createAnimator(ViewGroup sceneRoot, TransitionValues startValues, TransitionValues endValues) {

        if(startValues == null && endValues == null) return null;

        int cx = sceneRoot.getWidth();
        int cy = 0;

        float finalRadius = Math.max(sceneRoot.getWidth(), sceneRoot.getHeight()) * 2;

        Animator circularReveal = ViewAnimationUtils.createCircularReveal(sceneRoot, cx, cy, 0, finalRadius);
        circularReveal.setDuration(1000);

        return circularReveal;
    }


}
