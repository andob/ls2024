interface IRule
{
    fun isApplicable(node : ProofTreeNode) : Boolean

    fun apply(node : ProofTreeNode) = apply(RuleApplyFactory(node), node)
    fun apply(factory : RuleApplyFactory, node : ProofTreeNode) : ProofSubtree
}

class RuleApplyFactory(private val node : ProofTreeNode)
{
    fun newNode(formula : IFormula, left : ProofTreeNode? = null, right : ProofTreeNode? = null) : ProofTreeNode
    {
        return node.nodeFactory!!.newNode(formula, left, right)
    }

    fun newFormula(operation : Operation, x : IFormula) : IFormula
    {
        return node.formula.formulaFactory.new(operation, x)
    }
}

class DoubleNegationRule : IRule
{
    override fun isApplicable(node : ProofTreeNode) : Boolean
    {
        return (node.formula as? ComplexFormula)?.operation == Operation.Non &&
                ((node.formula as? ComplexFormula)?.x as? ComplexFormula)?.operation == Operation.Non
    }

    override fun apply(factory : RuleApplyFactory, node : ProofTreeNode) : ProofSubtree
    {
        node.formula as ComplexFormula
        node.formula.x as ComplexFormula
        return ProofSubtree(left = factory.newNode(node.formula.x.x))
    }
}

class OrRule : IRule
{
    override fun isApplicable(node : ProofTreeNode) : Boolean
    {
        return (node.formula as? ComplexFormula)?.operation == Operation.Or
    }

    override fun apply(factory : RuleApplyFactory, node : ProofTreeNode) : ProofSubtree
    {
        node.formula as ComplexFormula
        return ProofSubtree(
            left = factory.newNode(node.formula.x),
            right = factory.newNode(node.formula.y!!),
        )
    }
}

class NotOrRule : IRule
{
    override fun isApplicable(node : ProofTreeNode) : Boolean
    {
        return (node.formula as? ComplexFormula)?.operation == Operation.Non &&
                ((node.formula as? ComplexFormula)?.x as? ComplexFormula)?.operation == Operation.Or
    }

    override fun apply(factory : RuleApplyFactory, node : ProofTreeNode) : ProofSubtree
    {
        node.formula as ComplexFormula
        node.formula.x as ComplexFormula
        return ProofSubtree(
            left = factory.newNode(
                node.formula.formulaFactory.new(Operation.Non, node.formula.x.x),
                left = factory.newNode(node.formula.formulaFactory.new(Operation.Non, node.formula.x.y!!)),
            )
        )
    }
}

class AndRule : IRule
{
    override fun isApplicable(node : ProofTreeNode) : Boolean
    {
        return (node.formula as? ComplexFormula)?.operation == Operation.And
    }

    override fun apply(factory : RuleApplyFactory, node : ProofTreeNode) : ProofSubtree
    {
        node.formula as ComplexFormula
        return ProofSubtree(
            left = factory.newNode(
                formula = node.formula.x,
                left = factory.newNode(node.formula.y!!),
            )
        )
    }
}

class NotAndRule : IRule
{
    override fun isApplicable(node : ProofTreeNode) : Boolean
    {
        return (node.formula as? ComplexFormula)?.operation == Operation.Non &&
                ((node.formula as? ComplexFormula)?.x as? ComplexFormula)?.operation == Operation.And
    }

    override fun apply(factory : RuleApplyFactory, node : ProofTreeNode) : ProofSubtree
    {
        node.formula as ComplexFormula
        node.formula.x as ComplexFormula
        return ProofSubtree(
            left = factory.newNode(factory.newFormula(Operation.Non, node.formula.x.x)),
            right = factory.newNode(factory.newFormula(Operation.Non, node.formula.x.y!!)),
        )
    }
}

class ImplyRule : IRule
{
    override fun isApplicable(node : ProofTreeNode) : Boolean
    {
        return (node.formula as? ComplexFormula)?.operation == Operation.Imply
    }

    override fun apply(factory : RuleApplyFactory, node : ProofTreeNode) : ProofSubtree
    {
        node.formula as ComplexFormula
        return ProofSubtree(
            left = factory.newNode(factory.newFormula(Operation.Non, node.formula.x)),
            right = factory.newNode(node.formula.y!!),
        )
    }
}

class NotImplyRule : IRule
{
    override fun isApplicable(node : ProofTreeNode) : Boolean
    {
        return (node.formula as? ComplexFormula)?.operation == Operation.Non &&
                ((node.formula as? ComplexFormula)?.x as? ComplexFormula)?.operation == Operation.Imply
    }

    override fun apply(factory : RuleApplyFactory, node : ProofTreeNode) : ProofSubtree
    {
        node.formula as ComplexFormula
        node.formula.x as ComplexFormula
        return ProofSubtree(
            left = factory.newNode(
                formula = node.formula.x.x,
                left = factory.newNode(node.formula.formulaFactory.new(Operation.Non, node.formula.x.y!!)),
            )
        )
    }
}

class BiImplyRule : IRule
{
    override fun isApplicable(node : ProofTreeNode) : Boolean
    {
        return (node.formula as? ComplexFormula)?.operation == Operation.BiImply
    }

    override fun apply(factory : RuleApplyFactory, node : ProofTreeNode) : ProofSubtree
    {
        node.formula as ComplexFormula
        return ProofSubtree(
            left = factory.newNode(
                formula = node.formula.x,
                left = factory.newNode(node.formula.y!!)
            ),
            right = factory.newNode(
                formula = factory.newFormula(Operation.Non, node.formula.x),
                left = factory.newNode(factory.newFormula(Operation.Non, node.formula.y))
            ),
        )
    }
}

class NotBiImplyRule : IRule
{
    override fun isApplicable(node : ProofTreeNode) : Boolean
    {
        return (node.formula as? ComplexFormula)?.operation == Operation.Non &&
                ((node.formula as? ComplexFormula)?.x as? ComplexFormula)?.operation == Operation.BiImply
    }

    override fun apply(factory : RuleApplyFactory, node : ProofTreeNode) : ProofSubtree
    {
        node.formula as ComplexFormula
        node.formula.x as ComplexFormula
        return ProofSubtree(
            left = factory.newNode(
                formula = node.formula.x.x,
                left = factory.newNode(factory.newFormula(Operation.Non, node.formula.x.y!!))
            ),
            right = factory.newNode(
                formula = factory.newFormula(Operation.Non, node.formula.x.x),
                left = factory.newNode(node.formula.x.y)
            ),
        )
    }
}
