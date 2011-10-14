package rapi4j;

import java.util.EventObject;

public class CopyEvent extends EventObject {
	public static enum Type {
		Begin, Progress, End
	}

	private final String filePath;
	private final Type type;

	public CopyEvent(final Object source, final String filePath, final Type type) {
		super(source);
		this.type = type;
		this.filePath = filePath;
	}

	/**
	 * @return the filePath
	 */
	public String getFilePath() {
		return this.filePath;
	}

	/**
	 * @return the type
	 */
	public Type getType() {
		return type;
	}

	private static final long serialVersionUID = 5837190356952507175L;
}