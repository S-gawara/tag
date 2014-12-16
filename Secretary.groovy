// Class名とスクリプト名を一緒にはできない
public class Secretary_A extends TAG {
    // def schedule = [_p:要求内容, hour:時間, item:会議, with:agent名, _f:依頼主]
    def schedule = []
    // Secretary_C = Secretary_A < Secretary_B
    def human = [ Secretary_C:0, Secretary_A:0, Secretary_B:1 ]

    void setup() {
        server("localhost:8080")
    }       
  
    void loop(Map msg) {
        println msg
	// ACLからdo-appointmentを受信
        if (msg._p == 'do-appointment') {
	    send(msg.with, [_p:'appointment', hour:msg.hour, item:msg.item, with:msg.with])
	    // request(msg.with, [_p:'appointment', hour:msg.hour, item:msg.item, with:msg.with])

	// 他のagentからappointmentを受信
        } else if (msg._p == 'appointment' && msg.with == 'Secretary_A') {
	    // scheduleが空かどうかを調べる
	    if(schedule == null) {
                // scheduleに追加
                schedule.add([hour:msg.hour, item:msg.item, with:msg._f])

	        // acceptを送信
	 	send(msg._f, [_p:'accept'])
		// reply(msg, [_p:'accept'])

		// ACLにinformを送信
                send('user', [_p:'inform', hour:msg.hour, item:msg.item, with:msg._f])	
	    } else {
	        // hourが重複しているかどうか調べる
		def time = schedule.find{it.hour == msg.hour}
              	if (time != null) {
	            // 先客
	            def visitor1 = human.get(time.with)
		    // 後客
		    def visitor2 = human.get(msg._f)

		    if (visitor1 < visitor2) {
			// 登録されているscheduleを削除
			schedule = schedule.minus time

	                // acceptを送信
                        send(msg._f, [_p:'accept'])
			// reply(msg, [_p:'accept'])

			// cancelを送信
			send(time.with, [_p:'cancel', hour:time.hour, item:time.item, with:time.with])

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
			send(msg._f, [_p:'accept'])
			// reply(msg, [_p:'accept'])

			// ACLにinformを送信
                	send('user', [_p:'inform', hour:msg.hour, item:msg.item, with:msg._f])	
		}
	    }
	} else if (msg._p == 'accept' && msg.with == 'Secretary_A') {
	    // scheduleに追加
	    schedule.add([hour:msg.hour, item:msg.item, with:msg._f])
        } else if(msg._p == 'cancel' && msg.with == 'Secretary_A') {
            // acceptを送信
            send(msg._f, [_p:'accept'])
	    // reply(msg, [_p:'accept'])

	    // 登録されているscheduleを削除
	    schedule = schedule.minus time
	}
	// println "[schedule]:" + schedule
    }
}

TAG.start("localhost:8080",Secretary_A,Secretary_B,Secretary_C)
