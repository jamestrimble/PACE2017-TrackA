package tw.exact;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class NewTrieReordered implements SupersetDataStructure, LatexPrintable {
    private int targetWidth;
    private TrieNode root;
    private int[] stats;
    private ArrayList<BitsetPair> all = new ArrayList<>();
    private FrequencyComparator comparator;

    class FrequencyComparator implements Comparator<Integer> {
        int[] stats;
        FrequencyComparator(int[] stats) {
            this.stats = Arrays.copyOf(stats, stats.length);
        }

        @Override
        public int compare(Integer a, Integer b) {
            if (stats[a] > stats[b]) {
                return -1;
            }
            if (stats[a] < stats[b]) {
                return 1;
            }
            return a - b;
        }
    }

    private class BitsetPair {
        private XBitSet bs0;
        private XBitSet bs1;

        BitsetPair(XBitSet bs0, XBitSet bs1) {
            this.bs0 = bs0;
            this.bs1 = bs1;
        }
    }

    private class TrieNode {
        private TrieNode[] children = new TrieNode[0];
        private XBitSet subtrieUnionOfSSets;
        private XBitSet subtrieIntersectionOfNSets;
        private int key;
        private XBitSet[] SSets = new XBitSet[0];

        TrieNode(int key, XBitSet initialIntersectionOfNSets) {
            this.key = key;
            subtrieIntersectionOfNSets = initialIntersectionOfNSets;
            subtrieUnionOfSSets = new XBitSet();
        }

        TrieNode getOrAddChildNode(int key, XBitSet SSet, XBitSet NSet) {
            for (TrieNode child : children) {
                if (child.key == key) {
                    child.subtrieIntersectionOfNSets.and(NSet);
                    child.subtrieUnionOfSSets.or(SSet);
                    return child;
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
                        System.out.print("\\mathbf{\\underline{" + v + "}}");
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

    public NewTrieReordered(int n, int targetWidth) {
        stats = new int[n];
        comparator = new FrequencyComparator(stats);
        this.targetWidth = targetWidth;
        XBitSet initialIntersectionOfNSets = new XBitSet();
        initialIntersectionOfNSets.set(0, n);
        root = new TrieNode(-1, initialIntersectionOfNSets);
    }

    public void put_(XBitSet SSet, XBitSet NSet) {
        root.subtrieIntersectionOfNSets.and(NSet);
        root.subtrieUnionOfSSets.or(SSet);
        TrieNode node = root;
        // iterate over elements of NSet
        ArrayList<Integer> NSetMembersList = new ArrayList<Integer>(NSet.cardinality());
        for (int i = NSet.nextSetBit(0); i >= 0; i = NSet.nextSetBit(i+1)) {
            NSetMembersList.add(i);
        }
        Collections.sort(NSetMembersList, comparator);
        for (int i : NSetMembersList) {
            node = node.getOrAddChildNode(i, SSet, NSet);
        }
        node.SSets = Arrays.copyOf(node.SSets, node.SSets.length + 1);
        node.SSets[node.SSets.length - 1] = SSet;
    }

    public void put(XBitSet SSet, XBitSet NSet) {
        if (all.size() >= 1024 && Integer.bitCount(all.size()) == 1) {
            comparator = new FrequencyComparator(stats);
            XBitSet initialIntersectionOfNSets = new XBitSet();
            initialIntersectionOfNSets.set(0, stats.length);
            root = new TrieNode(-1, initialIntersectionOfNSets);
            for (BitsetPair pair : all) {
                put_(pair.bs0, pair.bs1);
            }
        }
        put_(SSet, NSet);
        for (int i = NSet.nextSetBit(0); i >= 0; i = NSet.nextSetBit(i+1)) {
            stats[i]++;
        }
        all.add(new BitsetPair(SSet, NSet));
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
        return new int[] {all.size()};
    }

    public void printLatex(int featureFlags) {
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
