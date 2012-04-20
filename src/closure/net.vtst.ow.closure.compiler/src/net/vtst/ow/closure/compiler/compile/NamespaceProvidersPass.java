package net.vtst.ow.closure.compiler.compile;

import com.google.javascript.jscomp.AbstractCompiler;
import com.google.javascript.jscomp.CodingConvention;
import com.google.javascript.jscomp.HotSwapCompilerPass;
import com.google.javascript.jscomp.NodeTraversal;
import com.google.javascript.rhino.Node;

/**
 * A compiler pass that builds a mapping of namespaces to declaring nodes (i.e. a 
 * {@code NamespaceProvidersMap}.
 * @author Vincent Simonet
 */
// TODO: It seems not to work
public class NamespaceProvidersPass implements HotSwapCompilerPass {
  
  private AbstractCompiler compiler;
  private NamespaceProvidersMap map;
  private CodingConvention codingConvention;

  public NamespaceProvidersPass(AbstractCompiler compiler, NamespaceProvidersMap map) {
    this.compiler = compiler;
    this.codingConvention = compiler.getCodingConvention();
    this.map = map;
  }

  @Override
  public void process(Node externs, Node root) {
    traversal.traverse(root);
  }

  @Override
  public void hotSwapScript(Node scriptRoot, Node originalRoot) {
    map.removeAll(scriptRoot);
    traversal.traverse(scriptRoot);
  }

  /**
   * The node traversal used by the compiler pass.
   */
  private NodeTraversal traversal = new NodeTraversal(compiler, new NodeTraversal.Callback() {
    
    private Node currentScript;

    @Override    
    public boolean shouldTraverse(NodeTraversal traversal, Node node, Node parent) {
      if (node.isCall()) {
        String provide = codingConvention.extractClassNameIfProvide(node, parent);
        if (provide != null) {
          map.put(provide, currentScript);
        }
        return false;
      } else if (node.isScript()) {
        currentScript = node;
        return true;
      } else {
        return (parent == null || parent.isExprResult());
      }
    }

    @Override
    public void visit(NodeTraversal traversal, Node node, Node parent) {}

  });

}
