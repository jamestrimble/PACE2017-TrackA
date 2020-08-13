package tw.exact;

import java.util.ArrayList;
import java.util.Arrays;

class NewTrieCompressed implements SupersetDataStructure, LatexPrintable {
    private int targetWidth;
    private TrieNodeCompressed root;
    private int size;

    NewTrieCompressed(int n, int targetWidth) {
        this.targetWidth = targetWidth;
        XBitSet initialIntersectionOfNSets = new XBitSet();
        initialIntersectionOfNSets.set(0, n);
        root = new TrieNodeCompressed(new int[0], initialIntersectionOfNSets);
    }

    public void put(XBitSet SSet, XBitSet NSet) {
        root.subtrieIntersectionOfNSets.and(NSet);
        root.subtrieUnionOfSSets.or(SSet);
        TrieNodeCompressed node = root;

        int[] key = NSet.toArray();

        while (key.length != 0) {
            node = node.getOrAddChildNode(key, SSet, NSet);
            key = Arrays.copyOfRange(key, node.key.length, key.length);
        }
        node.addSSet(SSet);
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
        int k = targetWidth + 1 - neighbours.cardinality();
        if (k >= 0) {
            root.query(component, neighbours, k, k, list);
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
