$(call inherit-product, device/malata/smb_a1011/smb_a1011.mk)

# Inherit some common lewamod stuff.
$(call inherit-product, vendor/lewa/products/common_lewa.mk)

#
# Setup device specific product configuration.
#
PRODUCT_NAME := lewa_smb_a1011
PRODUCT_BRAND := malata
PRODUCT_DEVICE := smb_a1011
PRODUCT_MODEL := SMB-A1011
PRODUCT_MANUFACTURER := malata
PRODUCT_BUILD_PROP_OVERRIDES += PRODUCT_NAME=smb_a1011 BUILD_ID=GRJ22 BUILD_FINGERPRINT=google/passion/passion:2.3.4/GRJ22/121341:user/release-keys PRIVATE_BUILD_DESC="passion-user 2.3.4 GRJ22 121341 release-keys"

# Extra overlay
PRODUCT_PACKAGE_OVERLAYS += \
    vendor/lewa/overlay/tablet \
    vendor/lewa/overlay/smb_a1002

# Release name and versioning
PRODUCT_RELEASE_NAME := SMB-A1011
PRODUCT_VERSION_DEVICE_SPECIFIC :=
-include vendor/lewa/products/common_versions.mk

#
# Copy passion specific prebuilt files
#


