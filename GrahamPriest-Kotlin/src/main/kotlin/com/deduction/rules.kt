package com.deduction

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
)

class DoubleNegationRule : IRule
{
    override fun isApplicable(node : ProofTreeNode) : Boolean
    {
        return (node.formula as? UnaryFormula)?.operation == Operation.Non &&
                ((node.formula as? UnaryFormula)?.x as? UnaryFormula)?.operation == Operation.Non
    }

    override fun apply(node : ProofTreeNode) : ProofSubtree
    {
        node.formula as UnaryFormula
        node.formula.x as UnaryFormula
        return ProofSubtree(left = ProofTreeNode(node.formula.x.x))
    }
}

class OrRule : IRule
{
    override fun isApplicable(node : ProofTreeNode) : Boolean
    {
        return (node.formula as? BinaryFormula)?.operation == Operation.Or
    }

    override fun apply(node : ProofTreeNode) : ProofSubtree
    {
        node.formula as BinaryFormula
        return ProofSubtree(
            left = ProofTreeNode(node.formula.x),
            right = ProofTreeNode(node.formula.y),
        )
    }
}

class NotOrRule : IRule
{
    override fun isApplicable(node : ProofTreeNode) : Boolean
    {
        return (node.formula as? UnaryFormula)?.operation == Operation.Non &&
                ((node.formula as? UnaryFormula)?.x as? BinaryFormula)?.operation == Operation.Or
    }

    override fun apply(node : ProofTreeNode) : ProofSubtree
    {
        node.formula as UnaryFormula
        node.formula.x as BinaryFormula
        return ProofSubtree(
            left = ProofTreeNode(UnaryFormula(Operation.Non, node.formula.x.x),
                left = ProofTreeNode(UnaryFormula(Operation.Non, node.formula.x.y)),
        ))
    }
}

class AndRule : IRule
{
    override fun isApplicable(node : ProofTreeNode) : Boolean
    {
        return (node.formula as? BinaryFormula)?.operation == Operation.And
    }

    override fun apply(node : ProofTreeNode) : ProofSubtree
    {
        node.formula as BinaryFormula
        return ProofSubtree(
            left = ProofTreeNode(node.formula.x,
                left = ProofTreeNode(node.formula.y),
            ))
    }

}

class NotAndRule : IRule
{
    override fun isApplicable(node : ProofTreeNode) : Boolean
    {
        return (node.formula as? UnaryFormula)?.operation == Operation.Non &&
                ((node.formula as? UnaryFormula)?.x as? BinaryFormula)?.operation == Operation.And
    }

    override fun apply(node : ProofTreeNode) : ProofSubtree
    {
        node.formula as UnaryFormula
        node.formula.x as BinaryFormula
        return ProofSubtree(
            left = ProofTreeNode(UnaryFormula(Operation.Non, node.formula.x.x)),
            right = ProofTreeNode(UnaryFormula(Operation.Non, node.formula.x.y)),
        )
    }
}

class ImplyRule : IRule
{
    override fun isApplicable(node : ProofTreeNode) : Boolean
    {
        return (node.formula as? BinaryFormula)?.operation == Operation.Imply
    }

    override fun apply(node : ProofTreeNode) : ProofSubtree
    {
        node.formula as BinaryFormula
        return ProofSubtree(
            left = ProofTreeNode(UnaryFormula(Operation.Non, node.formula.x)),
            right = ProofTreeNode(node.formula.y),
        )
    }
}

class NotImplyRule : IRule
{
    override fun isApplicable(node : ProofTreeNode) : Boolean
    {
        return (node.formula as? UnaryFormula)?.operation == Operation.Non &&
                ((node.formula as? UnaryFormula)?.x as? BinaryFormula)?.operation == Operation.Imply
    }

    override fun apply(node : ProofTreeNode) : ProofSubtree
    {
        node.formula as UnaryFormula
        node.formula.x as BinaryFormula
        return ProofSubtree(
            left = ProofTreeNode(node.formula.x.x,
                left = ProofTreeNode(UnaryFormula(Operation.Non, node.formula.x.y)),
        ))
    }
}

class BiImplyRule : IRule
{
    override fun isApplicable(node : ProofTreeNode) : Boolean
    {
        return (node.formula as? BinaryFormula)?.operation == Operation.BiImply
    }

    override fun apply(node : ProofTreeNode) : ProofSubtree
    {
        node.formula as BinaryFormula
        return ProofSubtree(
            left = ProofTreeNode(node.formula.x,
                left = ProofTreeNode(node.formula.y)
            ),
            right = ProofTreeNode(UnaryFormula(Operation.Non, node.formula.x),
                left = ProofTreeNode(UnaryFormula(Operation.Non, node.formula.y))
            ),
        )
    }
}

class NotBiImplyRule : IRule
{
    override fun isApplicable(node : ProofTreeNode) : Boolean
    {
        return (node.formula as? UnaryFormula)?.operation == Operation.Non &&
                ((node.formula as? UnaryFormula)?.x as? BinaryFormula)?.operation == Operation.BiImply
    }

    override fun apply(node : ProofTreeNode) : ProofSubtree
    {
        node.formula as UnaryFormula
        node.formula.x as BinaryFormula
        return ProofSubtree(
            left = ProofTreeNode(node.formula.x,
                left = ProofTreeNode(UnaryFormula(Operation.Non, node.formula.x.y))
            ),
            right = ProofTreeNode(node.formula.x,
                left = ProofTreeNode(UnaryFormula(Operation.Non, node.formula.x.y))
            ),
        )
    }
}
