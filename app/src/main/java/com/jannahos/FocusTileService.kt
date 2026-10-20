package com.jannahos

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

class FocusTileService : TileService() {
    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    override fun onClick() {
        super.onClick()
        val ctx = applicationContext
        // Toggle Focus Mode (App Blocker)
        val isEnabled = FocusSession.enabled(ctx)
        FocusSession.setEnabled(ctx, !isEnabled)
        updateTile()
    }

    private fun updateTile() {
        val tile = qsTile ?: return
        val isEnabled = FocusSession.enabled(applicationContext)
        tile.state = if (isEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = "Jannah Focus"
        tile.updateTile()
    }
}
