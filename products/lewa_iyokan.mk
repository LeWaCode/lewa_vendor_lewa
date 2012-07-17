# Inherit device configuration for iyokan.
$(call inherit-product, device/semc/iyokan/device_iyokan.mk)

# Inherit some common lewamod stuff.
$(call inherit-product, vendor/lewa/products/common_lewa.mk)

# Include GSM stuff
$(call inherit-product, vendor/lewa/products/gsm.mk)

#
# Setup device specific product configuration.
#
PRODUCT_NAME := lewa_iyokan
PRODUCT_BRAND := SEMC
PRODUCT_DEVICE := iyokan
PRODUCT_MODEL := MK16i
PRODUCT_MANUFACTURER := Sony Ericsson
PRODUCT_BUILD_PROP_OVERRIDES += PRODUCT_NAME=MK16i BUILD_ID=4.0.1.A.0.283 BUILD_FINGERPRINT=SEMC/MK16i_1247-1061/MK16i:2.3.4/4.0.1.A.0.283/bn_P:user/release-keys PRIVATE_BUILD_DESC="MK16i-user 2.3.4 4.0.1.A.0.283 bn_P test-keys"

# Build kernel
#PRODUCT_SPECIFIC_DEFINES += TARGET_PREBUILT_KERNEL=
#PRODUCT_SPECIFIC_DEFINES += TARGET_KERNEL_DIR=kernel-msm
#PRODUCT_SPECIFIC_DEFINES += TARGET_KERNEL_CONFIG=lewa_iyokan_defconfig

# Extra iyokan overlay
PRODUCT_PACKAGE_OVERLAYS += vendor/lewa/overlay/iyokan

# Add the Torch app
PRODUCT_PACKAGES += Torch


# BCM FM radio
#$(call inherit-product, vendor/lewa/products/bcm_fm_radio.mk)

# Release name and versioning
PRODUCT_RELEASE_NAME := XperiaPro-MK16i
PRODUCT_VERSION_DEVICE_SPECIFIC :=
-include vendor/lewa/products/common_versions.mk

#
# Copy passion specific prebuilt files
#


