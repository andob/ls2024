package com.deduction

@Suppress("LocalVariableName")
fun main()
{
    val P = Predicate(name = "P")
    val Q = Predicate(name = "Q")
    val R = Predicate(name = "R")

    //P -> (Q v R), P ^ ~R ├─ Q
//    val premise1 = BinaryFormula(P, Operation.Imply, BinaryFormula(Q, Operation.Or, R))
//    val premise2 = BinaryFormula(P, Operation.And, UnaryFormula(Operation.Non, R))
//    val conclusion = Q

    //P -> Q, R -> Q ├─ (P v R) -> Q
    val premise1 = BinaryFormula(P, Operation.Imply, Q)
    val premise2 = BinaryFormula(R, Operation.Imply, Q)
    val conclusion = BinaryFormula(BinaryFormula(P, Operation.Or, R), Operation.Imply, Q)

    val proofTree = proove(premises = listOf(premise1, premise2), conclusion = conclusion)
    printProofTree(proofTree)
}

fun printProofTree(proofTree : ProofTree)
{
    println(if (proofTree.isProofCorrect) "PROVED!" else "NOT PROVED!")

    printProofTreeNode(proofTree.rootNode)
}

fun printProofTreeNode(node : ProofTreeNode, indent : Int = 0)
{
    val prefix = if (indent == 0) ""
    else "├${(0 until indent).map { "───" }.joinToString("")} "

    val postfix = if (node.isContradictory) " X" else ""

    println(prefix + node.formula + postfix)

    if (node.left != null && node.right != null)
    {
        printProofTreeNode(node.left!!, indent = indent+1)
        printProofTreeNode(node.right!!, indent = indent+1)
    }
    else if (node.left != null)
    {
        printProofTreeNode(node.left!!, indent+1)
    }
}
