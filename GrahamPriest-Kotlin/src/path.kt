class ProofTreePath(val nodes : List<ProofTreeNode>)
{
    fun isContradictory() : Boolean
    {
        val contradictoryNodeIndices = nodes.indices.filter { index -> isContradictoryAtIndex(index) }
        contradictoryNodeIndices.forEach { index -> nodes[index].isContradictory = true }
        return contradictoryNodeIndices.isNotEmpty()
    }

    private fun isContradictoryAtIndex(targetIndex : Int) : Boolean
    {
        val targetNode = nodes[targetIndex]
        val (targetFormula, targetFormulaValue) = when
        {
            /* P*/ targetNode.formula is AtomicFormula -> Pair(targetNode.formula, true)
            /*~P*/ targetNode.formula is ComplexFormula && targetNode.formula.operation == Operation.Non &&
                    targetNode.formula.x is AtomicFormula -> Pair(targetNode.formula.x, false)
            else -> return false //tree is not yet completely expanded here
        }

        if (targetFormulaValue)
        {
            //leaf is P = true, find node where P = false
            for (j in 0 until targetIndex)
            {
                val node = nodes[j]
                if (node.formula is ComplexFormula && node.formula.operation == Operation.Non &&
                    node.formula.x is AtomicFormula && node.formula.x.isReplaceableWith(targetFormula))
                {
                    return true
                }
            }
        }
        else
        {
            //leaf is P = false, find node where P = true
            for (j in 0 until targetIndex)
            {
                val node = nodes[j]
                if (node.formula is AtomicFormula && node.formula.isReplaceableWith(targetFormula))
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

    fun getAllInstantiatedPredicateArguments() : List<BindingPredicateArgument>
    {
        return nodes.flatMap { node -> node.formula.findAllAtoms() }
                .flatMap { atomicFormula -> atomicFormula.arguments }
                .filterIsInstance<BindingPredicateArgument>()
                .filter { argument -> argument.isInstantiated() }
                .distinct().sortedDescending()
    }

    override fun toString() : String
    {
        return nodes.joinToString(separator = " -> ")
    }
}
