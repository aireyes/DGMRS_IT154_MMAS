package mmas;

public class Process {
    private String name;
    private int size, timeUnit;
    public Block block;
    ProcessState state;

    public Process(String name, int size, int timeUnit) {
        this.name = name;
        this.size = size;
        this.timeUnit = timeUnit;
        this.state = ProcessState.NEW;
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    public int getTimeUnit() {
        return timeUnit;
    }

    public Block getBlock() {
        return block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }
}
