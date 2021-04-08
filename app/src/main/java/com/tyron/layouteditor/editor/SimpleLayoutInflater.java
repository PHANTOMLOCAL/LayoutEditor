package com.tyron.layouteditor.editor;

import android.util.Log;
import android.view.InflateException;
import android.view.ViewGroup;

import java.util.Iterator;
import java.util.LinkedHashSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tyron.layouteditor.R;
import com.tyron.layouteditor.editor.widget.BaseWidget;
import com.tyron.layouteditor.values.Layout;
import com.tyron.layouteditor.values.ObjectValue;
import com.tyron.layouteditor.values.Value;

/**
 * A layout builder which can parse json to construct an android view out of it. It uses the
 * registered parsers to convert the json string to a view and then assign attributes.
 */
public class SimpleLayoutInflater implements EditorLayoutInflater {

    private static final String TAG = "SimpleLayoutInflater";

    @NonNull
    protected final EditorContext context;

    @NonNull
    protected final IdGenerator idGenerator;

    SimpleLayoutInflater(@NonNull EditorContext context, @NonNull IdGenerator idGenerator) {
        this.context = context;
        this.idGenerator = idGenerator;
    }

    @Override
    @Nullable
    public ViewTypeParser getParser(@NonNull String type) {
        return context.getParser(type);
    }

    @NonNull
    @Override
    public BaseWidget inflate(@NonNull Layout layout, @NonNull ObjectValue data, @Nullable ViewGroup parent, int dataIndex) {

        /*
         * Get the the view type parser for this layout type
         */
        final ViewTypeParser parser = getParser(layout.type);
        if (parser == null) {
            /*
             * If parser is not registered ask the application land for the view
             */
            return onUnknownViewEncountered(layout.type, layout, data, dataIndex);
        }

        /*
         * Create a view of {@code layout.type}
         */
        final BaseWidget view = createView(parser, layout, data, parent, dataIndex);

        if (view.getViewManager() == null) {

            /*
             * Do post creation logic
             */
            onAfterCreateView(parser, view, parent, dataIndex);

            /*
             * Create View Manager for {@code layout.type}
             */
            final BaseWidget.Manager viewManager = createViewManager(parser, view, layout, data, parent, dataIndex);

            /*
             * Set the View Manager on the view.
             */
            view.setViewManager(viewManager);
        }

        /*
         * Handle each attribute and set it on the view.
         */
        if (layout.attributes != null) {
            Iterator<Layout.Attribute> iterator = layout.attributes.iterator();
            Layout.Attribute attribute;

            //save attributes into a view tag so we can edit it later
            if(layout.tagAttributes != null) {
                view.getAsView().setTag(R.id.attributes, new LinkedHashSet<>(layout.tagAttributes));
            }
            while (iterator.hasNext()) {
                attribute = iterator.next();
                handleAttribute(parser, view, attribute.id, attribute.value);
            }
        }

        return view;
    }

    @NonNull
    @Override
    public BaseWidget inflate(@NonNull Layout layout, @NonNull ObjectValue data, int dataIndex) {
        return inflate(layout, data, null, dataIndex);
    }

    @NonNull
    @Override
    public BaseWidget inflate(@NonNull Layout layout, @NonNull ObjectValue data) {
        return inflate(layout, data, null, -1);
    }

    @NonNull
    @Override
    public BaseWidget inflate(@NonNull String name, @NonNull ObjectValue data, @Nullable ViewGroup parent, int dataIndex) {
        Layout layout = context.getLayout(name);
        if (null == layout) {
            throw new InflateException("layout : '" + name + "' not found");
        }
        return inflate(layout, data, parent, dataIndex);
    }

    @NonNull
    @Override
    public BaseWidget inflate(@NonNull String name, @NonNull ObjectValue data, int dataIndex) {
        return inflate(name, data, null, dataIndex);
    }

    @NonNull
    @Override
    public BaseWidget inflate(@NonNull String name, @NonNull ObjectValue data) {
        return inflate(name, data, null, -1);
    }

    @Override
    public int getUniqueViewId(@NonNull String id) {
        return idGenerator.getUnique(id);
    }

    @NonNull
    @Override
    public IdGenerator getIdGenerator() {
        return idGenerator;
    }

    protected BaseWidget createView(@NonNull ViewTypeParser parser, @NonNull Layout layout, @NonNull ObjectValue data,
                                     @Nullable ViewGroup parent, int dataIndex) {
        return parser.createView(context, layout, data, parent, dataIndex);
    }

    protected BaseWidget.Manager createViewManager(@NonNull ViewTypeParser parser, @NonNull BaseWidget view, @NonNull Layout layout,
                                                    @NonNull ObjectValue data, @Nullable ViewGroup parent, int dataIndex) {
        return parser.createViewManager(context, view, layout, data, parser, parent, dataIndex);
    }

    protected void onAfterCreateView(@NonNull ViewTypeParser parser, @NonNull BaseWidget view, @Nullable ViewGroup parent, int index) {
        parser.onAfterCreateView(view, parent, index);
    }

    @NonNull
    protected BaseWidget onUnknownViewEncountered(String type, Layout layout, ObjectValue data, int dataIndex) {
        if (EditorConstants.isLoggingEnabled()) {
            Log.d(TAG, "No ViewTypeParser for: " + type);
        }
        if (context.getCallback() != null) {
            BaseWidget view = context.getCallback().onUnknownViewType(context, type, layout, data, dataIndex);
            //noinspection ConstantConditions because we need to throw a ProteusInflateException specifically
            if (view == null) {
                throw new InflateException("inflater Callback#onUnknownViewType() must not return null");
            }
        }
        throw new InflateException("Layout contains type: 'include' but inflater callback is null");
    }

    protected boolean handleAttribute(@NonNull ViewTypeParser parser, @NonNull BaseWidget view, int attribute, @NonNull Value value) {
        if (EditorConstants.isLoggingEnabled()) {
            Log.d(TAG, "Handle '" + attribute + "' : " + value);
        }
        //noinspection unchecked
        return parser.handleAttribute(view.getAsView(), attribute, value);
    }
}