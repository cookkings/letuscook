package ch.unibas.dmi.dbis.cs108.letuscook.util;

import java.util.HashMap;
import java.util.Map;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/**
 * Enumeration representing various sounds used in the application.
 */
public enum Sounds {

	BACKGROUND(false),
	ANGRY,
	CLICK,
	EXPIRE,
	INTERACT,
	UPDATE,
	;

	private static final Map<Sounds, Media> sounds = new HashMap<>();

	private static final Map<Sounds, MediaPlayer> mediaPlayers = new HashMap<>();

	private static boolean muteSoundEffects = false;

	private final boolean isSoundEffect;

	/**
	 * Constructs a sound with the specified sound effect status.
	 *
	 * @param isSoundEffect True if the sound is a sound effect, false otherwise.
	 */
	Sounds(boolean isSoundEffect) {
		this.isSoundEffect = isSoundEffect;
	}

	/**
	 * Constructs a sound with the default sound effect status (true).
	 */
	Sounds() {
		this(true);
	}

	/**
	 * Constructs a sound with the default sound effect status (true).
	 */
	public static boolean getMuteSoundEffects() {
		return Sounds.muteSoundEffects;
	}

	/**
	 * Sets the mute status of sound effects.
	 *
	 * @param muteSoundEffects True to mute sound effects, false otherwise.
	 */
	public static void setMuteSoundEffects(boolean muteSoundEffects) {
		Sounds.muteSoundEffects = muteSoundEffects;
	}

	private static Media get(Sounds sound) {
		return sounds.computeIfAbsent(sound, key -> new Media(Resource.get(key.toString())));
	}

	/**
	 * Gets the media player associated with this sound.
	 *
	 * @return The MediaPlayer object associated with this sound.
	 */
	public MediaPlayer getMediaPlayer() {
		return Sounds.mediaPlayers.get(this);
	}

	/**
	 * Plays the sound.
	 */
	public void play() {
		if (this.isSoundEffect && Sounds.muteSoundEffects) {
			return;
		}

		MediaPlayer mediaPlayer = mediaPlayers.computeIfAbsent(this,
			key -> {
				var player = new MediaPlayer(Sounds.get(key));
				if (!key.isSoundEffect) {
					player.setCycleCount(MediaPlayer.INDEFINITE);
					player.setVolume(0.25);
				}
				return player;
			});

		mediaPlayer.stop();
		mediaPlayer.play();
	}

	/**
	 * Converts the enum constant to its corresponding filename.
	 *
	 * @return The filename as a string.
	 */
	@Override
	public String toString() {
		return "sound/" + this.name().toLowerCase() + ".mp3";
	}
}
