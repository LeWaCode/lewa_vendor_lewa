LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional


LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_STATIC_JAVA_LIBRARIES := lewaos gexinIM

LOCAL_REQUIRED_MODULES := SoundRecorder
LOCAL_PACKAGE_NAME := PIM
LOCAL_CERTIFICATE := shared

LOCAL_PROGUARD_FLAG_FILES := proguard.cfg

#LOCAL_PROGUARD_ENABLED := full

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)

#LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := pinyin4:lib/pinyin4j-2.5.0.jar 
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := gexinIM:lib/gexinIM.jar
include $(BUILD_MULTI_PREBUILT)
 
