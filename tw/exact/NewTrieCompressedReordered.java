package tw.exact;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

class NewTrieCompressedReordered implements SupersetDataStructure, LatexPrintable {
    private int targetWidth;
    private TrieNodeCompressed root;
    private int[] stats;
    private ArrayList<BitsetPair> all = new ArrayList<>();
    private FrequencyComparator comparator;
    private int reorderSize = 10;

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

    NewTrieCompressedReordered(int n, int targetWidth) {
        stats = new int[n];
        comparator = new FrequencyComparator(stats);
        this.targetWidth = targetWidth;
        XBitSet initialIntersectionOfNSets = new XBitSet();
        initialIntersectionOfNSets.set(0, n);
        root = new TrieNodeCompressed(new int[0], initialIntersectionOfNSets);
    }

    public void put_(XBitSet SSet, XBitSet NSet) {
        root.subtrieIntersectionOfNSets.and(NSet);
        root.subtrieUnionOfSSets.or(SSet);
        TrieNodeCompressed node = root;

        ArrayList<Integer> NSetMembersList = new ArrayList<Integer>(NSet.cardinality());
        for (int i = NSet.nextSetBit(0); i >= 0; i = NSet.nextSetBit(i+1)) {
            NSetMembersList.add(i);
        }
        Collections.sort(NSetMembersList, comparator);
        int[] key = new int[NSetMembersList.size()];
        for (int i=0; i<NSetMembersList.size(); i++) {
            key[i] = NSetMembersList.get(i);
        }

        while (key.length != 0) {
            node = node.getOrAddChildNode(key, SSet, NSet);
            key = Arrays.copyOfRange(key, node.key.length, key.length);
        }
        node.addSSet(SSet);
    }

    public void put(XBitSet SSet, XBitSet NSet) {
        if (all.size() == reorderSize) {
            reorderSize *= 10;
            comparator = new FrequencyComparator(stats);
            XBitSet initialIntersectionOfNSets = new XBitSet();
            initialIntersectionOfNSets.set(0, stats.length);
            root = new TrieNodeCompressed(new int[0], initialIntersectionOfNSets);
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
        root.printLatex(0, new ArrayList<Integer>(), featureFlags);
        System.out.println();
        System.out.println("\\end{forest}");
        System.out.println("\\end{document}");
    }
}
