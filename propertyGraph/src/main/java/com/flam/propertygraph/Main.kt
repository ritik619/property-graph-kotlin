interface IPerson {
    val name: String
    val age: Int
    val friends: MutableList<Person>;
    val pet: Pet;
}
enum class Animal{
DOG,
CAT }
interface IPet {
    val type : Animal
    val name: String
}

class Person(graph: Graph) : GraphNode<IPerson>(graph) {
    fun getDefaults(): Nullable<IPerson> {
        return mutableMapOf("name" to  ' ' , "age" to 0, "friends" to  emptyList<String>(), "pet" to  null)
    }
}
class Pet(graph: Graph) : GraphNode<IPet>(graph) {
    fun getDefaults(): Nullable<IPet> {
    return mutableMapOf("name" to  ' ' , "type" to Animal.DOG)
    }
}
interface ITestNode {
    val nodes: MutableList<TestNode>
}
class TestNode( graph: Graph) : GraphNode<ITestNode>(graph) {
    val propertyType = "test"

//    fun getDefaults(): ITestNode {
//        return super.getDefaults().apply { nodes = listOf() }
//    }

    fun addNode(node: TestNode): TestNode{
        return this.addRef("nodes", node) as TestNode
    }

    fun addNodeWithLabel(node: TestNode, label: String): TestNode {
        return addRef("nodes", node, mutableMapOf("label" to label)) as TestNode
    }

    fun removeNode(node: TestNode): TestNode {
        return removeRef("nodes", node) as TestNode
    }

    fun listNodes(): List<TestNode> {
        return listRefs("nodes") as List<TestNode>
    }
}

fun main() {
//    Testing Edge Management
//    val graph =  Graph();
//    val root =  TestNode(graph);
//    val a =  TestNode(graph);
//    val b =  TestNode(graph);
//    root.addNode(a).addNode(b);
//    a.addNode(b);
//    println("${root.listNodes()}, ${listOf(a, b)}, 'Added two nodes.")
//    println("${a.listNodes()}, ${listOf(b)}, 'Added a child}")
//    println("root ${ root },a ${ a },b ${ b }")
//    println(root.listNodes())
//    root.removeNode(a);
//
//    println("${root.listNodes()}, ${listOf(b)}, 'Removed a node.}")
//    println("------------------------")
//    b.dispose();
//    println("${root.listNodes()}, []  'Disposed a node.}")
//
////     Subjective behavior, but might as well unit test it.
//    root.addNode(a).addNode(b).addNode(b).addNode(b);
//    println("${root.listNodes()}, ${listOf(a, b, b, b)}, 'Added duplicate nodes.}")
//    root.removeNode(b);
//    println("${root.listNodes()}, ${listOf(a)}, 'Removed a duplicate node.}")
//    root.removeNode(b).removeNode(b).removeNode(b);
//    println("${root.listNodes()}, ${listOf(a)}, 'Removed a non-present node repeatedly.}")
//
//    // Detach.
//    a.detach();
//    println("${root.listNodes()}, ${emptyList<Any>()}, 'Detached a node.}")
////
////    // Dispose.
//    root.addNode(a);
//    a.dispose();
//    println("${root.listNodes()}, ${emptyList<Any>()}, 'Disposed a node.}")
////
//    root.addNode(b);
//    root.dispose();
//    println("${root.listNodes()}, ${emptyList<Any>()}, 'Disposed the root, confirmed empty.}")
//    println("${root.isDisposed()}, ${true}, 'Disposed the root, confirmed disposed.}")


//prevent cross graph edge
//    val graphA =  Graph();
//    val graphB =  Graph();
//
//    val rootA =  TestNode(graphA);
//    val rootB =  TestNode(graphB);
//
//    val nodeA =  TestNode(graphA);
//    val nodeB =  TestNode(graphB);
//
//    rootA.addNode(nodeA);

//    println("${rootB.addNode(nodeA)}, 'prevents connecting node from another graph, used")
//    println("${rootA.addNode(nodeB)}, 'prevents connecting node from another graph, unused")


//list connections
//    val graph =  Graph();
//    val root =  TestNode(graph);
//    val node1 =  TestNode(graph);
//    val node2 =  TestNode(graph);
//
//    node1.addNode(node2);
//    root.addNode(node1);
//
//    println("${graph.listEdges().size},   2, listEdges()")
//    println("${graph.listParentEdges(node1).map { it.getParent() }},${root},listParentEdges(A) ")
//    println("${graph.listChildEdges(node1).map{it.getChild()}} ${node2}, listChildEdges(A)")
//    println("${graph.listParentEdges(node2).map { it.getParent()}} $node1 listParentEdges(B)");
//    println("${graph.listChildEdges(node2).map { it.getChild() }} [],listParentEdges(B)")

//    dispose events
//    val graph =  Graph();
//    val node1 =  TestNode(graph);
//    val node2 =  TestNode(graph);
//
//    val disposed: MutableList<Any>
//    fun test (x:GraphEvent){
//        disposed.add(target)
//    }
//    graph.addEventListener("node:dispose", callback:{ (event) -> disposed.add(event.target) });
//
//    t.deepEqual(disposed, [], 'disposed: 0');
//    t.notOk(node1.isDisposed(), 'node1 active');
//    t.notOk(node2.isDisposed(), 'node2 active');
//
//    node2.dispose();
//    t.deepEqual(disposed, [node2], 'disposed: 1');
//
//    node1.dispose();
//    t.deepEqual(disposed, [node2, node1], 'disposed: 2');
//    t.ok(node1.isDisposed(), 'node1 disposed');
//    t.ok(node2.isDisposed(), 'node2 disposed');
}