package com.example.playlistmaker.domain.sharing.impl


import com.example.playlistmaker.domain.sharing.ExternalNavigator
import com.example.playlistmaker.domain.sharing.SharingInteractor
import com.example.playlistmaker.domain.sharing.model.EmailData

class SharingInteractorImpl(private val externalNavigator: ExternalNavigator) : SharingInteractor {//внедрение зависимостей с помощью DI и Koin
    override fun shareApp(link: String, title: String) {
        externalNavigator.shareLink(url = link, title = title)
    }

    override fun openTerms(link: String) {
        externalNavigator.openLink(url = link)
    }

    override fun openSupport(email: String, subject: String, text: String) {
        externalNavigator.openEmail(data = getSupportEmailData(
            email = email,
            subject = subject,
            text = text
        ))
    }

    private fun getSupportEmailData(email: String, subject: String, text: String): EmailData {
        return EmailData(
            email = email,
            subject = subject,
            text = text
        )
    }
}

