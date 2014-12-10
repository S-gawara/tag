class Simple extends TAG {

    void setup() {
        server("localhost:8080")
    }

    void loop(Map msg) {
        println msg
        if (msg._p=='request-information' && msg.question=='Designer?') {
            reply(msg, [_p:'inform', answer:'工大太郎'])
        } else {
            reply(msg, [_p:'sorry', msg:msg])
        }
    }
}
