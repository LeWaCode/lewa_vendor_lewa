# Inherit AOSP device configuration for galaxysb.
$(call inherit-product, device/samsung/galaxysbmtd/full_galaxysbmtd.mk)

# Inherit some common cyanogenmod stuff.
$(call inherit-product, vendor/cyanogen/products/common_full.mk)

# Include GSM stuff
$(call inherit-product, vendor/cyanogen/products/gsm.mk)

#
# Setup device specific product configuration.
#
PRODUCT_NAME := cyanogen_galaxysbmtd
PRODUCT_BRAND := samsung
PRODUCT_DEVICE := galaxysbmtd
PRODUCT_MODEL := GT-I9000B
PRODUCT_MANUFACTURER := samsung
PRODUCT_BUILD_PROP_OVERRIDES += PRODUCT_NAME=GT-I9000B TARGET_DEVICE=GT-I9000B BUILD_ID=GINGERBREAD BUILD_FINGERPRINT=samsung/GT-I9000B/GT-I9000B:2.3.3/GINGERBREAD/VVJV3:user/release-keys PRIVATE_BUILD_DESC="GT-I9000B-user 2.3.3 GINGERBREAD VVJVS release-keys"

# Build kernel
#PRODUCT_SPECIFIC_DEFINES += TARGET_PREBUILT_KERNEL=
#PRODUCT_SPECIFIC_DEFINES += TARGET_KERNEL_DIR=kernel-samsung
#PRODUCT_SPECIFIC_DEFINES += TARGET_KERNEL_CONFIG=cyanogen_galaxysb_defconfig

# Extra galaxys overlay
PRODUCT_PACKAGE_OVERLAYS += vendor/cyanogen/overlay/galaxysbmtd

# Add the FM app
PRODUCT_PACKAGES += FM

# Extra RIL settings
PRODUCT_PROPERTY_OVERRIDES += \
    ro.ril.enable.managed.roaming=1 \
    ro.ril.oem.nosim.ecclist=911,112,999,000,08,118,120,122,110,119,995 \
    ro.ril.emc.mode=2

# Add additional mounts
PRODUCT_PROPERTY_OVERRIDES += \
    ro.additionalmounts=/mnt/emmc \
    ro.vold.switchablepair=/mnt/sdcard,/mnt/emmc

# Release name and versioning
PRODUCT_RELEASE_NAME := GalaxyS_B
PRODUCT_VERSION_DEVICE_SPECIFIC :=
-include vendor/cyanogen/products/common_versions.mk

#
# Copy galaxys specific prebuilt files
#
PRODUCT_COPY_FILES +=  \
    vendor/cyanogen/prebuilt/hdpi/media/bootanimation.zip:system/media/bootanimation.zip
