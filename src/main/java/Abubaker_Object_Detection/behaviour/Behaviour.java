package Abubaker_Object_Detection.behaviour;

import bot.BotController;

/**
 * Defines a common contract for all bot behaviours (e.g.,
 * {@link CuriousBehaviour}, {@link ScaredyBehaviour}, and
 * {@link WanderBehaviour}). Each implementing class provides
 * its own logic for how the bot should act in a given scenario.
 * <p>
 * The {@code execute} method is called by the orchestrator to
 * perform the behaviour’s primary actions, which often involve
 * interacting with sensors, motors, lights, or cameras via the
 * {@link BotController}.
 */
public interface Behaviour {
	/**
	 * Carries out the defined behaviour on the provided {@code BotController}.
	 * Implementations may include movement, scanning, and other activities
	 * that represent the essence of this behaviour's logic.
	 *
	 * @param botController The bot controller through which actions such
	 *                      as movement and sensor readings are performed.
	 * @return An integer status code indicating the result of executing
	 *         the behaviour (e.g., 0 for normal completion, 1 if an object
	 *         is detected, or -1 if a blocking condition occurs).
	 */
	int execute(BotController botController);
}
