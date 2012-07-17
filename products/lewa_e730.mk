# Include GSM stuff
$(call inherit-product, vendor/lewa/products/gsm.mk)

# Inherit device configuration for lge e730.
$(call inherit-product, device/lge/e730/e730.mk)

# Inherit some common lewamod stuff.
$(call inherit-product, vendor/lewa/products/common_lewa.mk)

#
# Setup device specific product configuration.
#
PRODUCT_NAME := lewa_e730
PRODUCT_BRAND := lge
PRODUCT_DEVICE := e730
PRODUCT_MODEL := LG-E730
PRODUCT_MANUFACTURER := LGE
PRODUCT_BUILD_PROP_OVERRIDES += PRODUCT_NAME=lge_victor BUILD_ID=GRJ22 BUILD_FINGERPRINT=lge/lge_victor/victor:2.3.4/GRJ22/V10a-OCT-05-2011.2ED317DCA9:user/release-keys PRIVATE_BUILD_DESC="lge_victor-user 2.3.4 GRJ22 V10a-OCT-05-2011.2ED317DCA9 release-keys"

# Extra e730 overlay
PRODUCT_PACKAGE_OVERLAYS += vendor/lewa/overlay/e730

# Release name and versioning
PRODUCT_RELEASE_NAME := OptimusSol
PRODUCT_VERSION_DEVICE_SPECIFIC :=
-include vendor/lewa/products/common_versions.mk

#
# Copy passion specific prebuilt files
#


