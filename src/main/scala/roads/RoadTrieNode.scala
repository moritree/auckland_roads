package roads

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class RoadTrieNode(var depth: Int) {
  var children: mutable.Map[Char, RoadTrieNode] = _
  var roads: ListBuffer[Road] = _

  /**
    * Recursively add road with label s to the trie
    * @param s      label of road to add to trie
    * @param t_opt  tail of string, only for recursion
    */
  def add(s: String, t_opt: Option[String]): Unit = {
    val t: String = t_opt.getOrElse(s).toLowerCase
    var n: RoadTrieNode = this

    if (children == null) {
      n = new RoadTrieNode(n.depth + 1)
      children = collection.mutable.Map[Char, RoadTrieNode](t.head->n)
    } else {
      if (! children.contains(t.head)) {
        n = new RoadTrieNode(n.depth + 1)
        children.update(t.head, n)
      }
      else n = children(t.head)
    }

    assert(!t.isEmpty)

    if(t.length == 1) {
      Graph.roads.filter{p: Road => p.label.toUpperCase.equals(s.toUpperCase)}.foreach{f =>
        if (roads == null) roads = collection.mutable.ListBuffer[Road](f)
        else roads += f}
    }
    else n.add(s, Some(t.tail))
  }

  def findRoadsByPrefix(prefix: String, ls_opt: Option[ListBuffer[Road]]): ListBuffer[Road] = {
    var ls: ListBuffer[Road] = ls_opt.getOrElse(new ListBuffer[Road])

    if ( this.depth < prefix.length) {
      if (children.contains(prefix.apply(depth))) children(prefix.apply(depth)).findRoadsByPrefix(prefix, Some(ls))
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
}