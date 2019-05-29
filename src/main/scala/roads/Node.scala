package roads

class Node(val id: Int, val x: Double, val y: Double) {
  var roads: List[Road] = _
  val str: String = id + ": (" + x + ", " + y + ")"
  val loc: Location = Location.newFromLatLon(x, y)
}