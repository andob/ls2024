package com.deduction

enum class Operation
{
    Non, And, Or, Imply, BiImply;

    override fun toString() : String
    {
        return when(this)
        {
            Non -> "~"
            And -> "^"
            Or -> "v"
            Imply -> "->"
            BiImply -> "<->"
        }
    }
}

interface IFormula

data class Predicate(val name : String) : IFormula
{
    override fun equals(other : Any?) = (other as? Predicate)?.name == name
    override fun hashCode() = name.hashCode()
    override fun toString() = name
}

data class UnaryFormula
(
    val operation : Operation,
    val x : IFormula,
) : IFormula
{
    init
    {
        if (operation != Operation.Non)
            throw RuntimeException("Use only ~ for UnaryFormula!")
    }

    override fun toString() : String
    {
        return if (x is BinaryFormula) "~($x)" else "~$x"
    }
}

data class BinaryFormula
(
    val x : IFormula,
    val operation : Operation,
    val y : IFormula,
) : IFormula
{
    override fun toString() : String
    {
        val xString = if (x is BinaryFormula) "($x)" else "$x"
        val yString = if (y is BinaryFormula) "($y)" else "$y"
        return "$xString $operation $yString"
    }
}
