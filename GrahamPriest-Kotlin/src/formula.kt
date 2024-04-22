open class Operation(val sign : Char, val isUnary : Boolean)
{
    companion object
    {
        val Non = Operation(sign = '~', isUnary = true)
        val And = Operation(sign = '&', isUnary = false)
        val Or = Operation(sign = 'v', isUnary = false)
        val Imply = Operation(sign = '→', isUnary = false)
        val BiImply = Operation(sign = '↔', isUnary = false)
        val Necessary = Operation(sign = '□', isUnary = true)
        val Possible = Operation(sign = '◇', isUnary = true)
    }

    class ForAny(val x : String) : Operation(sign = '∀', isUnary = true)
    class Exists(val x : String) : Operation(sign = '∃', isUnary = true)

    override fun equals(other : Any?) = (other as? Operation)?.sign==sign
    override fun hashCode() = sign.hashCode()
    override fun toString() = sign.toString()
}

interface IFormula
{
    val possibleWorld : PossibleWorld
    val formulaFactory : FormulaFactory
}

class FormulaFactory
(
    val logic : ILogic = DEFAULT_LOGIC,
    private val possibleWorld : PossibleWorld = PossibleWorld(0),
)
{
    fun newAtom(name : String) : IFormula
    {
        return AtomicFormula(name, possibleWorld, formulaFactory = this)
    }

    fun new(operation : Operation, x : IFormula) : IFormula
    {
        if (!logic.isOperationAvailable(operation))
            throw RuntimeException("Operation $operation is not available in ${logic::class.simpleName}!")

        return ComplexFormula(x, operation, y = null, possibleWorld, formulaFactory = this)
    }

    fun new(x : IFormula, operation : Operation, y : IFormula) : IFormula
    {
        if (!logic.isOperationAvailable(operation))
            throw RuntimeException("Operation $operation is not available in ${logic::class.simpleName}!")

        return ComplexFormula(x, operation, y, possibleWorld, formulaFactory = this)
    }
}

class AtomicFormula
(
    val name : String,
    override val possibleWorld : PossibleWorld,
    override val formulaFactory : FormulaFactory,
) : IFormula
{
    override fun equals(other : Any?) : Boolean
    {
        return (other as? AtomicFormula)?.let { that ->
            that.name == this.name &&
            that.possibleWorld == this.possibleWorld
        }?:false
    }

    override fun hashCode() = name.hashCode()
    override fun toString() = name
}

class ComplexFormula
(
    val x : IFormula,
    val operation : Operation,
    val y : IFormula?,
    override val possibleWorld : PossibleWorld,
    override val formulaFactory : FormulaFactory,
) : IFormula
{
    init
    {
        if (operation.isUnary && y != null)
            throw RuntimeException("$operation should use only one argument!")
        if (!operation.isUnary && y == null)
            throw RuntimeException("$operation should use two arguments!")
    }

    override fun equals(other : Any?) : Boolean
    {
        return (other as? ComplexFormula)?.let { that ->
            that.x == this.x && that.y == this.y &&
            that.operation == this.operation &&
            that.possibleWorld == this.possibleWorld
        }?:false
    }

    override fun hashCode() : Int
    {
        return (31 * (31 * x.hashCode()) + (y?.hashCode()?:0)) + operation.hashCode()
    }

    override fun toString() : String
    {
        val xString = if (x is ComplexFormula && !x.operation.isUnary) "($x)" else "$x"

        if (y == null)
        {
            //this is a unary formula
            return "$operation$xString"
        }

        //this is a binary formula
        val yString = if (y is ComplexFormula && !y.operation.isUnary) "($y)" else "$y"
        return "$xString $operation $yString"
    }
}

fun IFormula.cloned() : IFormula
{
    return when(val original = this)
    {
        is AtomicFormula -> AtomicFormula(original.name, original.possibleWorld, original.formulaFactory)
        is ComplexFormula -> ComplexFormula(original.x, original.operation, original.y, original.possibleWorld, original.formulaFactory)
        else -> this
    }
}
