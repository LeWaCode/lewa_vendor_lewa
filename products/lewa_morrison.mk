# Inherit AOSP device configuration for zeppelin.
$(call inherit-product, device/motorola/morrison/morrison.mk)

# Inherit some common lewamod stuff.
$(call inherit-product, vendor/lewa/products/common_lewa.mk)

$(call inherit-product, vendor/lewa/products/bcm_fm_radio.mk)

$(call inherit-product, vendor/lewa/products/gsm.mk)

#
# Setup device specific product configuration.
#
PRODUCT_NAME := lewa_morrison
PRODUCT_BRAND := motorola
PRODUCT_DEVICE := morrison
PRODUCT_MODEL := MB200
PRODUCT_MANUFACTURER := Motorola

# Release name and versioning
PRODUCT_RELEASE_NAME := CLIQ
PRODUCT_VERSION_DEVICE_SPECIFIC :=
-include vendor/lewa/products/common_versions.mk

PRODUCT_BUILD_PROP_OVERRIDES += PRODUCT_NAME=morrison TARGET_DEVICE=morrison BUILD_ID=GRJ22 BUILD_FINGERPRINT=google/soju/crespo:2.3.4/GRJ22/121341:user/release-keys PRIVATE_BUILD_DESC="soju-user 2.3.4 GRJ22 121341 release-keys" BUILD_NUMBER=121341




PRODUCT_PACKAGE_OVERLAYS += vendor/lewa/overlay/morrison
