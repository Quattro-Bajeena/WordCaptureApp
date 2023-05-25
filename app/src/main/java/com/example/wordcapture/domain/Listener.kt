package com.example.wordcapture.domain

interface Listener {
    fun itemClicked(id: Long)
    fun animationStopped()
}