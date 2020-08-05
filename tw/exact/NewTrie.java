package tw.exact;

import java.util.ArrayList;
import java.util.Arrays;

class NewTrie {
    private int n;
    private int targetWidth;
    private TrieNode root;
    private int size;

    private class TrieNode {
        private TrieNode parent;
        private TrieNode[] children = new TrieNode[0];
        private XBitSet subtrieUnionOfSSets;
        private XBitSet subtrieIntersectionOfNSets;
        private int key;
        private XBitSet[] SSets = new XBitSet[0];

        TrieNode(TrieNode parent, int key, int n) {
            this.parent = parent;
            this.key = key;
            subtrieIntersectionOfNSets = new XBitSet(n);
            subtrieUnionOfSSets = new XBitSet(n);
            subtrieIntersectionOfNSets.set(0, n);
        }

        TrieNode getOrAddChildNode(int key, int n) {
            for (TrieNode child : children) {
                if (child.key == key) {
                    return child;
                }
            }
            // Node not found; add and return it
            TrieNode node = new TrieNode(this, key, n);
            children = Arrays.copyOf(children, children.length + 1);
            children[children.length - 1] = node;
            return node;
        }

        private void query(XBitSet queryS, XBitSet queryN, int maxNUnionSize,
                int nUnionSize, ArrayList<XBitSet> out_list) {
            if (queryN.unionWith(subtrieIntersectionOfNSets).cardinality() > maxNUnionSize) {
                return;
            }
            if (!queryS.isSubset(subtrieUnionOfSSets)) {
                return;
            }
            for (XBitSet SSet : SSets) {
                if (queryS.isSubset(SSet)) {
                    XBitSet bs = new XBitSet();
                    TrieNode n = this;
                    while (n.key != -1) {
                        bs.set(n.key);
                        n = n.parent;
                    }
                    out_list.add(bs);
                    break;
                }
            }
            for (TrieNode child : children) {
                int newNUnionSize = queryN.get(child.key) ? nUnionSize : nUnionSize + 1;
                if (newNUnionSize <= maxNUnionSize) {
                    child.query(queryS, queryN, maxNUnionSize, newNUnionSize, out_list);
                }
            }
        }

        private void printLatexBitset(XBitSet bitset, String colour) {
            System.out.print("\\\\ [-1ex] \\scriptsize {\\color{" + colour + "} $");
            if (bitset.isEmpty()) {
                System.out.print("\\emptyset");
            } else {
                String comma = "";
                for (int v = bitset.nextSetBit(0); v >= 0; v = bitset.nextSetBit(v+1)) {
                    System.out.print(comma + v);
                    comma = " ";
                }
            }
            System.out.print("$} ");
        }

        private void printLatex(ArrayList<Integer> currentNodeN, int featureFlags) {
            System.out.print("[{$");
            if (currentNodeN.isEmpty()) {
                System.out.print("\\emptyset");
            } else {
                for (int v : currentNodeN) {
                    if (v == currentNodeN.get(currentNodeN.size() - 1)) {
                        System.out.print("\\mathbf{" + v + "}");
                    } else {
                        System.out.print(v);
                    }
                }
            }
            System.out.print("$ ");

            if (0 != (featureFlags & 1)) {
                printLatexBitset(subtrieIntersectionOfNSets, "black!50");
            }

            if (0 != (featureFlags & 2)) {
                printLatexBitset(subtrieUnionOfSSets, "blue");
            }

            if (0 != (featureFlags & 4)) {
                for (XBitSet SSet : SSets) {
                    printLatexBitset(SSet, "blue!50");
                }
            }
            System.out.print("},align=center");
            if (SSets.length > 0) {
                System.out.print(",line width=.7mm");
            }
            for (TrieNode child : children) {
                currentNodeN.add(child.key);
                child.printLatex(currentNodeN, featureFlags);
                currentNodeN.remove(currentNodeN.size() - 1);
            }
            System.out.print("]");
        }
    }

    NewTrie(int n, int targetWidth) {
        this.n = n;
        this.targetWidth = targetWidth;
        root = new TrieNode(null, -1, n);
    }

    void put(XBitSet SSet, XBitSet NSet) {
        root.subtrieIntersectionOfNSets.and(NSet);
        root.subtrieUnionOfSSets.or(SSet);
        TrieNode node = root;
        // iterate over elements of NSet
        for (int i = NSet.nextSetBit(0); i >= 0; i = NSet.nextSetBit(i+1)) {
            node = node.getOrAddChildNode(i, n);
            node.subtrieIntersectionOfNSets.and(NSet);
            node.subtrieUnionOfSSets.or(SSet);
        }
        node.SSets = Arrays.copyOf(node.SSets, node.SSets.length + 1);
        node.SSets[node.SSets.length - 1] = SSet;
        ++size;
    }

    void put(XBitSet SSet, int neighborSize, XBitSet NSet) {
        put(SSet, NSet);
    }

    // for debugging
    void showList(ArrayList<XBitSet> bitsets) {
        for (XBitSet bs : bitsets) {
            System.out.println(bs);
        }
    }

    void collectSuperblocks(XBitSet component, XBitSet neighbours,
            ArrayList<XBitSet> list) {
        if (neighbours.cardinality() <= targetWidth + 1) {
            root.query(component, neighbours, targetWidth + 1, neighbours.cardinality(), list);
        }
    }

    int[] getSizes() {
        return new int[] {size};
    }

    void printLatex(int featureFlags) {
        System.out.println("\\documentclass{standalone}");
        System.out.println("\\usepackage{forest}");
        System.out.println("\\forestset{  default preamble={  for tree={draw,rounded corners}  }}");
        System.out.println("\\begin{document}");
        System.out.println("\\begin{forest}");
        root.printLatex(new ArrayList<Integer>(), featureFlags);
        System.out.println();
        System.out.println("\\end{forest}");
        System.out.println("\\end{document}");
    }
}
