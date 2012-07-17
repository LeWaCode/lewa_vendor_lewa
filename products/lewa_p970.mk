# Include GSM stuff
$(call inherit-product, vendor/lewa/products/gsm.mk)

# Inherit device configuration
$(call inherit-product, device/lge/p970/p970.mk)

# Inherit some common lewamod stuff.
$(call inherit-product, vendor/lewa/products/common_lewa.mk)

#
# Setup device specific product configuration.
#
PRODUCT_NAME := lewa_p970
PRODUCT_BRAND := lge
PRODUCT_DEVICE := p970
PRODUCT_MODEL := LG-P970
PRODUCT_MANUFACTURER := LGE
PRODUCT_BUILD_PROP_OVERRIDES += PRODUCT_NAME=lge_bproj BUILD_ID=GRJ22 BUILD_FINGERPRINT="lge/lge_bproj/bproj_EUR-XXX:2.3.4/GRJ22/LG-P970-V20d.421CC761:user/release-keys" PRIVATE_BUILD_DESC="lge_bproj-user 2.3.4 GRJ22 LG-P970-V20d.421CC761 release-keys"

# Broadcom FM radio
$(call inherit-product, vendor/lewa/products/bcm_fm_radio.mk)

# Release name and versioning
PRODUCT_RELEASE_NAME := OptimusBlack
PRODUCT_VERSION_DEVICE_SPECIFIC :=
-include vendor/lewa/products/common_versions.mk

#
# Copy prebuilt files
#



# Add the Torch app
PRODUCT_PACKAGES += Torch
