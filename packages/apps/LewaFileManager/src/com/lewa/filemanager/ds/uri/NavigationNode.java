package com.lewa.filemanager.ds.uri;

public class NavigationNode {

    public int defaultPosition = 0;
    public Object producingSource;
    public String displayname;
    
    public void setClickPosition(int clickPosition) {
        this.defaultPosition = clickPosition;
    }

    public static NavigationNode buildNode(Class<? extends NavigationNode> clazz, Object source) {
        try {
            return clazz.getConstructor(source.getClass()).newInstance(source);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
