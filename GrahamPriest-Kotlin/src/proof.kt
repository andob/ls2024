class Problem
(
    val logic : ILogic,
    val premises : List<IFormula>,
    val conclusion : IFormula,
    val description : String = "",
)
{
    companion object
    {
        fun fromConfig(config : String) : Problem
        {
            return ConfigParser.parse(config)
        }
    }

    fun prove() : ProofTree
    {
        val proofTree = buildInitialProofTree()

        val decompositionQueue = DecompositionPriorityQueue(logic)
        decompositionQueue.push(proofTree)

        while (!decompositionQueue.isEmpty() && proofTree.nodeIdSequence.hasNext())
        {
            val node = decompositionQueue.pop() ?: continue

            val ruleToApply = logic.getRules().find { rule -> rule.isApplicable(node) }
            val subtree = ruleToApply?.apply(node)
            if (subtree != null)
            {
                proofTree.appendSubtree(subtree, node.id)

                proofTree.checkForContradictions()

                decompositionQueue.push(subtree)

                val isSubtreeEmpty = subtree.getAllChildNodes().none { subtreeNode ->
                    subtreeNode.formula is AtomicFormula || subtreeNode.formula is ComplexFormula
                }

                if (isSubtreeEmpty && decompositionQueue.isNodeReusable(node))
                {
                    decompositionQueue.banReusableNode(node)
                }
            }
        }

        proofTree.hasTimeout = !proofTree.nodeIdSequence.hasNext()

        logic.clearCache()

        return proofTree
    }

    private fun buildInitialProofTree() : ProofTree
    {
        var id = 0
        val formulaFactory = conclusion.formulaFactory
        val nonConclusion = formulaFactory.new(Operation.Non, conclusion)

        if (premises.isEmpty())
        {
            val rootNode = ProofTreeNode(--id, nonConclusion)
            val tree = ProofTree(problem = this, rootNode)
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

        val tree = ProofTree(problem = this, rootNode)
        tree.attachNodeFactory(ProofTreeNodeFactory(tree))
        return tree
    }
}

class DecompositionPriorityQueue(val logic : ILogic)
{
    private enum class Priority { High, Normal, Low }

    private val consumableNodes = mutableListOf<ProofTreeNode>()
    private val reusableNodes = mutableListOf<ProofTreeNode>()
    private val nearlyBannedReusableNodes = mutableMapOf<ProofTreeNode, Int>()
    private val bannedReusableNodes = mutableListOf<ProofTreeNode>()

    companion object { const val REUSABLE_NODES_BAN_THRESHOLD = 10 }

    fun isEmpty() : Boolean
    {
        return consumableNodes.isEmpty() && reusableNodes.isEmpty()
    }

    fun push(tree : ProofTree)
    {
        pushImpl(tree.rootNode)
    }

    fun push(subtree : ProofSubtree)
    {
        pushImpl(subtree.left)
        pushImpl(subtree.right)
    }

    private fun pushImpl(node : ProofTreeNode?)
    {
        if (node?.left != null) { pushImpl(node.left) }
        if (node?.right != null) { pushImpl(node.right) }
        if (node != null) { consumableNodes.add(node) }
    }

    fun pop() : ProofTreeNode?
    {
        if (consumableNodes.isEmpty() && reusableNodes.isNotEmpty())
        {
            val reusableNode = reusableNodes.removeAt(0)
            if (!bannedReusableNodes.contains(reusableNode))
                consumableNodes.add(reusableNode)
        }

        if (consumableNodes.isEmpty())
            return null

        for (priority in arrayOf(Priority.High, Priority.Normal, Priority.Low))
        {
            for (index in 0 until consumableNodes.size)
            {
                val node = consumableNodes[index]
                if (getNodePriority(node) == priority)
                {
                    consumableNodes.removeAt(index)
                    if (shouldNodeBeReused(node))
                        reusableNodes.add(node)

                    return node
                }
            }
        }

        return null
    }

    private fun getNodePriority(node : ProofTreeNode) : Priority
    {
        val ruleToApply = logic.getRules().find { rule -> rule.isApplicable(node) }

        return when
        {
            node.formula is ComplexFormula && node.formula.operation is Operation.Necessary -> Priority.Low
            ruleToApply!=null && ruleToApply.wouldBranchTheTree() -> Priority.Normal
            else -> Priority.High
        }
    }

    private fun shouldNodeBeReused(node : ProofTreeNode) : Boolean
    {
        return when
        {
            bannedReusableNodes.contains(node) -> false
            node.formula is ComplexFormula && node.formula.operation is Operation.Necessary -> true
            else -> false
        }
    }

    fun isNodeReusable(node : ProofTreeNode) : Boolean
    {
        return reusableNodes.contains(node)
    }

    fun banReusableNode(node : ProofTreeNode)
    {
        val numberOfStrikes = nearlyBannedReusableNodes[node]?:0
        if (numberOfStrikes < REUSABLE_NODES_BAN_THRESHOLD)
        {
            nearlyBannedReusableNodes[node] = numberOfStrikes+1
        }
        else
        {
            bannedReusableNodes.add(node)
            nearlyBannedReusableNodes.remove(node)
        }
    }
}
