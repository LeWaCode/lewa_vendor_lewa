package com.lewa.os.common.ui.indexbar;  
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import android.R.integer;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.android.internal.util.HanziToPinyin;
import com.android.internal.util.HanziToPinyin.Token;
import com.lewa.PIM.R;
import com.lewa.os.util.PinyinUtil;
public class IndexBar extends RelativeLayout implements ImageButton.OnClickListener{  
    private Context mContext;
    private static final int PAGE_SIZE = 48;
    
    public IndexBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
	}

	private int[] id;
	private String[] title;
	private String[] title_pinyin;
	private String[] song_first_name;
	private String[] song_first_name1;
	private String[] song_first_name2;
	private String[] song_first_name3;
	private String[] artist;
	private String[] duration;
	private Animation showAction, hideAction;
	boolean alpha_menu_Showed;

	private AlphaAdapter alphaadapter;
	private GridView gridview;
	private Scroller_view scroller_view;
	private ListView list;
	private SectionIndexer sectionIndexter = null;  
	int t = 0;
	int h = 0, h1 = 0;
	int page_count = -1;
	int currentpage = 0;
	
	Cursor cursor;
    TextView alphabutton;
	private ImageButton down;
	RelativeLayout alpha_menu;

	public boolean isAlpha_menu_Showed() {
		return alpha_menu_Showed;
	}

	public Animation getShowAction() {
		return showAction;
	}

	public void setShowAction(Animation showAction) {
		this.showAction = showAction;
	}

	public Animation getHideAction() {
		return hideAction;
	}

	public void setHideAction(Animation hideAction) {
		this.hideAction = hideAction;
	}

	public GridView getGridview() {
		return gridview;
	}

	public Scroller_view getScrollerView () {
	    return scroller_view;
	}

	public void setGridview(GridView gridview) {
		this.gridview = gridview;
	}

	public ImageButton getDown() {
		return down;
	}

	public void setDown(ImageButton down) {
		this.down = down;
	}

	public void setAlpha_menu_Showed(boolean alpha_menu_Showed) {
		this.alpha_menu_Showed = alpha_menu_Showed;
	}

	public void setAlpha_menu(RelativeLayout alpha_menu) {
		this.alpha_menu = alpha_menu;
	}

	public RelativeLayout getAlpha_menu() {
		return alpha_menu;
	}

	List<Map<String, Object>> list1 = new ArrayList<Map<String, Object>>();
	ArrayList<HashMap<String, Object>> arraylist = new ArrayList<HashMap<String, Object>>();
	private List<? extends Map<String, ?>> sortedHead;
	private ListView listview;
	public static final String BUTTONKEY = "Button";

	public void load(RelativeLayout rl, ListView list2,
			List<Map<String, Object>> headLetterMap, int alphalistItem,
			String[] from, int[] to, Object onclicker2) {		
	}
    
	ViewGroup container;
	public void load(ViewGroup container, List<? extends Map<String, ?>> sortedHead, 
        int res, String[] from, int[] to, IndexBarOnClicker onclicker) {  
		this.container = container;
        this.onclicker = onclicker;

		showAction = new ScaleAnimation(
				1.0f,1.0f,0.0f,1.0f,Animation.RELATIVE_TO_SELF,1.0f, 
				Animation.RELATIVE_TO_SELF,1.0f); 
		showAction.setDuration(300); 
		
		hideAction = new ScaleAnimation(1.0f,1.0f,1.0f,0.0f,
				Animation.RELATIVE_TO_SELF, 1.0f,
				Animation.RELATIVE_TO_SELF, 1.0f); 
		hideAction.setDuration(300); 
        alpha_menu_Showed = false; 
        alpha_menu.setVisibility(View.GONE);

        //the alpha_menu has max of 6 columns, max of 8 rows, its item's height is 35dip, the space is 10 pixel
        int rows = (sortedHead.size() + 5) / 6;
        rows = (rows > 8)? 8 : rows;
        ViewGroup.LayoutParams layoutParams = alpha_menu.getLayoutParams();
        layoutParams.height = (int )((mContext.getResources().getDisplayMetrics().density * 35) * rows)
                + ((rows + 1) * 10);
 
		this.sortedHead = sortedHead;		
		Integer size = -1;
		song_first_name2 = new String[sortedHead.size()];
		//modify by zenghuaying fix requirement #11068
		int headSize = sortedHead.size();
		int totalPage = headSize % PAGE_SIZE == 0 ? headSize/PAGE_SIZE : headSize/PAGE_SIZE + 1;
		size = totalPage;
		
		for (int j = 0; j < size; j++) {
		    
		    List<Map<String, ?>> pageList = new ArrayList<Map<String,?>>();
		    int kStart = j*48;
		    int kMax = j == size - 1 ? headSize : (j+1) * PAGE_SIZE;
		    for(int k = kStart;k < kMax;k++){
		        pageList.add(sortedHead.get(k));
		    }
		    
			GridView gridview = new GridView(mContext);
			alphaadapter = new AlphaAdapter(mContext, pageList, res, from, to, j);
			gridview.setNumColumns(6);
			gridview.setColumnWidth(40);
			//gridview.setLayoutParams(new ViewGroup.LayoutParams(320,150));
            gridview.setGravity(Gravity.CENTER);
			gridview.setPadding(10, 10, 10, 10);
			gridview.setVerticalSpacing(10);
			gridview.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
            gridview.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
			
			gridview.setAdapter(alphaadapter);
			if (onclicker != null) {
				gridview.setOnItemClickListener(onclicker);				
			}
			scroller_view.addView(gridview, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.FILL_PARENT));
		}
		//modify end
		page_count = size;
	}

	RelativeLayout bar;
	public void setResrcIds( IndexBar bar, ViewGroup container,int scrooll_view_id, int menu_id,
			int alphabuttonid, int leftid, int rightid, int downid) {
	    container.addView(bar);
		scroller_view = (Scroller_view)bar.findViewById(scrooll_view_id);
		alpha_menu = (RelativeLayout) bar.findViewById(menu_id);
		alphabutton = (TextView )bar.findViewById(alphabuttonid);		
	}

	public String[] doMixedOrderBy(String[] titles,String[] sortedhead, Integer k) {
		int len = titles.length;
		for (int i = 0; i < len; i++) {
			if (!(song_first_name1[i].equals(" "))) {
				k++;
				sortedhead[k] = song_first_name1[i];
			}

			boolean flag = false;
			for (int p = i + 1; p < len; p++) {
				if (!(song_first_name1[p].equals(" "))) {
					t = p + 1;
					flag = true;
					break;
				}
			}

			if (flag) {
				for (int j = i + 1; j < t + 1; j++) {
					String name_first = title[i].trim().substring(0, 1).toUpperCase();
					if (!(sortedhead[k].endsWith(name_first))) {
						k++;
						sortedhead[k] = name_first;
					}
				}
			} else {
				for (int j = i + 1; j < len + 1; j++) {
					String name_first = titles[i].trim().substring(0, 1).toUpperCase();
					if (!(sortedhead[k].endsWith(name_first))) {
						k++;
						sortedhead[k] = name_first;
					}
				}
			}
		}

		for (int j = 0; j < k + 1; j++) {
			HashMap<String, Object> map = new HashMap<String,Object>();
			map.put("alphabutton", sortedhead[j]);
			arraylist.add(map);
		}
		return sortedhead;
	}  

	IndexBarOnClicker onclicker;

    public abstract class IndexBarOnClicker implements GridView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) { //(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            String text = ((HashMap)parent.getItemAtPosition(position)).get(IndexBar.BUTTONKEY).toString();
            if (!invodeClick(text)) {
                text = ((HashMap)parent.getItemAtPosition(position + 1)).get(IndexBar.BUTTONKEY).toString();
                invodeClick(text);
            }
        }
        public abstract boolean invodeClick(String text);
    }


	@Override
	public void onClick(View view) {

    }

	public void item_sort(String[] titles) {
		int len = titles.length;
		PinyinUtil pinyinutil = new PinyinUtil();
		for (int i = 0; i < titles.length; i++) {
			title_pinyin[i] = pinyinutil.hanziToPinyin(titles[i]);
			if ((titles[i].charAt(0) >= 97 && titles[i].charAt(0) <= 122) 
                || (titles[i].charAt(0) >= 65 && titles[i].charAt(0) <= 90)) {
				song_first_name[i] = titles[i].substring(0, 1);
			} else {
				song_first_name[i] = title_pinyin[i].substring(0, 1);
			}

			if(song_first_name[i].charAt(0) >= 97 && song_first_name[i].charAt(0) <= 122) {
				song_first_name[i]=song_first_name[i].toUpperCase();
			}
		}

		for (int i = 0; i < len; i++) {
			for (int j = 0; j < len - i - 1; j++) {
				String title_pinyin_first = title_pinyin[j].trim().substring(0, 1).toUpperCase();
				String title_pinyin_first1 = title_pinyin[j+1].trim().substring(0, 1).toUpperCase();
				if (title_pinyin_first.compareTo(title_pinyin_first1) > 0) {
					String pinyintemp = title_pinyin[j];
					title_pinyin[j] = title_pinyin[j+1];
					title_pinyin[j+1] = pinyintemp;
				}
			}
		}

		for (int i = 0; i < len; i++) {
			for (int j = 0; j < len - i - 1; j++) {
				if (song_first_name[j].compareTo(song_first_name[j+1]) > 0) {
					String temp = song_first_name[j];
					String temptitle = titles[j];
					String tempduration = duration[j];
					String tempartist = artist[j];

					song_first_name[j] = song_first_name[j+1];
					song_first_name[j+1] = temp;

					titles[j] = titles[j+1];
					titles[j+1] = temptitle;

					duration[j] = duration[j+1];
					duration[j+1] = tempduration;

					artist[j] = artist[j+1];
					artist[j+1] = tempartist;
				}
			}
		}

		for (int i = 0; i < len; i++) {
			song_first_name1[i] = song_first_name[i];
		}

		for (int i = 0; i < len - 1; i++) {
			for(int j = i + 1; j < len; j++) {
				if (song_first_name1[i].equals(song_first_name1[j])){
					song_first_name1[j] = " ";
				}
			}
		}

		int song_first_name3_id = 0;
		for (int i = 0; i < len; i++) {
			if (!(song_first_name1[i].equals(" "))) {
				song_first_name3[song_first_name3_id] = song_first_name1[i];
				song_first_name3_id++;
			}
		}
		
		for (int i = 0; i < song_first_name3.length - 1; i++) {
			if (!(song_first_name3[i] == null)) {
				String tempstring = song_first_name3[i];
				for (int j = 0; j < title_pinyin.length; j++) {
					if (tempstring.equals(title_pinyin[j].trim().substring(0,1).toUpperCase()) 
                        || tempstring.equals(title_pinyin[j].trim().substring(0,1))) {
						h = j;
						break;
					}
				}

				for(int j = 0; j < title_pinyin.length; j++) {
					if (tempstring.equals(title_pinyin[j].trim().substring(0,1).toUpperCase()) 
                        || tempstring.equals(title_pinyin[j].trim().substring(0,1))) {
						h1 = j;
					}
				}

				for (int k = h; k < h1; k++) {
					for (int m = k + 1; m < h1 + 1; m++) {
						if(titles[k].trim().substring(0, 1).compareTo(titles[m].trim().substring(0, 1)) > 0) {
							String temp = song_first_name[k];
							String temptitle = titles[k];
							String tempduration = duration[k];
							String tempartist = artist[k];

							song_first_name[k] = song_first_name[m];
							song_first_name[m] = temp;

							titles[k] = titles[m];
							titles[m] = temptitle;

							duration[k] = duration[m];
							duration[m] = tempduration;

							artist[k] = artist[m];
							artist[m] = tempartist;
						}
					}
				}
			}
		}
	}

	public static List<String> extractHeadLetter(List<String> data) {
        List<String> freshData = new ArrayList<String>();
        String headLetter = null;
        for (int i = 0; i < data.size(); i++){
            headLetter = data.get(i);
            if (headLetter.charAt(0) > 128) {
                ArrayList<Token> tokens = HanziToPinyin.getInstance().get(headLetter);
                if ((null != tokens) && (tokens.size() > 0)) {
                    Token token = tokens.get(0);
                    if (Token.PINYIN == token.type) {
                        headLetter = String.valueOf(token.target.charAt(0));
                        if (!freshData.contains(headLetter)) {
                            freshData.add(headLetter);
                        }
                    }
                }
            }
            
            headLetter = data.get(i);   
            if(!freshData.contains(headLetter)){
                freshData.add(headLetter);          
            }
        }

        //Added by GanFeng 20120129, setup the "#" group
        if (freshData.size() > 0) {
            headLetter = freshData.get(0);
            int primaryLetter = headLetter.charAt(0);
            if (primaryLetter > 128) {
                ArrayList<Token> tokens = HanziToPinyin.getInstance().get(headLetter);
                if ((null != tokens) && (tokens.size() > 0)) {
                    Token token = tokens.get(0);
                    if (Token.PINYIN != token.type) {
                        freshData.add(0, String.valueOf('#'));
                    }
                }
            }
            else if ((('#' != primaryLetter)
                    && ((primaryLetter < 'A') || (primaryLetter > 'Z')))) {
                freshData.add(0, String.valueOf('#'));
            }
        }
        
        //remove special character/number
        if (freshData.contains("#")) {
            int count = freshData.size();
            for (int i = 1; i < count; i++) {
                headLetter = freshData.get(1);
                int primaryLetter = headLetter.charAt(0);
                if (primaryLetter < 'A' || primaryLetter > 'Z') {
                    freshData.remove(1);
                    continue;
                } else {
                    return freshData;
                }                
            }
        }
        return freshData;
    }
	
	public static List<Map<String,Object>> toHeadLetterMap(List<String> data){
		Map<String, Object> map;
		List<String> strs = extractHeadLetter(data);
		List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
		for(int i = 0; i < strs.size(); i++) {
			map = new HashMap<String, Object>();
			map.put(BUTTONKEY,strs.get(i));
			result.add(map);
		}
		return result;
	}

	public void hide(Activity activity) {
		if (alpha_menu_Showed) { 
			alpha_menu_Showed = false;
			alpha_menu.startAnimation(hideAction);
			alpha_menu.setVisibility(View.GONE);
            scroller_view.removeAllViews();
            alpha_menu.removeAllViews();
			alpha_menu = null;
		} else {
            //activity.finish();
		}
	}

    public void show(){
        alpha_menu_Showed = true;
		alpha_menu.startAnimation(showAction);
		alpha_menu.setVisibility(View.VISIBLE); 
	}
}  
