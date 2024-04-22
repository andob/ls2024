class ProofTreePath(val nodes : List<ProofTreeNode>)
{
    fun isContradictory() : Boolean
    {
        if (nodes.size >= 2)
        {
            val isLastNodeContradictory = isContradictoryAtNodeIndex(nodes.size-1)
            val isLastButNotLeastNodeContradictory = isContradictoryAtNodeIndex(nodes.size-2)
            return isLastNodeContradictory || isLastButNotLeastNodeContradictory
        }

        return false
    }

    private fun isContradictoryAtNodeIndex(index : Int) : Boolean
    {
        val leafFormulaRaw = nodes[index].formula
        val (leafFormula, leafFormulaValue) = when
        {
            /* P*/ leafFormulaRaw is AtomicFormula -> Pair(leafFormulaRaw, true)
            /*~P*/ leafFormulaRaw is ComplexFormula && leafFormulaRaw.operation == Operation.Non &&
                   leafFormulaRaw.x is AtomicFormula -> Pair(leafFormulaRaw.x, false)
            else -> return false //tree is not yet completely expanded here
        }

        if (leafFormulaValue)
        {
            //leaf is P = true, find node where P = false
            for (index in 0 until index)
            {
                val node = nodes[index]
                if (node.formula is ComplexFormula && node.formula.operation == Operation.Non &&
                    node.formula.x is AtomicFormula && node.formula.x == leafFormula)
                {
                    return true
                }
            }
        }
        else
        {
            //leaf is P = false, find node where P = true
            for (index in 0 until index)
            {
                val node = nodes[index]
                if (node.formula is AtomicFormula && node.formula == leafFormula)
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

    fun getWorldToBeForked() : PossibleWorld
    {
        return nodes.maxOf { node -> node.formula.possibleWorld }
    }

    fun getAllForkedWorlds() : List<PossibleWorld>
    {
        return nodes.filter { node -> node.formula.possibleWorld.index>0 }
                .map { node -> node.formula.possibleWorld }
                .distinct().sortedDescending()
    }

    override fun toString() : String
    {
        return nodes.joinToString(separator = " -> ")
    }
}
