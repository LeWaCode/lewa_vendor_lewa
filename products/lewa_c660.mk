# Include GSM stuff
$(call inherit-product, vendor/lewa/products/gsm.mk)

# Inherit device configuration for c660.
$(call inherit-product, device/lge/c660/c660.mk)

# Inherit some common lewamod stuff.
$(call inherit-product, vendor/lewa/products/common_lewa.mk)

# Broadcom FM radio
$(call inherit-product, vendor/lewa/products/bcm_fm_radio.mk)

#
# Setup device specific product configuration.
#
PRODUCT_NAME := lewa_c660
PRODUCT_BRAND := lge
PRODUCT_DEVICE := c660
PRODUCT_MODEL := LG-C660
PRODUCT_MANUFACTURER := LGE
PRODUCT_BUILD_PROP_OVERRIDES += PRODUCT_NAME=lge_muscat BUILD_ID=GRJ22 BUILD_FINGERPRINT=LGE/lge_muscat/muscat:2.3.4/GRJ22/V10a-Aug-03-2011.2ED2F90A70:user/release-keys PRIVATE_BUILD_DESC="muscat-user 2.3.4 GRJ22 V10a-Aug-03-2011.2ED2F90A70 release-keys"

PRODUCT_PACKAGE_OVERLAYS += vendor/lewa/overlay/ldpi

# Release name and versioning
PRODUCT_RELEASE_NAME := OptimusPro
PRODUCT_VERSION_DEVICE_SPECIFIC :=
-include vendor/lewa/products/common_versions.mk

#
# Copy specific prebuilt files
#


