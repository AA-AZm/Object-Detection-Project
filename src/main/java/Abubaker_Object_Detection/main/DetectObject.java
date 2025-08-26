package Abubaker_Object_Detection.main;

import bot.*;
import nu.pattern.OpenCV;
import Abubaker_Object_Detection.orchestrator.*;
import Abubaker_Object_Detection.detection.*;
import Abubaker_Object_Detection.selection.OpenCVQRCodeScanner;
import Abubaker_Object_Detection.selection.QRCodeScanner;
import swiftbot.Button;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The main entry point of the application. This class configures the SwiftBot,
 * loads required libraries, orchestrates scanning for QR codes, selects a
 * particular behavioural mode, and executes the main loop of SwiftBot
 * interactions and wander behaviour until termination.
 */
//DetectObject class implements Runnable to allow for threading
public class DetectObject implements Runnable{

    private BotController botController;

    public DetectObject(BotController botController){
        this.botController = botController;
    }

    /**
     * The main method is the starting point of the application. It sets up all
     * dependencies and orchestrates the SwiftBot's operations.
     *
     */
    @Override
    public void run() {

        // Load the local OpenCV native library. This allows OpenCV-based functionality
        // (e.g., image processing and detection) to work properly.
        OpenCV.loadLocally();

        // Create a QR code scanner using OpenCV. This allows for identifying
        // and decoding QR codes in captured images.
        QRCodeScanner qrCodeScanner = new OpenCVQRCodeScanner();

        // Create an ObjectDetector instance for detecting objects around the SwiftBot.
        // The dummy image is initially passed in as a placeholder, as some
        // implementations require an image upon construction.
        ObjectDetector objectDetector = new OpenCVObjectDetector(new BufferedImage(1,1,1));

        // Instantiate an orchestrator that coordinates the bot controller,
        // QR code scanner, and object detector to define higher-level behaviours
        // and logic flows.
        BotOrchestrator orchestrator = new BotOrchestrator(
                botController, qrCodeScanner, objectDetector
        );

        // The 'behaviour' integer is used as a mode selector within the application.
        // 0 corresponds to a "Curious" SwiftBot, 1 to a "Scaredy" SwiftBot, etc.
        int behaviour = 0;

        // Print out a stylised ASCII banner for a visual introduction.
        System.out.println("                                                                        ");
        System.out.println("                                                                        ");
        System.out.println("    ,---,                  ___                           ___            ");
        System.out.println("  .'  .' `\\              ,--.'|_                       ,--.'|_          ");
        System.out.println(",---.'     \\             |  | :,'                      |  | :,'         ");
        System.out.println("|   |  .`\\  |            :  : ' :                      :  : ' :         ");
        System.out.println(":   : |  '  |   ,---.  .;__,'  /     ,---.     ,---. .;__,'  /          ");
        System.out.println("|   ' '  ;  :  /     \\ |  |   |     /     \\   /     \\|  |   |           ");
        System.out.println("'   | ;  .  | /    /  |:__,'| :    /    /  | /    / ':__,'| :           ");
        System.out.println("|   | :  |  '.    ' / |  '  : |__ .    ' / |.    ' /   '  : |__         ");
        System.out.println("'   : | /  ; '   ;   /|  |  | '.'|'   ;   /|'   ; :__  |  | '.'|        ");
        System.out.println("|   | '` ,/  '   |  / |  ;  :    ;'   |  / |'   | '.'| ;  :    ;        ");
        System.out.println(";   :  .'    |   :    |  |  ,   / |   :    ||   :    : |  ,   /          ");
        System.out.println("|   ,.'       \\   \\  /    ---`-'   \\   \\  /  \\   \\  /   ---`-'           ");
        System.out.println("'---'          `----'               `----'    `----'                     ");
        System.out.println("                                                                         ");
        System.out.println("                                                                         ");
        System.out.println("    ,----..                                                              ");
        System.out.println("   /   /   \\                                             ___             ");
        System.out.println("  /   .     :   ,---,                                  ,--.'|_           ");
        System.out.println(" .   /   ;.  \\,---.'|         .--.                     |  | :,'          ");
        System.out.println(".   ;   /  ` ;|   | :       .--,`|                     :  : ' :          ");
        System.out.println(";   |  ; \\ ; |:   : :       |  |.    ,---.     ,---. .;__,'  /           ");
        System.out.println("|   :  | ; | ':     |,-.    '--`_   /     \\   /     \\|  |   |            ");
        System.out.println(".   |  ' ' ' :|   : '  |    ,--,'| /    /  | /    / ':__,'| :            ");
        System.out.println("'   ;  \\; /  ||   |  / :    |  | '.    ' / |.    ' /   '  : |__          ");
        System.out.println(" \\   \\  ',  / '   : |: |    :  | |'   ;   /|'   ; :__  |  | '.'|         ");
        System.out.println("  ;   :    /  |   | '/ :  __|  : ''   |  / |'   | '.'| ;  :    ;         ");
        System.out.println("   \\   \\ .'   |   :    |.'__/\\_: ||   :    ||   :    : |  ,   /          ");
        System.out.println("    `---`     /    \\  / |   :    : \\   \\  /  \\   \\  /   ---`-'           ");
        System.out.println("              `-'----'   \\   \\  /   `----'    `----'                     ");
        System.out.println("                          `--`-'                                         ");
        System.out.println(" ");
        System.out.println(" ");
        System.out.println(" ");
        System.out.println(" ");
        System.out.println(" ");
        System.out.println(" ");
        System.out.println();

        // Print a line of dashes to visually separate sections of output.
        System.out.println("--------------------------------------");

        // Prompt the user to scan a QR code for selecting one of the three modes:
        // "Curious SwiftBot", "Scaredy SwiftBot", or "Dubious SwiftBot".
        System.out.println("Please scan a QR code for mode selection (Curious SwiftBot, Scaredy SwiftBot, Dubious SwiftBot).");

        // Default mode selection prior to scanning. In case scanning times out,
        // the mode may be overridden below to "Dubious".
        String mode = "Default";
        // (HINDSIGHT: Mode should really be an enum, but refactor skipped due to time constraints)

        // Allow a 3-second window for scanning. If a matching QR code is found,
        // an appropriate mode is selected, and the loop ends.
        long endTime = System.currentTimeMillis() + 3000;
        while (System.currentTimeMillis() < endTime) {

            // Continuously attempt to scan for a QR code until
            // a non-empty result is returned or timeout occurs.
            String scanned = orchestrator.scanQRCode();

            // If a "Curious SwiftBot" QR code is scanned, set behaviour to 0.
            if ("Curious SwiftBot".equals(scanned)) {
                //behaviour = 0;
                mode = "Curious";
                break;
                // If a "Scaredy SwiftBot" QR code is scanned, set behaviour to 1.
            } else if ("Scaredy SwiftBot".equals(scanned)) {
                behaviour = 1;
                mode = "Scaredy";
                break;
                // If a "Dubious SwiftBot" QR code is scanned, randomly choose
                // between behaviour 0 or 1.
            } else if ("Dubious SwiftBot".equals(scanned)) {
                behaviour = new Random().nextInt(2);
                mode = "Dubious";
                break;
            }

            // Sleep briefly (500ms) to reduce CPU usage and scanning overhead.
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // If the thread is interrupted, convert it to a runtime exception
                // to halt the program in a controlled manner.
                throw new RuntimeException(e);
            }

        }

        // If still in Default mode after the first attempt, allow another
        // 6-second window, but this time decode images directly using SwiftBotAPI.
        endTime = System.currentTimeMillis() + 6000;
        while (System.currentTimeMillis() < endTime && mode.equals("Default")) {

            // Manually capture and decode a still image of size 1080x1080.
            String scanned = botController.decodeQRImageNatively(botController.takeHighQualityPicture());

            if ("Curious SwiftBot".equals(scanned)) {
                //behaviour = 0;
                mode = "Curious";
                break;
            } else if ("Scaredy SwiftBot".equals(scanned)) {
                behaviour = 1;
                mode = "Scaredy";
                break;
            } else if ("Dubious SwiftBot".equals(scanned)) {
                behaviour = new Random().nextInt(2);
                mode = "Dubious";
                break;
            }
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        // If the mode is still "Default" after both scans, set the mode
        // to "Dubious SwiftBot" to avoid indefinite stalling.
        if (mode.equals("Default")) {
            behaviour = new Random().nextInt(2);
            System.out.println("Timed out. Dubious SwiftBot selected");
        }

        // Record the start time for logging and performance metrics.
        long startTime = System.currentTimeMillis();
        // orchestrator.startLog();  // Potentially start logging here if needed.

        // Indicate how the user can terminate the program. The user must press 'X'
        // on the SwiftBot to stop the main loop.
        System.out.println("Press 'X' on the SwiftBot to terminate the program.");

        // Use an AtomicInteger 'i' for controlling a loop. This is an arbitrary
        // countdown that allows the main loop to run a certain number of times
        // unless interrupted.
        AtomicInteger i = new AtomicInteger(99);

        // Track how many object encounters occur (i.e., how often the
        // wander() method detects something).
        int numEncounters = 0;

        // Enable a button on the SwiftBot (Button.X) to allow terminating the
        // program. Once pressed, the loop condition is set to -1 to break out.
        botController.enableButton(Button.X, () -> {
        	//System.out.println("Profession!");
            i.set(-1);
            Thread.currentThread().interrupt();
            Thread.yield();
            return;

        });

        int wanderState;
        // Main loop that runs until i is decremented to 0 or set to -1 by user input.
        while(i.get() > 0) {


            wanderState = orchestrator.wander();
            // The orchestrator's wander() method returns:
            //  0 if it continues wandering,
            //  1 if an encounter is detected,
            // -1 if an error or other stopping condition occurs.

            // If wanderState == 1, it means an object was encountered and
            // the orchestrator triggers a behaviour depending on the selected mode.
            if (wanderState == 1) {

                // If 'behaviour' == 0, run 'curiousBehaviour'.
                // Increase the encounter count for logging/tracking.
                if(behaviour == 0){
                    orchestrator.curiousBehaviour();
                    System.out.println("Num Encounters: " + numEncounters);
                    ++numEncounters;
                }
                // Otherwise, assume 'behaviour' == 1 for "Scaredy SwiftBot".
                else{
                    orchestrator.scaredyBehaviour();
                    System.out.println("Num Encounters: " + numEncounters);
                    ++numEncounters;
                }

            } else if (wanderState == -1) {
                // If wander() returns -1, it might signify an error or special condition.
                // Could handle a different scenario here if needed.
            }

            // Decrement the loop counter once per full iteration.
            i.decrementAndGet();
        }

        // Once the loop ends, store the end time to calculate the total execution duration.
        AtomicBoolean buttonTrigger = new AtomicBoolean(false);
        endTime = System.currentTimeMillis();
        long duration = (endTime - startTime) / 1000;

        // Disable all buttons to prevent unintended inputs once main loop finishes.
        botController.disableAllButtons();

        // Signal the orchestrator that the program is terminating, allowing it
        // to clean up resources or stop any running threads.
        orchestrator.terminate();

        // Provide the user with an option to display a brief execution log on-screen.
        System.out.println("Would you like to view the execution log? Press 'Y' on the SwiftBot for yes and 'X' on the SwiftBot for no.");

        // Enabling Button.X to skip log display and simply print the default message.
        botController.enableButton(Button.X, () -> {
            System.out.println("Log File Path: /data/home/pi");
            buttonTrigger.set(true);
        });

        // If user presses 'Y', display the log: the SwiftBot mode, execution time,
        // number of encounters, and file paths.
        int finalNumEncounters = numEncounters;
        String finalMode = mode;
        botController.enableButton(Button.Y, () -> {
            System.out.println();
            System.out.println("+----------------------------------+------------------------------------------------+");
            System.out.println("| SwiftBot Mode                    | "+ finalMode + "                                       |");
            System.out.println("+----------------------------------+------------------------------------------------+");
            System.out.println("| Execution Time                   | "+ duration + "                                    |");
            System.out.println("+----------------------------------+------------------------------------------------+");
            System.out.println("| Object Encounters                | "+ finalNumEncounters + "                                        |");
            System.out.println("+----------------------------------+------------------------------------------------+");
            System.out.println("| Image Save Path                  | /data/home/pi                                  |");
            System.out.println("+----------------------------------+------------------------------------------------+");
            System.out.println("| Log File Path                    | /data/home/pi/log.txt                          |");
            System.out.println("+----------------------------------+------------------------------------------------+");
            System.out.println("Duration: " + duration + " seconds");
            System.out.println("Number of encounters: " + finalNumEncounters);
            System.out.println("Image File Path: /data/home/pi");
            System.out.println("Log File Path: /data/home/pi");
            buttonTrigger.set(true);
        });

        // Keep looping until the user presses either 'X' or 'Y' to set
        // 'buttonTrigger' to true, upon which we break out.
        while(true){
            if(buttonTrigger.get()){
                break;
            }
        }

        // Attempt to write high-level metrics to a log file on disk
        // (e.g., /data/home/pi/log.txt). This is a simple demonstration of
        // appending to a file; error handling is shown to catch IO exceptions.
        try {
            File logFile = new File("/data/home/pi/log.txt");
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            java.io.FileWriter fw = new java.io.FileWriter(logFile, true);
            fw.write("Duration: " + duration + " seconds\n");
            fw.write("Number of encounters: " + numEncounters + "\n");
            fw.write("Image File Path: /data/home/pi\n");
            fw.write("Log File Path: /data/home/pi\n");
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Disable all buttons before final exit to avoid leftover handlers
        // and release resources on the SwiftBot.
        botController.disableAllButtons();

        // Exit the application with a status code of 0 (successful termination).
        //System.exit(0);

        //Terminate the thread
        //Thread.currentThread().interrupt();
        return;
    }

}
