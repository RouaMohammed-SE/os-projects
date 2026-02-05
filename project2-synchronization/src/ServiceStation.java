import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

class Semaphore {
    protected int value = 0;

    protected Semaphore() {
        value = 0;
    }

    protected Semaphore(int initial) {
        this.value = initial;
    }

    public synchronized int getValue() {
        return value;
    }

    public synchronized void p() {
        value--;
        if (value < 0) {
            try {
                wait();
            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();
            }
        }
    }


    public synchronized void v() {
        value++;
        if (value <= 0) {
            notify();
        }
    }
}

class Car extends Thread {
    private final String carName;
    private final Queue<String> queue;
    private final Semaphore full;
    private final Semaphore empty;
    private final Semaphore mutex;
    private final Semaphore pumps;
    private final int waitingCapacity;

    public Car(String carName, Queue<String> queue, Semaphore full, Semaphore empty,
               Semaphore mutex, Semaphore pumps, int waitingCapacity) {
        this.carName = carName;
        this.queue = queue;
        this.full = full;
        this.empty = empty;
        this.mutex = mutex;
        this.pumps = pumps;
        this.waitingCapacity = waitingCapacity;
    }

    @Override
    public void run() {
        empty.p();
        mutex.p();

        if (pumps.getValue() <= 0) {
            System.out.println(carName + " arrived and waiting");
        } else {
            System.out.println(carName + " arrived");
        }

        queue.add(carName);

        mutex.v();
        full.v();
    }
}

class Pump extends Thread {
    private final int pumpId;
    private final Queue<String> queue;
    private final Semaphore full;
    private final Semaphore empty;
    private final Semaphore mutex;
    private final Semaphore pumps;

    private static final int SERVICE_TIME_MS = 500;

    public Pump(int pumpId, Queue<String> queue, Semaphore full, Semaphore empty,
                Semaphore mutex, Semaphore pumps) {
        this.pumpId = pumpId;
        this.queue = queue;
        this.full = full;
        this.empty = empty;
        this.mutex = mutex;
        this.pumps = pumps;
    }

    @Override
    public void run() {
        while (true) {
            full.p();
            mutex.p();

            String car = null;
            if (!queue.isEmpty()) {
                car = queue.remove();
            }

            mutex.v();
            empty.v();

            if (car == null) {
                continue;
            }
            if (car.equals("DONE")) {

                break;
            }

            // Acquire a bay (counting semaphore) before starting service
            pumps.p();
            System.out.println("Pump " + pumpId + ": " + car + " Occupied");

            // Login and begin service logs
            System.out.println("Pump " + pumpId + ": " + car + " login");
            System.out.println("Pump " + pumpId + ": " + car + " begins service at Bay " + pumpId);

            // Simulate service time
            try {
                Thread.sleep(SERVICE_TIME_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Finish service and release bay
            System.out.println("Pump " + pumpId + ": " + car + " finishes service");
            pumps.v();

            System.out.println("Pump " + pumpId + ": Bay " + pumpId + " is now free");
        }
    }
}


public class ServiceStation {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);


        System.out.print("Enter waiting area capacity (1-10): ");
        int waitingCapacity = scanner.nextInt();
        while (waitingCapacity < 1 || waitingCapacity > 10) {
            System.out.println("Invalid!! Waiting area capacity must be between 1 and 10.");
            System.out.print("Enter waiting area capacity (1-10): ");
            waitingCapacity = scanner.nextInt();
        }


        System.out.print("Enter number of pumps: ");
        int numPumps = scanner.nextInt();
        while (numPumps < 1) {
            System.out.println("Invalid!! Number of pumps must be >= 1.");
            System.out.print("Enter number of pumps: ");
            numPumps = scanner.nextInt();
        }
        scanner.nextLine();

        System.out.print("Enter car order (e.g., C1 C2 C3 C4 C5): ");
        String carsLine = scanner.nextLine().trim();
        if (carsLine.isEmpty()) {
            System.out.println("No cars provided. Exiting.");
            scanner.close();
            return;
        }
        String[] carNames = carsLine.split("\\s+");
        int totalCars = carNames.length;

        Queue<String> queue = new LinkedList<>();
        Semaphore mutex = new Semaphore(1);
        Semaphore full = new Semaphore(0);
        Semaphore empty = new Semaphore(waitingCapacity);
        Semaphore pumps = new Semaphore(numPumps);

        Thread[] pumpThreads = new Thread[numPumps];
        for (int i = 0; i < numPumps; i++) {
            pumpThreads[i] = new Pump(i + 1, queue, full, empty, mutex, pumps);
            pumpThreads[i].start();
        }

        Thread[] carThreads = new Thread[totalCars];
        for (int i = 0; i < totalCars; i++) {
            carThreads[i] = new Car(carNames[i], queue, full, empty, mutex, pumps, waitingCapacity);
            carThreads[i].start();

        }


        for (int i = 0; i < totalCars; i++) {
            try {
                carThreads[i].join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        for (int i = 0; i < numPumps; i++) {
            empty.p();
            mutex.p();
            queue.add("DONE");
            mutex.v();
            full.v();
        }

        for (int i = 0; i < numPumps; i++) {
            try {
                pumpThreads[i].join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        scanner.close();
        System.out.println("All cars processed; simulation ends");
    }
}