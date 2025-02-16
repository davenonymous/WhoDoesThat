package com.davenonymous.whodoesthat.data.result;

import com.mojang.serialization.Codec;

public interface WithCodec<T> {
	Codec<T> codec();
}
