/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.ds.uri;

import com.lewa.filemanager.beans.FileInfo;
import java.util.Stack;

/**
 *
 * @author Administrator
 */
public class NavigationPool {

    public Stack<NavigationNode> navEntity = new Stack<NavigationNode>();

    public boolean isAtTop() {
        return this.navEntity.size() <= 1 ? true : false;
    }

    public NavigationNode backForward(int theId) {
        // TODO Auto-generated method stub
        for (int i = this.navEntity.size() - 1; i > theId; i--) {
            this.navEntity.pop();
        }
        return navEntity.pop();
    }

    public NavigationNode backward(int steps) {
        // TODO Auto-generated method stub
        for (; steps > 0; steps--) {
            this.navEntity.pop();
        }
        return navEntity.peek();
    }

    public void push(NavigationNode nav) {
        // TODO Auto-generated method stub
        if (this.navEntity.size() == 1 && nav.displayname.equals(NavigationConstants.SDCARD)
                && this.navEntity.peek().displayname.equals(NavigationConstants.SDCARD)) {
            return;
        } else if (this.navEntity.size() == 1
                && NavigationConstants.CATEGORYHOME.equals(nav.displayname)
                && NavigationConstants.CATEGORYHOME.equals(this.navEntity.peek().displayname)) {
            return;
        } else {
            this.navEntity.push(nav);
        }
    }

    public String getShowText() {
        StringBuilder str = new StringBuilder();
        str.append(this.navEntity.get(0).displayname);
        if (this.navEntity.size() < 2) {
            return str.toString();
        }
        for (int i = 1; i < this.navEntity.size(); i++) {
            str.append(" / " + this.navEntity.get(i).displayname);
        }
        return str.toString();
    }

    public Object getCurrNavEntitySource() {
        return !navEntity.isEmpty()?navEntity.peek().producingSource:null;
    }

    @Override
    public String toString() {
        String str = "NavigationPool{" + "navEntity=";
        for (int i = 0; i < navEntity.size(); i++) {
            str += (((FileInfo) (navEntity.get(i).producingSource)).path + "}\n");
        }
        return str;
    }
}
