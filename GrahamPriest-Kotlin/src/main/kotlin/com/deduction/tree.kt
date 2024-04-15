package com.deduction

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
}

class ProofTreeNode
(
    val formula : IFormula,
    var left : ProofTreeNode? = null,
    var right : ProofTreeNode? = null,
)
{
    var isContradictory = false

    fun findAllLeafs(leafs : MutableList<ProofTreeNode>)
    {
        if (this.left == null && this.right == null)
        {
            leafs.add(this)
        }
        else
        {
            this.left?.findAllLeafs(leafs)
            this.right?.findAllLeafs(leafs)
        }
    }

    fun findAllLeafsWithPaths(leafs : MutableList<Pair<ProofTreeNode, ProofTreePath>>, path : ProofTreePath)
    {
        if (this.left == null && this.right == null)
        {
            leafs.add(Pair(this, path))
        }
        else
        {
            this.left?.findAllLeafsWithPaths(leafs, path.plus(this.left!!))
            this.right?.findAllLeafsWithPaths(leafs, path.plus(this.right!!))
        }
    }
}
