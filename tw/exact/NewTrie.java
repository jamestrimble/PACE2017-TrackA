package tw.exact;

import java.util.ArrayList;
import java.util.Arrays;

class NewTrie {
    private int n;
    private int targetWidth;
    private TrieNode root;

    private class TrieNode {
        private TrieNode[] children = null;
        private XBitSet subtrieUnionOfSets;
        private XBitSet subtrieIntersectionOfNds;
        private int key;
        private XBitSet[] vals = null;
        private XBitSet nbs = null;

        TrieNode(int key, int n) {
            this.key = key;
            subtrieIntersectionOfNds = new XBitSet(n);
            subtrieUnionOfSets = new XBitSet(n);
            subtrieIntersectionOfNds.set(0, n);
        }

        private TrieNode addChildNode(int key, int n) {
            TrieNode node = new TrieNode(key, n);
            if (children == null) {
                children = new TrieNode[1];
                children[0] = node;
            } else {
                children = Arrays.copyOf(children, children.length + 1);
                children[children.length - 1] = node;
            }
            return node;
        }

        TrieNode getOrAddChildNode(int key, int n) {
            if (children != null) {
                for (TrieNode child : children) {
                    if (child.key == key) {
                        return child;
                    }
                }
            }
            return addChildNode(key, n);
        }

        private void getAllAlmostSubsetsHelper(
                XBitSet vertices,
                XBitSet nd,
                int maxNdUnionSize,
                int ndUnionSize,
                ArrayList<XBitSet> out_list) {
            if (nd.unionWith(subtrieIntersectionOfNds).cardinality() > maxNdUnionSize) {
                return;
            }
            if (!vertices.isSubset(subtrieUnionOfSets)) {
                return;
            }
            if (vals != null) {
                if (ndUnionSize <= maxNdUnionSize) {
                    for (XBitSet val : vals) {
                        if (vertices.isSubset(val)) {
                            out_list.add(nbs);
                            break;
                        }
                    }
                }
            }
            if (children != null) {
                for (TrieNode child : children) {
                    int newNdUnionSize = ndUnionSize;
                    if (!nd.get(child.key)) {
                        ++newNdUnionSize;
                    }
                    if (newNdUnionSize <= maxNdUnionSize) {
                        child.getAllAlmostSubsetsHelper(vertices, nd, maxNdUnionSize, newNdUnionSize, out_list);
                    }
                }
            }
        }
    }

    NewTrie(int n, int targetWidth) {
        this.n = n;
        this.targetWidth = targetWidth;
        root = new TrieNode(-1, n);
    }

    void put(XBitSet vertices, XBitSet neighbours) {
        root.subtrieIntersectionOfNds.and(neighbours);
        root.subtrieUnionOfSets.or(vertices);
        TrieNode node = root;
        // iterate over elements of neighbours
        for (int i = neighbours.nextSetBit(0); i >= 0; i = neighbours.nextSetBit(i+1)) {
            node = node.getOrAddChildNode(i, n);
            node.subtrieIntersectionOfNds.and(neighbours);
            node.subtrieUnionOfSets.or(vertices);
        }
        if (node.vals == null) {
            node.vals = new XBitSet[1];
            node.vals[0] = vertices;
            node.nbs = neighbours;
        } else {
            node.vals = Arrays.copyOf(node.vals, node.vals.length + 1);
            node.vals[node.vals.length - 1] = vertices;
        }
    }

    void put(XBitSet vertices, int neighborSize, XBitSet neighbours) {
        put(vertices, neighbours);
    }

    // for debugging
    void showList(ArrayList<XBitSet> bitsets) {
        for (XBitSet bs : bitsets) {
            for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i+1)) {
                System.out.print(i + " ");
            }
            System.out.println();
        }
        System.out.println("--------------");
    }

    void collectSuperblocks(XBitSet component, XBitSet neighbours,
            ArrayList<XBitSet> list) {
        root.getAllAlmostSubsetsHelper(component, neighbours, targetWidth + 1, neighbours.cardinality(), list);
    }

    int[] getSizes() {
        // a dummy implementation
        int sizes[] = new int[1];
        sizes[0] = -1;
        return sizes;
    }
}
