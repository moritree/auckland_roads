package roads

import scala.collection.mutable.ListBuffer
import scala.io.Source

object Graph {
  // Load all nodes
  println("Loading nodes...")
  val nodes: List[Node] = Source.fromFile("data/small/nodeID-lat-lon.tab").getLines
    .map(_.split("\t", 0).toList).toList
    .map(ls => new Node(ls.head.toInt, ls.tail.head.toDouble, ls.tail.tail.head.toDouble))
  println(nodes.length + " nodes loaded.\n")

  // Load all roads
  println("Loading roads...")
  val roads: List[Road] = Source.fromFile("data/small/roadID-roadInfo.tab").getLines.toList.tail
    .map(_.split("\t", -1).toList).map(ls =>
      new Road(ls.head.toInt, ls.apply(1).toInt, ls.apply(2), ls.apply(3),
        ls.apply(4).equals("1"), ls.apply(5).toInt, ls.apply(6).toInt,
        ls.apply(7).equals("1"), ls.apply(8).equals("1"), ls.apply(9).equals("1")))
  println(roads.length + " roads loaded.\n")

  // Generate prefix trie
  println("Generating road prefix trie...")
  val roadTrie: RoadTrieNode = new RoadTrieNode(0)
  roads.foreach(f => roadTrie.add(f.label.toLowerCase, None))
  println("Done.\n")

  // Load all segments
  println("Loading segments...")
  val segments: List[Segment] = Source.fromFile("data/small/roadSeg-roadID-length-nodeID-nodeID-coords.tab").getLines
    .drop(1).map(_.split("\t", -1).toList).toList.map(ls =>
      new Segment(Graph.roads.filter(_.id == ls.head.toInt).head,  // Search for road segment ID
      ls.apply(1).toDouble,                                        // Length from given double
      Graph.nodes.filter(_.id == ls.apply(2).toInt).head,          // Search for fromNode ID
      Graph.nodes.filter(_.id == ls.apply(3).toInt).head,          // Search for toNode ID
      ls.drop(4).map(_.toDouble).grouped(2).toList))               // Group coordinates into pairs)
  println(segments.length + " segments loaded.\n")

  // Add segments to node and road objects
  println("Adding segments to nodes and roads...")
  segments.foreach { f =>
    if (f.road.segments == null) f.road.segments = ListBuffer(f) else f.road.segments += f
    if (f.fromNode.segments == null) f.fromNode.segments = ListBuffer(f) else f.fromNode.segments += f
    if (f.toNode.segments == null) f.toNode.segments = ListBuffer(f) else f.toNode.segments += f
  }
  println("Done.\n")

  val gui: GUI = new GUI

  def main(args: Array[String]):Unit = {}
}