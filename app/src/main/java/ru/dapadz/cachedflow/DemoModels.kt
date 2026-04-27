package ru.dapadz.cachedflow

import kotlinx.serialization.Serializable

@Serializable
data class DemoProfile(
    val id: Int,
    val name: String,
    val badge: String,
    val active: Boolean
)

@Serializable
data class DemoArticle(
    val id: Int,
    val title: String,
    val weight: Float
)

interface DemoAnimal {
    val name: String
}

@Serializable
data class DemoDog(
    override val name: String,
    val isGoodBoy: Boolean
) : DemoAnimal

@Serializable
data class DemoCat(
    override val name: String,
    val livesLeft: Int
) : DemoAnimal
