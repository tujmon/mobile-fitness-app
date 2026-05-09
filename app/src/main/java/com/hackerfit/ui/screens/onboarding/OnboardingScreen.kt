package com.hackerfit.ui.screens.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class OnboardingPage(
    val title: String,
    val subtitle: String,
    val body: String
)

private val pages = listOf(
    OnboardingPage(
        title = "Bem-vindo ao HackerFit",
        subtitle = "Exerc\u00edcio para viver mais e melhor",
        body = "Baseado no livro \"The Hacker's Diet\" de John Walker. Exerc\u00edcios di\u00e1rios de 10 a 15 minutos, sem equipamento, que podem ser feitos em qualquer lugar."
    ),
    OnboardingPage(
        title = "A Escada Fitness",
        subtitle = "48 degraus de progresso",
        body = "O programa \u00e9 dividido em duas fases: a Escada Introdut\u00f3ria (degraus 1\u201315) com exerc\u00edcios mais f\u00e1ceis, e a Escada Vital\u00edcia (degraus 16\u201348) com exerc\u00edcios completos. Voc\u00ea avan\u00e7a no seu pr\u00f3prio ritmo."
    ),
    OnboardingPage(
        title = "Os 5 Exerc\u00edcios",
        subtitle = "Simples e eficazes",
        body = "Cada degrau inclui 5 exerc\u00edcios: Flex\u00e3o para Frente, Abdominal, Eleva\u00e7\u00e3o de Pernas, Flex\u00e3o de Bra\u00e7o e Corrida com Salto. Sem dor, sem press\u00e3o, progresso no seu ritmo."
    ),
    OnboardingPage(
        title = "Vamos Come\u00e7ar!",
        subtitle = "Degrau 1 te espera",
        body = "Todo mundo come\u00e7a no Degrau 1, independentemente do condicionamento f\u00edsico. Respeite seu corpo, pare se sentir dor, e avance quando estiver pronto."
    )
)

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    var currentPage by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = pages[currentPage].title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = pages[currentPage].subtitle,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = pages[currentPage].body,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            pages.indices.forEach { index ->
                HorizontalDivider(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .width(32.dp)
                        .height(4.dp),
                    color = if (index == currentPage) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    },
                    thickness = 4.dp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (currentPage < pages.lastIndex) {
                    currentPage++
                } else {
                    onComplete()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = if (currentPage < pages.lastIndex) "Pr\u00f3ximo" else "Come\u00e7ar",
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
