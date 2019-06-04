package roads

class Polygon(val polyNum: Int, val label: String, val endLevel: Int, val coords: List[(Double, Double)]) {
  val loc: List[Location] = coords.map(f => Location.newFromLatLon(f._1, f._2))
  val polyType: String = polyNum match {
    case city if 1 until 4 contains city => "city"
    case build if 6 until 20 contains build => "build"
    case green if 20 until 40 contains green => "green"
    case sea if 40 until 60 contains sea => "sea"
    case 69 => "sea"
    case lake if 60 until 69 contains lake => "lake"
    case river if 70 until 74 contains river => "river"
    case 76 => "river"
    case land if 77 until 84 contains land => "land_non_urban"
    case sea if 40 until 78 contains sea => "sea"
    case 258 => "land_urban"
    case _ => "other"
  }
}
