class agent1 extends TAG {
    // def schedule = [_p:要求内容, hour:時間, item:会議, with:agent名, _f:依頼主]
    def schedule = []
    // agent3 < agent2 < agent1
    def human = [ agent3:0, agent2:1, agent1:2 ]

    void setup() {
        server("localhost:8080")
    }       
  
    void loop(Map msg) {
        println msg
	// ACLからdo-appointmentを受信
        if (msg._p == 'do-appointment') {
	    send(msg.with, [_p:'appointment', hour:msg.hour, item:msg.item, with:msg.with])
	// 他のagentからappointmentを受信
        } else if (msg._p == 'appointment' && msg.with == 'agent1') {
		// scheduleが空かどうかを調べる
		if(schedule == null) {
                    // scheduleに追加
                    schedule.add([hour:msg.hour, item:msg.item, with:msg._f])
		} else {
		    // hourが重複しているかどうか調べる
		    def time = schedule.find{it.hour == msg.hour}
              	    if (time != null) {
                        // 優先度の比較
		        println "先客は" + time.with
//			def visitor = time.with
			println "agent3の値は" + human.get(time.with)


//			def visitor1 = human.find{ it.find == visitor }
			

//			def visitor = schedule.with
//			println "visitor" + visitor

			// cancelを送信
//		　　　　send(msg._f, [_p:'cancel'])
			// ACLに新しい情報を送信
//		　　　　send('user', [_p:'inform', hour:msg.hour, item:msg.item, with:msg._f])
		    }
		    // 重複していなければ実行
		    else {
		        // scheduleに追加
		        schedule.add([hour:msg.hour, item:msg.item, with:msg._f])

	    	        // acceptを送信
                        send(msg._f, [_p:'accept'])

	                // ACLにinformを送信
                        send('user', [_p:'inform', hour:msg.hour, item:msg.item, with:msg._f])
		    }
		}
        } else if (msg._p == 'cancel' && msg.with == 'agent1') { //msg.withでいい？
            // acceptを送信
            send(msg._f, [_p:'accept'])
	}
    }
}
