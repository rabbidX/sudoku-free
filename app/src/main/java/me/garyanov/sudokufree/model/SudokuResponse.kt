package me.garyanov.sudokufree.model

data class SudokuResponse(
    val difficulty: String? = null,
    val puzzle: String? = null,
    val solution: String? = null,
    val error: String? = null
)

