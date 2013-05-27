LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional


LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_SRC_FILES += \
  src/com/lewa/player/IMediaPlaybackService.aidl

LOCAL_STATIC_JAVA_LIBRARIES :=lewaos \
				              entaged

LOCAL_PACKAGE_NAME := LewaPlayer
LOCAL_CERTIFICATE := shared

LOCAL_OVERRIDES_PACKAGES := Music

LOCAL_PROGUARD_FLAG_FILES := proguard.cfg
LOCAL_PROGUARD_ENABLED := full


include $(BUILD_PACKAGE)

include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := entaged:lib/entagged.jar
include $(BUILD_MULTI_PREBUILT)
 
