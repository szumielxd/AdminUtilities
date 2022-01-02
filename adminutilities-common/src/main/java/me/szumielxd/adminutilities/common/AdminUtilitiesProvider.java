package me.szumielxd.adminutilities.common;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AdminUtilitiesProvider {
	
	
	private static @Nullable AdminUtilities instance = null;
	
	
	public static void init(@NotNull AdminUtilities instance) {
		AdminUtilitiesProvider.instance = Objects.requireNonNull(instance, "instance cannot be null");
	}
	
	
	public static @NotNull AdminUtilities get() {
		if (instance == null) throw new IllegalArgumentException("AdminUtilities is not initialized");
		return instance;
	}
	

}
