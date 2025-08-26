package bot;

//import swiftbot.ImageSize;
//import swiftbot.SwiftBotAPI;
import swiftbot.Button;
import swiftbot.ButtonFunction;
import swiftbot.ImageSize;
import swiftbot.SwiftBotAPI;

import java.util.Queue;
import java.awt.image.BufferedImage;

/**
 * A concrete implementation of the {@link BotController} interface that
 * interfaces directly with the {@link SwiftBotAPI}. Provides methods
 * for controlling the bot’s movements, lights, and camera.
 */
public class SwiftBotController implements BotController {

    /**
     * An instance of {@link SwiftBotAPI} providing low-level hardware
     * interactions for the SwiftBot (e.g. movement, sensor readings, etc.).
     */
    private final SwiftBotAPI swiftBotApi;

    private static final double CALIBRATION_FACTOR_50 = 0.33; // Factor to calculate distance at speed 50
    private static final double CALIBRATION_FACTOR_30 = 0.26; // Factor to calculate distance at speed 30

    private static final int SMOOTHING_SAMPLE_SIZE = 3; // Number of recent readings to average for smoothing
    private Queue<Double> recentDistances; // Stores the most recent distance readings
    private BotController botController; // Reference to the robot's API for accessing sensors
    /**
     * Constructs a {@code SwiftBotController} with a provided {@link SwiftBotAPI} instance.
     *
     * @param swiftBotApi The API object through which commands
     *                    are issued to the physical SwiftBot hardware.
     */
    public SwiftBotController(SwiftBotAPI swiftBotApi) {
        this.swiftBotApi = swiftBotApi;
    }

    /**
     * Sets the underlights on the SwiftBot to the given RGB array.
     *
     * @param rgbValue Array of 3 integers representing RGB values (0–255 each).
     */
    @Override
    public void setUnderlights(int[] rgbValue) {
        swiftBotApi.fillUnderlights(rgbValue);
    }

    @Override
    public void move(int speed, int speed2, int duration) {
        swiftBotApi.move(speed, speed2, duration);
    }

    /**
     * Moves the SwiftBot forward a fixed distance of ~30 cm, based on a calculated duration
     * derived from the given speed. Automatically stops after it has moved the desired distance.
     *
     * @param speed The motor power (positive integer) used for movement,
     *              from which the duration is computed.
     */
    @Override
    public void moveForward30cm(int speed) {

        swiftBotApi.stopMove();
        // Calculate duration to move 30 cm at the specified speed (heuristic).
        int duration = (int) ((190.0 / speed) * 1_000);
        // Start moving forward at the designated speed for both wheels.
        swiftBotApi.startMove(speed, speed);

        // End time after which the bot should stop.
        long endTime = System.currentTimeMillis() + duration;

        // Loop until we reach the target time.
        while(System.currentTimeMillis() < endTime){
            // Busy wait (could be improved, but typically short).
        }

        // Stop after finishing the movement.
        swiftBotApi.stopMove();
    }

    /**
     * Moves the SwiftBot forward at a default speed of 50.
     * This method does not automatically stop the bot.
     */
    @Override
    public void moveForward() {
        swiftBotApi.startMove(50, 50);
    }

    /**
     * Moves the SwiftBot forward at a specified speed for a total duration.
     *
     * @param speed The speed (motor power) to move forward, typically in the range 1–100.
     * @param totalDuration The total duration (in milliseconds) to move forward.
     */

    @Override
    public void moveForward(int speed, int totalDuration) {
        System.out.println("Moving forward at speed " + speed + " for a total of " + totalDuration + " milliseconds.");
        
        long startTime = System.currentTimeMillis(); // Record the start time
        long elapsedTime = 0; // Track the elapsed time

        // Keep moving forward until the total duration is reached
        while (elapsedTime < totalDuration) {
            botController.move(speed, speed, 100); // Move the bot in short bursts (100ms)

            // Check if an object is detected within range
            if (botController.isObjectDetected(40)) { // Check for objects within 40 cm
                System.out.println("Object detected! Stopping SwiftBot.");
                botController.stopMoving(); // Stop the bot if an object is detected
                return; // Exit the method immediately
            }

            elapsedTime = System.currentTimeMillis() - startTime; // Update the elapsed time
        }

        System.out.println("Completed the forward movement."); // Indicate the movement is done
    }

    /**
     * Moves the SwiftBot backward a fixed distance of ~30 cm, based on a calculated duration
     * derived from the given speed. Automatically stops after it has moved the desired distance.
     *
     * @param speed The motor power (positive integer) used for movement,
     *              from which the duration is computed.
     */
    @Override
    public void moveBackward30cm(int speed) {

        swiftBotApi.stopMove();
        // Calculate duration to move 30 cm at the specified speed (heuristic).
        int duration = (int) ((190.0 / speed) * 1_000);
        // Negative speed moves the SwiftBot in reverse.
        swiftBotApi.startMove(-speed, -speed);

        // End time after which the bot should stop.
        long endTime = System.currentTimeMillis() + duration;

        // Loop until we reach the target time.
        while(System.currentTimeMillis() < endTime){
            // Busy wait.
        }
        swiftBotApi.stopMove();
    }

    /**
     * Moves the SwiftBot backward at a default speed of -50.
     * This method does not automatically stop the bot.
     */
    @Override
    public void moveBackward() {
        swiftBotApi.startMove(-50, -50);
    }

    /**
     * Instructs the SwiftBot to continuously turn left at full power
     * (-100 for left wheel, +100 for right wheel).
     * This method does not automatically stop the bot.
     */
    @Override
    public void turnLeft(int speed) {
        swiftBotApi.startMove(-speed, speed);
    }

    /**
     * Performs an approximate 60-degree left turn by spinning the wheels in opposite
     * directions for a fixed duration (0.125 seconds). Then stops movement.
     */
    @Override
    public void turnLeft60() {

        swiftBotApi.stopMove();
        // Spin wheels for ~0.125 seconds to achieve ~60-degree turn.
        swiftBotApi.startMove(-100, 100);
        long rotationEndTime = System.currentTimeMillis() + 125;

        while(System.currentTimeMillis() < rotationEndTime){
            // Busy wait until time elapses.
        }
        swiftBotApi.stopMove();
    }

    /**
     * Instructs the SwiftBot to continuously turn right at full power
     * (+100 for left wheel, -100 for right wheel).
     * This method does not automatically stop the bot.
     */
    @Override
    public void turnRight(int speed) {
        swiftBotApi.startMove(speed, -speed);
    }

    /**
     * Performs an approximate 60-degree right turn by spinning the wheels in opposite
     * directions for a fixed duration (0.225 seconds). Then stops movement.
     */
    @Override
    public void turnRight60() {

        swiftBotApi.stopMove();
        // Spin wheels for ~0.225 seconds to achieve ~60-degree turn.
        swiftBotApi.startMove(100, -100);
        long rotationEndTime = System.currentTimeMillis() + 225;

        while(System.currentTimeMillis() < rotationEndTime){
            // Busy wait until time elapses.
        }
        swiftBotApi.stopMove();
    }


    /**
     * Moves the SwiftBot backward at a specified speed for a total duration.
     *
     * @param speed The speed (motor power) to move backward, typically in the range 1–100.
     * @param totalDuration The total duration (in milliseconds) to move backward.
     */
    @Override
    public double reverse(int speed, int totalDuration) {
        System.out.println("Reversing at speed " + speed + " for " + totalDuration + " milliseconds.");
        botController.move(-speed, -speed, totalDuration); // Reverse the bot at the specified speed

        // Calculate the distance moved during reverse using the calibration factor
        double calibrationFactor = (speed == 50) ? CALIBRATION_FACTOR_50 : CALIBRATION_FACTOR_30;
        double distance = speed * calibrationFactor * (totalDuration / 1000.0); // Convert duration to seconds

         // Return the distance moved backward
         return distance;
    }
    /**
     * Immediately stops any ongoing movement of the SwiftBot.
     */
    @Override
    public void stopMoving() {
        swiftBotApi.stopMove();
    }   

    /**
     * Uses the SwiftBot's ultrasound sensor to measure the distance to the nearest
     * object in front, in centimeters.
     *
     * @return The measured distance in centimeters, or {@code 0} in case of an error.
     */
    @Override
    public double measureDistance() {
        try {
            return swiftBotApi.useUltrasound();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Checks if an object is detected within a given threshold distance
    @Override
    public boolean isObjectDetected(double threshold) {
        double distance = measureDistance(); // Get the current distance reading from the robot's sensor

        // Add the new reading to the queue, maintaining the maximum sample size
        if (recentDistances.size() >= SMOOTHING_SAMPLE_SIZE) {
            recentDistances.poll(); // Remove the oldest reading to make room
        }
        recentDistances.offer(distance); // Add the newest distance reading to the queue

        double smoothedDistance = getSmoothedDistance(); // Calculate the smoothed (averaged) distance

        // Print the raw and smoothed distances for debugging purposes
        //ystem.out.println("Raw Distance: " + distance + " cm");
        //System.out.println("Smoothed Distance: " + smoothedDistance + " cm");

        // Check if the smoothed distance is less than or equal to the threshold
        if (smoothedDistance <= threshold) {
            System.out.println("Object Detected at " + smoothedDistance + " cm"); // Print detection message
            return true; // Object detected
        }

        return false; // No object detected
    }

    // Calculates the average (smoothed) distance from the recent readings
    @Override
    public double getSmoothedDistance() {
        if (recentDistances.isEmpty()) {
            return Double.MAX_VALUE; // Return a very large value if no readings are available
        }

        double total = 0; // Sum up all the distance readings
        for (double d : recentDistances) {
            total += d; // Add each reading to the total
        }
        return total / recentDistances.size(); // Divide by the number of readings to get the average
    }

    /**
     * Captures a still image in grayscale using the SwiftBot's camera at a resolution
     * of 480x480 pixels.
     *
     * @return A {@link BufferedImage} representing the captured grayscale image.
     */
    @Override
    public BufferedImage takeGreyPicture() {
        try {
            return swiftBotApi.takeGrayscaleStill(ImageSize.SQUARE_480x480);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public BufferedImage takeCustomPicture(ImageSize imageSize) {
        try {
            return swiftBotApi.takeStill(imageSize);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Captures a still image in colour at a higher resolution of 1080x1080 pixels.
     *
     * @return A {@link BufferedImage} representing the captured high-quality image.
     */
    public BufferedImage takeHighQualityPicture() {
        try {
            return swiftBotApi.takeStill(ImageSize.SQUARE_1080x1080);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Decodes a QR code from a BufferedImage using the SwiftBotAPI's native QR code decoding capabilities.
     * 
     * @param image The BufferedImage containing the QR code to decode.
     * @return A string representing the decoded QR code content.
     */
    @Override
    public String decodeQRImageNatively(BufferedImage image) {
        try {
            return swiftBotApi.decodeQRImage(image);
        } catch (Exception e) {
            throw new RuntimeException(e);  
        }
    }
    
    //Enable button
    @Override
    public void enableButton(Button button, ButtonFunction function) {
        swiftBotApi.enableButton(button, function);
    } 

/*     public void enableButton(Button button, Runnable action) {

        swiftBotApi.enableButton(button, (ButtonFunction) action::run); // Enable the button and assign the action
        System.out.println("Successfully enabled button: " + button); // Confirm the button has been enabled
    } */

    //Disable button
    @Override
    public void disableButton(Button button) {
        swiftBotApi.disableButton(button);
    }
    //Disable all buttons
    @Override
    public void disableAllButtons() {
        swiftBotApi.disableAllButtons();
    }
    //Fill underlights (repeat function, but convinient naming)
    public void fillUnderlights(int[] rgbValue) {
        swiftBotApi.fillUnderlights(rgbValue);
    }
    //Disable underlights
    @Override
    public void disableUnderlights() {
        swiftBotApi.disableUnderlights();
    }
    
    // Helper method to match a string to a valid button name
    @Override
    public Button getButtonFromString(String button) {
        switch (button.toUpperCase()) { // Convert the button name to uppercase for comparison
            case "A": return Button.A; // Return Button.A for "A"
            case "B": return Button.B; // Return Button.B for "B"
            case "X": return Button.X; // Return Button.X for "X"
            case "Y": return Button.Y; // Return Button.Y for "Y"
            default: return null; // Return null if the button name is invalid
        }
    }

    @Override
    public void enableButton(String button, Runnable action) {
        Button swiftBotButton = getButtonFromString(button); // Convert the button name to a Button object
        if (swiftBotButton == null) { // Check if the button name is valid
            System.out.println("Invalid button name provided: " + button); // Print an error message for invalid buttons
            return; // Exit the method
        }

        swiftBotApi.enableButton(swiftBotButton, (ButtonFunction) action::run); // Enable the button and assign the action
        System.out.println("Successfully enabled button: " + swiftBotButton); // Confirm the button has been enabled
    }

    @Override
    // Disable a button so that it no longer performs any actions
    public void disableButton(String button) {
        Button swiftBotButton = getButtonFromString(button); // Convert the button name to a Button object
        if (swiftBotButton == null) { // Check if the button name is valid
            System.out.println("Invalid button name provided: " + button); // Print an error message for invalid buttons
            return; // Exit the method
        }

        swiftBotApi.disableButton(swiftBotButton); // Disable the button
        System.out.println("Successfully disabled button: " + swiftBotButton); // Confirm the button has been disabled
    }

    @Override
    public double getCalibrationFactor50() {
        return CALIBRATION_FACTOR_50;
    }

    @Override
    public double getCalibrationFactor30() {
        return CALIBRATION_FACTOR_30;
    }
}
