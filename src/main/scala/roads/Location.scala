package roads
import java.awt.Point

class Location(val x: Double, val y: Double) {
  //
  // Conversion methods
  //

  /**
    * Makes a new Point object from this Location object and returns it. To
    * create this Point, an origin location and the scale of the window are
    * required. Note the vertical direction is inverted
    */
  def asPoint(origin: Location, scale: Double) : Point = {
    new Point(((x - origin.x) * scale).asInstanceOf[Int], ((origin.y - y) * scale).asInstanceOf[Int])
  }

  //
  // Utility methods
  //

  /**
    * Returns a new Location object that is this Location object
    * moved by the given dx and dy
    */
  def moveBy(dx: Double, dy: Double): Location = new Location(x + dx, y + dy)

  /**
    * Return distance between this location and another
    */
  def distance(other: Location): Double = Math.hypot(this.x - other.x, this.y - other.y)

  /**
    * Return true if this location is within dist of other.
    * Uses manhattan distance for greater speed.
    */
  def isClose(other: Location, dist: Double) : Boolean = Math.abs(this.x - other.x) + Math.abs(this.y - other.y) <= dist

//  override def toString : String = String.format("(%.3f, %.3f)", x, y)
}

object Location {
  // Center of Auckland City
  val CENTRE_LAT : Double = -36.847622
  val CENTRE_LON : Double = 174.763444

  // Kilometers per degree
  val SCALE_LAT : Double = 111.0
  val DEG_TO_RAD : Double = Math.PI / 180

  /**
    * Create a new Location object from the given latitude and longitude, which
    * is the format used in the data files.
    */
  def newFromLatLon(lat: Double, lon: Double) : Location = {
    val x : Double = (lon - CENTRE_LON) * (SCALE_LAT * Math.cos((lat - CENTRE_LAT) * DEG_TO_RAD))
    val y : Double = (lat - CENTRE_LAT) * SCALE_LAT
    apply(x, y)
  }

  def apply(x: Double, y:Double): Location = new Location(x, y)
}