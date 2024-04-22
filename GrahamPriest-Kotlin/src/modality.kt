class PossibleWorld(val index : Int) : Comparable<PossibleWorld>
{
    fun fork() : PossibleWorld = PossibleWorld(index = index+1)

    override fun compareTo(other : PossibleWorld) : Int = this.index.compareTo(other.index)
    override fun equals(other : Any?) = (other as? PossibleWorld)?.index == index
    override fun hashCode() = index.hashCode()
    override fun toString() = "w$index"
}

class NotNecessaryRule : IRule
{
    override fun isApplicable(node : ProofTreeNode) : Boolean
    {
        return (node.formula as? ComplexFormula)?.operation == Operation.Non &&
                ((node.formula as? ComplexFormula)?.x as? ComplexFormula)?.operation == Operation.Necessary
    }

    override fun apply(factory : RuleApplyFactory, node : ProofTreeNode) : ProofSubtree
    {
        val p = ((node.formula as ComplexFormula).x as ComplexFormula).x
        val possibleNonP = factory.newFormula(Operation.Possible, factory.newFormula(Operation.Non, p))
        return ProofSubtree(left = factory.newNode(possibleNonP))
    }
}

class NotPossibleRule : IRule
{
    override fun isApplicable(node : ProofTreeNode) : Boolean
    {
        return (node.formula as? ComplexFormula)?.operation == Operation.Non &&
                ((node.formula as? ComplexFormula)?.x as? ComplexFormula)?.operation == Operation.Possible
    }

    override fun apply(factory : RuleApplyFactory, node : ProofTreeNode) : ProofSubtree
    {
        val p = ((node.formula as ComplexFormula).x as ComplexFormula).x
        val necessaryNonP = factory.newFormula(Operation.Necessary, factory.newFormula(Operation.Non, p))
        return ProofSubtree(left = factory.newNode(necessaryNonP))
    }
}

class NecessaryRule : IRule
{
    override fun isApplicable(node : ProofTreeNode) : Boolean
    {
        return (node.formula as? ComplexFormula)?.operation == Operation.Necessary
    }

    override fun apply(factory : RuleApplyFactory, node : ProofTreeNode) : ProofSubtree
    {
        node.formula as ComplexFormula

        val path = node.getPathFromRootNode()
        val forkedWorlds = path.getAllForkedWorlds()
        if (forkedWorlds.isEmpty())
        {
            //todo to ask: is this rule ok?
            return PossibleRule().apply(factory, node)
        }

        var outputNode : ProofTreeNode? = null
        for (forkedWorld in forkedWorlds)
        {
            val previousOutputNode = outputNode
            outputNode = factory.newNode(node.formula.x.inWorld(forkedWorld))
            outputNode.comment = "${node.formula.possibleWorld}R${forkedWorld}"
            outputNode.left = previousOutputNode
        }

        return ProofSubtree(left = outputNode)
    }
}

class PossibleRule : IRule
{
    override fun isApplicable(node : ProofTreeNode) : Boolean
    {
        return (node.formula as? ComplexFormula)?.operation == Operation.Possible
    }

    override fun apply(factory : RuleApplyFactory, node : ProofTreeNode) : ProofSubtree
    {
        node.formula as ComplexFormula

        val path = node.getPathFromRootNode()
        val currentWorld = path.getWorldToBeForked()
        val forkedWorld = currentWorld.fork()
        val newNode = factory.newNode(node.formula.x.inWorld(forkedWorld))
        newNode.comment = "${currentWorld}R${forkedWorld}"
        return ProofSubtree(left = newNode)
    }
}

fun FormulaFactory.inWorld(possibleWorld : PossibleWorld) : FormulaFactory
{
    val original = this
    return FormulaFactory(original.logic, possibleWorld)
}

fun IFormula.inWorld(possibleWorld : PossibleWorld) : IFormula
{
    return when(val original = this)
    {
        is AtomicFormula -> AtomicFormula(original.name, possibleWorld, original.formulaFactory.inWorld(possibleWorld))
        is ComplexFormula -> ComplexFormula(original.x.inWorld(possibleWorld), original.operation, original.y?.inWorld(possibleWorld),
                                possibleWorld, original.formulaFactory.inWorld(possibleWorld))
        else -> this
    }
}
