
enum class Operation(val sign : Char, val isUnary : Boolean)
{
    Non(sign = '~', isUnary = true),
    And(sign = '&', isUnary = false),
    Or(sign = '∨', isUnary = false),
    Imply(sign = '→', isUnary = false),
    BiImply(sign = '↔', isUnary = false),
    Necessary(sign = '□', isUnary = true),
    Possible(sign = '◇', isUnary = true);

    override fun toString() = sign.toString()
}

interface IFormula
{
    val world : PossibleWorld?
}

data class AtomicFormula
(
    val name : String,
    override val world : PossibleWorld? = null,
) : IFormula
{
    override fun equals(other : Any?) = (other as? AtomicFormula)?.name == name
    override fun hashCode() = name.hashCode()
    override fun toString() = name
}

data class ComplexFormula
(
    val x : IFormula,
    val operation : Operation,
    val y : IFormula?,
    override val world : PossibleWorld? = null,
) : IFormula
{
    constructor(operation : Operation, x : IFormula) : this(x, operation, y = null)

    init
    {
        if (operation.isUnary && y != null)
            throw RuntimeException("$operation should use only one argument!")
        if (!operation.isUnary && y == null)
            throw RuntimeException("$operation should use two arguments!")
    }

    override fun toString() : String
    {
        if (y == null)
        {
            //this is a unary formula
            val xString = if (x is ComplexFormula) "($x)" else "$x"
            return "$operation$xString"
        }

        //this is a binary formula
        val xString = if (x is ComplexFormula) "($x)" else "$x"
        val yString = if (y is ComplexFormula) "($y)" else "$y"
        return "$xString $operation $yString"
    }
}
