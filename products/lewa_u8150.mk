# Inherit AOSP device configuration for U8150.
$(call inherit-product, device/huawei/u8150/device_u8150.mk)

# Inherit some common lewamod stuff.
$(call inherit-product, vendor/lewa/products/common_lewa.mk)

# Include GSM stuff
$(call inherit-product, vendor/lewa/products/gsm.mk)

#
# Setup device specific product configuration.
#
PRODUCT_NAME := lewa_u8150
PRODUCT_BRAND := huawei
PRODUCT_DEVICE := u8150
PRODUCT_MODEL := U8150
PRODUCT_MANUFACTURER := Huawei
PRODUCT_BUILD_PROP_OVERRIDES += PRODUCT_NAME=u8150 BUILD_ID=GRK39F BUILD_DISPLAY_ID=GWK74 BUILD_FINGERPRINT=google/passion/passion:2.3.6/GRK39F/189904:user/release-keys PRIVATE_BUILD_DESC="passion-user 2.3.6 GRK39F 189904 release-keys"

# Extra LDPI overlay
PRODUCT_PACKAGE_OVERLAYS += vendor/lewa/overlay/ldpi

# Release name and versioning
PRODUCT_RELEASE_NAME := U8150
PRODUCT_VERSION_DEVICE_SPECIFIC :=
-include vendor/lewa/products/common_versions.mk


 
