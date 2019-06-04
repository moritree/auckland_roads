package roads

import scala.collection.mutable.ListBuffer

class QuadTree(val x0: Double, val x1: Double, val y0: Double, val y1: Double) {
  Graph.treeList += this
  var isLeaf: Boolean = true
  val bucket: Int = 10

  var northWest: QuadTree = _
  var northEast: QuadTree = _
  var southWest: QuadTree = _
  var southEast: QuadTree = _

  val data: ListBuffer[Node] = ListBuffer()

  def insert(node: Node): Unit = {
    if (isLeaf) {
      data += node
      if (data.size > bucket) this.decomposeAndInsert(node)
    }
    else {
      if      (northWest.containsPoint(node.loc)) northWest.insert(node)
      else if (northEast.containsPoint(node.loc)) northEast.insert(node)
      else if (southWest.containsPoint(node.loc)) southWest.insert(node)
      else if (southEast.containsPoint(node.loc)) southEast.insert(node)
    }
  }

  def decomposeAndInsert(node: Node): Unit = {
    isLeaf = false

    northWest = new QuadTree(x0, (x1+x0)/2, y0, (y1+y0)/2)
    northEast = new QuadTree((x1+x0)/2, x1, y0, (y1+y0)/2)
    southWest = new QuadTree(x0, (x1+x0)/2, (y1+y0)/2, y1)
    southEast = new QuadTree((x1+x0)/2, x1, (y1+y0)/2, y1)

    data.foreach(f => insert(f))
  }

  def containsPoint(loc: Location): Boolean = loc.x >= x0 && loc.x <= x1 && loc.y >= y0 && loc.y <= y1

  def findClosest(loc: Location, range: Double): Node = {
    val list = allWithinRange(loc.x - range, loc.x + range, loc.y - range, loc.y + range)

    if (list != List(null)) list.reduce((a, b) =>
      if (a.loc.distance(loc) < b.loc.distance(loc)) a else b)
    else findClosest(loc, range + 1)
  }

  def allWithinRange(left: Double, right: Double, top: Double, bottom: Double): List[Node] = {
    val collect: ListBuffer[Node] = ListBuffer()
    if (x0 > right || x1 < left || y0 > bottom || y1 < top) return List(null)
    if (isLeaf) data.foreach(f => collect += f)
    else {
      northWest.allWithinRange(left, right, top, bottom).foreach(f => collect += f)
      northEast.allWithinRange(left, right, top, bottom).foreach(f => collect += f)
      southWest.allWithinRange(left, right, top, bottom).foreach(f => collect += f)
      southEast.allWithinRange(left, right, top, bottom).foreach(f => collect += f)
    }
    collect.filter(p => p != null).distinct.toList
  }

  def nWithin(): Int = {
    if (isLeaf) data.size
    else northWest.nWithin() + northEast.nWithin() + southWest.nWithin() + southEast.nWithin()
  }
}