package ch.unibas.dmi.dbis.cs108.letuscook.gui;

/**
 * Represents various views available in the GUI.
 */
public enum Views {
	SPLASH(false, false, false, false, false),
	START(false, false, false, false, false),
	LOBBIES(true, true, true, true, true),
	TUTORIAL(true, true, false, true, true),
	LOBBY(true, false, true, true, true),
	GAME(true, false, false, true, true),
	HIGHSCORES(false, false, false, true, false),
	RECIPES(false, false, false, true, false),
	;

	public final boolean showChat;

	public final boolean alwaysYell;

	public final boolean keepChatFocused;

	public final boolean showSidebar;

	public final boolean showFullSidebar;

	/**
	 * Constructs a new Views enum with the specified parameters.
	 *
	 * @param showChat        Indicates whether the chat should be shown.
	 * @param alwaysYell      Indicates whether chat messages should always be yelled.
	 * @param keepChatFocused Indicates whether the chat should be kept focused.
	 * @param showSidebar     Indicates whether the sidebar should be shown.
	 * @param showFullSidebar Indicates whether the full sidebar should be shown.
	 */
	Views(boolean showChat, boolean alwaysYell, boolean keepChatFocused, boolean showSidebar,
		boolean showFullSidebar) {
		this.showChat = showChat;
		this.alwaysYell = alwaysYell;
		this.keepChatFocused = keepChatFocused;
		this.showSidebar = showSidebar;
		this.showFullSidebar = showFullSidebar;
	}
}
