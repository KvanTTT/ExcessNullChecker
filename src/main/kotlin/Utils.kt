class Utils {
    companion object {
        fun getParamsCount(s: String?): Int {
            if (s == null)
                return 0

            var paramsCount = s.count { c: Char -> c == ';' }
            if (paramsCount > 0)
                return paramsCount

            return if (s.contains("()")) 0 else 1
        }
    }
}