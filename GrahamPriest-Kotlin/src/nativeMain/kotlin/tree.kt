class ProofSubtree
(
    val left : ProofTreeNode? = null,
    val right : ProofTreeNode? = null,
)

class ProofTree(val rootNode : ProofTreeNode)
{
    var isProofCorrect = false

    fun append(subtree : ProofSubtree)
    {
        val leafs = getAllLeafs()
        for (leaf in leafs)
        {
            leaf.left = subtree.left
            leaf.right = subtree.right
        }
    }

    fun checkForContradictions()
    {
        for ((leaf, path) in getAllLeafsWithPaths())
        {
            if (path.isContradictory())
            {
                leaf.isContradictory = true
            }
        }

        if (getAllLeafs().all { leaf -> leaf.isContradictory })
        {
            this.isProofCorrect = true
        }
    }

    fun getAllLeafs() : List<ProofTreeNode>
    {
        val leafs = mutableListOf<ProofTreeNode>()
        this.rootNode.findAllLeafs(leafs)
        return leafs
    }

    fun getAllLeafsWithPaths() : List<Pair<ProofTreeNode, ProofTreePath>>
    {
        val leafs = mutableListOf<Pair<ProofTreeNode, ProofTreePath>>()
        val path = ProofTreePath(nodes = listOf(rootNode))
        this.rootNode.findAllLeafsWithPaths(leafs, path)
        return leafs
    }

    fun getParentNodeOfNode(node : ProofTreeNode) : ProofTreeNode?
    {
        val path = getPathFromRootNodeToNode(node)
        return path?.getOrNull(path.size()-2)
    }

    fun getPathFromRootNodeToNode(node : ProofTreeNode) : ProofTreePath?
    {
        val path = ProofTreePath(nodes = listOf(rootNode))
        return this.rootNode.findPathUntilNode(node, path)
    }

    override fun toString() : String
    {
        val isProvedStr = if (this.isProofCorrect) "PROVED!" else "NOT PROVED!"

        val stringBuilder = StringBuilder()
        stringBuilder.append(isProvedStr).append('\n')
        this.rootNode.printAsSubtreeToStringBuilder(stringBuilder)
        return stringBuilder.toString()
    }
}

class ProofTreeNode
(
    val formula : IFormula,
    var left : ProofTreeNode? = null,
    var right : ProofTreeNode? = null,
)
{
    var isContradictory = false

    override fun toString() = formula.toString()

    fun findAllLeafs(outLeafs : MutableList<ProofTreeNode>)
    {
        if (this.left == null && this.right == null)
        {
            outLeafs.add(this)
        }
        else
        {
            this.left?.findAllLeafs(outLeafs)
            this.right?.findAllLeafs(outLeafs)
        }
    }

    fun findAllLeafsWithPaths(outLeafs : MutableList<Pair<ProofTreeNode, ProofTreePath>>, outPath : ProofTreePath)
    {
        if (this.left == null && this.right == null)
        {
            outLeafs.add(Pair(this, outPath))
        }
        else
        {
            this.left?.findAllLeafsWithPaths(outLeafs, outPath.plus(this.left!!))
            this.right?.findAllLeafsWithPaths(outLeafs, outPath.plus(this.right!!))
        }
    }

    fun findPathUntilNode(targetNode : ProofTreeNode, outPath : ProofTreePath) : ProofTreePath?
    {
        if (this.left == targetNode || this.right == targetNode)
        {
            return outPath.plus(targetNode)
        }

        val foundPathOnLeft = this.left?.findPathUntilNode(targetNode, outPath.plus(this.left!!))
        val foundPathOnRight = this.right?.findPathUntilNode(targetNode, outPath.plus(this.right!!))
        return foundPathOnLeft ?: foundPathOnRight
    }

    fun getTotalNumberOfChildNodes() : Int
    {
        return getTotalNumberOfChildNodesImpl() - 1 //decrement: start node is not child node
    }

    private fun getTotalNumberOfChildNodesImpl() : Int
    {
        val nodeCountOnLeft = this.left?.getTotalNumberOfChildNodesImpl()?:0
        val nodeCountOnRight = this.right?.getTotalNumberOfChildNodesImpl()?:0
        return 1 + nodeCountOnLeft + nodeCountOnRight
    }

    fun printAsSubtreeToStringBuilder(stringBuilder : StringBuilder)
    {
        printAsSubtreeToStringBuilderImpl(stringBuilder, indent = 0)
    }

    private fun printAsSubtreeToStringBuilderImpl(stringBuilder : StringBuilder, indent : Int)
    {
        val prefix = if (indent == 0) ""
        else "├${(0 until indent).map { "──" }.joinToString("")} "

        val postfix = if (this.isContradictory) " X" else ""
        stringBuilder.append(prefix).append(formula).append(postfix).append('\n')

        if (!this.isContradictory && this.left != null && this.right != null)
        {
            this.left!!.printAsSubtreeToStringBuilderImpl(stringBuilder, indent = indent+1)
            this.right!!.printAsSubtreeToStringBuilderImpl(stringBuilder, indent = indent+1)
        }
        else if (!this.isContradictory && this.left != null)
        {
            this.left!!.printAsSubtreeToStringBuilderImpl(stringBuilder, indent)
        }
    }
}
