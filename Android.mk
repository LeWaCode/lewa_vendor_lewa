# We need a way to prevent the stuff Google Apps replaces from being included in the build.
# This is a hacky way to do that.
ifdef LEWA_WITH_GOOGLE
    PACKAGES.Email.OVERRIDES := Provision QuickSearchBox
endif

# added by ioz9 for lewaMod ,2012-04-21
include vendor/lewa/prebuilt/system/LewaMod.mk
include vendor/lewa/prebuilt/frameworks/base/data/sounds/LewaAudio.mk

LOCAL_PATH:= vendor/lewa/prebuilt

ifeq ($(LEWA_DPI),mdpi)
PRODUCT_COPY_FILES += \
    $(LOCAL_PATH)/frameworks/lockscreen/mdpi/lockscreen.zip:/system/media/lockscreen.zip \
    $(LOCAL_PATH)/frameworks/theme/mdpi/default.lwt:/system/media/default.lwt \
    $(LOCAL_PATH)/frameworks/bootanimation/mdpi/media/bootanimation.zip:system/media/bootanimation.zip
endif

ifeq ($(LEWA_DPI),hdpi)
PRODUCT_COPY_FILES += \
    $(LOCAL_PATH)/frameworks/lockscreen/hdpi/lockscreen.zip:/system/media/lockscreen.zip \
    $(LOCAL_PATH)/frameworks/theme/hdpi/default.lwt:/system/media/default.lwt \
    $(LOCAL_PATH)/frameworks/bootanimation/hdpi/media/bootanimation.zip:system/media/bootanimation.zip
endif
