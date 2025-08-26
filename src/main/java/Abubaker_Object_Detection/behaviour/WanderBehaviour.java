package Abubaker_Object_Detection.behaviour;

import bot.BotController;
import Abubaker_Object_Detection.detection.ObjectDetector;

import java.awt.image.BufferedImage;

/**
 * The {@code WanderBehaviour} represents a default or "wandering" state for the bot. It
 * instructs the bot to move forward for up to five seconds, occasionally checking for obstacles.
 * If an obstacle is found, it determines whether it is an "object of interest" via
 * {@link ObjectDetector}, or something else (e.g., wall or block). Based on these findings, it
 * may return control to the orchestrator or attempt to navigate around the obstacle.
 * <p>
 * <strong>Key Points:</strong>
 * <ul>
 *   <li>Moves forward with underlights set to blue.</li>
 *   <li>Measures distance frequently. If a distance &lt; 50 cm is detected, takes a picture
 *       and uses the detector to decide if it's an object or a wall.</li>
 *   <li>If it's an object, returns {@code 1}; otherwise attempts multiple turning strategies
 *       to find an unblocked path.</li>
 *   <li>If the bot cannot find a path after a certain number of turns, it reverts to a slower
 *       rotation, meticulously checking distance to avoid being fully blocked. If it remains blocked,
 *       it returns {@code -1}.</li>
 *   <li>After five seconds of wandering without encountering an object, it makes a final 60-degree
 *       turn, waits, and recursively calls {@code execute} again to continue its wandering cycle.</li>
 * </ul>
 */
public class WanderBehaviour implements Behaviour{

    /** Used for detecting specific objects in captured images. */
    private final ObjectDetector objectDetector;

    /**
     * Creates a {@code WanderBehaviour} with a given {@link ObjectDetector}.
     *
     * @param objectDetector An object detection service for identifying known items in images.
     */
    public WanderBehaviour(ObjectDetector objectDetector) {
        this.objectDetector = objectDetector;
    }

    /**
     * Causes the bot to move around, checking for obstacles and attempting to navigate if blocked.
     * <p>
     * Detailed Flow:
     * <ol>
     *   <li>Sets underlights to blue and moves forward.</li>
     *   <li>Continuously checks the distance until 5 seconds pass. If any obstacle is detected
     *       at &lt; 50 cm:</li>
     *   <ul>
     *     <li>Captures an image, checks via {@link ObjectDetector} if it is a recognised object.</li>
     *     <li>If recognised, returns {@code 1} to signal object detection.</li>
     *     <li>If not recognised (assumed to be a wall/other obstacle), attempts to turn
     *         in 60-degree increments to find a path.</li>
     *     <li>If a path is not found after 12 increments, the bot uses finer rotations,
     *         checking distance each time, eventually returning {@code 0} if an open path
     *         is found, or {@code -1} if fully blocked.</li>
     *   </ul>
     *   <li>If 5 seconds elapse without encountering an object, the bot turns right 60 degrees, waits,
     *       and calls this method again (recursively) to continue wandering.</li>
     * </ol>
     *
     * @param botController Provides movement, distance measurement, and imaging capabilities.
     * @return An integer code signaling the encounter:
     *         <ul>
     *           <li>{@code 1} if a recognised object is detected.</li>
     *           <li>{@code 0} for normal completion or if an unrecognised obstacle is navigated.</li>
     *           <li>{@code -1} if the bot cannot find any path.</li>
     *         </ul>
     */
    @Override
    public int execute(BotController botController) {

        // Set Bot's underlights to blue (indicating "wandering" mode).
        int[] rgbBlue = {0, 0, 255};
        botController.setUnderlights(rgbBlue);

        // Record the current system time to enforce a 5-second wandering limit.
        long startTime = System.currentTimeMillis();

        // Begin forward movement.
        botController.moveForward();

        // Loop for up to 5 seconds, regularly checking the distance.
        while (System.currentTimeMillis() - startTime < 5000) {
            try {
                // Sleep briefly (125 ms) to avoid overburdening the hardware with distance checks.
                Thread.sleep(125);
                if(Thread.currentThread().isInterrupted()){return 0;}
            } catch (InterruptedException e) {
                // Convert the checked exception to a RuntimeException for simplicity.
                throw new RuntimeException(e);
            }

            // Measure distance to detect if an obstacle is within 50 cm.
            double distance = botController.measureDistance();

            // If the bot is too close to something:
            if (distance < 40) {

                // Capture an image of what's in front of the bot.
                BufferedImage potObject = botController.takeGreyPicture();
                if(Thread.currentThread().isInterrupted()){return 0;}
                // Check if the captured image contains a recognised object of interest.
                Boolean isObject = objectDetector.detectObject(potObject);

                // If a recognised object is detected, inform the orchestrator by returning 1.
                if (isObject) {
                    return 1; // Indicate that we found a known object.

                } else {
                    // The obstacle is not a known object. Assume it's a wall or impassable.
                    int i = 0; // Tracks the number of 60-degree turns attempted.

                    // Keep turning right 60 degrees until the distance is >= 50 cm (clear).
                    while (botController.measureDistance() < 50) {
                        botController.turnRight60();
                        if(Thread.currentThread().isInterrupted()){return 0;}
                        ++i;
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                        // If the bot has turned more than 12 times (i.e. rotated 720 degrees total)
                        // and still found no open path, switch to a finer rotation strategy.
                        if (i > 12){
                            botController.stopMoving();

                            // Reuse 'i' as a threshold for a slow rotation check. Start at 49 cm,
                            // decrementing as the bot slowly rotates right.
                            i = 49;

                            // Slowly rotating right while measuring distance to find an open path.
                            botController.turnRight(100);
                            while (botController.measureDistance() < i) {

                                try {
                                    if(Thread.currentThread().isInterrupted()){return 0;}
                                    Thread.sleep(400);
                                } catch (InterruptedException e){
                                    throw new RuntimeException(e);
                                }
                                --i;

                                // If i < 0, it implies the bot has tried many small increments
                                // and cannot clear the obstacle—bot may be trapped.
                                if (i < 0) {
                                    return -1; // Bot is stuck.
                                }
                            }
                            // Having found a path or exceeded the threshold, stop turning and move forward.
                            botController.stopMoving();
                            botController.moveForward();
                            return 0; // Indicate we navigated the obstacle partially/successfully.
                        }
                    }
                }
            }
        }

        // After the 5-second timer, the bot automatically attempts a new path:
        // Stop current forward movement logic by turning right 60 degrees.
        botController.turnRight60();

        // Wait briefly before continuing.
        try {
            if(Thread.currentThread().isInterrupted()){return 0;}
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Recursively call this behaviour to allow the bot to continue wandering
        // in a new orientation.
        if(Thread.currentThread().isInterrupted()){return 0;}
        //execute(botController);

        // Return 0 by default, indicating normal or ongoing wandering conclusion.
        return 0;
    }
}
