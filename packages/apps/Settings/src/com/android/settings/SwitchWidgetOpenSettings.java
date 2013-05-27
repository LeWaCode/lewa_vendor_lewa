package com.android.settings;

import com.android.settings.R;
import com.android.settings.NotificationFirewall;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.widget.LinearLayout;
import android.provider.Settings;

public class SwitchWidgetOpenSettings extends PreferenceActivity implements
		Preference.OnPreferenceChangeListener,Preference.OnPreferenceClickListener {

	public final int SWITCH_WIDGET_STYLE_SINGLE_PAGE = 1;
	public final int SWITCH_WIDGET_STYLE_DUAL_PAGES = 2;

	private static final String KEY_SWITCH_CHECKBOX = "switch_toggle_btn";
	private static final String KEY_SWITCH_CUSTOM = "switchcustom";
	private static final String KEY_SWITCH_RESTORE = "restorenotifications";
	private static final String KEY_SHAKE_CLEAR_CHECKBOX = "shake_clear_toggle_btn";

	private CheckBoxPreference switchCheckbox;
	private PreferenceScreen switchCustom;
	private PreferenceScreen switchRestore;
	private CheckBoxPreference shakeClearCheckbox;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.switch_widget_open_settings);

		switchCheckbox = (CheckBoxPreference) findPreference(KEY_SWITCH_CHECKBOX);
		switchCheckbox.setOnPreferenceChangeListener(this);
		this.setCheckBox();

		switchCustom = (PreferenceScreen) findPreference(KEY_SWITCH_CUSTOM);
		switchCustom.setOnPreferenceChangeListener(this);
		
		switchRestore = (PreferenceScreen) findPreference(KEY_SWITCH_RESTORE);
		switchRestore.setOnPreferenceClickListener(this);

		shakeClearCheckbox = (CheckBoxPreference) findPreference(KEY_SHAKE_CLEAR_CHECKBOX);
		shakeClearCheckbox.setOnPreferenceChangeListener(this);
		setShakeClearCheckBox();

	}

	private void setCheckBox() {
		int mode = Settings.System.getInt(getContentResolver(),
				Settings.System.SWITCH_WIDGET_STYLE,
				SWITCH_WIDGET_STYLE_DUAL_PAGES);
		if (mode == SWITCH_WIDGET_STYLE_SINGLE_PAGE) {
			switchCheckbox.setChecked(false);
		} else if (mode == SWITCH_WIDGET_STYLE_DUAL_PAGES) {
			switchCheckbox.setChecked(true);
		}
	}

	private void setShakeClearCheckBox() {
		boolean checked = Settings.System.getInt(getContentResolver(),
				Settings.System.SHAKE_CLEAR_NOTIFICATIONS, 1) == 1;
		shakeClearCheckbox.setChecked(checked);
	}

	private boolean isDual() {
		boolean flag = true;
		int mode = Settings.System.getInt(getContentResolver(),
				Settings.System.SWITCH_WIDGET_STYLE,
				SWITCH_WIDGET_STYLE_DUAL_PAGES);
		if (mode == SWITCH_WIDGET_STYLE_SINGLE_PAGE) {
			flag = false;
		} else if (mode == SWITCH_WIDGET_STYLE_DUAL_PAGES) {
			flag = true;
		}
		return flag;
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		// TODO Auto-generated method stub
		final String key = preference.getKey();
		if (key.equals(KEY_SWITCH_CHECKBOX)) {
			if (isDual()) {
				Settings.System.putInt(getContentResolver(),
						Settings.System.SWITCH_WIDGET_STYLE,
						SWITCH_WIDGET_STYLE_SINGLE_PAGE);
			} else {
				Settings.System.putInt(getContentResolver(),
						Settings.System.SWITCH_WIDGET_STYLE,
						SWITCH_WIDGET_STYLE_DUAL_PAGES);
			}
		} else if (key.equals(KEY_SWITCH_CUSTOM)) {
			Intent intent = new Intent();
			intent.setClass(SwitchWidgetOpenSettings.this,
					SwitchWidgetSettings.class);
			startActivity(intent);
		} else if (key.equals(KEY_SHAKE_CLEAR_CHECKBOX)) {
			Settings.System.putInt(getContentResolver(),
					Settings.System.SHAKE_CLEAR_NOTIFICATIONS,
					shakeClearCheckbox.isChecked() ? 0 : 1);
		}
		return true;
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		final String key=preference.getKey();
		if(key.equals(KEY_SWITCH_RESTORE)){
			LinearLayout inputLayout = (LinearLayout) getLayoutInflater().inflate(
					R.layout.switch_clear_history_dialog, null);
			AlertDialog.Builder builder = new AlertDialog.Builder(
					SwitchWidgetOpenSettings.this);
			builder.setView(inputLayout);
			builder.setTitle(R.string.switch_clear_history);

			builder.setPositiveButton(R.string.switch_label_ok,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							NotificationFirewall.getInstance(SwitchWidgetOpenSettings.this).removeAllBlockPackage();
						}
					});
			builder.setNegativeButton(R.string.switch_label_cancel,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
						}
					});
			builder.show();
		}
		return true;
	}
}
