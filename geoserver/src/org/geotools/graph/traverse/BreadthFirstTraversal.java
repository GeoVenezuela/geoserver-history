package org.geotools.graph.traverse;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.geotools.graph.Graph;
import org.geotools.graph.GraphComponent;



/**
 * Performs a Breadth First Traversal of the graph.
 * 
 * @author Justin Deoliveira
 */
public class BreadthFirstTraversal extends SourceGraphTraversal {

  private LinkedList m_active;
  
  public BreadthFirstTraversal(
    Graph graph, GraphWalker walker, GraphComponent source
  ) {
    super(graph, walker, source);
  }
  
  public Collection getActiveElements() {
    return(m_active);    
  }
  
  protected void walk() {
    m_active = new LinkedList();
    m_active.add(getSource());
    
    GraphComponent element = null;
    GraphComponent adjacent = null;
    while(!m_active.isEmpty()) {
      element = (GraphComponent)m_active.removeFirst(); 
      if (getWalker().isVisited(element)) continue;
      
      if (getWalker().visit(element, this) == STOP) return;
      
      Iterator itr;
      for (itr = element.getAdjacentElements().iterator(); itr.hasNext();) {
        adjacent = (GraphComponent)itr.next();
        if (!getWalker().isVisited(adjacent)) m_active.addLast(adjacent);
      }  
    }
  }
}