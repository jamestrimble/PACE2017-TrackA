package tw.exact;

import java.util.ArrayList;
import java.util.Arrays;

public class NewTrie {
    int graph_n;
    int targetWidth;
    TrieNode root;

    public class TrieNode {
        TrieNode[] successors = null;
        XBitSet subtrieUnionOfSets;
        XBitSet subtrieIntersectionOfNds;
        int key;
        XBitSet[] vals = null;
        XBitSet nbs = null;
        int graph_n;

        int numValsInSubtrie;
        // As an optimisation, we store one (S, N(S)) pair that appears in this
        // subtrie.  If numValsInSubtrie==1, we can use this pair and avoid
        // recursing further at query time.
        XBitSet verticesInSubTrie;
        XBitSet nbsInSubTrie;

        TrieNode(int key, int graph_n) {
            this.key = key;
            this.graph_n = graph_n;
            subtrieIntersectionOfNds = new XBitSet(graph_n);
            subtrieUnionOfSets = new XBitSet(graph_n);
            subtrieIntersectionOfNds.set(0, graph_n);
        }

        TrieNode addSuccessorNode(int key) {
            TrieNode node = new TrieNode(key, graph_n);
            if (successors == null) {
                successors = new TrieNode[1];
                successors[0] = node;
            } else {
                successors = Arrays.copyOf(successors, successors.length + 1);
                successors[successors.length - 1] = node;
            }
            return node;
        }

        TrieNode getOrAddSuccessorNode(int key) {
            if (successors != null) {
                for (TrieNode succ : successors) {
                    if (succ.key == key) {
                        return succ;
                    }
                }
            }
            return addSuccessorNode(key);
        }

        private void recordSetInSubtrie(XBitSet vertices, XBitSet neighbours) {
            subtrieIntersectionOfNds.and(neighbours);
            subtrieUnionOfSets.or(vertices);
            ++numValsInSubtrie;
            if (numValsInSubtrie == 1) {
                verticesInSubTrie = vertices;
                nbsInSubTrie = neighbours;
            }
        }

        private void getAllAlmostSubsetsHelper(
                XBitSet vertices,
                XBitSet nd,
                int maxNdUnionSize,
                int numExtrasPermitted,
                ArrayList<XBitSet> out_list) {
            if (nd.unionWith(subtrieIntersectionOfNds).cardinality() > maxNdUnionSize) {
                return;
            }
            if (!vertices.isSubset(subtrieUnionOfSets)) {
                return;
            }
            if (numValsInSubtrie == 1) {
                if (nd.unionWith(nbsInSubTrie).cardinality() <= maxNdUnionSize &&
                        vertices.isSubset(verticesInSubTrie)) {
                    out_list.add(nbsInSubTrie);
                }
                return;
            }
            if (vals != null) {
                if (nd.unionWith(nbs).cardinality() <= maxNdUnionSize) {
                    for (XBitSet val : vals) {
                        if (vertices.isSubset(val)) {
                            out_list.add(nbs);
                            break;
                        }
                    }
                }
            }
            if (successors != null) {
                for (TrieNode succ : successors) {
                    int newNumExtrasPermitted = numExtrasPermitted;
                    if (!nd.get(succ.key)) {
                        --newNumExtrasPermitted;
                    }
                    if (newNumExtrasPermitted >= 0) {
                        succ.getAllAlmostSubsetsHelper(vertices, nd, maxNdUnionSize, newNumExtrasPermitted, out_list);
                    }
                }
            }
        }
    }

    public NewTrie(int n, int targetWidth) {
        this.graph_n = n;
        this.targetWidth = targetWidth;

        root = new TrieNode(-1, n);
    }

    public void put(XBitSet vertices, XBitSet neighbours) {
        root.recordSetInSubtrie(vertices, neighbours);
        TrieNode node = root;
        // iterate over elements of neighbours
        for (int i = neighbours.nextSetBit(0); i >= 0; i = neighbours.nextSetBit(i+1)) {
            node = node.getOrAddSuccessorNode(i);
            node.recordSetInSubtrie(vertices, neighbours);
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

    public void put(XBitSet vertices, int neighborSize, XBitSet neighbours) {
        put(vertices, neighbours);
    }

    // for debugging
    public void showList(ArrayList<XBitSet> bitsets) {
        for (XBitSet bs : bitsets) {
            for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i+1)) {
                System.out.print(i + " ");
            }
            System.out.println();
        }
        System.out.println("--------------");
    }

    public void collectSuperblocks(XBitSet component, XBitSet neighbours,
            ArrayList<XBitSet> list) {
        root.getAllAlmostSubsetsHelper(component, neighbours, targetWidth + 1, targetWidth + 1 - neighbours.cardinality(), list);
    }

    public int[] getSizes() {
        // a dummy implementation
        int sizes[] = new int[1];
        sizes[0] = -1;
        return sizes;
    }
}
