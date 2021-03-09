package com.akon.kuripaka.reflection;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;

@UtilityClass
public class ClassUtil {

	private final String NMS_VERSION = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

	public ReflectionResult<Class<?>> getClass(String name) {
		return ReflectionResult.of(() -> Class.forName(name));
	}

	public ReflectionResult<Class<?>> getNMSClass(String name) {
		return getClass("net.minecraft.server." + NMS_VERSION + "." + name);
	}

	public ReflectionResult<Class<?>> getCBClass(String name) {
		return getClass("org.bukkit.craftbukkit." + NMS_VERSION + "." + name);
	}

}
