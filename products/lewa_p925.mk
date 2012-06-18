# Include GSM stuff
$(call inherit-product, vendor/cyanogen/products/gsm.mk)

# Inherit device configuration
$(call inherit-product, device/lge/p925/p925.mk)

# Inherit some common cyanogenmod stuff.
$(call inherit-product, vendor/cyanogen/products/common_full.mk)

#
# Setup device specific product configuration.
#
PRODUCT_NAME := cyanogen_p925
PRODUCT_BRAND := lge
PRODUCT_DEVICE := p925
PRODUCT_MODEL := LG-P925
PRODUCT_MANUFACTURER := LGE
PRODUCT_BUILD_PROP_OVERRIDES += PRODUCT_NAME=cosmopolitan BUILD_ID=GRJ90 BUILD_FINGERPRINT="lge/cosmopolitan/cosmo_EUR-XXX:2.3.5/GRJ90/LGP920-V21a.19defbe655:user/release-keys" PRIVATE_BUILD_DESC="cosmopolitan-user 2.3.5 GRJ90 LGP920-V21a-NOV-15-2011.423ad6cf release-keys"

# Release name and versioning
PRODUCT_RELEASE_NAME := Thrill
PRODUCT_VERSION_DEVICE_SPECIFIC :=
-include vendor/cyanogen/products/common_versions.mk

#
#
# Copy prebuilt files
#
PRODUCT_COPY_FILES +=  \
    vendor/cyanogen/prebuilt/hdpi/media/bootanimation.zip:system/media/bootanimation.zip

## TI FM radio
#$(call inherit-product, vendor/cyanogen/products/ti_fm_radio.mk)

# Add the Torch app
PRODUCT_PACKAGES += Torch
