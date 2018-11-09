package org.orecruncher.patchwork.common.item.magic;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.gson.annotations.SerializedName;

public class Devices {
	
	public static class DeviceEntry {
		@SerializedName("name")
		public String name;
		@SerializedName("type")
		public ItemMagicDevice.Type type = ItemMagicDevice.Type.TRINKET;
		@SerializedName("variant")
		public Integer variant = 0;
		@SerializedName("quality")
		public ItemMagicDevice.Quality quality = ItemMagicDevice.Quality.MUNDANE;
		@SerializedName("abilities")
		public List<String> abilities = ImmutableList.of();
	}
	
	@SerializedName("devices")
	public List<DeviceEntry> entries = ImmutableList.of();

}
