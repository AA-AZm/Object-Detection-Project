package Abubaker_Object_Detection.selection;

import java.awt.image.BufferedImage;

/**
 * The {@code QRCodeScanner} interface defines the contract for any class capable of scanning and decoding
 * QR codes from a provided {@link BufferedImage}.
 * <p>
 * Implementations of this interface, such as {@link OpenCVQRCodeScanner}, typically:
 * <ul>
 *     <li>Accept an image as input (e.g., from a camera feed or file source).</li>
 *     <li>Process or transform the image into an appropriate format.</li>
 *     <li>Analyse the image to detect any embedded QR code.</li>
 *     <li>Extract and return the decoded text content from the QR code.</li>
 * </ul>
 * Classes implementing {@code QRCodeScanner} may utilise a variety of techniques or libraries
 * to complete the detection and decoding process.
 */
public interface QRCodeScanner {

    /**
     * Scans the given {@link BufferedImage} for a QR code and returns the decoded result.
     * <p>
     * If no QR code is detected, this method should return an empty {@code String}.
     *
     * @param imageData the source image to scan for a QR code
     * @return the decoded QR code content, or an empty {@code String} if no code is detected
     */
    String scanQRCode(BufferedImage imageData);
}
