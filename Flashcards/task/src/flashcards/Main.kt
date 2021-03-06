package flashcards

import java.util.Scanner
import flashcards.FlashCards.Action.*
import java.io.File
import java.io.FileNotFoundException
import kotlin.random.Random
import kotlin.system.exitProcess

class IOLogger {
    private val log = mutableListOf<String>()

    fun input(prompt: String = ""): String {
        val scanner = Scanner(System.`in`)

        print(prompt)
        val userInput = scanner.nextLine()
        log.add(userInput + "\n")
        return userInput
    }

    fun print(message: String) {
        kotlin.io.print(message)
        log.add(message)
    }

    fun println(message: String) {
        kotlin.io.println(message)
        log.add(message + "\n")
    }

    fun save() {
        val file = File(input("File name:\n"))
        file.writeText("")
        log.forEach {
            file.appendText(it)
        }
        println("The log has been saved.")
    }
}

class FlashCards(private val importFile: String = "", private val exportFile: String = "") {

    private val log = IOLogger()

    data class Card(
        val term: String = "",
        val definition: String = "",
        var mistakes: Int = 0
    )

    private val termToDef = linkedMapOf<Int, Card>()
    private val defToTerm = linkedMapOf<Int, Card>()

    init {
        if (importFile.isNotBlank()) import()
    }

    enum class Action {
        ADD, REMOVE, IMPORT, EXPORT, ASK, EXIT, LOG, HARDEST, RESET, NONE;
    }

    private fun readTerm(): String {
        log.println("The card:")
        var term: String = log.input()
        if (termToDef.getOrDefault(term.hashCode(), Card()) != Card()) {
            log.println("The card \"${term}\" already exists.")
            term = ""
        }
        return term
    }

    private fun readDefinition(): String {
        var definition: String
        log.println("The definition of the card:")
        definition = log.input()
        if (defToTerm.getOrDefault(definition.hashCode(), Card()) != Card()) {
            log.println("The definition \"${definition}\" already exists. Try again:")
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
        val card = Card(theTerm, theDefinition)
        termToDef[termHashCode] = card
        defToTerm[defHashCode] = card

        return card
    }

    private fun oneCardQuiz(card: Card) {

        val termText = card.term
        val defText = card.definition
        val answer = log.input("Print the definition of \"${termText}\":\n")
        val termHash = termText.hashCode()
        val answerHash = answer.hashCode()

        when {
            // if correct.
            defToTerm[answerHash] == termToDef[termHash] -> log.println("Correct!")
            // if wrong, but correct for another card.
            defToTerm.getOrDefault(answerHash, Card()) != Card() -> {
                val correctTerm = defToTerm[answerHash]?.term
                log.println(
                    "Wrong. The right answer is \"${defText}\", " +
                            "but your definition is correct for " +
                            "\"${correctTerm}\"."
                )
                card.mistakes++
            }
            // if totally wrong.
            else -> {
                log.println("Wrong. The right answer is \"${card.definition}\".")
                card.mistakes++
            }
        }
    }

    private fun add() {
        val card = getOneCard()
        if (card == Card()) return
        log.println("The pair (\"${card.term}\":\"${card.definition}\") has been added.")
    }

    private fun remove() {
        val term = log.input("Which card?\n")
        val termHash = term.hashCode()
        if (termHash in termToDef) {
            val card = termToDef[termHash]
            val tHash = card?.term.hashCode()
            val dHash = card?.definition.hashCode()
            termToDef.remove(tHash)
            defToTerm.remove(dHash)
            log.println("The card has been removed.")
        } else
            log.println("Can't remove \"${term}\": there is no such card.")
    }

    private fun import() {
        var count = 0
        val file: File
        try {
            file = if (importFile.isBlank())
                File(log.input("File name:\n"))
            else
                File(importFile)
            file.forEachLine {
                if (it.isNotBlank()) {
                    val (term, def, mistake) = it.split(":")
                    val card = Card(term, def, mistake.toInt())
                    termToDef[term.hashCode()] = card
                    defToTerm[def.hashCode()] = card

                    count++
                }
            }
            log.println("$count cards have been loaded.")

        } catch (e: FileNotFoundException) {
            log.println("File not found.")
        }
    }

    private fun export() {
        var count = 0
        val file: File = if (exportFile.isBlank())
            File(log.input("File name:\n"))
        else
            File(exportFile)
        file.writeText("")
        for ((_, card) in termToDef.entries) {
            val termText = card.term
            val defText = card.definition
            val nMistakes = card.mistakes
            file.appendText("${termText}:${defText}:${nMistakes}\n")
            count++
        }
        log.println("$count cards have been saved.")
    }

    private fun ask() {
        val times = log.input("How many times to ask?\n").toInt()
        repeat(times) {
            val (_, card) = termToDef.entries.elementAt(Random.nextInt(termToDef.size))
            oneCardQuiz(card)
        }
    }

    private fun saveLog() {
        log.save()
    }

    private fun hardest() {
        val list = termToDef
            .filterValues {
                it.mistakes == termToDef.maxByOrNull { it.value.mistakes }
                    ?.value?.mistakes
            }.filterValues { it.mistakes != 0 }


        if (list.isNotEmpty()) {
            val mistakeCount = list.entries.elementAt(0).value.mistakes
            val terms = mutableListOf<String>()
            list.forEach { terms.add(it.value.term) }


            val listOfTerms = terms.joinToString(separator = ", ", transform = { "\"$it\"" })
            if (list.size == 1) {
                log.println("The hardest card is ${listOfTerms}. You have $mistakeCount errors answering it")
            } else
                log.println("The hardest cards are ${listOfTerms}. You have $mistakeCount errors answering them")
        } else
            log.println("There are no cards with errors.")
    }

    private fun reset() {
        for ((_, card) in termToDef)
            card.mistakes = 0
        log.println("Card statistics have been reset.")
    }

    fun menu() {
        var action = NONE
        while (action != EXIT) {
            val prompt = "\nInput the action (add, remove, import, export, ask, exit, " +
                    "log, hardest card, reset stats):\n"
            var c = log.input(prompt)
            c = when (c) {
                "hardest card" -> "hardest"
                "reset stats" -> "reset"
                else -> c
            }
            try {
                action = valueOf(c.toUpperCase())
                when (action) {
                    ADD -> add()
                    REMOVE -> remove()
                    IMPORT -> import()
                    EXPORT -> export()
                    ASK -> ask()
                    EXIT -> {
                        if (exportFile.isNotBlank()) export()
                        log.println("Bye Bye!")
                        exitProcess(0)
                    }
                    LOG -> saveLog()
                    HARDEST -> hardest()
                    RESET -> reset()
                    NONE -> {
                    }
                }
            } catch (e: Exception) {
            }
        }
    }
}

fun main(args: Array<String>) {
    val parsed = mutableMapOf<String, String>()
    for (i in 0..args.lastIndex step 2) {
        parsed[args[i]] = args[i + 1]
    }

    val importFile = parsed.getOrDefault("-import", "")
    val exportFile = parsed.getOrDefault("-export", "")

    val flashCards = FlashCards(importFile, exportFile)
    flashCards.menu()
}
