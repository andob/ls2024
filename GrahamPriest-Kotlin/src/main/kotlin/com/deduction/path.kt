package com.deduction

class ProofTreePath(val nodes : List<ProofTreeNode>)
{
    fun isContradictory() : Boolean
    {
        if (nodes.isEmpty())
        {
            return false
        }

        val leafFormula = nodes.last().formula
        if (leafFormula is BinaryFormula)
        {
            throw RuntimeException("The tree is not completely expanded!")
        }

        if (leafFormula is UnaryFormula && leafFormula.operation == Operation.Non && leafFormula.x !is Predicate)
        {
            throw RuntimeException("The tree is not completely expanded!")
        }

        val (leafPredicate, leafPredicateValue) = when (leafFormula)
        {
            is Predicate -> Pair(leafFormula, true)
            is UnaryFormula -> Pair(leafFormula.x as Predicate, false)
            else -> throw RuntimeException("The tree is not completely expanded!")
        }

        if (leafPredicateValue)
        {
            //leaf is P = true, find node where P = false
            for (index in 0 until nodes.size-1)
            {
                val node = nodes[index]
                if (node.formula is UnaryFormula && node.formula.operation == Operation.Non &&
                    node.formula.x is Predicate && node.formula.x == leafPredicate)
                {
                    return true
                }
            }
        }
        else
        {
            //leaf is P = false, find node where P = true
            for (index in 0 until nodes.size-1)
            {
                val node = nodes[index]
                if (node.formula is Predicate && node.formula == leafPredicate)
                {
                    return true
                }
            }
        }

        return false
    }

    fun plus(node : ProofTreeNode) : ProofTreePath
    {
        return ProofTreePath(nodes.plus(node))
    }
}
