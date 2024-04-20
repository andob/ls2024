data class Theory
(
    val premises : List<IFormula> = listOf(),
    val conclusion : IFormula,
    val possibleWorlds : List<PossibleWorld> = listOf(),
    val shouldStopOnFirstContradiction : Boolean = true,
)
{
    fun prove() : ProofTree
    {
        val proofTree = buildInitialProofTree()

        val decompositionQueue = ArrayDeque<ProofTreeNode>()
        decompositionQueue.addAll(proofTree)

        while (!decompositionQueue.isEmpty())
        {
            val node = decompositionQueue.removeFirst()

            val subtree = RULES.find { rule -> rule.isApplicable(node) }?.apply(node)
            if (subtree != null)
            {
                proofTree.append(subtree)

                proofTree.checkForContradictions()

                if (subtree.left != null && !subtree.left.isContradictory)
                {
                    decompositionQueue.add(subtree.left)
                }
                if (subtree.right != null && !subtree.right.isContradictory)
                {
                    decompositionQueue.add(subtree.right)
                }
            }
        }

        return proofTree
    }

    private fun buildInitialProofTree() : ProofTree
    {
        if (premises.isEmpty())
        {
            return ProofTree(ProofTreeNode(conclusion))
        }

        val rootNode = ProofTreeNode(premises.first())
        var node = rootNode

        for (index in 1 until premises.size)
        {
            val premise = premises[index]
            node.left = ProofTreeNode(premise)
            node = node.left!!
        }

        val nonConclusion = ComplexFormula(Operation.Non, conclusion)
        node.left = ProofTreeNode(nonConclusion)
        return ProofTree(rootNode)
    }
}

private fun ArrayDeque<ProofTreeNode>.addAll(proofTree : ProofTree) = addAllImpl(proofTree.rootNode)
private fun ArrayDeque<ProofTreeNode>.addAllImpl(proofTreeNode : ProofTreeNode)
{
    proofTreeNode.left?.let(this::addAllImpl)
    proofTreeNode.right?.let(this::addAllImpl)
    addLast(proofTreeNode)
}
