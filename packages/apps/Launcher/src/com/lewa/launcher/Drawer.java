package com.lewa.launcher;

import java.util.ArrayList;

public interface Drawer {

	public int getVisibility();

	public boolean isOpaque();

	public boolean hasFocus();

	public boolean requestFocus();

	public void setDragger(DragController dragger);
	public void setLauncher(Launcher launcher);

	public void setVisibility(int state);
	public void invalidate();

	public void setAdapter(ApplicationsAdapter adapter);

    //Begin [pan add]
	public int getNumberPerScreen();
    public int getTotalScreens();
    public int getCurrentScreen();
    public void updateBarForPackage(ArrayList<String> packageNames);
    public void autoArrange();
    public void setScrollSpeed(boolean changed);
    //End
}
