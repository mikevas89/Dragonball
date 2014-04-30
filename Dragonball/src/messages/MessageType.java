package messages;

public enum MessageType {
	ClientServerPing,
	ServerClientPing, 
	Subscribe2Server,
	UnSubscribeFromServer,
	Action,
	GetBattlefield,
	RedirectConnection,
	//more types are put here
	
	ServerServerPing,
	ServerSubscribedAck,
	//Subscribe2Server,
	CheckPending,
	PendingMoveInvalid,
	ProblematicServer,
	ResponseProblematicServer,
	NewCheckPoint,
	NewPlayer,
	
	NewValidAction,
	//GetBattlefield
	RequestBattlefield
}
