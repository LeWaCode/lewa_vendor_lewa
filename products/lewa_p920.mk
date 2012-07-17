# Include GSM stuff
$(call inherit-product, vendor/lewa/products/gsm.mk)

# Inherit device configuration
$(call inherit-product, device/lge/p920/p920.mk)

# Inherit some common lewamod stuff.
$(call inherit-product, vendor/lewa/products/common_lewa.mk)

#
# Setup device specific product configuration.
#
PRODUCT_NAME := lewa_p920
PRODUCT_BRAND := lge
PRODUCT_DEVICE := p920
PRODUCT_MODEL := LG-P920
PRODUCT_MANUFACTURER := LGE
PRODUCT_BUILD_PROP_OVERRIDES += PRODUCT_NAME=cosmopolitan BUILD_ID=GRJ90 BUILD_FINGERPRINT="lge/cosmopolitan/cosmo_EUR-XXX:2.3.5/GRJ90/LGP920-V21a.19defbe655:user/release-keys" PRIVATE_BUILD_DESC="cosmopolitan-user 2.3.5 GRJ90 LGP920-V21a-NOV-15-2011.423ad6cf release-keys"

# Release name and versioning
PRODUCT_RELEASE_NAME := Optimus3D
PRODUCT_VERSION_DEVICE_SPECIFIC :=
-include vendor/lewa/products/common_versions.mk

#
#
# Copy prebuilt files
#



# TI FM radio
$(call inherit-product, vendor/lewa/products/ti_fm_radio.mk)

# Add the Torch app
PRODUCT_PACKAGES += Torch
