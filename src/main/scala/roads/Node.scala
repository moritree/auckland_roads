package roads

import scala.collection.mutable.ListBuffer

class Node(val id: Int, val x: Double, val y: Double) {
  var roads: List[Road] = _
  var segments: ListBuffer[Segment] = _
  val str: String = id + ": (" + x + ", " + y + ")"
  val loc: Location = Location.newFromLatLon(x, y)
}