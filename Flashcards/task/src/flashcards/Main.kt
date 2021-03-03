package flashcards

import java.util.Scanner

fun main() {
    val flashCards = FlashCards()
    flashCards.getCards()
    flashCards.allCardsQuiz()
}
fun input(prompt: String = ""): String {
    val scanner = Scanner(System.`in`)

    print(prompt)
    return scanner.nextLine()
}

class FlashCards {
    data class Card(val term: String, val definition: String)
    private var numberOfCards = 0
    private val cards = mutableListOf<Card>()

    private fun getOneCard(cardNumber: Int) {
        val term = input("Card #${cardNumber + 1}:\n")
        val definition = input("The definition for card #${cardNumber + 1}:\n")
        cards.add(Card(term, definition))
    }

    fun getCards() {
        numberOfCards = input("Input the number of cards:\n").toInt()

        repeat(numberOfCards) {
            getOneCard(it)
        }
    }

    private fun oneCardQuiz(card: Card) {
        val answer = input("Print the definition of \"${card.term}\":\n")
        if (answer == card.definition)
            println("Correct!")
        else
            println("Wrong. The right answer is \"${card.definition}\".")
    }

    fun allCardsQuiz() {
        for (card in cards)
            oneCardQuiz(card)
    }
}
