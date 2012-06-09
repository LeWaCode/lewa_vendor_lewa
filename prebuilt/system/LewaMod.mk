#
# Lewa Mod Package
#

LOCAL_PATH:= vendor/lewa/prebuilt/system

ifeq ($(LEWA_PHONE),cdma)
PRODUCT_COPY_FILES += \
    $(LOCAL_PATH)/app/Phone_cdma.apk:system/app/Phone.apk
endif

ifeq ($(LEWA_PHONE),gsm)
PRODUCT_COPY_FILES += \
    $(LOCAL_PATH)/app/Phone_gsm.apk:system/app/Phone.apk
endif

# App
PRODUCT_COPY_FILES += \
    $(LOCAL_PATH)/app/AccountAndSyncSettings.apk:system/app/AccountAndSyncSettings.apk \
    $(LOCAL_PATH)/app/AMAP.apk:system/app/AMAP.apk \
    $(LOCAL_PATH)/app/Calendar365.apk:system/app/Calendar365.apk \
    $(LOCAL_PATH)/app/ContactsProvider.apk:system/app/ContactsProvider.apk \
    $(LOCAL_PATH)/app/Cstore.apk:system/app/Cstore.apk \
    $(LOCAL_PATH)/app/DeskClock.apk:system/app/DeskClock.apk \
    $(LOCAL_PATH)/app/DownloadProvider.apk:system/app/DownloadProvider.apk \
    $(LOCAL_PATH)/app/DownloadProviderUi.apk:system/app/DownloadProviderUi.apk \
    $(LOCAL_PATH)/app/GexinService.apk:system/app/GexinService.apk \
    $(LOCAL_PATH)/app/Intercept.apk:system/app/Intercept.apk \
    $(LOCAL_PATH)/app/LabiSync.apk:system/app/LabiSync.apk \
    $(LOCAL_PATH)/app/LewaFace.apk:system/app/LewaFace.apk \
    $(LOCAL_PATH)/app/LewaFc.apk:system/app/LewaFc.apk \
    $(LOCAL_PATH)/app/LewaFeedback.apk:system/app/LewaFeedback.apk \
    $(LOCAL_PATH)/app/LewaFileManager.apk:system/app/LewaFileManager.apk \
    $(LOCAL_PATH)/app/LeWaFM.apk:system/app/LeWaFM.apk \
    $(LOCAL_PATH)/app/LewaLauncher.apk:system/app/LewaLauncher.apk \
    $(LOCAL_PATH)/app/LewaPlayer.apk:system/app/LewaPlayer.apk \
    $(LOCAL_PATH)/app/LewaPond.apk:system/app/LewaPond.apk \
    $(LOCAL_PATH)/app/LewaPush.apk:system/app/LewaPush.apk \
    $(LOCAL_PATH)/app/LewaUpdater.apk:system/app/LewaUpdater.apk \
    $(LOCAL_PATH)/app/LocationProvider.apk:system/app/LocationProvider.apk \
    $(LOCAL_PATH)/app/MediaProvider.apk:system/app/MediaProvider.apk \
    $(LOCAL_PATH)/app/PackageInstaller.apk:system/app/PackageInstaller.apk \
    $(LOCAL_PATH)/app/PicFolder.apk:system/app/PicFolder.apk \
    $(LOCAL_PATH)/app/PIM.apk:system/app/PIM.apk \
    $(LOCAL_PATH)/app/PlayPlusClient_Lewa.apk:system/app/PlayPlusClient_Lewa.apk \
    $(LOCAL_PATH)/app/Power_Manager.apk:system/app/Power_Manager.apk \
    $(LOCAL_PATH)/app/Settings.apk:system/app/Settings.apk \
    $(LOCAL_PATH)/app/SettingsProvider.apk:system/app/SettingsProvider.apk \
    $(LOCAL_PATH)/app/Swapper.apk:system/app/Swapper.apk \
    $(LOCAL_PATH)/app/SystemUI.apk:system/app/SystemUI.apk \
    $(LOCAL_PATH)/app/TelephonyProvider.apk:system/app/TelephonyProvider.apk

# lib
PRODUCT_COPY_FILES += \
    $(LOCAL_PATH)/lib/libams.so:system/lib/libams.so \
    $(LOCAL_PATH)/lib/libapkchecker.so:system/lib/libapkchecker.so \
    $(LOCAL_PATH)/lib/libapollo.so:system/lib/libapollo.so \
    $(LOCAL_PATH)/lib/libcryptor.so:system/lib/libcryptor.so \
    $(LOCAL_PATH)/lib/liblocation.so:system/lib/liblocation.so \
    $(LOCAL_PATH)/lib/libmicrobes_jni.so:system/lib/libmicrobes_jni.so \
    $(LOCAL_PATH)/lib/libminimapv315.so:system/lib/libminimapv315.so \
    $(LOCAL_PATH)/lib/libphoneloc_jni.so:system/lib/libphoneloc_jni.so \
    $(LOCAL_PATH)/lib/libqpicjni86.so:system/lib/libqpicjni86.so \
    $(LOCAL_PATH)/lib/libsmschecker.so:system/lib/libsmschecker.so

# framework
PRODUCT_COPY_FILES += \
    $(LOCAL_PATH)/framework/android.policy.jar:system/framework/android.policy.jar \
    $(LOCAL_PATH)/framework/framework.jar:system/framework/framework.jar \
    $(LOCAL_PATH)/framework/services.jar:system/framework/services.jar

# usr
PRODUCT_COPY_FILES += \
    $(LOCAL_PATH)/usr/share/phoneloc.dat:system/usr/share/phoneloc.dat
