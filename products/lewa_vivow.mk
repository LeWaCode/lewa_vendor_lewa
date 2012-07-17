# Inherit device configuration for vivow.
$(call inherit-product, device/htc/vivow/vivow.mk)

# Inherit some common lewamod stuff.
$(call inherit-product, vendor/lewa/products/common_lewa.mk)

#
# Setup device specific product configuration.
#
PRODUCT_NAME := lewa_vivow
PRODUCT_BRAND := verizon_wwe
PRODUCT_DEVICE := vivow
PRODUCT_MODEL := Incredible 2
PRODUCT_MANUFACTURER := HTC
PRODUCT_BUILD_PROP_OVERRIDES += PRODUCT_NAME=htc_vivow BUILD_ID=GRI40 BUILD_FINGERPRINT=verizon_wwe/htc_vivow/vivow:2.3.3/GRI40/81006:user/release-keys PRIVATE_BUILD_DESC="2.18.605.4 CL81006 release-keys"

# Build kernel
#PRODUCT_SPECIFIC_DEFINES += TARGET_PREBUILT_KERNEL=
#PRODUCT_SPECIFIC_DEFINES += TARGET_KERNEL_DIR=kernel-msm
#PRODUCT_SPECIFIC_DEFINES += TARGET_KERNEL_CONFIG=lewa_vivow_defconfig

# Extra vivow overlay
PRODUCT_PACKAGE_OVERLAYS += vendor/lewa/overlay/vivow

# Add the Torch app
PRODUCT_PACKAGES += Torch

# Broadcom FM radio
$(call inherit-product, vendor/lewa/products/bcm_fm_radio.mk)

# Release name and versioning
PRODUCT_RELEASE_NAME := Inc2
PRODUCT_VERSION_DEVICE_SPECIFIC :=
-include vendor/lewa/products/common_versions.mk

#
# Copy passion specific prebuilt files
#


