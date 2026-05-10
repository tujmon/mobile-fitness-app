package com.hackerfit.ui.screens.onboarding

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val title: String,
    val subtitle: String,
    val body: String
)

private val pages = listOf(
    OnboardingPage(
        title = "Bem-vindo ao HackerFit",
        subtitle = "Exercicio para viver mais e melhor",
        body = "Baseado no livro \"The Hacker's Diet\" de John Walker. Exerc\u00edcios di\u00e1rios de 10 a 15 minutos, sem equipamento, que podem ser feitos em qualquer lugar."
    ),
    OnboardingPage(
        title = "A Escada Fitness",
        subtitle = "48 degraus de progresso",
        body = "O programa \u00e9 dividido em duas fases: a Escada Introdut\u00f3ria (degraus 1-15) com exerc\u00edcios mais f\u00e1ceis, e a Escada Vital\u00edcia (degraus 16-48) com exerc\u00edcios completos. Voc\u00ea avan\u00e7a no seu pr\u00f3prio ritmo."
    ),
    OnboardingPage(
        title = "Os 5 Exercicios",
        subtitle = "Simples e eficazes",
        body = "Cada degrau inclui 5 exerc\u00edcios: Flex\u00e3o para Frente, Abdominal, Eleva\u00e7\u00e3o de Pernas, Flex\u00e3o de Bra\u00e7o e Corrida com Salto. Sem dor, sem press\u00e3o, progresso no seu ritmo."
    ),
    OnboardingPage(
        title = "Vamos Comecar!",
        subtitle = "Degrau 1 te espera",
        body = "Todo mundo come\u00e7a no Degrau 1, independentemente do condicionamento f\u00edsico. Respeite seu corpo, pare se sentir dor, e avance quando estiver pronto."
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.weight(0.5f))

                Text(
                    text = pages[page].title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = pages[page].subtitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = pages[page].body,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.weight(1f))
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                pages.indices.forEach { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (isSelected) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant
                            )
                    )
                }
            }

            Button(
                onClick = {
                    if (pagerState.currentPage < pages.lastIndex) {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
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
                    text = if (pagerState.currentPage < pages.lastIndex) "Pr\u00f3ximo" else "Come\u00e7ar",
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (pagerState.currentPage < pages.lastIndex) {
                TextButton(
                    onClick = onComplete,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Pular",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
