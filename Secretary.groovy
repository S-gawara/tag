// Class名とスクリプト名を一緒にはできない
public class Secretary_A extends TAG {
    // def schedule = [_p:要求内容, hour:時間, item:会議, with:依頼相手, _f:依頼主]
    def schedule = []
    // 重要度(Secretary_A = Secretary_C < Secretary_B)
    def human = [ Secretary_A:0, Secretary_B:1, Secretary_C:0 ]

    void setup() {
    }
  
    void loop(Map msg) {
        println msg
	// ACLからdo-appointment受信
        if (msg._p == 'do-appointment') {
	    send(msg.with, [_p:'appointment', hour:msg.hour, item:msg.item, with:msg.with])
	// 他のSecretaryからappointment受信
        } else if (msg._p == 'appointment' && msg.with == 'Secretary_A') {
	    // accept送信
	    send(msg._f, [_p:'accept'])
	    // scheduleが空かどうか調べる
	    if(schedule == null) {
                // schedule追加
                schedule.add([hour:msg.hour, item:msg.item, with:msg._f])
		// ACLにinform送信
		send('user', [_p:'inform', hour:msg.hour, item:msg.item, with:msg._f])
	    } else {
	        // hourが重複しているかどうか調べる
		def time = schedule.find{it.hour == msg.hour}
              	if (time != null) {
	            // 先客
	            def visitor1 = human.get(time.with)
		    // 後客
		    def visitor2 = human.get(msg._f)

		    // 優先度の比較
		    if (visitor1 < visitor2) {
			// cancel送信
			send(time.with, [_p:'cancel', hour:time.hour, item:time.item, with:time.with])
		        // schedule追加
			schedule.add([hour:msg.hour, item:msg.item, with:msg._f])
		    } else {
		        // refusalを送信
		        send(msg._f, [_p:'refusal'])
		    }
		} else { // 時間が重複していなければ，比較必要なし
                	// schedule追加
                	schedule.add([hour:msg.hour, item:msg.item, with:msg._f])
			// ACLにinform送信
			send('user', [_p:'inform', hour:msg.hour, item:msg.item, with:msg._f])
		}
	    }
	} else if (msg._p == 'accept' && msg.with == 'Secretary_A') {
	    // schedule追加
	    schedule.add([hour:msg.hour, item:msg.item, with:msg._f])
        } else if(msg._p == 'cancel' && msg.with == 'Secretary_A') {
            // accept送信
            send(msg._f, [_p:'accept'])
	    // ACLにinform送信
	    send('user', [_p:'inform', hour:msg.hour, item:msg.item, with:msg._f])
	}
    }
}

TAG.start("localhost:8080", Secretary_A, Secretary_B, Secretary_C)
