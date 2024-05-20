package com.cygns.logik

sealed class Node(val token: Token) {
    abstract fun visit(assignment: VariableAssignment): Boolean

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Node) return false
        if (token != other.token) return false
        return true
    }

    override fun hashCode(): Int {
        return token.hashCode()
    }

    /**
     * Converts a [Node] into a representation that can be parsed by the LaTeX compiler
     */
    abstract fun toLaTeX(): String
}

class Variable(token: Token) : Node(token) {
    override fun visit(assignment: VariableAssignment): Boolean {
        return assignment.getValue(this)
    }

    override fun toLaTeX() = toString()

    override fun toString(): String {
        return token.value
    }
}

sealed class UnaryOperator(token: Token, val arg: Node) : Node(token) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UnaryOperator) return false
        if (!super.equals(other)) return false
        if (arg != other.arg) return false
        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + arg.hashCode()
        return result
    }

    override fun toString(): String {
        return "(${token.value} $arg)"
    }
}

class Not(token: Token, arg: Node) : UnaryOperator(token, arg) {
    override fun visit(assignment: VariableAssignment): Boolean {
        return !arg.visit(assignment)
    }

    override fun toLaTeX(): String {
        return "\\lnot ${arg.toLaTeX()}"
    }
}

class Possible(token: Token, arg: Node) : UnaryOperator(token, arg) {
    override fun visit(assignment: VariableAssignment): Boolean {
        return !arg.visit(assignment)
    }

    override fun toLaTeX(): String {
        return "\\lpossible ${arg.toLaTeX()}"
    }
}

class Necessary(token: Token, arg: Node) : UnaryOperator(token, arg) {
    override fun visit(assignment: VariableAssignment): Boolean {
        return !arg.visit(assignment)
    }

    override fun toLaTeX(): String {
        return "\\lnecessary ${arg.toLaTeX()}"
    }
}

class PossibleInFuture(token: Token, arg: Node) : UnaryOperator(token, arg) {
    override fun visit(assignment: VariableAssignment): Boolean {
        return !arg.visit(assignment)
    }

    override fun toLaTeX(): String {
        return "\\lpossible_in_future ${arg.toLaTeX()}"
    }
}

class NecessaryInFuture(token: Token, arg: Node) : UnaryOperator(token, arg) {
    override fun visit(assignment: VariableAssignment): Boolean {
        return !arg.visit(assignment)
    }

    override fun toLaTeX(): String {
        return "\\lnecessary_in_future ${arg.toLaTeX()}"
    }
}

class PossibleInPast(token: Token, arg: Node) : UnaryOperator(token, arg) {
    override fun visit(assignment: VariableAssignment): Boolean {
        return !arg.visit(assignment)
    }

    override fun toLaTeX(): String {
        return "\\lpossible_in_past ${arg.toLaTeX()}"
    }
}

class NecessaryInPast(token: Token, arg: Node) : UnaryOperator(token, arg) {
    override fun visit(assignment: VariableAssignment): Boolean {
        return !arg.visit(assignment)
    }

    override fun toLaTeX(): String {
        return "\\lnecessary_in_past ${arg.toLaTeX()}"
    }
}

class Literal(token: Token) : Node(token) {
    override fun visit(assignment: VariableAssignment): Boolean {
        return token.value.toBoolean()
    }

    override fun toString(): String {
        return token.value
    }

    override fun toLaTeX() = toString()
}

sealed class BinaryOperator(token: Token, val left: Node, val right: Node) : Node(token) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BinaryOperator) return false
        if (!super.equals(other)) return false
        if (left != other.left) return false
        if (right != other.right) return false
        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + left.hashCode()
        result = 31 * result + right.hashCode()
        return result
    }

    override fun toString(): String {
        return "(${token.value} $left $right)"
    }

}

class Implies(token: Token, left: Node, right: Node) : BinaryOperator(token, left, right) {
    override fun visit(assignment: VariableAssignment): Boolean {
        val leftValue = left.visit(assignment)
        val rightValue = right.visit(assignment)
        return !leftValue || (leftValue && rightValue)
    }

    override fun toLaTeX(): String {
        return "(${left.toLaTeX()} \\implies ${right.toLaTeX()})"
    }
}

class IfAndOnlyIf(token: Token, left: Node, right: Node) : BinaryOperator(token, left, right) {
    override fun visit(assignment: VariableAssignment): Boolean {
        val leftValue = left.visit(assignment)
        val rightValue = right.visit(assignment)
        return leftValue == rightValue
    }

    override fun toLaTeX(): String {
        return "(${left.toLaTeX()} \\iff ${right.toLaTeX()})"
    }
}

class ExclusiveOr(token: Token, left: Node, right: Node) : BinaryOperator(token, left, right) {
    override fun visit(assignment: VariableAssignment): Boolean {
        val leftValue = left.visit(assignment)
        val rightValue = right.visit(assignment)
        return leftValue != rightValue
    }

    override fun toLaTeX(): String {
        return "(${left.toLaTeX()} \\oplus ${right.toLaTeX()})"
    }
}

class Or(token: Token, left: Node, right: Node) : BinaryOperator(token, left, right) {
    override fun visit(assignment: VariableAssignment): Boolean {
        val leftValue = left.visit(assignment)
        val rightValue = right.visit(assignment)
        return leftValue || rightValue
    }

    override fun toLaTeX(): String {
        return "(${left.toLaTeX()} \\lor ${right.toLaTeX()})"
    }
}

class And(token: Token, left: Node, right: Node) : BinaryOperator(token, left, right) {
    override fun visit(assignment: VariableAssignment): Boolean {
        val leftValue = left.visit(assignment)
        val rightValue = right.visit(assignment)
        return leftValue && rightValue
    }

    override fun toLaTeX(): String {
        return "(${left.toLaTeX()} \\land ${right.toLaTeX()})"
    }
}

class Nand(token: Token, left: Node, right: Node) : BinaryOperator(token, left, right) {
    override fun visit(assignment: VariableAssignment): Boolean {
        val leftValue = left.visit(assignment)
        val rightValue = right.visit(assignment)
        return !(leftValue && rightValue)
    }

    override fun toLaTeX(): String {
        return "(${left.toLaTeX()}) nand (${right.toLaTeX()})"
    }
}