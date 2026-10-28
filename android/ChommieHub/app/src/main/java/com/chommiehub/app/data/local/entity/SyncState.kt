package com.chommiehub.app.data.local.entity

enum class SyncState {
    /** Saved on the phone, waiting to be sent. */
    PENDING,

    /** The server already has this change. */
    SYNCED,

    /** Sending failed after several tries. */
    FAILED
}