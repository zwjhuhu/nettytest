package net.zwj.netty.filedistribute;

import java.io.Serializable;

public class FileRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8035836538509530979L;
	private String cmd;
	private int count;
	private int offset;
	private int chunckSize;
	private byte[] entity;

	public String getCmd() {
		return cmd;
	}

	public void setCmd(String cmd) {
		this.cmd = cmd;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getChunckSize() {
		return chunckSize;
	}

	public void setChunckSize(int chunckSize) {
		this.chunckSize = chunckSize;
	}

	public byte[] getEntity() {
		return entity;
	}

	public void setEntity(byte[] entity) {
		this.entity = entity;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

}
