package roads

class Road (val id: Int, val road_type: Int, var label: String, var city: String, val oneway: Boolean, val speed: Int,
            val roadclass: Int, val notforcar: Boolean, val notforpede: Boolean, val notforbicy: Boolean){
  label = label.split(" ").map(_.capitalize).toList.mkString(" ")
  city = city.split(" ").map(_.capitalize).toList.mkString(" ")
  val str: String = id + ": " + label + ", " + city
}