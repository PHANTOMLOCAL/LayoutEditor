package com.tyron.layouteditor.editor.widget.viewgroup;

import android.animation.LayoutTransition;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import com.tyron.layouteditor.Theme;
import com.tyron.layouteditor.editor.widget.Attributes;
import com.tyron.layouteditor.editor.widget.BaseWidget;
import com.tyron.layouteditor.models.Attribute;
import com.tyron.layouteditor.util.NotificationCenter;

import java.util.ArrayList;
import java.util.List;

public class RelativeLayoutItem extends RelativeLayout implements BaseWidget {

    public RelativeLayoutItem(Context context) {
        super(context);
        init();
    }

    private void init() {
        setWillNotDraw(false);

        LayoutTransition layoutTransition = new LayoutTransition();
        layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
        layoutTransition.setDuration(180L);
        layoutTransition.disableTransitionType(LayoutTransition.DISAPPEARING);
        setLayoutTransition(layoutTransition);
    }

    private Manager viewManager;

    @Override
    public @NonNull View getAsView() {
        return this;
    }

    @Override
    public Manager getViewManager() {
        return viewManager;
    }

    @Override
    public void setViewManager(@NonNull Manager manager) {
        this.viewManager = manager;
    }


    Rect rect = new Rect();
    @Override
    public void onDraw(Canvas canvas){

        getLocalVisibleRect(rect);
        canvas.drawRect(rect, Theme.getViewBackgroundPaint());
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.didUpdateWidget);
    }


    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.didUpdateWidget);
    }

}
