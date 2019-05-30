package roads
import java.awt.geom.Ellipse2D
import java.awt.{BasicStroke, Color, Point}

import scala.swing.event.{ButtonClicked, _}
import scala.swing.{BorderPanel, Button, Component, Dimension, Graphics2D, MainFrame, ScrollPane, Swing, TextArea}

class GUI extends MainFrame {
  title = "GUI program"
  preferredSize = new Dimension(640, 480)
  minimumSize = new Dimension(360, 360)

  var x_off: Double = 0
  var y_off: Double = 0

  var roadOptions: List[Road] = _   // All roads matching the search
  var suggest: List[String] = _     // List of unique road names from roadOptions

  //  Component on which to draw the map
  object Canvas extends Component {
    preferredSize = new Dimension(320, 320)
    var d: Dimension = size

    // Center of the screen (px)
    var center_x: Double = d.width/2
    var center_y: Double = d.height/2

    var n_entities = 0
    var sel_node: Node = _

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

          // Select closest node
          sel_node = Graph.nodes.filter{r: Node => withinScreen(r.loc, 0)}.reduce((a, b) =>
            if (Math.hypot(scrPosX(a.loc.x) - p.x, scrPosY(a.loc.y) - p.y)
              <= Math.hypot(scrPosX(b.loc.x) - p.x, scrPosY(b.loc.y) - p.y)) a else b)
          if (sel_node.roads == null) {
            sel_node.roads = Graph.segments
              .filter{p: Segment => p.loc.count {q: Location => withinScreen(q, strokeWidth)} >= 1}
              .filter{p: Segment => p.toNode == sel_node || p.fromNode == sel_node}.map{f: Segment => f.road}.distinct
              .map{f: Road => Graph.roads.filter{p: Road => p.label == f.label}}.reduce{(a, b) => a ++ b}
          }
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

      // Line width scales with image
      strokeWidth = (0.008 * scale + 2).toFloat
      g.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND))

      // Draw nodes
      val diam = 0.03 * scale
      if (scale >= 300) {
        for (node <- Graph.nodes.filter { p: Node => withinScreen(p.loc, diam / 2)}) {
          // Paint blue if this node is selected, light grey otherwise
          if (sel_node == node) g.setColor(Color.BLUE)
          else g.setColor(Color.LIGHT_GRAY)

          g.fill(new Ellipse2D.Double(scrPosX(node.loc.x) - diam / 2, scrPosY(node.loc.y) - diam / 2, diam, diam))
          n_entities += 1
        }
      }

      // Draw segments
      for (seg <- Graph.segments
          .filter{p: Segment => (withinScreen(p.toNode.loc, 50) || withinScreen(p.fromNode.loc, 50)) &&
            (Math.hypot(p.toNode.loc.x - p.fromNode.loc.x, p.toNode.loc.y - p.fromNode.loc.y)*scale > 5 ||
              n_entities < 10000)}){

        // Paint red if this segment is selected, grey otherwise
        if (sel_node != null && sel_node.roads.contains(seg.road)) g.setColor(Color.RED)
        else g.setColor(Color.DARK_GRAY)

        // If this segment's loc objects have not been initialized yet, do so
        if (seg.loc == null) {
          seg.loc = seg.coords.map((x: List[Double]) => Location.newFromLatLon(x.head, x.last))
        }

        for (i <- 0 until seg.coords.length - 1)
          g.drawLine(Math.round(scrPosX(seg.loc.apply(i).x)).toInt, Math.round(scrPosY(seg.loc.apply(i).y)).toInt,
            Math.round(scrPosX(seg.loc.apply(i + 1).x)).toInt, Math.round(scrPosY(seg.loc.apply(i + 1).y)).toInt)

        n_entities += 1
      }

      println("n_drawn=" + n_entities + ", scale=" + scale.round)
    }

    // methods for checking what we can draw
    def withinScreen(loc: Location, buff: Double): Boolean = {
      (scrPosX(loc.x) + buff >= 0 && scrPosX(loc.x) - buff <= d.width
        && scrPosY(loc.y) + buff >= 0  && scrPosY(loc.y) - buff <= d.height)
    }
    def scrPosX(x: Double): Double = center_x + x_off + ((x - x_off) * scale)
    def scrPosY(y: Double): Double = center_y + y_off + ((y - y_off) * scale)
  }

  // Search bar for road prefixes
  val searchBar = new swing.TextField()
  searchBar.editable = true

  // Text area displaying suggestions
  val opt = new TextArea() {
    rows = 4
    editable = false
    lineWrap = true
    wordWrap = true
  }

  // Control buttons
  val (in_button, out_button, north_button, south_button, east_button, west_button)
  = (new Button("+"), new Button("-"), new Button("^"),
    new Button("v"), new Button(">"), new Button("<"))
  listenTo(in_button, out_button, north_button, south_button, east_button, west_button, searchBar)

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
        add(new ScrollPane(opt), BorderPanel.Position.Center)
      }, BorderPanel.Position.Center)

      border = Swing.EmptyBorder(10)
    }, BorderPanel.Position.North)

    add (Canvas, BorderPanel.Position.Center)
    border = Swing.EmptyBorder(0, 10, 10, 10)
  }

  reactions += {
    case EditDone(`searchBar`) =>
      roadOptions = Graph.roadTrie.findRoadsByPrefix(searchBar.text.toString, None).toList
      suggest = roadOptions.map(f => f.label).distinct.map(f => f.capitalize)
      opt.text = suggest.map(f => f + "\n").mkString
      opt.caret.position = 0
    case ButtonClicked(`in_button`) => Canvas.scale *= 1.2;
    case ButtonClicked(`out_button`) => Canvas.scale *= 0.8;
    case ButtonClicked(`north_button`) => y_off -= 20/Canvas.scale;
    case ButtonClicked(`south_button`) => y_off += 20/Canvas.scale;
    case ButtonClicked(`east_button`) => x_off += 20/Canvas.scale;
    case ButtonClicked(`west_button`) => x_off -= 20/Canvas.scale;
  }
  reactions += { case _ => Canvas.repaint()}


  println(Graph.roadTrie.findRoadsByPrefix("man".toLowerCase, None).toList)
  visible = true
}

object GUI {
  def apply: GUI = new GUI
}