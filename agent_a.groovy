class agent_a extends TAG {
    def schedule = []

    void setup() {
        server("localhost:8080")
    }
    void loop(Map msg) {
        println msg
        if (msg._p == 'request-appointment') {
            if (msg.with == 'agent_c') {  // 自分宛だったら
                // schedule.add([hour:msg.hour, item:msg.item, with:msg.with]) // schedulemに追加
		// appointmentを送信
                // send('agent-c', [_p:'appointment', hour:schedule.hour, item:scledule.item, with:schedule.with])
                // reply(msg, [_p:'inform', name:onsen.name, type:onsen.type, city:onsen.city, point:onsen.point])
	        // send('from-user', [_p:'inform', hour:schedule.hour, item:scledule.item, with:schedule.with])
            }
        } else if (msg._p == 'appointment') {
	    // 自分宛だったら
	    if (msg.with == 'agent_a'){
//	        schedule.add([hour:msg.hour, item:msg.item, with:msg._f])
		send('agent_c', [_p:'accept'])
		send('user', [_p:'inform', hour:schedule.hour, item:scledule.item, with:schedule.with]) 
	    }
            return
        }
        reply(msg, [_p:'sorry', msg:msg])
    }
}

