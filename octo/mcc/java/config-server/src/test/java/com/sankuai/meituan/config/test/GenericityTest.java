package com.sankuai.meituan.config.test;

import com.google.common.collect.Maps;
import org.junit.Test;

import java.util.Map;

public class GenericityTest {
	@Test
	public void testGenericity() {
		Map<String, String> map = createMap();
		System.out.println(map.keySet());
	}

	@Test
	public void testResource() {
		Resource<String, Integer> resource = createResource();
		System.out.println(resource.getK());
		System.out.println(resource.getV());
	}

	private Map createMap() {
		Map<String, String[]> map = Maps.newHashMap();
		map.put("test", new String[]{"test1", "test2"});
		return map;
	}

	private Resource createResource() {
		Resource<String, Integer> resource = new Resource<>("1", 1);
		return resource;
	}
}

class Resource<K, V>{
	private K k;
	private V v;

	public Resource(K k, V v) {
		this.k = k;
		this.v = v;
	}

	public K getK() {
		return k;
	}

	public V getV() {
		return v;
	}
}
