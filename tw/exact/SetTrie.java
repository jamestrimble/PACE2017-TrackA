package tw.exact;

import java.util.ArrayList;
import java.util.Arrays;

class SetTrie implements SupersetDataStructure {
    private int targetWidth;
    private SetTrieNode root;
    private int size;

    public SetTrie(int n, int targetWidth) {
        this.targetWidth = targetWidth;
        XBitSet initialIntersectionOfNSets = new XBitSet();
        initialIntersectionOfNSets.set(0, n);
        root = new SetTrieNode(-1, initialIntersectionOfNSets);
    }

    public void put(XBitSet SSet, XBitSet NSet) {
//        System.out.println(SSet.cardinality() + " " + NSet.cardinality());
        root.subtrieIntersectionOfNSets.and(NSet);
        root.subtrieUnionOfSSets.or(SSet);
        SetTrieNode node = root;
        // iterate over elements of SSet
        for (int i = SSet.nextSetBit(0); i >= 0; i = SSet.nextSetBit(i+1)) {
            node = node.getOrAddChildNode(i, SSet, NSet);
        }
        node.addSets(SSet, NSet);
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
            root.query(component, neighbours, k, list);
        }
    }

    public int[] getSizes() {
        return new int[] {size};
    }
}
