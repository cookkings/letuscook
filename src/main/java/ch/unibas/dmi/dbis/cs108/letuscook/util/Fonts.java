package ch.unibas.dmi.dbis.cs108.letuscook.util;

import ch.unibas.dmi.dbis.cs108.letuscook.orders.Workbench;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.text.Font;

/**
 * Enumeration representing various fonts used in the application.
 */
public enum Fonts {

	GAME_TITLE("upheavtt", 60),
	GAME_SUBTITLE("Pixellari", 25),
	GAME_NICKNAME("Pixellari", 15),
	GAME_DEBUG("Pixellari", Workbench.SIZE.px() / 10),
	UI_TITLE("Pixellari", 30),
	UI_PRIMARY("Pixellari", 14),
	UI_SECONDARY("Pixellari", 10),
	UI_SCORE("upheavtt", 40),
	;

	private static final Map<Fonts, Font> fonts = new HashMap<>();
	public static double SCALE = 1;
	private final String family;

	private final double sizePx;

	Fonts(String family, double sizePx) {
		this.family = family;
		this.sizePx = sizePx;
	}

	/**
	 * Retrieves the font for the given font type.
	 *
	 * @param font The font type.
	 * @return The corresponding Font object.
	 */
	public static Font get(Fonts font) {
		return fonts.computeIfAbsent(font,
			key -> Font.loadFont(Resource.get("fonts/" + font.family + ".ttf"),
				font.sizePx * SCALE));
	}
}
