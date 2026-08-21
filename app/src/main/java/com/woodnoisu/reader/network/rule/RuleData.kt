package com.woodnoisu.reader.network.rule

class RuleData {
    val variableMap: HashMap<String, String> = HashMap()

    fun putVariable(key: String, value: String) {
        variableMap[key] = value
    }
}
