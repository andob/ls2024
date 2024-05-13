package com.cygns.logik

/**
 * The Logik singleton object. To parse an expression, use [parse]
 * This object also has various settings that modify the way Logik works. For example, [trueText] and [falseText] allow you to
 * modify what Logik prints when it encounters the boolean literals `true` or `false` (e.g. when printing our a truth table),
 * and [defaultOnlyShowTrueExpressions], [defaultShowSubExpressions] govern default settings for what [TruthTable]s show
 * when printed out or converted to LaTeX.
 */
object Logik {

    /**
     * The text displayed when an expression evaluates to true in a [TruthTable]. This applies to both their string representation
     * and their LaTeX table representation.
     */
    val trueText = "true"

    /**
     * The text displayed when an expression evaluates to false in a [TruthTable]. This applies to both their string representation
     * and their LaTeX table representation.
     */
    val falseText = "false"

    /**
     * The character that signifies to the compiler that a sub expression should be included in a truth table generation.
     * This is particularly useful for seeing behavior of large expressions, because you can highlight each of the individual
     * parts and see how they behave separately. This should be used in conjunction with parenthesis, e.g. (if the character were a '*'):
     *
     *
     *     (*((x and y) or (z and w)) xor ((x and z) or (y and w)))
     *
     *
     * That will separate the statement enclosed by the parenthesis immediately following the '*' and all truth tables will
     * show it along with its value right next to the main statement.
     */
    val subExpressionHighlightChar: Char = '*'

    /**
     * By default, whether or not to show sub expressions (expressions highlighted by the [subExpressionHighlightChar]).
     */
    val defaultShowSubExpressions = false

    /**
     * By default, whether or not to only include `true` results when printing a [TruthTable] or converting it to a LaTeX table.
     */
    val defaultOnlyShowTrueExpressions = false

    fun Boolean.logikString() = if (this) trueText else falseText

    /**
     * Parses some [text] as a logical expression and returns it as a [LogikStatement].
     *
     * A list of accepted operators/other symbols and their aliases as follows:
     *
     *
     * * variables  - any one character (as defined by a Regex match with \w)
     * * not        - ¬, !, not, lnot, \not, \lnot
     * * and        - ∧, &&, &, and, land, \and, \land
     * * or         - ∨, ||, or, lor, \or, \lor (NOTE: '|' is nand, not or)
     * * xor        - ⊕, xor, lxor, \xor, \lxor, \oplus
     * * nand       - |, sh, nand, lnand, \nand, \lnand
     * * implies    - ⇒, =⇒, implies, \implies
     * * iff        - ⇔, iff, liff, \iff, \liff
     */
    fun parse(text: String) = LogikStatement(text)
}