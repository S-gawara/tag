public class Searcher extends TAG {
 
    public void setup() {
    }
 
    public void loop(Map msg) {
        if (msg._p=='request-information' && msg.ninkiOnsen=='tohoku') {
            def replies = request(['ODB_aomori', 'ODB_iwate', 'ODB_akita'], // 3つのエージェントに
                                  [_p:'request-information', point:'max'])  // request-informationを送信し，全ての返事を待つ
            replies.each{println it} // requestの返事を表示する
            Map maxpoint = replies.max{it.point} // msgsの中からpointが最大の要素を探す
            reply(msg, maxpoint)
        } else {
            send('user', [_p:'sorry', msg:msg])
        }
    }
 
}
 
TAG.start("localhost:8080", Searcher, ODB_aomori, ODB_iwate, ODB_akita)

