# Inherit AOSP device configuration for dream_sapphire.
$(call inherit-product, device/huawei/u8800x/device_u8800x.mk)

# Inherit some common cyanogenmod stuff.
$(call inherit-product, vendor/cyanogen/products/common.mk)

# Include GSM-only stuff
$(call inherit-product, vendor/cyanogen/products/gsm.mk)

#
# Setup device specific product configuration.
#
PRODUCT_NAME := cyanogen_u8800x
PRODUCT_BRAND := huawei
PRODUCT_DEVICE := u8800x
PRODUCT_MODEL := U8800
PRODUCT_MANUFACTURER := Huawei
PRODUCT_BUILD_PROP_OVERRIDES += PRODUCT_NAME=u8800 BUILD_ID=GRJ22 BUILD_DISPLAY_ID=GWK74 BUILD_FINGERPRINT=google/passion/passion:2.3.4/GRJ22/121341:user/release-keys PRIVATE_BUILD_DESC="passion-user 2.3.4 GRJ22 121341 release-keys"

# Release name and versioning
PRODUCT_RELEASE_NAME := U8800
PRODUCT_VERSION_DEVICE_SPECIFIC :=
-include vendor/cyanogen/products/common_versions.mk


#
# Copy DS specific prebuilt files
#


