# Inherit AOSP device configuration for dream_sapphire.
$(call inherit-product, device/huawei/c8500/c8500.mk)

# Inherit some common cyanogenmod stuff.
$(call inherit-product, vendor/cyanogen/products/common_full.mk)

# Include GSM-only stuff
$(call inherit-product, vendor/cyanogen/products/gsm.mk)

#
# Setup device specific product configuration.
#
PRODUCT_NAME := cyanogen_c8500
PRODUCT_BRAND := huawei
PRODUCT_DEVICE := c8500
PRODUCT_MODEL := C8500
PRODUCT_MANUFACTURER := Huawei
PRODUCT_BUILD_PROP_OVERRIDES += PRODUCT_NAME=c8500 BUILD_ID=GRJ22 BUILD_DISPLAY_ID=GWK74 BUILD_FINGERPRINT=google/passion/passion:2.3.4/GRJ22/121341:user/release-keys PRIVATE_BUILD_DESC="passion-user 2.3.4 GRJ22 121341 release-keys"

# Release name and versioning
PRODUCT_RELEASE_NAME := C8500
PRODUCT_VERSION_DEVICE_SPECIFIC :=
-include vendor/cyanogen/products/common_versions.mk

#
# Copy  specific prebuilt files
#


