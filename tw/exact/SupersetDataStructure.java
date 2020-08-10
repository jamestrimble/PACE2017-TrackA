package tw.exact;

import java.util.ArrayList;

interface SupersetDataStructure {
    void put(XBitSet SSet, XBitSet NSet);

    void collectSuperblocks(XBitSet component, XBitSet neighbours,
            ArrayList<XBitSet> list);

    int[] getSizes();
}
