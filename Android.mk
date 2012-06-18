# We need a way to prevent the stuff Google Apps replaces from being included in the build.
# This is a hacky way to do that.
ifdef LEWA_WITH_GOOGLE
    PACKAGES.Email.OVERRIDES := Provision QuickSearchBox
endif

# added by ioz9 for lewaMod ,2012-04-21
include vendor/lewa/prebuilt/LewaMod.mk

