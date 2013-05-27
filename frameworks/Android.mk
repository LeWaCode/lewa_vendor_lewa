LOCAL_PATH:= $(call my-dir)

# the library
# ============================================================
include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(call all-java-files-under, java android)

#LOCAL_NO_STANDARD_LIBRARIES := true

LOCAL_MODULE := lewaos
LOCAL_JAVA_LIBRARIES := framework
#LOCAL_MODULE_CLASS := JAVA_LIBRARIES
LOCAL_MODULE_TAGS := optional

#LOCAL_NO_EMMA_INSTRUMENT := true
#LOCAL_NO_EMMA_COMPILE := true

#LOCAL_DX_FLAGS := --core-library
#include $(BUILD_JAVA_LIBRARY)
include $(BUILD_STATIC_JAVA_LIBRARY)

# additionally, build unit tests in a separate .apk
# include $(call all-makefiles-under,$(LOCAL_PATH))

include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := lewacore:libs/lewacore.jar 
include $(BUILD_MULTI_PREBUILT)
