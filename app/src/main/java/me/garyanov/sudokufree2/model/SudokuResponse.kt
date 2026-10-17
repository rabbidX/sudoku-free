package me.garyanov.sudokufree2.model

data class SudokuResponse(
    val difficulty: String? = null,
    val puzzle: String? = null,
    val solution: String? = null,
    val error: String? = null
)

