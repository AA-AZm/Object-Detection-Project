package bot;

import java.awt.image.BufferedImage;
import swiftbot.Button;
//import java.lang.Runnable;
import swiftbot.ButtonFunction;
import swiftbot.ImageSize;

/**
 * Defines the fundamental operations that a SwiftBot must support, such as
 * movement, measuring distance, and taking pictures. A typical implementation
 * of this interface is provided by the {@link SwiftBotController} class, which
 * directly interfaces with the underlying SwiftBot hardware.
 */
public interface BotController {


    /**
     * Sets the underlights on the SwiftBot to the specified RGB values.
     *
     * @param rgbValue An array of three integers [R, G, B] each between 0 and 255,
     *                 representing the colour to be displayed.
     */
    void setUnderlights(int[] rgbValue);
    

    void move(int speed, int speed2, int duration);

    /**
     * Moves the SwiftBot forward at a specified speed for a distance of approximately
     * 30 cm (implementation can vary). Generally stops automatically upon completion.
     *
     * @param speed The speed (motor power) to move forward, typically in the range 1–100.
     */
    void moveForward30cm(int speed);

    /**
     * Moves the SwiftBot forward continuously at a default speed. This method does not
     * include any fixed distance or duration, leaving it to the caller to decide when
     * to stop.
     */
    void moveForward(); 

    /**
     * Moves the SwiftBot forward at a specified speed for a total duration.
     *
     * @param speed The speed (motor power) to move forward, typically in the range 1–100.
     * @param totalDuration The total duration (in milliseconds) to move forward.
     */
    void moveForward(int speed, int totalDuration); 
    

    /**
     * Moves the SwiftBot backward at a specified speed for a distance of approximately
     * 30 cm (implementation can vary). Generally stops automatically upon completion.
     *
     * @param speed The speed (motor power) to move backward, typically in the range 1–100.
     */
    void moveBackward30cm(int speed);

    /**
     * Moves the SwiftBot backward continuously at a default speed. This method does not
     * include any fixed distance or duration, leaving it to the caller to decide when
     * to stop.
     */
    void moveBackward();

    /**
     * Causes the SwiftBot to rotate left continuously at specified speed.
     * Does not automatically stop, so further commands may be needed to halt movement.
     */
    void turnLeft(int speed);

    /**
     * Performs a quick ~60-degree left turn by spinning the wheels in opposite directions
     * for a short fixed duration. Generally stops automatically upon completion.
     */
    void turnLeft60();

    /**
     * Causes the SwiftBot to rotate right continuously at specified speed.
     * Does not automatically stop, so further commands may be needed to halt movement.
     */
    void turnRight(int speed);

    /**
     * Performs a quick ~60-degree right turn by spinning the wheels in opposite directions
     * for a short fixed duration. Generally stops automatically upon completion.
     */
    void turnRight60();

    /**
     * Immediately stops any ongoing movement of the SwiftBot, overriding previous
     * movement commands.
     */

    /** 
     * Moves the SwiftBot backward at a specified speed for a total duration.
     *
     * @param speed The speed (motor power) to move backward, typically in the range 1–100.
     * @param totalDuration The total duration (in milliseconds) to move backward.
     */
    double reverse(int speed, int totalDuration);
    

    void stopMoving();

    /**
     * Obtains a distance measurement (in centimetres) from the SwiftBot's ultrasound sensor,
     * representing how far away the nearest object is located in front.
     *
     * @return A double value indicating the measured distance in centimetres.
     */
    double measureDistance();

    /**
     * Checks if an object is detected within a given threshold  distance.
     *
     * @param threshold The distance threshold within which an object is considered detected.
     * @return true if an object is detected within the threshold, false otherwise.
     */
    boolean isObjectDetected(double threshold);

    /** 
     * Calculates the average (smoothed) distance from the recent readings.
     *
     * @return A double value representing the average distance.
     */
    double getSmoothedDistance();

    /**
     * Captures a single still image using the SwiftBot's camera at a moderate resolution
     * (often 480x480 pixels in grayscale).
     *
     * @return A {@link BufferedImage} of the captured picture.
     */
    BufferedImage takeGreyPicture();

    /**
     * Captures a single still image using the SwiftBot's camera at a specified resolution.
     *
     * @param imageSize The desired resolution of the captured image.
     * @return A {@link BufferedImage} of the captured picture.
     */
    BufferedImage takeCustomPicture(ImageSize imageSize);

    /**
     * Captures a single still image using the SwiftBot's camera at a higher resolution
     * (often 1080x1080 pixels in full colour).
     *
     * @return A {@link BufferedImage} of the captured high-quality picture.
     */
    BufferedImage takeHighQualityPicture();


    /** 
     * Decodes a QR code from a BufferedImage using the SwiftBotAPI's native QR code decoding capabilities.
     * 
     * @param image The BufferedImage containing the QR code to decode.
     * @return A string representing the decoded QR code content.
     */
    String decodeQRImageNatively(BufferedImage image);

    /**
     * Enables a button to perform a specific action when pressed.
     * 
     * @param button The button to enable.
     * @param function The action to perform when the button is pressed.
     */
    void enableButton(Button button, ButtonFunction function);

    /**
     * Disables a button so that it no longer performs any actions.
     * 
     * @param button The button to disable.
     */
    void disableButton(Button button);

    /**
     * Disables all buttons so that they no longer perform any actions.
     */
    void disableAllButtons();

    /**
     * Fills the underlights with a specific RGB value.
     * 
     * @param rgbValue The RGB value to fill the underlights with.
     */
    void fillUnderlights(int[] rgbValue);
    
    /**
     * Disables the underlights so that they no longer emit any light.
     */
    void disableUnderlights();

    /**
     * Converts a string representation of a button to a Button object.
     * 
     * @param button The string representation of the button.
     * @return The Button object corresponding to the string.
     */
    Button getButtonFromString(String button);

    /**
     * Enables a button to perform a specific action when pressed.
     * 
     * @param button The string representation of the button.
     * @param action The action to perform when the button is pressed.
     */
    void enableButton(String button, Runnable action);

    /**
     * Disables a button so that it no longer performs any actions.
     * 
     * @param button The string representation of the button.
     */
    void disableButton(String button);  

    double getCalibrationFactor50();

    double getCalibrationFactor30();
}
