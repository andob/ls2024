package com.cygns.logik

/**
 * This class stores the boolean values of all [Variable]s for a [statement]. When a [statement] is evaluated, it creates
 * (or accepts an already existing) [VariableAssignment] and the result of the evaluation is based on that context.
 * [getValue] and [setValue] methods provide get/set access to the variables this is keeping track of.
 */
class VariableAssignment(val statement: LogikStatement, private val values: MutableMap<Variable, Boolean>) {

    /**
     * Constructs a new [VariableAssignment] for the [statement] with all [Variable]s set to true
     */
    constructor(statement: LogikStatement) : this(
        statement,
        statement.variables.map { it to true }.toMap().toMutableMap()
    )

    /**
     * Constructs a new [VariableAssignment] for the [statement], mapping default values of each variable to the boolean value
     * of its corresponding [Pair] inside of [pairs]. If no default value is specified, it defaults to true.
     */
    constructor(statement: LogikStatement, pairs: Collection<Pair<String, Boolean>>) : this(statement) {
        for ((key, value) in pairs) {
            val prep = statement.variables.firstOrNull { it.token.value == key }
                ?: throw LogikEvaluationException("Variable $key is not defined for statement $statement")
            values[prep] = value
        }
    }

    /**
     * Constructs a new [VariableAssignment] as a copy of [other]
     */
    constructor(other: VariableAssignment) : this(
        other.statement,
        other.values.map { Pair(it.key, it.value) }.toMap().toMutableMap()
    )

    /**
     * @return the value of a [Variable] in this [VariableAssignment]
     * @throw [LogikEvaluationException] if there is no variable [prep] defined for this [statement]
     */
    fun getValue(prep: Variable) =
        values[prep] ?: throw LogikEvaluationException("Variable $prep is not defined for statement $statement")

    /**
     * Sets the value of a [Variable] in this [VariableAssignment]
     * @throw [LogikEvaluationException] if there is no variable [prep] defined for this [statement]
     */
    fun setValue(prep: Variable, value: Boolean) {
        if (prep !in statement.variables) {
            throw LogikEvaluationException("Variable $prep is not defined for statement $statement")
        }
        values[prep] = value
    }

    override fun toString(): String {
        return values.entries.joinToString(separator = " | ") { it.key.token.value + " = " + it.value.toString() + if (it.value) " " else "" }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VariableAssignment) return false

        val theseEntries = values.entries.toList()
        val otherEntries = other.values.entries.toList()

        if (otherEntries.size != theseEntries.size) {
            return false
        }

        for (i in theseEntries.indices) {
            val thisEntry = theseEntries[i]
            val otherEntry = otherEntries[i]
            if (thisEntry.key != otherEntry.key) {
                return false
            }
            if (thisEntry.value != otherEntry.value) {
                return false
            }
        }

        return true
    }

    override fun hashCode(): Int {
        return values.hashCode()
    }
}

/**
 * A class which holds a compiled form of a logical expression. To evaluate the expression,
 * use [evaluate].
 * Access to the [Variable]s inside this expression is through [variables]
 */
class LogikStatement internal constructor(val text: String) {

    /**
     * A list of all [Variable]s defined in this expression
     */
    val variables = mutableListOf<Variable>()

    internal val subExpressions = mutableListOf<Node>()

    private val tokens: Array<Token>
    private var currentIndex = 0

    private var currentToken: Token
    internal val baseNode: Node

    init {
        fun getToken(word: String): List<Token> {
            if(word == "") {
                return emptyList()
            }
            val tokens = mutableListOf<Token>()
            val types = TokenType.values()
            val fullMatch = types.firstOrNull { it.regex matches word }
            if(fullMatch == null) {
                types.forEach {
                    val matchResult = it.regex.find(word)
                    if(matchResult != null && matchResult.range.start == 0) {
                        tokens.add(Token(it, matchResult.value))
                        tokens.addAll(getToken(word.removeRange(matchResult.range)))
                        return tokens
                    }
                }
                throw LogikCompileException("Unknown token $word")
            } else {
                return listOf(Token(fullMatch, word))
            }
        }

        val tokenList = mutableListOf<Token>()
        // parenthesis
        var editedText = text.replace("(", "( ").replace(")", " )")
        // some syntax sugar
        editedText = editedText.replace("!", " ! ")
        // convert text into tokens w/ regex
        val words = editedText.split(" ")
            .mapNotNull { if (it.all { char -> char == ' ' }) null else it.trim() } //split by spaces, don't include blank words, trim off spaces


        for (word in words) {
            val matchingTokens = getToken(word)
            tokenList.addAll(matchingTokens)
        }
        tokens = tokenList.toTypedArray()
        currentToken = tokens[0]
        baseNode = compile()
        variables.sortBy { it.token.value }
    }

    /**
     * Evaluates this [LogikStatement] for some [assignment], defaulting to a [VariableAssignment] in which each variable is true.
     * @return the truth value of the logical expression of this statement
     * when the atomic prepositions have the truth values defined in the given [assignment].
     */
    fun evaluate(assignment: VariableAssignment = VariableAssignment(this)) = baseNode.visit(assignment)

    /**
     * Evaluates this [LogikStatement] with variables set to the value assigned to them in the map. The key of the map should
     * be the name of the variable and the value of the map should be what you want the truth value of the variable to be.
     * @return the truth value of the logical expression of this statement
     * when the atomic prepositions have the truth values defined in the given [map], or true if they are not defined in the [map]
     */
    fun evaluate(map: Map<String, Boolean>): Boolean =
        evaluate(VariableAssignment(this, map.map { Pair(it.key, it.value) }))

    /**
     * Evaluates this [LogikStatement] with a list of (variable name, variable value) pairs. The first element of the [Pair] should
     * be the name of the variable and the second element of the pair should be what you want the truth value of the variable to be.
     * @return the truth value of the logical expression of this statement
     * when the atomic prepositions have the truth values defined in the given [list], or true if they are not defined in the [list]
     */
    fun evaluate(vararg list: Pair<String, Boolean>) = evaluate(list.toMap())

    fun addRedundantVariables(vararg names: String) {
        for (name in names) {
            if (variables.none { it.token.value == name }) {
                variables.add(Variable(Token(TokenType.VARIABLE, name)))
            }
        }
        variables.sortBy { it.token.value }
    }

    private fun compile() = nextExpression(OperatorPrecedence.LOWEST)

    private fun eat(requiredType: TokenType) {
        if (currentToken.type == requiredType) {
            if (currentIndex == tokens.lastIndex) {
                return
            }
            currentIndex += 1
            currentToken = tokens[currentIndex]
        } else {
            throw LogikCompileException("Required $requiredType, but it was ${currentToken.type} at word index $currentIndex")
        }
    }

    private fun nextExpression(precedence: OperatorPrecedence): Node {
        // the recursion here ensures that the first thing that gets called is factor, and then under it, in lowering priority,
        // calls to expression
        var node = if (precedence == OperatorPrecedence.HIGHEST) {
            nextFactor()
        } else {
            nextExpression(precedence + 1)
        }
        // then go along the tokens until we have eaten all the ones of the current precedence
        while (currentToken.type.precedence == precedence) {
            val token = currentToken
            if (token.type.category == TokenCategory.OP_BINARY_INFIX) {
                eat(token.type)
                // infix means the (now previous) node is the first arg, next node is the next arg
                val leftArg = node
                val rightArg =
                    if (precedence == OperatorPrecedence.HIGHEST) nextFactor() else nextExpression(precedence + 1)
                node = token.toNode(leftArg, rightArg)
            } else if (token.type.category == TokenCategory.OP_UNARY_LEFT) {
                eat(token.type)
                val arg = node
                node = token.toNode(arg)
            }
        }
        return node
    }

    private fun nextFactor(): Node {
        // a factor is an node that could begin an expression
        val token = currentToken
        if (token.type.category == TokenCategory.LITERAL) {
            eat(token.type)
            return Literal(token)
        } else if (token.type.category == TokenCategory.VARIABLE) {
            eat(token.type)
            val previouslyExistingVariable = variables.firstOrNull { it.token.value == token.value }
            if (previouslyExistingVariable != null) {
                return previouslyExistingVariable
            }
            val prep = Variable(token)
            variables.add(prep)
            return prep
        } else if (token.type.category == TokenCategory.OP_UNARY_RIGHT) {
            if (currentIndex != tokens.lastIndex) {
                eat(token.type)
                return token.toNode(
                    if (token.type.precedence == OperatorPrecedence.HIGHEST)
                        nextFactor() else
                        nextExpression(token.type.precedence + 1)
                )
            } else {
                throw LogikCompileException("Expected an expression at word index $currentIndex, but the text ended")
            }
        } else if (token.type.category == TokenCategory.OP_BINARY_PREFIX) {
            if (currentIndex != tokens.lastIndex) {
                eat(token.type)
                return token.toNode(
                    if (token.type.precedence == OperatorPrecedence.HIGHEST)
                        nextFactor() else
                        nextExpression(token.type.precedence + 1),
                    if (token.type.precedence == OperatorPrecedence.HIGHEST)
                        nextFactor() else
                        nextExpression(token.type.precedence + 1)
                )
            }
        } else if (token.type == TokenType.OPEN_PAREN) {
            if (currentIndex != tokens.lastIndex) {
                eat(TokenType.OPEN_PAREN)
                val node = nextExpression(OperatorPrecedence.LOWEST)
                if (token.value.contains(Logik.subExpressionHighlightChar)) {
                    // if it has been highlighted as a significant sub expr
                    subExpressions.add(node)
                }
                eat(TokenType.CLOSE_PAREN)
                return node
            } else {
                throw LogikCompileException("Expected an expression at word index $currentIndex, but the text ended")
            }
        }
        throw LogikCompileException("Incorrectly placed token $currentToken at word index $currentIndex")
    }
}