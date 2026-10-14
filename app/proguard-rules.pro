# kotlinx.serialization, WorkManager, Room und androidx.startup bringen eigene R8-Regeln mit.

# Glance erzeugt ActionCallbacks (actionRunCallback<…>()) per Reflection über den Klassennamen
# und den parameterlosen Konstruktor. Ohne diese Regel entfernt R8 beides und der
# Aktualisieren-Knopf im Widget tut in der Release-Version nichts.
-keep class * implements androidx.glance.appwidget.action.ActionCallback {
    public <init>();
}

# Glance merkt sich Widgets unter dem Klassennamen. Ein fester Name verhindert, dass das
# Widget nach einem App-Update kurz „vergessen“ wird, weil R8 die Klasse anders benannt hat.
-keepnames class * extends androidx.glance.appwidget.GlanceAppWidget
