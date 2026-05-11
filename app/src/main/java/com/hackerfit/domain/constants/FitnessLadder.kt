package com.hackerfit.domain.constants

data class ExerciseDefinition(
    val index: Int,
    val name: String,
    val introductoryDescription: String,
    val lifetimeDescription: String
)

data class RungData(
    val number: Int,
    val bend: Int,
    val sitUp: Int,
    val legLift: Int,
    val pushUp: Int,
    val runJumpSets: Int,
    val runJumpExtraSteps: Int
)

object FitnessLadder {

    val exercises = listOf(
        ExerciseDefinition(
            index = 0,
            name = "Flexao para Frente",
            introductoryDescription = "Fique em pe com as pernas afastadas, maos estendidas acima da cabeca. Dobre para frente o maximo que puder, tentando tocar os dedos dos pes. Endireite-se e dobre para tras moderadamente. Repita.",
            lifetimeDescription = "Fique em pe com as pernas afastadas, maos estendidas acima da cabeca. Dobre para frente e toque o chao entre as pernas, salte alguns centimetros para cima e toque o chao novamente. Endireite-se e dobre para tras. Repita."
        ),
        ExerciseDefinition(
            index = 1,
            name = "Abdominal",
            introductoryDescription = "Deite-se de costas, pes ligeiramente afastados, maos ao lado do corpo. Levante a cabeca e os ombros do chao o suficiente para ver os calcanhares. Abaixe suavemente. Repita.",
            lifetimeDescription = "Deite-se de costas, pes ligeiramente afastados, maos ao lado do corpo. Levante o corpo superior, dobrando na cintura, ate sentar-se verticalmente. Mantenha os bracos ao lado e os pes no chao. Abaixe suavemente. Repita."
        ),
        ExerciseDefinition(
            index = 2,
            name = "Elevacao de Pernas",
            introductoryDescription = "Deite-se de brucos, pernas ligeiramente afastadas, palmas sob as coxas. Levante a perna esquerda (dobrando no quadril e joelho) enquanto levanta a cabeca. Abaixe. Depois levante a perna direita e a cabeca da mesma forma. Cada repeticao conta ambas as pernas.",
            lifetimeDescription = "Deite-se de brucos, pernas ligeiramente afastadas, palmas sob as coxas. Levante AMBAS as pernas pelo menos alto o suficiente para que as coxas saiam das maos. Simultaneamente levante a cabeca e os ombros. Abaixe tudo suavemente. Repita."
        ),
        ExerciseDefinition(
            index = 3,
            name = "Flexao de Braco",
            introductoryDescription = "Deite-se de brucos, palmas logo fora dos ombros, bracos dobrados. Mantendo os JOELHOS no chao e permitindo que as pernas dobrem nos joelhos, levante o corpo superior ate que os bracos estejam esticados. Abaixe. Repita.",
            lifetimeDescription = "Deite-se de brucos, palmas logo fora dos ombros, bracos dobrados. Mantendo o CORPO INTEIRO reto (sem joelhos no chao), levante o corpo ate que os bracos estejam esticados. Abaixe. Repita."
        ),
        ExerciseDefinition(
            index = 4,
            name = "Corrida e Salto",
            introductoryDescription = "Corra no lugar em ritmo acelerado, levantando as pernas 10-15 cm do chao. Conte um passo apenas quando o PE ESQUERDO tocar o chao. A cada 75 passos, pare e faca 7 polichinelos introdutorios: fique com as pernas juntas, bracos ao lado; salte estendendo as pernas para o lado e os bracos para fora ate a altura dos ombros; salte de volta.",
            lifetimeDescription = "Corra no lugar em ritmo acelerado, levantando as pernas 10-15 cm do chao. A cada 75 passos, pare e faca 10 polichinelos completos: mesmo movimento, mas com extensao total."
        )
    )

    val introductoryLadder = listOf(
        RungData(1, 2, 3, 4, 2, 1, 30),
        RungData(2, 3, 4, 5, 3, 1, 65),
        RungData(3, 4, 6, 6, 3, 2, 20),
        RungData(4, 6, 7, 8, 4, 2, 50),
        RungData(5, 7, 9, 9, 5, 3, 0),
        RungData(6, 8, 10, 10, 6, 3, 30),
        RungData(7, 10, 11, 12, 7, 3, 55),
        RungData(8, 12, 13, 14, 8, 4, 5),
        RungData(9, 14, 15, 16, 9, 4, 25),
        RungData(10, 16, 16, 18, 11, 4, 50),
        RungData(11, 18, 18, 20, 12, 4, 70),
        RungData(12, 20, 20, 22, 13, 5, 15),
        RungData(13, 23, 21, 25, 15, 5, 30),
        RungData(14, 25, 23, 27, 16, 5, 50),
        RungData(15, 28, 25, 30, 18, 5, 65)
    )

    val lifetimeLadder = listOf(
        RungData(16, 14, 10, 12, 9, 4, 40),
        RungData(17, 15, 11, 14, 10, 4, 55),
        RungData(18, 16, 12, 16, 11, 5, 0),
        RungData(19, 18, 13, 17, 12, 5, 15),
        RungData(20, 19, 14, 19, 13, 5, 30),
        RungData(21, 21, 15, 21, 14, 5, 45),
        RungData(22, 22, 16, 23, 15, 5, 60),
        RungData(23, 24, 17, 25, 16, 5, 70),
        RungData(24, 25, 18, 27, 17, 6, 10),
        RungData(25, 27, 20, 29, 18, 6, 20),
        RungData(26, 29, 21, 31, 19, 6, 30),
        RungData(27, 31, 23, 33, 20, 6, 40),
        RungData(28, 33, 24, 36, 21, 6, 50),
        RungData(29, 34, 26, 38, 22, 6, 60),
        RungData(30, 36, 28, 40, 23, 6, 65),
        RungData(31, 38, 29, 43, 24, 7, 0),
        RungData(32, 40, 31, 45, 25, 7, 5),
        RungData(33, 43, 33, 48, 26, 7, 10),
        RungData(34, 45, 35, 51, 27, 7, 15),
        RungData(35, 47, 37, 54, 28, 7, 15),
        RungData(36, 49, 39, 56, 29, 7, 20),
        RungData(37, 51, 41, 59, 30, 7, 20),
        RungData(38, 54, 43, 62, 31, 7, 20),
        RungData(39, 56, 46, 65, 32, 7, 25),
        RungData(40, 59, 48, 68, 33, 7, 30),
        RungData(41, 61, 50, 72, 34, 7, 30),
        RungData(42, 64, 53, 75, 35, 7, 30),
        RungData(43, 66, 55, 78, 36, 7, 35),
        RungData(44, 69, 58, 81, 37, 7, 35),
        RungData(45, 72, 61, 85, 38, 7, 35),
        RungData(46, 74, 64, 88, 39, 7, 50),
        RungData(47, 77, 66, 92, 40, 7, 50),
        RungData(48, 80, 69, 96, 41, 7, 50)
    )

    fun getRung(rungNumber: Int): RungData {
        val safe = rungNumber.coerceIn(1, 48)
        return if (safe <= 15) {
            introductoryLadder[safe - 1]
        } else {
            lifetimeLadder[safe - 16]
        }
    }

    fun getPhase(rungNumber: Int): String {
        return if (rungNumber <= 15) "introductory" else "lifetime"
    }

    fun isIntroductory(rungNumber: Int): Boolean = rungNumber <= 15

    fun getJumpingJacksPerSet(rungNumber: Int): Int {
        return if (isIntroductory(rungNumber)) 7 else 10
    }
}
