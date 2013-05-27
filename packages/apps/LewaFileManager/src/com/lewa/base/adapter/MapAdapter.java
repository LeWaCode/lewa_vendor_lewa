package com.lewa.base.adapter;

import com.lewa.filemanager.activities.views.ViewUtil;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import com.lewa.base.Logs;

public class MapAdapter extends BaseAdapter {

    protected Context context;
    protected List<String> fieldnames;
    protected int[] viewsid;
    private int itemLayout;
    private ItemDataSrc itemDataSrc;
    private ActionListener[] handlers;
    private Map<Integer, ListenerBox> listenerMaps;
    private Map<View, Integer> viewContentMap = new HashMap<View, Integer>();
    boolean ischecked;
    public boolean isVisible = true;
    public Map<Integer, Boolean> checkBoxVisibleOptions = new HashMap<Integer, Boolean>();
    public List<Integer> viewInitIndices = new ArrayList<Integer>();
    public int viewChildCount = 0;
    public boolean startChildViewsCount = true;
    protected boolean isCreatedView;
    public int selectedNum;
    public int latestPosition;
    public List<Integer> selected = new ArrayList<Integer>();
    public Class clazz;
    public String checkboxname;
    public List<Integer> selectedbck;

    public void setCheckBoxName(String checkboxFieldName) {
        if (this.fieldnames.indexOf(checkboxFieldName) == -1) {
            throw new IllegalStateException("");
        }
        checkboxname = checkboxFieldName;
    }

    public int getTotalSelection() {
        return this.selected.size();
    }

    public void selectAll(boolean shouldSelect) {
        if (shouldSelect) {
            if (this.selectedbck != null) {
                this.selected = new ArrayList(this.selectedbck);
            }
        } else {
            if (this.selected != null) {
                this.selected.clear();
            }
        }
    }

    public Boolean isSelected() {
        return !this.selected.isEmpty();
    }

    /**
     * 
     *  增加或去掉标记的选择项
     */
    public void addSelected(int pos, boolean isAdd) {
        if (isAdd) {
            selected.add(pos);
        } else {
            int i = selected.lastIndexOf(pos);
            if (i >= 0) {
                selected.remove(i);
            }
        }
    }

    public void reinitSelectedAllBck(int count) {
        selected = new ArrayList<Integer>(count);
        selectedbck = new ArrayList<Integer>(count);
        fill(selectedbck, count);
        viewContentMap.clear();
    }

    public void treatCursor(Object item, View convertView, int position) {
        Object value = null;
        String name;
        Cursor cur = (Cursor) item;
        Class type;
        if (cur.getColumnCount() > 0) {
            while (cur.moveToNext()) {
                for (int i = 0; i < cur.getColumnCount(); i++) {
                    name = cur.getColumnName(i);
                    type = itemDataSrc.getNameTypePair().get(name);
                    try {
                        if (type == Integer.class) {
                            value = cur.getInt(i);
                        } else if (type == Long.class) {
                            value = cur.getLong(i);
                        } else if (type == Double.class) {
                            value = cur.getDouble(i);
                        } else if (type == String.class) {
                            value = cur.getString(i);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (value == null) {
                        continue;
                    }

                    findAndBindView(convertView, position, item, name, value);
                }
            }
        }
    }

    public void treatObject(Object item, View convertView, int position) throws SecurityException {
        Object value = null;
        String name;
        boolean isAccessible;
        clazz = item.getClass();
        Field[] fields = clazz.getFields();
        for (Field field : fields) {
            isAccessible = field.isAccessible();
            field.setAccessible(true);
            if (this.fieldnames.contains(field.getName())) {
                name = field.getName();
                try {
                    value = field.get(item);
                    // value = value == null ? "" : value;
                } catch (IllegalArgumentException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (value != null) {
                    findAndBindView(convertView, position, item, name, value);
                }
            }
            field.setAccessible(isAccessible);
        }
    }

    public void treatMap(Object item, View convertView, int position) {
        String name;
        Object value;
        Map<String, Object> items = (Map<String, Object>) item;
        for (int i = 0; i < this.fieldnames.size(); i++) {
            name = this.fieldnames.get(i);
            if (items.containsKey(name)) {
                value = items.get(name);
                findAndBindView(convertView, position, item, name, value);
            }
        }
    }

    private void fill(List<Integer> boolList, int count) {
        boolList.removeAll(boolList);
        for (int i = 0; i < count; i++) {
            boolList.add(i);
        }
    }

    public boolean isCreatedView() {
        return isCreatedView;
    }

    public void setCreatedView(boolean isCreateView) {
        this.isCreatedView = isCreateView;
    }

    public Map<View, Integer> getViewContentMap() {
        return viewContentMap;

    }

    public void setStartChildViewsCount(boolean flag) {
        startChildViewsCount = flag;
        viewChildCount = 0;
    }

    public void setItemDataSrc(ItemDataSrc itemDataSrc) {
        this.itemDataSrc = itemDataSrc;
        visibleALlCheckBox();
    }

    public void addItemDataSrcList(List<? extends Object> objs) {
        if (this.getItemDataSrc() == null || this.getItemDataSrc().getContent() == null) {
            this.setItemDataSrc(new ItemDataSrc(objs));
        } else {
            ((List) this.getItemDataSrc().getContent()).addAll(objs);
        }
        visibleALlCheckBox();
    }

    public void clearSelectOption() {
    }

    public void clearDataSrc() {
        this.getItemDataSrc().clear();
    }

    public MapAdapter(Context context, AdaptInfo adaptInfo) {
        changeAdaptInfo(adaptInfo);
        this.context = context;
        // TODO Auto-generated constructor stub

    }

    private void visibleALlCheckBox() {
        if (this.itemDataSrc == null) {
            return;
        }
        this.markVisible(true);
    }

    public ItemDataSrc getItemDataSrc() {
        return itemDataSrc;
    }

    public MapAdapter(Context context) {
        this.context = context;
    }

    public void changeAdaptInfo(AdaptInfo adaptInfo) {
        this.fieldnames = Arrays.asList(adaptInfo.objectFields);
        this.viewsid = adaptInfo.viewIds;
        this.itemLayout = adaptInfo.listviewItemLayoutId;
        this.itemDataSrc = adaptInfo.listviewItemData;
        this.handlers = adaptInfo.actionListeners;
        deployListeners(handlers);
    }

    private void deployListeners(ActionListener[] actionListeners) {
        if (listenerMaps == null) {
            listenerMaps = new HashMap<Integer, ListenerBox>();
        }
        if (actionListeners == null || actionListeners.length == 0) {
            return;
        }
        for (ActionListener listener : actionListeners) {
            if (!listenerMaps.containsKey(listener.getResrcId())) {
                listenerMaps.put(listener.getResrcId(), new ListenerBox(this, listener));
            } else {
                listenerMaps.get(listener.getResrcId()).addActionListener(listener);
            }
        }
    }

    private void addListener(Integer resid, ListenerBox listener) {
        if (this.listenerMaps == null) {
            this.listenerMaps = new HashMap<Integer, ListenerBox>();
        }
        this.listenerMaps.put(resid, listener);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return itemDataSrc == null ? 0 : itemDataSrc.getCount();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        //dangerous itemDataSrc.getCount() <= position
        return (itemDataSrc == null || itemDataSrc.getCount() < position) ? null : itemDataSrc.getItem(position);

    }

    public long getItemId(int arg0) {
        return 0;
    }

    private class ViewHolder {

        View[] viewCaches;
    }

    protected View createItemView() {
        // TODO Auto-generated method stub

        LayoutInflater layoutInflater = LayoutInflater.from(context);
        return layoutInflater.inflate(itemLayout, null);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        this.latestPosition = position;
        Object item = getItem(position);
        setCreatedView(false);
        if (null == convertView) {
            setCreatedView(true);
            convertView = createItemView();
        }
        ListenerBox listener;
        viewContentMap.put(convertView, position);
        if (listenerMaps != null) {
            for (Entry en : listenerMaps.entrySet()) {
                Integer resourceid = ((Integer) en.getKey());
                listener = ((ListenerBox) en.getValue());
                View view = ViewUtil.findViewById(convertView, resourceid);
                for (Entry<Integer, ActionListener> e : listener.handlers.entrySet()) {
                    int actionType = e.getKey().intValue();
                    switch (actionType) {
                        case ActionListener.OnClick:
                            view.setOnClickListener(listener);
                            break;
                        case ActionListener.OnLongClick:
                            view.setOnLongClickListener(listener);
                            break;
                        case ActionListener.OnTouch:
                            view.setOnTouchListener(listener);
                            break;
                        case ActionListener.OnCheckChanged:
                            if (view instanceof CheckBox) {
                                ((CheckBox) view).setOnCheckedChangeListener(listener);
                            }
                            break;
                    }
                }
            }
        }
        getViewInDetail(item, position, convertView);
        return convertView;
    }

    protected void getViewInDetail(Object item, int position, View convertView) {
        if (item == null) {
            return;
        }

        if (item instanceof Cursor) {
            treatCursor(item, convertView, position);
        } else if (item instanceof Map) {
            treatMap(item, convertView, position);
        } else {
            treatObject(item, convertView, position);
        }
    }

    protected void findAndBindView(View convertView, int pos, Object item, String name,
            Object value) {
        int theViewId = this.fieldnames.indexOf(name);
        View theView = convertView.findViewById(this.viewsid[theViewId]);
        theView.setVisibility(View.VISIBLE);
        if (theView instanceof ImageView) {
            if (value == null || value.toString().equals("-1")) {
                return;
            }
            if (value instanceof Integer) {
                ((ImageView) theView).setImageResource(Integer.parseInt(value.toString()));
            } else if (value.getClass() == BitmapDrawable.class) {
                ((ImageView) theView).setImageDrawable((BitmapDrawable) value);
            } else if (value instanceof Drawable) {
                ((ImageView) theView).setImageDrawable((Drawable) value);
            }
        }
        if (theView instanceof CheckBox) {
            ((CheckBox) theView).setChecked(selected.contains(pos));
            ((CheckBox) theView).setVisibility(getVisibleOption(isVisible));
        } else if (theView instanceof TextView) {
            ((TextView) theView).setText(value instanceof SpannableStringBuilder?(SpannableStringBuilder)value:value.toString());
        }
    }

    public void markVisible(boolean isVisible) {
        this.isVisible = isVisible;
    }

    public int getVisibleOption(boolean isVisible) {
        if (isVisible) {
            return View.VISIBLE;
        } else {
            return View.GONE;
        }
    }

    public boolean hasSelected() {
        return this.selected.size() > 0;
    }

    public boolean isSelectAll() {

        return this.selected.size() == this.getCount();
    }

    public static abstract class ActionListener {

        public static final int OnClick = 0;
        public static final int OnLongClick = 1;
        public static final int OnTouch = 2;
        public static final int OnCheckChanged = 3;
        public int listenerType = -1;
        private MapAdapter baseAdapter;
        public int resrcId;
        public View itemView;
        private ListenerBox listener;

        public int getListenerType() {
            return listenerType;
        }

        public void setLWBaseAdapter(MapAdapter lWBaseAdapter) {
            setBaseAdapter(lWBaseAdapter);
        }

        public int getResrcId() {
            return resrcId;
        }

        public ActionListener(int resrcId, int listenerType) {
            this.listenerType = listenerType;
            this.resrcId = resrcId;
        }

        public abstract void handle(View view, ListenerBox listener);

        public void invokeHandle(View view, ListenerBox listener) {
            this.listener = listener;
            handle(view, listener);
        }

        public int findViewIndex(View view) {
            View vg = (View) view.getParent();
            if (vg.getTag() == null) {
                return findViewIndex(vg);
            }
            return (Integer) vg.getTag();
        }

        public void setBaseAdapter(MapAdapter baseAdapter) {
            this.baseAdapter = baseAdapter;
        }

        public MapAdapter getBaseAdapter() {
            return baseAdapter;
        }
    }

    public static class AdaptInfo {

        public ItemDataSrc listviewItemData;//actual data-carried object
        public String[] objectFields;//data fields in sequence which map to view ids
        public int[] viewIds; //id array in listview item  
        public int listviewItemLayoutId;//layout id for each item in listview
        public ActionListener[] actionListeners;//varied action listeners in which all events would be received and have been done handling.
    }

    @Override
    public void notifyDataSetChanged() {
        // TODO Auto-generated method stub
        super.notifyDataSetChanged();
    }
}
