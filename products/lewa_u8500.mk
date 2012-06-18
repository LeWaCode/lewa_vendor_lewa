# Inherit AOSP device configuration for dream_sapphire.
$(call inherit-product, device/huawei/u8500/u8500.mk)

# Inherit some common cyanogenmod stuff.
$(call inherit-product, vendor/cyanogen/products/common.mk)

# Include GSM-only stuff
$(call inherit-product, vendor/cyanogen/products/gsm.mk)

#
# Setup device specific product configuration.
#
PRODUCT_NAME := cyanogen_u8500
PRODUCT_BRAND := huawei
PRODUCT_DEVICE := u8500
PRODUCT_MODEL := U8500
PRODUCT_MANUFACTURER := Huawei
PRODUCT_BUILD_PROP_OVERRIDES += PRODUCT_NAME=u8500 BUILD_ID=GWK74 BUILD_DISPLAY_ID=GWK74 BUILD_FINGERPRINT=google/passion/passion:2.3.7/GWK74/121341:user/release-keys PRIVATE_BUILD_DESC="passion-user 2.3.7 GWK74 121341 release-keys"

# Release name and versioning
PRODUCT_RELEASE_NAME := U8500
PRODUCT_VERSION_DEVICE_SPECIFIC :=
-include vendor/cyanogen/products/common_versions.mk

#
# Copy  specific prebuilt files
#


