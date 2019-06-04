package roads
import java.awt.{BasicStroke, Color, Point}

import scala.collection.mutable.ListBuffer
import scala.swing.event.{ButtonClicked, _}
import scala.swing.{BorderPanel, Button, Component, Dimension, Graphics2D, ListView, MainFrame, ScrollPane, Swing}

class GUI extends MainFrame {
  title = "GUI program"
  preferredSize = new Dimension(640, 480)
  minimumSize = new Dimension(360, 360)

  var x_off: Double = 0
  var y_off: Double = 0

  var roadOptions: List[Road] = _   // All roads matching the search
  var suggest: List[String] = _     // List of unique road names from roadOptions
  var sel_node: Node = _
  var sel_roads: ListBuffer[Road] = _

  //  Component on which to draw the map
  object Canvas extends Component {
    preferredSize = new Dimension(320, 320)
    var d: Dimension = size

    // Center of the screen (px)
    var center_x: Double = d.width/2
    var center_y: Double = d.height/2

    var n_entities = 0

    var scale: Double = 40
    var strokeWidth: Float = (0.004 * scale + 1).toFloat

    var prev_mouse = new Point(center_x.toInt, center_y.toInt)

    // Mouse controls
    listenTo(mouse.clicks, mouse.wheel, mouse.moves)
    reactions += {
      case MouseClicked(_, p, _, t, _) =>
        // Double click: Select nearest intersection
        if (t == 2) {
          prev_mouse = p
          roadOptions = null

          // Select closest node
          val mouse_xpos = (p.x - center_x)/scale + x_off
          val mouse_ypos = (p.y - center_y)/scale + y_off
          sel_node = Graph.nodeTree.findClosest(new Location(mouse_xpos, mouse_ypos), 1)
        }
      case MousePressed(_, p, _, _, _) => prev_mouse = p
      case MouseWheelMoved(_, _, _, i) => scale *= 1 - i*0.005
      case MouseDragged(_, p, _) =>
        x_off += -(p.x - prev_mouse.x)/scale
        y_off += -(p.y - prev_mouse.y)/scale
        prev_mouse = p
    }
    reactions += { case _ => this.repaint() }

    override def paintComponent(g: Graphics2D): Unit = {
      if (sel_roads != null) sel_roads.clear()
      if (roadOptions != null) {
        if (sel_roads == null) sel_roads = ListBuffer(roadOptions.head)
        sel_roads ++= roadOptions
      }
      if (sel_node != null && sel_node.segments != null) {
        if(sel_roads == null) sel_roads = ListBuffer(sel_node.segments.head.road)
        sel_roads ++= sel_node.segments.map(f => f.road)
      }

      d = size
      center_x = d.width/2
      center_y = d.height/2
      n_entities = 0

      // Enable antialiasing
      g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
        java.awt.RenderingHints.VALUE_ANTIALIAS_ON)

      // Draw background
      g.setColor(Color.WHITE)
      g.fillRect(0, 0, d.width, d.height)

      // Draw polygons

//      g.setStroke(new BasicStroke(1))
////      // Draw QuadTree areas
//      g.setColor(Color.LIGHT_GRAY)
//      for (tree <- Graph.treeList) {
//        var x0 = tree.x0
//        var y0 = tree.y0
//        var x1 = tree.x1
//        var y1 = tree.y1
//        var tx = scrPosX(x0).round.toInt
//        var ty = scrPosY(y0).round.toInt
//        g.drawRect(tx, ty, scrPosX(x1).round.toInt - tx, scrPosY(y1).round.toInt - ty)
//      }

//      println(actPosX(0), actPosX(d.width), actPosY(0), actPosY(d.height))
//      println(Graph.nodeTree.allWithinRange(actPosX(0), actPosX(d.width), actPosY(0), actPosY(d.height)))

      // Draw polygons
      for (poly <- Graph.polygons) {
        poly.polyType match {
          case "build" => g.setColor(Color.GRAY.darker())
          case "city" | "land_urban" => g.setColor(Color.GRAY)
          case "land_non_urban" => g.setColor(new Color(160, 200, 150))
          case "lake" => g.setColor(new Color(140, 180, 240))
          case "river" => g.setColor(Color.BLUE.brighter())
          case "sea" => g.setColor(new Color(110, 150, 190))
          case "green" => g.setColor(new Color(180, 240, 120))
          case _ => g.setColor(Color.BLACK)
        }
        g.fillPolygon(
          poly.loc.map(f => scrPosX(f.x).toInt).toArray,
          poly.loc.map(f => scrPosY(f.y).toInt).toArray,
          poly.loc.length)
      }

      // Draw segments
      val diam = 0.03 * scale
      val visible = Graph.nodeTree.allWithinRange(actPosX(0), actPosX(d.width), actPosY(0), actPosY(d.height))
      if (visible != List(null)) {
//        if (scale >= 300) for (node <- visible) {
//          // Paint blue if this node is selected, light grey otherwise
//          if (sel_node == node) g.setColor(Color.BLUE)
//          else g.setColor(Color.LIGHT_GRAY)
//
//          g.fill(new Ellipse2D.Double(scrPosX(node.loc.x) - diam / 2, scrPosY(node.loc.y) - diam / 2, diam, diam))
//          n_entities += 1
//        }

        for (seg <- visible.flatMap(f => if(f != null && f.segments != null) f.segments.distinct else null)) {
          if (!(scale <= 20 && seg.road.roadclass == 0)) {
            // Line width scales with image
            strokeWidth = ((3 + seg.road.roadclass / 2) * 0.005 * scale).toFloat
            g.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND))

            // Paint red if this segment is selected, grey otherwise
            if (sel_roads != null && sel_roads.count(f => if (f.segments != null) f.segments.contains(seg) else false) > 0)
              g.setColor(Color.RED)
            else g.setColor(Color.DARK_GRAY)

            for (i <- 0 until seg.coords.length - 1)
              g.drawLine(Math.round(scrPosX(seg.loc.apply(i).x)).toInt, Math.round(scrPosY(seg.loc.apply(i).y)).toInt,
                Math.round(scrPosX(seg.loc.apply(i + 1).x)).toInt, Math.round(scrPosY(seg.loc.apply(i + 1).y)).toInt)

            n_entities += 1
          }
        }
      }

//      if(sel_node != null) println("sel_node.segments=" + sel_node.segments)
//      if(sel_roads != null) println("sel_roads=" + sel_roads)
      println("n_drawn=" + n_entities + ", scale=" + scale.round)
    }

    // methods for checking what we can draw
    def withinScreen(loc: Location, buff: Double): Boolean = {
      (scrPosX(loc.x) + buff >= 0 && scrPosX(loc.x) - buff <= d.width
        && scrPosY(loc.y) + buff >= 0  && scrPosY(loc.y) - buff <= d.height)
    }
    def scrPosX(x: Double): Double = center_x + x_off + ((x - x_off) * scale)
    def scrPosY(y: Double): Double = center_y + y_off + ((y - y_off) * scale)

    def actPosX(x: Double): Double = (x - center_x)/scale + x_off
    def actPosY(y: Double): Double = (y - center_y)/scale + y_off
  }

  // Search bar for road prefixes
  val searchBar = new swing.TextField()
  searchBar.editable = true

  // List displaying suggestions
  val optionsList: ListView[String] = new scala.swing.ListView[String](Seq("")) {
    visibleRowCount = 4
  }

  // Control buttons
  val (in_button, out_button, north_button, south_button, east_button, west_button)
  = (new Button("+"), new Button("-"), new Button("^"),
    new Button("v"), new Button(">"), new Button("<"))
  listenTo(in_button, out_button, north_button, south_button,
    east_button, west_button, searchBar.keys, optionsList.selection)

  // Set up GUI components
  contents = new BorderPanel {
    add (new BorderPanel {
      // Zoom buttons
      add(new BorderPanel {
        add(in_button, BorderPanel.Position.West)
        add(out_button, BorderPanel.Position.East)
      }, BorderPanel.Position.West)

      // Direction buttons
      add (new BorderPanel {
        add(north_button, BorderPanel.Position.North)
        add(west_button, BorderPanel.Position.West)
        add(east_button, BorderPanel.Position.East)
        add(south_button, BorderPanel.Position.South)
      }, BorderPanel.Position.East)

      // Search bar & suggestions pane
      add (new BorderPanel {
        add(searchBar, BorderPanel.Position.North)
        add(new ScrollPane(optionsList), BorderPanel.Position.Center)
      }, BorderPanel.Position.Center)

      border = Swing.EmptyBorder(10)
    }, BorderPanel.Position.North)

    add (Canvas, BorderPanel.Position.Center)
    border = Swing.EmptyBorder(0, 10, 10, 10)
  }

  reactions += {
    case SelectionChanged(`optionsList`) => if (optionsList.selection.items.nonEmpty) {
      searchBar.text = optionsList.selection.items(0)
      refreshSearch()
    }
    case KeyReleased(`searchBar`, _, _, _) => refreshSearch()
    case ButtonClicked(`in_button`)     => Canvas.scale *= 1.2;
    case ButtonClicked(`out_button`)    => Canvas.scale *= 0.8;
    case ButtonClicked(`north_button`)  => y_off -= 20/Canvas.scale;
    case ButtonClicked(`south_button`)  => y_off += 20/Canvas.scale;
    case ButtonClicked(`east_button`)   => x_off += 20/Canvas.scale;
    case ButtonClicked(`west_button`)   => x_off -= 20/Canvas.scale;
  }
  reactions += { case _ => Canvas.repaint()}

  def refreshSearch(): Unit = {
    sel_node = null
    roadOptions = null
    optionsList.listData = Seq[String]("")
    sel_roads = null
    suggest = null
    if (searchBar.text.toString != "") {
      roadOptions = Graph.roadTrie.findRoadsByPrefix(searchBar.text.toString.toLowerCase, None).toList
      optionsList.listData = roadOptions.map(f => f.label).distinct.map(f => f.capitalize)
      sel_roads = roadOptions.to[ListBuffer]
    }
  }

  visible = true
}