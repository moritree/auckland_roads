package roads

class Segment(val road: Road, val length: Double, val fromNode: Node, val toNode: Node, val coords: List[List[Double]]){
  val str: String = "Road=" + road.id + ", Len=" + length + ", from=" + fromNode.id + ", to=" + toNode.id
  var loc: List[Location] = coords.map((x: List[Double]) => Location.newFromLatLon(x.head, x.last))
}