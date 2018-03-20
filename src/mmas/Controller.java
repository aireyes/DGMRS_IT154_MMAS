package mmas;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.*;

public class Controller implements Initializable {

    public final int COUNTER_SIZE = 1500;

    public final ObservableList<String> opt_algo = FXCollections.observableArrayList(
            "FIRST_FIT",
            "BEST_FIT",
            "WORST_FIT"
    );

    int[] addresses;
    List<Process> processesQueue;

    int memorySize;
    int pointer;
    int counter;

    /* processes */
    List<Process> orig_processes;
    List<Process> processes;
    Memory memory;

    @FXML
    Label
            lbl_memorySize;

    @FXML
    Button
            btn_run,
            btn_continue;

    @FXML
    Slider
            slider_memorySize,
            slider_coalescing,
            slider_compaction;

    @FXML
    Label
            lbl_coalescing,
            lbl_compaction;

    @FXML
    Canvas
            canvas_processes,
            canvas_processesName,
            canvas_memory,
            canvas_counter;
    @FXML
    ComboBox combo_algo;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        /* initialize spinners */
        slider_memorySize.valueProperty().addListener((observable, oldValue, newValue) -> {
            slider_memorySize.setValue(newValue.intValue());
            memorySize = (int) slider_memorySize.getValue();
            lbl_memorySize.setText(String.valueOf(memorySize));
        });

        slider_coalescing.valueProperty().addListener((observable, oldValue, newValue) -> {
            slider_coalescing.setValue(newValue.intValue());
            lbl_coalescing.setText(String.valueOf(((int) slider_coalescing.getValue())));
        });

        slider_compaction.valueProperty().addListener((observable, oldValue, newValue) -> {
            slider_compaction.setValue(newValue.intValue());
            lbl_compaction.setText(String.valueOf((int) slider_compaction.getValue()));
        });

        combo_algo.setItems(opt_algo);
        combo_algo.getSelectionModel().select(0);

        try {
            reset();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void reset() throws InterruptedException {
        orig_processes = Queue.getProcesses("processes/processes_set1.csv"); /* set default processes */
        processes = new ArrayList<>();
        for (Process p : orig_processes) {
            processes.add(p);
        }
        displayProcessesName(orig_processes); /* display processes name */
        displayProcesses(orig_processes); /* display processes */
        displayMemory(); /* display memory */
        displayCounter();

        memorySize = (int) slider_memorySize.getValue();
        lbl_memorySize.setText(String.valueOf(memorySize));
        lbl_coalescing.setText(String.valueOf(((int) slider_coalescing.getValue())));
        lbl_compaction.setText(String.valueOf((int) slider_compaction.getValue()));

        pointer = 0;
        memory = new Memory();

        counter = 0;

        processesQueue = new ArrayList<>();
        addresses = new int[(int) slider_memorySize.getValue()];
        clearMemory();
        displayMemory();
    }

    public void displayProcessesName(List<Process> orig_processes) { /* display process name */
        GraphicsContext graphicsContext = canvas_processesName.getGraphicsContext2D();
        double height = canvas_processesName.getHeight();
        graphicsContext.setLineWidth(1);
        graphicsContext.setStroke(Color.BLACK);
        for (int i = 0; i < orig_processes.size(); i++) {
            double h = (height / orig_processes.size()) / 2;
            graphicsContext.strokeText("JOB: " + orig_processes.get(i).getName() + ", SIZE: " + orig_processes.get(i).getSize() + " KB", 0, (i * (height / orig_processes.size()) + h));
        }
        graphicsContext.stroke();
    }

    public void displayProcesses(List<Process> orig_processes) { /* display processes */
        int maxSize = Queue.getMaxTU(orig_processes);
        GraphicsContext graphicsContext = canvas_processes.getGraphicsContext2D();
        graphicsContext.setFill(Color.BLACK);
        graphicsContext.fillRect(0, 0, canvas_processes.getWidth(), canvas_processes.getHeight());

        double width = canvas_processes.getWidth();
        double height = canvas_processes.getHeight();
        graphicsContext.setLineWidth(1);
        graphicsContext.setStroke(Color.BLACK);
        /* draw processes */
        for (int i = 0; i < orig_processes.size(); i++) {
            for (int j = 0; j < orig_processes.get(i).getTimeUnit(); j++) {
                if (orig_processes.get(i).state == ProcessState.NEW) {
                    graphicsContext.setFill(Color.YELLOW);
                } else if (orig_processes.get(i).state == ProcessState.RUNNING) {
                    graphicsContext.setFill(Color.RED);
                } else if (orig_processes.get(i).state == ProcessState.TERMINATED) {
                    graphicsContext.setFill(Color.GREY);
                } else {
                    graphicsContext.setFill(Color.ORANGE);
                }
                graphicsContext.fillRect(j * (width / maxSize), i * (height / orig_processes.size()), width / maxSize, height / orig_processes.size());
                graphicsContext.strokeRect(j * (width / maxSize), i * (height / orig_processes.size()), width / maxSize, height / orig_processes.size());
            }
        }
        graphicsContext.stroke();
    }

    public void displayCounter() throws InterruptedException {
        if (counter % slider_compaction.getValue() == 0) {
            compact();
        }
        GraphicsContext graphicsContext = canvas_counter.getGraphicsContext2D();
        graphicsContext.setFill(Color.BLACK);
        graphicsContext.fillRect(0, 0, canvas_counter.getWidth(), canvas_counter.getHeight());
        for (int i = 0; i < COUNTER_SIZE; i++) {
            if (i < counter) {
                graphicsContext.setFill(Color.ORANGE);
            } else if (i == counter) {

                graphicsContext.setFill(Color.WHITE);
            } else {
                graphicsContext.setFill(Color.YELLOW);
            }
            graphicsContext.strokeRect((canvas_counter.getWidth() / 2) + (i * 50) - 25 - (counter * 50), (canvas_counter.getHeight() / 2) - 25, 50, 50);
            graphicsContext.fillRect((canvas_counter.getWidth() / 2) + (i * 50) - 25 - (counter * 50), (canvas_counter.getHeight() / 2) - 25, 50, 50);
            graphicsContext.strokeText(String.valueOf(i + 1), (canvas_counter.getWidth() / 2) + (i * 50) - (counter * 50) - ((double) String.valueOf(i + 1).length() / 2), canvas_counter.getHeight() / 2);
        }
    }

    @FXML
    public void btn_runClick(ActionEvent event) throws InterruptedException {
        reset();
        addresses = new int[(int) slider_memorySize.getValue()];
        btn_run.setDisable(true);
        slider_memorySize.setDisable(true);
        slider_coalescing.setDisable(true);
        slider_compaction.setDisable(true);
        new LoadProcessesThread().start();
    }

    public void displayMemory() {
        GraphicsContext graphicsContext = canvas_memory.getGraphicsContext2D();
        graphicsContext.setFill(Color.BLACK);
        graphicsContext.fillRect(0, 0, canvas_memory.getWidth(), canvas_memory.getHeight());
        for (Process p : orig_processes) {
            if (p.state == ProcessState.NEW)
                continue;

            double size = p.getSize();
            double base = p.getBlock().memoryBaseAddress;

            double memorySize = slider_memorySize.getValue();
            double y = (base / memorySize) * canvas_memory.getHeight();
            double h = (size / memorySize) * canvas_memory.getHeight();
            if (p.state == ProcessState.NEW) {
                graphicsContext.setFill(Color.YELLOW);
            } else if (p.state == ProcessState.RUNNING) {
                graphicsContext.setFill(Color.RED);
            } else if (p.state == ProcessState.TERMINATED) {
                graphicsContext.setFill(Color.BLACK);
                continue;
            } else {
                graphicsContext.setFill(Color.ORANGE);
            }
            graphicsContext.setStroke(Color.BLACK);
            graphicsContext.strokeRect(0, y, canvas_memory.getWidth(), h);
            graphicsContext.fillRect(0, y, canvas_memory.getWidth(), h);

            graphicsContext.setLineWidth(1);
            graphicsContext.setStroke(Color.BLACK);
            graphicsContext.strokeText(p.getName(), (canvas_memory.getWidth() / 2) - ((double) p.getName().length() / 2), y + (h / 2));
        }
    }

    public double addProcess(double size) {
        if (combo_algo.getSelectionModel().getSelectedItem().equals("FIRST_FIT")) {
            double free = 0;
            double base = -1;
            for (int i = 0; i < slider_memorySize.getValue(); i++) {
                if (addresses[i] == 0) {
                    if (free == 0)
                        base = i;
                    free++;
                    if (free >= size) {
                        occupy(base, size);
                        return base;
                    }
                } else {
                    free = 0;
                    base = -1;
                }
            }
            System.out.println("Sorry the memory is full.");
            return -1;
        } else {
            List<Double> sizes = new ArrayList<>();
            List<Double> bases = new ArrayList<>();
            double free = 0;
            double base = -1;
            for (int i = 0; i < slider_memorySize.getValue(); i++) {
                if (addresses[i] == 0) {
                    if (free == 0)
                        base = i;
                    free++;
                    if (free > 0 && i >= slider_memorySize.getValue() - 1 && free >= size) {
                        sizes.add(free);
                        bases.add(base);
                    }
                } else {
                    if (free > 0 && free >= size) {
                        sizes.add(free);
                        bases.add(base);
                    }
                    free = 0;
                    base = -1;
                }
            }
            if (!bases.isEmpty()) {
                if (combo_algo.getSelectionModel().getSelectedItem().equals("BEST_FIT")) {
                    double s = Collections.min(sizes);
                    double b = bases.get(sizes.indexOf(s));
                    if (s >= size) {
                        occupy(b, size);
                        return b;
                    }
                    return -1;
                } else if (combo_algo.getSelectionModel().getSelectedItem().equals("WORST_FIT")) {
                    double s = Collections.min(sizes);
                    double b = bases.get(sizes.indexOf(s));
                    if (s >= size) {
                        occupy(b, size);
                        return b;
                    }
                    return -1;
                } else {
                    System.out.println("Sorry the memory is full.");
                    return -1;
                }
            }

        }
        return -1;
    }

    public void occupy(double base, double size) {
        for (int i = 0; i < size; i++) {
            addresses[(int) base + i] = 1;
        }
    }

    public void clearMemory() {
        for (int i = 0; i < slider_memorySize.getValue(); i++) {
            addresses[i] = 0;
        }
    }

    public void removeProcess(double base, double size) {
        for (int i = 0; i < size; i++) {
            addresses[(int) base + i] = 0;
        }
    }

    public void compact() throws InterruptedException {
        System.out.println("Compacting");
        List<Block> blocks = new ArrayList<>();
        for (Process p : orig_processes) {
            if (p.state == ProcessState.READY || p.state == ProcessState.RUNNING)
                blocks.add(p.getBlock());
        }

        Collections.sort(blocks);
        int num = 0;
        Block prevBlock = null;
        for (Block b : blocks) {
            System.out.println(b.memoryBaseAddress);
            if (num == 0) {
                prevBlock = b;
                num++;
                continue;
            }
            if (prevBlock != null) {
                double size = b.memoryLimitAddress - b.memoryBaseAddress + 1;
                removeProcess(b.memoryBaseAddress, size);
                b.memoryBaseAddress = prevBlock.memoryLimitAddress + 1;
                b.memoryLimitAddress = b.memoryBaseAddress + size - 1;
                occupy(b.memoryBaseAddress, size);
                counter++;
                displayCounter();
                displayMemory();
                Thread.sleep(500);
            }
            prevBlock = b;
        }

    }


    class LoadProcessesThread extends Thread {

        boolean suspended;

        public LoadProcessesThread() {
            suspended = false;
            btn_continue.setOnAction(event -> {
                if (!suspended) pause();
                else res();
            });
        }

        @Override
        public void run() {
            try {
                process();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void process() throws InterruptedException {
            synchronized (this) {
                while (suspended)
                    wait();
            }
            for (Process p : processes) {
                synchronized (this) {
                    while (suspended)
                        wait();
                }
                double size = p.getSize();
                double base = addProcess(size);

                if (base == -1/* || p.getBlock() != null*/)
                    continue;

                p.state = ProcessState.READY;

                double memorySize = slider_memorySize.getValue();
                double y = (base / memorySize) * canvas_memory.getHeight();
                double h = (size / memorySize) * canvas_memory.getHeight();
                GraphicsContext graphicsContext = canvas_memory.getGraphicsContext2D();
                if (p.state == ProcessState.NEW) {
                    graphicsContext.setFill(Color.YELLOW);
                } else if (p.state == ProcessState.RUNNING) {
                    graphicsContext.setFill(Color.RED);
                } else if (p.state == ProcessState.TERMINATED) {
                    graphicsContext.setFill(Color.GREY);
                } else {
                    graphicsContext.setFill(Color.ORANGE);
                }
//                graphicsContext.setFill(Color.ORANGE);
                graphicsContext.setStroke(Color.BLACK);
                graphicsContext.strokeRect(0, y, canvas_memory.getWidth(), h);
                graphicsContext.fillRect(0, y, canvas_memory.getWidth(), h);
                p.setBlock(new Block(base, p.getSize() + base - 1));

                graphicsContext.setLineWidth(1);
                graphicsContext.setStroke(Color.BLACK);
                graphicsContext.strokeText(p.getName(), (canvas_memory.getWidth() / 2) - ((double) p.getName().length() / 2), y + (h / 2));
                processesQueue.add(p);

                displayProcesses(orig_processes);

                counter++;
                displayCounter();

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            for (Process p : processesQueue)
                processes.remove(p);

            if (!processesQueue.isEmpty()) {
                processesQueue.get(0).state = ProcessState.RUNNING;
                for (int i = 0; i < processesQueue.get(0).getTimeUnit(); i++) {
                    synchronized (this) {
                        while (suspended)
                            wait();
                    }
                    counter++;
                    displayCounter();
                    displayProcesses(orig_processes);
                    displayMemory();
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Process currentProcess = processesQueue.get(0);
                double base = currentProcess.getBlock().memoryBaseAddress;
                double size = currentProcess.getSize();
                double memorySize = slider_memorySize.getValue();
                double y = (base / memorySize) * canvas_memory.getHeight();
                double h = (size / memorySize) * canvas_memory.getHeight();
                GraphicsContext graphicsContext = canvas_memory.getGraphicsContext2D();
                graphicsContext.setFill(Color.BLACK);
                graphicsContext.setStroke(Color.BLACK);
                graphicsContext.strokeRect(0, y, canvas_memory.getWidth(), h);
                graphicsContext.fillRect(0, y, canvas_memory.getWidth(), h);

                System.out.println(currentProcess.getBlock().memoryBaseAddress);
                System.out.println(currentProcess.getSize());

                processesQueue.get(0).state = ProcessState.TERMINATED;
                displayProcesses(orig_processes);

                if (processesQueue.get(0).getBlock().memoryBaseAddress > 0 && addresses[(int) processesQueue.get(0).getBlock().memoryBaseAddress - 1] == 0)
                    coalesce();

//                if (processesQueue.get(0).getBlock().memoryLimitAddress < memorySize && addresses[(int) processesQueue.get(0).getBlock().memoryLimitAddress + 1] == 0)
                if (processesQueue.get(0).getBlock().memoryLimitAddress < memorySize - 1) {
                    if (addresses[(int) processesQueue.get(0).getBlock().memoryLimitAddress + 1] == 0)
                        coalesce();
                }

                removeProcess(processesQueue.get(0).getBlock().memoryBaseAddress, processesQueue.get(0).getSize());
                for (int i = 0; i < processesQueue.get(0).getSize() - 1; i++) {
                    synchronized (this) {
                        while (suspended)
                            wait();
                    }
                    System.out.println(addresses[(int) processesQueue.get(0).getBlock().memoryBaseAddress + i]);
                }
                processesQueue.remove(0);
                process();
            }
            if (processes.isEmpty() && processesQueue.isEmpty()) {
                btn_run.setDisable(false);
                slider_memorySize.setDisable(false);
                slider_coalescing.setDisable(false);
                slider_compaction.setDisable(false);
                return;
            }
        }

        public void coalesce() throws InterruptedException {
            for (int i = 0; i < Integer.parseInt(lbl_coalescing.getText()); i++) {
                synchronized (this) {
                    while (suspended)
                        wait();
                }
                System.out.println("Coalescing...");
                counter++;
                displayCounter();
                displayMemory();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void pause() {
            suspended = true;
        }

        public synchronized void res() {
            suspended = false;
            notify();
        }
    }
}