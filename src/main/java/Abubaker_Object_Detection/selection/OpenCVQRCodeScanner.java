package Abubaker_Object_Detection.selection;

import nu.pattern.OpenCV;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.QRCodeDetector;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

/**
 * The {@code OpenCVQRCodeScanner} class implements the {@link QRCodeScanner} interface using the OpenCV library
 * to detect and decode QR codes from a provided {@link BufferedImage}.
 * <p>
 * This class demonstrates the following workflow:
 * <ol>
 *     <li>Initialising and loading the local OpenCV library</li>
 *     <li>Converting the incoming {@link BufferedImage} into an OpenCV-compatible {@link Mat}</li>
 *     <li>Applying a {@link QRCodeDetector} to detect and decode any QR code in the image</li>
 *     <li>Optionally providing a snippet of code (commented out) that visualises the detected QR code boundary</li>
 *     <li>Releasing OpenCV resources after the detection process completes</li>
 * </ol>
 * If the detection is successful, the decoded QR code content is returned as a {@code String}; otherwise, an
 * empty string is returned.
 */
public class OpenCVQRCodeScanner implements QRCodeScanner {

    /**
     * Scans the provided {@link BufferedImage} for a QR code using the OpenCV {@link QRCodeDetector}.
     * <p>
     * Steps involved:
     * <ul>
     *     <li>Loads the OpenCV library locally.</li>
     *     <li>Determines the dimensions and ensures the image is in {@code TYPE_3BYTE_BGR} format.</li>
     *     <li>Copies the pixel data into an OpenCV-compatible {@link Mat}.</li>
     *     <li>Detects and decodes any QR code using the {@link QRCodeDetector}.</li>
     *     <li>Releases allocated OpenCV resources.</li>
     * </ul>
     * If no QR code is detected, this method returns an empty {@code String}.
     *
     * @param bufferedImage the source image to be scanned for a QR code
     * @return the decoded QR code content, or an empty {@code String} if no code is detected
     */
    @Override
    public String scanQRCode(BufferedImage bufferedImage) {

        OpenCV.loadLocally();

        // Get image dimensions
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        // Convert to appropriate format if needed
        BufferedImage convertedImg;
        if (bufferedImage.getType() == BufferedImage.TYPE_3BYTE_BGR) {
            convertedImg = bufferedImage;
        } else {
            convertedImg = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
            convertedImg.getGraphics().drawImage(bufferedImage, 0, 0, null);
        }

        // Get the pixel data
        byte[] pixels = ((DataBufferByte) convertedImg.getRaster().getDataBuffer()).getData();

        // Create a Mat with the image data
        Mat mat = new Mat(height, width, CvType.CV_8UC3);
        mat.put(0, 0, pixels);
        String result = "";

        try {
            // Create QR code detector
            QRCodeDetector qrCodeDetector = new QRCodeDetector();

            // Detect and decode QR code
            Mat points = new Mat();
            result = qrCodeDetector.detectAndDecode(mat, points);

            /*
            // Optional: Draw detection points on the image (for visualisation)
            if (!result.isEmpty() && !points.empty() && points.rows() >= 4) {
                for (int i = 0; i < 4; i++) {
                    int nextPointIndex = (i + 1) % 4;
                    Point p1 = new Point(points.get(0, i)[0], points.get(0, i)[1]);
                    Point p2 = new Point(points.get(0, nextPointIndex)[0], points.get(0, nextPointIndex)[1]);
                    Imgproc.line(mat, p1, p2, new Scalar(0, 255, 0), 2);
                }
            }
            */

            // Release all OpenCV resources
            points.release();
            mat.release();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
