package com.tyron.layouteditor.editor.widget.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.tyron.layouteditor.editor.widget.Attributes;
import com.tyron.layouteditor.editor.widget.BaseWidget;
import com.tyron.layouteditor.editor.widget.viewgroup.RelativeLayoutItem;
import com.tyron.layouteditor.models.Attribute;
import com.tyron.layouteditor.util.NotificationCenter;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("AppCompatCustomView")
public class EditTextItem extends EditText implements BaseWidget {

    private Manager viewManager;

    public EditTextItem(Context context) {
        super(context);
        setFocusable(false);
        setFocusableInTouchMode(false);
    }

    @NonNull
    @Override
    public View getAsView() {
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
