# Generic lewamod product
PRODUCT_NAME := lewa
PRODUCT_BRAND := lewa
PRODUCT_DEVICE := generic

PRODUCT_BUILD_PROP_OVERRIDES += BUILD_UTC_DATE=0

# Used by BusyBox
KERNEL_MODULES_DIR:=/system/lib/modules

# Tiny toolbox
TINY_TOOLBOX:=true

PRODUCT_PROPERTY_OVERRIDES += \
    ro.url.legal=http://www.google.com/intl/%s/mobile/android/basic/phone-legal.html \
    ro.url.legal.android_privacy=http://www.google.com/intl/%s/mobile/android/basic/privacy.html \
    ro.com.google.clientidbase=android-google \
    ro.com.android.wifi-watchlist=GoogleGuest \
    ro.setupwizard.enterprise_mode=1 \
    ro.com.android.dateformat=MM-dd-yyyy \
    ro.com.android.dataroaming=false

# added by george,for swapper
PRODUCT_PROPERTY_OVERRIDES += \
    ro.lewa.swapper.flash_swappiness=99 \
    ro.lewa.swapper.sd_swappiness=60 \
    ro.lewa.swapper.part_path=/dev/block/mmcblk0p3 \
    ro.error.receiver.system.apps=com.lewa.fc \
    ro.error.receiver.default=com.lewa.fc \
    persist.sys.notif-speaker=1 \
    persist.sys.ring-speaker=1 \
    persist.sys.alarm-speaker=1 \
    

# lewaMod specific product packages
PRODUCT_PACKAGES += \
    liblewa-dsp \
    Pacman \
    screenshot \
    CMParts \
    CMScreenshot

# Extra tools in lewaMod
PRODUCT_PACKAGES += \
    openvpn

# Copy over the changelog to the device
PRODUCT_COPY_FILES += \
    vendor/lewa/CHANGELOG.mkdn:system/etc/CHANGELOG-CM.txt

# Common CM overlay
PRODUCT_PACKAGE_OVERLAYS += vendor/lewa/overlay/common

# Lewa Perbuilt
PRODUCT_COPY_FILES += \
    vendor/lewa/prebuilt/common/bin/backuptool.sh:system/bin/backuptool.sh \
    vendor/lewa/prebuilt/common/bin/modelid_cfg.sh:system/bin/modelid_cfg.sh \
    vendor/lewa/prebuilt/common/bin/verify_cache_partition_size.sh:system/bin/verify_cache_partition_size.sh \
    vendor/lewa/prebuilt/common/etc/resolv.conf:system/etc/resolv.conf \
    vendor/lewa/prebuilt/common/etc/sysctl.conf:system/etc/sysctl.conf \
    vendor/lewa/prebuilt/common/etc/terminfo/l/linux:system/etc/terminfo/l/linux \
    vendor/lewa/prebuilt/common/etc/terminfo/u/unknown:system/etc/terminfo/u/unknown \
    vendor/lewa/prebuilt/common/etc/profile:system/etc/profile \
    vendor/lewa/prebuilt/common/etc/init.partner.sh:system/etc/init.partner.sh \
    vendor/lewa/prebuilt/common/etc/init.local.rc:system/etc/init.local.rc \
    vendor/lewa/prebuilt/common/etc/init.d/00banner:system/etc/init.d/00banner \
    vendor/lewa/prebuilt/common/etc/init.d/01sysctl:system/etc/init.d/01sysctl \
    vendor/lewa/prebuilt/common/etc/init.d/03firstboot:system/etc/init.d/03firstboot \
    vendor/lewa/prebuilt/common/etc/init.d/04modules:system/etc/init.d/04modules \
    vendor/lewa/prebuilt/common/etc/init.d/05mountsd:system/etc/init.d/05mountsd \
    vendor/lewa/prebuilt/common/etc/init.d/06mountdl:system/etc/init.d/06mountdl \
    vendor/lewa/prebuilt/common/etc/init.d/20userinit:system/etc/init.d/20userinit \
    vendor/lewa/prebuilt/common/bin/handle_compcache:system/bin/handle_compcache \
    vendor/lewa/prebuilt/common/bin/compcache:system/bin/compcache \
    vendor/lewa/prebuilt/common/bin/fix_permissions:system/bin/fix_permissions \
    vendor/lewa/prebuilt/common/bin/sysinit:system/bin/sysinit \
    vendor/lewa/prebuilt/common/xbin/htop:system/xbin/htop \
    vendor/lewa/prebuilt/common/xbin/irssi:system/xbin/irssi \
    vendor/lewa/prebuilt/common/xbin/powertop:system/xbin/powertop \
    vendor/lewa/prebuilt/common/xbin/openvpn-up.sh:system/xbin/openvpn-up.sh \
    vendor/lewa/prebuilt/common/etc/init.d/51clean:system/etc/init.d/51clean \
    vendor/lewa/prebuilt/common/misc/1-app2sd.sh:system/misc/1-app2sd.sh \
    vendor/lewa/prebuilt/common/misc/2-data2ext.sh:system/misc/2-data2ext.sh

# Enable SIP+VoIP on all targets
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/android.software.sip.voip.xml:system/etc/permissions/android.software.sip.voip.xml

ifdef LEWA_WITH_GOOGLE

# use all present proprietary apk
PRODUCT_COPY_FILES += $(shell test -f vendor/lewa/proprietary/*.apk && \
    find vendor/lewa/proprietary -name '*.apk' \
    -printf '%p:system/app/%f ')

# use all present proprietary lib
PRODUCT_COPY_FILES += $(shell test -f vendor/lewa/proprietary/*.so && \
    find vendor/lewa/proprietary -name '*.so' \
    -printf '%p:system/lib/%f ')

# use all present proprietary jar
PRODUCT_COPY_FILES += $(shell test -f vendor/lewa/proprietary/*.jar && \
    find vendor/lewa/proprietary -name '*.jar' \
    -printf '%p:system/framework/%f ')

# use all present proprietary xml (permissions)
PRODUCT_COPY_FILES += $(shell test -f vendor/lewa/proprietary/*.xml && \
    find vendor/lewa/proprietary -name '*.xml' \
    -printf '%p:system/etc/permissions/%f ')

else
PRODUCT_PACKAGES += \
    Provision \
    GoogleSearch
endif

# Lewa GoogleApp
PRODUCT_COPY_FILES += \
    packages/apps/GoogleApp/etc/permissions/features.xml:/system/etc/permissions/features.xml \
    packages/apps/GoogleApp/etc/permissions/com.google.android.maps.xml:/system/etc/permissions/com.google.android.maps.xml \
    packages/apps/GoogleApp/framework/com.google.android.maps.jar:/system/framework/com.google.android.maps.jar \
    packages/apps/GoogleApp/lib/libvoicesearch.so:/system/lib/libvoicesearch.so \
    packages/apps/GoogleApp/app/Vending.apk:/system/app/Vending.apk \
    packages/apps/GoogleApp/app/MarketUpdater.apk:/system/app/MarketUpdater.apk \
    packages/apps/GoogleApp/app/GoogleServicesFramework.apk:/system/app/GoogleServicesFramework.apk \
    packages/apps/GoogleApp/app/GooglePartnerSetup.apk:/system/app/GooglePartnerSetup.apk \
    packages/apps/GoogleApp/app/GoogleContactsSyncAdapter.apk:/system/app/GoogleContactsSyncAdapter.apk \
    packages/apps/GoogleApp/app/GoogleCalendarSyncAdapter.apk:/system/app/GoogleCalendarSyncAdapter.apk \
    packages/apps/GoogleApp/app/GoogleBackupTransport.apk:/system/app/GoogleBackupTransport.apk \
    packages/apps/GoogleApp/app/NetworkLocation.apk:/system/app/NetworkLocation.apk \
    packages/apps/GoogleApp/lib/libmicrobes_jni.so:/system/lib/libmicrobes_jni.so \
    packages/apps/GoogleApp/lib/libtalk_jni.so:/system/lib/libtalk_jni.so \

# default IME
PRODUCT_PACKAGES += LatinIME


# Include extra dictionaries for LatinIME
PRODUCT_PACKAGE_OVERLAYS += vendor/lewa/overlay/dictionaries

# Default ringtone
PRODUCT_PROPERTY_OVERRIDES += \
    ro.config.ringtone=Champagne_Edition.ogg \
    ro.config.notification_sound=regulus.ogg \
    ro.config.alarm_alert=Alarm_Beep_03.ogg
