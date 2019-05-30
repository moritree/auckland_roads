# auckland_roads

*Adapted from assignments for COMP261 at VUW.*

This program reads data for intersections, roads, and road segments, from the supplied data files into a set of data structures, and then uses these to draw an interactive map of the Auckland road system. The map can be navigated and zoomed either by buttons in the GUI or by mouse controls. The user can double click on the map to select the nearest intersection, which then highlights the intersection and all of the connected roads. Roads can be searched for using the search box in the GUI. Intersections are only highlighted when zoomed in, and the number of road segments drawn is limited for performance, so when the map is zoomed out only the longer roads are shown. This only has an effect when using the large data set.

This program is written in a mostly functional style using Scala, and the GUI is implemented using scala.swing.

## Data

The data is adapted from the [NZ Open GPS Project](http://nzopengps.org/).
The intersection and segment data form a graph, with the intersections as nodes and the road segments as edges. The names of the roads are stored in a prefix trie, so the user can search in the GUI for a road name prefix, and all matching roads will be displayed in the text area below.

### Data types
**Nodes** are locations where roads end, join, or intersect. The data file containing the nodes specifies the location of the nodes in latitude/longitude, so we have to convert this to kilometers. One degree of latitude corresponds to 111km, while longitude varies depending on the latitude, but in Auckland one degree of longitude is around 88.6km.

**Road segments** are parts of a road between two intersections (nodes). Although segments are not all straight lines, the only intersections on a road segment are at its ends. There are always at least two pairs of coordinates, for the beginning and end locations of the segment, but there may be more if the segment has bends.

**Roads** are sequences of segments. These need not be an entire road - a real road that has different properties for some parts will be represented in the data by several road objects, all with the same name label. Each segment stores a pointer to the road that it belongs to, and the roads are also stored by their labels in a prefix trie which links to the road objects. Unlike segments, Road objects may intersect with each other at locations other than their end points, according to the intersections of the segments of this road.

### Data sets
There are 2 datasets:

**large:** A set of data for the complete Auckland region, (30035 roads, 12875 distinct road names, 354760 intersections, and 42480 road segments). This data set takes several minutes to load.

**small:** A much smaller set of data for a region around the central city (746 roads, 481 distinct road names, 1080 intersections, 1412 road segments).

## To do

- [ ] Highlight all roads selected from the search box
- [ ] Implement directed graph (some roads are one way)
* To increase performance: 
 - [x] Node and road objects also store connected segments
 - [ ] Implement a quad-tree index of all the nodes for selection of the closest intersection to the mouse
- [ ] Allow user to select suggestions from the search box
- [ ] Display information labels for selected intersection/road(s)
