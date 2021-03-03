package flashcards

import java.util.Scanner

fun main() {
    println("Input (a term, then a definition, and, finally, an answer):\n")
    val card = input()
    val definition = input()
    val answer = input()

    if (definition == answer)
        println("Your answer is right!")
    else
        println("Your nswer is wrong...")
}
fun input(prompt: String = ""): String? {
    val scanner = Scanner(System.`in`)

    print(prompt)
    return scanner.nextLine()
}
