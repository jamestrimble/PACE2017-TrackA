package tw.exact;

interface LatexPrintable {
    void put(XBitSet SSet, XBitSet NSet);

    void printLatex(int featureFlags);
}
