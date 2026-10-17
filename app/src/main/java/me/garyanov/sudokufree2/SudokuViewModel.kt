package me.garyanov.sudokufree2

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import me.garyanov.sudokufree2.model.SudokuCell
import me.garyanov.sudokufree2.model.SudokuRequest
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SudokuViewModel : ViewModel() {

    // Состояние игры
    var board = mutableStateListOf<List<SudokuCell>>()
        private set

    var isGameWon = mutableStateOf(false)
        private set

    var selectedDifficulty = mutableStateOf("easy")
        private set

    var selectedCell = mutableStateOf<Pair<Int, Int>?>(null)
        private set

    var isLoading = mutableStateOf(false)
        private set

    var isNotesMode = mutableStateOf(false)
        private set

    // Инициализация Retrofit (обычно через DI, но для простоты тут)
    private val api = Retrofit.Builder()
        .baseUrl("https://www.youdosudoku.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(SudokuApi::class.java)

    private var currentApiKey = "8r06MaG-mvbrgGVymkGGOPVCS3_CT21dIogoAhvSH_M"

    init {
        startNewGame()
    }

    /** Создаёт пустое поле 9×9 */
    private fun createEmptyBoard() {
        board.clear()
        repeat(9) {
            board.add(List(9) { SudokuCell(0, true, 0) })
        }
    }

    fun startNewGame() {
        Log.e("Start game", "On press button")
        viewModelScope.launch {
            isLoading.value = true
            isGameWon.value = false
            isNotesMode.value = false
            createEmptyBoard()

            var attempt = 0
            val maxAttempts = 5
            var success = false

            while (attempt < maxAttempts && !success) {
                attempt++
                try {
                    // Подготовка тела запроса
                    val body = SudokuRequest(
                        difficulty = selectedDifficulty.value,
                        solution = true,
                        array = false
                    )

                    // Запрос к API
                    Log.e("New game request", String.format("body = %s, key = %s", body, currentApiKey))
                    val response = api.getNewGame(body, currentApiKey)
                    Log.e("New game", response.toString())

                    // Проверка 1: Ошибка "Invalid API key" внутри JSON
                    if (response.error?.contains("Invalid API key", ignoreCase = true) == true) {
                        // Генерируем новый ключ
                        val newKeyResponse = api.createKey()
                        Log.e("New key", newKeyResponse.toString())
                        currentApiKey = newKeyResponse.key
                        continue
                    }

                    // Проверка 2: Другие ошибки API
                    if (response.error != null) {
                        delay(500) // Небольшая пауза перед retry
                        continue
                    }

                    // Успех: есть пазл
                    if (response.puzzle != null && response.solution != null) {
                        parseBoard(response.puzzle, response.solution)
                        success = true
                        Log.e("Parse board", "success")
                    }

                } catch (e: Exception) {
                    // Ошибки сети (нет интернета, таймаут, 500-е ошибки сервера)
                    Log.e("Request error", e.stackTraceToString())
                    e.printStackTrace()
                    if (attempt < maxAttempts) {
                        delay(1000) // Ждем секунду перед retry
                    }
                }
            }

            // Если после всех попыток успеха нет, можно вывести сообщение (пока просто лог)
            if (!success) {
                println("Failed to load game after $maxAttempts attempts")
            }

            isLoading.value = false
        }
    }


    private fun parseBoard(puzzle: String, solution: String) {
        val newBoard = mutableListOf<List<SudokuCell>>()
        for (i in 0 until 81) {
            if (i % 9 == 0) newBoard.add(mutableListOf())

            val puzzleChar = puzzle.getOrNull(i) ?: '0'
            val solutionChar = solution.getOrNull(i) ?: '0'

            val value = puzzleChar.digitToIntOrNull() ?: 0
            val solutionValue = solutionChar.digitToIntOrNull() ?: 0

            // Если в puzzle 0 - ячейка редактируемая
            val isEditable = value == 0

            (newBoard.last() as MutableList).add(SudokuCell(value, isEditable, solutionValue))
        }
        board.clear()
        board.addAll(newBoard)
    }

    fun onCellClicked(row: Int, col: Int) {
        if (isGameWon.value) return
        if (board[row][col].isEditable) {
            selectedCell.value = Pair(row, col)
        }
    }

    fun toggleNotesMode() {
        isNotesMode.value = !isNotesMode.value
    }

    fun onKeyPressed(number: Int) {
        if (number == 0) {
            eraseCurrentCell()
            return
        }

        val (row, col) = selectedCell.value ?: return
        val currentCell = board[row][col]
        if (!currentCell.isEditable) return

        if (isNotesMode.value) {
            if (currentCell.value != 0 || number in currentCell.notes || currentCell.notes.size >= 4) return
            board[row] = board[row].toMutableList().also {
                it[col] = currentCell.copy(notes = currentCell.notes + number)
            }
            return
        }

        board[row] = board[row].toMutableList().also {
            it[col] = currentCell.copy(value = number, notes = emptyList())
        }
        checkWin()
    }

    fun eraseCurrentCell() {
        val (row, col) = selectedCell.value ?: return
        val currentCell = board[row][col]
        if (!currentCell.isEditable) return

        val updatedCell = if (currentCell.notes.isNotEmpty()) {
            currentCell.copy(notes = currentCell.notes.dropLast(1))
        } else {
            currentCell.copy(value = 0, notes = emptyList())
        }
        board[row] = board[row].toMutableList().also { it[col] = updatedCell }
    }

    fun useHint() {
        val (row, col) = selectedCell.value ?: return
        val currentCell = board[row][col]
        if (!currentCell.isEditable) return

        board[row] = board[row].toMutableList().also {
            it[col] = currentCell.copy(value = currentCell.solutionValue, notes = emptyList())
        }
        checkWin()
    }

    fun setDifficulty(diff: String) {
        selectedDifficulty.value = diff
    }

    private fun checkWin() {
        // Функция all вернет true, только если условие верно для ВСЕХ ячеек
        // Как только встретится хоть одна неверная, она остановится и вернет false
        isGameWon.value = board.all { row ->
            row.all { cell ->
                cell.value == cell.solutionValue
            }
        }
    }
}
