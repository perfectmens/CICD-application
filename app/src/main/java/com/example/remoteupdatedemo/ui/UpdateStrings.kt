package com.example.remoteupdatedemo.ui

/**
 * Single source of truth for user-facing update status strings.
 * Shared between MainViewModel (production) and MainViewModelTest (tests).
 * Change a string here — both sides stay in sync automatically.
 */
object UpdateStrings {
    const val UP_TO_DATE_MESSAGE =
        "Remote update functionality will be added later."

    const val NETWORK_ERROR_PREFIX =
        "Remote update functionality will be added later."

    const val NETWORK_ERROR_SUFFIX = " (Backend server offline)"

    /** Full error message sent to UpdateStatus.Error */
    val NETWORK_ERROR_MESSAGE get() = "$NETWORK_ERROR_PREFIX$NETWORK_ERROR_SUFFIX"
}
