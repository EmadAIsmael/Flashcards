package flashcards

import java.util.Scanner
import flashcards.FlashCards.Action.*
import java.io.File
import java.io.FileNotFoundException
import kotlin.random.Random
import kotlin.system.exitProcess

fun input(prompt: String = ""): String {
    val scanner = Scanner(System.`in`)

    print(prompt)
    return scanner.nextLine()
}

class FlashCards {
    private val data = mutableMapOf<Int, String>()
    private val termToDef = mutableMapOf<Int, Int>()
    private val defToTerm = mutableMapOf<Int, Int>()

    data class Card(val term: Int = 0, val definition: Int = 0)

    enum class Action {
        ADD, REMOVE, IMPORT, EXPORT, ASK, EXIT, NONE;
    }

    private fun readTerm(): String {
        println("The card:")
        var term: String = input()
        if (data.getOrDefault(term.hashCode(), "") != "") {
            println("The card \"${term}\" already exists.")
            term = ""
        }
        return term
    }

    private fun readDefinition(): String {
        var definition = ""
        println("The definition of the card:")
        definition = input()
        if (data.getOrDefault(definition.hashCode(), "") != "") {
            println("The definition \"${definition}\" already exists. Try again:")
            definition = ""
        }
        return definition
    }

    private fun getOneCard(): Card {
        val theTerm = readTerm()
        if (theTerm == "")
            return Card()

        val theDefinition = readDefinition()
        if (theDefinition == "")
            return Card()

        val termHashCode = theTerm.hashCode()
        val defHashCode = theDefinition.hashCode()
        data[termHashCode] = theTerm
        data[defHashCode] = theDefinition
        termToDef[termHashCode] = defHashCode
        defToTerm[defHashCode] = termHashCode

        return Card(termHashCode, defHashCode)
    }

    private fun getDefByTerm(term: Int): Int? = termToDef[term]

    private fun getTermByDef(def: Int): Int? = defToTerm[def]

    private fun oneCardQuiz(card: Card) {

        val termText = data[card.term]
        val defText = data[card.definition]
        val answer = input("Print the definition of \"${termText}\":\n")
        val userDefHashCode = answer.hashCode()

        when {
            userDefHashCode == getDefByTerm(card.term) -> println("Correct!")
            getTermByDef(userDefHashCode) != null -> {
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

    private fun add() {
        val card = getOneCard()
        if (card == Card()) return
        println("The pair (\"${data[card.term]}\":\"${data[card.definition]}\") has been added.")
    }

    private fun remove() {
        val term = input("Which card?\n")
        val termHash = term.hashCode()
        if (termHash in data) {
            val def = termToDef[termHash]
            termToDef.remove(termHash)
            defToTerm.remove(def)
            data.remove(term.hashCode())
            data.remove(def.hashCode())
            println("The card has been removed.")
        } else
            println("Can't remove \"${term}\": there is no such card.")
    }

    private fun import() {
        var count = 0
        try {
            val file = File(input("File name:\n"))
            file.forEachLine {
                if (it.isNotBlank()) {
                    val (term, def) = it.split(", ")
                    termToDef[term.hashCode()] = def.hashCode()
                    defToTerm[def.hashCode()] = term.hashCode()
                    data[term.hashCode()] = term
                    data[def.hashCode()] = def

                    count++
                }
            }
            println("${count} cards have been loaded.")

        } catch (e: FileNotFoundException) {
            println("File not found.")
        }
    }

    private fun export() {
        var count = 0
        val file = File(input("File name:\n"))
        file.writeText("")
        for ((term, def) in termToDef.entries) {
            val termText = data[term]
            val defText = data[def]
            file.appendText("${termText}, ${defText}\n")
            count++
        }
        println("${count} cards have been saved.")
    }

    private fun ask() {
        val times = input("How many times to ask?\n").toInt()
        repeat(times) {
            val entry = termToDef.entries.elementAt(Random.nextInt(termToDef.size))
            val card = Card(entry.key, entry.value)
            oneCardQuiz(card)
        }
    }

    fun menu() {
        var action = NONE
        while (action != EXIT) {
            val c = input("\nInput the action (add, remove, import, export, ask, exit):\n")
            action = valueOf(c.toUpperCase())
            when (action) {
                ADD -> add()
                REMOVE -> remove()
                IMPORT -> import()
                EXPORT -> export()
                ASK -> ask()
                EXIT -> {
                    println("Bye Bye!")
                    exitProcess(0)
                }
                NONE -> {
                }
            }
        }
    }
}

fun main() {
    val flashCards = FlashCards()

    flashCards.menu()
}
