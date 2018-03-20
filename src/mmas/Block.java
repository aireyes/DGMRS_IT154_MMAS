package mmas;

public class Block implements Comparable<Block> {
    double memoryBaseAddress, memoryLimitAddress;
    public Block(double memoryBaseAddress, double memoryLimitAddress) {
        this.memoryBaseAddress = memoryBaseAddress;
        this.memoryLimitAddress = memoryLimitAddress;
    }

    @Override
    public int compareTo(Block o) {
        return (int) (memoryBaseAddress - o.memoryBaseAddress);
    }
}
