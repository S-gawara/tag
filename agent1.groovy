class agent1 extends TAG {
    def schedule = []

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
            // schedulに追加
            schedule.add([hour:msg.hour, item:msg.item, with:msg._f])

	    // acceptを送信
            send(msg._f, [_p:'accept'])

	    // ACLにinformを送信
            send('user', [_p:'inform', hour:msg.hour, item:msg.item, with:msg._f])
        } //  else if (msg._p == 'cansel' &&) {
    }
}
