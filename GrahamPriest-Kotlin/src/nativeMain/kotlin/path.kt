class ProofTreePath(val nodes : List<ProofTreeNode>)
{
    fun isContradictory() : Boolean
    {
        if (nodes.isEmpty())
        {
            return false
        }

        val leafFormula = nodes.last().formula

        val (leafPredicate, leafPredicateValue) = when
        {
            /* P*/ leafFormula is AtomicFormula -> Pair(leafFormula, true)
            /*~P*/ leafFormula is ComplexFormula && leafFormula.operation == Operation.Non &&
                   leafFormula.x is AtomicFormula -> Pair(leafFormula.x, false)
            else -> return false //tree is not yet completely expanded here
        }

        if (leafPredicateValue)
        {
            //leaf is P = true, find node where P = false
            for (index in 0 until nodes.size-1)
            {
                val node = nodes[index]
                if (node.formula is ComplexFormula && node.formula.operation == Operation.Non &&
                    node.formula.x is AtomicFormula && node.formula.x == leafPredicate)
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
                if (node.formula is AtomicFormula && node.formula == leafPredicate)
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

    fun size() : Int
    {
        return nodes.size
    }

    fun getOrNull(index : Int) : ProofTreeNode?
    {
        return nodes.getOrNull(index)
    }

    override fun toString() : String
    {
        return nodes.joinToString(separator = " -> ")
    }
}
