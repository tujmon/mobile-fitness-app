package com.hackerfit.domain.constants

data class ExerciseDefinition(
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
            name = "Flex\u00e3o para Frente",
            introductoryDescription = "Fique em p\u00e9 com as pernas afastadas, m\u00e3os estendidas acima da cabe\u00e7a. Dobre para frente o m\u00e1ximo que puder, tentando tocar os dedos dos p\u00e9s. Endireite-se e dobre para tr\u00e1s moderadamente. Repita.",
            lifetimeDescription = "Fique em p\u00e9 com as pernas afastadas, m\u00e3os estendidas acima da cabe\u00e7a. Dobre para frente e toque o ch\u00e3o entre as pernas, salte alguns cent\u00edmetros para cima e toque o ch\u00e3o novamente. Endireite-se e dobre para tr\u00e1s. Repita."
        ),
        ExerciseDefinition(
            name = "Abdominal",
            introductoryDescription = "Deite-se de costas, p\u00e9s ligeiramente afastados, m\u00e3os ao lado do corpo. Levante a cabe\u00e7a e os ombros do ch\u00e3o o suficiente para ver os calcanhares. Abaixe suavemente. Repita.",
            lifetimeDescription = "Deite-se de costas, p\u00e9s ligeiramente afastados, m\u00e3os ao lado do corpo. Levante o corpo superior, dobrando na cintura, at\u00e9 sentar-se verticalmente. Mantenha os bra\u00e7os ao lado e os p\u00e9s no ch\u00e3o. Abaixe suavemente. Repita."
        ),
        ExerciseDefinition(
            name = "Eleva\u00e7\u00e3o de Pernas",
            introductoryDescription = "Deite-se de bru\u00e7os, pernas ligeiramente afastadas, palmas sob as coxas. Levante a perna esquerda (dobrando no quadril e joelho) enquanto levanta a cabe\u00e7a. Abaixe. Depois levante a perna direita e a cabe\u00e7a da mesma forma. Cada repeti\u00e7\u00e3o conta ambas as pernas.",
            lifetimeDescription = "Deite-se de bru\u00e7os, pernas ligeiramente afastadas, palmas sob as coxas. Levante AMBAS as pernas pelo menos alto o suficiente para que as coxas saiam das m\u00e3os. Simultaneamente levante a cabe\u00e7a e os ombros. Abaixe tudo suavemente. Repita."
        ),
        ExerciseDefinition(
            name = "Flex\u00e3o de Bra\u00e7o",
            introductoryDescription = "Deite-se de bru\u00e7os, palmas logo fora dos ombros, bra\u00e7os dobrados. Mantendo os JOELHOS no ch\u00e3o e permitindo que as pernas dobrem nos joelhos, levante o corpo superior at\u00e9 que os bra\u00e7os estejam esticados. Abaixe. Repita.",
            lifetimeDescription = "Deite-se de bru\u00e7os, palmas logo fora dos ombros, bra\u00e7os dobrados. Mantendo o CORPO INTEIRO reto (sem joelhos no ch\u00e3o), levante o corpo at\u00e9 que os bra\u00e7os estejam esticados. Abaixe. Repita."
        ),
        ExerciseDefinition(
            name = "Corrida e Salto",
            introductoryDescription = "Corra no lugar em ritmo acelerado, levantando as pernas 10\u201315 cm do ch\u00e3o. Conte um passo apenas quando o P\u00c9 ESQUERDO tocar o ch\u00e3o. A cada 75 passos, pare e fa\u00e7a 7 polichinelos introdut\u00f3rios: fique com as pernas juntas, bra\u00e7os ao lado; salte estendendo as pernas para o lado e os bra\u00e7os para fora at\u00e9 a altura dos ombros; salte de volta.",
            lifetimeDescription = "Corra no lugar em ritmo acelerado, levantando as pernas 10\u201315 cm do ch\u00e3o. A cada 75 passos, pare e fa\u00e7a 10 polichinelos completos: mesmo movimento, mas com extens\u00e3o total."
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
        return if (rungNumber <= 15) {
            introductoryLadder[rungNumber - 1]
        } else {
            lifetimeLadder[rungNumber - 16]
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
