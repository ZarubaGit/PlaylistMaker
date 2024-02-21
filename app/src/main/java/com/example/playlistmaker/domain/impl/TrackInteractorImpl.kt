package com.example.playlistmaker.domain.impl

import com.example.playlistmaker.domain.api.TrackInteractor
import com.example.playlistmaker.domain.api.TrackRepository

class TrackInteractorImpl(private val repository: TrackRepository): TrackInteractor {
    override fun search(text: String, consumer: TrackInteractor.TrackConsumer) {
        val t = Thread{
            consumer.consume(repository.search(text))
        }
        t.start()
    }
}