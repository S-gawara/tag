class Secretary_B extends TAG {
    // def schedule = [_p:要求内容, hour:時間, item:会議, with:agent名, _f:依頼主]
    def schedule = []
    // Secretary_A < Secretary_C < Secretary_B
    def human = [ Secretary_A:0, Secretary_B:2, Secretary_C:1 ]

    void setup() {
    }

    void loop(Map msg) {
        println msg
	// ACLからdo-appointmentを受信
	if (msg._p == 'do-appointment') {
	    send(msg.with, [_p:'appointment', hour:msg.hour, item:msg.item, with:msg.with])
	    // 他のagentからappointmentを受信
	} else if (msg._p == 'appointment' && msg.with == 'Secretary_B') {
	    // acceptを送信
	    send(msg._f, [_p:'accept'])
	    // scheduleが空かどうかを調べる
	    if(schedule == null) {
	        // scheduleに追加
	        schedule.add([hour:msg.hour, item:msg.item, with:msg._f])
		// ACLにinformを送信
		send('user', [_p:'inform', hour:msg.hour, item:msg.item, with:msg._f])	
	    } else {
	        // acceptを送信
	        // send(msg._f, [_p:'accept'])
		
		// hourが重複しているかどうか調べる
		def time = schedule.find{it.hour == msg.hour}
		if (time != null) {
		    // 先客
		    def visitor1 = human.get(time.with)
		    // 後客
		    def visitor2 = human.get(msg._f)

		    // 優先度の比較
		    if (visitor1 < visitor2) {
		        // cancelを送信
			send(time.with, [_p:'cancel'])
			// scheduleに追加
			schedule.add([hour:msg.hour, item:msg.item, with:msg._f])                         
			// ACLにinformを送信
			send('user', [_p:'inform', hour:msg.hour, item:msg.item, with:msg._f])
		    } else {
			// refusalを送信
			send(msg._f, [_p:'refusal'])
		    }
		} else {
		    // scheduleに追加
		    schedule.add([hour:msg.hour, item:msg.item, with:msg._f])
		    // acceptを送信
		    // send(msg._f, [_p:'accept'])
		    // ACLにinformを送信
		    send('user', [_p:'inform', hour:msg.hour, item:msg.item, with:msg._f])
		}
	    }
	} else if (msg._p == 'cancel' && msg.with == 'Secretary_B') {
	    // acceptを送信
	    send(msg._f, [_p:'accept'])
	}
    }
}
