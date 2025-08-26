package Abubaker_Object_Detection.detection;

import java.awt.image.BufferedImage;

/**
 * Defines a contract for object detection on images captured by a bot or other source.
 * Any class implementing this interface should provide a concrete strategy for analysing
 * an input {@link BufferedImage} and determining whether it contains an object of interest.
 * <p>
 * A typical implementation of this interface is provided by the
 * {@link OpenCVObjectDetector} class, which utilises OpenCV-based image
 * processing and contour analysis to detect objects in a given image.
 */
public interface ObjectDetector {

    /**
     * Examines a provided image to decide if an object of interest is present.
     * Implementations can use various algorithms (e.g., edge detection, machine
     * learning models, etc.) and criteria (contour size, shape, colour, etc.)
     * to make this determination.
     *
     * @param image A {@link BufferedImage} on which detection is performed.
     * @return {@code true} if the image contains an object according to the
     *         implementation's criteria, or {@code false} otherwise.
     */
    boolean detectObject(BufferedImage image);
}
