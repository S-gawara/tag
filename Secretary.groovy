class Secretary extends Tag {
	public void setup()
	}
	public void loop(Map msg) {

	}
		// Aがappoint送信
		if (msg._p=='appointment' && msg.to && msg.to Searcher $$ msg.content){
			def schedule.add([hour:m.hour, item:m.item, with:m.with])
			send"acept")
			send([hour:m.hour, item:m.item, with:m.with])
			reply(msg, [_p:'', ])

			// if(schedule.length > 1){
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
