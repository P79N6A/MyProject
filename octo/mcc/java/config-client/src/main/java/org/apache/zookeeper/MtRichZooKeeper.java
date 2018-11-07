package org.apache.zookeeper;

import java.io.IOException;

@Deprecated
public class MtRichZooKeeper extends ZooKeeper {
	protected String lastConnAddress = null;
	private final String connString;

	public MtRichZooKeeper(String connectString, int sessionTimeout, Watcher watcher) throws IOException {
		super(connectString, sessionTimeout, watcher);
		this.connString = connectString;
	}

	public MtRichZooKeeper(String connectString, int sessionTimeout, Watcher watcher, boolean canBeReadOnly) throws IOException {
		super(connectString, sessionTimeout, watcher, canBeReadOnly);
		this.connString = connectString;
	}

	public MtRichZooKeeper(String connectString, int sessionTimeout, Watcher watcher, long sessionId, byte[] sessionPasswd) throws IOException {
		super(connectString, sessionTimeout, watcher, sessionId, sessionPasswd);
		this.connString = connectString;
	}

	public MtRichZooKeeper(String connectString, int sessionTimeout, Watcher watcher, long sessionId, byte[] sessionPasswd, boolean canBeReadOnly) throws IOException {
		super(connectString, sessionTimeout, watcher, sessionId, sessionPasswd, canBeReadOnly);
		this.connString = connectString;
	}

	public String getCurrentConnAddress() {
		return connString;
	}

	public String getConnString() {
		return connString;
	}
}
