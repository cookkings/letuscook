package ch.unibas.dmi.dbis.cs108.letuscook.util;

import ch.unibas.dmi.dbis.cs108.letuscook.gui.Controls;
import ch.unibas.dmi.dbis.cs108.letuscook.gui.Units;
import ch.unibas.dmi.dbis.cs108.letuscook.orders.Workbench;
import ch.unibas.dmi.dbis.cs108.letuscook.server.Game;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.image.Image;

/**
 * Enumeration representing various images used in the application.
 */
public enum Images {

	ICON,
	LOGO,
	SETTINGS,
	FLOOR(Game.WIDTH, Game.HEIGHT),
	BACKGROUND(new Units(Game.HEIGHT.u() * 16 / 9), Game.HEIGHT),
	INVISIBLE_SYMBOL,
	RETURN,
	CHANGE_NICKNAME,
	GLOBAL_CHAT,
	HIGHSCORE,
	LEAVE,
	WHISPER,
	RECIPES,
	VISIBLE_SYMBOL,
	PRIMARY_BUTTON(Controls.primaryButtonWidth, Controls.primaryButtonHeight),
	SECONDARY_BUTTON(Controls.secondaryButtonWidth, Controls.secondaryButtonHeight),
	LOBBY_BUTTON_IDLE(SECONDARY_BUTTON.dimensions.getX(), SECONDARY_BUTTON.dimensions.getY()),
	LOBBY_BUTTON_ACTIVE(SECONDARY_BUTTON.dimensions.getX(), SECONDARY_BUTTON.dimensions.getY()),
	SIDEBAR_BUTTON(new Units(Controls.sidebarWidthToHeightRatio), new Units(1)),
	MEMBER_READY(new Units(1.5), new Units(1.5)),
	MEMBER_ASLEEP(new Units(1.5), new Units(1.5)),
	;

	private static final Map<Images, Image> images = new HashMap<>();

	private static final int CUSTOMER_COUNT = 5;

	private static Image[] customers;

	private final Vector dimensions;

	Images() {
		this.dimensions = null;
	}

	Images(Units width, Units height) {
		this.dimensions = new Vector(width, height);
	}

	/**
	 * Retrieves the image for the given image type.
	 *
	 * @param image The image type.
	 * @return The corresponding Image object.
	 */
	public static Image get(Images image) {
		if (image.dimensions == null) {
			return Images.images.computeIfAbsent(image,
				key -> new Image(Resource.get(key.toString())));
		}

		return Images.images.computeIfAbsent(image,
			key -> new Image(Resource.get(key.toString()), image.dimensions.getX().px(),
				image.dimensions.getY().px(), true, false));
	}

	/**
	 * Retrieves a customer image based on the provided hash value and anger state.
	 *
	 * @param hash  The hash value.
	 * @param angry The anger state.
	 * @return The corresponding Image object.
	 */
	public static Image customer(int hash, boolean angry) {
		if (Images.customers == null) {
			Images.customers = new Image[Images.CUSTOMER_COUNT * 2];

			for (int i = 0; i < Images.CUSTOMER_COUNT; ++i) {
				var neutralVariant = new Image(
					Resource.get("customer/" + (i + 1) + "_neutral.png"),
					Workbench.SIZE.px(), Workbench.SIZE.px(), true, false);
				var angryVariant = new Image(Resource.get("customer/" + (i + 1) + "_angry.png"),
					Workbench.SIZE.px(), Workbench.SIZE.px(), true, false);

				Images.customers[2 * i] = neutralVariant;
				Images.customers[2 * i + 1] = angryVariant;
			}
		}

		return Images.customers[2 * (Math.abs(hash) % Images.CUSTOMER_COUNT) + (angry ? 1 : 0)];
	}

	/**
	 * Converts the enum constant to its corresponding filename.
	 *
	 * @return The filename as a string.
	 */
	@Override
	public String toString() {
		return "misc/" + this.name().toLowerCase() + ".png";
	}
}
