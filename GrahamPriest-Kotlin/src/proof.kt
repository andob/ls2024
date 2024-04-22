class Theory
(
    val premises : List<IFormula>,
    val conclusion : IFormula,
    val logic : ILogic = DEFAULT_LOGIC,
)
{
    fun prove() : ProofTree
    {
        val proofTree = buildInitialProofTree()

        val decompositionQueue = ArrayDeque<ProofTreeNode>()
        decompositionQueue.addAll(proofTree)

        while (!decompositionQueue.isEmpty() && proofTree.nodeIdSequence.hasNext())
        {
            val node = decompositionQueue.removeFirst()

            val subtree = logic.getRules().find { rule -> rule.isApplicable(node) }?.apply(node)
            if (subtree != null)
            {
                proofTree.appendSubtree(subtree, node.id)

                proofTree.checkForContradictions()

                decompositionQueue.addAll(subtree.getAllChildNodes())
            }
        }

        return proofTree
    }

    private fun buildInitialProofTree() : ProofTree
    {
        var id = 0L
        val formulaFactory = conclusion.formulaFactory
        val nonConclusion = formulaFactory.new(Operation.Non, conclusion)

        if (premises.isEmpty())
        {
            val tree = ProofTree(ProofTreeNode(--id, nonConclusion))
            tree.attachNodeFactory(ProofTreeNodeFactory(tree))
            return tree
        }

        val rootNode = ProofTreeNode(--id, premises.first())
        var node = rootNode

        for (index in 1 until premises.size)
        {
            val premise = premises[index]
            node.left = ProofTreeNode(--id, premise)
            node = node.left!!
        }

        node.left = ProofTreeNode(--id, nonConclusion)

        val tree = ProofTree(rootNode)
        tree.attachNodeFactory(ProofTreeNodeFactory(tree))
        return tree
    }
}

private fun ArrayDeque<ProofTreeNode>.addAll(proofTree : ProofTree) = addAllImpl(proofTree.rootNode)
private fun ArrayDeque<ProofTreeNode>.addAllImpl(proofTreeNode : ProofTreeNode)
{
    proofTreeNode.left?.let(this::addAllImpl)
    proofTreeNode.right?.let(this::addAllImpl)
    addLast(proofTreeNode)
}
