fun main() {
    val graph =Graph();
    val n1=GraphNode(graph)
    val n2=GraphNode(graph)
    val n3=GraphNode(graph)

    graph.createEdge("new",n1,n2)
    graph.createEdge("new",n2,n3)
    println(graph.listEdges())
}