LOCAL_PATH:= $(call my-dir)

# app
PRODUCT_COPY_FILES += \
    $(LOCAL_PATH)/app/PicFolder.apk:system/app/PicFolder.apk \
    $(LOCAL_PATH)/app/iReader.apk:system/app/iReader.apk \
    $(LOCAL_PATH)/app/DolphinBrowser.apk:system/app/DolphinBrowser.apk\
    $(LOCAL_PATH)/app/lewamarket.apk:/system/app/lewamarket.apk 
    
#    $(LOCAL_PATH)/app/AMAP.apk:system/app/AMAP.apk \
#    $(LOCAL_PATH)/app/Calendar365.apk:system/app/Calendar365.apk \
#    $(LOCAL_PATH)/app/GexinService.apk:system/app/GexinService.apk \
#    $(LOCAL_PATH)/app/PlayPlusClient_Lewa.apk:system/app/PlayPlusClient_Lewa.apk \


# lib
PRODUCT_COPY_FILES += \
    $(LOCAL_PATH)/lib/libams-1.1.0.so:system/lib/libams-1.1.0.so \
    $(LOCAL_PATH)/lib/libapollo-1.1.2.so:system/lib/libapollo-1.1.2.so \
    $(LOCAL_PATH)/lib/libcryptor-1.0.0.so:system/lib/libcryptor-1.0.0.so \
    $(LOCAL_PATH)/lib/liblbs.so:system/lib/liblbs.so \
    $(LOCAL_PATH)/lib/liblocation-1.0.0.so:system/lib/liblocation-1.0.0.so \
    $(LOCAL_PATH)/lib/libnative-1.0.0.so:system/lib/libnative-1.0.0.so \
    $(LOCAL_PATH)/lib/libsmschecker-1.0.1.so:system/lib/libsmschecker-1.0.1.so \
    $(LOCAL_PATH)/lib/libminimapv315.so:system/lib/libminimapv315.so \
    $(LOCAL_PATH)/lib/libqpicjni88.so:system/lib/libqpicjni88.so \
    $(LOCAL_PATH)/lib/libiReader_common.so:system/lib/libiReader_common.so \
    $(LOCAL_PATH)/lib/libiReader_ebk3parser.so:system/lib/libiReader_ebk3parser.so \
    $(LOCAL_PATH)/lib/libiReader_epubparser.so:system/lib/libiReader_epubparser.so \
    $(LOCAL_PATH)/lib/libiReader_filezip.so:system/lib/libiReader_filezip.so \
    $(LOCAL_PATH)/lib/libiReader_icu.so:system/lib/libiReader_icu.so \
    $(LOCAL_PATH)/lib/libiReader_txtparser.so:system/lib/libiReader_txtparser.so \
    


#    $(LOCAL_PATH)/lib/libmobilesafe360-jni.so:system/lib/libmobilesafe360-jni.so \

