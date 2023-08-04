//package io.github.natanfudge.hardcraft.algorithms
//
//import kotlin.properties.ReadOnlyProperty
//import kotlin.reflect.KProperty
//
//
//class Graph(val ) {
//    val vertices: List<Vertex>
//    val edges: List<Edge>
//
//    fun neighborsOf(vertex: Vertex): List<Vertex>
//    fun edgesOf(vertex: Vertex): List<Edge>
//
////    companion object {
////        fun Builder(directed: Boolean) = if (directed) Builder.Directed() else Builder.Undirected()
////    }
//
//    class Builder {
//        private val vertices = mutableSetOf<VertexTag>()
//        private val edges = mutableSetOf<EdgeTag>()
//        fun addVertex(vertex: VertexTag): Boolean {
//            if (vertex in vertices) return false
//            return vertices.add(vertex)
//        }
//
//        operator fun contains(vertex: Vertex) = vertex.tag in vertices
//
//        fun addVertex(vertex: Vertex) = addVertex(vertex.tag)
//
//        fun addEdges(vararg edges: EdgeTag) {
//            for (edge in edges) addEdge(edge)
//        }
//
//        infix fun VertexTag.edgeTo(other: VertexTag): EdgeTag {
//            return addEdge(this, other)
//        }
//
//        fun VertexTag.to(vararg others: VertexTag) = others.map { addEdge(this, it) }
//
//        fun addEdge(start: VertexTag, end: VertexTag) = addEdge(EdgeTag(start, end))
//        fun addEdge(start: Vertex, end: Vertex) = addEdge(start.tag, end.tag)
//
//        fun addEdge(edge: EdgeTag): EdgeTag {
//            require(edge.start != edge.end) {
//                "Edge is a loop on vertex ${edge.start}, and loops are not supported"
//            }
//            vertices.add(edge.start)
//            vertices.add(edge.end)
//            edges.add(edge)
//            return edge
//        }
//
//        fun addEdge(edge: Edge) = addEdge(edge.start.tag, edge.end.tag)
//
//        fun buildUndirected() = buildImpl(directed = false)
//        fun buildDirected() = buildImpl(directed = true).withAttribute(GraphAttribute.Directed)
//
//        fun build(directed: Boolean) = if (directed) buildDirected() else buildUndirected()
//
//        private fun buildImpl(directed: Boolean): Graph {
//            val verticesOrdered = vertices.sortedBy { it.name }
//            val vertexToIndex = buildMap {
//                verticesOrdered.forEachIndexed { index, tag -> put(tag, index) }
//            }
//
//            fun VertexTag.build() = Vertex(tag = this, index = vertexToIndex.getValue(this))
//            fun EdgeTag.build(): Edge {
//                return Edge(start = start.build(), end = end.build())
//            }
//
//            val actualEdges = if (directed) edges else edges.deduplicateIdenticalUndirectedEdges()
//
//            return LinkedListGraph(
//                verticesOrdered.map { it.build() },
//                actualEdges.map { it.build() },
//                directed
//            )
//        }
//
//        // a -> b and b -> a is the same in an undirected graph
//        private fun Set<EdgeTag>.deduplicateIdenticalUndirectedEdges(): List<EdgeTag> {
//            // By representing edges as a set of the start and end, a -> b and b -> a will be the same set.
//            val deduplicated = map { (start, end) -> setOf(start, end) }.toSet()
//            return deduplicated.map {
//                val (start, end) = it.toList()
//                EdgeTag(start, end)
//            }
//        }
//    }
//}
//
//fun Graph.validatePath(path: List<EdgeTag>) {
//    val existingEdges = edges.map { it.tag }.toHashSet()
//    for (edge in path) {
//        if (edge !in existingEdges) {
//            throw IllegalArgumentException("Path ${path.pathToString()} is invalid: it contains non-existent edge ${edge.start}->${edge.end}.")
//        }
//    }
//}
//
//
//fun Graph.matchingEdge(tag: EdgeTag) =     edges.find { it.tag == tag }
//    ?: throw IllegalArgumentException("No such edge $tag in graph.")
//fun Graph.matchingVertex(tag: VertexTag) =     vertices.find { it.tag == tag }
//    ?: throw IllegalArgumentException("No such vertex $tag in graph.")
//
//fun List<EdgeTag>.bindTo(graph: Graph) : List<Edge> {
//    graph.validatePath(this)
//    val edges = this.toHashSet()
//    return graph.edges.filter { it.tag in edges }
//}
//
//fun buildGraph(directed: Boolean, builder: Graph.Builder.() -> Unit): Graph =
//    Graph.Builder().apply(builder).build(directed)
//
//fun buildDirectedGraph(builder: Graph.Builder.() -> Unit): DirectedGraph =
//    Graph.Builder().apply(builder).buildDirected()
//
//fun buildUndirectedGraph(builder: Graph.Builder.() -> Unit): Graph =
//    buildGraph(false, builder)
//
//fun buildWeightedGraph(directed: Boolean,defaultWeight: Int = 1, builder: context(Graph.Builder, WeightsBuilder)() -> Unit): WeightedGraph {
//    val graphBuilder = Graph.Builder()
//    val weightBuilder= WeightsBuilder()
//    builder(graphBuilder,weightBuilder)
//    val graph = graphBuilder.build(directed)
//    val weights = graph.edges.map { it.tag }.associateWith { defaultWeight } + weightBuilder.build()
//    return graph.withWeightTags(weights)
//}
//
//fun buildWeightedDirectedGraph(defaultWeight: Int = 1, builder: context(Graph.Builder, WeightsBuilder) () -> Unit): WeightedDirectedGraph
//= buildWeightedGraph(directed = true, defaultWeight, builder) as WeightedDirectedGraph
//
//class WeightsBuilder {
//    private val weights: MutableMap<EdgeTag,Int> = mutableMapOf()
//    fun EdgeTag.weighing(weight: Int) {
//        weights[this@weighing] = weight
//    }
//
//    fun List<EdgeTag>.weighing(vararg weights: Int) {
//        require(this.size == weights.size)
//        zip(weights.toList()).forEach { (tag, weight) -> tag.weighing(weight) }
//    }
//    fun build(): Map<EdgeTag, Int> = weights
//}
//
//
//class LinkedListGraph(
//    override val vertices: List<Vertex>,
//    override val edges: List<Edge>,
//    private val directed: Boolean
//) : Graph {
//
//    private val neighbors: Map<Vertex, List<Edge>> = run {
//        val map = vertices.associateWith { mutableListOf<Edge>() }
//
//        for (edge in edges) {
//            map[edge.start]!!.add(edge)
//            if (!directed) map[edge.end]!!.add(Edge(start = edge.end, end = edge.start))
//        }
//        map
//    }
//
//    override fun neighborsOf(vertex: Vertex): List<Vertex> = neighbors[vertex]!!.map { it.end }
//    override fun edgesOf(vertex: Vertex): List<Edge> = neighbors[vertex]!!
//
//    override fun toString(): String {
//        return "${vertices.size} vertices, ${edges.size} edges"
//    }
//
//
//}
//
//
//data class Vertex(val color: Color, val index: Int, val name: String) {
//    constructor(tag: VertexTag, index: Int) : this(tag.color, index, tag.name)
//
//    val tag = VertexTag(color, name)
//    override fun toString(): String = "($name)"
//    override fun equals(other: Any?): Boolean = other is Vertex && other.name == name
//    override fun hashCode(): Int = name.hashCode()
//}
//
//data class VertexTag(val color: Color, val name: String) {
//    override fun toString(): String = name
//}
//
//infix fun Color.named(name: String) = VertexTag(this, name)
//
//class VertexTagProperty(private val color: Color): ReadOnlyProperty<Any?,VertexTag>{
//    override fun getValue(thisRef: Any?, property: KProperty<*>): VertexTag {
//        return VertexTag(color, property.name)
//    }
//}
//
//fun vertex(color: Color) = VertexTagProperty(color)
//
//data class Edge(val start: Vertex, val end: Vertex) {
//    fun flipped() = Edge(end,start)
//    val tag = EdgeTag(start.tag, end.tag)
//}
//
//data class EdgeTag(val start: VertexTag, val end: VertexTag)
