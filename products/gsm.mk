# GSM APN list
PRODUCT_COPY_FILES += \
    vendor/lewa/prebuilt/common/etc/apns-conf.xml:system/etc/apns-conf.xml

# GSM SPN overrides list
PRODUCT_COPY_FILES += \
    vendor/lewa/prebuilt/common/etc/spn-conf.xml:system/etc/spn-conf.xml

PRODUCT_PACKAGES += \
    Stk
