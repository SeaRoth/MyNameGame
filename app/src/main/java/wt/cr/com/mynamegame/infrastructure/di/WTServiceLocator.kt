package wt.cr.com.mynamegame.infrastructure.di

class WTServiceLocator private constructor() {
    companion object {
        val locatorMap: MutableMap<Class<*>, Any> = mutableMapOf()

        fun <T> resolve(type: Class<out T>): T {
            return type.cast(locatorMap[type])
        }

        fun <T> put(type: Class<T>?, instance: T) {
            if (type == null) {
                throw NullPointerException("Type is null")
            }
            if (instance == null) {
                throw NullPointerException("Instance is null")
            }
            locatorMap.put(type, instance)
        }
    }

}
