LOCAL_PATH:= $(call my-dir)

ifneq ($(TARGET_SIMULATOR),true)
ifeq ($(BOARD_HAVE_FM_RADIO),true)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-subdir-java-files) \
    src/com/android/fm/radio/IFMRadioServiceCallbacks.aidl \
    src/com/android/fm/radio/IFMRadioService.aidl \

LOCAL_PACKAGE_NAME := LeWaFM
LOCAL_CERTIFICATE := platform

LOCAL_PROGUARD_FLAG_FILES := proguard.cfg
LOCAL_PROGUARD_ENABLED := full

include $(BUILD_PACKAGE)

endif # TARGET_SIMULATOR
endif # BOARD_HAVE_FM_RADIO
