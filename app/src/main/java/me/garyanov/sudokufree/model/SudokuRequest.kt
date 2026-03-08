package me.garyanov.sudokufree.model

data class SudokuRequest(
    val difficulty: String,
    val solution: Boolean,
    val array: Boolean
)