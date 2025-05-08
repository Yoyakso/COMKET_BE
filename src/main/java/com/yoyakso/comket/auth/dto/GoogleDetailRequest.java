package com.yoyakso.comket.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GoogleDetailRequest {
	private String accessToken;
}