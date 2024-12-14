import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.List;

/**
 * During the bathroom break, someone notices that these robots seem awfully similar to ones built and used at the North Pole. If they're the same type of robots, they should have a hard-coded Easter egg: very rarely, most of the robots should arrange themselves into a picture of a Christmas tree.
 * <p>
 * What is the fewest number of seconds that must elapse for the robots to display the Easter egg?
 */
public class RobotView extends Application {
    static Room room;
    static List<Robot> robots;
    private int iterations = 0;
    private Canvas canvas;
    private Text iterationLabel;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        canvas = new Canvas(room.width() * 6, room.height() * 6);
        iterationLabel = new Text("Iterations: 0");
        root.setCenter(canvas);
        root.setBottom(iterationLabel);

        Scene scene = new Scene(root);
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case SPACE, RIGHT -> {
                    var it = 1;
                    if (event.isShiftDown()) {
                        it = 101;
                    }
                    updateRobots(it,true);
                    drawRobots();
                    iterationLabel.setText("Iterations: " + iterations);
                }
                case LEFT -> {
                    var it = 1;
                    if (event.isShiftDown()) {
                        it = 101;
                    }
                    updateRobots(it, false);
                    drawRobots();
                    iterationLabel.setText("Iterations: " + iterations);
                }
                case null, default -> {
                }
            }
        });

        primaryStage.setScene(scene);
        primaryStage.setTitle("Robot View");
        primaryStage.show();

        drawRobots();
    }

    private void updateRobots(int n, boolean forward) {
        for(var i = 0; i < n; i++) {
            robots = robots.stream()
                    .map(r -> forward ? r.move(room) : r.moveBack(room)).toList();
        }
        iterations = forward ? iterations + n : iterations - n;
    }

    private void drawRobots() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setFill(Color.GREEN);
        for (Robot robot : robots) {
            gc.fillOval(robot.x() * 6, (room.height() - robot.y() - 1) * 6, 5, 5);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
