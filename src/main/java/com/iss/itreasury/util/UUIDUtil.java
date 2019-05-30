package com.iss.itreasury.util;

import java.util.UUID;

public class UUIDUtil {
	public static String creatUUID() {
		return UUID.randomUUID().toString().replace("-", "");
	}
}
