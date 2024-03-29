package com.example.playlistmaker.domain.api

import com.example.playlistmaker.domain.models.Track

interface TrackInteractor {
    fun search(text: String, consumer: TrackConsumer)

    interface TrackConsumer{
        fun consume(foundTracks: List<Track>?, errorMessage: Int?)
    }
}