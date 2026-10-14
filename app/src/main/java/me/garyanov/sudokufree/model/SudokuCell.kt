package me.garyanov.sudokufree.model

// Состояние одной ячейки
data class SudokuCell(
    val value: Int,         // Текущее значение (0 - пусто)
    val isEditable: Boolean, // Можно ли редактировать
    val solutionValue: Int,  // Правильное значение (для подсказки)
    val notes: List<Int> = emptyList() // Черновые цифры в порядке ввода
)