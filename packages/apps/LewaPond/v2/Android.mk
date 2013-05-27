LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional


LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_STATIC_JAVA_LIBRARIES := lewacore

LOCAL_PACKAGE_NAME := LewaPond
LOCAL_CERTIFICATE := shared

LOCAL_PROGUARD_FLAG_FILES := proguard.cfg

#LOCAL_PROGUARD_ENABLED := full

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)
#LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := hudee:lib/butterfly.jar  \
#                                       hudeeproto:lib/protobuf.jar
										
include $(BUILD_MULTI_PREBUILT)
 
