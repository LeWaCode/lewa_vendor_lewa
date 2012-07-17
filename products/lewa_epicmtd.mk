# Inherit AOSP device configuration for epic.
$(call inherit-product, device/samsung/epicmtd/full_epicmtd.mk)

# Inherit some common lewamod stuff.
$(call inherit-product, vendor/lewa/products/common_lewa.mk)

#
# Setup device specific product configuration.
#
PRODUCT_NAME := lewa_epicmtd
PRODUCT_BRAND := samsung
PRODUCT_DEVICE := epicmtd
PRODUCT_MODEL := SPH-D700
PRODUCT_MANUFACTURER := samsung
PRODUCT_BUILD_PROP_OVERRIDES += PRODUCT_NAME=SPH-D700 TARGET_DEVICE=SPH-D700 BUILD_ID=GINGERBREAD BUILD_FINGERPRINT=sprint/SPH-D700/SPH-D700:2.3.5/GINGERBREAD/EI22:user/release-keys PRIVATE_BUILD_DESC="SPH-D700-user 2.3.5 GINGERBREAD EI22 release-keys"

# Extra epic overlay
PRODUCT_PACKAGE_OVERLAYS += vendor/lewa/overlay/epicmtd

# Add the Torch app
PRODUCT_PACKAGES += Torch

# Release name and versioning
PRODUCT_RELEASE_NAME := Epic
PRODUCT_VERSION_DEVICE_SPECIFIC :=
-include vendor/lewa/products/common_versions.mk

#
# Copy galaxys specific prebuilt files
#


