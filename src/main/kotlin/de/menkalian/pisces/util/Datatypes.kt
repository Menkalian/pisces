package de.menkalian.pisces.util

import de.menkalian.pisces.audio.data.EPlayTrackResult
import de.menkalian.pisces.audio.data.TrackInfo

/**
 * Kombination aus dem Lade/Suchergebnis eines Tracks und den Informationen zu dem/den geladenen Track(s).
 */
typealias QueueResult = Pair<EPlayTrackResult, List<TrackInfo>>
