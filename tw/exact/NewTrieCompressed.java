package tw.exact;

import java.util.ArrayList;
import java.util.Arrays;

class NewTrieCompressed implements SupersetDataStructure, LatexPrintable {
    private int targetWidth;
    private TrieNode root;
    private int size;

    private class TrieNode {
        private TrieNode[] children = new TrieNode[0];
        private XBitSet subtrieUnionOfSSets;
        private XBitSet subtrieIntersectionOfNSets;
        private int[] key = new int[0];
        private XBitSet[] SSets = new XBitSet[0];

        TrieNode(int[] key, XBitSet initialIntersectionOfNSets) {
            this.key = key;
            subtrieIntersectionOfNSets = initialIntersectionOfNSets;
            subtrieUnionOfSSets = new XBitSet();
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

        TrieNode getOrAddChildNode(int[] key, XBitSet SSet, XBitSet NSet) {
            for (int i=0; i<children.length; i++) {
                TrieNode child = children[i];
                int prefixLen = commonPrefixLength(child.key, key);
                if (prefixLen == child.key.length) {
                    // child.key is a prefix of key
                    child.subtrieIntersectionOfNSets.and(NSet);
                    child.subtrieUnionOfSSets.or(SSet);
                    return child;
                } else if (prefixLen != 0) {
                    int[] prefix = Arrays.copyOf(key, prefixLen);
                    TrieNode node = new TrieNode(prefix, (XBitSet) child.subtrieIntersectionOfNSets.clone());
                    node.subtrieUnionOfSSets.or(child.subtrieUnionOfSSets);
                    node.subtrieIntersectionOfNSets.and(NSet);
                    node.subtrieUnionOfSSets.or(SSet);
                    node.children = new TrieNode[] {child};
                    child.key = Arrays.copyOfRange(child.key, prefixLen, child.key.length);
                    children[i] = node;
                    return node;
                }
            }
            // Node not found; add and return it
            TrieNode node = new TrieNode(key, (XBitSet) NSet.clone());
            node.subtrieUnionOfSSets.or(SSet);
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
                    out_list.add((XBitSet) subtrieIntersectionOfNSets.clone());
                    break;
                }
            }
            for (TrieNode child : children) {
                int newNUnionSize = nUnionSize;
                for (int v : child.key) {
                    if (!queryN.get(v)) {
                        ++newNUnionSize;
                    }
                }
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

        private void printLatex(int prevNodeNLength, ArrayList<Integer> currentNodeN, int featureFlags) {
            System.out.print("[{$");
            if (currentNodeN.isEmpty()) {
                System.out.print("\\emptyset");
            } else {
                for (int i=0; i<currentNodeN.size(); i++) {
                int v = currentNodeN.get(i);
                    if (i == prevNodeNLength) {
                        System.out.print("\\mathbf{\\underline{");
                    }
                    System.out.print(v);
                }
                System.out.print("}}");
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
                int len = currentNodeN.size();
                for (int v : child.key) {
                    currentNodeN.add(v);
                }
                child.printLatex(len, currentNodeN, featureFlags);
                for (int v : child.key) {
                    currentNodeN.remove(currentNodeN.size() - 1);
                }
            }
            System.out.print("]");
        }
    }

    NewTrieCompressed(int n, int targetWidth) {
        this.targetWidth = targetWidth;
        XBitSet initialIntersectionOfNSets = new XBitSet();
        initialIntersectionOfNSets.set(0, n);
        root = new TrieNode(new int[0], initialIntersectionOfNSets);
    }

    public void put(XBitSet SSet, XBitSet NSet) {
        root.subtrieIntersectionOfNSets.and(NSet);
        root.subtrieUnionOfSSets.or(SSet);
        TrieNode node = root;

        int[] key = NSet.toArray();

        while (key.length != 0) {
            node = node.getOrAddChildNode(key, SSet, NSet);
            key = Arrays.copyOfRange(key, node.key.length, key.length);
        }
        node.SSets = Arrays.copyOf(node.SSets, node.SSets.length + 1);
        node.SSets[node.SSets.length - 1] = SSet;
        ++size;
    }

    // for debugging
    void showList(ArrayList<XBitSet> bitsets) {
        for (XBitSet bs : bitsets) {
            System.out.println(bs);
        }
    }

    public void collectSuperblocks(XBitSet component, XBitSet neighbours,
            ArrayList<XBitSet> list) {
        if (neighbours.cardinality() <= targetWidth + 1) {
            root.query(component, neighbours, targetWidth + 1, neighbours.cardinality(), list);
        }
    }

    public int[] getSizes() {
        return new int[] {size};
    }

    public void printLatex(int featureFlags) {
        System.out.println("\\documentclass{standalone}");
        System.out.println("\\usepackage{forest}");
        System.out.println("\\forestset{  default preamble={  for tree={draw,rounded corners}  }}");
        System.out.println("\\begin{document}");
        System.out.println("\\begin{forest}");
        root.printLatex(0, new ArrayList<Integer>(), featureFlags);
        System.out.println();
        System.out.println("\\end{forest}");
        System.out.println("\\end{document}");
    }
}
