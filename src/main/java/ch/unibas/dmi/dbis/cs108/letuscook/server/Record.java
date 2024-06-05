package ch.unibas.dmi.dbis.cs108.letuscook.server;

/**
 * A representation of a person.
 */
public class Record {

	/**
	 * The person's IP address.
	 */
	private final String address;

	/**
	 * The person's nickname.
	 */
	private String nickname;

	/**
	 * Create a record.
	 *
	 * @param nickname the nickname.
	 */
	public Record(String nickname, String address) {
		assert nickname != null : "cannot create record with null nickname";

		this.nickname = nickname;
		this.address = address;
	}

	/**
	 * @return whether this record has an address.
	 */
	boolean hasAddress() {
		return this.address != null;
	}

	/**
	 * @return this record's address.
	 */
	String getAddress() {
		assert this.hasAddress();

		return this.address;
	}

	/**
	 * @return this record's nickname.
	 */
	public String getNickname() {
		assert this.nickname != null : "nickname is null";

		return this.nickname;
	}

	/**
	 * Set this record's nickname.
	 *
	 * @param nickname the nickname.
	 */
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
}
