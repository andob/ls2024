package com.cygns.logik

class LogikEvaluationException(message: String) : Exception(message)
class LogikCompileException(message: String) : Exception(message)

enum class TokenType(
    val regex: Regex,
    val category: TokenCategory,
    val toNode: TokenType.(value: String, arguments: Array<out Node>) -> Node = { _, _ ->
        throw LogikCompileException(
            "TokenType $this has no node equivalent"
        )
    },
    val precedence: OperatorPrecedence = OperatorPrecedence.TOKEN_NOT_OPERATOR
) {
    VARIABLE(
        Regex("""\b\w\b"""),
        TokenCategory.VARIABLE,
        { value, _ -> Variable(Token(this, value)) }
    ),
    NOT(
        Regex("""(¬)|(!)|(¬)|(\bl?not\b)|(\\l?not\b)"""),
        TokenCategory.OP_UNARY_RIGHT,
        { value, args -> Not(Token(this, value), args[0]) },
        OperatorPrecedence.HIGHEST
    ),
    POSSIBLE(
        Regex("""(◇)|(\bl?possible\b)|(\\l?possible\b)"""),
        TokenCategory.OP_UNARY_RIGHT,
        { value, args -> Possible(Token(this, value), args[0]) },
        OperatorPrecedence.HIGHEST
    ),
    NECESSARY(
        Regex("""(□)|(\bl?necessary\b)|(\\l?necessary\b)"""),
        TokenCategory.OP_UNARY_RIGHT,
        { value, args -> Necessary(Token(this, value), args[0]) },
        OperatorPrecedence.HIGHEST
    ),
    AND(
        Regex("""(∧)|(&&)|(&)|(\bl?and\b)|(\\l?and\b)"""),
        TokenCategory.OP_BINARY_INFIX,
        { value, args -> And(Token(this, value), args[0], args[1]) },
        OperatorPrecedence.HIGH
    ),
    OR(
        Regex("""(∨)|(\|\|)|(\\l?or\b)|(\bl?or\b)"""),
        TokenCategory.OP_BINARY_INFIX,
        { value, args -> Or(Token(this, value), args[0], args[1]) },
        OperatorPrecedence.HIGH
    ),
//    XOR(
//        Regex("""(⊕)|(\bl?xor\b)|(\\l?xor\b)|(\\oplus)"""),
//        TokenCategory.OP_BINARY_INFIX,
//        { value, args -> ExclusiveOr(Token(this, value), args[0], args[1]) },
//        OperatorPrecedence.HIGH
//    ),
//    NAND(
//        Regex("""(\bsh\b)|(\bl?nand\b)|(\\l?nand\n)|(\|)"""),
//        TokenCategory.OP_BINARY_INFIX,
//        { value, args -> Nand(Token(this, value), args[0], args[1]) },
//        OperatorPrecedence.HIGH
//    ),
    IMPLIES(
        Regex("""(=?⇒)|(⊃)|(\bimplies\b)|(\\implies\b)"""),
        TokenCategory.OP_BINARY_INFIX,
        { value, args -> Implies(Token(this, value), args[0], args[1]) },
        OperatorPrecedence.MEDIUM
    ),
    IFF(
        Regex("""(⇔)|(≡)|(\bl?iff\b)|(\\l?iff\b)"""),
        TokenCategory.OP_BINARY_INFIX,
        { value, args -> IfAndOnlyIf(Token(this, value), args[0], args[1]) },
        OperatorPrecedence.LOW
    ),
    OPEN_PAREN(
        Regex(Regex.escape(Logik.subExpressionHighlightChar.toString()) + """?\("""),
        TokenCategory.GROUPING
    ),
    CLOSE_PAREN(
        Regex("""\)"""),
        TokenCategory.GROUPING
    ),
//    BOOLEAN(
//        Regex("""(\btrue\b)|(\bfalse\b)|(\b([tTfF01yYnN])\b)"""),
//        TokenCategory.LITERAL,
//        { value, _ -> Literal(Token(this, value)) }
//    )
}

enum class TokenCategory {
    GROUPING,
    LITERAL,
    VARIABLE,
    OP_UNARY_LEFT,
    OP_UNARY_RIGHT,
    OP_BINARY_INFIX,

    // no support for postfix because it requires a stack
    OP_BINARY_PREFIX
}

enum class OperatorPrecedence {
    TOKEN_NOT_OPERATOR,
    LOWEST,
    LOW,
    MEDIUM,
    HIGH,
    HIGHEST;

    operator fun plus(i: Int) = values()[ordinal + i]
}

data class Token(val type: TokenType, val value: String) {
    fun toNode(vararg args: Node) = type.toNode(type, value, args)
}