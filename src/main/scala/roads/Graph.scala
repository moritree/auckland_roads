package roads

import scala.collection.mutable.ListBuffer
import scala.io.Source

object Graph {
  // Load all nodes
  println("Loading nodes...")
  val nodes: List[Node] = Source.fromFile("data/large/nodeID-lat-lon.tab").getLines
    .map(_.split("\t", 0).toList).toList
    .map(ls => new Node(ls.head.toInt, ls.tail.head.toDouble, ls.tail.tail.head.toDouble))
  println(nodes.length + " nodes loaded.\n")

  // Load all roads
  println("Loading roads...")
  val roads: List[Road] = Source.fromFile("data/large/roadID-roadInfo.tab").getLines.toList.tail
    .map(_.split("\t", -1).toList).map(ls =>
      new Road(ls.head.toInt, ls.apply(1).toInt, ls.apply(2), ls.apply(3),
        ls.apply(4).equals("1"), ls.apply(5).toInt, ls.apply(6).toInt,
        ls.apply(7).equals("1"), ls.apply(8).equals("1"), ls.apply(9).equals("1")))
  println(roads.length + " roads loaded.\n")

  // Load all segments
  println("Loading segments...")
  val segments: List[Segment] = Source.fromFile("data/large/roadSeg-roadID-length-nodeID-nodeID-coords.tab").getLines
    .drop(1).map(_.split("\t", -1).toList).toList.map(ls =>
      new Segment(Graph.roads.filter(_.id == ls.head.toInt).head,  // Search for road segment ID
      ls.apply(1).toDouble,                                        // Length from given double
      Graph.nodes.filter(_.id == ls.apply(2).toInt).head,          // Search for fromNode ID
      Graph.nodes.filter(_.id == ls.apply(3).toInt).head,          // Search for toNode ID
      ls.drop(4).map(_.toDouble).grouped(2).toList))               // Group coordinates into pairs)
  println(segments.length + " segments loaded.\n")

  // Load polygons
  println("Loading polygons...")
  val polygons: List[Polygon] = Source.fromFile("data/large/polygon-shapes.mp").mkString.split("\\[POLYGON\\]")
    .toList.map(f => f.split("\\n").map(_.trim).toList.filter(_ != "").dropRight(1)).filter(_.nonEmpty).map(f =>
    new Polygon(
      Integer.parseInt(f.head.drop(7), 16),
      if (f.apply(1).contains("Label")) f.apply(1).drop(6) else "",
      0,
      f.last.drop(6).split(",").toList.map(_.replaceAll("\\(|\\)|,", ""))
        .map(_.toDouble).grouped(2).map(f => (f.head, f.last)).toList))
  println(polygons.length + " polygons loaded. \n")

  // Generate prefix trie
  println("Generating road prefix trie...")
  val roadTrie: RoadTrie = new RoadTrie(0)
  roads.foreach(f => roadTrie.add(f.label.toLowerCase, None))
  println(roadTrie.nWithin() + " road names loaded.\n")

  // Insert nodes into QuadTree
  println("Generating node QuadTree...")
  val treeList: ListBuffer[QuadTree] = ListBuffer()
  val nodeTree = new QuadTree(nodes.map(f => f.loc.x).min, nodes.map(f => f.loc.x).max,
    nodes.map(f => f.loc.y).min, nodes.map(f => f.loc.y).max)
  nodes.foreach(f => nodeTree.insert(f))
  println("Done.\n")

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