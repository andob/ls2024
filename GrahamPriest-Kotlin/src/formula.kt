open class Operation(val sign : Char, val isUnary : Boolean, val isModal : Boolean)
{
    companion object
    {
        val Non = Operation(sign = '~', isUnary = true, isModal = false)
        val And = Operation(sign = '&', isUnary = false, isModal = false)
        val Or = Operation(sign = 'v', isUnary = false, isModal = false)
        val Imply = Operation(sign = '→', isUnary = false, isModal = false)
        val BiImply = Operation(sign = '↔', isUnary = false, isModal = false)
        val Necessary = Operation(sign = '□', isUnary = true, isModal = true)
        val Possible = Operation(sign = '◇', isUnary = true, isModal = true)
    }

    override fun equals(other : Any?) = (other as? Operation)?.sign==sign
    override fun hashCode() = sign.hashCode()
    override fun toString() = sign.toString()

    class ForAll(val x : BindingPredicateArgument) : Operation(sign = '∀', isUnary = true, isModal = false)
    {
        override fun equals(other : Any?) = super.equals(other) && (other as? ForAll)?.x==x
        override fun hashCode() = hash(sign, x)
        override fun toString() = "$sign$x"
    }

    class Exists(val x : BindingPredicateArgument) : Operation(sign = '∃', isUnary = true, isModal = false)
    {
        override fun equals(other : Any?) = super.equals(other) && (other as? Exists)?.x==x
        override fun hashCode() = hash(sign, x)
        override fun toString() = "$sign$x"
    }
}

interface IFormula
{
    val possibleWorld : PossibleWorld
    val formulaFactory : FormulaFactory

    fun cloned() : IFormula
    {
        return when(val original = this)
        {
            is AtomicFormula -> AtomicFormula(original.name, original.arguments.cloned(), original.possibleWorld, original.formulaFactory)
            is ComplexFormula -> ComplexFormula(original.x, original.operation, original.y, original.possibleWorld, original.formulaFactory)
            is ModalRelationDescriptorFormula -> ModalRelationDescriptorFormula(original.fromWorld, original.toWorld, original.formulaFactory)
            else -> original
        }
    }

    fun findAllAtoms() : List<AtomicFormula>
    {
        val outAtoms = mutableListOf<AtomicFormula>()
        findAllAtoms(outAtoms)
        return outAtoms
    }

    private fun findAllAtoms(outAtoms : MutableList<AtomicFormula>)
    {
        if (this is AtomicFormula)
        {
            outAtoms.add(this)
        }
        else if (this is ComplexFormula)
        {
            this.x.findAllAtoms(outAtoms)
            this.y?.findAllAtoms(outAtoms)
        }
    }
}

class FormulaFactory
(
    val logic : ILogic,
    private val possibleWorld : PossibleWorld = PossibleWorld(0),
)
{
    fun newAtom(name : String) : AtomicFormula
    {
        return AtomicFormula(name, arguments = emptyList(), possibleWorld, formulaFactory = this)
    }

    fun newBindingVariable(name : String) : BindingPredicateArgument
    {
        return BindingPredicateArgument(name)
    }

    fun newUnboundedVariable(name : String) : UnboundedPredicateArgument
    {
        return UnboundedPredicateArgument(name)
    }

    fun new(operation : Operation, x : IFormula) : ComplexFormula
    {
        if (!logic.isOperationAvailable(operation))
            throw RuntimeException("Operation $operation is not available in ${logic::class.simpleName}!")

        return ComplexFormula(x, operation, y = null, possibleWorld, formulaFactory = this)
    }

    fun new(x : IFormula, operation : Operation, y : IFormula) : ComplexFormula
    {
        if (!logic.isOperationAvailable(operation))
            throw RuntimeException("Operation $operation is not available in ${logic::class.simpleName}!")

        return ComplexFormula(x, operation, y, possibleWorld, formulaFactory = this)
    }

    fun newModalRelationDescriptor(fromWorld : PossibleWorld, toWorld : PossibleWorld) : ModalRelationDescriptorFormula
    {
        return ModalRelationDescriptorFormula(fromWorld, toWorld, formulaFactory = this)
    }
}

class AtomicFormula
(
    val name : String,
    val arguments : List<IPredicateArgument>,
    override val possibleWorld : PossibleWorld,
    override val formulaFactory : FormulaFactory,
) : IFormula
{
    operator fun invoke(vararg arguments : IPredicateArgument) : AtomicFormula
    {
        val newArguments = this.arguments.plus(arguments)
        return AtomicFormula(name, newArguments, possibleWorld, formulaFactory)
    }

    override fun hashCode() = name.hashCode()
    override fun equals(other : Any?) : Boolean
    {
        return (other as? AtomicFormula)?.let { that ->
            that.name == this.name &&
            that.possibleWorld == this.possibleWorld &&
            that.arguments.size == this.arguments.size &&
            (0 until that.arguments.size).all { i -> that.arguments[i] == this.arguments[i] }
        }?:false
    }

    fun isReplaceableWith(that : AtomicFormula) : Boolean
    {
        return that.name == this.name &&
            that.possibleWorld == this.possibleWorld &&
            that.arguments.size == this.arguments.size &&
            (0 until that.arguments.size).all { i -> that.arguments[i].isReplaceableWith(this.arguments[i]) }
    }

    override fun toString() : String
    {
        return if (arguments.isEmpty()) name
        else "$name(${arguments.joinToString(separator = ",")})"
    }
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

    override fun hashCode() = hash(x, y, operation)
    override fun equals(other : Any?) : Boolean
    {
        return (other as? ComplexFormula)?.let { that ->
            that.x == this.x && that.y == this.y &&
            that.operation == this.operation &&
            that.possibleWorld == this.possibleWorld
        }?:false
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

class ModalRelationDescriptorFormula
(
    override val possibleWorld : PossibleWorld,
    private val targetPossibleWorld : PossibleWorld,
    override val formulaFactory : FormulaFactory,
) : IFormula
{
    val fromWorld get() = possibleWorld
    val toWorld get() = targetPossibleWorld

    override fun hashCode() = hash(fromWorld, toWorld)
    override fun equals(other : Any?) : Boolean
    {
        return (other as? ModalRelationDescriptorFormula)?.let { that ->
            that.fromWorld == this.fromWorld && that.toWorld == this.toWorld
        }?:false
    }

    override fun toString() : String
    {
        return "${fromWorld}R$toWorld"
    }
}
