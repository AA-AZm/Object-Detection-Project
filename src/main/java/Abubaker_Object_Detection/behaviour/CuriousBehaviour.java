package Abubaker_Object_Detection.behaviour;

import bot.BotController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

// The weird HTML syntax is for Java docs

/**
 * A concrete implementation of the {@link Behaviour} interface that models a "curious" reaction
 * to objects. The bot attempts to position itself at an optimal distance (around 30 cm) from
 * the detected object, takes a picture, and then re-checks the distance before making minor
 * positional adjustments.
 */
public class CuriousBehaviour implements Behaviour {

    /**
     * Executes the "curious" behaviour using the provided {@link BotController}.
     * <p>
     * Steps:
     * <ul>
     *   <li>Set underlights to green as a signal for 'curiosity'.</li>
     *   <li>Measure distance to an object and, if necessary, move forward or backward until
     *       the object is roughly 30 cm away.</li>
     *   <li>Stop the bot and take a picture of the object, saving it to the local filesystem.</li>
     *   <li>Pause for 5 seconds, then measure the distance again. If the distance changes
     *       substantially, re-run the behaviour (recursive call).</li>
     *   <li>Finally, turn right twice by 60 degrees each.</li>
     * </ul>
     *
     * @param botController The controller providing bot actions (movement, sensor reading, etc.).
     * @return An integer status code indicating the result of execution (always 0 here).
     */
    @Override
    public int execute(BotController botController) {

        // Set the bot underlights to green to indicate "curiosity" status.
        botController.setUnderlights(new int[] {0, 255, 0});

        // Measure the initial distance to the nearest object.
        double distance = botController.measureDistance();

        // If the object is further than 30 cm, move forward until it is closer.
        if (distance > 30) {
            while (distance > 30) {
                botController.moveForward();
                try {
                    Thread.sleep(25);  // Brief pause to reduce CPU load.
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                distance = botController.measureDistance();  // Recalculate distance to decide whether to continue.
            }
            botController.stopMoving();
        }

        // If the object is closer than 30 cm, move backward until it is at least 30 cm away.
        if (distance < 30) {
            while (distance < 30) {
                botController.moveBackward();
                try {
                    Thread.sleep(25);  // Brief pause.
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                distance = botController.measureDistance();
            }
            botController.stopMoving();
        }

        // At this point, the distance is (ideally) near 30 cm, so stop moving completely.
        botController.stopMoving();

        // Turn off the underlights (set them to black/off) before taking the picture.
        botController.setUnderlights(new int[] {0, 0, 0});

        // Capture a picture of the object using the bot's camera.
        BufferedImage picture = botController.takeGreyPicture();

        // Build a filename based on the current system time.
        String fileName = "picture_" + System.currentTimeMillis() + ".jpg";
        File outputFile = new File(fileName);

        // Attempt to save the captured image as a JPG file.
        try {
            ImageIO.write(picture, "jpg", outputFile);
            System.out.println("Image saved successfully to " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error saving the image: " + e.getMessage());
        }

        // Pause for 5 seconds to provide a delay before re-checking the distance.
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Measure the distance again in case the object or the bot has shifted.
        distance = botController.measureDistance();

        // If the distance has changed significantly from ~30 cm (now >33 cm or <27 cm),
        // recursively call this behaviour to correct the position again.
        if ((distance > 34) || (distance < 26)) {
            execute(botController);
        }

        // After completing the behaviour (including any needed re-adjustments),
        // perform two consecutive 60-degree right turns as a final step.
        botController.turnRight60();
        botController.turnRight60();

        // Return an integer status code. Zero is used here to indicate normal completion.
        return 0;
    }
}
