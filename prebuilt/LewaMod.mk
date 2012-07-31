#
# Lewa Mod Package
#

include vendor/lewa/prebuilt/sounds/LewaAudio.mk

LOCAL_PATH:= vendor/lewa/prebuilt

# App
PRODUCT_COPY_FILES += \
    $(LOCAL_PATH)/system/app/$(LEWA_DPI)/AccountAndSyncSettings.apk:system/app/AccountAndSyncSettings.apk \
    $(LOCAL_PATH)/system/app/$(LEWA_DPI)/AMAP.apk:system/app/AMAP.apk \
    $(LOCAL_PATH)/system/app/$(LEWA_DPI)/Calendar365.apk:system/app/Calendar365.apk \
    $(LOCAL_PATH)/system/app/$(LEWA_DPI)/ContactsProvider.apk:system/app/ContactsProvider.apk \
    $(LOCAL_PATH)/system/app/$(LEWA_DPI)/Cstore.apk:system/app/Cstore.apk \
    $(LOCAL_PATH)/system/app/$(LEWA_DPI)/DeskClock.apk:system/app/DeskClock.apk \
    $(LOCAL_PATH)/system/app/$(LEWA_DPI)/DownloadProvider.apk:system/app/DownloadProvider.apk \
    $(LOCAL_PATH)/system/app/$(LEWA_DPI)/DownloadProviderUi.apk:system/app/DownloadProviderUi.apk \
    $(LOCAL_PATH)/system/app/$(LEWA_DPI)/GexinService.apk:system/app/GexinService.apk \
    $(LOCAL_PATH)/system/app/$(LEWA_DPI)/Intercept.apk:system/app/Intercept.apk \
    $(LOCAL_PATH)/system/app/$(LEWA_DPI)/LabiSync.apk:system/app/LabiSync.apk \
    $(LOCAL_PATH)/system/app/$(LEWA_DPI)/LewaFace.apk:system/app/LewaFace.apk \
    $(LOCAL_PATH)/system/app/$(LEWA_DPI)/LewaFc.apk:system/app/LewaFc.apk \
    $(LOCAL_PATH)/system/app/$(LEWA_DPI)/LewaFeedback.apk:system/app/LewaFeedback.apk \
    $(LOCAL_PATH)/system/app/$(LEWA_DPI)/LewaFileManager.apk:system/app/LewaFileManager.apk \
    $(LOCAL_PATH)/system/app/$(LEWA_DPI)/LeWaFM.apk:system/app/LeWaFM.apk \
    $(LOCAL_PATH)/system/app/$(LEWA_DPI)/LewaLauncher.apk:system/app/LewaLauncher.apk \
    $(LOCAL_PATH)/system/app/$(LEWA_DPI)/LewaPlayer.apk:system/app/LewaPlayer.apk \
    $(LOCAL_PATH)/system/app/$(LEWA_DPI)/LewaPond.apk:system/app/LewaPond.apk \
    $(LOCAL_PATH)/system/app/$(LEWA_DPI)/LewaPush.apk:system/app/LewaPush.apk \
    $(LOCAL_PATH)/system/app/$(LEWA_DPI)/LewaSearch.apk:system/app/LewaSearch.apk \
    $(LOCAL_PATH)/system/app/$(LEWA_DPI)/LewaUpdater.apk:system/app/LewaUpdater.apk \
    $(LOCAL_PATH)/system/app/$(LEWA_DPI)/LocationProvider.apk:system/app/LocationProvider.apk \
    $(LOCAL_PATH)/system/app/$(LEWA_DPI)/MediaProvider.apk:system/app/MediaProvider.apk \
    $(LOCAL_PATH)/system/app/$(LEWA_DPI)/PackageInstaller.apk:system/app/PackageInstaller.apk \
    $(LOCAL_PATH)/system/app/$(LEWA_DPI)/Phone_$(LEWA_PHONE).apk:system/app/Phone.apk \
    $(LOCAL_PATH)/system/app/$(LEWA_DPI)/PicFolder.apk:system/app/PicFolder.apk \
    $(LOCAL_PATH)/system/app/$(LEWA_DPI)/PIM.apk:system/app/PIM.apk \
    $(LOCAL_PATH)/system/app/$(LEWA_DPI)/PlayPlusClient_Lewa.apk:system/app/PlayPlusClient_Lewa.apk \
    $(LOCAL_PATH)/system/app/$(LEWA_DPI)/power+.apk:system/app/power+.apk \
    $(LOCAL_PATH)/system/app/$(LEWA_DPI)/Settings.apk:system/app/Settings.apk \
    $(LOCAL_PATH)/system/app/$(LEWA_DPI)/SettingsProvider.apk:system/app/SettingsProvider.apk \
    $(LOCAL_PATH)/system/app/$(LEWA_DPI)/Swapper.apk:system/app/Swapper.apk \
    $(LOCAL_PATH)/system/app/$(LEWA_DPI)/SystemUI.apk:system/app/SystemUI.apk \
    $(LOCAL_PATH)/system/app/$(LEWA_DPI)/TelephonyProvider.apk:system/app/TelephonyProvider.apk

# bin
PRODUCT_COPY_FILES += \
    $(LOCAL_PATH)/system/bin/su0:system/bin/su0

# lib
PRODUCT_COPY_FILES += \
    $(LOCAL_PATH)/system/lib/libams.so:system/lib/libams.so \
    $(LOCAL_PATH)/system/lib/libapkchecker.so:system/lib/libapkchecker.so \
    $(LOCAL_PATH)/system/lib/libapollo.so:system/lib/libapollo.so \
    $(LOCAL_PATH)/system/lib/libcryptor.so:system/lib/libcryptor.so \
    $(LOCAL_PATH)/system/lib/liblocation.so:system/lib/liblocation.so \
    $(LOCAL_PATH)/system/lib/libmicrobes_jni.so:system/lib/libmicrobes_jni.so \
    $(LOCAL_PATH)/system/lib/libminimapv315.so:system/lib/libminimapv315.so \
    $(LOCAL_PATH)/system/lib/libphoneloc_jni.so:system/lib/libphoneloc_jni.so \
    $(LOCAL_PATH)/system/lib/libqpicjni87.so:system/lib/libqpicjni87.so \
    $(LOCAL_PATH)/system/lib/libsmschecker.so:system/lib/libsmschecker.so

# framework
PRODUCT_COPY_FILES += \
    $(LOCAL_PATH)/system/framework/$(LEWA_DPI)/android.policy.jar:system/framework/android.policy.jar \
    $(LOCAL_PATH)/system/framework/$(LEWA_DPI)/framework.jar:system/framework/framework.jar \
    $(LOCAL_PATH)/system/framework/$(LEWA_DPI)/services.jar:system/framework/services.jar

# usr
PRODUCT_COPY_FILES += \
    $(LOCAL_PATH)/system/usr/share/phoneloc.dat:system/usr/share/phoneloc.dat

# media
PRODUCT_COPY_FILES += \
    $(LOCAL_PATH)/system/media/$(LEWA_DPI)/lockscreen.zip:/system/media/lockscreen.zip \
    $(LOCAL_PATH)/system/media/$(LEWA_DPI)/default.lwt:/system/media/default.lwt \
    $(LOCAL_PATH)/system/media/$(LEWA_DPI)/bootanimation.zip:system/media/bootanimation.zip
