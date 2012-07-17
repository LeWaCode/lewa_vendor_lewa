# Include GSM stuff
$(call inherit-product, vendor/lewa/products/gsm.mk)

# Inherit device configuration for e510.
$(call inherit-product, device/lge/e510/e510.mk)

# Inherit some common lewamod stuff.
$(call inherit-product, vendor/lewa/products/common_lewa.mk)


#
# Setup device specific product configuration.
#
PRODUCT_NAME := lewa_e510
PRODUCT_BRAND := lge
PRODUCT_DEVICE := e510
PRODUCT_MODEL := LG-E510
PRODUCT_MANUFACTURER := LGE
PRODUCT_BUILD_PROP_OVERRIDES += PRODUCT_NAME=lge_univa BUILD_ID=GRJ22 BUILD_FINGERPRINT=lge/lge_univa/univa_open-eu:2.3.4/GRJ22/lge510-V10c.19d3f3d48a:user/release-keys PRIVATE_BUILD_DESC="lge_univa-user 2.3.4 GINGERBREAD V10c.19d3f3d48a release-keys"

# Release name and versioning
PRODUCT_RELEASE_NAME := OptimusHub
PRODUCT_VERSION_DEVICE_SPECIFIC :=
-include vendor/lewa/products/common_versions.mk

#
# Copy specific prebuilt files
#


