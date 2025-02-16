package com.davenonymous.whodoesthat.util;

import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Comparator;

public class GsonHelper {
	public static String toStableIndentedString(JsonElement json) {
		StringWriter stringwriter = new StringWriter();
		JsonWriter jsonwriter = new JsonWriter(stringwriter);
		jsonwriter.setIndent("  ");

		try {
			net.minecraft.util.GsonHelper.writeValue(jsonwriter, json, Comparator.naturalOrder());
		} catch (IOException ioexception) {
			throw new AssertionError(ioexception);
		}

		return stringwriter.toString();
	}
}
