# Inherit AOSP device configuration for z71.
$(call inherit-product, device/commtiva/z71/device_z71.mk)

# Inherit some common lewamod stuff.
$(call inherit-product, vendor/lewa/products/common_lewa.mk)

# Include GSM stuff
$(call inherit-product, vendor/lewa/products/gsm.mk)

#
# Setup device specific product configuration.
#
PRODUCT_NAME := lewa_z71
PRODUCT_BRAND := commtiva
PRODUCT_DEVICE := z71
PRODUCT_MODEL := Z71
PRODUCT_MANUFACTURER := Commtiva
PRODUCT_BUILD_PROP_OVERRIDES += PRODUCT_NAME=commtiva_z71 BUILD_ID=GRJ22 BUILD_FINGERPRINT=google/passion/passion:2.3.4/GRJ22/121341:user/release-keys PRIVATE_BUILD_DESC="passion-user 2.3.4 GRJ22 121341 release-keys"

#PRODUCT_SPECIFIC_DEFINES += TARGET_PRELINKER_MAP=$(TOP)/vendor/lewa/prelink-linux-arm-z71.map

# Release name and versioning
PRODUCT_RELEASE_NAME := Z71
PRODUCT_VERSION_DEVICE_SPECIFIC :=
-include vendor/lewa/products/common_versions.mk

#
# Copy legend specific prebuilt files
#


