# Inherit device configuration for satsuma.
$(call inherit-product, device/semc/satsuma/device_satsuma.mk)

# Inherit some common lewamod stuff.
$(call inherit-product, vendor/lewa/products/common_lewa.mk)

# Include GSM stuff
$(call inherit-product, vendor/lewa/products/gsm.mk)

#
# Setup device specific product configuration.
#
PRODUCT_NAME := lewa_satsuma
PRODUCT_BRAND := SEMC
PRODUCT_DEVICE := satsuma
PRODUCT_MODEL := ST17i
PRODUCT_MANUFACTURER := Sony Ericsson
PRODUCT_BUILD_PROP_OVERRIDES += PRODUCT_NAME=ST17i BUILD_ID=4.0.2.A.0.42 BUILD_FINGERPRINT=SEMC/ST17i_1249-6227/ST17i:2.3.4/4.0.2.A.0.42/j_b_3w:user/release-keys PRIVATE_BUILD_DESC="ST17i-user 2.3.4 4.0.2.A.0.42 j_b_3w test-keys"

# Build kernel
#PRODUCT_SPECIFIC_DEFINES += TARGET_PREBUILT_KERNEL=
#PRODUCT_SPECIFIC_DEFINES += TARGET_KERNEL_DIR=kernel-msm
#PRODUCT_SPECIFIC_DEFINES += TARGET_KERNEL_CONFIG=lewa_satsuma_defconfig

# Extra satsuma overlay
PRODUCT_PACKAGE_OVERLAYS += vendor/lewa/overlay/satsuma

# Add the Torch app
PRODUCT_PACKAGES += Torch


# BCM FM radio
#$(call inherit-product, vendor/lewa/products/bcm_fm_radio.mk)

# Release name and versioning
PRODUCT_RELEASE_NAME := XperiaActive-ST17i
PRODUCT_VERSION_DEVICE_SPECIFIC :=
-include vendor/lewa/products/common_versions.mk

#
# Copy passion specific prebuilt files
#


