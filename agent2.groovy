class agent2 extends TAG {
    def schedule = []

    void setup() {
        server("localhost:8080")
    }       
  
    void loop(Map msg) {
        println msg
        // ACLからdo-appointmentを受信
        if (msg._p == 'do-appointment') {
                // agentへappointmentを送信
                if(msg.with == 'agent1'){
                    send('agent1', [_p:'appointment', hour:msg.hour, item:msg.item, with:msg.with])
                // agent3へappointmentを送信
                } else if(msg.with == 'agent3') {
                    send('agent3', [_p:'appointment', hour:msg.hour, item:msg.item, with:msg.with])
                }
        // 他のagentからappointmentを受信
        } else if (msg._p == 'appointment' && msg.with == 'agent2') {
            // schedulに追加
            schedule.add([hour:msg.hour, item:msg.item, with:msg._f])

            // agentへ返信
            if (msg._f == 'agent1'){
                send('agent1', [_p:'accept'])
            } else if (msg._f == 'agent3'){
                send('agent3', [_p:'accept'])
            }

            // ACLにinformを送信
            send('user', [_p:'inform', hour:msg.hour, item:msg.item, with:msg._f])
        } // else {
          //  reply(msg, [_p:'sorry', msg:msg])
        // }                                                                                                            
    }
}
