package flashcards

import java.util.Scanner

fun input(prompt: String = ""): String {
    val scanner = Scanner(System.`in`)

    print(prompt)
    return scanner.nextLine()
}

class FlashCards {
    private var numberOfCards = 0
    private val data = mutableMapOf<Int, String>()
    private val termToDef = mutableMapOf<Int, Int>()
    private val defToTerm = mutableMapOf<Int, Int>()

    data class Card(val term: Int, val definition: Int)

    private val cards = mutableListOf<Card>()

    private fun readTerm(cardNumber: Int): String {
        var term = ""
        var isRepeatedTerm = false
        println("Card #${cardNumber + 1}:")
        while (!isRepeatedTerm) {
            term = input()
            if (data.getOrDefault(term.hashCode(), "") == "") {
                isRepeatedTerm = true
            } else
                println("The term \"${term}\" already exists. Try again: ")
        }
        return term
    }

    private fun readDefinition(cardNumber: Int): String {
        var definition = ""
        var isRepeatedTerm = false
        println("The definition for card #${cardNumber + 1}:")
        while (!isRepeatedTerm) {
            definition = input()
            if (data.getOrDefault(definition.hashCode(), "") == "") {
                isRepeatedTerm = true
            } else
                println("The definition \"${definition}\" already exists. Try again:")
        }
        return definition
    }

    private fun getOneCard(cardNumber: Int) {
        val theTerm = readTerm(cardNumber)
        val theDefinition = readDefinition(cardNumber)

        val termHashCode = theTerm.hashCode()
        val defHashCode = theDefinition.hashCode()
        data[termHashCode] = theTerm
        data[defHashCode] = theDefinition
        termToDef[termHashCode] = defHashCode
        defToTerm[defHashCode] = termHashCode

         cards.add(Card(termHashCode, defHashCode))
    }

    fun getCards() {
        numberOfCards = input("Input the number of cards:\n").toInt()

        repeat(numberOfCards) {
            getOneCard(it)
        }
    }

    fun getDefByTerm(term: Int): Int? {
        return termToDef[term]
    }

    fun getTermByDef(def: Int): Int? {
        return defToTerm[def]
    }

    private fun oneCardQuiz(card: Card) {

        val termText = data[card.term]
        val defText = data[card.definition]
        val answer = input("Print the definition of \"${termText}\":\n")
        val userDefHashCode = answer.hashCode()

        when {
            userDefHashCode == getDefByTerm(card.term) -> println("Correct!")
            getTermByDef(userDefHashCode)  != null -> {
                val correctTerm = data[defToTerm[answer.hashCode()]]
                println(
                    "Wrong. The right answer is \"${defText}\", " +
                            "but your definition is correct for " +
                            "\"${correctTerm}\"."
                )
            }
            else -> println("Wrong. The right answer is \"${data[card.definition]}\".")
        }
    }

    fun allCardsQuiz() {
        for (card in cards)
            oneCardQuiz(card)
    }
}

fun main() {
    val flashCards = FlashCards()
    flashCards.getCards()
    flashCards.allCardsQuiz()
}
