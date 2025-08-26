package Abubaker_Object_Detection.orchestrator;

import bot.BotController;
import Abubaker_Object_Detection.detection.ObjectDetector;
import Abubaker_Object_Detection.selection.QRCodeScanner;
import Abubaker_Object_Detection.behaviour.Behaviour;
import Abubaker_Object_Detection.behaviour.CuriousBehaviour;
import Abubaker_Object_Detection.behaviour.ScaredyBehaviour;
import Abubaker_Object_Detection.behaviour.WanderBehaviour;
import Abubaker_Object_Detection.util.Logger;
//import org.bytedeco.openblas.*;

import java.awt.image.BufferedImage;

/**
 * The {@code BotOrchestrator} class is responsible for coordinating high-level operations
 * of the bot, including behaviour switching, QR code scanning, and initiating the logging process.
 * <p>
 * This class serves as the main orchestrator that bridges core functionalities such as:
 * <ul>
 *     <li>Image capture and QR code scanning</li>
 *     <li>Dynamic switching between behavioural strategies</li>
 *     <li>Logging bot activities</li>
 * </ul>
 * Each behaviour encapsulated in this orchestrator delegates specific execution to
 * individual behaviour strategy classes implementing the {@link Behaviour} interface.
 * </p>
 */
public class BotOrchestrator {

    /**
     * The controller interface to the bot's hardware or low-level control layer.
     */
    private final BotController botController;

    /**
     * The component responsible for detecting and decoding QR codes from images.
     */
    private final QRCodeScanner qrCodeScanner;

    /**
     * The object detection component used to identify nearby objects or hazards in the environment.
     */
    private final ObjectDetector objectDetector;

    /**
     * The currently active behaviour of the bot.
     */
    private Behaviour currentBehaviour;

    /**
     * Constructs a new {@code BotOrchestrator} instance with the required dependencies.
     *
     * @param botController   the controller used to interact with the bot's movement and vision systems
     * @param qrCodeScanner   the scanner used to detect and read QR codes
     * @param objectDetector  the detector used to identify objects in the bot's surroundings
     */
    public BotOrchestrator(
            BotController botController,
            QRCodeScanner qrCodeScanner,
            ObjectDetector objectDetector
    ) {
        this.botController = botController;
        this.qrCodeScanner = qrCodeScanner;
        this.objectDetector = objectDetector;
    }

    /**
     * Captures a high-resolution image from the bot and scans it for a QR code.
     *
     * @return the content of the QR code as a {@code String}, or {@code null} if no QR code is detected
     */
    public String scanQRCode() {
        BufferedImage imageData = botController.takeHighQualityPicture();
        return qrCodeScanner.scanQRCode(imageData);
    }

    /**
     * Initiates the logging process for bot activities using the {@link Logger} utility.
     * This should be called at the beginning of any bot session to ensure actions are recorded.
     */
    public void startLog() {
        Logger.startLog();
    }

    /**
     * Switches the bot's behaviour to a 'wander' strategy and executes it.
     * This behaviour causes the bot to move in anexploratory manner,
     * avoiding obstacles (that aren't classified as objects).
     *
     * @return the result of the behaviour's execution, typically used for diagnostics or feedback
     */
    public int wander() {
        setBehaviour(new WanderBehaviour(objectDetector));
        return currentBehaviour.execute(botController);
    }

    /**
     * Switches the bot's behaviour to a 'curious' strategy and executes it.
     */
    public void curiousBehaviour() {
        setBehaviour(new CuriousBehaviour());
        currentBehaviour.execute(botController);
    }

    /**
     * Switches the bot's behaviour to a 'scaredy' strategy and executes it.
     */
    public void scaredyBehaviour() {
        setBehaviour(new ScaredyBehaviour(objectDetector));
        currentBehaviour.execute(botController);
    }

    /**
     * Terminates the bot's session and performs any necessary shutdown procedures.
     * <p>
     * This method currently logs a termination message but can be extended to release
     * resources, stop motors, or perform final diagnostics.
     * </p>
     */
    public void terminate() {
        Logger.logInfo("Terminating...");
        // Release resources if any
    }

    /**
     * Internal helper method to switch the currently active behaviour.
     *
     * @param behaviour the new {@link Behaviour} strategy to apply
     */
    private void setBehaviour(Behaviour behaviour) {
        this.currentBehaviour = behaviour;
    }

}
