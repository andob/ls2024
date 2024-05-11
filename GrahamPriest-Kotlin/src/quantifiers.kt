interface IPredicateArgument
{
    fun isReplaceableWith(that : IPredicateArgument) : Boolean
}

class UnboundedPredicateArgument(val name : String) : IPredicateArgument
{
    override fun equals(other : Any?) = (other as? UnboundedPredicateArgument)?.name==name
    override fun hashCode() = name.hashCode()
    override fun toString() = name

    override fun isReplaceableWith(that : IPredicateArgument) : Boolean = when
    {
        that is UnboundedPredicateArgument -> that.name==this.name
        that is BindingPredicateArgument -> that.isInstantiated()
        else -> false
    }
}

class BindingPredicateArgument
(
    val typeName : String, //instance types / "variables" eg: x,y,z,...
    var instanceName : String? = null, //instances / "constants" eg: a,b,c,...
    val equivalences : MutableList<BindingPredicateArgument> = mutableListOf(),
) : IPredicateArgument, Comparable<BindingPredicateArgument>
{
    fun isInstantiated() = instanceName != null

    override fun compareTo(other : BindingPredicateArgument) : Int
    {
        return this.typeName.compareTo(other.typeName)
    }

    override fun hashCode() = hash(typeName, instanceName)
    override fun equals(other : Any?) : Boolean
    {
        return (other as? BindingPredicateArgument)?.let { that ->
            that.typeName == this.typeName && that.instanceName == this.instanceName
        }?:false
    }

    fun couldBecomeEquivalentTo(y : BindingPredicateArgument) = equivalences.add(y)

    override fun isReplaceableWith(that : IPredicateArgument) : Boolean = when
    {
        that is UnboundedPredicateArgument -> true
        that is BindingPredicateArgument -> that.typeName==this.typeName ||
            this.equivalences.any { equivalentType -> that.typeName==equivalentType.typeName }
        else -> false
    }

    override fun toString() : String
    {
        return if (instanceName!=null) "$instanceName:$typeName" else typeName
    }
}

fun List<IPredicateArgument>.cloned() = map { argument -> argument.cloned() }
fun IPredicateArgument.cloned() : IPredicateArgument
{
    return when(val original = this)
    {
        is UnboundedPredicateArgument -> UnboundedPredicateArgument(original.name)
        is BindingPredicateArgument -> BindingPredicateArgument(original.typeName, original.instanceName, original.equivalences)
        else -> original
    }
}

class NotExistsRule : IRule
{
    override fun isApplicable(node : ProofTreeNode) : Boolean
    {
        return (node.formula as? ComplexFormula)?.operation == Operation.Non &&
                ((node.formula as? ComplexFormula)?.x as? ComplexFormula)?.operation is Operation.Exists
    }

    override fun apply(factory : RuleApplyFactory, node : ProofTreeNode) : ProofSubtree
    {
        val original = ((node.formula as ComplexFormula).x as ComplexFormula)

        val p = original.x
        val x = (original.operation as Operation.Exists).x
        val forAllNonP = factory.newFormula(Operation.ForAll(x), factory.newFormula(Operation.Non, p))
        return ProofSubtree(left = factory.newNode(forAllNonP))
    }
}

class NotForAllRule : IRule
{
    override fun isApplicable(node : ProofTreeNode) : Boolean
    {
        return (node.formula as? ComplexFormula)?.operation == Operation.Non &&
                ((node.formula as? ComplexFormula)?.x as? ComplexFormula)?.operation is Operation.ForAll
    }

    override fun apply(factory : RuleApplyFactory, node : ProofTreeNode) : ProofSubtree
    {
        val original = ((node.formula as ComplexFormula).x as ComplexFormula)

        val p = original.x
        val x = (original.operation as Operation.ForAll).x
        val existsNonP = factory.newFormula(Operation.Exists(x), factory.newFormula(Operation.Non, p))
        return ProofSubtree(left = factory.newNode(existsNonP))
    }
}

class ExistsRule : IRule
{
    override fun isApplicable(node : ProofTreeNode) : Boolean
    {
        return (node.formula as? ComplexFormula)?.operation is Operation.Exists
    }

    override fun apply(factory : RuleApplyFactory, node : ProofTreeNode) : ProofSubtree
    {
        val originalFormula = node.formula as ComplexFormula
        val argument = ((originalFormula.operation as? Operation.Exists)?.x)
            ?:((originalFormula.operation as? Operation.ForAll)?.x)!!

        val modifiedFormula = originalFormula.x.instantiated(argument, factory)
        return ProofSubtree(left = factory.newNode(modifiedFormula))
    }
}

class ForAllRule : IRule
{
    override fun isApplicable(node : ProofTreeNode) : Boolean
    {
        return (node.formula as? ComplexFormula)?.operation is Operation.ForAll
    }

    override fun apply(factory : RuleApplyFactory, node : ProofTreeNode) : ProofSubtree
    {
        val originalFormula = node.formula as ComplexFormula
        val argument = (originalFormula.operation as Operation.ForAll).x

        val path = node.getPathFromRootToLeafsThroughNode()
        val previousInstances = path.getAllInstantiatedPredicateArguments()
            .filter { instance -> instance.isReplaceableWith(argument) }
        if (previousInstances.isEmpty())
        {
            return ExistsRule().apply(factory, node)
        }

        val outputNodes = mutableListOf<ProofTreeNode>()
        for (previousInstance in previousInstances)
        {
            val instantiatedFormula = originalFormula.x.instantiated(argument, previousInstance.instanceName!!)
            outputNodes.add(factory.newNode(instantiatedFormula))
        }

        return ProofSubtree.newWithSequentialVerticalNodesOnLeft(outputNodes)
    }
}

fun IFormula.instantiated(argument : BindingPredicateArgument, factory : RuleApplyFactory) : IFormula
{
    val instanceName = Lazy { factory.newPredicateArgumentInstanceName() }
    return instantiated(argument, instanceName)
}

fun IFormula.instantiated(argument : BindingPredicateArgument, instanceName : String) : IFormula
{
    return instantiated(argument, instanceName = Lazy { instanceName })
}

fun IFormula.instantiated(argument : BindingPredicateArgument, instanceName : Lazy<String>) : IFormula
{
    val original = this
    if (original is ComplexFormula)
    {
        val instantiatedX = original.x.instantiated(argument, instanceName)
        val instantiatedY = original.y?.instantiated(argument, instanceName)
        return ComplexFormula(instantiatedX, original.operation, instantiatedY, original.possibleWorld, original.formulaFactory)
    }

    if (original is AtomicFormula)
    {
        val instantiatedArguments = original.arguments.cloned()

        instantiatedArguments.filterIsInstance<BindingPredicateArgument>()
            .filter { a -> a.typeName == argument.typeName && a.instanceName == null }
            .forEach { a -> a.instanceName = instanceName.get() }

        return AtomicFormula(original.name, instantiatedArguments, original.possibleWorld, original.formulaFactory)
    }

    return this
}
