package com.siraquiz.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val Bg = Color(0xFF071A16)
private val Panel = Color(0xFF0D2A23)
private val Gold = Color(0xFFE7B84B)
private val Cream = Color(0xFFF5E8C8)
private val Green = Color(0xFF159447)
private val Locked = Color(0xFF59635F)
private val Current = Color(0xFFE7B84B)

private const val PREFS = "sira_progress"
private const val KEY_UNLOCKED = "unlocked"
private const val KEY_CURRENT = "current"
private const val KEY_SCORE = "score"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SiraQuizApp() }
    }
}

@Composable
fun SiraQuizApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    }

    var screen by remember { mutableStateOf("home") }
    var unlocked by remember { mutableIntStateOf(prefs.getInt(KEY_UNLOCKED, 1).coerceAtLeast(1)) }
    var current by remember { mutableIntStateOf(prefs.getInt(KEY_CURRENT, 0).coerceAtLeast(0)) }
    var score by remember { mutableIntStateOf(prefs.getInt(KEY_SCORE, 0).coerceAtLeast(0)) }
    var selected by remember { mutableIntStateOf(-1) }
    var seconds by remember { mutableIntStateOf(30) }

    fun saveProgress() {
        prefs.edit()
            .putInt(KEY_UNLOCKED, unlocked)
            .putInt(KEY_CURRENT, current)
            .putInt(KEY_SCORE, score)
            .apply()
    }

    fun openQuestion(number: Int) {
        if (number in 1..unlocked && number <= questionBank.size) {
            current = number - 1
            selected = -1
            seconds = 30
            saveProgress()
            screen = "quiz"
        }
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Gold,
            background = Bg,
            surface = Panel,
            onPrimary = Color.Black,
            onBackground = Cream,
            onSurface = Cream
        )
    ) {
        Surface(Modifier.fillMaxSize(), color = Bg) {
            when (screen) {
                "home" -> HomeScreen(
                    hasProgress = current > 0 || unlocked > 1,
                    onContinue = {
                        openQuestion(current + 1)
                    },
                    onStart = {
                        current = 0
                        unlocked = 1
                        score = 0
                        selected = -1
                        seconds = 30
                        saveProgress()
                        screen = "map"
                    }
                )

                "map" -> QuestionMapScreen(
                    total = questionBank.size,
                    unlocked = unlocked,
                    current = current,
                    onBack = { screen = "home" },
                    onQuestion = { openQuestion(it) }
                )

                "quiz" -> {
                    val q = questionBank.getOrNull(current)
                    if (q == null) {
                        screen = "map"
                    } else {
                        QuizScreen(
                            question = q,
                            number = current + 1,
                            total = questionBank.size,
                            selected = selected,
                            seconds = seconds,
                            onSelect = { answer ->
                                if (selected == -1) {
                                    selected = answer
                                }
                            },
                            onTick = {
                                if (seconds > 0 && selected == -1) {
                                    seconds--
                                }
                            },
                            onNext = {
                                if (selected != -1 || seconds == 0) {
                                    if (selected == q.correct) {
                                        score += 10
                                    }

                                    if (current + 2 > unlocked) {
                                        unlocked = (current + 2).coerceAtMost(questionBank.size)
                                    }

                                    if (current < questionBank.lastIndex) {
                                        current++
                                        selected = -1
                                        seconds = 30
                                        saveProgress()
                                    } else {
                                        saveProgress()
                                        screen = "result"
                                    }
                                }
                            },
                            onBack = {
                                saveProgress()
                                screen = "map"
                            }
                        )
                    }
                }

                "result" -> ResultScreen(
                    score = score,
                    total = questionBank.size,
                    onMap = { screen = "map" },
                    onRestart = {
                        unlocked = 1
                        current = 0
                        score = 0
                        selected = -1
                        seconds = 30
                        saveProgress()
                        screen = "map"
                    }
                )
            }
        }
    }
}

@Composable
private fun HomeScreen(
    hasProgress: Boolean,
    onContinue: () -> Unit,
    onStart: () -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.sira_background),
            contentDescription = "Фон Сира — Викторина",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.18f))
        )

        Column(
            Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(1f))

            if (hasProgress) {
                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("ПРОДОЛЖИТЬ", fontWeight = FontWeight.Bold, color = Color.Black)
                }
                Spacer(Modifier.height(14.dp))
            }

            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Green),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(
                    if (hasProgress) "НАЧАТЬ ЗАНОВО" else "НАЧАТЬ ВИКТОРИНУ",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(18.dp))
        }
    }
}

private fun questionIcon(category: String): String {
    return when {
        category.contains("Детство") || category.contains("юность") -> "👶"
        category.contains("пророче") || category.contains("откров") -> "📖"
        category.contains("Меккан") -> "🕋"
        category.contains("Хиджра") || category.contains("Медина") -> "🕌"
        category.contains("Бадр") || category.contains("Ухуд") || category.contains("Хандак") -> "🛡️"
        category.contains("Акаба") -> "🤝"
        category.contains("Семья") || category.contains("семья") -> "❤️"
        category.contains("Хайбар") || category.contains("Табук") -> "⚔️"
        else -> "📜"
    }
}

@Composable
private fun QuestionMapScreen(
    total: Int,
    unlocked: Int,
    current: Int,
    onBack: () -> Unit,
    onQuestion: (Int) -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Bg)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Назад",
                    tint = Gold,
                    modifier = Modifier.size(32.dp)
                )
            }
            Column {
                Text(
                    "КАРТА ВОПРОСОВ",
                    color = Cream,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    "Пройдено: ${unlocked - 1} из $total",
                    color = Gold,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = {
                if (total == 0) 0f
                else (unlocked - 1).toFloat() / total.toFloat()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(10.dp)),
            color = Green,
            trackColor = Color(0xFF23443B)
        )

        Spacer(Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp)
        ) {
            itemsIndexed(
                items = List(total) { it + 1 },
                key = { _, number -> number }
            ) { _, number ->

                val isUnlocked = number <= unlocked
                val isCurrent = number == current + 1
                val isCompleted = number < unlocked

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isCurrent -> Current
                                    isCompleted -> Green
                                    else -> Locked
                                }
                            )
                            .then(
                                if (isUnlocked) {
                                    Modifier.clickable { onQuestion(number) }
                                } else {
                                    Modifier
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (!isUnlocked) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = "Закрыто",
                                    tint = Color(0xFFD9DDDB),
                                    modifier = Modifier.size(22.dp)
                                )
                            } else {
                                Text(
                                    questionIcon(questionBank[number - 1].category),
                                    fontSize = 17.sp
                                )
                            }

                            Text(
                                number.toString(),
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizScreen(
    question: QuizQuestion,
    number: Int,
    total: Int,
    selected: Int,
    seconds: Int,
    onSelect: (Int) -> Unit,
    onTick: () -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    LaunchedEffect(number, selected) {
        if (selected == -1) {
            while (seconds > 0) {
                delay(1000)
                onTick()
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Bg)
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Назад", tint = Gold)
            }
            Column {
                Text(
                    "Вопрос $number из $total",
                    color = Cream,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(questionIcon(question.category), fontSize = 14.sp)
            }
            Spacer(Modifier.weight(1f))
            Text(
                "${seconds}s",
                color = if (seconds <= 5) Color.Red else Gold,
                fontWeight = FontWeight.Bold
            )
        }

        LinearProgressIndicator(
            progress = { number.toFloat() / total.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            color = Green,
            trackColor = Color(0xFF23443B)
        )

        Text(question.category, color = Gold, fontSize = 12.sp)

        Spacer(Modifier.height(12.dp))

        Card(
            Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Cream),
            shape = RoundedCornerShape(22.dp)
        ) {
            Text(
                question.question,
                color = Color(0xFF18231F),
                fontSize = 19.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(20.dp)
            )
        }

        Spacer(Modifier.height(12.dp))

        for (i in question.options.indices) {
            val active = selected == i

            OutlinedButton(
                onClick = { onSelect(i) },
                enabled = selected == -1,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .heightIn(min = 52.dp),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (active) Green else Panel,
                    contentColor = Cream
                )
            ) {
                Text(
                    "${('A'.code + i).toChar()}.  ${question.options[i]}",
                    fontSize = 15.sp
                )
            }
        }

        Spacer(Modifier.weight(1f))

        if (selected != -1 || seconds == 0) {
            val correctAnswer = selected == question.correct
            Text(
                if (correctAnswer) {
                    "✓ Правильно! ${question.explanation}"
                } else {
                    "✗ ${question.explanation}"
                },
                color = if (correctAnswer) Color(0xFF62E39B) else Color(0xFFFF9B8F),
                modifier = Modifier.padding(6.dp),
                textAlign = TextAlign.Center
            )
        }

        Button(
            onClick = onNext,
            enabled = selected != -1 || seconds == 0,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Gold,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                if (number == total) "ЗАВЕРШИТЬ" else "СЛЕДУЮЩИЙ ВОПРОС",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ResultScreen(
    score: Int,
    total: Int,
    onMap: () -> Unit,
    onRestart: () -> Unit
) {
    val max = total * 10
    val correct = score / 10

    Column(
        Modifier
            .fillMaxSize()
            .background(Bg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "السيرة النبوية",
            color = Gold,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Викторина завершена",
            color = Cream,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "$score / $max",
            color = Gold,
            fontSize = 52.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Правильных ответов: $correct из $total",
            color = Cream,
            fontSize = 17.sp
        )

        Spacer(Modifier.height(26.dp))

        Button(
            onClick = onMap,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Green),
            shape = RoundedCornerShape(18.dp)
        ) {
            Text("К КАРТЕ ВОПРОСОВ", fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onRestart,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Gold),
            shape = RoundedCornerShape(18.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("НАЧАТЬ ЗАНОВО", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}
