package me.garyanov.sudokufree2.model

data class SudokuRequest(
    val difficulty: String,
    val solution: Boolean,
    val array: Boolean
)