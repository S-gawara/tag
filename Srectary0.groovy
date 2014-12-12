class Srectary extends TAG {                                                                    
    void setup() {
    }

    void loop(Map msg) {
        println msg
        if (msg._p=='inform') { // informを受け取る
	    send('user', [msg.msg])
        } else {
            reply(msg, [_p:'sorry', msg:msg])
        }
    }
}

TAG.start("localhost:8080", Srectary, agent_a)
