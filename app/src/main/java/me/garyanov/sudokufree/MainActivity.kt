package me.garyanov.sudokufree

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import me.garyanov.sudokufree.model.SudokuCell

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                SudokuScreen()
            }
        }
    }
}

@Composable
fun SudokuScreen(viewModel: SudokuViewModel = viewModel()) {
    // Переменные состояния должны быть внутри Composable функции
    var difficultyExpanded by remember { mutableStateOf(false) }

    // Цвет фона зависит от победы
    val backgroundColor = if (viewModel.isGameWon.value) Color(0xFFCCFFCC) else Color.White

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Судоку",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(8.dp)
        )

        // Панель управления
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Кнопка новой игры
            Button(onClick = { viewModel.startNewGame() }) {
                Text("Новая игра")
            }

            // Выбор сложности
            Box {
                Button(onClick = { difficultyExpanded = true }) {
                    Text(viewModel.selectedDifficulty.value)
                }
                DropdownMenu(
                    expanded = difficultyExpanded,
                    onDismissRequest = { difficultyExpanded = false }
                ) {
                    listOf("easy", "medium", "hard").forEach { diff ->
                        DropdownMenuItem(
                            text = { Text(diff) },
                            onClick = {
                                viewModel.setDifficulty(diff)
                                difficultyExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Игровое поле
        if (viewModel.isLoading.value) {
            CircularProgressIndicator()
        } else {
            SudokuBoard(
                board = viewModel.board,
                selectedCell = viewModel.selectedCell.value,
                onCellClick = { r, c -> viewModel.onCellClicked(r, c) },
                isGameWon = viewModel.isGameWon.value
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Клавиатура
        NumericKeypad(
            onKeyPress = { num -> viewModel.onKeyPressed(num) },
            onHint = { viewModel.useHint() },
            isGameWon = viewModel.isGameWon.value
        )
    }
}

@Composable
fun SudokuBoard(
    board: List<List<SudokuCell>>,
    selectedCell: Pair<Int, Int>?,
    onCellClick: (Int, Int) -> Unit,
    isGameWon: Boolean
) {
    // Внешний контейнер с толстой рамкой
    Box(
        modifier = Modifier
            .border(2.dp, Color.Black) // Толстая внешняя рамка
            .size(360.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            board.forEachIndexed { rowIndex, row ->
                Row(modifier = Modifier.weight(1f)) {
                    row.forEachIndexed { colIndex, cell ->
                        val isSelected = selectedCell == Pair(rowIndex, colIndex)

                        // Определяем цвет фона
                        val cellBgColor = when {
                            isSelected -> Color.LightGray
                            !cell.isEditable -> Color(0xFFEEEEEE) // Серый для неизменяемых
                            else -> Color.White
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .border(0.5.dp, Color.Gray) // Тонкие внутренние линии
                                .background(cellBgColor)
                                .clickable(enabled = !isGameWon) {
                                    onCellClick(rowIndex, colIndex)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (cell.value == 0) "" else cell.value.toString(),
                                fontSize = 24.sp,
                                fontWeight = if (cell.isEditable) FontWeight.Normal else FontWeight.Bold,
                                color = if (cell.isEditable) Color.Blue else Color.Black
                            )
                        }
                    }
                }

                // Рисуем толстую горизонтальную линию после 3-го и 6-го ряда
                if (rowIndex == 2 || rowIndex == 5) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(Color.Black)
                    )
                }
            }
        }
        // Рисуем толстые вертикальные линии (поверх ячеек)
        // Это немного "хак" для простоты, чтобы не усложнять верстку каждого ряда
        Row(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.weight(1f))
            Box(modifier = Modifier.width(2.dp).fillMaxHeight().background(Color.Black))
            Spacer(modifier = Modifier.weight(1f))
            Box(modifier = Modifier.width(2.dp).fillMaxHeight().background(Color.Black))
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun NumericKeypad(
    onKeyPress: (Int) -> Unit,
    onHint: () -> Unit,
    isGameWon: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Ряд 1-9
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            (1..9).forEach { num ->
                Button(
                    onClick = { onKeyPress(num) },
                    enabled = !isGameWon,
                    modifier = Modifier.size(38.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("$num", fontSize = 18.sp)
                }
            }
        }

        // Ряд 0 и Подсказка
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
        ) {
            Button(
                onClick = { onKeyPress(0) }, // 0 = очистить
                enabled = !isGameWon,
                modifier = Modifier.size(60.dp, 38.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text("C", fontSize = 18.sp) // C = Clear
            }
            Button(
                onClick = { onHint() },
                enabled = !isGameWon,
                modifier = Modifier.size(60.dp, 38.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("?", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}