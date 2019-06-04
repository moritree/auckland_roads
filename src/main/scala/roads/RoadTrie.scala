package roads

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class RoadTrie(var depth: Int) {
  var children: mutable.Map[Char, RoadTrie] = _
  var roads: ListBuffer[Road] = _

  /** Recursively add road with label s to the trie
    *
    * @param s      label of road
    * @param t_opt  tail of string, only for recursion
    */
  def add(s: String, t_opt: Option[String]): Unit = {
    val t: String = t_opt.getOrElse(s).toLowerCase
    var n: RoadTrie = this

    if (children == null) {
      n = new RoadTrie(n.depth + 1)
      children = collection.mutable.Map[Char, RoadTrie](t.head->n)
    } else {
      if (! children.contains(t.head)) {
        n = new RoadTrie(n.depth + 1)
        children.update(t.head, n)
      }
      else n = children(t.head)
    }

    if(t.length == 1) {
      Graph.roads.filter{p: Road => p.label.toUpperCase.equals(s.toUpperCase)}.foreach{f =>
        if (n.roads == null) n.roads = collection.mutable.ListBuffer[Road](f)
        else n.roads += f; n.roads = n.roads.distinct}
    }
    else n.add(s, Some(t.tail))
  }

  /** Find a list of all the roads which match the given label prefix (not case sensitive)
    *
    * @param prefix   Prefix to search for
    * @param ls_opt   List in progress of roads, only for recursion
    * @return         List of matching roads
    */
  def findRoadsByPrefix(prefix: String, ls_opt: Option[ListBuffer[Road]]): ListBuffer[Road] = {
    // Initialize new list if not supplied
    var ls: ListBuffer[Road] = ls_opt.getOrElse(new ListBuffer[Road])

    // Add nothing to the list that doesn't match the entire prefix
    if ( this.depth < prefix.length) {
      if (children.contains(prefix.apply(depth)))
        children(prefix.apply(depth)).findRoadsByPrefix(prefix, Some(ls))
    }
    else {
      if (roads != null) roads foreach (f => ls += f)
      if (children != null) {
        for (c <- children) {
          c._2.findRoadsByPrefix(prefix, None).foreach(f => ls += f)
        }
      }
    }
    ls.distinct
  }

  def nWithin(): Int = {
    (if (roads != null) roads.map(_.label).distinct.length else 0) +
      (if (children != null) children.map(_._2.nWithin()).sum else 0)
  }
}