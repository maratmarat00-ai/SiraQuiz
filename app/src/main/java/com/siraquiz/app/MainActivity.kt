package com.siraquiz.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
data class QuizQuestion(
    val category: String,
    val question: String,
    val options: List<String>,
    val correct: Int,
    val explanation: String
)

private val questions = questionBank
private val bg = Color(0xFF071A16)
private val panel = Color(0xFF0D2A23)
private val gold = Color(0xFFE7B84B)
private val cream = Color(0xFFF5E8C8)
private val green = Color(0xFF159447)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SiraQuizApp() }
    }
}

@Composable
fun SiraQuizApp() {
    var screen by remember { mutableStateOf("home") }
    var activeQuestions by remember { mutableStateOf(questions) }
    var index by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var selected by remember { mutableIntStateOf(-1) }
    var seconds by remember { mutableIntStateOf(30) }

    fun startQuiz(list: List<QuizQuestion>) {
        if (list.isEmpty()) return

        activeQuestions = list.shuffled()
        index = 0
        score = 0
        selected = -1
        seconds = 30
        screen = "quiz"
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = gold,
            background = bg,
            surface = panel,
            onPrimary = Color.Black,
            onBackground = cream,
            onSurface = cream
        )
    ) {
        Surface(
            Modifier.fillMaxSize(),
            color = bg
        ) {
            AnimatedContent(
                targetState = screen,
                label = "screen"
            ) { s ->

                when (s) {

                    "home" -> HomeScreen {
                        screen = "categories"
                    }

                    "categories" -> CategoriesScreen(
                        
onSelect = { level ->

    val prefix = level.removeSuffix(" ВОПРОСЫ")

val selectedQuestions = questions.filter {
    it.category.startsWith(prefix)
}
    startQuiz(selectedQuestions)
},
                                
                        onBack = {
                            screen = "home"
                        }
                    )

                    "quiz" -> QuizScreen(
                        activeQuestions,
                        index,
                        score,
                        selected,
                        seconds,

                        onSelect = {
                            selected = it
                        },

                        onTick = {
                            if (seconds > 0) seconds--
                        },

                        onNext = {

                            if (selected == activeQuestions[index].correct) {
                                score += 10
                            }

                            if (index == activeQuestions.lastIndex) {
                                screen = "result"
                            } else {
                                index++
                                selected = -1
                                seconds = 30
                            }
                        },

                        onHome = {
                            screen = "home"
                        }
                    )

                    else -> ResultScreen(
                        score,
                        activeQuestions.size
                    ) {
                        screen = "categories"
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(start: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
   Image(
    painter = painterResource(id = R.drawable.home_background),
    contentDescription = "Фон приложения",
    contentScale = ContentScale.Crop,
    modifier = Modifier.fillMaxSize()
)
        Button(
            onClick = start,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(0.82f)
                .padding(bottom = 90.dp)
                .height(64.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = green
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = null
            )

            Spacer(Modifier.width(8.dp))

            Text(
                "НАЧАТЬ ВИКТОРИНУ",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


            @Composable
fun CategoriesScreen(
    onSelect: (String) -> Unit,
    onBack: () -> Unit
) {
    data class Level(
        val title: String,
        val subtitle: String,
        val image: Int,
        val accent: Color
    )

    val levels = listOf(
        Level(
            "ЛЁГКИЕ ВОПРОСЫ",
            "Начальный уровень",
            R.drawable.level_easy,
            Color(0xFF38D27A)
        ),
        Level(
            "СРЕДНИЕ ВОПРОСЫ",
            "Средний уровень",
            R.drawable.level_medium,
            Color(0xFFFFC533)
        ),
        Level(
            "СЛОЖНЫЕ ВОПРОСЫ",
            "Продвинутый уровень",
            R.drawable.level_hard,
            Color(0xFFFF5555)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {

        // Верхняя часть
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Назад",
                    tint = gold,
                    modifier = Modifier.size(34.dp)
                )
            }

            Column(
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Text(
                    text = "ВЫБЕРИТЕ УРОВЕНЬ",
                    color = cream,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "Проверьте свои знания сиры",
                    color = gold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Три красивые карточки
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            items(levels) { level ->

                Card(
                    onClick = {
                        onSelect(level.title)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = RoundedCornerShape(30.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF0B3028)
                    ),
                    border = BorderStroke(
                        2.dp,
                        level.accent.copy(alpha = 0.95f)
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 8.dp
                    )
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        // Изображение уровня
                        Box(
                            modifier = Modifier
                                .size(118.dp)
                                .clip(RoundedCornerShape(26.dp))
                                .background(
                                    level.accent.copy(alpha = 0.14f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(level.image),
                                contentDescription = level.title,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(
                                        RoundedCornerShape(26.dp)
                                    )
                            )
                        }

                        Spacer(
                            modifier = Modifier.width(18.dp)
                        )

                        // Текст
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {

                            Text(
                                text = level.title,
                                color = cream,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                lineHeight = 25.sp
                            )

                            Spacer(
                                modifier = Modifier.height(8.dp)
                            )

                            Text(
                                text = level.subtitle,
                                color = level.accent,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "Выбрать",
                            tint = level.accent,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }
            }
        }
    }
} 

@Composable
fun QuizScreen(
    list: List<QuizQuestion>, index: Int, score: Int, selected: Int, seconds: Int,
    onSelect: (Int) -> Unit, onTick: () -> Unit, onNext: () -> Unit, onHome: () -> Unit
) {
    LaunchedEffect(index) {
        while (seconds > 0 && selected == -1) { delay(1000); onTick() }
    }
    val q = list[index]
    Column(Modifier.fillMaxSize().padding(18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onHome) { Icon(Icons.Default.ArrowBack, "Назад", tint = gold) }
            Text("Вопрос ${index + 1} из ${list.size}", color = cream, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Text("${seconds}s", color = if (seconds <= 5) Color.Red else gold, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(
            progress = { (index + 1f) / list.size },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            color = green,
            trackColor = Color(0xFF23443B)
        )
        Text(q.category, color = gold, fontSize = 12.sp)
        Text("Счёт: $score", color = cream, fontSize = 14.sp)
        Spacer(Modifier.height(14.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = cream),
            shape = RoundedCornerShape(22.dp)
        ) {
            Text(
                q.question,
                color = Color(0xFF18231F),
                fontSize = 19.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(20.dp)
            )
        }
        Spacer(Modifier.height(14.dp))
        for (i in q.options.indices) {
            val option = q.options[i]
            val active = selected == i
            OutlinedButton(
                onClick = { if (selected == -1) onSelect(i) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).heightIn(min = 52.dp),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (active) green else panel,
                    contentColor = cream
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (active) gold else Color(0xFF4A695E)
                )
            ) {
                Text(
                    "${('A'.code + i).toChar()}.  $option",
                    fontSize = 15.sp,
                    textAlign = TextAlign.Start
                )
            }
        }
        Spacer(Modifier.weight(1f))
        if (selected != -1 || seconds == 0) {
            Text(
                if (selected == q.correct) "✓ Правильно! ${q.explanation}" else "✗ ${q.explanation}",
                color = if (selected == q.correct) Color(0xFF62E39B) else Color(0xFFFF9B8F),
                modifier = Modifier.padding(6.dp),
                textAlign = TextAlign.Center
            )
        }
        Button(
            onClick = onNext,
            enabled = selected != -1 || seconds == 0,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = gold,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                if (index == list.lastIndex) "ЗАВЕРШИТЬ" else "ДАЛЕЕ",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ResultScreen(score: Int, total: Int, restart: () -> Unit) {
    val max = total * 10
    val correct = score / 10
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("السيرة النبوية", color = gold, fontSize = 34.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text("Ваш результат", color = cream, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("$score / $max", color = gold, fontSize = 54.sp, fontWeight = FontWeight.Bold)
        Text("Правильных ответов: $correct из $total", color = cream, fontSize = 17.sp)
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = restart,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = green),
            shape = RoundedCornerShape(18.dp)
        ) {
            Icon(Icons.Default.Refresh, null)
            Spacer(Modifier.width(8.dp))
            Text("ВЫБРАТЬ СНОВА")
        }
    }
}
