class agent_c extends TAG {
    def schedule = []
    
    void setup() {
        server("localhost:8080")
    }
    
    void loop(Map msg) {
        println msg
	// ACLからcからaに送信命令を受信
        if (msg._p=='request-appointment') {
	    // aにappointを送信
	    // Map m = send('agent_a', [_p:'inform', hour:schedule.hour, item:scledule.item, with:schedule.with])
            if (msg.with == 'agent_a') {
                send('agent_a', [_p:'appointment', hour:msg.hour, item:msg.item, with:msg.with])
                // schedule.add([hour:msg.hour, item:msg.item, with:msg.with]) // schedulemに追加

                // reply(msg, [_p:'accept', hour:schedule.hour, item:scledule.item, with:schedule.with]) // acceptを送信
                // reply(msg, [_p:'inform', name:onsen.name, type:onsen.type, city:onsen.city, point:onsen.point])
                // send('from-user', [_p:'inform', hour:schedule.hour, item:scledule.item, with:schedule.with])
            }
            return
        }
        reply(msg, [_p:'sorry', msg:msg])
    }
}
