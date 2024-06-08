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
            /*  P*/ targetNode.formula is AtomicFormula -> Pair(targetNode.formula, true)

            /* ¬P*/ targetNode.formula is ComplexFormula && targetNode.formula.operation == Operation.Non &&
                    targetNode.formula.x is AtomicFormula -> Pair(targetNode.formula.x, false)

            /* ◇P*/ targetNode.formula is ComplexFormula && targetNode.formula.operation is ModalOperation &&
                    targetNode.formula.x is AtomicFormula -> Pair(targetNode.formula, true)

            /*¬◇P*/ targetNode.formula is ComplexFormula && targetNode.formula.operation == Operation.Non &&
                    targetNode.formula.x is ComplexFormula && targetNode.formula.x.operation is ModalOperation &&
                    targetNode.formula.x.x is AtomicFormula -> Pair(targetNode.formula.x, false)

            else -> return false //tree is not yet completely expanded here
        }

        if (targetFormulaValue)
        {
            //leaf is P = true, find node where P = false
            for (j in 0 until targetIndex)
            {
                val node = nodes[j]
                if (node.formula is ComplexFormula && node.formula.operation == Operation.Non &&
                    node.formula.x is AtomicFormula && targetFormula is AtomicFormula &&
                    node.formula.x.isReplaceableWith(targetFormula))
                {
                    return true
                }

                if (node.formula is ComplexFormula && node.formula.operation == Operation.Non &&
                    node.formula.x is ComplexFormula && node.formula.x.operation is ModalOperation &&
                    targetFormula is ComplexFormula && targetFormula.operation is ModalOperation &&
                    node.formula.x.x is AtomicFormula && targetFormula.x is AtomicFormula &&
                    node.formula.x.x.isReplaceableWith(targetFormula.x))
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
                if (node.formula is AtomicFormula && targetFormula is AtomicFormula &&
                    node.formula.isReplaceableWith(targetFormula))
                {
                    return true
                }

                if (node.formula is ComplexFormula && node.formula.operation is ModalOperation &&
                    targetFormula is ComplexFormula && targetFormula.operation == Operation.Non &&
                    targetFormula.x is ComplexFormula && targetFormula.x.operation is ModalOperation &&
                    node.formula.x is AtomicFormula && targetFormula.x.x is AtomicFormula &&
                    node.formula.x.isReplaceableWith(targetFormula.x.x))
                {
                    return true
                }
            }
        }

        return false
    }

    fun plus(newNode : ProofTreeNode) : ProofTreePath
    {
        return ProofTreePath(nodes.plus(newNode))
    }

    fun plus(newNodes : List<ProofTreeNode>) : ProofTreePath
    {
        return ProofTreePath(nodes.plus(newNodes))
    }

    fun getRootNode() : ProofTreeNode
    {
        return nodes.firstOrNull()!!
    }

    fun getAllFormulas() : List<IFormula>
    {
        return nodes.map { node -> node.formula }
    }

    fun getAllPossibleWorlds() : List<PossibleWorld>
    {
        return nodes.map { node -> node.formula.possibleWorld }
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
