//package com.lewa.spm.adapter;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import com.lewa.spm.activity.CurrModeActivity;
//import com.lewa.spm.mode.PowerSavingMode;
//
//import com.lewa.spm.R;
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.BaseAdapter;
//import android.widget.CompoundButton;
//import android.widget.CompoundButton.OnCheckedChangeListener;
//import android.widget.ImageView;
//import android.widget.RadioButton;
//import android.widget.TextView;
//
//public class ModeAdapter extends BaseAdapter{
//	PowerSavingMode getModeType;
//	ModeInfo modeInfo;
//	private Context mContext;
//	private int list_count;
//	private List<ModeInfo> modeInfoList;
//	private int tempRadioBtId = -1;
//	
//	
//	public ModeAdapter(Context mContext) {
//		super();
//		this.mContext = mContext;
//		getModeType = new PowerSavingMode(mContext);
//	}
//	
//	@SuppressWarnings("unused")
//	private final class ModeViewHolder{
//		ImageView mModeIcon;
//		TextView mModeNameTxt;
//		TextView mModeSummaryTxt;
//		RadioButton mModeRadio;
//	}
//
//
//	public void setListNum(int num){
//		list_count = num;
//	}
//
//	@Override
//	public int getCount() {
//		return list_count;
//	}
//
//	@Override
//	public Object getItem(int id) {
//		return id;
//	}
//
//	@Override
//	public long getItemId(int position) {
//		return position;
//	}
//
//	@Override
//	public View getView(int position, View convertView, ViewGroup parent) {
//		modeInfoList = new ArrayList<ModeInfo>();
//		modeInfoList = getModeType.setTypeToList();
//		modeInfo = new ModeInfo();
//		modeInfo = modeInfoList.get(position);
//		ModeViewHolder holder = null;
//		if (convertView == null) {
//			View view = LayoutInflater.from(mContext).inflate(R.layout.mode_choice_list_item, null);
//			holder = new ModeViewHolder();
//			holder.mModeIcon = (ImageView)view.findViewById(R.id.spm_mode_icon);
//			holder.mModeNameTxt = (TextView)view.findViewById(R.id.spm_mode_text);
//			holder.mModeSummaryTxt = (TextView)view.findViewById(R.id.spm_mode_summary);
//			holder.mModeRadio = (RadioButton)view.findViewById(R.id.spm_mode_radiobt);
//			holder.mModeRadio.setId(position);
//			view.setTag(holder);
//			convertView = view;
//		} else {
//			holder = (ModeViewHolder)convertView.getTag();
//		}
//			holder.mModeRadio.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//				
//				@Override
//				public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
//					if(isChecked){
//                        if(tempRadioBtId != -1){
//                            RadioButton tempButton = (RadioButton) CurrModeActivity.this.findViewById(tempRadioBtId);
//                            if(tempButton != null){
//                               tempButton.setChecked(false);
//                            }
//                            
//                        }
//                        
//                        tempRadioBtId = arg0.getId();
//                    }
//                }
//            });
//            

//            if(tempRadioBtId == position){
//            	holder.mModeRadio.setChecked(true);
//            }
//					
//			holder.mModeIcon.setImageDrawable(modeInfo.modeIcon);
//			holder.mModeNameTxt.setText(modeInfo.modeName);
//			holder.mModeSummaryTxt.setText(modeInfo.modeExplain);
//			return convertView;
//	}
//
//}
