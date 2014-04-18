package messages;

public enum MessageType {
	ClientServerPing,
	//ServerClientPing, not so useful
	Subscribe2Server,
	UnSubscribeFromServer,
	Action,
	GetBattlefield,
	RedirectConnection
	//more types are put here
}
