package excessNullChecker

data class Signature(val fullName: String, val static: Boolean, val paramsCount: Int, val isVoid: Boolean) {
    companion object {
        fun get(static: Boolean, name: String?, params: String?): Signature {
            if (params == null)
                return Signature("", static,0, true)

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

            return Signature(name + params, static, paramsCount, index >= params.length || params[index] == 'V')
        }
    }
}