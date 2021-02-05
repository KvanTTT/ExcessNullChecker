data class Signature(val name: String, val paramsCount: Int, val isVoid: Boolean) {
    companion object {
        fun get(name: String?, params: String?): Signature {
            if (params == null)
                return Signature("",0, true)

            var paramsCount = 0
            var index = 0

            while (index < params.length) {
                val c: Char = params[index]
                if (c == ')') {
                    index++
                    break
                }

                if (c != '(') {
                    if (c == 'L') {
                        while (index < params.length && params[index] != ';') {
                            index++
                        }
                    }
                    paramsCount++
                }

                index++
            }

            return Signature(name ?: "", paramsCount, index >= params.length || params[index] == 'V')
        }
    }
}