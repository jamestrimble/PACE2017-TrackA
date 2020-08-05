package tw.exact;

import java.util.ArrayList;
import java.util.Arrays;

class NewTrie {
    private int n;
    private int targetWidth;
    private TrieNode root;
    private int size;

    private class TrieNode {
        private TrieNode[] children = null;
        private XBitSet subtrieUnionOfSSets;
        private XBitSet subtrieIntersectionOfNSets;
        private int[] key = new int[0];
        private XBitSet[] SSets = null;  // TODO: use zero-length arrays

        TrieNode(int[] key, int n) {
            this.key = key;
            subtrieIntersectionOfNSets = new XBitSet(n);
            subtrieUnionOfSSets = new XBitSet(n);
            subtrieIntersectionOfNSets.set(0, n);
        }

        boolean isPrefix(int[] a, int[] b) {
            if (a.length > b.length) {
                return false;
            }
            for (int i=0; i<a.length; i++) {
                if (a[i] != b[i]) {
                    return false;
                }
            }
            return true;
        }

        int commonPrefixLength(int[] a, int[] b) {
            int minLen = a.length <= b.length ? a.length : b.length;
            int retval = 0;
            for (int i=0; i<minLen; i++) {
                if (a[i] != b[i]) {
                    return retval;
                }
                ++retval;
            }
            return retval;
        }

        TrieNode getOrAddChildNode(int[] key, int n) {
            if (children != null) {
                for (int i=0; i<children.length; i++) {
                    TrieNode child = children[i];
                    if (isPrefix(child.key, key)) {
                        return child;
                    } else {
                        int prefixLen = commonPrefixLength(child.key, key);
                        if (prefixLen != 0) {
                            int[] prefix = Arrays.copyOf(key, prefixLen);
                            TrieNode node = new TrieNode(prefix, n);
                            node.subtrieIntersectionOfNSets.and(
                                    child.subtrieIntersectionOfNSets);
                            node.subtrieUnionOfSSets.or(
                                    child.subtrieUnionOfSSets);
                            node.children = new TrieNode[] {child};
                            child.key = Arrays.copyOfRange(child.key, prefixLen, child.key.length);
                            children[i] = node;
                            return node;
                        }
                    }
                }
            }
            // Node not found; add and return it
            TrieNode node = new TrieNode(key, n);
            if (children == null) {
                children = new TrieNode[] {node};
            } else {
                children = Arrays.copyOf(children, children.length + 1);
                children[children.length - 1] = node;
            }
            return node;
        }

        private void query(XBitSet queryS, XBitSet queryN, int maxNUnionSize,
                int nUnionSize, XBitSet currentNodeN, ArrayList<XBitSet> out_list) {
            if (queryN.unionWith(subtrieIntersectionOfNSets).cardinality() > maxNUnionSize) {
                return;
            }
            if (!queryS.isSubset(subtrieUnionOfSSets)) {
                return;
            }
            if (SSets != null) {
                for (XBitSet SSet : SSets) {
                    if (queryS.isSubset(SSet)) {
                        out_list.add((XBitSet) currentNodeN.clone());
                        break;
                    }
                }
            }
            if (children != null) {
                for (TrieNode child : children) {
                    int newNUnionSize = nUnionSize;
                    for (int v : child.key) {
                        if (!queryN.get(v)) {
                            ++newNUnionSize;
                        }
                    }
                    if (newNUnionSize <= maxNUnionSize) {
                        for (int v : child.key) {
                            currentNodeN.set(v);
                        }
                        child.query(queryS, queryN, maxNUnionSize, newNUnionSize, currentNodeN, out_list);
                        for (int v : child.key) {
                            currentNodeN.clear(v);
                        }
                    }
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
                if (SSets != null) {
                    for (XBitSet SSet : SSets) {
                        printLatexBitset(SSet, "blue!50");
                    }
                }
            }
            System.out.print("},align=center");
            if (SSets != null) {
                System.out.print(",line width=.7mm");
            }
            if (children != null) {
                for (TrieNode child : children) {
                    for (int v : child.key) {
                        currentNodeN.add(v);
                    }
                    child.printLatex(currentNodeN, featureFlags);
                    for (int v : child.key) {
                        currentNodeN.remove(currentNodeN.size() - 1);
                    }
                }
            }
            System.out.print("]");
        }
    }

    NewTrie(int n, int targetWidth) {
        this.n = n;
        this.targetWidth = targetWidth;
        root = new TrieNode(new int[0], n);
    }

    void put(XBitSet SSet, XBitSet NSet) {
        root.subtrieIntersectionOfNSets.and(NSet);
        root.subtrieUnionOfSSets.or(SSet);
        TrieNode node = root;

        int[] key = new int[NSet.cardinality()];
        int pos = 0;
        for (int v = NSet.nextSetBit(0); v >= 0; v = NSet.nextSetBit(v+1)) {
            key[pos] = v;
            ++pos;
        }

        while (key.length != 0) {
            node = node.getOrAddChildNode(key, n);
            node.subtrieIntersectionOfNSets.and(NSet);
            node.subtrieUnionOfSSets.or(SSet);
            key = Arrays.copyOfRange(key, node.key.length, key.length);
        }
        if (node.SSets == null) {
            node.SSets = new XBitSet[] {SSet};
        } else {
            node.SSets = Arrays.copyOf(node.SSets, node.SSets.length + 1);
            node.SSets[node.SSets.length - 1] = SSet;
        }
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
        XBitSet nbs = new XBitSet(n);
        if (neighbours.cardinality() <= targetWidth + 1) {
            root.query(component, neighbours, targetWidth + 1, neighbours.cardinality(), nbs, list);
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
