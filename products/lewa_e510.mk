# Include GSM stuff
$(call inherit-product, vendor/cyanogen/products/gsm.mk)

# Inherit device configuration for e510.
$(call inherit-product, device/lge/e510/e510.mk)

# Inherit some common cyanogenmod stuff.
$(call inherit-product, vendor/cyanogen/products/common_full.mk)


#
# Setup device specific product configuration.
#
PRODUCT_NAME := cyanogen_e510
PRODUCT_BRAND := lge
PRODUCT_DEVICE := e510
PRODUCT_MODEL := LG-E510
PRODUCT_MANUFACTURER := LGE
PRODUCT_BUILD_PROP_OVERRIDES += PRODUCT_NAME=lge_univa BUILD_ID=GRJ22 BUILD_FINGERPRINT=lge/lge_univa/univa_open-eu:2.3.4/GRJ22/lge510-V10c.19d3f3d48a:user/release-keys PRIVATE_BUILD_DESC="lge_univa-user 2.3.4 GINGERBREAD V10c.19d3f3d48a release-keys"

# Release name and versioning
PRODUCT_RELEASE_NAME := OptimusHub
PRODUCT_VERSION_DEVICE_SPECIFIC :=
-include vendor/cyanogen/products/common_versions.mk

#
# Copy specific prebuilt files
#
PRODUCT_COPY_FILES +=  \
    vendor/cyanogen/prebuilt/mdpi/media/bootanimation.zip:system/media/bootanimation.zip
