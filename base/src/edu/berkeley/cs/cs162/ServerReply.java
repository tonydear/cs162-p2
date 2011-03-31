package edu.berkeley.cs.cs162;

public enum ServerReply {
	NONE,
	OK,
	REJECTED,
	QUEUED,
	OK_CREATE,
	OK_JOIN,
	BAD_GROUP,
	FAIL_FULL,
	NOT_MEMBER,
	ALREADY_MEMBER,
	BAD_DEST,
	FAIL,
	sendack,
	receive,
	timeout,
	error
}
