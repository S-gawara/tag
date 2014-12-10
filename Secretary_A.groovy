class Secretary_A extends Tag {
	public void setup(){
		server("local.host:8080")
	}
	public void loop(Map msg) {
		println msg
		if (msg._p=='appointment' && msg.hour !=  && msg.item &&){
			// B
			if (msg.to Seracher == b) {
				def schedule.add([hour:m.hour, item:m.item, with:m.with])
				def replies = request(['Secretary_B'], [_p:'appointment'], )
				send("acept")
				send([hour:m.hour, item:m.item, with:m.with])
				reply(msg, [_p:'', ])
			// C
			if (msg.){
			// }

		}else if(msg._p=='cancel' && ,sg){
			()
		}else if(){
		}else if(){
		}else {
			send('user', [_p:'sorry', msg:msg])
		}
	}
}
TAG.start("localhost:8080", Secretary, Secretary_A, Secretary_B, Secretary_C)


// 予約事象
(schedule :hour <開始時刻> :item <予約事象名> :with<人名> )
