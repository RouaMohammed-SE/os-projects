import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

// -------------------- Semaphore --------------------
class Semaphore {
    protected int value = 0;
    public Semaphore() { value = 0; }
    public Semaphore(int initial) { this.value = initial; }
    public synchronized int getValue() { return value; }
    public synchronized void p() {
        value--;
        if (value < 0) {
            try { wait(); } catch (InterruptedException ignored) {}
        }
    }
    public synchronized void v() {
        value++;
        if (value <= 0) notify();
    }
}

// -------------------- Car (Producer) --------------------
class Car extends Thread {
    private final String carName;
    private final Queue<String> queue;
    private final Semaphore full, empty, mutex, pumps;
    private final ServiceStation gui;

    public Car(String carName, Queue<String> queue, Semaphore full,
               Semaphore empty, Semaphore mutex, Semaphore pumps, ServiceStation gui) {
        this.carName = carName;
        this.queue = queue;
        this.full = full;
        this.empty = empty;
        this.mutex = mutex;
        this.pumps = pumps;
        this.gui = gui;
    }

    @Override
    public void run() {
        empty.p();
        mutex.p();

        boolean waiting = pumps.getValue() <= 0;
        gui.log(waiting
                ? carName + " arrived and waiting"
                : carName + " arrived");

        queue.add(carName);
        Platform.runLater(() -> gui.addWaitingLabel(carName));

        mutex.v();
        full.v();
    }
}

// -------------------- Pump (Consumer) --------------------
class Pump extends Thread {
    private final int pumpId;
    private final Queue<String> queue;
    private final Semaphore full, empty, mutex, pumps;
    private final ServiceStation gui;
    private static final int SERVICE_MS = 600;

    public Pump(int pumpId, Queue<String> queue, Semaphore full,
                Semaphore empty, Semaphore mutex, Semaphore pumps,
                ServiceStation gui) {
        this.pumpId = pumpId;
        this.queue = queue;
        this.full = full;
        this.empty = empty;
        this.mutex = mutex;
        this.pumps = pumps;
        this.gui = gui;
    }

    @Override
    public void run() {
        while (true) {
            full.p();
            mutex.p();

            String item = queue.poll();
            mutex.v();
            empty.v();

            if (item == null) continue;

            if (item.equals("DONE")) {
                break;
            }

            pumps.p();
            gui.log("Pump " + pumpId + ": " + item + " Occupied");

            Platform.runLater(() -> {
                gui.removeWaitingLabel(item);
                gui.setPumpOccupied(pumpId, item);
            });

            gui.log("Pump " + pumpId + ": " + item + " login");
            gui.log("Pump " + pumpId + ": " + item + " begins service at Bay " + pumpId);

            try { Thread.sleep(SERVICE_MS); }
            catch(Exception ignored) {}

            gui.log("Pump " + pumpId + ": " + item + " finishes service");

            pumps.v();
            Platform.runLater(() -> gui.setPumpFree(pumpId));
        }
    }
}

// -------------------- Main GUI --------------------
public class ServiceStation extends Application {

    private TextArea logArea;
    private FlowPane waitingFlow;
    private Label currentCountLabel;
    private final Map<String, Label> waitingLabels = new HashMap<>();
    private final Map<Integer, VBox> pumpBoxes = new HashMap<>();

    @Override
    public void start(Stage stage) {
        Image icon = new Image("file:D:/College/Year-3/OS/Assignments/GasStationSimulator/GasStation.jpg");
        stage.getIcons().add(icon);

        // Input controls
        Label capLabel = new Label("Waiting Area Capacity (1-10):");
        TextField capField = new TextField("5");
        Label pumpLabel = new Label("Number of Pumps:");
        TextField pumpField = new TextField("3");
        Label carsLabel = new Label("Car Order (e.g. C1 C2 C3 C4):");
        TextField carsField = new TextField("C1 C2 C3 C4 C5");
        Button startBtn = new Button("Start Simulation");

        // Waiting Area
        Label waitingTitle = new Label("Cars Waiting (Max capacity shown):");
        waitingFlow = new FlowPane(8, 8);
        waitingFlow.setPadding(new Insets(8));
        waitingFlow.setPrefHeight(120);
        waitingFlow.setStyle("-fx-background-color:white;-fx-border-color:#e0e0e0;-fx-border-radius:6;");
        currentCountLabel = new Label("Current: 0/0");
        currentCountLabel.setPadding(new Insets(6,0,0,0));

        VBox left = new VBox(8, waitingTitle, waitingFlow, currentCountLabel);
        left.setPadding(new Insets(8));
        left.setPrefWidth(360);

        // Pumps Panel setup
        Label pumpsTitle = new Label("Service Bays (Pumps):");
        pumpsTitle.setStyle("-fx-font-weight:bold; -fx-font-size:14;");
        GridPane pumpsGrid = new GridPane();
        pumpsGrid.setHgap(12);
        pumpsGrid.setVgap(12);
        pumpsGrid.setPadding(new Insets(6));
        ScrollPane pumpScroll = new ScrollPane(pumpsGrid);
        pumpScroll.setFitToWidth(true);
        pumpScroll.setPrefHeight(400);
        VBox right = new VBox(8, pumpsTitle, pumpScroll);
        right.setPadding(new Insets(8));
        right.setPrefWidth(360);

        HBox center = new HBox(12, left, right);
        center.setPadding(new Insets(8));

        // Log
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(200);

        // Top Grid for inputs
        GridPane topGrid = new GridPane();
        topGrid.setHgap(10);
        topGrid.setVgap(10);
        topGrid.add(capLabel, 0, 0);
        topGrid.add(capField, 1, 0);
        topGrid.add(pumpLabel, 0, 1);
        topGrid.add(pumpField, 1, 1);
        topGrid.add(carsLabel, 0, 2);
        topGrid.add(carsField, 1, 2);
        topGrid.add(startBtn, 1, 3);

        VBox root = new VBox(10, topGrid, center, new Label("Simulation Log:"), logArea);
        root.setPadding(new Insets(12));
        Scene scene = new Scene(root, 780, 640);
        stage.setScene(scene);
        stage.setTitle("Gas Station Simulation (GUI)");
        stage.show();

        // Start button action
        startBtn.setOnAction(e -> {
            try {
                int waitingCapacity = Integer.parseInt(capField.getText().trim());
                int numPumps = Integer.parseInt(pumpField.getText().trim());
                String[] cars = carsField.getText().trim().split("\\s+");
                if (waitingCapacity < 1 || waitingCapacity > 10) {
                    alert("Waiting area capacity must be between 1 and 10!");
                    return;
                }
                startBtn.setDisable(true);
                startSimulation(waitingCapacity, numPumps, cars, pumpsGrid, () -> startBtn.setDisable(false));
            } catch (Exception ex) {
                alert("Invalid input values!");
            }
        });
    }

    private void alert(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.showAndWait();
    }

    // -------------------- startSimulation --------------------
    private void startSimulation(int waitingCapacity, int numPumps,
                                 String[] cars, GridPane pumpsGrid, Runnable onFinish) {

        Platform.runLater(() -> {
            waitingFlow.getChildren().clear();
            waitingLabels.clear();
            pumpBoxes.clear();
            pumpsGrid.getChildren().clear();
            logArea.clear();
            currentCountLabel.setText("Current: 0/" + cars.length);

            for (int i = 1; i <= numPumps; i++) {
                VBox pumpBox = createPumpBox(i);
                pumpBoxes.put(i, pumpBox);
                int col = (i - 1) % 2;
                int row = (i - 1) / 2;
                pumpsGrid.add(pumpBox, col, row);
            }
        });

        Queue<String> queue = new LinkedList<>();
        Semaphore mutex = new Semaphore(1);
        Semaphore full = new Semaphore(0);
        Semaphore empty = new Semaphore(waitingCapacity);
        Semaphore pumps = new Semaphore(numPumps);

        // Start pump threads
        List<Thread> pumpThreads = new ArrayList<>();
        for (int i = 1; i <= numPumps; i++) {
            Pump p = new Pump(i, queue, full, empty, mutex, pumps, this);
            pumpThreads.add(p);
            p.start();
        }

        // Start car threads
        List<Thread> carThreads = new ArrayList<>();
        for (String carName : cars) {
            Car c = new Car(carName, queue, full, empty, mutex, pumps, this);
            carThreads.add(c);
            c.start();
        }

        // Watcher thread to send poison pills
        Thread watcher = new Thread(() -> {
            try {
                for (Thread t : carThreads) t.join();

                // Send one DONE per pump
                for (int i = 0; i < numPumps; i++) {
                    empty.p();
                    mutex.p();
                    queue.add("DONE");
                    mutex.v();
                    full.v();
                }

                for (Thread t : pumpThreads) t.join();

            } catch (Exception ignored) {}

            Platform.runLater(() -> {
                log("All cars processed; simulation ends");
                onFinish.run();
            });
        });

        watcher.start();
    }

    // -------------------- UI Helpers --------------------
    public void addWaitingLabel(String carName) {
        Label lbl = new Label(carName);
        lbl.setStyle("-fx-background-color:#ffb84d;-fx-padding:8 12;-fx-background-radius:6;-fx-font-weight:bold;");
        waitingLabels.put(carName, lbl);
        waitingFlow.getChildren().add(lbl);
        updateCurrentCount();
    }

    public void removeWaitingLabel(String carName) {
        Label lbl = waitingLabels.remove(carName);
        if (lbl != null) waitingFlow.getChildren().remove(lbl);
        updateCurrentCount();
    }

    public void updateCurrentCount() {
        int current = waitingFlow.getChildren().size();
        String prev = currentCountLabel.getText();
        String total = prev.contains("/") ? prev.split("/")[1] : "?";
        currentCountLabel.setText("Current: " + current + "/" + total);
    }

    public void setPumpOccupied(int pumpIndex, String carName) {
        VBox box = pumpBoxes.get(pumpIndex);
        if (box == null) return;
        Label status = (Label) box.lookup("#status");
        status.setText(carName + " In Service");
        status.setStyle("-fx-background-color:#1ec06d;-fx-text-fill:white;-fx-padding:6;-fx-background-radius:6;");
    }

    public void setPumpFree(int pumpIndex) {
        VBox box = pumpBoxes.get(pumpIndex);
        if (box == null) return;
        Label status = (Label) box.lookup("#status");
        status.setText("Free");
        status.setStyle("-fx-background-color:#e9eefc;-fx-text-fill:#333;-fx-padding:6;-fx-background-radius:6;");
    }

    private VBox createPumpBox(int pumpIndex) {
        Label title = new Label("Pump " + pumpIndex);
        title.setStyle("-fx-font-weight:bold;-fx-padding:4 0 6 0;");
        Label status = new Label("Free");
        status.setId("status");
        status.setStyle("-fx-background-color:#e9eefc;-fx-text-fill:#333;-fx-padding:6;-fx-background-radius:6;");
        Circle c = new Circle(8, Color.web("#ff6b6b"));
        Label mini = new Label("idle");
        mini.setStyle("-fx-font-size:11;");
        HBox iconRow = new HBox(6, c, mini);
        iconRow.setAlignment(Pos.CENTER_LEFT);
        VBox box = new VBox(6, title, status, iconRow);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(10));
        box.setPrefWidth(180);
        box.setStyle("-fx-background-color:#ffffff;-fx-border-color:#e0e0e0;-fx-border-radius:8;-fx-background-radius:8;");
        return box;
    }

    public void log(String msg) {
        System.out.println(msg);
        Platform.runLater(() -> logArea.appendText(msg + "\n"));
    }

    public static void main(String[] args) {
        launch(args);
    }
}