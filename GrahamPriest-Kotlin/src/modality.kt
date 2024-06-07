class PossibleWorld(val index : Int) : Comparable<PossibleWorld>
{
    fun fork() : PossibleWorld = PossibleWorld(index = index+1)

    override fun compareTo(other : PossibleWorld) : Int = this.index.compareTo(other.index)
    override fun equals(other : Any?) = (other as? PossibleWorld)?.index == index
    override fun hashCode() = index.hashCode()
    override fun toString() = "w$index"
}

enum class ModalLogicType
(
    val isReflexive : Boolean, //ρ
    val isSymmetric : Boolean, //σ
    val isTransitive : Boolean, //τ
    val isExtendable : Boolean, //η
    val isTemporal : Boolean,
    val isNormal : Boolean,
)
{
    K(isReflexive = false, isSymmetric = false, isTransitive = false, isExtendable = false, isTemporal = false, isNormal = true),
    Kᵗ(isReflexive = false, isSymmetric = false, isTransitive = false, isExtendable = false, isTemporal = true, isNormal = true),
    T(isReflexive = true, isSymmetric = false, isTransitive = false, isExtendable = false, isTemporal = false, isNormal = true),
    D(isReflexive = false, isSymmetric = false, isTransitive = false, isExtendable = true, isTemporal = false, isNormal = true),
    B(isReflexive = true, isSymmetric = true, isTransitive = false, isExtendable = false, isTemporal = false, isNormal = true),
    N(isReflexive = false, isSymmetric = false, isTransitive = false, isExtendable = false, isTemporal = false, isNormal = false),
    S2(isReflexive = true, isSymmetric = false, isTransitive = false, isExtendable = false, isTemporal = false, isNormal = false),
    S3(isReflexive = true, isSymmetric = false, isTransitive = true, isExtendable = false, isTemporal = false, isNormal = false),
    S35(isReflexive = true, isSymmetric = true, isTransitive = true, isExtendable = false, isTemporal = false, isNormal = false),
    S4(isReflexive = true, isSymmetric = false, isTransitive = true, isExtendable = false, isTemporal = false, isNormal = true),
    S5(isReflexive = true, isSymmetric = true, isTransitive = true, isExtendable = false, isTemporal = false, isNormal = true),
}

class NotPossibleRule : IRule
{
    override fun isApplicable(logic : ILogic, node : ProofTreeNode) : Boolean
    {
        return (node.formula as? ComplexFormula)?.operation == Operation.Non &&
                ((node.formula as? ComplexFormula)?.x as? ComplexFormula)?.operation is Operation.Possible
    }

    override fun wouldBranchTheTree() = false

    override fun apply(factory : RuleApplyFactory, node : ProofTreeNode) : ProofSubtree
    {
        val p = ((node.formula as ComplexFormula).x as ComplexFormula).x
        val possible = (node.formula.x as ComplexFormula).operation as Operation.Possible
        val necessary = possible.convertedToNecessary()
        val necessaryNonP = factory.newFormula(necessary, factory.newFormula(Operation.Non, p))
        return ProofSubtree(left = factory.newNode(necessaryNonP))
    }

    private fun Operation.Possible.convertedToNecessary() = when(this)
    {
        is Operation.Possible.InFuture -> Operation.Necessary.InFuture()
        is Operation.Possible.InPast -> Operation.Necessary.InPast()
        else -> Operation.Necessary()
    }
}

class NotNecessaryRule : IRule
{
    override fun isApplicable(logic : ILogic, node : ProofTreeNode) : Boolean
    {
        return (node.formula as? ComplexFormula)?.operation == Operation.Non &&
                ((node.formula as? ComplexFormula)?.x as? ComplexFormula)?.operation is Operation.Necessary
    }

    override fun wouldBranchTheTree() = false

    override fun apply(factory : RuleApplyFactory, node : ProofTreeNode) : ProofSubtree
    {
        val p = ((node.formula as ComplexFormula).x as ComplexFormula).x
        val necessary = (node.formula.x as ComplexFormula).operation as Operation.Necessary
        val possible = necessary.convertedToPossible()
        val possibleNonP = factory.newFormula(possible, factory.newFormula(Operation.Non, p))
        return ProofSubtree(left = factory.newNode(possibleNonP))
    }

    private fun Operation.Necessary.convertedToPossible() = when(this)
    {
        is Operation.Necessary.InFuture -> Operation.Possible.InFuture()
        is Operation.Necessary.InPast -> Operation.Possible.InPast()
        else -> Operation.Possible()
    }
}

class PossibleRule : IRule
{
    override fun isApplicable(logic : ILogic, node : ProofTreeNode) : Boolean
    {
        val formulaIsPossible = (node.formula as? ComplexFormula)?.operation is Operation.Possible
        if (formulaIsPossible && !(logic as FirstOrderModalLogic).type.isNormal)
        {
            val path = node.getPathFromRootToLeafsThroughNode()
            val pathIsInhabitedWithNecessary = path.getAllFormulas().any { formula ->
                formula is ComplexFormula && formula.operation is Operation.Necessary &&
                formula.possibleWorld == node.formula.possibleWorld
            }

            return node.formula.possibleWorld.index==0 || pathIsInhabitedWithNecessary
        }

        return formulaIsPossible
    }

    override fun wouldBranchTheTree() = false

    override fun apply(factory : RuleApplyFactory, node : ProofTreeNode) : ProofSubtree
    {
        val modalLogic = factory.getLogic() as FirstOrderModalLogic
        val originalSubFormula = (node.formula as ComplexFormula).x
        val originalOperation = node.formula.operation as ModalOperation

        val path = node.getPathFromRootToLeafsThroughNode()
        val forkedWorld = path.nodes.maxOf { it.formula.possibleWorld }.fork()

        val newNode = factory.newNode(originalSubFormula.inWorld(forkedWorld))

        val modalRelationDescriptor = if (modalLogic.type.isTemporal && originalOperation.isInverted)
            factory.newModalRelationDescriptor(fromWorld = forkedWorld, toWorld = node.formula.possibleWorld)
        else factory.newModalRelationDescriptor(fromWorld = node.formula.possibleWorld, toWorld = forkedWorld)

        val newDescriptorNode = factory.newNode(modalRelationDescriptor)
        newDescriptorNode.left = newNode
        return ProofSubtree(left = newDescriptorNode)
    }
}

class NecessaryRule : IRule
{
    override fun isApplicable(logic : ILogic, node : ProofTreeNode) : Boolean
    {
        return (node.formula as? ComplexFormula)?.operation is Operation.Necessary
    }

    override fun wouldBranchTheTree() = false

    override fun apply(factory : RuleApplyFactory, node : ProofTreeNode) : ProofSubtree
    {
        val modalLogic = factory.getLogic() as FirstOrderModalLogic
        val originalSubFormula = (node.formula as ComplexFormula).x
        val originalOperation = node.formula.operation as Operation.Necessary

        val path = node.getPathFromRootToLeafsThroughNode()
        val graph = buildNecessityGraph(modalLogic, originalOperation, path)

        if (graph.vertices.isEmpty())
        {
            return if (modalLogic.type.isExtendable)
                PossibleRule().apply(factory, node)
            else ProofSubtree.empty()
        }

        val graphIterationArgs = Graph.IterationArgs(startNode = originalSubFormula.possibleWorld,
            startVertices = buildGraphIterationArgsStartVertices(path, node))
        val iteratedGraph = graph.iterate(graphIterationArgs)

        val outputNodes = mutableListOf<ProofTreeNode>()
        for ((fromWorld, toWorld) in iteratedGraph)
        {
            outputNodes.add(factory.newNode(factory.newModalRelationDescriptor(fromWorld, toWorld)))
            outputNodes.add(factory.newNode(originalSubFormula.inWorld(toWorld)))

            //already contradiction - do not iterate the graph further
            if (path.plus(outputNodes).isContradictory()) { break }
        }

        if (!modalLogic.previousResultsOfNecessaryRule.contains(node, outputNodes))
        {
            modalLogic.previousResultsOfNecessaryRule.add(node, outputNodes)
            return ProofSubtree.newWithSequentialVerticalNodesOnLeft(outputNodes)
        }

        return ProofSubtree.empty()
    }

    private fun buildNecessityGraph(modalLogic : FirstOrderModalLogic, operation : Operation.Necessary, path : ProofTreePath) : Graph<PossibleWorld>
    {
        val graph = Graph.withNodes(path.getAllPossibleWorlds())

        for (formula in path.getAllFormulas().filterIsInstance<ModalRelationDescriptorFormula>())
        {
            graph.addVertex(formula.fromWorld, formula.toWorld)
        }

        if (modalLogic.type.isTemporal && operation.isInverted)
        {
            graph.invertAllVertices()
        }

//        if (modalLogic.type.isTemporal)
//        {
//            graph.addMissingTemporalConvergenceVertices()
//        }

        if (modalLogic.type.isReflexive)
        {
            graph.addMissingReflexiveVertices()
        }

        if (modalLogic.type.isSymmetric)
        {
            graph.addMissingSymmetricVertices()
        }

        if (modalLogic.type.isTransitive)
        {
            graph.addMissingTransitiveVertices()
        }

        return graph
    }

    private fun buildGraphIterationArgsStartVertices(path : ProofTreePath, node : ProofTreeNode) : List<Graph.Vertex<PossibleWorld>>
    {
        val allDescriptors = path.getAllFormulas().filterIsInstance<ModalRelationDescriptorFormula>()

        val fromWorld = node.formula.possibleWorld
        val accessibleWorlds = path.getAllPossibleWorlds().filter { toWorld ->
            allDescriptors.any { descriptor ->
                descriptor.fromWorld == fromWorld && descriptor.toWorld == toWorld
            }
        }

        return accessibleWorlds.map { toWorld -> Graph.Vertex(fromWorld, toWorld) }
    }

    class PreviousResults
    {
        private val data : MutableMap<ProofTreeNode, MutableList<List<ProofTreeNode>>> = mutableMapOf()

        fun add(inputNode : ProofTreeNode, outputNodes : List<ProofTreeNode>)
        {
            data[inputNode] = data[inputNode] ?: mutableListOf()
            data[inputNode]!!.add(outputNodes)
        }

        fun contains(inputNode : ProofTreeNode, outputNodes : List<ProofTreeNode>) : Boolean
        {
            for (expectedOutputNodes in data[inputNode] ?: listOf())
            {
                if (expectedOutputNodes.size == outputNodes.size)
                {
                    var numberOfSameFormulas = 0
                    for (i in expectedOutputNodes.indices)
                    {
                        if (outputNodes[i].formula == expectedOutputNodes[i].formula)
                            numberOfSameFormulas++
                    }

                    if (numberOfSameFormulas == expectedOutputNodes.size)
                        return true
                }
            }

            return false
        }

        fun clear()
        {
            data.clear()
        }
    }
}

fun FormulaFactory.inWorld(possibleWorld : PossibleWorld) : FormulaFactory
{
    val original = this
    return FormulaFactory(original.logic, possibleWorld)
}

fun IFormula.inWorld(possibleWorld : PossibleWorld) : IFormula
{
    return when(val original = this)
    {
        is AtomicFormula -> AtomicFormula(original.name, original.arguments, possibleWorld, original.formulaFactory.inWorld(possibleWorld))
        is ComplexFormula -> ComplexFormula(original.x.inWorld(possibleWorld), original.operation, original.y?.inWorld(possibleWorld),
                                possibleWorld, original.formulaFactory.inWorld(possibleWorld))
        else -> original.cloned()
    }
}
