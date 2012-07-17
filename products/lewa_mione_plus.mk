# Include GSM stuff
$(call inherit-product, vendor/lewa/products/gsm.mk)

# Inherit device configuration
$(call inherit-product, device/xiaomi/mione_plus/device_mione_plus.mk)

# Inherit some common lewamod stuff.
$(call inherit-product, vendor/lewa/products/common_lewa.mk)

#
# Setup device specific product configuration.
#
PRODUCT_NAME := lewa_mione_plus
PRODUCT_BRAND := xiaomi
PRODUCT_DEVICE := mione_plus
PRODUCT_MODEL := MI-ONE Plus
PRODUCT_MANUFACTURER := Xiaomi
PRODUCT_BUILD_PROP_OVERRIDES += PRODUCT_NAME=libra_mione_plus BUILD_ID=GWK74 BUILD_FINGERPRINT=Xiaomi/libra_mione_plus/mione_plus/GINGERBREAD/2.3.7d:userdebug/test-keys PRIVATE_BUILD_DESC="libra_mione_plus-userdebug 2.3.7 GINGERBREAD 2.3.7d test-keys" BUILD_NUMBER=2.3.7d

# Broadcom FM radio
$(call inherit-product, vendor/lewa/products/bcm_fm_radio.mk)

# Release name and versioning
PRODUCT_RELEASE_NAME := MionePlus
PRODUCT_VERSION_DEVICE_SPECIFIC :=
-include vendor/lewa/products/common_versions.mk

#
# Copy prebuilt files
#



# Add the Torch app
PRODUCT_PACKAGES += Torch
