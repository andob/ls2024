class Graph<NODE : Comparable<NODE>>
private constructor
(
    val nodes : MutableList<NODE>,
    val vertices : MutableList<Vertex<NODE>>,
)
{
    class Vertex<NODE : Comparable<NODE>>(val from : NODE, val to : NODE)
    {
        operator fun component1() : NODE = from
        operator fun component2() : NODE = to

        override fun hashCode() = hash(from, to)
        override fun equals(other : Any?) : Boolean
        {
            return (other as? Vertex<*>)?.let { that ->
                that.from==this.from && that.to==this.to
            }?:false
        }

        override fun toString() : String
        {
            return "$from -> $to"
        }
    }

    companion object
    {
        fun <NODE : Comparable<NODE>> withNodes(nodes : List<NODE>) : Graph<NODE>
        {
            return Graph(nodes.toMutableList(), vertices = mutableListOf())
        }
    }
    
    fun addVertex(from : NODE, to : NODE)
    {
        vertices.add(Vertex(from, to))
    }

    fun invertAllVertices()
    {
        val invertedVertices = vertices.map { vertex -> Vertex(vertex.to, vertex.from) }

        vertices.clear()
        vertices.addAll(invertedVertices)
    }

    fun addMissingReflexiveVertices()
    {
        for (node in nodes)
        {
            val reflexiveVertex = Vertex(node, node)
            if (!vertices.contains(reflexiveVertex))
            {
                vertices.add(reflexiveVertex)
            }
        }
    }

    fun addMissingSymmetricVertices()
    {
        val verticesToAdd = mutableListOf<Vertex<NODE>>()

        for (vertex in vertices)
        {
            val symmetricVertex = Vertex(vertex.to, vertex.from)
            verticesToAdd.add(symmetricVertex)
        }

        for (vertexToAdd in verticesToAdd)
        {
            if (!vertices.contains(vertexToAdd))
            {
                vertices.add(vertexToAdd)
            }
        }
    }

    fun addMissingTransitiveVertices()
    {
        val verticesToAdd = mutableListOf<Vertex<NODE>>()

        for (iVertex in vertices)
        {
            for (jVertex in vertices)
            {
                if (iVertex!=jVertex && iVertex.to==jVertex.from)
                {
                    val transitiveVertex = Vertex(iVertex.from, jVertex.to)
                    verticesToAdd.add(transitiveVertex)
                }
            }
        }

        for (vertexToAdd in verticesToAdd)
        {
            if (!vertices.contains(vertexToAdd))
            {
                vertices.add(vertexToAdd)
            }
        }
    }

    fun addMissingTemporalConvergenceVertices()
    {
        val verticesToAdd = mutableListOf<Vertex<NODE>>()

        for (iVertex in vertices)
        {
            for (jVertex in vertices)
            {
                if (iVertex!=jVertex && iVertex.from==jVertex.from)
                {
                    val convergentVertex = Vertex(iVertex.to, jVertex.to)
                    verticesToAdd.add(convergentVertex)
                }
            }
        }

        for (vertexToAdd in verticesToAdd)
        {
            if (!vertices.contains(vertexToAdd))
            {
                vertices.add(vertexToAdd)
            }
        }
    }

    class IterationArgs<NODE : Comparable<NODE>>
    (
        val startNode : NODE,
        val startVertices : List<Vertex<NODE>> = listOf(),
    )

    fun iterate(args : IterationArgs<NODE>) : List<Vertex<NODE>>
    {
        val outputList = mutableListOf<Vertex<NODE>>()
        iterate(args, callback = { vertex -> outputList.add(vertex) })
        return outputList
    }

    fun iterate(args : IterationArgs<NODE>, callback : (Vertex<NODE>) -> Unit)
    {
        val alreadyVisitedNodes = mutableSetOf<NODE>()
        val alreadyVisitedVertices = mutableSetOf<Vertex<NODE>>()

        val nodesToVisit = mutableListOf(args.startNode)

        for (startVertex in args.startVertices)
        {
            if (startVertex.from==args.startNode && vertices.contains(startVertex))
            {
                alreadyVisitedVertices.add(startVertex)
                callback.invoke(startVertex)
                nodesToVisit.add(startVertex.from)
                nodesToVisit.add(startVertex.to)
            }
        }

        fun visitVertices(verticesToVisit : List<Vertex<NODE>>)
        {
            for (vertex in verticesToVisit)
            {
                if (!alreadyVisitedVertices.contains(vertex))
                {
                    alreadyVisitedVertices.add(vertex)
                    callback.invoke(vertex)

                    if (!alreadyVisitedNodes.contains(vertex.from))
                    {
                        nodesToVisit.add(vertex.from)
                    }

                    if (!alreadyVisitedNodes.contains(vertex.to))
                    {
                        nodesToVisit.add(vertex.to)
                    }
                }
            }
        }

        while (nodesToVisit.isNotEmpty())
        {
            //prevent infinite graph iteration
            if (alreadyVisitedNodes.size == this.nodes.size) { return }
            if (alreadyVisitedVertices.size == this.vertices.size) { return }

            val targetNode = nodesToVisit.removeLast()
            if (!alreadyVisitedNodes.contains(targetNode))
            {
                alreadyVisitedNodes.add(targetNode)

                val reflexiveVerticesToVisit = vertices.filter { vertex ->
                    vertex.from==targetNode && vertex.to==targetNode
                }

                visitVertices(reflexiveVerticesToVisit)

                val nonReflexiveVerticesToVisit = vertices.filter { vertex ->
                    vertex.from==targetNode && vertex.to!=targetNode
                }

                visitVertices(nonReflexiveVerticesToVisit)
            }
        }
    }
}
