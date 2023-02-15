package de.menkalian.pisces.web

import de.menkalian.pisces.web.data.UserData
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class WebUserController {
    @GetMapping("/user/details")
    fun getUserDetails(
        authenticationToken: OAuth2AuthenticationToken,
    ): UserData {
        val oAuth2User = authenticationToken.principal as DefaultOAuth2User
        return UserData(
            oAuth2User.getAttribute("username") ?: "",
            "http://cdn.discordapp.com/avatars/${oAuth2User.getAttribute<String>("id")}/${oAuth2User.getAttribute<String>("avatar")}.png"
        )
    }
}