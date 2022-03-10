package com.scitrader.finance.study

data class StudyId(val id: String) {
    override fun toString(): String {
        return id
    }

    companion object {
        @JvmStatic
        fun uniqueId(name: String) : StudyId {
            return StudyId("$name${System.currentTimeMillis()}")
        }
    }
}
