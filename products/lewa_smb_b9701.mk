$(call inherit-product, device/malata/smb_b9701/smb_b9701.mk)

# Inherit some common lewamod stuff.
$(call inherit-product, vendor/lewa/products/common_lewa.mk)

#
# Setup device specific product configuration.
#
PRODUCT_NAME := lewa_smb_b9701
PRODUCT_BRAND := nvidia
PRODUCT_DEVICE := smb_b9701
PRODUCT_MODEL := Zpad-T8
PRODUCT_MANUFACTURER := malata
PRODUCT_BUILD_PROP_OVERRIDES += PRODUCT_NAME=smb_b9701 BUILD_ID=GRJ22 BUILD_FINGERPRINT=google/passion/passion:2.3.4/GRJ22/121341:user/release-keys PRIVATE_BUILD_DESC="passion-user 2.3.4 GRJ22 121341 release-keys"

# Extra overlay
PRODUCT_PACKAGE_OVERLAYS += \
    vendor/lewa/overlay/tablet \
    vendor/lewa/overlay/smb_b9701

# Release name and versioning
PRODUCT_RELEASE_NAME := smb_b9701
PRODUCT_VERSION_DEVICE_SPECIFIC :=
-include vendor/lewa/products/common_versions.mk

#
# Copy passion specific prebuilt files
#


