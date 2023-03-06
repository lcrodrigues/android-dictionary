package com.example.dictionary.feature_dictionary.data.remote.dto

import com.example.dictionary.feature_dictionary.data.local.entity.WordInfoEntity

data class WordInfoDto(
    val meanings: List<MeaningDto>,
    val origin: String?,
    val phonetic: String,
    val word: String
) {
    fun toWordInfoEntity() = WordInfoEntity(
        word,
        phonetic,
        origin,
        meanings.map { it.toMeaning() }
    )
}