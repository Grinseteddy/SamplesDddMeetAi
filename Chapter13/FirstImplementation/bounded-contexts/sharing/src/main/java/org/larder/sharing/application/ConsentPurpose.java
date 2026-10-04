package org.larder.sharing.application;

/** What Sharing needs a cook's consent for; Consent Management knows each purpose as one consent text. */
public enum ConsentPurpose {

    /** "I allow other cooks to mention me as helper in their thanks." */
    MENTION_AS_HELPER,

    /** "I allow to use my photos in public thanks and recipes." */
    PHOTOS_IN_PUBLIC_THANKS
}
