#
# Lewa Mod Package
#

include vendor/lewa/prebuilt/sounds/LewaAudio.mk

LOCAL_PATH:= vendor/lewa/prebuilt

# usr
PRODUCT_COPY_FILES += \
    $(LOCAL_PATH)/system/usr/share/phoneloc.dat:system/usr/share/phoneloc.dat

# media
PRODUCT_COPY_FILES += \
    $(LOCAL_PATH)/system/media/$(LEWA_DPI)/lockscreen.zip:/system/media/lockscreen.zip \
    $(LOCAL_PATH)/system/media/$(LEWA_DPI)/default.lwt:/system/media/default.lwt \
    $(LOCAL_PATH)/system/media/$(LEWA_DPI)/bootanimation.zip:system/media/bootanimation.zip
