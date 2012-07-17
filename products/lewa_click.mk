# Inherit AOSP device configuration for click.
$(call inherit-product, device/htc/click/click.mk)

# Inherit some common lewamod stuff.
$(call inherit-product, vendor/lewa/products/common_lewa_no_themes.mk)

# Include GSM stuff
$(call inherit-product, vendor/lewa/products/gsm.mk)

# Broadcom FM radio
$(call inherit-product, vendor/lewa/products/ti_fm_radio.mk)

# Build kernel
PRODUCT_SPECIFIC_DEFINES += TARGET_PREBUILT_KERNEL=device/htc/click/kernel

#
# Setup device specific product configuration.
#
PRODUCT_NAME := lewa_click
PRODUCT_BRAND := htc_wwe
PRODUCT_DEVICE := click
PRODUCT_MODEL := HTC Tattoo
PRODUCT_MANUFACTURER := HTC
PRODUCT_BUILD_PROP_OVERRIDES += PRODUCT_NAME=htc_click BUILD_ID=GRI40 BUILD_FINGERPRINT=google/passion/passion:2.3.3/GRI40/102588:user/release-keys PRIVATE_BUILD_DESC="passion-user 2.3.3 GRI40 102588 release-keys"

# Add LDPI assets, in addition to MDPI
PRODUCT_LOCALES += ldpi mdpi

# Extra Passion overlay
PRODUCT_PACKAGE_OVERLAYS += vendor/lewa/overlay/ldpi

# Boot animation



# Release name and versioning
PRODUCT_RELEASE_NAME := click
PRODUCT_VERSION_DEVICE_SPECIFIC :=
-include vendor/lewa/products/common_versions.mk
