package com.sankuai.meituan.config.model;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.sankuai.meituan.config.constant.ParamName;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Deprecated
public class ClientConnNode {
	public static final ClientConnNode ROOT = new ClientConnNode(ParamName.CONFIG_BASE_PATH.substring(1), null);

	private String name;
	private final ClientConnNode parentNode;
	private final Set<String> clientIps = Sets.newHashSet();
	private final Map<String, ClientConnNode> childByName = Maps.newHashMap();

	private ClientConnNode(String name, ClientConnNode parentNode) {
		this.name = name;
		this.parentNode = parentNode;
	}

	public static void registerConnector(String nodePath, String ip) {
		Assert.isTrue(StringUtils.startsWith(nodePath, ParamName.CONFIG_BASE_PATH), "mtconfig的节点路径必须以/config开头");
		ROOT.registerConnector(StringUtils.split(nodePath, '/'), 1, ip);
	}

	public static Set<String> getClientIps(String nodePath) {
		ClientConnNode node = ROOT.findByPath(StringUtils.split(nodePath, '/'), 1);
		if (node != null) {
			return node.getClientIps();
		}else {
			return Collections.emptySet();
		}
	}

	private void registerConnector(String[] nodePath, int nextPathIndex, String ip) {
		if (nextPathIndex < nodePath.length) {
			String childName = nodePath[nextPathIndex];
			if (! childByName.containsKey(childName)) {
				ClientConnNode newChild = new ClientConnNode(childName, this);
				this.childByName.put(childName, newChild);
			}
			childByName.get(childName).registerConnector(nodePath, nextPathIndex + 1, ip);
		} else {
			clientIps.add(ip);
		}
	}

	private Set<String> getClientIps() {
		//TODO 改成用iterator拼接器
		Set<String> ips = Sets.newHashSet();
		ips.addAll(this.clientIps);
		for (ClientConnNode clientConnNode : childByName.values()) {
			ips.addAll(clientConnNode.getClientIps());
		}
		return ips;
	}

	private ClientConnNode findByPath(String[] nodePath, int nextPathIndex) {
		if (nextPathIndex < nodePath.length) {
			if (! childByName.containsKey(nodePath[nextPathIndex])) {
				return null;
			} else {
				return childByName.get(nodePath[nextPathIndex]).findByPath(nodePath, nextPathIndex + 1);
			}
		} else {
			return this;
		}
	}
}
