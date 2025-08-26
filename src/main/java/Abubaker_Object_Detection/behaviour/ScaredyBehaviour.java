package Abubaker_Object_Detection.behaviour;

import bot.BotController;
import Abubaker_Object_Detection.detection.ObjectDetector;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Represents a "Scaredy" behaviour for the bot. This behaviour drives the bot closer
 * to an object until it detects it within a specified range, then reacts fearfully
 * (e.g. blinking red lights, moving back, turning). It finally checks again whether
 * the object remains in close proximity. If so, it signals via return code.
 */
public class ScaredyBehaviour implements Behaviour {

    /** Responsible for detecting objects in captured images. */
    private final ObjectDetector objectDetector;

    /**
     * Constructs a new {@code ScaredyBehaviour} with the provided {@link ObjectDetector}.
     *
     * @param objectDetector The detector used to identify the presence of objects in images.
     */
    public ScaredyBehaviour(ObjectDetector objectDetector) {
        this.objectDetector = objectDetector;
    }

    /**
     * Executes the "Scaredy" behaviour sequence:
     * <ol>
     *   <li>Moves forward until an object is detected within ~50 cm.</li>
     *   <li>Sets underlights to red, captures a picture, and saves it locally.</li>
     *   <li>Blinks the underlights red 5 times, then moves backward briefly and
     *       rotates right to avoid the detected object.</li>
     *   <li>Moves forward for 3 seconds, stops, waits 5 seconds, and turns left.</li>
     *   <li>If the object is still within ~45 cm, takes another picture and uses the
     *       {@link ObjectDetector} to confirm its presence.</li>
     * </ol>
     * Returns {@code 1} if the object is still present after re-checking, otherwise returns {@code 0}.
     *
     * @param botController The bot's controller that handles movement, lights, and sensor readings.
     * @return An integer status code indicating whether an object is still detected (1) or not (0).
     */
    @Override
    public int execute(BotController botController) {

        // Measure initial distance to the nearest object.
        double distance = botController.measureDistance();

        // Continue moving forward until within ~40 cm of the object.
        while (distance > 40) {
            botController.moveForward();
            try {
                Thread.sleep(125);
            } catch (InterruptedException e) {
                // Convert to a runtime exception to terminate in case of interruption.
                throw new RuntimeException(e);
            }
            // Re-measure distance each iteration.
            distance = botController.measureDistance();
        }

        botController.stopMoving();
        // Capture a picture once the bot is within the specified distance.
        BufferedImage picture = botController.takeGreyPicture();

        // Set the bot's underlights to red to indicate a "scared" or alert state.
        botController.setUnderlights(new int[] {255, 0, 0});

        // Generate a filename based on the system time to avoid collisions.
        String fileName = "picture_" + System.currentTimeMillis() + ".jpg";
        File outputFile = new File(fileName);

        // Try saving the captured image as a JPG, printing a success or error message.
        try {
            ImageIO.write(picture, "jpg", outputFile);
            System.out.println("Image saved successfully to " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error saving the image: " + e.getMessage());
        }

        // Blink the red underlights 5 times (on/off cycle).
        for (int i = 0; i < 5; i++) {
            botController.setUnderlights(new int[] {0, 0, 0});
            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            botController.setUnderlights(new int[] {255, 0, 0});
            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        // Move backward to increase distance from the object.
        botController.moveBackward();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Turn right three times (3x60° = 180°) to face away from the object.
        botController.turnRight60();
        botController.turnRight60();
        botController.turnRight60();

        // Move forward again for a short duration to further distance from the object.
        botController.moveForward();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Stop the bot and wait 5 seconds before checking proximity again.
        botController.stopMoving();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Turn left once (60°). Then recheck distance to see if the object is still near.
        botController.turnLeft60();
        distance = botController.measureDistance();

        // If the object is still close (~under 35 cm), capture another image
        // and use the detector to confirm. If present, return status code 1.
        if (distance < 35) {
            BufferedImage picture2 = botController.takeGreyPicture();
            if (objectDetector.detectObject(picture2)) {
                return 1;
            }
        }
        botController.setUnderlights(new int[] {0, 0, 255});

        // Return 0 if no object is detected at the final check or if the distance is safe.
        return 0;
    }
}
