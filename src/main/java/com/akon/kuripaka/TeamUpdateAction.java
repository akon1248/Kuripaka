package com.akon.kuripaka;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum TeamUpdateAction {

	ADD(0),
	REMOVE(1),
	CHANGE(2),
	ADD_PLAYERS(3),
	REMOVE_PLAYERS(4);

	@Getter
	private final int actionId;

}
