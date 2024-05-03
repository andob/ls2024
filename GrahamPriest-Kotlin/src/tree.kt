class ProofTree
(
    val rootNode : ProofTreeNode
)
{
    private var isProofCorrect = false

    val nodeIdSequence = object : Iterator<Long>
    {
        private var id : Long = 0
        override fun hasNext() : Boolean = id < Long.MAX_VALUE
        override fun next() : Long = id++
    }

    val predicateArgumentInstanceNameSequence = object : Iterator<String>
    {
        private var char : Char = 'a'
        private var secondaryIndex : Long = 0
        override fun hasNext() : Boolean = (char < 'z' && secondaryIndex < Long.MAX_VALUE)
        override fun next() : String = if (char <= 'z') "${char++}" else "c${secondaryIndex++}"
    }

    fun appendSubtree(subtree : ProofSubtree, nodeId : Long)
    {
        for ((leaf, path) in getAllLeafsWithPaths())
        {
            if (!leaf.isContradictory && (leaf.id == nodeId || path.nodes.any { node -> node.id == nodeId }))
            {
                leaf.left = subtree.left?.cloned()
                leaf.right = subtree.right?.cloned()
            }
        }
    }

    fun checkForContradictions()
    {
        val leafs = getAllLeafsWithPaths()
        val numberOfContradictoryLeafs = leafs
            .filter { (_, path) -> path.isContradictory() }.size

        if (numberOfContradictoryLeafs == leafs.size)
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

    fun attachNodeFactory(nodeFactory : ProofTreeNodeFactory)
    {
        this.rootNode.attachNodeFactory(nodeFactory)
    }

    fun getParentNodeOfNode(node : ProofTreeNode) : ProofTreeNode?
    {
        val path = getPathFromRootNodeToNode(node)
        return path.nodes.getOrNull(path.nodes.size-2)
    }

    fun getPathFromRootNodeToNode(node : ProofTreeNode) : ProofTreePath
    {
        val initialPath = ProofTreePath(nodes = listOf(rootNode))
        val detectedPath = this.rootNode.findPathUntilNode(node, initialPath)
        return detectedPath ?: initialPath
    }

    fun getPathFromRootNodeToNodeIncludingLeafs(node : ProofTreeNode) : ProofTreePath
    {
        val paths = getAllLeafsWithPaths().map { (_, path) -> path }
        val foundPath = paths.find { path -> path.nodes.contains(node) }
        return foundPath ?: ProofTreePath(listOf(rootNode))
    }

    fun getAllForkedWorlds() : List<PossibleWorld>
    {
        return getAllLeafsWithPaths().flatMap { (_, path) -> path.getAllForkedWorlds() }.distinct().sortedDescending()
    }

    override fun toString() : String
    {
        val isProvedStr = if (this.isProofCorrect) "PROVED!" else "NOT PROVED!"

        val formatOptions = ProofTreeNode.PrintAsSubtreeFormatOptions(
            shouldShowPossibleWorlds = getAllForkedWorlds().isNotEmpty())

        val stringBuilder = StringBuilder()
        stringBuilder.append(isProvedStr).append('\n')
        this.rootNode.printAsSubtreeToStringBuilder(stringBuilder, formatOptions)
        return stringBuilder.toString()
    }
}

class ProofTreeNodeFactory(val tree : ProofTree)
{
    fun newNode(formula : IFormula, left : ProofTreeNode? = null, right : ProofTreeNode? = null) : ProofTreeNode
    {
        return ProofTreeNode(id = tree.nodeIdSequence.next(), formula, left, right, nodeFactory = this)
    }
}

class ProofTreeNode
(
    val id : Long,
    val formula : IFormula,
    var left : ProofTreeNode? = null,
    var right : ProofTreeNode? = null,
    var comment : String? = null,
    var nodeFactory : ProofTreeNodeFactory? = null,
)
{
    var isContradictory = false

    override fun hashCode() = hash(id, formula, left, right)
    override fun equals(other : Any?) : Boolean
    {
        return (other as? ProofTreeNode)?.let { that ->
            that.id == this.id && that.formula == this.formula
        }?:false
    }

    override fun toString() = formula.toString()

    fun cloned() : ProofTreeNode = ProofTreeNode(id, formula.cloned(), left?.cloned(), right?.cloned(), comment, nodeFactory)

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

    fun attachNodeFactory(nodeFactory : ProofTreeNodeFactory)
    {
        this.nodeFactory = nodeFactory
        this.left?.attachNodeFactory(nodeFactory)
        this.right?.attachNodeFactory(nodeFactory)
    }

    fun getAllChildNodes() : List<ProofTreeNode>
    {
        val outNodes = mutableListOf<ProofTreeNode>()
        findAllChildNodes(outNodes)
        return outNodes.toList()
    }

    private fun findAllChildNodes(outNodes : MutableList<ProofTreeNode>) : List<ProofTreeNode>
    {
        outNodes.add(this)
        this.left?.findAllChildNodes(outNodes)
        this.right?.findAllChildNodes(outNodes)
        return outNodes
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

    fun getParentNode() : ProofTreeNode?
    {
        val proofTree = this.nodeFactory!!.tree
        return proofTree.getParentNodeOfNode(this)
    }

    fun getPathFromRootNodeToNode() : ProofTreePath
    {
        val proofTree = this.nodeFactory!!.tree
        return proofTree.getPathFromRootNodeToNode(this)
    }

    fun getPathFromRootNodeToNodeIncludingLeafs() : ProofTreePath
    {
        val proofTree = this.nodeFactory!!.tree
        return proofTree.getPathFromRootNodeToNodeIncludingLeafs(this)
    }

    class PrintAsSubtreeFormatOptions
    (
        val shouldShowPossibleWorlds : Boolean,
        val shouldAlwaysIncrementIndent : Boolean = false,
    )

    fun printAsSubtreeToStringBuilder(stringBuilder : StringBuilder, options : PrintAsSubtreeFormatOptions, indent : Int = 0)
    {
        if (indent>0)
        {
            stringBuilder.append('├')
            stringBuilder.append((0 until indent).map { "──" }.joinToString(""))
            stringBuilder.append(' ')
        }

        stringBuilder.append(this.formula)

        if (!this.comment.isNullOrEmpty())
        {
            stringBuilder.append(' ').append(this.comment)
        }

        if (options.shouldShowPossibleWorlds)
        {
            stringBuilder.append(' ').append(this.formula.possibleWorld)
        }

        if (this.isContradictory)
        {
            stringBuilder.append(" X")
        }

        stringBuilder.append('\n')

        if (this.left != null && this.right != null)
        {
            this.left?.printAsSubtreeToStringBuilder(stringBuilder, options, indent+1)
            this.right?.printAsSubtreeToStringBuilder(stringBuilder, options, indent+1)
        }
        else if (this.left != null)
        {
            val newIndent = if (options.shouldAlwaysIncrementIndent) indent+1 else indent
            this.left?.printAsSubtreeToStringBuilder(stringBuilder, options, newIndent)
        }
    }
}

class ProofSubtree
(
    val left : ProofTreeNode? = null,
    val right : ProofTreeNode? = null,
)
{
    fun getAllChildNodes() : List<ProofTreeNode>
    {
        val nodesOnLeft = left?.getAllChildNodes() ?: listOf()
        val nodesOnRight = right?.getAllChildNodes() ?: listOf()
        return nodesOnLeft.plus(nodesOnRight)
    }
}
