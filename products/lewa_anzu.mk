# Inherit device configuration for anzu.
$(call inherit-product, device/semc/anzu/device_anzu.mk)

# Inherit some common lewamod stuff.
$(call inherit-product, vendor/lewa/products/common_lewa.mk)

# Include GSM stuff
$(call inherit-product, vendor/lewa/products/gsm.mk)

#
# Setup device specific product configuration.
#
PRODUCT_NAME := lewa_anzu
PRODUCT_BRAND := SEMC
PRODUCT_DEVICE := anzu
PRODUCT_MODEL := LT15i
PRODUCT_MANUFACTURER := Sony Ericsson
PRODUCT_BUILD_PROP_OVERRIDES += PRODUCT_NAME=LT15i BUILD_ID=4.0.2.A.0.42 BUILD_FINGERPRINT=SEMC/LT15i_1247-1061/LT15i:2.3.4/4.0.2.A.0.42/bn_P:user/release-keys PRIVATE_BUILD_DESC="LT15i-user 2.3.4 4.0.3.A.0.42 bn_P test-keys"

# Build kernel
#PRODUCT_SPECIFIC_DEFINES += TARGET_PREBUILT_KERNEL=
#PRODUCT_SPECIFIC_DEFINES += TARGET_KERNEL_DIR=kernel-msm
#PRODUCT_SPECIFIC_DEFINES += TARGET_KERNEL_CONFIG=lewa_anzu_defconfig

# Extra anzu overlay
PRODUCT_PACKAGE_OVERLAYS += vendor/lewa/overlay/anzu

# Add the Torch app
PRODUCT_PACKAGES += Torch


# BCM FM radio
#$(call inherit-product, vendor/lewa/products/bcm_fm_radio.mk)

# Release name and versioning
PRODUCT_RELEASE_NAME := XperiaArc-LT15i
PRODUCT_VERSION_DEVICE_SPECIFIC :=
-include vendor/lewa/products/common_versions.mk

#
# Copy passion specific prebuilt files
#


