package com.yoyakso.comket.oauth2.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GoogleDetailResponse {
	private String id;
	private String email;
	private String name;
	private String picture;
}
