package me.garyanov.sudokufree

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import me.garyanov.sudokufree.model.SudokuCell
import java.util.Locale

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
    var isRussian by remember { mutableStateOf(Locale.getDefault().language == "ru") }

    // Цвет фона зависит от победы
    val backgroundColor = if (viewModel.isGameWon.value) Color(0xFFCCFFCC) else Color.White

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        val boxScope = this@BoxWithConstraints
        val compact = boxScope.maxHeight < 760.dp
        val veryCompact = boxScope.maxHeight < 560.dp
        val keySize = if (boxScope.maxHeight < 440.dp) 36.dp else if (veryCompact) 40.dp else if (compact) 48.dp else 64.dp
        val keyGap = if (boxScope.maxHeight < 440.dp) 2.dp else if (veryCompact) 4.dp else if (compact) 6.dp else 12.dp
        val sectionGap = if (compact) 6.dp else 16.dp
        val controlsHeight = if (compact) 84.dp else 112.dp
        val winMessageSpace = if (viewModel.isGameWon.value) 48.dp else 0.dp
        val boardSize = minOf(boxScope.maxWidth - 16.dp,
            boxScope.maxHeight - controlsHeight - sectionGap * 2 - keySize * 4 - keyGap * 3 - winMessageSpace - 16.dp
        ).coerceAtLeast(96.dp)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(horizontal = if (compact) 4.dp else 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            )
            {
                Button(
                    onClick = { isRussian = !isRussian },
                    contentPadding = PaddingValues(
                        horizontal = 10.dp,
                        vertical = if (compact) 2.dp else 4.dp
                    ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Gray,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        if (isRussian) "En" else "Ru",
                        fontSize = if (compact) 11.sp else 12.sp
                    )
                }
            }

        // Панель управления
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
            // Кнопка новой игры
                Button(
                    onClick = { viewModel.startNewGame() },
                    contentPadding = PaddingValues(horizontal = if (compact) 8.dp else 16.dp, vertical = if (compact) 4.dp else 8.dp)
                ) {
                    Text(if (isRussian) "Новая игра" else "New Game", fontSize = if (compact) 12.sp else 14.sp)
                }

                Text(
                    text = if (isRussian) "Судоку" else "Sudoku",
                    style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(if (compact) 2.dp else 8.dp)
                )

            // Выбор сложности
                Box {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(if (compact) 2.dp else 4.dp)
                    ) {
                        Button(
                            onClick = { difficultyExpanded = true },
                            contentPadding = PaddingValues(horizontal = if (compact) 6.dp else 12.dp, vertical = if (compact) 4.dp else 8.dp)
                        ) {
                            Text(
                                text = when (viewModel.selectedDifficulty.value) {
                                    "easy" -> if (isRussian) "Лёгкий" else "Easy"
                                    "medium" -> if (isRussian) "Средний" else "Medium"
                                    else -> if (isRussian) "Сложный" else "Hard"
                                },
                                fontSize = if (compact) 11.sp else 14.sp
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = difficultyExpanded,
                        onDismissRequest = { difficultyExpanded = false }
                    ) {
                        listOf("easy", "medium", "hard").forEach { diff ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        when (diff) {
                                            "easy" -> if (isRussian) "Лёгкий" else "Easy"
                                            "medium" -> if (isRussian) "Средний" else "Medium"
                                            else -> if (isRussian) "Сложный" else "Hard"
                                        }
                                    )
                                },
                                onClick = {
                                    viewModel.setDifficulty(diff)
                                    difficultyExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(sectionGap))

        // Если игра ещё не загружена — показываем пустую сетку 9×9
            val displayBoard = viewModel.board.ifEmpty {
                List(9) { List(9) { SudokuCell(0, true, 0) } }
            }
        // Игровое поле
            if (viewModel.isLoading.value) {
                CircularProgressIndicator()
            } else {
                SudokuBoard(
                    board = displayBoard,
                    selectedCell = viewModel.selectedCell.value,
                    onCellClick = { r, c ->
                    // Клики обрабатываем только когда реальная доска загружена
                        if (viewModel.board.isNotEmpty())
                            viewModel.onCellClicked(r, c)
                    },
                    isGameWon = viewModel.isGameWon.value,
                    boardSize = boardSize
                )
            }

            if (viewModel.isGameWon.value) {
                Spacer(modifier = Modifier.height(if (compact) 4.dp else 12.dp))
                Text(
                    text = if (isRussian) "Победа!" else "You won!",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (compact) 16.sp else 22.sp
                )
            } else {
                Spacer(modifier = Modifier.height(sectionGap))
            }

        // Клавиатура
            NumericKeypad(
                onKeyPress = { num -> viewModel.onKeyPressed(num) },
                onHint = { viewModel.useHint() },
                onToggleNotes = { viewModel.toggleNotesMode() },
                isNotesMode = viewModel.isNotesMode.value,
                isGameWon = viewModel.isGameWon.value,
                keySize = keySize,
                keyGap = keyGap
            )
        }
    }
}

@Composable
fun SudokuBoard(
    board: List<List<SudokuCell>>,
    selectedCell: Pair<Int, Int>?,
    onCellClick: (Int, Int) -> Unit,
    isGameWon: Boolean,
    boardSize: androidx.compose.ui.unit.Dp = 360.dp
) {
    // Внешний контейнер с толстой рамкой
    Box(
        modifier = Modifier
            .border(2.dp, Color.Black) // Толстая внешняя рамка
            .size(boardSize)
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
                            if (cell.value != 0) {
                                Text(
                                    text = cell.value.toString(),
                                    fontSize = 24.sp,
                                    fontWeight = if (cell.isEditable) FontWeight.Normal else FontWeight.Bold,
                                    color = if (cell.isEditable) Color.Blue else Color.Black
                                )
                            } else {
                                DraftNotes(notes = cell.notes, boardSize = boardSize)
                            }
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
    onToggleNotes: () -> Unit,
    isNotesMode: Boolean,
    isGameWon: Boolean,
    keySize: androidx.compose.ui.unit.Dp = 64.dp,
    keyGap: androidx.compose.ui.unit.Dp = 12.dp
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(keyGap)
    ) {
        // 3 ряда по 3 кнопки (1-9)
        for (row in 0 until 3) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(keyGap)
            ) {
                for (col in 0 until 3) {
                    val number = row * 3 + col + 1
                    Button(
                        onClick = { onKeyPress(number) },
                        enabled = !isGameWon,
                        modifier = Modifier.size(keySize),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("$number", fontSize = if (keySize <= 40.dp) 18.sp else if (keySize <= 48.dp) 20.sp else 24.sp)
                    }
                }
            }
        }

        // Кнопки очистки, черновика и подсказки
        Row(
            horizontalArrangement = Arrangement.spacedBy(keyGap, Alignment.CenterHorizontally)
        ) {
            Button(
                onClick = { onKeyPress(0) },
                enabled = !isGameWon,
                modifier = Modifier.size(keySize * 1.25f, keySize),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                ActionIcon(isHint = false, isPencil = false, size = keySize * 0.5f)
            }
            Button(
                onClick = onToggleNotes,
                enabled = !isGameWon,
                modifier = Modifier.size(keySize, keySize),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isNotesMode) Color.DarkGray else Color.Gray
                )
            ) {
                ActionIcon(isHint = false, isPencil = true, size = keySize * 0.5f)
            }
            Button(
                onClick = { onHint() },
                enabled = !isGameWon,
                modifier = Modifier.size(keySize * 1.25f, keySize),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                ActionIcon(isHint = true, isPencil = false, size = keySize * 0.5f)
            }
        }
    }
}
@Composable
private fun ActionIcon(isHint: Boolean, isPencil: Boolean, size: Dp) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeWidth = w * 0.075f

        if (isHint) {
            val bulb = Path().apply {
                moveTo(w * 0.36f, h * 0.69f)
                cubicTo(w * 0.34f, h * 0.58f, w * 0.22f, h * 0.51f, w * 0.22f, h * 0.38f)
                cubicTo(w * 0.22f, h * 0.20f, w * 0.35f, h * 0.10f, w * 0.50f, h * 0.10f)
                cubicTo(w * 0.65f, h * 0.10f, w * 0.78f, h * 0.20f, w * 0.78f, h * 0.38f)
                cubicTo(w * 0.78f, h * 0.51f, w * 0.66f, h * 0.58f, w * 0.64f, h * 0.69f)
                close()
            }
            drawPath(bulb, color = Color.White, style = Stroke(width = strokeWidth))
            drawLine(Color.White, androidx.compose.ui.geometry.Offset(w * 0.38f, h * 0.75f), androidx.compose.ui.geometry.Offset(w * 0.62f, h * 0.75f), strokeWidth)
            drawLine(Color.White, androidx.compose.ui.geometry.Offset(w * 0.40f, h * 0.84f), androidx.compose.ui.geometry.Offset(w * 0.60f, h * 0.84f), strokeWidth)
            drawLine(Color.White, androidx.compose.ui.geometry.Offset(w * 0.50f, h * 0.01f), androidx.compose.ui.geometry.Offset(w * 0.50f, h * 0.07f), strokeWidth)
            drawLine(Color.White, androidx.compose.ui.geometry.Offset(w * 0.08f, h * 0.20f), androidx.compose.ui.geometry.Offset(w * 0.16f, h * 0.25f), strokeWidth)
            drawLine(Color.White, androidx.compose.ui.geometry.Offset(w * 0.92f, h * 0.20f), androidx.compose.ui.geometry.Offset(w * 0.84f, h * 0.25f), strokeWidth)
        } else if (isPencil) {
            val pencil = Path().apply {
                moveTo(w * 0.24f, h * 0.70f)
                lineTo(w * 0.69f, h * 0.25f)
                lineTo(w * 0.82f, h * 0.38f)
                lineTo(w * 0.37f, h * 0.83f)
                close()
            }
            drawPath(pencil, color = Color.White)
            val tip = Path().apply {
                moveTo(w * 0.24f, h * 0.70f)
                lineTo(w * 0.37f, h * 0.83f)
                lineTo(w * 0.18f, h * 0.89f)
                close()
            }
            drawPath(tip, color = Color.White)
            drawLine(Color.Gray, androidx.compose.ui.geometry.Offset(w * 0.36f, h * 0.58f), androidx.compose.ui.geometry.Offset(w * 0.49f, h * 0.71f), strokeWidth)
            drawLine(Color.White, androidx.compose.ui.geometry.Offset(w * 0.73f, h * 0.21f), androidx.compose.ui.geometry.Offset(w * 0.86f, h * 0.34f), strokeWidth)
        } else {
            val eraser = Path().apply {
                moveTo(w * 0.18f, h * 0.60f)
                lineTo(w * 0.48f, h * 0.20f)
                cubicTo(w * 0.52f, h * 0.15f, w * 0.58f, h * 0.14f, w * 0.63f, h * 0.18f)
                lineTo(w * 0.84f, h * 0.34f)
                cubicTo(w * 0.89f, h * 0.38f, w * 0.90f, h * 0.44f, w * 0.86f, h * 0.49f)
                lineTo(w * 0.60f, h * 0.82f)
                lineTo(w * 0.38f, h * 0.82f)
                lineTo(w * 0.20f, h * 0.69f)
                cubicTo(w * 0.16f, h * 0.67f, w * 0.15f, h * 0.63f, w * 0.18f, h * 0.60f)
                close()
            }
            drawPath(eraser, color = Color.White)
            drawLine(Color.Gray, androidx.compose.ui.geometry.Offset(w * 0.36f, h * 0.39f), androidx.compose.ui.geometry.Offset(w * 0.66f, h * 0.62f), strokeWidth)
            drawLine(Color.White, androidx.compose.ui.geometry.Offset(w * 0.39f, h * 0.88f), androidx.compose.ui.geometry.Offset(w * 0.76f, h * 0.88f), strokeWidth)
        }
    }
}
@Composable
private fun DraftNotes(notes: List<Int>, boardSize: Dp) {
    if (notes.isEmpty()) return

    val positions = listOf(Alignment.TopStart, Alignment.TopEnd, Alignment.BottomStart, Alignment.BottomEnd)
    val noteFontSize = (boardSize.value / 6f ).coerceIn(8f, 12f).sp
    Box(modifier = Modifier.fillMaxSize()) {
        notes.take(4).forEachIndexed { index, note ->
            Box(
                modifier = Modifier
                    .align(positions[index])
                    .fillMaxWidth(0.5f)
                    .fillMaxHeight(0.5f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = note.toString(),
                    color = Color(0xFF7896C8),
                    fontSize = noteFontSize,
                    lineHeight = noteFontSize
                )
            }
        }
    }
}
