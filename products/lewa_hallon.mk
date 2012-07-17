# Inherit device configuration for hallon.
$(call inherit-product, device/semc/hallon/device_hallon.mk)

# Inherit some common lewamod stuff.
$(call inherit-product, vendor/lewa/products/common_lewa.mk)

# Include GSM stuff
$(call inherit-product, vendor/lewa/products/gsm.mk)

#
# Setup device specific product configuration.
#
PRODUCT_NAME := lewa_hallon
PRODUCT_BRAND := SEMC
PRODUCT_DEVICE := hallon
PRODUCT_MODEL := MT15i
PRODUCT_MANUFACTURER := Sony Ericsson
PRODUCT_BUILD_PROP_OVERRIDES += PRODUCT_NAME=MT15i BUILD_ID=4.0.2.A.0.42 BUILD_FINGERPRINT=SEMC/MT15i_1247-0875/MT15i:2.3.4/4.0.2.A.0.42/bn_P:user/release-keys PRIVATE_BUILD_DESC="MT15i-user 2.3.4 4.0.2.A.0.42 bn_P test-keys"

# Build kernel
#PRODUCT_SPECIFIC_DEFINES += TARGET_PREBUILT_KERNEL=
#PRODUCT_SPECIFIC_DEFINES += TARGET_KERNEL_DIR=kernel-msm
#PRODUCT_SPECIFIC_DEFINES += TARGET_KERNEL_CONFIG=lewa_hallon_defconfig

# Extra hallon overlay
PRODUCT_PACKAGE_OVERLAYS += vendor/lewa/overlay/hallon

# Add the Torch app
PRODUCT_PACKAGES += Torch


# BCM FM radio
#$(call inherit-product, vendor/lewa/products/bcm_fm_radio.mk)

# Release name and versioning
PRODUCT_RELEASE_NAME := XperiaNeo-MT15i
PRODUCT_VERSION_DEVICE_SPECIFIC :=
-include vendor/lewa/products/common_versions.mk

#
# Copy passion specific prebuilt files
#


