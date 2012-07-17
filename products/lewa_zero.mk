# Inherit AOSP device configuration for zero.
$(call inherit-product, device/geeksphone/zero/zero.mk)

# Inherit some common lewamod stuff.
$(call inherit-product, vendor/lewa/products/common_lewa.mk)

# Include GSM stuff
$(call inherit-product, vendor/lewa/products/gsm.mk)

#
# Setup device specific product configuration.
#
PRODUCT_NAME := lewa_zero
PRODUCT_BRAND := geeksphone
PRODUCT_DEVICE := zero
PRODUCT_MODEL := Geeksphone ZERO
PRODUCT_MANUFACTURER := Geeksphone

PRODUCT_BUILD_PROP_OVERRIDES += PRODUCT_NAME=geeksphone_zero BUILD_ID=FRG83 BUILD_FINGERPRINT=qcom/msm7627_ffa/msm7627_ffa/7x27:2.2.1/FRG83/eng.SIMCOM.20110314.124514:user/test-keys PRIVATE_BUILD_DESC="msm7627_ffa-user 2.2.1 FRG83 eng.SIMCOM.20110314.124514 test-keys"


# Broadcom FM radio
$(call inherit-product, vendor/lewa/products/bcm_fm_radio.mk)

# Release name and versioning
PRODUCT_RELEASE_NAME := ZERO
PRODUCT_VERSION_DEVICE_SPECIFIC :=
-include vendor/lewa/products/common_versions.mk

#
# Copy specific prebuilt files
#


