package mmas;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Queue {

    public static List<Block> blocks = new ArrayList<>();

    public static List<Process> getProcesses(String fileName) {
        List<Process> processes = new ArrayList<>();
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";

        try {
            br = new BufferedReader(new FileReader(fileName));
            while ((line = br.readLine()) != null) {
                String[] process = line.split(cvsSplitBy);
                Process p = new Process(process[0], Integer.parseInt(process[1]), Integer.parseInt(process[2]));
                System.out.println(p.getName() + " " + p.getSize() + " " + p.getTimeUnit());
                processes.add(p);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return processes;
    }

    public static int getMaxSize(List<Process> processes) {
        int[] sizes = new int[processes.size()];
        int i = 0;
        for (Process p : processes) {
            sizes[i++] = p.getSize();
        }
        Arrays.sort(sizes);
        return sizes[processes.size() - 1];
    }

    public static int getMaxTU(List<Process> processes) {
        int[] sizes = new int[processes.size()];
        int i = 0;
        for (Process p : processes) {
            sizes[i++] = p.getTimeUnit();
        }
        Arrays.sort(sizes);
        return sizes[processes.size() - 1];
    }
}
