package com.deduction

import java.util.LinkedList
import java.util.Queue

fun proove(premises : List<IFormula>, conclusion : IFormula) : ProofTree
{
    val proofTree = buildInitialProofTree(premises, conclusion)

    val decompositionQueue : Queue<ProofTreeNode> = LinkedList()
    decompositionQueue.addAll(proofTree)

    while (!decompositionQueue.isEmpty())
    {
        val node = decompositionQueue.remove()!!

        val subtree = RULES.find { rule -> rule.isApplicable(node) }?.apply(node)
        if (subtree != null)
        {
            proofTree.append(subtree)

            if (subtree.left != null && RULES.any { rule -> rule.isApplicable(subtree.left) })
            {
                decompositionQueue.add(subtree.left)
            }

            if (subtree.right != null && RULES.any { rule -> rule.isApplicable(subtree.right) })
            {
                decompositionQueue.add(subtree.right)
            }
        }
    }

    for ((leaf, path) in proofTree.getAllLeafsWithPaths())
    {
        if (path.isContradictory())
        {
            leaf.isContradictory = true
        }
    }

    if (proofTree.getAllLeafs().all { leaf -> leaf.isContradictory })
    {
        proofTree.isProofCorrect = true
    }

    return proofTree
}

private fun buildInitialProofTree(premises : List<IFormula>, conclusion : IFormula) : ProofTree
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

    node.left = ProofTreeNode(UnaryFormula(Operation.Non, conclusion))
    return ProofTree(rootNode)
}

private fun Queue<ProofTreeNode>.addAll(proofTree : ProofTree) = addAllImpl(proofTree.rootNode)
private fun Queue<ProofTreeNode>.addAllImpl(proofTreeNode : ProofTreeNode)
{
    proofTreeNode.left?.let(this::addAllImpl)
    proofTreeNode.right?.let(this::addAllImpl)
    add(proofTreeNode)
}
