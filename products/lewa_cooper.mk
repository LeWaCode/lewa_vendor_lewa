# Inherit AOSP device configuration for blade.
$(call inherit-product, device/samsung/cooper/device_cooper.mk)

# Inherit some common lewamod stuff.
$(call inherit-product, vendor/lewa/products/common_lewa.mk)

# Include GSM stuff
$(call inherit-product, vendor/lewa/products/gsm.mk)

#
# Setup device specific product configuration.
#
PRODUCT_NAME := lewa_cooper
PRODUCT_BRAND := samsung
PRODUCT_DEVICE := cooper
PRODUCT_MODEL := GT-S5830
PRODUCT_MANUFACTURER := Samsung
PRODUCT_BUILD_PROP_OVERRIDES += PRODUCT_NAME=cooper BUILD_ID=GRWK74 BUILD_FINGERPRINT=samsung/GT-S5830/GT-S5830:2.3.4/GINGERBREAD/XXKPH:user/test-keys PRIVATE_BUILD_DESC="GT-S5830-user 2.3.4 GINGERBREAD XXKPH test-keys"

# Release name and versioning
PRODUCT_RELEASE_NAME := GalaxyAce
PRODUCT_VERSION_DEVICE_SPECIFIC :=
-include vendor/lewa/products/common_versions.mk

#
# Copy legend specific prebuilt files
#


