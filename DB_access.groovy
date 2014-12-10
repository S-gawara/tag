class DB_access extends TAG {
    
    void setup() {
        server("localhost:8080")
    }

    void loop(Map msg) {
        println msg
        if (msg._p=='from-user' && msg.whereis!=null) { // 所在地の問合せ
            Map m = request('ODB_aomori', [_p:'request-information', name:msg.whereis])
            reply(msg, [_p:'to-user', name:m.name, city:m.city])
        } else if (msg._p=='from-user' && msg.whattype!=null) { // 泉質の問合せ
            Map m = request('ODB_aomori', [_p:'request-information', name:msg.whattype])
            reply(msg, [_p:'to-user', name:m.name, type:m.type])
        } else if (msg._p=='from-user' && msg.rating!=null) { // 評価の問合せ
            Map m = request('ODB_aomori', [_p:'request-information', name:msg.rating])
            reply(msg, [_p:'to-user', name:m.name, point:m.point])
        } else {
            reply(msg, [_p:'sorry', msg:msg])
        }
    }
}
