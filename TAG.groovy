abstract class TAG implements Runnable {

    /** エージェント名 */
    String agentname
    
    /** メッセージサーバのURL */
    String serverHost
    
    /** サーバからfetchしたメッセージをためておくローカルバッファ */
    List<TagMessage> messageBuffer = []
    
    /** 届いたrequest．replyあるいはrequestしなければならない */
    TagMessage requestedMessage
    
    /** メッセージ文字列をevaluate()するのに使う */
    GroovyShell shell
    
    /*
     * 実行中の行番号の取得(http://programamemo2.blogspot.jp/2007/10/groovy_16.html)
     */
    def getCurrentLineNumber() {
        def the_thisClassName = getClass().getName()
        def the_inThisObject = new Throwable().getStackTrace().findAll {the_stackTraceElement ->
            the_thisClassName.equals(the_stackTraceElement.getClassName())
        }
        return the_inThisObject[1].getLineNumber()
    }
    
    static {
        // メソッド追加(http://npnl.hatenablog.jp/entry/2012/12/10/041636)
        // assert [a:1, b:2, c:3].has([a:1, b:2])
        Map.metaClass.has = { map ->
            if (map instanceof Map)
                map.every{ key, value -> get(key)==value}
            else
                throw new IllegalArgumentException(getCurrentLineNumber()+": has()の引数はMapであるべきです")
        }
    }

    /** 複数のエージェントの同時起動(単独の場合，groovyによって自動的にスレッドが作られる) */
    def static start(def host, Object[] classname) {
        def s = []
        classname.each {
            if (it instanceof Class && TAG.isAssignableFrom(it)) { // TAGのサブクラスの場合
                TAG ag = it.newInstance()
                ag.server(host)
                new Thread(ag).start()
                s.add(ag.agentname)
            } else if (it instanceof TAG) { // TAGのインスタンスの場合
                it.server(host)
                new Thread(it).start()
                s.add(it.agentname)
            } else {
                System.err.println("TAG.start(): invalid agent: "+it)
                System.exit(1)
            }
        }
        println("start: "+s)
    }
    
    
    /** コンストラクタ */
    def TAG() {
        agentname = getClass().getSimpleName() // エージェント名は指定無き場合クラス名となる
        shell = new GroovyShell()
    }
    
    /** 名前を指定するコンストラクタ */
    def TAG(String name) {
        agentname = name
        shell = new GroovyShell()
    }
    
    /** hostには"localhost:8080"や"www.hara.net.it-chiba.ac.jp:8080"などを指定する */
    void server(String host) {
        if (serverHost == null) // TAG.start()で指定されている場合，そちらを優先する
          serverHost = host
    }

    /** 初期化用 */
    abstract void setup()
    
    /** メインループ */
    abstract void loop(Map msg)
    
    /** スクリプトを実行する */
    void run() {
        setup()
        while (true) {
            if (!messageBuffer.isEmpty() || fetch() > 0) { // バッファに残っている場合はfetch()しない
                TagMessage m = messageBuffer.remove(0)
                requestedMessage = m._p.startsWith("request") ? m : null
                loop(m)
                if (requestedMessage != null)  // requestに対しreplyあるいはrequestしてない場合は例外を上げる
                    throw new Exception(getClass().getName()+": forgot reply/request for "+m)
            } else {
                Thread.sleep(1000)
            }
        }
    } 
    
    /** sendする */
    String send(String agent, Map map) {
        return send([agent], map, null)
    }
    
    /** replyする */
    String reply(TagMessage msg, Map map) {
        String uuid = send([msg._f], map, getUUID(msg))
        if (requestedMessage!=null && requestedMessage.getUuid()==msg.getUuid()) {
            requestedMessage = null
        }
        return uuid
    }
    
    /** 複数に同時にsendする */
    String send(List agents, Map map, String replyID) {
        map._f = agentname
        def json = mapToString(map)
        String uuid = UUID.randomUUID().toString()
        String toString = agents.collect{"to="+it}.join("&"); // "to=ag1&to=ag2&to=ag3"のようなStringを作る
        String params = "c=send&from=$agentname&$toString&msg=$json&uuid=$uuid"+(replyID==null ? "" : "&replyto=$replyID")
        sendToMessageServer(params)
        return uuid
    }

    /** メッセージサーバにsend/replyする */
    private void sendToMessageServer(String params) {
        HttpURLConnection connection = "http://$serverHost/tag/".toURL().openConnection()
        connection.setDoOutput(true)
        connection.outputStream.withWriter('UTF-8') { Writer writer ->
          writer << params
        }
        String response = connection.inputStream.withReader { Reader reader -> reader.text }
        connection.disconnect() // closeはwithWriterとwithReaderが行う
    }


    /** fetchする */
    int fetch() {
        HttpURLConnection connection = "http://$serverHost/tag/".toURL().openConnection()
        connection.setDoOutput(true)
        connection.outputStream.withWriter('UTF-8') { Writer writer ->
          writer << "c=fetch&agent=$agentname"
        }
        String response = connection.inputStream.withReader('UTF-8') { Reader reader -> reader.text }
        connection.disconnect()
        if (response == "0") {
            return 0
        } else {
            int size=0
            response.splitEachLine("\n", {
                String res = it[0] // 1行
                int p = res.indexOf('*')
                int q = res.indexOf('*', p+1)
                String uuid = res.substring(0, p)
                String replyTo = res.substring(p+1, q) // 指定されていない場合は"0"
                Map map  = shell.evaluate(res.substring(q+1))
                messageBuffer.add(new TagMessage(uuid, (replyTo=="0" ? null : replyTo), map))
                size++
                })
            return size
        }
    }
    
    /** uuidListに含まれるreplyIDを持つメッセージが1つ届くまで待つ．loop()の途中でrun()のようなメッセージ待ちを行う． */
    TagMessage waitAny(List<String> uuidList) {
        TagMessage msg = null
        while (true) {
            fetch() // バッファに残っている場合もfetch()する
            msg = messageBuffer.find{def m -> uuidList.contains(m.getReplyId())}
            if (msg != null) {
                messageBuffer.remove(msg)
                break
            }
            Thread.sleep(1000)
        }
        return msg
    }
    
    /** uuidListに含まれるreplyIDを持つメッセージが全て届くまで待つ． */
    List<TagMessage> waitAll(List<String> uuidList) {
        List<TagMessage> messages = []
        List<String> remaining = [] + uuidList
        while (true) {
            fetch() // バッファに残っている場合もfetch()する
            def msgs = messageBuffer.findAll{def m -> remaining.contains(m.getReplyId())}
            if (msgs != []) {
                messageBuffer -= msgs
                messages += msgs
                remaining -= msgs.collect{it.getReplyId()}
                if (remaining == []) 
                   break
            }
            Thread.sleep(1000)
        }
        return messages
    }
    
    /** requestを送信し，返事を待つ */
    TagMessage request(String agent, Map map) {
        if (!map._p.startsWith("request")) {
            throw new Exception("Message which is sent by request() should have a \"request\" performative.")
        }
        String uuid = send(agent, map)
        TagMessage msg = waitAny([uuid])
        if (msg._p.startsWith("request"))
            requestedMessage = msg
        return msg
    }
    
    /** カウンターrequestを送信し，返事を待つ */
    TagMessage request(TagMessage request, Map map) {
        if (!map._p.startsWith("request")) {
            throw new Exception("Message which is sent by request() should have a \"request\" performative.")
        }
        String uuid = reply(request, map)
        TagMessage msg = waitAny([uuid])
        if (msg._p.startsWith("request"))
            requestedMessage = msg
        return msg
    }
    
    /** いくつかのrequestを送信し，全ての返事を待つ．カウンターリクエストは来ないという前提． */
    List<TagMessage> request(List<String> agents, Map map) {
        if (!map._p.startsWith("request")) {
            throw new Exception("Message which is sent by request() should have a \"request\" performative.")
        }
        List<String> uuids = agents.collect{send(it, map)} // 個別にsendするので，uuidは全て異なる
        List<TagMessage> msgs = waitAll(uuids)
        if (msgs.find{it._p.startsWith("request")})
            throw new Exception("request($agents, $map) receives request:"+msgs.find{it._p.startsWith("request")}); // もし来たら例外
        return msgs
    }
    
    /** mapをevaluate()可能な文字列に変換する */
    String mapToString(Map map) {
        if (map.isEmpty()) {
            return "[:]"
        } else {
            def s = "["
            map.each {key, value ->
                s += "$key:"
                s += valueToString(value)
                s += ", "
            }
            return s.substring(0, s.length()-2)+"]"
        }
    }

    String listToString(List list) {
        def s = "["
        list.each {
            s += valueToString(it)
            s += ", "
        }
        return s.substring(0, s.length()-2)+"]"
    }

    String valueToString(def value) {
        if (value == null) return "null"
        switch (value.getClass()) {
            case String:  return "'$value'"
            case Number:  return value
            case Map:     return mapToString(value)
            case List:    return listToString(value)
            case Boolean: return value.toString()
        }
        System.err0.println("invalid value: "+value)
        return null
    }

    String getUUID(TagMessage msg) { return msg.getUuid() }
    
    String getReplyID(TagMessage msg) { return msg.getReplyId() }

}

/** TAG.groovyだけがあれば動くように内部クラスにしている(クラス名とファイル名を一致させないとクラスを見つけられない) */
class TagMessage extends java.util.LinkedHashMap {

    String uuid // メッセージID
    String replyId // reply対象のメッセージID．ない場合はnull

    /**
     * replyToは，ない場合はnull
     */
    TagMessage(String uuid, String replyId, Map map) {
        super(map)
        this.uuid = uuid
        this.replyId = replyId
    }
}
