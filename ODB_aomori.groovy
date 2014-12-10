class ODB_aomori extends TAG {

	def onsenlist = [
		[name:'湯野川温泉', type:'単純泉',         city:'川内町', point:80],
		[name:'湯坂温泉',   type:'単純硫化水素泉', city:'大畑町', point:50],
		[name:'大間温泉',   type:'食塩泉',         city:'大間町', point:30] ]

			void setup() {
				server("localhost:8080")
			}

	void loop(Map msg) {
		println msg
			if (msg._p=='request-information') {
				if (msg.name != null) {  // 温泉名による検索
					def onsen = onsenlist.find{it.name==msg.name}
					if (onsen!=null) {
						reply(msg, [_p:'inform', name:onsen.name, type:onsen.type, city:onsen.city, point:onsen.point])
					} else {
						reply(msg, [_p:'retrieve-faild', name:msg.name, type:'unknown', city:'unknown', point:'unknown'])
					}
					return
				} else if (msg.type != null) { // 泉質による検索
					def onsen = onsenlist.find{it.type==msg.type}
					if (onsen!=null) {
						reply(msg, [_p:'inform', name:onsen.name, type:onsen.type, city:onsen.city, point:onsen.point])
					} else {
						reply(msg, [_p:'retrieve-faild', name:msg.name, type:'unknown', city:'unknown', point:'unknown'])
					}
					return
				} else if (msg.point == 'max') { // 評価値による検索
					def onsen = onsenlist.max{it.point}
					reply(msg, [_p:'inform', name:onsen.name, type:onsen.type, city:onsen.city, point:onsen.point])
						return
				} else if (msg.city != null) { // 町による検索
					def onsen = onsenlist.find{it.city==msg.city}
					if (onsen!=null) {
						reply(msg, [_p:'inform', name:onsen.name, type:onsen.type, city:onsen.city, point:onsen.point])
					} else {
						reply(msg, [_p:'retrieve-faild', name:msg.name, type:'unknown', city:'unknown', point:'unknown'])
					}
					return
				}
			}
			reply(msg, [_p:'sorry', msg:msg])
		}
}

