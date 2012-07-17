# Inherit AOSP device configuration for legend.
$(call inherit-product, device/htc/legend/legend.mk)

# Inherit some common lewamod stuff.
$(call inherit-product, vendor/lewa/products/common_lewa.mk)

# Include GSM stuff
$(call inherit-product, vendor/lewa/products/gsm.mk)


PRODUCT_NAME := lewa_legend
PRODUCT_BRAND := htc
PRODUCT_DEVICE := legend
PRODUCT_MODEL := Legend
PRODUCT_MANUFACTURER := HTC
PRODUCT_BUILD_PROP_OVERRIDES += PRODUCT_NAME=htc_legend BUILD_ID=GRH55 BUILD_FINGERPRINT=google/soju/crespo:2.3/GRH55/79397:user/release-keys PRIVATE_BUILD_DESC="soju-user 2.3 GRH55 79397 release-keys"

PRODUCT_SPECIFIC_DEFINES += TARGET_PRELINKER_MAP=$(TOP)/vendor/lewa/prelink-linux-arm-msm722x.map

PRODUCT_PACKAGES += Torch

# TI FM radio
$(call inherit-product, vendor/lewa/products/ti_fm_radio.mk)

PRODUCT_PACKAGE_OVERLAYS += vendor/lewa/overlay/legend

# Release name and versioning
PRODUCT_RELEASE_NAME := Legend
PRODUCT_VERSION_DEVICE_SPECIFIC :=
-include vendor/lewa/products/common_versions.mk



