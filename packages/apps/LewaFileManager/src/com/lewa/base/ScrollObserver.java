package com.lewa.base;

import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

public class ScrollObserver implements OnScrollListener {

    private boolean isRefreshFoot;
    private boolean loadBool;
    private IconLoadScrollTackler transactor;

    public ScrollObserver(IconLoadScrollTackler transactor) {
        super();
        this.transactor = transactor;
    }

    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
            if (loadBool) {
                transactor.idleDo(view);
                loadBool = false;
            }
        } else if (isRefreshFoot) {
            transactor.touchBottom(view);
        } else {
            loadBool = true;
            if (scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                transactor.scrolled(view);
            } else if (scrollState == OnScrollListener.SCROLL_STATE_FLING) {
                transactor.scrolled(view);
            }
        }
    }

    public void onScroll(AbsListView view, int firstVisibleItem,
            int visibleItemCount, int totalItemCount) {
        if (firstVisibleItem + visibleItemCount == totalItemCount) {
            isRefreshFoot = true;
        } else {
            isRefreshFoot = false;
        }
        transactor.scrolling(view, firstVisibleItem + visibleItemCount);
    }

    public static interface IconLoadScrollTackler {

        public void idleDo(AbsListView view);

        public void touchBottom(AbsListView view);

        public void scrolled(AbsListView view);

        public void scrolling(AbsListView view, int tailVisibleItem);
    }
}
