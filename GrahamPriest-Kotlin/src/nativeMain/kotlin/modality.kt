
data class PossibleWorld
(
    val name : String
)
{
    override fun equals(other : Any?) = (other as? PossibleWorld)?.name == name
    override fun hashCode() = name.hashCode()
    override fun toString() = name
}

class NotNecessaryRule : IRule
{
    override fun isApplicable(node : ProofTreeNode) : Boolean
    {
        return (node.formula as? ComplexFormula)?.operation == Operation.Non &&
                ((node.formula as? ComplexFormula)?.x as? ComplexFormula)?.operation == Operation.Necessary &&
                ((node.formula as? ComplexFormula)?.x as? ComplexFormula)?.x?.world != null
    }

    override fun apply(node : ProofTreeNode) : ProofSubtree
    {
        val p = ((node.formula as ComplexFormula).x as ComplexFormula).x
        val possibleNonP = ComplexFormula(Operation.Possible, ComplexFormula(Operation.Non, p))
        return ProofSubtree(left = ProofTreeNode(possibleNonP))
    }
}

class NotPossibleRule : IRule
{
    override fun isApplicable(node : ProofTreeNode) : Boolean
    {
        return (node.formula as? ComplexFormula)?.operation == Operation.Non &&
                ((node.formula as? ComplexFormula)?.x as? ComplexFormula)?.operation == Operation.Possible &&
                ((node.formula as? ComplexFormula)?.x as? ComplexFormula)?.x?.world != null
    }

    override fun apply(node : ProofTreeNode) : ProofSubtree
    {
        val p = ((node.formula as ComplexFormula).x as ComplexFormula).x
        val necessaryNonP = ComplexFormula(Operation.Necessary, ComplexFormula(Operation.Non, p))
        return ProofSubtree(left = ProofTreeNode(necessaryNonP))
    }
}

class NecessaryRule : IRule
{
    override fun isApplicable(node : ProofTreeNode) : Boolean
    {
        return (node.formula as? ComplexFormula)?.operation == Operation.Necessary &&
                (node.formula as? ComplexFormula)?.x?.world != null
    }

    override fun apply(node : ProofTreeNode) : ProofSubtree
    {
        TODO("NecessaryRule is not yet implemented!")
    }
}

class PossibleRule : IRule
{
    override fun isApplicable(node : ProofTreeNode) : Boolean
    {
        return (node.formula as? ComplexFormula)?.operation == Operation.Possible &&
                (node.formula as? ComplexFormula)?.x?.world != null
    }

    override fun apply(node : ProofTreeNode) : ProofSubtree
    {
        TODO("PossibleRule is not yet implemented!")
    }
}
