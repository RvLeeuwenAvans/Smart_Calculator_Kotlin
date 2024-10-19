package calculator

import java.math.BigInteger
import java.util.*


object Calculator {
    private var storedVariables = mutableMapOf<String, BigInteger>()

    object Operation {
        val add: (BigInteger, BigInteger) -> BigInteger = { a, b -> a + b }
        val subtract: (BigInteger, BigInteger) -> BigInteger = { a, b -> a - b }
        val divide: (BigInteger, BigInteger) -> BigInteger = { a, b -> a / b }
        val multiply: (BigInteger, BigInteger) -> BigInteger = { a, b -> a * b }
    }

    fun processInput(inputLine: String) {
        try {
            val sanitizedInput = sanitizeInput(inputLine)

            when (true) {
                Regex("=").containsMatchIn(sanitizedInput) -> assignVariable(sanitizedInput)
                Regex("^\\s*([a-zA-z]+)$").containsMatchIn(sanitizedInput)
                    -> println(storedVariables[sanitizedInput] ?: "Unknown variable")

                else -> if (inputLine.isNotEmpty()) println(
                    evaluatePostfix(
                        PostFixConverter.infixToPostfix(
                            sanitizedInput
                        )
                    )
                )
            }
        } catch (e: Exception) {
            println(e.message)
        }
    }

    private fun assignVariable(line: String) {
        if (!Regex("^\\s*(?<name>[a-zA-z]+)\\s*?=").containsMatchIn(line))
            return println("Invalid identifier")

        val (name, value) = Regex("^\\s*(?<name>[a-zA-z]+)\\s*?=\\s*(?<value>-?\\d+|[a-zA-z]+)\$")
            .find(line)?.destructured ?: return println("Invalid assignment")
        this.storedVariables[name] = getIntValue(value) ?: return println("Unknown variable")
    }

    private fun getIntValue(input: String) = input.toBigIntegerOrNull() ?: storedVariables[input]

    private fun getOperation(operator: String) = when (operator.first()) {
        '-' -> Operation.subtract
        '+' -> Operation.add
        '/' -> Operation.divide
        '*' -> Operation.multiply
        '^' -> Operation.multiply
        else -> throw InvalidExpressionException()
    }

    private fun evaluatePostfix(postfixResult: List<String>): BigInteger {
        val inputStack = ArrayDeque(postfixResult)
        val outputStack = ArrayDeque<String>()

        inputStack.forEach { element ->
            // If the scanned character is an operand (number here), push it to the stack.
            if (getIntValue(element) != null) {
                outputStack.push(element)
            } else {
                val value2 = getIntValue(outputStack.pop()) ?: throw IllegalArgumentException("Unable to parse value")
                val value1 = getIntValue(outputStack.pop()) ?: throw IllegalArgumentException("Unable to parse value")

                outputStack.push(getOperation(element)(value1, value2).toString())
            }
        }
        return outputStack.pop().toBigInteger()
    }

    private fun sanitizeInput(line: String): String {
        if (line.contains("**") || line.contains("//")) throw InvalidExpressionException()

        var sanitizedInput = line.replace("\\+{2,}".toRegex(), "+")
        sanitizedInput = "-{2,}".toRegex().replace(sanitizedInput) { matchResult ->
            if (matchResult.value.length % 2 == 0) "+" else "-"
        }

        return sanitizedInput.trim()
    }
}

private object PostFixConverter {
    fun infixToPostfix(s: String) = run {
        try {
            val st = ArrayDeque<Char>()
            val result = StringBuilder()

            s.forEach { element ->
                when (element) {
                    // If the scanned character is an operand, add it to output.
                    in 'a'..'z', in 'A'..'Z', in '0'..'9', ' ' -> result.append("$element")
                    // direct parentheses and add operands, operator to the result, stack respectively
                    '(' -> st.push('(')
                    ')' -> {
                        while (st.peek() != '(') {
                            result.append(" ${st.pop()}")
                        }
                        st.pop()
                    }

                    else -> {
                        while (st.isNotEmpty() && isPredecessorEqualOrHigher(element, st)) {
                            result.append(" ${st.pop()}")
                        }
                        st.push(element)
                    }
                }
            }
            // Pop all the remaining elements from the stack
            while (!st.isEmpty()) {
                result.append(" ${st.pop()}")
            }

            result.toString()
                .split(" ")
                .filter { it.isNotEmpty() }
        } catch (e: NoSuchElementException) {
            throw InvalidExpressionException()
        }
    }

    // Function to return precedence of operators
    private fun getOperatorPrecedence(c: Char): Int {
        return if (c == '^') 3
        else if (c == '/' || c == '*') 2
        else if (c == '+' || c == '-') 1
        else -1
    }

    private fun isPredecessorEqualOrHigher(operator: Char, operatorStack: ArrayDeque<Char>): Boolean {
        val operatorPrecedence = getOperatorPrecedence(operator)
        val precedingOperatorPrecedence = getOperatorPrecedence(operatorStack.peek())
        return operatorPrecedence < precedingOperatorPrecedence || operatorPrecedence == precedingOperatorPrecedence
    }
}

