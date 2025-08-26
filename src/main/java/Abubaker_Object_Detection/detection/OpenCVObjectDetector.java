package Abubaker_Object_Detection.detection;

import java.awt.image.BufferedImage;


//import org.bytedeco.javacv.Frame;
//import org.bytedeco.javacv.Java2DFrameConverter;
//import org.bytedeco.javacv.OpenCVFrameConverter;

import nu.pattern.OpenCV;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;


//import org.bytedeco.opencv.global.*; //opencv_core;
//import org.bytedeco.opencv.global.opencv_imgproc;
//import org.bytedeco.opencv.opencv_core.Mat;
//import org.bytedeco.opencv.opencv_core.Scalar;
//import org.opencv.*;
//import org.bytedeco.opencv.opencv_core.Core;
//import org.bytedeco.opencv.opencv_core.Rect;
//import org.bytedeco.opencv.opencv_core.MatVector;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import java.util.ArrayList;
import java.util.List;

/**
 * An OpenCV-based implementation of the {@link ObjectDetector} interface. This class leverages
 * various image processing techniques—such as thresholding, Canny edge detection, and contour
 * analysis—to detect potential objects in a given {@link BufferedImage}.
 * <p>
 * The detection process includes:
 * <ul>
 *   <li>Converting the input image to a consistent BGR format, if necessary.</li>
 *   <li>Optionally performing grayscale conversion and thresholding (Otsu).</li>
 *   <li>Applying adaptive thresholds for Canny edge detection based on image statistics (mean and standard deviation).</li>
 *   <li>Finding and filtering contours according to minimum size and aspect ratio constraints.</li>
 * </ul>
 * If an object is detected (by satisfying area and aspect ratio checks), an output image
 * ("detected_objects.jpg") may be saved for diagnostic or testing purposes.
 */
public class OpenCVObjectDetector implements ObjectDetector {

    /**
     * Creates an {@code OpenCVObjectDetector} instance. Though the constructor receives an
     * initial {@link BufferedImage}, it does not currently store or process it directly.
     * Instead, the actual detection occurs in {@link #detectObject(BufferedImage)}.
     *
     * @param image A {@link BufferedImage} that could be used for initialization or setup
     *              in future enhancements. Currently unused.
     */
    // Constructor
    public OpenCVObjectDetector(BufferedImage image) {
    }

    /**
     * Analyzes the provided {@link BufferedImage} to detect if it contains an object of interest.
     * The method applies preprocessing steps including:
     * <ol>
     *   <li>Ensuring a 3-byte BGR format.</li>
     *   <li>Converting to grayscale.</li>
     *   <li>Thresholding using Otsu’s method.</li>
     *   <li>Adaptive Canny edge detection (thresholds determined by the image’s mean and standard deviation).</li>
     *   <li>Contour detection.</li>
     *   <li>Filtering contours based on area, aspect ratio, and basic geometry.</li>
     * </ol>
     * <p>If a contour satisfies the criteria indicating a plausible object, this method returns
     * {@code true}. It also saves a debug image ("detected_objects.jpg") to the local filesystem.
     * Otherwise, {@code false} is returned.
     *
     * @param bufferedImage The image in which to detect an object.
     * @return {@code true} if an object is detected according to the size and aspect ratio filters,
     *         or {@code false} otherwise.
     */
    @Override
    public boolean detectObject(BufferedImage bufferedImage) {

        // Local constants and adaptively determined thresholds for image processing
        final double GAUSSIAN_KERNEL_SIZE = 7;       // Used for Gaussian blur kernel size
        double CANNY_THRESHOLD1 = 30;               // Lower threshold for the Canny algorithm
        double CANNY_THRESHOLD2 = 120;              // Upper threshold for the Canny algorithm
        double MIN_CONTOUR_AREA = 300;              // Minimum area for a contour to be considered valid
        final double MIN_ASPECT_RATIO = 0.15;        // Minimum allowed aspect ratio of a bounding rectangle
        final double MAX_ASPECT_RATIO = 5;          // Maximum allowed aspect ratio of a bounding rectangle

        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        // Ensure the BufferedImage is in 3-byte BGR format
        BufferedImage convertedImg;
        if (bufferedImage.getType() == BufferedImage.TYPE_3BYTE_BGR) {
            convertedImg = bufferedImage;
        } else {
            convertedImg = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
            convertedImg.getGraphics().drawImage(bufferedImage, 0, 0, null);
        }

        // Retrieve the raw pixel data
        byte[] pixels = ((java.awt.image.DataBufferByte) convertedImg.getRaster().getDataBuffer()).getData();

        // Create an OpenCV Mat with the pixel data
        Mat mat = new Mat(height, width, org.opencv.core.CvType.CV_8UC3);
        mat.put(0, 0, pixels);

        // Convert to grayscale
        Mat grayImg = new Mat();
        org.opencv.imgproc.Imgproc.cvtColor(mat, grayImg, org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY);

        // Calculate mean and standard deviation for dynamic thresholding
        MatOfDouble mean = new MatOfDouble();
        MatOfDouble stdDev = new MatOfDouble();
        org.opencv.core.Core.meanStdDev(grayImg, mean, stdDev);

        double meanValue = mean.get(0, 0)[0];
        double stdDevValue = stdDev.get(0, 0)[0];

        // Adapt Canny thresholds based on image statistics
        CANNY_THRESHOLD1 = Math.max(10, meanValue * 0.5 - stdDevValue);
        CANNY_THRESHOLD2 = Math.min(250, meanValue + stdDevValue * 2);

        // Adapt contour minimum area threshold based on a fraction of total image area
        double imageArea = width * (double) height;
        MIN_CONTOUR_AREA = Math.max(100, imageArea * 0.0005);  // 0.05% of image area

        // Threshold the grayscale image using Otsu's method
        Mat thresholdImg = new Mat();
        org.opencv.imgproc.Imgproc.threshold(grayImg, thresholdImg, 0, 255,
                org.opencv.imgproc.Imgproc.THRESH_BINARY + org.opencv.imgproc.Imgproc.THRESH_OTSU);

        // Apply Canny edge detection
        Mat edgesImg = new Mat();
        org.opencv.imgproc.Imgproc.Canny(thresholdImg, edgesImg, CANNY_THRESHOLD1, CANNY_THRESHOLD2);

        // Find external contours
        java.util.List<MatOfPoint> contours = new java.util.ArrayList<>();
        Mat hierarchy = new Mat();
        org.opencv.imgproc.Imgproc.findContours(edgesImg, contours, hierarchy,
                org.opencv.imgproc.Imgproc.RETR_EXTERNAL, org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE);

        boolean objectFound = false;

        // Analyze each contour's size and shape
        for (MatOfPoint contour : contours) {
            double area = org.opencv.imgproc.Imgproc.contourArea(contour);

            // Skip contours that are too small
            if (area < MIN_CONTOUR_AREA) {
                continue;
            }

            // Extract bounding rectangle of the contour
            org.opencv.core.Rect boundingRect = org.opencv.imgproc.Imgproc.boundingRect(contour);
            double aspectRatio = (double) boundingRect.width / boundingRect.height;

            // Check if aspect ratio is within a reasonable range
            if (aspectRatio >= MIN_ASPECT_RATIO && aspectRatio <= MAX_ASPECT_RATIO) {
                objectFound = true;
                break;
            }
        }

        // Optionally save the original (color) image if an object is detected
        if (objectFound) {
            org.opencv.imgcodecs.Imgcodecs.imwrite("detected_objects.jpg", mat);
        }

        // Release resources to avoid memory leaks
        grayImg.release();
        thresholdImg.release();
        edgesImg.release();
        hierarchy.release();
        mat.release();

        return objectFound;
    }

}
