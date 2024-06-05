package ch.unibas.dmi.dbis.cs108.letuscook.gui;

import ch.unibas.dmi.dbis.cs108.letuscook.client.Client;
import ch.unibas.dmi.dbis.cs108.letuscook.orders.CustomerWorkbench;
import ch.unibas.dmi.dbis.cs108.letuscook.orders.GrillWorkbench;
import ch.unibas.dmi.dbis.cs108.letuscook.orders.Item;
import ch.unibas.dmi.dbis.cs108.letuscook.orders.ItemWorkbench;
import ch.unibas.dmi.dbis.cs108.letuscook.orders.Order;
import ch.unibas.dmi.dbis.cs108.letuscook.orders.PlateWorkbench;
import ch.unibas.dmi.dbis.cs108.letuscook.orders.Stack;
import ch.unibas.dmi.dbis.cs108.letuscook.orders.State;
import ch.unibas.dmi.dbis.cs108.letuscook.orders.Workbench;
import ch.unibas.dmi.dbis.cs108.letuscook.server.Game;
import ch.unibas.dmi.dbis.cs108.letuscook.server.Lobby;
import ch.unibas.dmi.dbis.cs108.letuscook.server.Player;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Images;
import java.util.Optional;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.DialogPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

/**
 * The TutorialView class represents the graphical user interface (GUI) for the tutorial section in
 * the Let Us Cook game.
 */
public class TutorialView extends KitchenView {

	private final EventHandler<? super javafx.scene.input.KeyEvent> superOnKeyReleased;
	CustomerWorkbench tutorialBurgerWorkbench;
	private boolean wPressed = false;
	private boolean aPressed = false;
	private boolean sPressed = false;
	private boolean dPressed = false;

	/**
	 * Constructs a new TutorialView object with the specified stage.
	 *
	 * @param stage The stage for the view.
	 */
	TutorialView(Stage stage) {
		super(
			stage,
			Views.TUTORIAL,
			Images.LEAVE,
			() -> {
				((TutorialView) ClientGUI.the().getActiveView()).getGame().stop();
				ClientGUI.changeRequestedView(Views.LOBBIES);
			},
			"Back"
		);

		this.superOnKeyReleased = this.scene.getOnKeyReleased();

		var lobby = new Lobby(false, "Tutorial");
		lobby.addMember(Client.the().getOwnActor());
		var game = new Game(lobby, 0, new Player());
		this.setGame(game);

		Platform.runLater(() -> {
			createAndShowInformationAlert("Welcome!",
				"Welcome in the kitchen! I'll give you a moment to get familiar with your surroundings.\nUse W, A, S, D to to move around.",
				true);

			this.setConsumer(this::firstStep);
		});
	}

	private void setConsumer(Consumer<KeyCode> step) {
		this.scene.setOnKeyReleased(event -> {
			step.accept(event.getCode());

			this.superOnKeyReleased.handle(event);
		});
	}

	/**
	 * Executes the first step of the tutorial based on the provided KeyCode.
	 *
	 * @param code The KeyCode representing the key pressed by the user.
	 */
	
	// check if the player pressed all wasd keys at least once
	private void firstStep(KeyCode code) {
		switch (code) {
			case W -> this.wPressed = true;
			case A -> this.aPressed = true;
			case S -> this.sPressed = true;
			case D -> this.dPressed = true;
		}

		if (this.wPressed && this.aPressed && this.sPressed && this.dPressed) {

			for (Workbench workbenches : this.getGame().workbenches) {
				if (workbenches instanceof CustomerWorkbench customerWorkbench) {
					this.tutorialBurgerWorkbench = customerWorkbench;
					customerWorkbench.forceSetState(State.ACTIVE);
					customerWorkbench.forceSetOrder(Order.HAMBURGER);
					break;
				}
			}

			createAndShowInformationAlert("New order!",
				"No time to waste, your first order just came in! Let's prepare that hamburger!",
				true);

			createAndShowInformationAlert("Get to cooking!",
				"Move to the counter below and use E or SPACE to pick up a patty.",
				true);

			this.setConsumer(this::secondStep);
		}
	}

	// get the raw patty from the itemworkbench
	private void secondStep(KeyCode code) {
		switch (code) {
			case E, SPACE -> {
				Optional<Workbench> workbench = this.getGame()
					.getWorkbenchIfInReach(this.getGame().getTutorialPlayer());
				if (workbench.isPresent()) {
					if (workbench.orElseThrow() instanceof ItemWorkbench) {
						if (workbench.get().peekContents().equals(Stack.of(Item.RAW_PATTY))) {
							this.getGame()
								.getTutorialPlayer().setHolding(workbench.get().trade(this.getGame()
									.getTutorialPlayer().getHolding()));

							createAndShowInformationAlert("Grill it!",
								"Next, place the patty on the grill and wait for it to finish cooking.",
								true);

							this.setConsumer(this::thirdStep);
						}
					}
				}
			}
		}
	}

	//put the raw patty on the grill
	private void thirdStep(KeyCode code) {
		switch (code) {
			case E, SPACE -> {
				Optional<Workbench> workbench = this.getGame()
					.getWorkbenchIfInReach(this.getGame().getTutorialPlayer());
				if (workbench.isPresent()) {
					if (workbench.orElseThrow() instanceof GrillWorkbench && this.getGame()
						.getTutorialPlayer().getHolding().equals(Stack.of(Item.RAW_PATTY))) {
						this.getGame()
							.getTutorialPlayer().setHolding(workbench.get().trade(this.getGame()
								.getTutorialPlayer().getHolding()));

						Thread pattyThread = new Thread(() -> {
							while (workbench.get().ticksUntilStateChange.get() > 0) {
								try {
									Thread.sleep(3000);
								} catch (InterruptedException e) {
									throw new RuntimeException(e);
								}
							}
							workbench.get().forceSetState(State.FINISHED);
							workbench.get()
								.forceSetContentsAccordingToState(Stack.of(Item.GRILLED_PATTY));
							workbench.get().ticksUntilStateChange.set(
								Order.HAMBURGER.getExpirationTimeSeconds() * Game.TPS);

							Platform.runLater(() -> {
								createAndShowInformationAlert("Perfect!",
									"Your patty is ready! Quickly remove it from the grill and place it on one of the plates in the middle.",
									true);

								this.setConsumer(this::fourthStep);
							});
						}, "pattyThread");

						pattyThread.setDaemon(true);
						pattyThread.start();

					}
				}
			}
		}
	}

	//take the grilled patty from the grill
	private void fourthStep(KeyCode code) {
		switch (code) {
			case E, SPACE -> {
				Optional<Workbench> workbench = this.getGame()
					.getWorkbenchIfInReach(this.getGame().getTutorialPlayer());
				if (workbench.isPresent()) {
					if (workbench.orElseThrow() instanceof GrillWorkbench && this.getGame()
						.getTutorialPlayer().getHolding().equals(Stack.of())) {
						this.getGame()
							.getTutorialPlayer().setHolding(workbench.get().trade(this.getGame()
								.getTutorialPlayer().getHolding()));

						this.setConsumer(this::fifthStep);
					}
				}
			}
		}
	}

	// put the grilled patty on the plate
	private void fifthStep(KeyCode code) {
		switch (code) {
			case E, SPACE -> {
				Optional<Workbench> workbench = this.getGame()
					.getWorkbenchIfInReach(this.getGame().getTutorialPlayer());
				if (workbench.isPresent()) {
					if (workbench.orElseThrow() instanceof PlateWorkbench && this.getGame()
						.getTutorialPlayer().getHolding().equals(Stack.of(Item.GRILLED_PATTY))) {
						this.getGame()
							.getTutorialPlayer().setHolding(workbench.get().trade(this.getGame()
								.getTutorialPlayer().getHolding()));

						createAndShowInformationAlert("Stay on your toes!",
							"Remember: items burn if they're on the heat for too long! You can discard items in the bin in the top right.",
							true);

						createAndShowInformationAlert("Almost there!",
							"Let's finish off your burger! Grab some buns (bread) from the counter and add them to the plate.",
							true);

						this.setConsumer(this::sixthStep);
					}
				}
			}
		}
	}

	// take the bun from the itemworkbench
	private void sixthStep(KeyCode code) {
		switch (code) {
			case E, SPACE -> {
				Optional<Workbench> workbench = this.getGame()
					.getWorkbenchIfInReach(this.getGame().getTutorialPlayer());
				if (workbench.isPresent()) {
					if (workbench.orElseThrow() instanceof ItemWorkbench) {
						if (workbench.get().peekContents().equals(Stack.of(Item.BREAD))) {
							this.getGame()
								.getTutorialPlayer().setHolding(workbench.get().trade(this.getGame()
									.getTutorialPlayer().getHolding()));

							this.setConsumer(this::seventhStep);
						}
					}
				}
			}
		}
	}

	// put the bun on the plate with the patty
	private void seventhStep(KeyCode code) {
		switch (code) {
			case E, SPACE -> {
				Optional<Workbench> workbench = this.getGame()
					.getWorkbenchIfInReach(this.getGame().getTutorialPlayer());
				if (workbench.isPresent()) {
					if (workbench.orElseThrow() instanceof PlateWorkbench && this.getGame()
						.getTutorialPlayer().getHolding().equals(Stack.of(Item.BREAD))) {
						this.getGame()
							.getTutorialPlayer().setHolding(workbench.get().trade(this.getGame()
								.getTutorialPlayer().getHolding()));

						createAndShowInformationAlert("Well done!",
							"Bravo! That burger's bursting with flavor! Now all there's left to do is to deliver it to our hungry customer!",
							true);

						this.setConsumer(this::eighthStep);
					}
				}
			}
		}
	}

	// take the burger
	private void eighthStep(KeyCode code) {
		switch (code) {
			case E, SPACE -> {
				Optional<Workbench> workbench = this.getGame()
					.getWorkbenchIfInReach(this.getGame().getTutorialPlayer());
				if (workbench.isPresent()) {
					if (workbench.orElseThrow() instanceof PlateWorkbench) {
						if (workbench.get().peekContents()
							.equals(Stack.of(Item.BREAD, Item.GRILLED_PATTY))) {
							this.getGame()
								.getTutorialPlayer().setHolding(workbench.get().trade(this.getGame()
									.getTutorialPlayer().getHolding()));

							this.setConsumer(this::ninthStep);
						}
					}
				}
			}
		}
	}

	// serve the burger
	private void ninthStep(KeyCode code) {
		switch (code) {
			case E, SPACE -> {
				Optional<Workbench> workbench = this.getGame()
					.getWorkbenchIfInReach(this.getGame().getTutorialPlayer());
				if (workbench.isPresent()) {
					if (workbench.orElseThrow() instanceof CustomerWorkbench) {
						this.getGame().getTutorialPlayer()
							.setHolding(this.tutorialBurgerWorkbench.trade(
								this.getGame().getTutorialPlayer().getHolding()));
						this.getGame().forceSetScore(Order.HAMBURGER.getPrice());

						createAndShowInformationAlert("Nice!",
							"Hooray! You've completed your first order!",
							true);

						createAndShowInformationAlert("Crack the highscore!",
							"Since you delivered the burger in time, your score went up by "
								+ Order.HAMBURGER.getPrice()
								+ " points! Try to beat your friends' scores, or join them to conquer the leaderboard as a group!",
							true);

						createAndShowInformationAlert("Huh?",
							"If you're ever unsure about how an order is prepared, you can find all the instructions under 'Recipes' in the sidebar.",
							true);

						createAndShowInformationAlert("Perfect!",
							"I think you're ready! Let us cook!", true);

						this.returnButtonAction.run();
					}
				}
			}
		}
	}

	private void createAndShowInformationAlert(String title, String contentText, boolean wait) {
		this.keys.clear();

		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(contentText);
		alert.getDialogPane().lookup(".content.label")
			.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px; -fx-text-fill: #333333;");

		DialogPane alertPane = alert.getDialogPane();
		alertPane.setGraphic(new ImageView(Images.get(Images.ICON)));

		ClientGUI.the().showAlert(alert, wait);
	}
}
