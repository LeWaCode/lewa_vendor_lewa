# Broadcom FM Radio
PRODUCT_PACKAGES += \
    hcitool

# remove by ioz9 for LeWaFm
#    FM \

PRODUCT_PACKAGE_OVERLAYS += vendor/lewa/overlay/bcm_fm_radio
