package calculator

var running = true

fun main() {
    while (running) {

        val input = readlnOrNull()

        when (true) {
            input.isNullOrEmpty() -> continue
            (input == "/help") ->
                println("The program calculates result of an  expression containing subtraction and/or addition ")

            (input == "/exit") -> return println("Bye!").also { running = false }
            (Regex("^/.").containsMatchIn(input)) -> println("Unknown command")
            else -> Calculator.processInput(input.trimIndent())
        }
    }
}