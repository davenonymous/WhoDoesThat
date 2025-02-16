package com.davenonymous.whodoesthat.data.getter;

import com.davenonymous.whodoesthat.data.result.WithCodec;
import net.neoforged.neoforgespi.language.ModFileScanData;

public interface IDataGetter<T extends WithCodec<T>> {
	String getKey();

	T getData(ModFileScanData data);
}
