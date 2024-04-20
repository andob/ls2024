interface IRule
{
    fun isApplicable(node : ProofTreeNode) : Boolean
    fun apply(node : ProofTreeNode) : ProofSubtree
}

val RULES = arrayOf(
    DoubleNegationRule(),
    OrRule(),
    NotOrRule(),
    AndRule(),
    NotAndRule(),
    ImplyRule(),
    NotImplyRule(),
    BiImplyRule(),
    NotBiImplyRule(),

    //modal logic rules
    NotNecessaryRule(),
    NotPossibleRule(),
    NecessaryRule(),
    PossibleRule(),
)

class DoubleNegationRule : IRule
{
    override fun isApplicable(node : ProofTreeNode) : Boolean
    {
        return (node.formula as? ComplexFormula)?.operation == Operation.Non &&
                ((node.formula as? ComplexFormula)?.x as? ComplexFormula)?.operation == Operation.Non
    }

    override fun apply(node : ProofTreeNode) : ProofSubtree
    {
        node.formula as ComplexFormula
        node.formula.x as ComplexFormula
        return ProofSubtree(left = ProofTreeNode(node.formula.x.x))
    }
}

class OrRule : IRule
{
    override fun isApplicable(node : ProofTreeNode) : Boolean
    {
        return (node.formula as? ComplexFormula)?.operation == Operation.Or
    }

    override fun apply(node : ProofTreeNode) : ProofSubtree
    {
        node.formula as ComplexFormula
        return ProofSubtree(
            left = ProofTreeNode(node.formula.x),
            right = ProofTreeNode(node.formula.y!!),
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

    override fun apply(node : ProofTreeNode) : ProofSubtree
    {
        node.formula as ComplexFormula
        node.formula.x as ComplexFormula
        return ProofSubtree(
            left = ProofTreeNode(
                ComplexFormula(Operation.Non, node.formula.x.x),
                left = ProofTreeNode(ComplexFormula(Operation.Non, node.formula.x.y!!)),
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

    override fun apply(node : ProofTreeNode) : ProofSubtree
    {
        node.formula as ComplexFormula
        return ProofSubtree(
            left = ProofTreeNode(
                node.formula.x,
                left = ProofTreeNode(node.formula.y!!),
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

    override fun apply(node : ProofTreeNode) : ProofSubtree
    {
        node.formula as ComplexFormula
        node.formula.x as ComplexFormula
        return ProofSubtree(
            left = ProofTreeNode(ComplexFormula(Operation.Non, node.formula.x.x)),
            right = ProofTreeNode(ComplexFormula(Operation.Non, node.formula.x.y!!)),
        )
    }
}

class ImplyRule : IRule
{
    override fun isApplicable(node : ProofTreeNode) : Boolean
    {
        return (node.formula as? ComplexFormula)?.operation == Operation.Imply
    }

    override fun apply(node : ProofTreeNode) : ProofSubtree
    {
        node.formula as ComplexFormula
        return ProofSubtree(
            left = ProofTreeNode(ComplexFormula(Operation.Non, node.formula.x)),
            right = ProofTreeNode(node.formula.y!!),
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

    override fun apply(node : ProofTreeNode) : ProofSubtree
    {
        node.formula as ComplexFormula
        node.formula.x as ComplexFormula
        return ProofSubtree(
            left = ProofTreeNode(
                node.formula.x.x,
                left = ProofTreeNode(ComplexFormula(Operation.Non, node.formula.x.y!!)),
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

    override fun apply(node : ProofTreeNode) : ProofSubtree
    {
        node.formula as ComplexFormula
        return ProofSubtree(
            left = ProofTreeNode(
                node.formula.x,
                left = ProofTreeNode(node.formula.y!!)
            ),
            right = ProofTreeNode(
                ComplexFormula(Operation.Non, node.formula.x),
                left = ProofTreeNode(ComplexFormula(Operation.Non, node.formula.y))
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

    override fun apply(node : ProofTreeNode) : ProofSubtree
    {
        node.formula as ComplexFormula
        node.formula.x as ComplexFormula
        return ProofSubtree(
            left = ProofTreeNode(
                node.formula.x,
                left = ProofTreeNode(ComplexFormula(Operation.Non, node.formula.x.y!!))
            ),
            right = ProofTreeNode(
                node.formula.x,
                left = ProofTreeNode(ComplexFormula(Operation.Non, node.formula.x.y))
            ),
        )
    }
}
