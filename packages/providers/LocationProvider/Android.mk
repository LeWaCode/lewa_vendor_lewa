LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

# Only compile source java files in this apk.
LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_JAVA_LIBRARIES := ext

LOCAL_STATIC_JAVA_LIBRARIES += android-common

# The Emma tool analyzes code coverage when running unit tests on the
# application. This configuration line selects which packages will be analyzed,
# leaving out code which is tested by other means (e.g. static libraries) that
# would dilute the coverage results. These options do not affect regular
# production builds.
LOCAL_EMMA_COVERAGE_FILTER := +com.lewa.providers.location.*
LOCAL_JNI_SHARED_LIBRARIES := libphoneloc_jni

LOCAL_PACKAGE_NAME := LocationProvider
LOCAL_CERTIFICATE := shared

include $(BUILD_PACKAGE)

PRODUCT_COPY_FILES += $(LOCAL_PATH)/data/phoneloc.dat:system/usr/share/phoneloc.dat 

# Use the following include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))
