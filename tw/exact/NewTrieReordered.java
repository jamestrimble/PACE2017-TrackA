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
        node.addSSet(SSet);
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
        int k = targetWidth + 1 - neighbours.cardinality();
        if (k >= 0) {
            root.query(component, neighbours, k, k, list);
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
